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

import javax.portlet.ActionRequest;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderResponse;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.cms.NuxeoDocumentContext;
import fr.toutatice.portail.cms.nuxeo.api.fragment.FragmentModule;

/**
 * Property fragment module.
 *
 * @see FragmentModule
 */
public class PropertyFragmentModule extends FragmentModule {

    /** Text property fragment identifier. */
    public static final String TEXT_ID = "text_property";
    /** HTML property fragment identifier. */
    public static final String HTML_ID = "html_property";

    /** Nuxeo path window property name. */
    public static final String NUXEO_PATH_WINDOW_PROPERTY = Constants.WINDOW_PROP_URI;
    /** Property name window property name. */
    public static final String PROPERTY_NAME_WINDOW_PROPERTY = "osivia.propertyName";
    /** Scope window property name. */
    public static final String SCOPE_WINDOW_PROPERTY = "osivia.cms.forcePublicationScope";
    /** CMS menu display indicator window property name. */
    public static final String CMS_MENU_DISPLAY_WINDOW_PROPERTY = "osivia.cms.menu";
    /** Reference URI window property name. */
    public static final String REF_URI_WINDOW_PROPERTY = "osivia.refURI";

    /** JSP name. */
    private static final String JSP_NAME = "property";
    /** Reference URI. */
    private static final String REF_URI = "refURI";


    /** HTML content indicator. */
    private final boolean html;


    /**
     * Constructor.
     *
     * @param portletContext portlet context
     * @param html HTML content indicator
     */
    public PropertyFragmentModule(PortletContext portletContext, boolean html) {
        super(portletContext);
        this.html = html;
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
        // CMS menu display indicator
        boolean cmsMenuDisplay = BooleanUtils.toBoolean(window.getProperty(CMS_MENU_DISPLAY_WINDOW_PROPERTY));
        // Reference URI
        String refURI = window.getProperty(REF_URI_WINDOW_PROPERTY);

        if (StringUtils.isNotEmpty(nuxeoPath)) {
            // Computed path
            nuxeoPath = nuxeoController.getComputedPath(nuxeoPath);

            // Nuxeo document
            NuxeoDocumentContext documentContext = nuxeoController.getDocumentContext(nuxeoPath);
            Document document = documentContext.getDocument();
            nuxeoController.setCurrentDoc(document);

            // Title
            if (StringUtils.isNotBlank(document.getTitle())) {
                response.setTitle(document.getTitle());
            }

            if (StringUtils.isNotEmpty(propertyName)) {
                // Property value
                String propertyValue;

                if (this.html && StringUtils.isNotEmpty(refURI)) {
                    propertyValue = StringUtils.EMPTY;

                    Object object = document.getProperties().get(propertyName);
                    if (object instanceof PropertyList) {
                        PropertyList propertyList = (PropertyList) object;
                        if ((propertyList != null) && !propertyList.isEmpty()) {
                            for (int i = 0; i < propertyList.size(); i++) {
                                PropertyMap propertyMap = propertyList.getMap(i);
                                String refURIValue = propertyMap.getString(REF_URI);

                                if (refURI.equalsIgnoreCase(refURIValue)) {
                                    propertyValue = propertyMap.getString("data");
                                    break;
                                }
                            }
                        }
                    } else {
                        request.setAttribute("messageKey", "FRAGMENT_MESSAGE_INVALID_COMPLEX_PROPERTY");
                    }
                } else {
                    propertyValue = document.getProperties().getString(propertyName);
                }

                if (this.html) {
                    // Transform HTML content
                    propertyValue = nuxeoController.transformHTMLContent(propertyValue);
                } else if ("dc:description".equals(propertyName)) {
                    // Add CSS classes
                    propertyValue = "<div class=\"text-left text-pre-wrap\">" + propertyValue + "</div>";
                }

                request.setAttribute("content", propertyValue);

                if (cmsMenuDisplay) {
                    nuxeoController.insertContentMenuBarItems();
                }

            } else {
                request.setAttribute("messageKey", "FRAGMENT_MESSAGE_PROPERTY_UNDEFINED");
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

        // CMS menu display indicator
        boolean cmsMenuDisplay = BooleanUtils.toBoolean(window.getProperty(CMS_MENU_DISPLAY_WINDOW_PROPERTY));
        request.setAttribute("cmsMenu", cmsMenuDisplay);
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

            // Property name
            window.setProperty(PROPERTY_NAME_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("propertyName")));

            // Scope
            window.setProperty(SCOPE_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("scope")));

            // CMS menu display indicator
            window.setProperty(CMS_MENU_DISPLAY_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("cmsMenu")));
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
        return JSP_NAME;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getAdminJSPName() {
        return JSP_NAME;
    }

}
