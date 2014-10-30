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

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.urls.Link;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.IFragmentModule;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.ZoomEditableWindow;

/**
 * Display zooms of a current page.
 *
 * @author Loïc Billon
 * @see IFragmentModule
 */
public class ZoomFragmentModule implements IFragmentModule {

    /** Zoom fragment identifier. */
    public static final String ID = "zoom_property";

    /** Nuxeo path window property name. */
    public static final String NUXEO_PATH_WINDOW_PROPERTY = Constants.WINDOW_PROP_URI;
    /** Scope window property name. */
    public static final String SCOPE_WINDOW_PROPERTY = "osivia.cms.forcePublicationScope";
    /** Reference URI window property name. */
    public static final String REF_URI_WINDOW_PROPERTY = "osivia.refURI";

    /** View JSP name. */
    private static final String VIEW_JSP_NAME = "zoom";
    /** Ref URI. */
    private static final String REF_URI = "refURI";
    /** HREF. */
    private static final String HREF = "href";
    /** Content. */
    private static final String CONTENT = "content";
    /** Picture. */
    private static final String PICTURE = "picture";
    /** Template. */
    private static final String TEMPLATE = "zoomTemplate";

    /** Singleton instance. */
    private static IFragmentModule instance;


    /**
     * Private constructor.
     */
    private ZoomFragmentModule() {
        super();
    }


    /**
     * Get singleton instance.
     *
     * @return singleton instance
     */
    public static IFragmentModule getInstance() {
        if (instance == null) {
            instance = new ZoomFragmentModule();
        }
        return instance;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void doView(PortalControllerContext portalControllerContext) throws PortletException {
        // Request
        RenderRequest request = (RenderRequest) portalControllerContext.getRequest();
        // Response
        RenderResponse response = (RenderResponse) portalControllerContext.getResponse();
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(request, response, portalControllerContext.getPortletCtx());

        // Current window
        PortalWindow window = WindowFactory.getWindow(request);
        // Nuxeo path
        String nuxeoPath = window.getProperty(NUXEO_PATH_WINDOW_PROPERTY);
        // Empty content indicator
        boolean emptyContent = true;


        if (StringUtils.isNotEmpty(nuxeoPath)) {
            nuxeoPath = nuxeoController.getComputedPath(nuxeoPath);

            // Fetch document
            Document document = nuxeoController.fetchDocument(nuxeoPath);

            // Title
            if (document.getTitle() != null) {
                response.setTitle(document.getTitle());
            }

            // Zoom schema
            String schema = ZoomEditableWindow.ZOOM_SCHEMA;
            if (StringUtils.isNotEmpty(schema)) {
                // Ref URI
                String refURI = window.getProperty(REF_URI_WINDOW_PROPERTY);
                if (StringUtils.isNotEmpty(refURI)) {
                    // Content
                    Object content = document.getProperties().get(schema);
                    if (content instanceof PropertyList) {
                        PropertyList propertyList = (PropertyList) content;
                        if ((propertyList != null) && (propertyList.size() > 0)) {
                            for (int index = 0; index < propertyList.size(); index++) {
                                PropertyMap propertyMap = propertyList.getMap(index);

                                String refURIValue = (String) propertyMap.get(REF_URI);
                                if (refURI.equalsIgnoreCase(refURIValue)) {
                                    // Template
                                    request.setAttribute("template", propertyMap.getString(TEMPLATE));

                                    // Title
                                    request.setAttribute("title", window.getProperty("osivia.title"));

                                    // URL
                                    String href = propertyMap.getString(HREF);
                                    Link link = nuxeoController.getLinkFromNuxeoURL(href);
                                    request.setAttribute("url", link.getUrl());

                                    // Image source
                                    String imageSource = null;
                                    if (StringUtils.isNotBlank(propertyMap.getString(PICTURE))) {
                                        imageSource = nuxeoController.createAttachedPictureLink(nuxeoPath, propertyMap.getString(PICTURE));
                                    }
                                    request.setAttribute("imageSource", imageSource);

                                    // Content
                                    request.setAttribute("content",
                                            nuxeoController.transformHTMLContent(StringUtils.trimToEmpty(propertyMap.getString(CONTENT))));

                                    emptyContent = false;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (emptyContent) {
            request.setAttribute("osivia.emptyResponse", "1");
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void doAdmin(PortalControllerContext portalControllerContext) throws PortletException {
        // Do nothing
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void processAdminAction(PortalControllerContext portalControllerContext) throws PortletException {
        // Do nothing
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDisplayedInAdmin() {
        return false;
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
        return null;
    }

}
