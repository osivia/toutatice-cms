package fr.toutatice.portail.cms.nuxeo.core;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.ResourceURL;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.portal.core.model.portal.Window;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;

import fr.toutatice.portail.api.cache.services.ICacheService;
import fr.toutatice.portail.api.statut.IStatutService;
import fr.toutatice.portail.api.urls.IPortalUrlFactory;
import fr.toutatice.portail.api.urls.Link;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.core.nuxeo.INuxeoService;
import fr.toutatice.portail.core.profils.IProfilManager;

/**
 * Portlet d'affichage d'un document Nuxeo
 */

public class CMSPortlet extends GenericPortlet {

	protected static Log logger = LogFactory.getLog(CMSPortlet.class);

	INuxeoService nuxeoNavigationService;
	
	public INuxeoService getNuxeoNavigationService() throws Exception {

		if (nuxeoNavigationService == null)
			nuxeoNavigationService = (INuxeoService) getPortletContext().getAttribute("NuxeoService");

		return nuxeoNavigationService;

	}
	

	public void init(PortletConfig config) throws PortletException {

		super.init(config);

	
		try	{
			new NuxeoController(getPortletContext()).startNuxeoService();

		} catch( Exception e)	{
			throw new PortletException( e);
		}
		

	}

	@Override
	public void destroy() {

		try {
			// Destruction des threads éventuels
			new NuxeoController(getPortletContext()).stopNuxeoService();
			
		} catch (Exception e) {
			logger.error(e);
		}

		super.destroy();
	}

	public String formatResourceLastModified() {

		SimpleDateFormat inputFormater = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
		inputFormater.setTimeZone(TimeZone.getTimeZone("GMT"));
		return inputFormater.format(new Date(System.currentTimeMillis()));
	}

	public boolean isResourceExpired(String sOriginalDate, ResourceResponse resourceResponse) {

		boolean isExpired = true;

		if (sOriginalDate != null) {

			SimpleDateFormat inputFormater = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
			inputFormater.setTimeZone(TimeZone.getTimeZone("GMT"));
			try {
				Date originalDate = inputFormater.parse(sOriginalDate);
				if (System.currentTimeMillis() < originalDate.getTime()
						+ resourceResponse.getCacheControl().getExpirationTime() * 1000)
					isExpired = false;
			} catch (Exception e) {

			}
		}

		return isExpired;
	}

	public boolean serveResourceByCache(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
			throws PortletException, IOException {

		String sOriginalDate = resourceRequest.getProperty("if-modified-since");
		if (sOriginalDate == null)
			sOriginalDate = resourceRequest.getProperty("If-Modified-Since");

		if (!isResourceExpired(sOriginalDate, resourceResponse)) { // validation
																	// request

			// resourceResponse.setContentLength(0);
			resourceResponse.setProperty(ResourceResponse.HTTP_STATUS_CODE,
					String.valueOf(HttpServletResponse.SC_NOT_MODIFIED));
			resourceResponse.setProperty("Last-Modified", sOriginalDate);

			// au moins un caractère
			// resourceResponse.getPortletOutputStream().write(' ');
			resourceResponse.getPortletOutputStream().close();

			return true;
		}

		return false;
	}
	
	// v1.0.14 : recherche d'un document lié
	// Par défaut sur scope courant, sinon sur scope user
	
	public Document fetchLinkedDocument( NuxeoController ctx, String docPath)	throws Exception {
		
		String decodedPath = URLDecoder.decode(docPath, "UTF-8");
		Document doc = null;
		
		try	{
			
			doc = (org.nuxeo.ecm.automation.client.jaxrs.model.Document) ctx
			.executeNuxeoCommand(new DocumentFetchPublishedCommand(decodedPath));			
		} catch( NuxeoException e){
			 if( e.getErrorCode() == NuxeoException.ERROR_FORBIDDEN)	{
				 
				 if( ctx.getScope() != null){
				 	// Unreachable by profil, get user scope
					 
					 //TODO 1.1 : mettre le scope du user
				 
					ctx.setScope(null);	

					doc = (org.nuxeo.ecm.automation.client.jaxrs.model.Document) ctx
								.executeNuxeoCommand(new DocumentFetchPublishedCommand(decodedPath));
				 }

			}
			 else throw e;
		}
		
		return doc;
		
	}
	
	
	
	protected void serveResourceException(ResourceRequest resourceRequest, ResourceResponse resourceResponse,
			NuxeoException e) throws PortletException, IOException {

		if (e.getErrorCode() == NuxeoException.ERROR_NOTFOUND) {

			URL url = new URL("http", resourceRequest.getServerName(), resourceRequest.getServerPort(),
					System.getProperty("error.default_page_uri"));
			String sUrl = url.toString() + "?httpCode=" + HttpServletResponse.SC_NOT_FOUND;

			resourceResponse.setProperty(ResourceResponse.HTTP_STATUS_CODE,
					String.valueOf(HttpServletResponse.SC_MOVED_TEMPORARILY));
			resourceResponse.setProperty("Location", sUrl);
			resourceResponse.getPortletOutputStream().close();

			String message = "Resource CMSPortlet " + resourceRequest.getParameterMap() + " not found (error 404).";

			logger.error(message);
		}

		else if (e.getErrorCode() == NuxeoException.ERROR_FORBIDDEN) {

			URL url = new URL("http", resourceRequest.getServerName(), resourceRequest.getServerPort(),
					System.getProperty("error.default_page_uri"));
			String sUrl = url.toString() + "?httpCode=" + HttpServletResponse.SC_FORBIDDEN;

			resourceResponse.setProperty(ResourceResponse.HTTP_STATUS_CODE,
					String.valueOf(HttpServletResponse.SC_MOVED_TEMPORARILY));
			resourceResponse.setProperty("Location", sUrl);
			resourceResponse.getPortletOutputStream().close();

		} else
			throw e;
	}

	public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
			throws PortletException, IOException {
		
	

		try {

			if (serveResourceByCache(resourceRequest, resourceResponse))
				return;

			

			// v2 : plus utilisée - on se branche sur le CMS classique
		
			
			/*

			// Redirection par path de document
			
			if ("documentLink".equals(resourceRequest.getParameter("type"))) {

				NuxeoController ctx = new NuxeoController(resourceRequest, null, getPortletContext());


				String docPath = resourceRequest.getParameter("docPath");
				docPath = URLDecoder.decode(docPath, "UTF-8");
				
				
				
				
				
				
				// Liens vers un document qui n'a pas été lu (accès direct par le path)
				// On change de parcours de publication				
				// Le scope peut être insuffisant en terme de droit 
				//  >> on supprime le scope
				// V 1.0.14 : suppression
				//String scope = ctx.getScope();
				//ctx.setScope(null);
				
				// l'affichage des  méta-données n'est pas propagé non plus
				// 
				String hideMetadatas = ctx.getHideMetaDatas();
				ctx.setHideMetaDatas(null);
				
				// V 1.0.14 
				Document doc = fetchLinkedDocument(ctx, docPath);

				
				String url = null;
				
				if ("ContextualLink".equals(doc.getType()))	{
					url = doc.getString("clink:link");
				} else	{
					Link link = ctx.getLink(doc);
					url = link.getUrl();
					
				}
				
				// On remet le scope
				// V 1.0.14 : suppression
				//ctx.setScope(scope);
				ctx.setHideMetaDatas(hideMetadatas);
				
				
				// To keep historic
				if( resourceRequest.getParameter("pageMarker") != null)	{
					url = url.replaceAll("/pagemarker/([0-9]*)/","/pagemarker/"+resourceRequest.getParameter("pageMarker")+"/");
				}
				
				resourceResponse.setProperty(ResourceResponse.HTTP_STATUS_CODE,
						String.valueOf(HttpServletResponse.SC_MOVED_TEMPORARILY));
				resourceResponse.setProperty("Location", url);

				resourceResponse.getPortletOutputStream().close();

	
				
			}
			
			*/
			

			// Redirection
			if ("link".equals(resourceRequest.getParameter("type"))) {

				NuxeoController ctx = new NuxeoController(resourceRequest, null, getPortletContext());


				String id = resourceRequest.getResourceID();

				Document doc = (org.nuxeo.ecm.automation.client.jaxrs.model.Document) ctx
						.executeNuxeoCommand(new DocumentFetchCommand(id));
				

				resourceResponse.setProperty(ResourceResponse.HTTP_STATUS_CODE,
						String.valueOf(HttpServletResponse.SC_MOVED_TEMPORARILY));
				resourceResponse.setProperty("Location", doc.getString("clink:link"));
				resourceResponse.getPortletOutputStream().close();	
				

			}

			// Téléchargement d'un fichier présent dans un document externe
			if ("file".equals(resourceRequest.getParameter("type"))) {

				String docPath = resourceRequest.getParameter("docPath");
				String fieldName = resourceRequest.getParameter("fieldName");

				NuxeoController ctx = new NuxeoController(resourceRequest, null, getPortletContext());

				// V 1.0.19 
				if( !"1".equals( resourceRequest.getParameter("displayLiveVersion")))	{
					Document doc = fetchLinkedDocument(ctx, docPath);
					docPath = doc.getPath();
				}
				
				
				BinaryContent content = (BinaryContent) ResourceUtil.getFileContent(ctx, docPath, fieldName);

				// Les headers doivent être positionnées avant la réponse
				resourceResponse.setContentType(content.getMimeType());
				resourceResponse.setProperty("Content-Disposition", "attachment; filename=\"" + content.getName() + "\"");

				ResourceUtil.copy(new FileInputStream(content.getFile()), resourceResponse.getPortletOutputStream(),
						4096);
					

				resourceResponse.setProperty("Cache-Control", "max-age="
						+ resourceResponse.getCacheControl().getExpirationTime());
				
				resourceResponse.setProperty("Last-Modified", formatResourceLastModified());

				

			}

			if ("attachedFile".equals(resourceRequest.getParameter("type"))) {

				// Gestion d'un cache global

				String docPath = resourceRequest.getParameter("docPath");
				String fileIndex = resourceRequest.getParameter("fileIndex");

				NuxeoController ctx = new NuxeoController(resourceRequest, null, getPortletContext());
	

				BinaryContent content = ResourceUtil.getBinaryContent(ctx, docPath, fileIndex);

				// Les headers doivent être positionnées avant la réponse
				resourceResponse.setContentType(content.getMimeType());
				resourceResponse.setProperty("Content-Disposition", "attachment; filename=" + content.getName() + "");

				ResourceUtil.copy(new FileInputStream(content.getFile()), resourceResponse.getPortletOutputStream(),
						4096);
				
				
				resourceResponse.setProperty("Cache-Control", "max-age="
						+ resourceResponse.getCacheControl().getExpirationTime());
				resourceResponse.setProperty("Last-Modified", formatResourceLastModified());
			}
			
			
			
			
			if ("attachedPicture".equals(resourceRequest.getParameter("type"))) {


				String docPath = resourceRequest.getParameter("docPath");
				String pictureIndex = resourceRequest.getParameter("pictureIndex");

				NuxeoController ctx = new NuxeoController(resourceRequest, null, getPortletContext());
	
				BinaryContent content = ResourceUtil.getInternalPictureContent(ctx, docPath, pictureIndex);

				// Les headers doivent être positionnées avant la réponse
				resourceResponse.setContentType(content.getMimeType());
				resourceResponse.setProperty("Content-Disposition", "attachment; filename=" + content.getName() + "");

				ResourceUtil.copy(new FileInputStream(content.getFile()), resourceResponse.getPortletOutputStream(),
						4096);
				
				
				resourceResponse.setProperty("Cache-Control", "max-age="
						+ resourceResponse.getCacheControl().getExpirationTime());
				resourceResponse.setProperty("Last-Modified", formatResourceLastModified());
			}
			
			if ("picture".equals(resourceRequest.getParameter("type"))) {
				

				// Gestion d'un cache global

				String docPath = resourceRequest.getParameter("docPath");
				String content = resourceRequest.getParameter("content");

				NuxeoController ctx = new NuxeoController(resourceRequest, null, getPortletContext());
	

				
				// V 1.0.19 
				if( !"1".equals( resourceRequest.getParameter("displayLiveVersion")))	{
					Document doc = fetchLinkedDocument(ctx, docPath);
					docPath = doc.getPath();
				}
	
				BinaryContent picture = ResourceUtil.getPictureContent(ctx, docPath, content);

				// Les headers doivent être positionnées avant la réponse
				resourceResponse.setContentType(picture.getMimeType());
				resourceResponse.setProperty("Content-Disposition", "attachment; filename=" + picture.getName() + "");

				ResourceUtil.copy(new FileInputStream(picture.getFile()), resourceResponse.getPortletOutputStream(),
						4096);
				
				
				resourceResponse.setProperty("Cache-Control", "max-age="
						+ resourceResponse.getCacheControl().getExpirationTime());
				resourceResponse.setProperty("Last-Modified", formatResourceLastModified());
			}
			
			
		} catch( NuxeoException e){
			serveResourceException(  resourceRequest,  resourceResponse, e);
				
		}	

	    catch (Exception e) {
			throw new PortletException(e);

		}
	}

}
