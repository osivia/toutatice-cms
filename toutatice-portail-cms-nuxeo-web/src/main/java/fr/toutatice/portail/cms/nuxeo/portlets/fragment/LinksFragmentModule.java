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

import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.IFragmentModule;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.LinksEditableWindow;

/**
 * Display links of a current page.
 *
 * @author Loïc Billon
 * @see IFragmentModule
 */
public class LinksFragmentModule implements IFragmentModule {

    /** Links fragment identifier. */
    public static final String ID = "links_property";

    /** View JSP name. */
    private static final String VIEW_JSP_NAME = "links";
    /** Ref URI. */
    private static final String REF_URI = "refURI";
    /** Template. */
    private static final String TEMPLATE = "linksTemplate";

    /** Singleton instance. */
    private static IFragmentModule instance;


    /**
     * Private constructor.
     */
    private LinksFragmentModule() {
        super();
    }


    /**
     * Get singleton instance.
     *
     * @return singleton instance
     */
    public static IFragmentModule getInstance() {
        if (instance == null) {
            instance = new LinksFragmentModule();
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
                    List<LinkFragmentBean> links = new ArrayList<LinkFragmentBean>();

                    // Content
                    Object content = document.getProperties().get(linksSchema);
                    if (content instanceof PropertyList) {
                        PropertyList dataContents = (PropertyList) content;
                        if ((dataContents != null) && (dataContents.size() > 0)) {
                            for (int index = 0; index < dataContents.size(); index++) {
                                PropertyMap propertyMap = dataContents.getMap(index);

                                String refURIValue = (String) propertyMap.get(REF_URI);
                                String href = propertyMap.getString(LinkFragmentBean.HREF_PROPERTY);
                                if (refURI.equalsIgnoreCase(refURIValue) && StringUtils.isNotBlank(href)) {
                                    // Link
                                    LinkFragmentBean link = new LinkFragmentBean(nuxeoController.getLinkFromNuxeoURL(href));

                                    // Title
                                    link.setTitle(propertyMap.getString(LinkFragmentBean.TITLE_PROPERTY));

                                    // Glyphicon
                                    link.setGlyphicon(propertyMap.getString(LinkFragmentBean.ICON_PROPERTY));

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
