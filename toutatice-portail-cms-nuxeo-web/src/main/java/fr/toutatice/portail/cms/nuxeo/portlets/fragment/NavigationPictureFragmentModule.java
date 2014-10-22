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
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSObjectPath;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.context.ControllerContextAdapter;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.IFragmentModule;

/**
 * Navigation picture fragment module.
 *
 * @see IFragmentModule
 */
public class NavigationPictureFragmentModule implements IFragmentModule {

    /** Navigation picture fragment identifier. */
    public static final String ID = "navigation_picture";

    /** Property name window property name. */
    public static final String PROPERTY_NAME_WINDOW_PROPERTY = "osivia.propertyName";

    /** Admin JSP name. */
    private static final String ADMIN_JSP_NAME = "navigation-picture";
    /** View JSP name. */
    private static final String VIEW_JSP_NAME = "picture";

    /** Singleton instance. */
    private static IFragmentModule instance;


    /**
     * Private constructor.
     */
    private NavigationPictureFragmentModule() {
        super();
    }


    /**
     * Get singleton instance.
     *
     * @return singleton instance
     */
    public static IFragmentModule getInstance() {
        if (instance == null) {
            instance = new NavigationPictureFragmentModule();
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

        // Navigation context
        CMSServiceCtx cmsReadNavContext = new CMSServiceCtx();
        cmsReadNavContext.setControllerContext(ControllerContextAdapter.getControllerContext(nuxeoController.getPortalCtx()));
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
    public void processAdminAction(PortalControllerContext portalControllerContext) throws PortletException {
        // Request
        PortletRequest request = portalControllerContext.getRequest();

        // Current window
        PortalWindow window = WindowFactory.getWindow(request);

        // Property name
        window.setProperty(PROPERTY_NAME_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("propertyName")));
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
     * @param ctx nuxeo controller
     * @param navCtx CMS context
     * @param propertyName property name
     * @return picture document
     * @throws PortletException
     */
    private Document computePicture(NuxeoController ctx, CMSServiceCtx navCtx, String propertyName) throws PortletException {
        try {
            Document pictureContainer = null;
            boolean hasPicture = false;

            String pathToCheck = ctx.getNavigationPath();

            // On regarde dans le document courant
            Document currentDoc = ctx.fetchDocument(ctx.getContentPath());

            if (this.docHasPicture(currentDoc, propertyName)) {
                return currentDoc;
            } else {
                // Puis dans l'arbre de navigation
                do {
                    CMSItem cmsItemNav = NuxeoController.getCMSService().getPortalNavigationItem(navCtx, ctx.getSpacePath(), pathToCheck);
                    if ((cmsItemNav != null) && (cmsItemNav.getNativeItem() != null)) {
                        pictureContainer = (Document) cmsItemNav.getNativeItem();
                        hasPicture = this.docHasPicture(pictureContainer, propertyName);
                    }

                    // One level up
                    CMSObjectPath parentPath = CMSObjectPath.parse(pathToCheck).getParent();
                    pathToCheck = parentPath.toString();
                } while (!hasPicture && pathToCheck.contains(ctx.getSpacePath()));
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
        boolean hasPicture;
        PropertyMap picture = (PropertyMap) currentDoc.getProperties().get(propertyName);
        hasPicture = (picture != null) && (picture.get("data") != null);
        return hasPicture;
    }

}
