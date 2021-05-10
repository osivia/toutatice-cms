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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSObjectPath;
import org.osivia.portal.core.cms.CMSServiceCtx;


import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.cms.NuxeoDocumentContext;
import fr.toutatice.portail.cms.nuxeo.api.fragment.FragmentModule;

/**
 * Navigation picture fragment module.
 *
 * @see FragmentModule
 */
public class NavigationPictureFragmentModule extends FragmentModule {

    /** Navigation picture fragment identifier. */
    public static final String ID = "navigation_picture";

    /** Property name window property name. */
    public static final String PROPERTY_NAME_WINDOW_PROPERTY = "osivia.propertyName";

    /** Admin JSP name. */
    private static final String ADMIN_JSP_NAME = "navigation-picture";
    /** View JSP name. */
    private static final String VIEW_JSP_NAME = "picture";


    /**
     * Constructor.
     *
     * @param portletContext portlet context
     */
    public NavigationPictureFragmentModule(PortletContext portletContext) {
        super(portletContext);
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

        // Navigation context
        CMSServiceCtx cmsReadNavContext = new CMSServiceCtx();
        cmsReadNavContext.setPortalControllerContext(nuxeoController.getPortalCtx());
        cmsReadNavContext.setScope(nuxeoController.getNavigationScope());

        String propertyName = window.getProperty(PROPERTY_NAME_WINDOW_PROPERTY);
        if (StringUtils.isNotEmpty(nuxeoController.getNavigationPath())) {
            if (StringUtils.isNotEmpty(propertyName)) {
                Document navigationPictureContainer = this.computePicture(nuxeoController, cmsReadNavContext, propertyName);
                if (navigationPictureContainer != null) {
                    String imageSource = nuxeoController.createFileLink(navigationPictureContainer, propertyName);
                    request.setAttribute("imageSource", imageSource);
                } else {
                    request.setAttribute("osivia.emptyResponse", "1");
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

        // Current window
        PortalWindow window = WindowFactory.getWindow(request);

        // Property name
        String propertyName = window.getProperty(PROPERTY_NAME_WINDOW_PROPERTY);
        request.setAttribute("propertyName", propertyName);
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

            // Property name
            window.setProperty(PROPERTY_NAME_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("propertyName")));
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


    /**
     * Compute picture.
     *
     * @param nuxeoController nuxeo controller
     * @param navCtx CMS context
     * @param propertyName property name
     * @return picture document
     * @throws PortletException
     */
    private Document computePicture(NuxeoController nuxeoController, CMSServiceCtx navCtx, String propertyName) throws PortletException {
        try {
            Document pictureContainer = null;
            boolean hasPicture = false;

            String pathToCheck = nuxeoController.getNavigationPath();

            // Nuxeo document
            NuxeoDocumentContext documentContext = nuxeoController.getDocumentContext(nuxeoController.getContentPath());
            Document document = documentContext.getDocument();

            if (this.docHasPicture(document, propertyName)) {
                return document;
            } else {
                // Puis dans l'arbre de navigation
                do {
                    CMSItem cmsItemNav = NuxeoController.getCMSService().getPortalNavigationItem(navCtx, nuxeoController.getSpacePath(), pathToCheck);
                    if ((cmsItemNav != null) && (cmsItemNav.getNativeItem() != null)) {
                        pictureContainer = (Document) cmsItemNav.getNativeItem();
                        hasPicture = this.docHasPicture(pictureContainer, propertyName);
                    }

                    // One level up
                    CMSObjectPath parentPath = CMSObjectPath.parse(pathToCheck).getParent();
                    pathToCheck = parentPath.toString();
                } while (!hasPicture && pathToCheck.contains(nuxeoController.getSpacePath()));
            }
            if (hasPicture) {
                return pictureContainer;
            } else {
                return null;
            }
        } catch (CMSException e) {
            throw new PortletException(e);
        }
    }


    /**
     * Check if document has picture.
     *
     * @param currentDoc document
     * @param propertyName property name
     * @return true if document has picture
     */
    private boolean docHasPicture(Document currentDoc, String propertyName) {
        // Picture property map
        PropertyMap picture = (PropertyMap) currentDoc.getProperties().get(propertyName);

        return (picture != null) && (NumberUtils.toLong(picture.getString("length")) > 0);
    }

}
