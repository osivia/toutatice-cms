package fr.toutatice.portail.cms.nuxeo.portlets.document;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSecurityException;
import javax.portlet.RenderMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.portal.core.model.portal.Window;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.menubar.MenubarItem;
import org.osivia.portal.api.status.IStatusService;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.api.urls.Link;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.ICMSServiceLocator;
import org.osivia.portal.core.profils.ProfilBean;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.core.DocumentFetchCommand;
import fr.toutatice.portail.cms.nuxeo.core.DocumentFetchPublishedCommand;
import fr.toutatice.portail.cms.nuxeo.core.NuxeoCommandServiceFactory;
import fr.toutatice.portail.cms.nuxeo.core.PortletErrorHandler;
import fr.toutatice.portail.cms.nuxeo.core.WysiwygParser;
import fr.toutatice.portail.cms.nuxeo.core.XSLFunctions;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.CMSCustomizer;


import fr.toutatice.portail.cms.nuxeo.portlets.service.CMSService;
import fr.toutatice.portail.cms.nuxeo.portlets.thumbnail.ThumbnailServlet;
import fr.toutatice.portail.core.nuxeo.INuxeoService;

/**
 * Portlet d'affichage d'un document Nuxeo
 */

public class ViewDocumentPortlet extends fr.toutatice.portail.cms.nuxeo.core.CMSPortlet {

	private static Log logger = LogFactory.getLog(ViewDocumentPortlet.class);
	
	private INuxeoService nuxeoService;
	
	public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
	throws PortletException, IOException {

		try {
			// Redirection sur lien contextuel
			
			if ("link".equals(resourceRequest.getParameter("type"))) {

				NuxeoController ctx = new NuxeoController(resourceRequest, null, getPortletContext());

				String id = resourceRequest.getResourceID();

				Document doc = (org.nuxeo.ecm.automation.client.jaxrs.model.Document) ctx
						.executeNuxeoCommand(new DocumentFetchCommand(id));

				resourceResponse.setProperty(ResourceResponse.HTTP_STATUS_CODE,
						String.valueOf(HttpServletResponse.SC_MOVED_TEMPORARILY));
				resourceResponse.setProperty("Location", doc.getString("clink:link"));
				resourceResponse.getPortletOutputStream().close();

			}	else
				super.serveResource(resourceRequest, resourceResponse);

		} catch (NuxeoException e) {
			serveResourceException(resourceRequest, resourceResponse, e);
		} catch (Exception e) {
			throw new PortletException(e);

		}
	}

	
	

	public void init(PortletConfig config) throws PortletException {

		super.init(config);

	
		try	{
			// Enregistremennt des gestionnaires de liens et de template
			
			nuxeoService = (INuxeoService) getPortletContext().getAttribute("NuxeoService");
			if (nuxeoService == null) 
				throw new PortletException("Cannot start ViewDocumentPortlet portlet due to service unavailability");
			
			CMSCustomizer customizer = new CMSCustomizer(getPortletContext());
			nuxeoService.registerCMSCustomizer(customizer);
			
			CMSService CMSservice = new CMSService(getPortletContext());
			ICMSServiceLocator cmsLocator = Locator.findMBean(ICMSServiceLocator.class, "osivia:service=CmsServiceLocator");
			cmsLocator.register(CMSservice);

			
			customizer.setCMSService(CMSservice);
			CMSservice.setCustomizer(customizer);
			

			
			// v1.0.16
			ThumbnailServlet.setPortletContext(getPortletContext());
			
			

		} catch( Exception e)	{
			throw new PortletException( e);
		}
		

	}


	public void processAction(ActionRequest req, ActionResponse res) throws IOException, PortletException {

		logger.debug("processAction ");

		if ("admin".equals(req.getPortletMode().toString()) && req.getParameter("modifierPrefs") != null) {

			PortalWindow window = WindowFactory.getWindow(req);
			window.setProperty("osivia.nuxeoPath", req.getParameter("nuxeoPath"));

			/*
			if (req.getParameter("scope") != null && req.getParameter("scope").length() > 0)
				window.setProperty("osivia.cms.scope", req.getParameter("scope"));
			else if (window.getProperty("osivia.cms.scope") != null)
				window.setProperty("osivia.cms.scope", null);
				*/

			if ("1".equals(req.getParameter("onlyDescription")))
				window.setProperty("osivia.document.onlyDescription", "1");
			else if (window.getProperty("osivia.document.onlyDescription") != null)
				window.setProperty("osivia.document.onlyDescription", null);
			
			if (! "1".equals(req.getParameter("showMetadatas")))
				window.setProperty("osivia.cms.hideMetaDatas", "1");
			else if (window.getProperty("osivia.cms.hideMetaDatas") != null)
				window.setProperty("osivia.cms.hideMetaDatas", null);
						
			
			
			if (req.getParameter("displayLiveVersion") != null && req.getParameter("displayLiveVersion").length() > 0)
				window.setProperty("osivia.cms.displayLiveVersion", req.getParameter("displayLiveVersion"));
			else if (window.getProperty("osivia.cms.displayLiveVersion") != null)
				window.setProperty("osivia.cms.displayLiveVersion", null);
			
			
			res.setPortletMode(PortletMode.VIEW);
			res.setWindowState(WindowState.NORMAL);
		}

		if ("admin".equals(req.getPortletMode().toString()) && req.getParameter("annuler") != null) {

			res.setPortletMode(PortletMode.VIEW);
			res.setWindowState(WindowState.NORMAL);
		}
	}

	

	@RenderMode(name = "admin")
	public void doAdmin(RenderRequest req, RenderResponse res) throws IOException, PortletException {

		res.setContentType("text/html");
		NuxeoController ctx = new NuxeoController(req, res, getPortletContext());

		PortletRequestDispatcher rd = null;

		PortalWindow window = WindowFactory.getWindow(req);
		String nuxeoPath = window.getProperty("osivia.nuxeoPath");
		if (nuxeoPath == null)
			nuxeoPath = "";
		req.setAttribute("nuxeoPath", nuxeoPath);

		String onlyDescription = window.getProperty("osivia.document.onlyDescription");
		req.setAttribute("onlyDescription", onlyDescription);
		
		String showMetadatas = "1";
		if( "1".equals(window.getProperty("osivia.cms.hideMetaDatas")))
				showMetadatas = "0";
		req.setAttribute("showMetadatas", showMetadatas);		

		/*
		String scope = window.getProperty("osivia.cms.scope");
		req.setAttribute("scope", scope);
		*/
		
		String displayLiveVersion = window.getProperty("osivia.cms.displayLiveVersion");
		req.setAttribute("displayLiveVersion", displayLiveVersion);
		
		req.setAttribute("ctx", ctx);

		rd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/document/admin.jsp");
		rd.include(req, res);


	}

	@SuppressWarnings("unchecked")
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException,
			PortletSecurityException, IOException {

		logger.debug("doView");

		try {

			response.setContentType("text/html");

			PortalWindow window = WindowFactory.getWindow(request);

			/* On détermine l'uid et le scope */

			String nuxeoPath = null;


				// portal window parameter (appels dynamiques depuis le portail)
				nuxeoPath = window.getProperty("osivia.cms.uri");
				

				// logger.debug("doView "+ uid);

				if (nuxeoPath == null) {
					// WIndow parameter (back-office)
					nuxeoPath = window.getProperty("osivia.nuxeoPath");
				}


				if (nuxeoPath != null) {

						NuxeoController ctx = new NuxeoController(request, response, getPortletContext());
						
						nuxeoPath = ctx.getComputedPath(nuxeoPath);
							
						
						
						//TODO : 
						// gestion des erreurs
						//gestion du no-uri-proxy_conversion : a quoi sert-il ?
	

						Document doc = ctx.fetchDocument(nuxeoPath);
						

						/*
						if( "1".equals(window.getProperty("osivia.cms.no_uri_proxy_conversion")) || ctx.isDisplayingLiveVersion() )
							doc = (org.nuxeo.ecm.automation.client.jaxrs.model.Document) ctx.executeNuxeoCommand(new DocumentFetchCommand(nuxeoPath));
						else
							doc = (org.nuxeo.ecm.automation.client.jaxrs.model.Document) ctx.executeNuxeoCommand(new DocumentFetchPublishedCommand(nuxeoPath));
							*/
		
						if (doc.getTitle() != null)
							response.setTitle(doc.getTitle());
						

						
						
						request.setAttribute("doc", doc);
						

						if (!"1".equals(window.getProperty("osivia.document.onlyDescription")) || request.getWindowState().equals(WindowState.MAXIMIZED)) {

							/* transformation de la partie wysiwyg */

							String note = doc.getString("note:note", "");
							
//							synchronized (WysiwygParser.getInstance())
							{
							Transformer transformer =  WysiwygParser.getInstance().getTemplate().newTransformer();

							//v 1.0.11 : pb. des pices jointes dans le proxy
							ctx.setCurrentDoc(doc);
							
							
							// Insert standard menu bar for content item
							ctx.insertContentMenuBarItems();
							

							transformer.setParameter("bridge", new XSLFunctions(ctx));
							OutputStream output = new ByteArrayOutputStream();
							XMLReader parser = WysiwygParser.getInstance().getParser();
							transformer.transform(new SAXSource(parser, new InputSource(new StringReader(note))),
									new StreamResult(output));


							String noteTransformee = output.toString();
							request.setAttribute("note", noteTransformee);
							}				
							
							
							
						}

						request.setAttribute("ctx", ctx);
						
						String description = doc.getString("dc:description", "");
						request.setAttribute("description", description);

						request.setAttribute("onlyDescription", window.getProperty("osivia.document.onlyDescription"));
						
						
						String showMetadatas = "1";
						if( "1".equals(window.getProperty("osivia.cms.hideMetaDatas")))
							showMetadatas = "0";
						request.setAttribute("showMetadatas", showMetadatas);
						
						
						
					

						getPortletContext().getRequestDispatcher("/WEB-INF/jsp/document/view.jsp").include(request,
								response);
				} else {
					response.setContentType("text/html");
					response.getWriter().print("<h2>Document non défini</h2>");
					response.getWriter().close();
					return;
				}
			

		} catch( NuxeoException e){
			PortletErrorHandler.handleGenericErrors(response, e);
		}
		catch (Exception e) {
			if( ! (e instanceof PortletException))
				throw new PortletException(e);
		}

		logger.debug("doView end");

	}



}
