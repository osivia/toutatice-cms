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
package fr.toutatice.portail.cms.nuxeo.portlets.fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

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
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;

import fr.toutatice.portail.cms.nuxeo.api.CMSPortlet;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.PortletErrorHandler;
import fr.toutatice.portail.cms.nuxeo.api.domain.FragmentType;
import fr.toutatice.portail.cms.nuxeo.api.domain.PluginModule;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCustomizer;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoService;

/**
 * View fragment portlet
 *
 * @see CMSPortlet
 */
public class ViewFragmentPortlet extends CMSPortlet {

    /** Fragment type identifier window property name. */
    public static final String FRAGMENT_TYPE_ID_WINDOW_PROPERTY = "osivia.fragmentTypeId";

    /** Admin JSP path. */
    private static final String PATH_ADMIN = "/WEB-INF/jsp/fragment/admin.jsp";
    /** View JSP path. */
    private static final String PATH_VIEW = "/WEB-INF/jsp/fragment/view.jsp";

    /** CMS customizer. */
    private INuxeoCustomizer customizer;


    /**
     * Default constructor.
     */
    public ViewFragmentPortlet() {
        super();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void init(PortletConfig config) throws PortletException {
        super.init(config);

        // Nuxeo service
        INuxeoService nuxeoService = Locator.findMBean(INuxeoService.class, "osivia:service=NuxeoService");
        // CMS customizer
        this.customizer = nuxeoService.getCMSCustomizer();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void processAction(ActionRequest request, ActionResponse response) throws IOException, PortletException {
        String action = request.getParameter(ActionRequest.ACTION_NAME);

        if ("admin".equals(request.getPortletMode().toString())) {
            if ("select".equals(action)) {
                // Select fragment type action

                // Fragment type identifier
                String fragmentTypeId = request.getParameter("fragmentTypeId");
                response.setRenderParameter("fragmentTypeId", fragmentTypeId);
            } else {
                if ("save".equals(action)) {
                    // Save action

                    // Portal controller context
                    PortalControllerContext portalControllerContext = new PortalControllerContext(this.getPortletContext(), request, response);
                    // Current window
                    PortalWindow window = WindowFactory.getWindow(request);

                    // Fragment type
                    String fragmentTypeId = StringUtils.trimToNull(request.getParameter("fragmentTypeId"));
                    window.setProperty(FRAGMENT_TYPE_ID_WINDOW_PROPERTY, fragmentTypeId);
                    if (fragmentTypeId != null) {
                        FragmentType fragmentType = this.customizer.getFragmentTypes(Locale.getDefault()).get(fragmentTypeId);
                        if (fragmentType != null) {
                            ClassLoader restoreLoader = null;
                            try {
                                // save current class loader

                                if (fragmentType.getModule() instanceof PluginModule) {
                                    restoreLoader = Thread.currentThread().getContextClassLoader();
                                    Thread.currentThread().setContextClassLoader(((PluginModule) fragmentType.getModule()).getCl());
                                }

                                // Specific fragment admin action
                                fragmentType.getModule().processAdminAction(portalControllerContext);

                            } finally {
                                if (restoreLoader != null) {
                                    Thread.currentThread().setContextClassLoader(restoreLoader);
                                }
                            }



                        }
                    }
                }

                response.setPortletMode(PortletMode.VIEW);
                response.setWindowState(WindowState.NORMAL);
            }
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
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(request, response, this.getPortletContext());

        // Portal controller context
        PortalControllerContext portalControllerContext = new PortalControllerContext(this.getPortletContext(), request, response);
        // Current window
        PortalWindow window = WindowFactory.getWindow(request);

        // Fragment type identifier
        String fragmentTypeId = request.getParameter("fragmentTypeId");
        if (fragmentTypeId == null) {
            fragmentTypeId = window.getProperty(FRAGMENT_TYPE_ID_WINDOW_PROPERTY);
        }
        request.setAttribute("fragmentTypeId", fragmentTypeId);

        // Fragment type
        if (fragmentTypeId != null) {
            FragmentType fragmentType = this.customizer.getFragmentTypes(Locale.getDefault()).get(fragmentTypeId);
            if (fragmentType != null) {
                // Specific fragment admin view


                ClassLoader restoreLoader = null;
                try {
                    // save current class loader

                    if (fragmentType.getModule() instanceof PluginModule) {
                        restoreLoader = Thread.currentThread().getContextClassLoader();
                        Thread.currentThread().setContextClassLoader(((PluginModule) fragmentType.getModule()).getCl());
                    }

                    // Specific fragment admin action
                    fragmentType.getModule().doAdmin(portalControllerContext);

                } finally {
                    if (restoreLoader != null) {
                        Thread.currentThread().setContextClassLoader(restoreLoader);
                    }
                }



                request.setAttribute("fragmentType", fragmentType);

                if (fragmentType.getModule().getAdminJSPName() != null) {
                    String jspName = "fragment-admin-" + fragmentType.getModule().getAdminJSPName();
                }
            }
        }

        // Fragment types
        Collection<FragmentType> fragmentTypes = this.customizer.getFragmentTypes(request.getLocale()).values();
        List<FragmentType> filteredFragmentTypes = new ArrayList<FragmentType>(fragmentTypes.size());
        for (FragmentType fragmentType : fragmentTypes) {
            if (fragmentType.getModule().isDisplayedInAdmin()) {
                filteredFragmentTypes.add(fragmentType);
            }
        }
        request.setAttribute("fragmentTypes", filteredFragmentTypes);


        response.setContentType("text/html");
        this.getPortletContext().getRequestDispatcher(PATH_ADMIN).include(request, response);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        try {
            // Nuxeo controller
            NuxeoController nuxeoController = new NuxeoController(request, response, this.getPortletContext());

            // Portal controller context
            PortalControllerContext portalControllerContext = nuxeoController.getPortalCtx();
            // Current window
            PortalWindow window = WindowFactory.getWindow(request);

            // Fragment type identifier
            String fragmentTypeId = window.getProperty(FRAGMENT_TYPE_ID_WINDOW_PROPERTY);
            if (StringUtils.isNotEmpty(fragmentTypeId)) {
                // Fragment type
                FragmentType fragmentType = this.customizer.getFragmentTypes(Locale.getDefault()).get(fragmentTypeId);
                if (fragmentType != null) {

                    ClassLoader restoreLoader = null;

                    try {
                        // save current class loader

                        if (fragmentType.getModule() instanceof PluginModule) {
                            restoreLoader = Thread.currentThread().getContextClassLoader();
                            Thread.currentThread().setContextClassLoader(((PluginModule) fragmentType.getModule()).getCl());
                        }

                        fragmentType.getModule().doView(portalControllerContext);

                    } finally {
                        if (restoreLoader != null) {
                            Thread.currentThread().setContextClassLoader(restoreLoader);
                        }
                    }


                    request.setAttribute("fragmentType", fragmentType);
                    if (fragmentType.getModule().getViewJSPName() != null) {
                        String jspName = "fragment-view-" + fragmentType.getModule().getViewJSPName();
                    }
                }
            }

            response.setContentType("text/html");
            this.getPortletContext().getRequestDispatcher(PATH_VIEW).include(request, response);
        } catch (NuxeoException e) {
            PortletErrorHandler.handleGenericErrors(response, e);
        } catch (PortletException e) {
            throw e;
        } catch (Exception e) {
            throw new PortletException(e);
        }
    }

}
