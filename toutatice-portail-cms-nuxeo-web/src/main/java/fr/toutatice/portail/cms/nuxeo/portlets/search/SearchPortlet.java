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
 */
package fr.toutatice.portail.cms.nuxeo.portlets.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.RenderMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.Window;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PaginableDocuments;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.internationalization.Bundle;
import org.osivia.portal.api.internationalization.IBundleFactory;
import org.osivia.portal.api.internationalization.IInternationalizationService;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;
import org.osivia.portal.core.portalobjects.PortalObjectUtils;

import fr.toutatice.portail.cms.nuxeo.api.CMSPortlet;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilterContext;
import fr.toutatice.portail.cms.nuxeo.api.PortletErrorHandler;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.api.services.dao.DocumentDAO;


/**
 * Nuxeo search portlet.
 *
 * @see CMSPortlet
 */
public class SearchPortlet extends CMSPortlet {

    /** View path. */
    private static final String PATH_VIEW = "/WEB-INF/jsp/search/view.jsp";
    /** Result path. */
    private static final String PATH_RESULT = "/WEB-INF/jsp/search/result.jsp";
    /** Admin path. */
    private static final String PATH_ADMIN = "/WEB-INF/jsp/search/admin.jsp";

    /** Portal URL factory. */
    private IPortalUrlFactory portalUrlFactory;
    /** Bundle factory. */
    private IBundleFactory bundleFactory;
    /** Document DAO. */
    private DocumentDAO documentDAO;


    /**
     * Constructor.
     */
    public SearchPortlet() {
        super();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void init(PortletConfig config) throws PortletException {
        super.init(config);

        // Portal URL factory
        this.portalUrlFactory = (IPortalUrlFactory) this.getPortletContext().getAttribute("UrlService");
        if (this.portalUrlFactory == null) {
            throw new PortletException("Cannot start TestPortlet due to service unavailability");
        }

        // Internationalization service initialization
        IInternationalizationService internationalizationService = (IInternationalizationService) this.getPortletContext().getAttribute(
                Constants.INTERNATIONALIZATION_SERVICE_NAME);
        this.bundleFactory = internationalizationService.getBundleFactory(this.getClass().getClassLoader());

        // Document DAO
        this.documentDAO = DocumentDAO.getInstance();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void processAction(ActionRequest request, ActionResponse response) throws IOException, PortletException {
        String action = request.getParameter(ActionRequest.ACTION_NAME);

        if (PortletMode.VIEW.equals(request.getPortletMode())) {
            // View
            if ("search".equals(action)) {
                // Refresh search keywords
                response.setRenderParameter("keywords", request.getParameter("keywords"));
            }
        } else if ("admin".equals(request.getPortletMode().toString())) {
            // Admin
            if ("save".equals(action)) {
                // Current window
                PortalWindow window = WindowFactory.getWindow(request);

                // Path
                String path = request.getParameter("path");
                window.setProperty(Constants.WINDOW_PROP_URI, path);
              
            }

            response.setPortletMode(PortletMode.VIEW);
            response.setWindowState(WindowState.NORMAL);
        }
    }


    /**
     * Admin view display.
     *
     * @param request request
     * @param response response
     * @throws PortletException
     * @throws IOException
     */
    @RenderMode(name = "admin")
    public void doAdmin(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        // Current window
        PortalWindow window = WindowFactory.getWindow(request);

        // Path
        String path = window.getProperty(Constants.WINDOW_PROP_URI);
        request.setAttribute("path", StringUtils.trimToEmpty(path));

        response.setContentType("text/html");
        this.getPortletContext().getRequestDispatcher(PATH_ADMIN).include(request, response);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(request, response, this.getPortletContext());
        // Current window
        PortalWindow portalWindow = WindowFactory.getWindow(request);
        // Bundle
        Bundle bundle = this.bundleFactory.getBundle(request.getLocale());

        // Path
        String path = portalWindow.getProperty(Constants.WINDOW_PROP_URI);
        // Keywords
        String keywords = request.getParameter("keywords");
        if (keywords == null) {
            // Get public keywords
            keywords = request.getParameter("osivia.keywords");
        }

        try {
            String requestDispatcherPath;

            if (keywords != null) {
                // Result page

                // Current page
                String currentPageParam = request.getParameter("currentPage");
                int currentPage = NumberUtils.toInt(currentPageParam);

                // No cache for search
                nuxeoController.setCacheTimeOut(0);

                NuxeoQueryFilterContext queryFilter = new NuxeoQueryFilterContext();
                if( path != null && path.length() > 0)  {
                    queryFilter = nuxeoController.getQueryFilterContextForPath(path);
                }
                
                // Search command execution
                SearchCommand command = new SearchCommand(queryFilter, path, keywords, currentPage);
                PaginableDocuments docs = (PaginableDocuments) nuxeoController.executeNuxeoCommand(command);

                // Result list
                List<DocumentDTO> documentsDTO = new ArrayList<DocumentDTO>(docs.size());
                for (Document document : docs) {
                    DocumentDTO documentDTO = this.documentDAO.toDTO(document);
                    documentsDTO.add(documentDTO);
                }

                int minPage = Math.max(0, currentPage - docs.getPageSize());
                int maxPage = Math.min(currentPage + docs.getPageSize(), docs.getPageCount()) - 1;

                request.setAttribute("nuxeoController", nuxeoController);
                request.setAttribute("keywords", keywords);
                request.setAttribute("documents", documentsDTO);
                request.setAttribute("totalSize", docs.getTotalSize());
                request.setAttribute("currentPage", currentPage);
                request.setAttribute("minPage", minPage);
                request.setAttribute("maxPage", maxPage);

                // Title
                response.setTitle(bundle.getString("SEARCH_RESULT"));

                requestDispatcherPath = PATH_RESULT;
            } else {
                // Search page

                // Current technical window
                Window technicalWindow = (Window) request.getAttribute("osivia.window");
                // Current page
                Page page = (Page) technicalWindow.getParent();
                // Current page identifier
                String pageId = PortalObjectUtils.getHTMLSafeId(page.getId());

                // URL window properties
                Map<String, String> windowProperties = new HashMap<String, String>();
                windowProperties.put(Constants.WINDOW_PROP_URI, nuxeoController.getComputedPath(path));
                windowProperties.put("osivia.title", bundle.getString("SEARCH_RESULT"));
                windowProperties.put("osivia.hideDecorators", "1");

                // URL parameters
                Map<String, String> params = new HashMap<String, String>();
                params.put("keywords", "__REPLACE_KEYWORDS__");

                // URL
                String url = this.portalUrlFactory.getStartPortletInRegionUrl(nuxeoController.getPortalCtx(), pageId,
                        "toutatice-portail-cms-nuxeo-searchPortletInstance", "virtual", "portalServiceWindow", windowProperties, params);
                request.setAttribute("searchUrl", url);

                requestDispatcherPath = PATH_VIEW;
            }

            response.setContentType("text/html");
            this.getPortletContext().getRequestDispatcher(requestDispatcherPath).include(request, response);
        } catch (NuxeoException e) {
            PortletErrorHandler.handleGenericErrors(response, e);
        } catch (PortletException e) {
            throw e;
        } catch (Exception e) {
            throw new PortletException(e);
        }
    }

}
