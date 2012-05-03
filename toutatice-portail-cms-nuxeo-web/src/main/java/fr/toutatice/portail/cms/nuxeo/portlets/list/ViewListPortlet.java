package fr.toutatice.portail.cms.nuxeo.portlets.list;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.portal.core.controller.command.mapper.URLFactory;
import org.jboss.portal.core.model.instance.InstanceURLFactory;
import org.jboss.portal.core.model.portal.PortalObjectPath;
import org.jboss.portal.core.model.portal.Window;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
import org.nuxeo.ecm.automation.client.jaxrs.model.PaginableDocuments;
import org.nuxeo.ecm.automation.client.jaxrs.model.PropertyMap;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import bsh.EvalError;
import bsh.Interpreter;

import fr.toutatice.portail.api.contexte.PortalControllerContext;
import fr.toutatice.portail.api.statut.IStatutService;
import fr.toutatice.portail.api.urls.IPortalUrlFactory;
import fr.toutatice.portail.api.urls.Link;
import fr.toutatice.portail.api.windows.PortalWindow;
import fr.toutatice.portail.api.windows.WindowFactory;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.PageSelectors;
import fr.toutatice.portail.cms.nuxeo.core.BinaryContent;
import fr.toutatice.portail.cms.nuxeo.core.CMSPortlet;
import fr.toutatice.portail.cms.nuxeo.core.DocumentFetchCommand;
import fr.toutatice.portail.cms.nuxeo.core.DocumentFetchPublishedCommand;
import fr.toutatice.portail.cms.nuxeo.core.NuxeoQueryFilter;
import fr.toutatice.portail.cms.nuxeo.core.PortletErrorHandler;
import fr.toutatice.portail.cms.nuxeo.core.ResourceUtil;
import fr.toutatice.portail.cms.nuxeo.core.WysiwygParser;
import fr.toutatice.portail.cms.nuxeo.core.XSLFunctions;

import fr.toutatice.portail.cms.nuxeo.portlets.bridge.Formater;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.CMSCustomizer;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.ListTemplate;


import fr.toutatice.portail.core.profils.ProfilBean;

/**
 * Portlet d'affichage d'un document Nuxeo
 */

public class ViewListPortlet extends CMSPortlet  {

	private static Log logger = LogFactory.getLog(ViewListPortlet.class);
	
	public static  Map<String, ListTemplate> getListTemplates() {
		List<ListTemplate> templatesList = CMSCustomizer.getListTemplates();
		Map<String, ListTemplate> templatesMap = new LinkedHashMap<String, ListTemplate>();
		for (ListTemplate template : templatesList)
			templatesMap.put(template.getKey(), template);
		return templatesMap;
	}
	
	
	
	
	

	public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
	throws PortletException, IOException {

		try {

			/* Génération Flux RSS */
			
			if ("rss".equals(resourceRequest.getParameter("type"))) {
				
				String cmsPath = resourceRequest.getParameter("pia.cms.path");
				
				/* Contexts initialization */

				NuxeoController ctx = new NuxeoController(resourceRequest, resourceResponse, getPortletContext());
				
				PortalControllerContext portalCtx = new PortalControllerContext(getPortletContext(), resourceRequest,resourceResponse);
			
				ctx.setContextualization(IPortalUrlFactory.CONTEXTUALIZATION_PORTAL);

				/* On détermine l'uid et le scope */

				String nuxeoRequest = null;

				PortalWindow window = WindowFactory.getWindow(resourceRequest);

				nuxeoRequest = window.getProperty("pia.nuxeoRequest");
				


				if ("beanShell".equals(window.getProperty("pia.requestInterpretor"))) {
					// Evaluation beanshell
					Interpreter i = new Interpreter();
					i.set("params", PageSelectors.decodeProperties(resourceRequest.getParameter("selectors")));
					i.set("request", resourceRequest);
					i.set("NXQLFormater", new NXQLFormater());
					i.set("path",  resourceRequest.getParameter("pia.cms.path"));					

					nuxeoRequest = (String) i.eval(nuxeoRequest);
				}


				/* Initialisation de la page courante et de la taille de la page */

				int pageSize = 10;

				String pageSizeAttributeName = "pia.cms.pageSizeMax";
				if (window.getProperty(pageSizeAttributeName) != null) {
					pageSize = Integer.parseInt(window.getProperty(pageSizeAttributeName));
				}

				String style = window.getProperty("pia.cms.style");
				if (style == null)
					style = CMSCustomizer.STYLE_NORMAL;

				/* Reinitialisation du numéro de page si changement de critères */

				String selectors = resourceRequest.getParameter("selectors");

				if (nuxeoRequest != null) {

					// Calcul de la taille de la page

					ListTemplate template = getListTemplates().get(style);
					if (template == null)
						template = getListTemplates().get(CMSCustomizer.STYLE_NORMAL);

					String schemas = getListTemplates().get(style).getSchemas();

					PaginableDocuments docs = (PaginableDocuments) ctx.executeNuxeoCommand(new ListCommand(
							nuxeoRequest, ctx.isDisplayingLiveVersion(), 0, pageSize, schemas));

					org.w3c.dom.Document document = RssGenerator.createDocument(ctx, portalCtx,  window.getProperty("pia.rssTitle"), docs, window.getProperty("pia.rssLinkRef"));
					
					
					/* Envoi du flux */

					TransformerFactory tFactory = TransformerFactory.newInstance();
					Transformer transformer = tFactory.newTransformer();
					DOMSource source = new DOMSource(document);
					StringWriter sw = new StringWriter();
					StreamResult result = new StreamResult(sw);
					transformer.transform(source, result);
					String xmlString = sw.toString();

					InputStream in = new ByteArrayInputStream(xmlString.getBytes());

					ResourceUtil.copy(in, resourceResponse.getPortletOutputStream(), 4096);

					resourceResponse.setContentType("application/rss+xml");

					resourceResponse.setProperty("Cache-Control", "max-age="
							+ resourceResponse.getCacheControl().getExpirationTime());

					resourceResponse.setProperty("Last-Modified", formatResourceLastModified());

				} else
					throw new IllegalArgumentException("No request defined for RSS");
			} else
				super.serveResource(resourceRequest, resourceResponse);

		} catch (Exception e) {
			throw new PortletException(e);

		}
	}
	
	
	public void processAction(ActionRequest req, ActionResponse res) throws IOException, PortletException {

		logger.debug("processAction ");

		if ("admin".equals(req.getPortletMode().toString()) && req.getParameter("modifierPrefs") != null) {

			PortalWindow window = WindowFactory.getWindow(req);
			window.setProperty("pia.nuxeoRequest", req.getParameter("nuxeoRequest"));
			
			
			if ("1".equals(req.getParameter("beanShell")))
				window.setProperty("pia.requestInterpretor", "beanShell");
			else if (window.getProperty("pia.requestInterpretor") != null)
				window.setProperty("pia.requestInterpretor", null);	

			
			if ("1".equals(req.getParameter("displayNuxeoRequest")))
				window.setProperty("pia.displayNuxeoRequest", "1");
			else if (window.getProperty("pia.displayNuxeoRequest") != null)
				window.setProperty("pia.displayNuxeoRequest", null);	


			if ("1".equals(req.getParameter("displayLiveVersion")))
				window.setProperty("pia.cms.displayLiveVersion", "1");
			else if (window.getProperty("pia.cms.displayLiveVersion") != null)
				window.setProperty("pia.cms.displayLiveVersion", null);	
			
			if (! "1".equals(req.getParameter("showMetadatas")))
				window.setProperty("pia.cms.hideMetaDatas", "1");
			else if (window.getProperty("pia.cms.hideMetaDatas") != null)
				window.setProperty("pia.cms.hideMetaDatas", null);
			

			
			if (req.getParameter("scope") != null && req.getParameter("scope").length() > 0)
				window.setProperty("pia.cms.scope", req.getParameter("scope"));
			else if (window.getProperty("pia.cms.scope") != null)
				window.setProperty("pia.cms.scope", null);
			
			if (req.getParameter("style") != null && req.getParameter("style").length() > 0)
				window.setProperty("pia.cms.style", req.getParameter("style"));
			else if (window.getProperty("pia.cms.style") != null)
				window.setProperty("pia.cms.style", null);
			
			// Taille de page
			int pageSize = 0;
			if (req.getParameter("pageSize") != null )	{
				try	{
					pageSize = Integer.parseInt(req.getParameter("pageSize"));
				} catch(Exception e){
					// Mal formatté
				}
			}
			
			if (pageSize > 0)
				window.setProperty("pia.cms.pageSize", Integer.toString(pageSize));
			else if (window.getProperty("pia.cms.pageSize") != null)
				window.setProperty("pia.cms.pageSize", null);
			
			
			// Taille de page max
			int pageSizeMax = 0;
			if (req.getParameter("pageSizeMax") != null )	{
				try	{
					pageSizeMax = Integer.parseInt(req.getParameter("pageSizeMax"));
				} catch(Exception e){
					// Mal formatté
				}
			}
			
			if (pageSizeMax > 0)
				window.setProperty("pia.cms.pageSizeMax", Integer.toString(pageSizeMax));
			else if (window.getProperty("pia.cms.pageSizeMax") != null)
				window.setProperty("pia.cms.pageSizeMax", null);			
	
				
			//Limite
			int maxItems = 0;
			if (req.getParameter("maxItems") != null )	{
				try	{
					maxItems = Integer.parseInt(req.getParameter("maxItems"));
				} catch(Exception e){
					// Mal formatté
				}
			}
			
			if (maxItems > 0)
				window.setProperty("pia.cms.maxItems", Integer.toString(maxItems));
			else if (window.getProperty("pia.cms.maxItems") != null)
				window.setProperty("pia.cms.maxItems", null);
			
			if (req.getParameter("permaLinkRef") != null && req.getParameter("permaLinkRef").length() > 0)
				window.setProperty("pia.permaLinkRef", req.getParameter("permaLinkRef"));
			else if (window.getProperty("pia.permaLinkRef") != null)
				window.setProperty("pia.permaLinkRef", null);
			
			if (req.getParameter("rssLinkRef") != null && req.getParameter("rssLinkRef").length() > 0)
				window.setProperty("pia.rssLinkRef", req.getParameter("rssLinkRef"));
			else if (window.getProperty("pia.rssLinkRef") != null)
				window.setProperty("pia.rssLinkRef", null);
			
			
			
			if (req.getParameter("rssTitle") != null && req.getParameter("rssTitle").length() > 0)
				window.setProperty("pia.rssTitle", req.getParameter("rssTitle"));
			else if (window.getProperty("pia.rssTitle") != null)
				window.setProperty("pia.rssTitle", null);
			


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
		
		try	{
	
		NuxeoController ctx = new NuxeoController(req, res, getPortletContext());

		
		PortletRequestDispatcher rd = null;

		PortalWindow window = WindowFactory.getWindow(req);
		
		
		String nuxeoRequest = window.getProperty("pia.nuxeoRequest");
		if (nuxeoRequest == null)
			nuxeoRequest = "";
		req.setAttribute("nuxeoRequest", nuxeoRequest);
		
		String displayLiveVersion = window.getProperty("pia.cms.displayLiveVersion");
		if (displayLiveVersion == null)
			displayLiveVersion = "";
		req.setAttribute("displayLiveVersion", displayLiveVersion);
		
		String showMetadatas = "1";
		if( "1".equals(window.getProperty("pia.cms.hideMetaDatas")))
				showMetadatas = "0";
		req.setAttribute("showMetadatas", showMetadatas);		

		

		String beanShell = "";
		String interpretor = window.getProperty("pia.requestInterpretor");
		if( "beanShell".equals( interpretor))
			beanShell = "1";
		req.setAttribute("beanShell", beanShell);

		
		req.setAttribute("displayNuxeoRequest", window.getProperty("pia.displayNuxeoRequest"));


		String scope = window.getProperty("pia.cms.scope");
		req.setAttribute("scope", scope);
		
		/* Styles d'affichage */
		
		Map<String, ListTemplate> templates = getListTemplates();
		req.setAttribute("templates", templates);
	
		String style = window.getProperty("pia.cms.style");
		if( style == null)
			style = CMSCustomizer.STYLE_NORMAL;
		req.setAttribute("style", style);
		
		String pageSize = window.getProperty("pia.cms.pageSize");
		req.setAttribute("pageSize", pageSize);
		
		String pageSizeMax = window.getProperty("pia.cms.pageSizeMax");
		req.setAttribute("pageSizeMax", pageSizeMax);		
		
		String maxItems = window.getProperty("pia.cms.maxItems");
		req.setAttribute("maxItems", maxItems);
		
		String permaLinkRef = window.getProperty("pia.permaLinkRef");
		if (permaLinkRef == null )
			permaLinkRef = "";
		req.setAttribute("permaLinkRef", permaLinkRef);
		

		String rssLinkRef = window.getProperty("pia.rssLinkRef");
		if (rssLinkRef == null )
			rssLinkRef = "";
		req.setAttribute("rssLinkRef", rssLinkRef);
	

		String rssTitle = window.getProperty("pia.rssTitle");
		if (rssTitle == null )
			rssTitle = "";
		req.setAttribute("rssTitle", rssTitle);
		
		req.setAttribute("ctx", ctx);
		
		rd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/liste/admin.jsp");
		rd.include(req, res);
		}
		
		catch (Exception e) {
			if( ! (e instanceof PortletException))
			throw new PortletException(e);
		}

	}

	@SuppressWarnings("unchecked")
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException,
			PortletSecurityException, IOException {
	

		logger.debug("doView");

		try {

			response.setContentType("text/html");
		

	
			/* On détermine l'uid et le scope */

			String nuxeoRequest = null;

			PortalWindow window = WindowFactory.getWindow(request);
				
			nuxeoRequest = window.getProperty("pia.nuxeoRequest");
			
			
			if ("beanShell".equals(window.getProperty("pia.requestInterpretor")))	{
				// Evaluation beanshell
				Interpreter i = new Interpreter();
				i.set("params", PageSelectors.decodeProperties(request.getParameter("selectors")));
				i.set("path",  request.getParameter("pia.cms.path"));
				i.set("request", request);
				i.set("NXQLFormater", new NXQLFormater());
				

				nuxeoRequest = (String)  i.eval( nuxeoRequest);
			}
			
			/* Filtre pour sélectionner uniquement les version publiées */

			int maxItems = -1;
			if (window.getProperty("pia.cms.maxItems") != null)
				maxItems = Integer.parseInt(window.getProperty("pia.cms.maxItems"));
			
			
			/* Initialisation de la page courante et de la taille de la page*/ 
			  
			
			 int pageSize = -1;
			 int currentPage = 0;
			
			 
			 String pageSizeAttributeName = "pia.cms.pageSize";
			 if( WindowState.MAXIMIZED.equals(request.getWindowState()))
				 pageSizeAttributeName = "pia.cms.pageSizeMax";
			
			 if (window.getProperty(pageSizeAttributeName) != null )		{
				pageSize = Integer.parseInt(window.getProperty(pageSizeAttributeName));
				
				String currentState = request.getParameter("currentState");
				String sCurrentPage =  request.getParameter("currentPage");

				// La page est réinitialisée si on change de mode
				if( sCurrentPage != null && request.getWindowState().toString().equals(currentState))
					currentPage = Integer.parseInt(sCurrentPage);
			}
			
			
			
			/* Reinitialisation du numéro de page si changement de critères */
			
			String selectors = request.getParameter("selectors");
			String lastSelectors = request.getParameter("lastSelectors");
			
			if( (( selectors != null)	&& ( !selectors.equals(lastSelectors)))
					|| ( selectors == null)	&& ( lastSelectors != null)
				)
					currentPage = 0;

			
			
			String style = window.getProperty("pia.cms.style");
			if( style == null)
				style = CMSCustomizer.STYLE_NORMAL;



			if (nuxeoRequest != null) {



						NuxeoController ctx = new NuxeoController(request, response, getPortletContext());

						
						// Calcul de la taille de la page
						
						int requestPageSize = 100;
						if( pageSize != -1)	
							requestPageSize = pageSize;
						
						// Pas d'autre solution que de servir de la pagination pour limiter le nombre d'items ...
						// limite de la taille de la page par le nombre d'items (sur la première page)
						if( maxItems != -1 && currentPage == 0)	{
							requestPageSize = Math.min( requestPageSize, maxItems);
						}
						
						ListTemplate template = getListTemplates().get(style);
						if( template == null)
							template = getListTemplates().get(CMSCustomizer.STYLE_NORMAL);
						
						String schemas = getListTemplates().get(style).getSchemas();

						
						PaginableDocuments docs = (PaginableDocuments) ctx.executeNuxeoCommand(new ListCommand(nuxeoRequest,ctx.isDisplayingLiveVersion(), currentPage, requestPageSize, schemas));

	
						
						int nbPages = 0;
					
						if( pageSize != -1)	{
							if( maxItems == -1)	
								nbPages = ((docs.getTotalSize() - 1) / pageSize ) + 1;
							else	{
								nbPages = ((Math.min( docs.getTotalSize(), maxItems)  - 1) / pageSize ) + 1;
								
								// Attention à la dernière page ...
								// Elle peut contenir trop d'éléments ...
								
								if( docs.size() < (currentPage + 1) * requestPageSize )	{
									
									int pageLimit  = Math.max(0, maxItems - currentPage  * requestPageSize);
									
									while (docs.size() > pageLimit)
										docs.remove(docs.size() - 1);
								}
		
							}
						}
						
						request.setAttribute("docs", docs);
						request.setAttribute("ctx", ctx);

						request.setAttribute("currentPage", currentPage);						
						
						request.setAttribute("nbPages", nbPages );
						
						if( "1".equals(window.getProperty("pia.displayNuxeoRequest")))
								request.setAttribute("nuxeoRequest", nuxeoRequest );
						
						
						request.setAttribute("selectors", request.getParameter("selectors") );

						
						request.setAttribute("style", style);
						
						
						String permaLinkRef = window.getProperty("pia.permaLinkRef");
						if( permaLinkRef != null)	{
							Map<String, String> publicParams = new HashMap<String, String>();
							if( selectors != null)
								publicParams.put("selectors", selectors);
							String permLinkType =  IPortalUrlFactory.PERM_LINK_TYPE_PAGE;
							if( request.getParameter("pia.cms.path") != null)	{
								permLinkType = IPortalUrlFactory.PERM_LINK_TYPE_CMS;
								permaLinkRef = null;
							}
							
							String permaLinkURL = ctx.getPortalUrlFactory().getPermaLink(new PortalControllerContext(getPortletContext(), request,
										response), permaLinkRef, publicParams, request.getParameter("pia.cms.path"), permLinkType);
							request.setAttribute("permaLinkURL", permaLinkURL);
						}
					
						
						String rssLinkRef = window.getProperty("pia.rssLinkRef");
						if( rssLinkRef != null)	{
							Map<String, String> publicParams = new HashMap<String, String>();
							if( selectors != null)
								publicParams.put("selectors", selectors);
							
							
								String rssLinkURL = ctx.getPortalUrlFactory().getPermaLink(new PortalControllerContext(getPortletContext(), request,
										response), rssLinkRef, publicParams, request.getParameter("pia.cms.path"), IPortalUrlFactory.PERM_LINK_TYPE_RSS);
								
								request.setAttribute("rssLinkURL", rssLinkURL);
						}
						
						

						getPortletContext().getRequestDispatcher("/WEB-INF/jsp/liste/view.jsp").include(request,
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
