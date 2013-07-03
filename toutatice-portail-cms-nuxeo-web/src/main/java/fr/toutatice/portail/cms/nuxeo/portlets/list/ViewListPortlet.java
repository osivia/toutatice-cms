package fr.toutatice.portail.cms.nuxeo.portlets.list;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
import org.nuxeo.ecm.automation.client.jaxrs.model.PaginableDocuments;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.context.ControllerContextAdapter;

import bsh.Interpreter;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.PageSelectors;
import fr.toutatice.portail.cms.nuxeo.core.CMSPortlet;
import fr.toutatice.portail.cms.nuxeo.core.PortletErrorHandler;
import fr.toutatice.portail.cms.nuxeo.core.ResourceUtil;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.CMSCustomizer;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.FragmentType;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.ListTemplate;

/**
 * Portlet d'affichage d'un document Nuxeo
 */

public class ViewListPortlet extends CMSPortlet {

	private static Log logger = LogFactory.getLog(ViewListPortlet.class);

	public static  Map<String, ListTemplate> getListTemplates() {
		List<ListTemplate> templatesList = CMSCustomizer.getListTemplates();
		Map<String, ListTemplate> templatesMap = new LinkedHashMap<String, ListTemplate>();
		for (ListTemplate template : templatesList)
			templatesMap.put(template.getKey(), template);
		return templatesMap;
	}

	
	public ListTemplate getCurrentTemplate( PortalWindow window)	{
		
		String style = window.getProperty("osivia.cms.style");
		if (style == null)
			style = CMSCustomizer.STYLE_NORMAL;

		ListTemplate template = getListTemplates().get(style);
	
		if (template == null)
			template = getListTemplates().get(CMSCustomizer.STYLE_NORMAL);
		
		return template;
	}
	
	public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
	throws PortletException, IOException {

		try {

			/* Génération Flux RSS */
		
			
			/* pour tests
			if( "zoom".equals(resourceRequest.getResourceID()))	{
				
				NuxeoController ctx = new NuxeoController(resourceRequest, resourceResponse, getPortletContext());
				
				Document doc = ctx.fetchDocument(resourceRequest.getParameter("docId"));
				
				resourceRequest.setAttribute("doc", doc);
				resourceRequest.setAttribute("ctx", ctx);
				
				getPortletContext().getRequestDispatcher("/WEB-INF/jsp/liste/zoom.jsp").include(resourceRequest, resourceResponse);
			}
			*/
			
			
			if ("rss".equals(resourceRequest.getParameter("type"))) {
				
			
				/* Contexts initialization */

				NuxeoController ctx = new NuxeoController(resourceRequest, resourceResponse, getPortletContext());
				
				PortalControllerContext portalCtx = new PortalControllerContext(getPortletContext(), resourceRequest,resourceResponse);
			
				//ctx.setContextualization(IPortalUrlFactory.CONTEXTUALIZATION_PORTAL);

				/* On détermine l'uid et le scope */

				String nuxeoRequest = null;

				PortalWindow window = WindowFactory.getWindow(resourceRequest);

				nuxeoRequest = window.getProperty("osivia.nuxeoRequest");
				


				if ("beanShell".equals(window.getProperty("osivia.requestInterpretor"))) {
					// Evaluation beanshell
					Interpreter i = new Interpreter();
					i.set("params", PageSelectors.decodeProperties(resourceRequest.getParameter("selectors")));
					i.set("request", resourceRequest);
					i.set("NXQLFormater", new NXQLFormater());
					//i.set("path",  resourceRequest.getParameter("osivia.cms.path"));		
					
					i.set("basePath",  ctx.getBasePath());
					i.set("spacePath",  ctx.getSpacePath());
					i.set("navigationPath",  ctx.getNavigationPath());
					i.set("contentPath",  ctx.getContentPath());


					nuxeoRequest = (String) i.eval(nuxeoRequest);
				}


				/* Initialisation de la page courante et de la taille de la page */

				int pageSize = 10;

				String pageSizeAttributeName = "osivia.cms.pageSizeMax";
				if (window.getProperty(pageSizeAttributeName) != null) {
					pageSize = Integer.parseInt(window.getProperty(pageSizeAttributeName));
				}

				String style = window.getProperty("osivia.cms.style");
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

					boolean applyPortalRequestFilter = true;
					if( "global".equals(window.getProperty("osivia.cms.requestFilteringPolicy")))
							applyPortalRequestFilter = false;
					
					PaginableDocuments docs = (PaginableDocuments) ctx.executeNuxeoCommand(new ListCommand(
							nuxeoRequest, ctx.isDisplayingLiveVersion(), 0, pageSize, schemas, window.getProperty("osivia.cms.requestFilteringPolicy")));

					org.w3c.dom.Document document = RssGenerator.createDocument(ctx, portalCtx,  window.getProperty("osivia.rssTitle"), docs, window.getProperty("osivia.rssLinkRef"));
					
					
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
		
		PortalWindow window = WindowFactory.getWindow(req);
		NuxeoController ctx = new NuxeoController(req, res, getPortletContext());

		if ("admin".equals(req.getPortletMode().toString()) && req.getParameter("modifierPrefs") != null) {


			window.setProperty("osivia.nuxeoRequest", req.getParameter("nuxeoRequest"));

			if ("1".equals(req.getParameter("beanShell")))
				window.setProperty("osivia.requestInterpretor", "beanShell");
			else if (window.getProperty("osivia.requestInterpretor") != null)
				window.setProperty("osivia.requestInterpretor", null);

			if ("1".equals(req.getParameter("displayNuxeoRequest")))
				window.setProperty("osivia.displayNuxeoRequest", "1");
			else if (window.getProperty("osivia.displayNuxeoRequest") != null)
				window.setProperty("osivia.displayNuxeoRequest", null);

			
			if (req.getParameter("displayLiveVersion") != null && req.getParameter("displayLiveVersion").length() > 0)
				window.setProperty("osivia.cms.displayLiveVersion", req.getParameter("displayLiveVersion"));
			else if (window.getProperty("osivia.cms.displayLiveVersion") != null)
				window.setProperty("osivia.cms.displayLiveVersion", null);
			
	
			
			if (req.getParameter("requestFilteringPolicy") != null && req.getParameter("requestFilteringPolicy").length() > 0)
				window.setProperty("osivia.cms.requestFilteringPolicy", req.getParameter("requestFilteringPolicy"));
			else if (window.getProperty("osivia.cms.requestFilteringPolicy") != null)
				window.setProperty("osivia.cms.requestFilteringPolicy", null);

			
		
			if (!"1".equals(req.getParameter("showMetadatas")))
				window.setProperty("osivia.cms.hideMetaDatas", "1");
			else if (window.getProperty("osivia.cms.hideMetaDatas") != null)
				window.setProperty("osivia.cms.hideMetaDatas", null);

			if (req.getParameter("scope") != null && req.getParameter("scope").length() > 0)
				window.setProperty("osivia.cms.scope", req.getParameter("scope"));
			else if (window.getProperty("osivia.cms.scope") != null)
				window.setProperty("osivia.cms.scope", null);

			if (req.getParameter("style") != null && req.getParameter("style").length() > 0)
				window.setProperty("osivia.cms.style", req.getParameter("style"));
			else if (window.getProperty("osivia.cms.style") != null)
				window.setProperty("osivia.cms.style", null);

			// Taille de page
			int pageSize = 0;
			if (req.getParameter("pageSize") != null) {
				try {
					pageSize = Integer.parseInt(req.getParameter("pageSize"));
				} catch (Exception e) {
					// Mal formatté
				}
			}

			if (pageSize > 0)
				window.setProperty("osivia.cms.pageSize", Integer.toString(pageSize));
			else if (window.getProperty("osivia.cms.pageSize") != null)
				window.setProperty("osivia.cms.pageSize", null);

			// Taille de page max
			int pageSizeMax = 0;
			if (req.getParameter("pageSizeMax") != null) {
				try {
					pageSizeMax = Integer.parseInt(req.getParameter("pageSizeMax"));
				} catch (Exception e) {
					// Mal formatté
				}
			}

			if (pageSizeMax > 0)
				window.setProperty("osivia.cms.pageSizeMax", Integer.toString(pageSizeMax));
			else if (window.getProperty("osivia.cms.pageSizeMax") != null)
				window.setProperty("osivia.cms.pageSizeMax", null);

			// Limite
			int maxItems = 0;
			if (req.getParameter("maxItems") != null) {
				try {
					maxItems = Integer.parseInt(req.getParameter("maxItems"));
				} catch (Exception e) {
					// Mal formatté
				}
			}

			if (maxItems > 0)
				window.setProperty("osivia.cms.maxItems", Integer.toString(maxItems));
			else if (window.getProperty("osivia.cms.maxItems") != null)
				window.setProperty("osivia.cms.maxItems", null);

			if (req.getParameter("permaLinkRef") != null && req.getParameter("permaLinkRef").length() > 0)
				window.setProperty("osivia.permaLinkRef", req.getParameter("permaLinkRef"));
			else if (window.getProperty("osivia.permaLinkRef") != null)
				window.setProperty("osivia.permaLinkRef", null);
			
			if (req.getParameter("rssLinkRef") != null && req.getParameter("rssLinkRef").length() > 0)
				window.setProperty("osivia.rssLinkRef", req.getParameter("rssLinkRef"));
			else if (window.getProperty("osivia.rssLinkRef") != null)
				window.setProperty("osivia.rssLinkRef", null);
			
			
			
			if (req.getParameter("rssTitle") != null && req.getParameter("rssTitle").length() > 0)
				window.setProperty("osivia.rssTitle", req.getParameter("rssTitle"));
			else if (window.getProperty("osivia.rssTitle") != null)
				window.setProperty("osivia.rssTitle", null);
			


			res.setPortletMode(PortletMode.VIEW);
			res.setWindowState(WindowState.NORMAL);
		}

		if ("admin".equals(req.getPortletMode().toString()) && req.getParameter("annuler") != null) {

			res.setPortletMode(PortletMode.VIEW);
			res.setWindowState(WindowState.NORMAL);
		}
		
		
		
		
		// v2.0.8 : ajout custom
		ListTemplate template = getCurrentTemplate( window);
		
		if( template.getModule() != null)	{
			try {
				template.getModule().processAction(ctx, window, req, res);
			} catch (Exception e) {
				throw new PortletException(e);
			}
		}
	}

	@RenderMode(name = "admin")
	public void doAdmin(RenderRequest req, RenderResponse res) throws IOException, PortletException {

		res.setContentType("text/html");

		try {

			NuxeoController ctx = new NuxeoController(req, res, getPortletContext());

			PortletRequestDispatcher rd = null;

			PortalWindow window = WindowFactory.getWindow(req);

			String nuxeoRequest = window.getProperty("osivia.nuxeoRequest");
			if (nuxeoRequest == null)
				nuxeoRequest = "";
			req.setAttribute("nuxeoRequest", nuxeoRequest);

			String displayLiveVersion = window.getProperty("osivia.cms.displayLiveVersion");
			req.setAttribute("displayLiveVersion", displayLiveVersion);
			
			
			String requestFilteringPolicy = window.getProperty("osivia.cms.requestFilteringPolicy");
			req.setAttribute("requestFilteringPolicy", requestFilteringPolicy);
			
			
		
			String showMetadatas = "1";
			if ("1".equals(window.getProperty("osivia.cms.hideMetaDatas")))
				showMetadatas = "0";
			req.setAttribute("showMetadatas", showMetadatas);

			String beanShell = "";
			String interpretor = window.getProperty("osivia.requestInterpretor");
			if ("beanShell".equals(interpretor))
				beanShell = "1";
			req.setAttribute("beanShell", beanShell);

			req.setAttribute("displayNuxeoRequest", window.getProperty("osivia.displayNuxeoRequest"));
			
			//v2.0.5
			req.setAttribute("changeDisplayMode", window.getProperty("osivia.changeDisplayMode"));
			req.setAttribute("forceContextualization", window.getProperty("osivia.forceContextualization"));

			String scope = window.getProperty("osivia.cms.scope");
			req.setAttribute("scope", scope);

			/* Styles d'affichage */

			Map<String, ListTemplate> templates = getListTemplates();
			req.setAttribute("templates", templates);

			String style = window.getProperty("osivia.cms.style");
			if (style == null)
				style = CMSCustomizer.STYLE_NORMAL;
			req.setAttribute("style", style);

			String pageSize = window.getProperty("osivia.cms.pageSize");
			req.setAttribute("pageSize", pageSize);

			String pageSizeMax = window.getProperty("osivia.cms.pageSizeMax");
			req.setAttribute("pageSizeMax", pageSizeMax);

			String maxItems = window.getProperty("osivia.cms.maxItems");
			req.setAttribute("maxItems", maxItems);

			String permaLinkRef = window.getProperty("osivia.permaLinkRef");
			if (permaLinkRef == null)
				permaLinkRef = "";
			req.setAttribute("permaLinkRef", permaLinkRef);


		String rssLinkRef = window.getProperty("osivia.rssLinkRef");
		if (rssLinkRef == null )
			rssLinkRef = "";
		req.setAttribute("rssLinkRef", rssLinkRef);
	

		String rssTitle = window.getProperty("osivia.rssTitle");
		if (rssTitle == null )
			rssTitle = "";
		req.setAttribute("rssTitle", rssTitle);
		
		


			req.setAttribute("ctx", ctx);

			rd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/liste/admin.jsp");
			rd.include(req, res);
		}

		catch (Exception e) {
			if (!(e instanceof PortletException))
				throw new PortletException(e);
		}

	}

	@SuppressWarnings("unchecked")
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException,
			PortletSecurityException, IOException {

		logger.debug("doView");

		try {
			
			response.setContentType("text/html");
			
			NuxeoController ctx = new NuxeoController(request, response, getPortletContext());
			request.setAttribute("ctx", ctx);
		

			/* On détermine l'uid et le scope */

			String nuxeoRequest = null;

			PortalWindow window = WindowFactory.getWindow(request);

			// Mode fil de documents
			Map<String, Integer> documentOrder = new HashMap<String, Integer>();
			
			if("true".equals(window.getProperty("osivia.cms.feed"))) {
				nuxeoRequest = "return \"";
				boolean first = true;
				int nbElements = Integer.parseInt(window.getProperty("osivia.cms.news.size"));
				
				
				for(Integer i = 0; i<nbElements; i++) {
					if(first) first = false;
					else {
						nuxeoRequest = nuxeoRequest.concat(" OR ");	
					}
					
					String path = window.getProperty("osivia.cms.news."+i.toString()+".docURI");
					
					documentOrder.put(path, Integer.parseInt(window.getProperty("osivia.cms.news."+i.toString()+".order")));
					
					nuxeoRequest = nuxeoRequest.concat("ecm:path = '"+path+"'");
					
				}
				nuxeoRequest = nuxeoRequest.concat("\";");
			}
			// Mode requêtage classique
			else {
				nuxeoRequest = window.getProperty("osivia.nuxeoRequest");
			}

			if ("beanShell".equals(window.getProperty("osivia.requestInterpretor"))) {
				// Evaluation beanshell
				Interpreter i = new Interpreter();
				i.set("params", PageSelectors.decodeProperties(request.getParameter("selectors")));
				i.set("basePath",  ctx.getBasePath());
				i.set("spacePath",  ctx.getSpacePath());
				i.set("navigationPath",  ctx.getNavigationPath());
				/* Initialisation de navigationPubInfos pour éviter des erreurs 
				 * de "non définition" lors 
				 * de la construction d'une requête avec cette variable.
				 */
				i.set("navigationPubInfos",  null);
				if( ctx.getNavigationPath() != null)	{
					CMSPublicationInfos navigationPubInfos = ctx.getCMSService().getPublicationInfos(ctx.getCMSCtx(), ctx.getNavigationPath());
					i.set("navigationPubInfos",  navigationPubInfos);
				}
				
				i.set("spaceId", null);
				if(ctx.getSpacePath() != null){
					CMSPublicationInfos spacePubInfos = ctx.getCMSService().getPublicationInfos(ctx.getCMSCtx(), ctx.getSpacePath());
					if(spacePubInfos != null)
						i.set("spaceId", spacePubInfos.getLiveId()); 
				}					
				
				i.set("contentPath",  ctx.getContentPath());
				i.set("request", request);
				i.set("NXQLFormater", new NXQLFormater());
				
				CMSItem navItem = ctx.getNavigationItem();
				i.set("navItem",  navItem);



				
				nuxeoRequest = (String) i.eval(nuxeoRequest);
			}

			if("EMPTY_REQUEST".equals(nuxeoRequest))	{
				request.setAttribute("osivia.emptyResponse", "1");
				response.getWriter().print("<h2>Requête vide</h2>");
				response.getWriter().close();
				return;
				
			}
	
			
			/* Filtre pour sélectionner uniquement les version publiées */

			int maxItems = -1;
			if (window.getProperty("osivia.cms.maxItems") != null)
				maxItems = Integer.parseInt(window.getProperty("osivia.cms.maxItems"));

			/* Initialisation de la page courante et de la taille de la page */

			int pageSize = -1;
			int currentPage = 0;

			String pageSizeAttributeName = "osivia.cms.pageSize";
			if (WindowState.MAXIMIZED.equals(request.getWindowState()))
				pageSizeAttributeName = "osivia.cms.pageSizeMax";

			if (window.getProperty(pageSizeAttributeName) != null) {
				pageSize = Integer.parseInt(window.getProperty(pageSizeAttributeName));

				String currentState = request.getParameter("currentState");
				String sCurrentPage = request.getParameter("currentPage");

				// La page est réinitialisée si on change de mode
				if (sCurrentPage != null && request.getWindowState().toString().equals(currentState))
					currentPage = Integer.parseInt(sCurrentPage);
			}

			/* Reinitialisation du numéro de page si changement de critères */

			String selectors = request.getParameter("selectors");
			String lastSelectors = request.getParameter("lastSelectors");

			if (((selectors != null) && (!selectors.equals(lastSelectors))) || (selectors == null)
					&& (lastSelectors != null))
				currentPage = 0;

			String style = window.getProperty("osivia.cms.style");
			if (style == null)
				style = CMSCustomizer.STYLE_NORMAL;

			if (nuxeoRequest != null) {

	
				// Calcul de la taille de la page

				int requestPageSize = 100;
				if (pageSize != -1)
					requestPageSize = pageSize;

				// Pas d'autre solution que de servir de la pagination pour
				// limiter le nombre d'items ...
				// limite de la taille de la page par le nombre d'items (sur la
				// première page)
				if (maxItems != -1 && currentPage == 0) {
					requestPageSize = Math.min(requestPageSize, maxItems);
				}

				// v2.0.8 : ajout custom
				ListTemplate template = getCurrentTemplate( window);
	

				String schemas = template.getSchemas();

	
				PaginableDocuments docs = (PaginableDocuments) ctx.executeNuxeoCommand(new ListCommand(nuxeoRequest,
						ctx.isDisplayingLiveVersion(), currentPage, requestPageSize, schemas,window.getProperty("osivia.cms.requestFilteringPolicy")));

				
				// En mode fil, on trie les documents suivant l'ordre défini par l'utilisateur
				if("true".equals(window.getProperty("osivia.cms.feed"))) {
					Collections.sort(docs, new DocumentComparator(documentOrder));
				}
				
				int nbPages = 0;

				if (pageSize != -1) {
					if (maxItems == -1)
						nbPages = ((docs.getTotalSize() - 1) / pageSize) + 1;
					else {
						nbPages = ((Math.min(docs.getTotalSize(), maxItems) - 1) / pageSize) + 1;

						// Attention à la dernière page ...
						// Elle peut contenir trop d'éléments ...

						if (docs.size() < (currentPage + 1) * requestPageSize) {

							int pageLimit = Math.max(0, maxItems - currentPage * requestPageSize);

							while (docs.size() > pageLimit)
								docs.remove(docs.size() - 1);
						}

					}
				}

				request.setAttribute("docs", docs);

				request.setAttribute("currentPage", currentPage);

				request.setAttribute("nbPages", nbPages);

				if ("1".equals(window.getProperty("osivia.displayNuxeoRequest")))
					request.setAttribute("nuxeoRequest", nuxeoRequest);

				request.setAttribute("selectors", request.getParameter("selectors"));

				request.setAttribute("style", style);

						String permaLinkRef = window.getProperty("osivia.permaLinkRef");
						if( permaLinkRef != null)	{
							Map<String, String> publicParams = new HashMap<String, String>();
							if( selectors != null)
								publicParams.put("selectors", selectors);
							String permLinkType =  IPortalUrlFactory.PERM_LINK_TYPE_PAGE;
							if( request.getParameter("osivia.cms.path") != null)	{
								permLinkType = IPortalUrlFactory.PERM_LINK_TYPE_CMS;
								permaLinkRef = null;
							}
							
							String permaLinkURL = ctx.getPortalUrlFactory().getPermaLink(new PortalControllerContext(getPortletContext(), request,
										response), permaLinkRef, publicParams, request.getParameter("osivia.cms.path"), permLinkType);
							request.setAttribute("permaLinkURL", permaLinkURL);
						}
					


						
						String rssLinkRef = window.getProperty("osivia.rssLinkRef");
						if( rssLinkRef != null)	{
							// JSS20120123 : pourquoi filtrer sur les lives
							//if( !ctx.isDisplayingLiveVersion())	{
							
						
							boolean anonymousAccess = true;
							
							if( request.getParameter("osivia.cms.path") != null)	{
								
								// check if the navigation folder is accessible in anonymous mode (for rss) 
								CMSServiceCtx cmsReadNavContext = new CMSServiceCtx();
								cmsReadNavContext.setControllerContext(ControllerContextAdapter.getControllerContext(ctx.getPortalCtx()));
								cmsReadNavContext.setScope(ctx.getScope());		
								
								anonymousAccess = ctx.getCMSService().checkContentAnonymousAccess(cmsReadNavContext, request.getParameter("osivia.cms.path"));
							
								
							}
							
							if( anonymousAccess){
							
							Map<String, String> publicParams = new HashMap<String, String>();
							if( selectors != null)
								publicParams.put("selectors", selectors);
							
							
								String rssLinkURL = ctx.getPortalUrlFactory().getPermaLink(new PortalControllerContext(getPortletContext(), request,
										response), rssLinkRef, publicParams, request.getParameter("osivia.cms.path"), IPortalUrlFactory.PERM_LINK_TYPE_RSS);
								
								request.setAttribute("rssLinkURL", rssLinkURL);
							}
							//}
						}
						
						// Notify portal if empty response (to enable 'hideEmptyPortlet' use cases)
						if( currentPage == 0 && docs.size() == 0)
							request.setAttribute("osivia.emptyResponse", "1");
						
		
				// v2.0.8 : ajout custom
				if( template.getModule() != null)
					template.getModule().doView(ctx, window, request, response);

		
				getPortletContext().getRequestDispatcher("/WEB-INF/jsp/liste/view.jsp").include(request, response);

			} else {
				
				if((StringUtils.isNotEmpty((String) request.getAttribute("bsh.title"))) || (StringUtils.isNotEmpty((String) request.getAttribute("bsh.html")))){
					
					if(StringUtils.isNotEmpty((String) request.getAttribute("bsh.title")))
						response.setTitle((String) request.getAttribute("bsh.title"));
					
					if(StringUtils.isNotEmpty((String) request.getAttribute("bsh.html"))){
						
						response.setContentType("text/html");
						response.getWriter().print((String) request.getAttribute("bsh.html"));
						response.getWriter().close();
						
					}
					return;
					
				}else{

					response.setContentType("text/html");
					response.getWriter().print("<h2>Requête non définie</h2>");
					response.getWriter().close();
					return;
				
				}

			}

		} catch (NuxeoException e) {
			PortletErrorHandler.handleGenericErrors(response, e);
		}

		catch (Exception e) {
			if( e instanceof bsh.EvalError){
				response.setContentType("text/html");
				response.getWriter().print("<h2>Requête incorrecte : </h2>");
				response.getWriter().print(((bsh.EvalError) e).getMessage());
				response.getWriter().close();
				return;
				
				
			}	else
			
			if (!(e instanceof PortletException))
				throw new PortletException(e);
		}

		logger.debug("doView end");
	}

}
