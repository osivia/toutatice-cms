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

import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderResponse;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.urls.Link;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.IFragmentModule;

/**
 * Document attachment picture fragment module.
 *
 * @see IFragmentModule
 */
public class DocumentPictureFragmentModule implements IFragmentModule {

    /** Document picture fragment identifier. */
    public static final String ID = "document_picture";

    /** Nuxeo path window property name. */
    public static final String NUXEO_PATH_WINDOW_PROPERTY = Constants.WINDOW_PROP_URI;
    /** Property name window property name. */
    public static final String PROPERTY_NAME_WINDOW_PROPERTY = "osivia.propertyName";
    /** Scope window property name. */
    public static final String SCOPE_WINDOW_PROPERTY = "osivia.cms.forcePublicationScope";
    /** Target path window property name. */
    public static final String TARGET_PATH_WINDOW_PROPERTY = "osivia.targetPath";

    /** Admin JSP name. */
    private static final String ADMIN_JSP_NAME = "document-picture";
    /** View JSP name. */
    private static final String VIEW_JSP_NAME = "picture";

    /** Singleton instance. */
    private static IFragmentModule instance;


    /**
     * Private constructor.
     */
    private DocumentPictureFragmentModule() {
        super();
    }


    /**
     * Get singleton instance.
     *
     * @return singleton instance
     */
    public static IFragmentModule getInstance() {
        if (instance == null) {
            instance = new DocumentPictureFragmentModule();
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
        RenderResponse response = (RenderResponse) portalControllerContext.getResponse();
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(request, response, portalControllerContext.getPortletCtx());

        // Current window
        PortalWindow window = WindowFactory.getWindow(request);
        // Nuxeo path
        String nuxeoPath = window.getProperty(NUXEO_PATH_WINDOW_PROPERTY);
        // Property name
        String propertyName = window.getProperty(PROPERTY_NAME_WINDOW_PROPERTY);
        // Target path
        String targetPath = window.getProperty(TARGET_PATH_WINDOW_PROPERTY);

        if (StringUtils.isNotEmpty(nuxeoPath)) {
            
            // Computed path
            nuxeoPath = nuxeoController.getComputedPath(nuxeoPath);

            // Fetch Nuxeo document
            Document document = nuxeoController.fetchDocument(nuxeoPath);
            nuxeoController.setCurrentDoc(document);
            
            
            if (StringUtils.startsWith(nuxeoPath, "/nuxeo/")) {
                // Portal path
                String portalPath = nuxeoController.transformNuxeoLink(nuxeoPath);
                request.setAttribute("imageSource", portalPath);
            } else {
                // Title
                if (StringUtils.isNotBlank(document.getTitle())) {
                    response.setTitle(document.getTitle());
                }

                if (StringUtils.isNotEmpty(propertyName)) {
                    boolean emptyContent = true;
                    PropertyMap map = document.getProperties().getMap(propertyName);

                    if (map != null) {
                        String pathFile = map.getString("data");

                        if (pathFile != null) {
                           
                            // Image source
                            String imageSource = nuxeoController.createFileLink(document, propertyName);
                            request.setAttribute("imageSource", imageSource);

                            emptyContent = false;
                        }
                    }

                    if (emptyContent) {
                        request.setAttribute("osivia.emptyResponse", "1");
                    }
                } else {
                    request.setAttribute("messageKey", "FRAGMENT_MESSAGE_PROPERTY_UNDEFINED");
                }
            }


            // Target path
            if (StringUtils.isNotEmpty(targetPath)) {
                Link link = nuxeoController.getLinkFromNuxeoURL(targetPath);
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
        // Response
        PortletResponse response = portalControllerContext.getResponse();
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(request, response, portalControllerContext.getPortletCtx());

        // Current window
        PortalWindow window = WindowFactory.getWindow(request);

        // Nuxeo path
        String nuxeoPath = window.getProperty(NUXEO_PATH_WINDOW_PROPERTY);
        request.setAttribute("nuxeoPath", nuxeoPath);

        // Property name
        String propertyName = window.getProperty(PROPERTY_NAME_WINDOW_PROPERTY);
        request.setAttribute("propertyName", propertyName);

        // Scope
        String scope = window.getProperty(SCOPE_WINDOW_PROPERTY);
        String scopes = nuxeoController.formatScopeList(scope);
        request.setAttribute("scopes", scopes);

        // Target path
        String targetPath = window.getProperty(TARGET_PATH_WINDOW_PROPERTY);
        request.setAttribute("targetPath", targetPath);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void processAdminAction(PortalControllerContext portalControllerContext) throws PortletException {
        // Request
        PortletRequest request = portalControllerContext.getRequest();

        // Current window
        PortalWindow window = WindowFactory.getWindow(request);

        // Nuxeo path
        window.setProperty(NUXEO_PATH_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("nuxeoPath")));

        // Property name
        window.setProperty(PROPERTY_NAME_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("propertyName")));

        // Scope
        window.setProperty(SCOPE_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("scope")));

        // Target path
        window.setProperty(TARGET_PATH_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("targetPath")));
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
