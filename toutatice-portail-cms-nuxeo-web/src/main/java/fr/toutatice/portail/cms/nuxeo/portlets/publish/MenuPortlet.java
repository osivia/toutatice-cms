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
package fr.toutatice.portail.cms.nuxeo.portlets.publish;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.RenderMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jboss.portal.core.controller.ControllerContext;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.api.urls.Link;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSItemType;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.ICMSService;
import org.osivia.portal.core.context.ControllerContextAdapter;
import org.osivia.portal.core.security.CmsPermissionHelper;

import fr.toutatice.portail.cms.nuxeo.api.CMSPortlet;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.PortletErrorHandler;

/**
 * Publication menu portlet.
 *
 * @see CMSPortlet
 */
public class MenuPortlet extends CMSPortlet {

    /** Horizontal menu window property name. */
    private static final String HORIZONTAL_WINDOW_PROPERTY = "osivia.cms.horizontal";
    /** Max levels window property name. */
    private static final String MAX_LEVELS_WINDOW_PROPERTY = "osivia.cms.maxLevels";
    /** Start level window property name. */
    private static final String START_LEVEL_WINDOW_PROPERTY = "osivia.cms.startLevel";
    /** Open levels window property name. */
    private static final String OPEN_LEVELS_WINDOW_PROPERTY = "osivia.cms.openLevels";

    /** Default max levels. */
    private static final int DEFAULT_MAX_LEVELS = 3;
    /** Default start level. */
    private static final int DEFAULT_START_LEVEL = 1;
    /** Default open levels. */
    private static final int DEFAULT_OPEN_LEVELS = 1;


    /** View path. */
    private static final String PATH_VIEW = "/WEB-INF/jsp/publish/view.jsp";
    /** Admin path. */
    private static final String PATH_ADMIN = "/WEB-INF/jsp/publish/admin.jsp";

    /** Portal URL factory. */
    private IPortalUrlFactory portalUrlFactory;


    /**
     * Default constructor.
     */
    public MenuPortlet() {
        super();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void init(PortletConfig config) throws PortletException {
        super.init(config);

        // Portal URL factory
        this.portalUrlFactory = (IPortalUrlFactory) this.getPortletContext().getAttribute("UrlService");
        if (this.portalUrlFactory == null) {
            throw new PortletException("Cannot start TestPortlet due to service unavailability");
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void processAction(ActionRequest request, ActionResponse response) throws IOException, PortletException {
        String action = request.getParameter(ActionRequest.ACTION_NAME);

        if ("admin".equals(request.getPortletMode().toString())) {
            if ("save".equals(action)) {
                // Save action

                // Current window
                PortalWindow window = WindowFactory.getWindow(request);

                // Horizontal menu
                boolean horizontal = BooleanUtils.toBoolean(request.getParameter("horizontal"));
                window.setProperty(HORIZONTAL_WINDOW_PROPERTY, BooleanUtils.toStringTrueFalse(horizontal));

                if (!horizontal) {
                    // Open levels
                    int openLevels = NumberUtils.toInt(request.getParameter("openLevels"));
                    if (openLevels > 0) {
                        window.setProperty(OPEN_LEVELS_WINDOW_PROPERTY, String.valueOf(openLevels));
                    } else {
                        window.setProperty(OPEN_LEVELS_WINDOW_PROPERTY, null);
                    }

                    // Start level
                    int startLevel = NumberUtils.toInt(request.getParameter("startLevel"));
                    if (startLevel > 0) {
                        window.setProperty(START_LEVEL_WINDOW_PROPERTY, String.valueOf(startLevel));
                    } else {
                        window.setProperty(START_LEVEL_WINDOW_PROPERTY, null);
                    }

                    // Max levels
                    int maxLevels = NumberUtils.toInt(request.getParameter("maxLevels"));
                    if (maxLevels > 0) {
                        window.setProperty(MAX_LEVELS_WINDOW_PROPERTY, String.valueOf(maxLevels));
                    } else {
                        window.setProperty(MAX_LEVELS_WINDOW_PROPERTY, null);
                    }

                    // JSTree display
                    boolean filtering = BooleanUtils.toBoolean(request.getParameter("jstree"));
                    window.setProperty("osivia.cms.filtering", BooleanUtils.toStringTrueFalse(filtering));
                }
            }

            response.setPortletMode(PortletMode.VIEW);
            response.setWindowState(WindowState.NORMAL);
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
        // Current window
        PortalWindow window = WindowFactory.getWindow(request);


        // Horizontal menu
        boolean horizontal = BooleanUtils.toBoolean(window.getProperty(HORIZONTAL_WINDOW_PROPERTY));
        request.setAttribute("horizontal", horizontal);

        // Open levels
        String openLevels = window.getProperty(OPEN_LEVELS_WINDOW_PROPERTY);
        request.setAttribute("openLevels", openLevels);
        request.setAttribute("defaultOpenLevels", DEFAULT_OPEN_LEVELS);

        // Start level
        String startLevel = window.getProperty(START_LEVEL_WINDOW_PROPERTY);
        request.setAttribute("startLevel", startLevel);

        // Max levels
        String maxLevels = window.getProperty(MAX_LEVELS_WINDOW_PROPERTY);
        request.setAttribute("maxLevels", maxLevels);
        request.setAttribute("defaultMaxLevels", DEFAULT_MAX_LEVELS);

        // JSTree filtering
        boolean filtering = BooleanUtils.toBoolean(window.getProperty("osivia.cms.filtering"));
        request.setAttribute("jstree", filtering);


        response.setContentType("text/html");
        this.getPortletContext().getRequestDispatcher(PATH_ADMIN).include(request, response);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        try {
            // Current window
            PortalWindow window = WindowFactory.getWindow(request);
            // Nuxeo controller
            NuxeoController nuxeoController = new NuxeoController(request, response, this.getPortletContext());

            // v2.1 : proto dafpic : on fixe le path du menu meme dans les pages de recherche (toutes les pages non contextualisées)
            String basePath = nuxeoController.getBasePath();
            String spacePath = nuxeoController.getSpacePath();


            String menuRootPath = nuxeoController.getMenuRootPath();
            if (menuRootPath != null) {
                basePath = menuRootPath;
                spacePath = menuRootPath;
            }


            // Horizontal menu
            boolean horizontal = BooleanUtils.toBoolean(window.getProperty(HORIZONTAL_WINDOW_PROPERTY));

            // Max levels
            int maxLevels = DEFAULT_MAX_LEVELS;
            String maxLevelWindowProperty = window.getProperty(MAX_LEVELS_WINDOW_PROPERTY);
            if (!horizontal && StringUtils.isNotBlank(maxLevelWindowProperty)) {
                maxLevels = NumberUtils.toInt(maxLevelWindowProperty);
            }

            // Start level
            int startLevel = DEFAULT_START_LEVEL;
            String startLevelWindowProperty = window.getProperty(START_LEVEL_WINDOW_PROPERTY);
            if (!horizontal && StringUtils.isNotBlank(startLevelWindowProperty)) {
                startLevel = NumberUtils.toInt(startLevelWindowProperty);
            }

            // Open levels
            int openLevels = DEFAULT_OPEN_LEVELS;
            String openLevelsWindowProperty = window.getProperty(OPEN_LEVELS_WINDOW_PROPERTY);
            if (!horizontal && StringUtils.isNotBlank(openLevelsWindowProperty)) {
                openLevels = NumberUtils.toInt(openLevelsWindowProperty);
            }


            if (basePath != null) {
                // Asynchronous refresh
                // nuxeoController.setAsynchronousUpdates(true);

                // Navigation context
                CMSServiceCtx cmsReadNavContext = new CMSServiceCtx();
                ControllerContext controllerContext = ControllerContextAdapter.getControllerContext(nuxeoController.getPortalCtx());
                cmsReadNavContext.setControllerContext(controllerContext);
                cmsReadNavContext.setScope(nuxeoController.getNavigationScope());

                if (CmsPermissionHelper.getCurrentCmsVersion(controllerContext).equals(CmsPermissionHelper.CMS_VERSION_PREVIEW)) {
                    cmsReadNavContext.setDisplayLiveVersion("1");
                }

                int partialOpenLevels = -1;
                CMSItem navItem = NuxeoController.getCMSService().getPortalNavigationItem(cmsReadNavContext, basePath, basePath);

                if ("1".equals(navItem.getProperties().get("partialLoading"))) {
                    partialOpenLevels = openLevels;

                }

                // Navigation display item
                NavigationDisplayItem displayItem = this.getDisplayItem(nuxeoController, cmsReadNavContext, 0, maxLevels, startLevel,
                        spacePath, basePath, basePath, true, partialOpenLevels);
                if (displayItem != null) {
                    if (displayItem.getTitle() != null) {
                        response.setTitle(displayItem.getTitle());
                    }
                    request.setAttribute("displayItem", displayItem);
                }
                request.setAttribute("startLevel", startLevel);
                request.setAttribute("openLevels", openLevels);
            }

            response.setContentType("text/html");

            // Dispatcher
            String requestDispatcherPath = PATH_VIEW;
            boolean filtering = BooleanUtils.toBoolean(window.getProperty("osivia.cms.filtering"));
            if (horizontal) {
                requestDispatcherPath = "/WEB-INF/jsp/publish/view-horizontal.jsp";
            } else if (filtering) {
                requestDispatcherPath = "/WEB-INF/jsp/publish/view-jstree.jsp";
            }
            this.getPortletContext().getRequestDispatcher(requestDispatcherPath).include(request, response);
        } catch (NuxeoException e) {
            PortletErrorHandler.handleGenericErrors(response, e);
        } catch (PortletException e) {
            throw e;
        } catch (Exception e) {
            throw new PortletException(e);
        }
    }


    /**
     * Get navigation display item.
     *
     * @param nuxeoController Nuxeo controller
     * @param cmsContext CMS context
     * @param level current level
     * @param maxLevel maximum level
     * @param startLevel start level
     * @param spacePath space path
     * @param basePath base path
     * @param nuxeoPath Nuxeo path
     * @param isParentNavigable parent navigable indicator
     * @param partialOpenLevels partial open levels indicator
     * @return navigation display item
     * @throws CMSException
     */
    private NavigationDisplayItem getDisplayItem(NuxeoController nuxeoController, CMSServiceCtx cmsContext, int level, int maxLevel, int startLevel,
            String spacePath,
            String basePath, String nuxeoPath, boolean isParentNavigable, int partialOpenLevels) throws CMSException {
        // TODO : factoriser dans NuxeoController

        // CMS service
        ICMSService cmsService = NuxeoController.getCMSService();
        // CMS item
        CMSItem navItem = cmsService.getPortalNavigationItem(cmsContext, spacePath, nuxeoPath);
        if (navItem == null) {
            return null;
        }

        // Nuxeo document
        Document doc = (Document) navItem.getNativeItem();
        // Nuxeo document link
        Link link = nuxeoController.getLink(doc, "menu");

        // Navigation path
        String navPath = navItem.getPath();
        // Item path
        String itemPath = nuxeoController.getItemNavigationPath();
        // Selected item
        boolean selected = false;
        // Current item
        boolean current = false;

        if (itemPath != null) {
            if (itemPath.startsWith(navPath) && isParentNavigable) {
                // Non navigational items are not selected because children elements are managed at portlet level and not CMS levels ; so selection can not be sure
                // See FAQ sample : links between questions don't interact with CMS
                selected = true;

                if (itemPath.equals(navPath)) {
                    current = true;
                }
            } else if (nuxeoController.getItemNavigationPath().equals(doc.getPath())) {
                // Les path proxy items de l'arbre navigation peut être filtrés à tort
                // cas du 'publier vers' de Nuxeo
                // Dans ce cas, on compare avec le nativeItem
                selected = true;
                current = true;
            }
        }

        // Navigation display item
        NavigationDisplayItem displayItem;
        if ((level + 1) >= startLevel) {
            displayItem = new NavigationDisplayItem(doc.getTitle(), link.getUrl(), link.isExternal(), selected, current, navItem);

            // Add children
            List<NavigationDisplayItem> displayChildren = this.getDisplayItemChildren(nuxeoController, cmsContext, level, maxLevel, startLevel, spacePath,
                    basePath, nuxeoPath, partialOpenLevels, navItem, doc, selected);
            displayItem.getChildren().addAll(displayChildren);
        } else if (selected) {
            displayItem = null;

            // Search selected child
            List<NavigationDisplayItem> displayItemChildren = this.getDisplayItemChildren(nuxeoController, cmsContext, level, maxLevel, startLevel, spacePath,
                    basePath, nuxeoPath, partialOpenLevels, navItem, doc, selected);

            for (NavigationDisplayItem displayItemChild : displayItemChildren) {
                if (displayItemChild.isSelected()) {
                    displayItem = displayItemChild;
                    break;
                }
            }

            if ((displayItem == null) && (level == 0)) {
                displayItem = new NavigationDisplayItem(doc.getTitle(), link.getUrl(), link.isExternal(), selected, current, navItem);
            }
        } else {
            displayItem = null;
        }

        return displayItem;
    }


    /**
     * Get navigation display item children.
     *
     * @param nuxeoController Nuxeo controller
     * @param cmsContext CMS context
     * @param level current level
     * @param maxLevel maximum level
     * @param startLevel start level
     * @param spacePath space path
     * @param basePath base path
     * @param nuxeoPath Nuxeo path
     * @param partialOpenLevels partial open levels
     * @param navItem navigation item
     * @param doc Nuxeo document
     * @param selected selected indicator
     * @return navigation display item children
     * @throws CMSException
     */
    private List<NavigationDisplayItem> getDisplayItemChildren(NuxeoController nuxeoController, CMSServiceCtx cmsContext, int level, int maxLevel,
            int startLevel, String spacePath, String basePath, String nuxeoPath, int partialOpenLevels, CMSItem navItem, Document doc,
            boolean selected) throws CMSException {
        // CMS service
        ICMSService cmsService = NuxeoController.getCMSService();

        // Display children
        List<NavigationDisplayItem> displayChildren;

        boolean partial = (partialOpenLevels != -1);
        boolean fullOpened = (!partial && (level < maxLevel));
        boolean partialOpened = (partial && (selected || ((level + 1) <= partialOpenLevels)));
        if (fullOpened || partialOpened) {
            List<CMSItem> navItems = cmsService.getPortalNavigationSubitems(cmsContext, basePath, nuxeoPath);
            displayChildren = new ArrayList<NavigationDisplayItem>(navItems.size());

            for (CMSItem child : navItems) {
                if ("1".equals(child.getProperties().get("menuItem"))) {
                    NavigationDisplayItem newItem = this.getDisplayItem(nuxeoController, cmsContext, level + 1, maxLevel, startLevel, spacePath, basePath,
                            child.getPath(), "1".equals(navItem.getProperties().get("navigationElement")), partialOpenLevels);
                    if (newItem != null) {
                        displayChildren.add(newItem);
                    }
                }
            }
        } else {
            displayChildren = new ArrayList<NavigationDisplayItem>(0);
        }

        // v2.0.9 Ajout tri pour affichage cohérent avec FileBrowser
        CMSItemType cmsItemType = nuxeoController.getCMSItemTypes().get(doc.getType());
        if ((cmsItemType == null) || !cmsItemType.isOrdered()) {
            Collections.sort(displayChildren, new MenuComparator(nuxeoController));
        }

        return displayChildren;
    }

}
