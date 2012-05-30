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
import javax.portlet.WindowState;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.portal.core.model.portal.Window;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import fr.toutatice.portail.api.menubar.MenubarItem;
import fr.toutatice.portail.api.statut.IStatutService;
import fr.toutatice.portail.api.urls.Link;
import fr.toutatice.portail.api.windows.PortalWindow;
import fr.toutatice.portail.api.windows.WindowFactory;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.core.DocumentFetchCommand;
import fr.toutatice.portail.cms.nuxeo.core.DocumentFetchPublishedCommand;
import fr.toutatice.portail.cms.nuxeo.core.NuxeoCommandServiceFactory;
import fr.toutatice.portail.cms.nuxeo.core.PortletErrorHandler;
import fr.toutatice.portail.cms.nuxeo.core.WysiwygParser;
import fr.toutatice.portail.cms.nuxeo.core.XSLFunctions;


import fr.toutatice.portail.cms.nuxeo.portlets.customizer.LinkHandler;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.CMSCustomizer;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.DefaultCMSCustomizer;
import fr.toutatice.portail.cms.nuxeo.portlets.thumbnail.ThumbnailServlet;
import fr.toutatice.portail.core.nuxeo.INuxeoService;
import fr.toutatice.portail.core.profils.ProfilBean;

/**
 * Portlet d'affichage d'un document Nuxeo
 */

public class ViewDocumentPortlet extends fr.toutatice.portail.cms.nuxeo.core.CMSPortlet {

	private static Log logger = LogFactory.getLog(ViewDocumentPortlet.class);
	
	private INuxeoService nuxeoService;
	
	
	// v 1.0.11 : ajout lien modification dans Nuxeo
	
	public static Link getAdministrationLink( NuxeoController ctx ) throws Exception {
		
		if( ctx.getRequest().getRemoteUser() == null)
			return null;
		
		
		String savedScope = ctx.getScope();

		try {
			// Scope user
			ctx.setScope(null);

			Document doc = (org.nuxeo.ecm.automation.client.jaxrs.model.Document) ctx
					.executeNuxeoCommand(new DocumentFetchLiveCommand(ctx.getCurrentDoc().getPath(), "Write"));

			if (doc != null) {
				return new Link(ctx.getNuxeoPublicBaseUri().toString() + "/nxdoc/default/" + doc.getId()
						+ "/view_documents", true);
			}
		}

		catch (Exception e) {

			if (e instanceof NuxeoException) {
				NuxeoException ne = (NuxeoException) e;

				if (ne.getErrorCode() == NuxeoException.ERROR_FORBIDDEN
						|| ne.getErrorCode() == NuxeoException.ERROR_NOTFOUND) {
					// On ne fait rien : le document n'existe pas ou je n'ai pas
					// les droits
				} else
					throw e;
			}

		}

		finally {
			ctx.setScope(savedScope);
		}

		return null;
		
	}

	public void init(PortletConfig config) throws PortletException {

		super.init(config);

	
		try	{
			// Enregistremennt des gestionnaires de liens et de template
			
			nuxeoService = (INuxeoService) getPortletContext().getAttribute("NuxeoService");
			if (nuxeoService == null) 
				throw new PortletException("Cannot start ViewDocumentPortlet portlet due to service unavailability");
			
			nuxeoService.registerLinkHandler(new LinkHandler(getPortletContext()));
			
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
			window.setProperty("pia.nuxeoPath", req.getParameter("nuxeoPath"));

			if (req.getParameter("scope") != null && req.getParameter("scope").length() > 0)
				window.setProperty("pia.cms.scope", req.getParameter("scope"));
			else if (window.getProperty("pia.cms.scope") != null)
				window.setProperty("pia.cms.scope", null);

			if ("1".equals(req.getParameter("onlyDescription")))
				window.setProperty("pia.document.onlyDescription", "1");
			else if (window.getProperty("pia.document.onlyDescription") != null)
				window.setProperty("pia.document.onlyDescription", null);
			
			if (! "1".equals(req.getParameter("showMetadatas")))
				window.setProperty("pia.cms.hideMetaDatas", "1");
			else if (window.getProperty("pia.cms.hideMetaDatas") != null)
				window.setProperty("pia.cms.hideMetaDatas", null);
						
			
			if ("1".equals(req.getParameter("displayLiveVersion")))
				window.setProperty("pia.cms.displayLiveVersion", "1");
			else if (window.getProperty("pia.cms.displayLiveVersion") != null)
				window.setProperty("pia.cms.displayLiveVersion", null);	

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
		String nuxeoPath = window.getProperty("pia.nuxeoPath");
		if (nuxeoPath == null)
			nuxeoPath = "";
		req.setAttribute("nuxeoPath", nuxeoPath);

		String onlyDescription = window.getProperty("pia.document.onlyDescription");
		req.setAttribute("onlyDescription", onlyDescription);
		
		String showMetadatas = "1";
		if( "1".equals(window.getProperty("pia.cms.hideMetaDatas")))
				showMetadatas = "0";
		req.setAttribute("showMetadatas", showMetadatas);		

		String scope = window.getProperty("pia.cms.scope");
		req.setAttribute("scope", scope);
		
		
		String displayLiveVersion = window.getProperty("pia.cms.displayLiveVersion");
		if (displayLiveVersion == null)
			displayLiveVersion = "";
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
				nuxeoPath = window.getProperty("pia.cms.uri");
				

				// logger.debug("doView "+ uid);

				if (nuxeoPath == null) {
					// WIndow parameter (back-office)
					nuxeoPath = window.getProperty("pia.nuxeoPath");
				}


				if (nuxeoPath != null) {

						NuxeoController ctx = new NuxeoController(request, response, getPortletContext());
							
						
						Document doc = null;
						

						if( "1".equals(window.getProperty("pia.cms.no_uri_proxy_conversion")) || ctx.isDisplayingLiveVersion() )
							doc = (org.nuxeo.ecm.automation.client.jaxrs.model.Document) ctx.executeNuxeoCommand(new DocumentFetchCommand(nuxeoPath));
						else
							doc = (org.nuxeo.ecm.automation.client.jaxrs.model.Document) ctx.executeNuxeoCommand(new DocumentFetchPublishedCommand(nuxeoPath));
		
						if (doc.getTitle() != null)
							response.setTitle(doc.getTitle());
						
						request.setAttribute("doc", doc);
						

						if (!"1".equals(window.getProperty("pia.document.onlyDescription")) || request.getWindowState().equals(WindowState.MAXIMIZED)) {

							/* transformation de la partie wysiwyg */

							String note = doc.getString("note:note", "");
							
//							synchronized (WysiwygParser.getInstance())
							{
							Transformer transformer =  WysiwygParser.getInstance().getTemplate().newTransformer();

							//v 1.0.11 : pb. des pices jointes dans le proxy
							ctx.setCurrentDoc(doc);
							

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

						request.setAttribute("onlyDescription", window.getProperty("pia.document.onlyDescription"));
						
						
						String showMetadatas = "1";
						if( "1".equals(window.getProperty("pia.cms.hideMetaDatas")))
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
