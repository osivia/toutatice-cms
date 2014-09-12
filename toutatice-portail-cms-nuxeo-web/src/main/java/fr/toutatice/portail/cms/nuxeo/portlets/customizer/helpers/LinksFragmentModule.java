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

import java.util.ArrayList;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.windows.PortalWindow;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.IFragmentModule;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.Link;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.LinksEditableWindow;

/**
 * Display links of a current page.
 *
 * @author Loïc Billon
 * @see IFragmentModule
 */
public class LinksFragmentModule implements IFragmentModule {

    /** name. */
    public static final String ID = "links_property";
    /** description of module. */
    public static final String DESC = "Liste de liens";
    /** jsp view-zoom in portlet fragment. */
    public static final String JSP = "links";
    /** jsp admin property in portlet fragment. */
    public static final String ADMIN_JSP = "links";

    /** Ref URI. */
    private static final String REF_URI = "refURI";
    /** Template. */
    private static final String TEMPLATE = "linksTemplate";


    /**
     * Default constructor.
     */
    public LinksFragmentModule() {
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

            // Ref URI
            String refURI = window.getProperty("osivia.refURI");
            if (StringUtils.isNotEmpty(refURI)) {
                // Links schema
                String linksSchema = LinksEditableWindow.LINKS_SCHEMA;
                if (StringUtils.isNotEmpty(linksSchema)) {
                    List<Link> links = new ArrayList<Link>();

                    // Content
                    Object content = document.getProperties().get(linksSchema);
                    if (content instanceof PropertyList) {
                        PropertyList dataContents = (PropertyList) content;
                        if ((dataContents != null) && (dataContents.size() > 0)) {
                            for (int index = 0; index < dataContents.size(); index++) {
                                PropertyMap propertyMap = dataContents.getMap(index);

                                String refURIValue = (String) propertyMap.get(REF_URI);
                                if (refURI.equalsIgnoreCase(refURIValue) && StringUtils.isNotBlank(propertyMap.getString(Link.HREF))) {
                                    Link link = new Link();

                                    // HREF
                                    String href = propertyMap.getString(Link.HREF);
                                    if (StringUtils.isNotBlank(href) && href.startsWith("/")) {
                                        href = nuxeoController.getCMSLinkByPath(href, null).getUrl();
                                    }
                                    link.setHref(href);

                                    // Title
                                    link.setTitle(propertyMap.getString(Link.TITLE));

                                    // Glyphicon
                                    link.setIcon(propertyMap.getString(Link.ICON));

                                    links.add(link);
                                }
                            }
                        }
                    }

                    if (!links.isEmpty()) {
                        request.setAttribute("links", links);
                        emptyContent = false;
                    }
                }

                // links fragments schema
                String linksFragmentsSchema = LinksEditableWindow.LINKS_FGT_SCHEMA;
                if (StringUtils.isNotEmpty(linksFragmentsSchema)) {
                    // Content
                    Object content = document.getProperties().get(linksFragmentsSchema);
                    if (content instanceof PropertyList) {
                        PropertyList dataContents = (PropertyList) content;
                        if ((dataContents != null) && (dataContents.size() > 0)) {
                            for (int index = 0; index < dataContents.size(); index++) {
                                PropertyMap propertyMap = dataContents.getMap(index);

                                String refURIValue = (String) propertyMap.get(REF_URI);
                                if (refURI.equalsIgnoreCase(refURIValue)) {
                                    // Template
                                    request.setAttribute("template", propertyMap.getString(TEMPLATE));

                                    break;
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

        request.setAttribute("propertyName", LinksEditableWindow.LINKS_SCHEMA);

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
