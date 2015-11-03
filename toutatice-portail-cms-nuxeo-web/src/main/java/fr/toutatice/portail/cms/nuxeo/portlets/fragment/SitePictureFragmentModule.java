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
package fr.toutatice.portail.cms.nuxeo.portlets.fragment;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.portlet.ActionRequest;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.urls.Link;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.IFragmentModule;

/**
 * Site picture fragment module.
 *
 * @see IFragmentModule
 */
public class SitePictureFragmentModule implements IFragmentModule {

    /** Site picture fragment identifier. */
    public static final String ID = "site_picture";

    /** Nuxeo path window property name. */
    public static final String NUXEO_PATH_WINDOW_PROPERTY = Constants.WINDOW_PROP_URI;
    /** Target path window property name. */
    public static final String TARGET_PATH_WINDOW_PROPERTY = "osivia.targetPath";

    /** Admin JSP name. */
    private static final String ADMIN_JSP_NAME = "site-picture";
    /** View JSP name. */
    private static final String VIEW_JSP_NAME = "picture";

    /** Singleton instance. */
    private static IFragmentModule instance;


    /**
     * Private constructor.
     */
    private SitePictureFragmentModule() {
        super();
    }


    /**
     * Get singleton instance.
     *
     * @return singleton instance
     */
    public static IFragmentModule getInstance() {
        if (instance == null) {
            instance = new SitePictureFragmentModule();
        }
        return instance;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void doView(PortalControllerContext portalControllerContext) throws PortletException {
        // Request
        PortletRequest request = portalControllerContext.getRequest();
        // Response
        PortletResponse response = portalControllerContext.getResponse();
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(request, response, portalControllerContext.getPortletCtx());

        // Current window
        PortalWindow window = WindowFactory.getWindow(request);
        // Nuxeo path
        String nuxeoPath = window.getProperty(NUXEO_PATH_WINDOW_PROPERTY);
        // Target path
        String targetPath = window.getProperty(TARGET_PATH_WINDOW_PROPERTY);

        if (StringUtils.isNotEmpty(nuxeoPath)) {
            // Computed path
            nuxeoPath = nuxeoController.getComputedPath(nuxeoPath);

            try {
                nuxeoPath = URLEncoder.encode(nuxeoPath, CharEncoding.UTF_8);
            } catch (UnsupportedEncodingException e) {
                request.setAttribute("messageKey", "ERROR_GENERIC_MESSAGE");
            }

            // Image source
            StringBuilder imageSource = new StringBuilder();
            imageSource.append(request.getContextPath());
            imageSource.append("/sitepicture?path=");
            imageSource.append(nuxeoPath);
            request.setAttribute("imageSource", imageSource.toString());


            if (targetPath != null) {
                // Computed target path
                targetPath = nuxeoController.getComputedPath(targetPath);

                // Link
                Link link = nuxeoController.getCMSLinkByPath(targetPath, null);
                request.setAttribute("link", link);
            }
        } else {
            request.setAttribute("messageKey", "MESSAGE_PATH_UNDEFINED");
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void doAdmin(PortalControllerContext portalControllerContext) throws PortletException {
        // Request
        PortletRequest request = portalControllerContext.getRequest();

        // Current window
        PortalWindow window = WindowFactory.getWindow(request);

        // Nuxeo path
        String nuxeoPath = window.getProperty(NUXEO_PATH_WINDOW_PROPERTY);
        request.setAttribute("nuxeoPath", nuxeoPath);

        // Target path
        String targetPath = window.getProperty(TARGET_PATH_WINDOW_PROPERTY);
        request.setAttribute("targetPath", targetPath);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void processAction(PortalControllerContext portalControllerContext) throws PortletException {
        // Request
        PortletRequest request = portalControllerContext.getRequest();

        if ("admin".equals(request.getPortletMode().toString()) && "save".equals(request.getParameter(ActionRequest.ACTION_NAME))) {
            // Current window
            PortalWindow window = WindowFactory.getWindow(request);

            // Nuxeo path
            window.setProperty(NUXEO_PATH_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("nuxeoPath")));

            // Target path
            window.setProperty(TARGET_PATH_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("targetPath")));
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDisplayedInAdmin() {
        return true;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getViewJSPName() {
        return VIEW_JSP_NAME;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getAdminJSPName() {
        return ADMIN_JSP_NAME;
    }

}
