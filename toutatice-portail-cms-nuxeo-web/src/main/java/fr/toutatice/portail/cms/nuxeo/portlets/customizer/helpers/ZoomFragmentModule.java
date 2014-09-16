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
package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.html.HTMLConstants;
import org.osivia.portal.api.windows.PortalWindow;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.IFragmentModule;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.ZoomEditableWindow;

/**
 * Display zooms of a current page.
 *
 * @author Loïc Billon
 * @see IFragmentModule
 */
public class ZoomFragmentModule implements IFragmentModule {

    /** Name. */
    public static final String ID = "zoom_property";
    /** Module description. */
    public static final String DESC = "Zoom";
    /** View JSP name. */
    public static final String JSP = "zoom";
    /** Admin JSP name. */
    public static final String ADMIN_JSP = "zoom";


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


    /**
     * Default constructor.
     */
    public ZoomFragmentModule() {
        super();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void injectViewAttributes(NuxeoController nuxeoController, PortalWindow window, PortletRequest request, RenderResponse response) throws Exception {
        // Nuxeo path
        String nuxeoPath = window.getProperty(Constants.WINDOW_PROP_URI);
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
                String refURI = window.getProperty("osivia.refURI");
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
                                    String url = propertyMap.getString(HREF);
                                    if (StringUtils.startsWith(url, "/")) {
                                        url = nuxeoController.getCMSLinkByPath(url, null).getUrl();
                                    } else if (StringUtils.isNotBlank(url)) {
                                        request.setAttribute("external", true);
                                    } else {
                                        url = HTMLConstants.A_HREF_DEFAULT;
                                    }
                                    request.setAttribute("url", url);

                                    // Image source
                                    String imageSource = null;
                                    if (StringUtils.isNotBlank(propertyMap.getString(PICTURE))) {
                                        imageSource = nuxeoController.createAttachedPictureLink(nuxeoPath, propertyMap.getString(PICTURE));
                                    }
                                    request.setAttribute("imageSource", imageSource);

                                    // Content
                                    request.setAttribute("content", propertyMap.getString(CONTENT));

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
    public void injectAdminAttributes(NuxeoController ctx, PortalWindow window, PortletRequest request, RenderResponse response) throws Exception {
        String nuxeoPath = window.getProperty(Constants.WINDOW_PROP_URI);
        request.setAttribute("nuxeoPath", StringUtils.trimToEmpty(nuxeoPath));

        String scope = window.getProperty("osivia.cms.forcePublicationScope");
        request.setAttribute("scope", scope);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void processAdminAttributes(NuxeoController ctx, PortalWindow window, ActionRequest request, ActionResponse res) throws Exception {
        if (request.getParameter("nuxeoPath") != null) {
            window.setProperty(Constants.WINDOW_PROP_URI, request.getParameter("nuxeoPath"));
        }

        if ((request.getParameter("scope") != null) && (request.getParameter("scope").length() > 0)) {
            window.setProperty("osivia.cms.forcePublicationScope", request.getParameter("scope"));
        } else if (window.getProperty("osivia.cms.forcePublicationScope") != null) {
            window.setProperty("osivia.cms.forcePublicationScope", null);
        }
    }

}
