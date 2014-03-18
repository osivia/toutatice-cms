/*
 * (C) Copyright 2014 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 *
 *    
 */
package fr.toutatice.portail.cms.nuxeo.portlets.search;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
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

import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.PortalObjectPath;
import org.jboss.portal.core.model.portal.Window;
import org.nuxeo.ecm.automation.client.model.PaginableDocuments;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;

import fr.toutatice.portail.cms.nuxeo.api.CMSPortlet;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.PortletErrorHandler;



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

        // logger.debug("processAction ");
		
		if( PortletMode.VIEW.equals(req.getPortletMode()) && req.getParameter("searchAction") != null){
			res.setRenderParameter("keywords", req.getParameter("keywords"));
		}


		if ("admin".equals(req.getPortletMode().toString()) && req.getParameter("modifierPrefs") != null) {

			PortalWindow window = WindowFactory.getWindow(req);
			window.setProperty(Constants.WINDOW_PROP_URI, req.getParameter("nuxeoPath"));

			
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
		String nuxeoPath = window.getProperty(Constants.WINDOW_PROP_URI);
		if (nuxeoPath == null)
			nuxeoPath = "";
		req.setAttribute("nuxeoPath", nuxeoPath);
		
		String displayLiveVersion = window.getProperty("osivia.cms.displayLiveVersion");
		req.setAttribute("displayLiveVersion", displayLiveVersion);
		
		req.setAttribute("ctx", ctx);

		rd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/search/admin.jsp");
		rd.include(req, res);

	}

	@SuppressWarnings("unchecked")
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException,
			PortletSecurityException, IOException {

        // LBI - pb LinkageException, voir #67
        // logger.debug("doView");
		
		NuxeoController ctx = new NuxeoController(request, response, getPortletContext());

		PortalWindow window = WindowFactory.getWindow(request);

		String	nuxeoPath =  window.getProperty(Constants.WINDOW_PROP_URI);

			
		String keywords = request.getParameter("keywords");
		
		if( keywords == null)
			// on prend les keywords public
			keywords = request.getParameter("osivia.keywords");

			
		
		/* Filtre pour sélectionner uniquement les version publiées */


		try {
			response.setContentType("text/html");
			
		

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
				
				//v2.0.5 : on repete le formulaire de recherche
				//request.setAttribute("hideSearchSubForm", window.getProperty("osivia.hideSearchSubForm"));

				request.setAttribute("keywords", keywords);
				
				request.setAttribute("currentPage", currentPage);
				
				response.setTitle("Résultats de la recherche");
				

				getPortletContext().getRequestDispatcher("/WEB-INF/jsp/search/result.jsp").include(request, response);

			} else {

				// Page de recherche
				Window windowPortal = (Window) request.getAttribute("osivia.window");

				Page page = (Page) windowPortal.getParent();
				String pageId = URLEncoder.encode(page.getId().toString(PortalObjectPath.SAFEST_FORMAT), "UTF-8");

				// Url d'appel de la recherche

				Map<String, String> windowProperties = new HashMap<String, String>();
				windowProperties.put(Constants.WINDOW_PROP_URI, ctx.getComputedPath(nuxeoPath));
				windowProperties.put("osivia.cms.displayLiveVersion", ctx.getDisplayLiveVersion());
				windowProperties.put("osivia.hideSearchSubForm", "1");
				
				windowProperties.put("osivia.title", "Résultats de la recherche");
				windowProperties.put("osivia.hideDecorators", "1");
				

				Map<String, String> params = new HashMap<String, String>();
				params.put("keywords", "__REPLACE_KEYWORDS__");


				String url = urlFactory.getStartPortletInRegionUrl(new PortalControllerContext(getPortletContext(), request,
						response), pageId, "toutatice-portail-cms-nuxeo-searchPortletInstance", "virtual", "portalServiceWindow", windowProperties, params);

				request.setAttribute("searchUrl", url);
				
				request.setAttribute("ctx", ctx);

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

        // logger.debug("doView end");

	}

	

}
