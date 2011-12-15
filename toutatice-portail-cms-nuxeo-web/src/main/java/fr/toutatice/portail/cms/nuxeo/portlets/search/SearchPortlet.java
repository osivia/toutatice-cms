package fr.toutatice.portail.cms.nuxeo.portlets.search;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;
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

import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.PortalObjectPath;
import org.jboss.portal.core.model.portal.Window;
import org.nuxeo.ecm.automation.client.jaxrs.model.Documents;
import org.nuxeo.ecm.automation.client.jaxrs.model.PaginableDocuments;




import fr.toutatice.portail.api.contexte.PortalControllerContext;
import fr.toutatice.portail.api.urls.IPortalUrlFactory;
import fr.toutatice.portail.api.windows.PortalWindow;
import fr.toutatice.portail.api.windows.WindowFactory;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.core.CMSPortlet;
import fr.toutatice.portail.cms.nuxeo.core.PortletErrorHandler;


/**
 * Portlet d'affichage d'un document Nuxeo
 */

public class SearchPortlet extends CMSPortlet {
	
	protected IPortalUrlFactory urlFactory;
	
	public void init(PortletConfig config) throws PortletException {

		super.init(config);
		
		urlFactory = (IPortalUrlFactory) getPortletContext().getAttribute("UrlService");
	}
	
	
	public void processAction(ActionRequest req, ActionResponse res) throws IOException, PortletException {

		logger.debug("processAction ");
		
		if( PortletMode.VIEW.equals(req.getPortletMode()) && req.getParameter("searchAction") != null){
			res.setRenderParameter("keywords", req.getParameter("keywords"));
			
		}


		if ("admin".equals(req.getPortletMode().toString()) && req.getParameter("modifierPrefs") != null) {

			PortalWindow window = WindowFactory.getWindow(req);
			window.setProperty("pia.nuxeoPath", req.getParameter("nuxeoPath"));

			if (req.getParameter("scope") != null && req.getParameter("scope").length() > 0)
				window.setProperty("pia.cms.scope", req.getParameter("scope"));
			else if (window.getProperty("pia.cms.scope") != null)
				window.setProperty("pia.cms.scope", null);
			
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

		String scope = window.getProperty("pia.cms.scope");
		req.setAttribute("scope", scope);
		
		String displayLiveVersion = window.getProperty("pia.cms.displayLiveVersion");
		if (displayLiveVersion == null)
			displayLiveVersion = "";
		req.setAttribute("displayLiveVersion", displayLiveVersion);
		
		
		req.setAttribute("ctx", ctx);

		rd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/search/admin.jsp");
		rd.include(req, res);

	}

	@SuppressWarnings("unchecked")
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException,
			PortletSecurityException, IOException {

		logger.debug("doView");

		PortalWindow window = WindowFactory.getWindow(request);

		String	nuxeoPath =  window.getProperty("pia.nuxeoPath");
			
		String keywords = request.getParameter("keywords");
		
		if( keywords == null)
			// on prend les keywords public
			keywords = request.getParameter("pia.keywords");

			
		
		/* Filtre pour sélectionner uniquement les version publiées */


		try {
			response.setContentType("text/html");
			
			NuxeoController ctx = new NuxeoController(request, response, getPortletContext());

			if (keywords != null) {
				// Page de resultats

				
				int currentPage = 0;
				String sCurrentPage =  request.getParameter("currentPage");
				if( sCurrentPage != null)
					currentPage = Integer.parseInt(sCurrentPage);

				// Pas de cache sur les recherches
				ctx.setCacheTimeOut(0);
				
	
				PaginableDocuments docs = (PaginableDocuments) ctx.executeNuxeoCommand(new SearchCommand(nuxeoPath, ctx.isDisplayingLiveVersion(), keywords, currentPage ));

				request.setAttribute("docs", docs);
				request.setAttribute("ctx", ctx);

				request.setAttribute("keywords", keywords);
				
				request.setAttribute("currentPage", currentPage);
				

				getPortletContext().getRequestDispatcher("/WEB-INF/jsp/search/result.jsp").include(request, response);

			} else {

				// Page de recherche
				Window windowPortal = (Window) request.getAttribute("pia.window");

				Page page = (Page) windowPortal.getParent();
				String pageId = URLEncoder.encode(page.getId().toString(PortalObjectPath.SAFEST_FORMAT), "UTF-8");

				// Url d'appel de la recherche

				Map<String, String> windowProperties = new HashMap<String, String>();
				if (ctx.getScope() != null)
					windowProperties.put("pia.cms.scope", ctx.getScope());
				windowProperties.put("pia.nuxeoPath", nuxeoPath);
				windowProperties.put("pia.cms.displayLiveVersion", ctx.getDisplayLiveVersion());
				
				windowProperties.put("pia.title", "Résultats de la recherche");
				windowProperties.put("pia.hideDecorators", "1");
				

				Map<String, String> params = new HashMap<String, String>();
				params.put("keywords", "__REPLACE_KEYWORDS__");


				String url = urlFactory.getStartProcUrl(new PortalControllerContext(getPortletContext(), request,
						response), pageId, "toutatice-portail-cms-nuxeo-searchPortletInstance", "virtual", "portalServiceWindow", windowProperties, params);

				request.setAttribute("searchUrl", url);

				getPortletContext().getRequestDispatcher("/WEB-INF/jsp/search/view.jsp").include(request, response);
			}
		}
		
		catch( NuxeoException e){
			PortletErrorHandler.handleGenericErrors(response, e);
		}


		catch (Exception e) {
			if (!(e instanceof PortletException))
				throw new PortletException(e);
		}

		logger.debug("doView end");

	}

	

}
