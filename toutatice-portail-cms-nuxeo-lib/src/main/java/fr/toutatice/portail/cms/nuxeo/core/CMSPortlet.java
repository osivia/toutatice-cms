package fr.toutatice.portail.cms.nuxeo.core;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
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
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.portal.core.model.portal.Window;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;

import fr.toutatice.portail.api.cache.services.ICacheService;
import fr.toutatice.portail.api.statut.IStatutService;
import fr.toutatice.portail.api.urls.IPortalUrlFactory;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.core.profils.IProfilManager;

/**
 * Portlet d'affichage d'un document Nuxeo
 */

public class CMSPortlet extends GenericPortlet {

	protected static Log logger = LogFactory.getLog(CMSPortlet.class);



	public void init(PortletConfig config) throws PortletException {

		super.init(config);

	
		try	{
			new NuxeoController(getPortletContext()).startNuxeoService();
			NuxeoCommandServiceFactory.startNuxeoCommandService( getPortletContext());
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

	public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
			throws PortletException, IOException {

		try {

			if (serveResourceByCache(resourceRequest, resourceResponse))
				return;



			// Redirection
			if ("link".equals(resourceRequest.getParameter("type"))) {

				NuxeoController ctx = new NuxeoController(resourceRequest, null, getPortletContext());


				String id = resourceRequest.getResourceID();

				Document doc = (org.nuxeo.ecm.automation.client.jaxrs.model.Document) ctx
						.executeNuxeoCommand(new DocumentFetchCommand(id));

				String html = "";

				if (doc.getType().equals("ContextualLink")) {
					html += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
					html += "<html xmlns=\"http://www.w3.org/1999/xhtml\">";
					html += "<head>";
					html += "<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" />";
					html += "<meta http-equiv=\"refresh\" content=\"0; url=" + doc.getString("clink:link") + "\" />";
					html += "<title>Redirection</title>";
					html += "<meta name=\"robots\" content=\"noindex,follow\" />";
					html += "</head>";
				}
				ResourceUtil.copy(new ByteArrayInputStream(html.getBytes()), resourceResponse.getPortletOutputStream(),
						4096);
			}

			if ("file".equals(resourceRequest.getParameter("type"))) {

				String docPath = resourceRequest.getParameter("docPath");
				String fieldName = resourceRequest.getParameter("fieldName");

				NuxeoController ctx = new NuxeoController(resourceRequest, null, getPortletContext());



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

		} catch (Exception e) {
			throw new PortletException(e);

		}
	}

}
