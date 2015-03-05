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
package fr.toutatice.portail.cms.nuxeo.portlets.publish;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.portlet.RenderMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.WindowState;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jboss.portal.core.controller.ControllerContext;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.internationalization.Bundle;
import org.osivia.portal.api.internationalization.IBundleFactory;
import org.osivia.portal.api.internationalization.IInternationalizationService;
import org.osivia.portal.api.notifications.INotificationsService;
import org.osivia.portal.api.notifications.NotificationsType;
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
import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.PortletErrorHandler;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCustomizer;
import fr.toutatice.portail.cms.nuxeo.portlets.files.MoveDocumentCommand;

/**
 * Publication menu portlet.
 *
 * @see CMSPortlet
 */
public class MenuPortlet extends CMSPortlet {

    /** Max levels window property name. */
    private static final String MAX_LEVELS_WINDOW_PROPERTY = "osivia.cms.maxLevels";
    /** Start level window property name. */
    private static final String START_LEVEL_WINDOW_PROPERTY = "osivia.cms.startLevel";
    /** Open levels window property name. */
    private static final String OPEN_LEVELS_WINDOW_PROPERTY = "osivia.cms.openLevels";
    /** Template window property name. */
    private static final String TEMPLATE_WINDOW_PROPERTY = "osivia.cms.template";
    /** Force navigation window property name. */
    private static final String FORCE_NAVIGATION_WINDOW_PROPERTY = "osivia.cms.forceNavigation";

    /** Default max levels. */
    private static final int DEFAULT_MAX_LEVELS = 3;
    /** Default start level. */
    private static final int DEFAULT_START_LEVEL = 1;
    /** Default open levels. */
    private static final int DEFAULT_OPEN_LEVELS = 1;


    /** View JSP path prefix. */
    private static final String PATH_VIEW_PREFIX = "/WEB-INF/jsp/publish/view";
    /** Admin JSP path. */
    private static final String PATH_ADMIN = "/WEB-INF/jsp/publish/admin.jsp";


    /** Bundle factory. */
    private IBundleFactory bundleFactory;
    /** Notifications service. */
    private INotificationsService notificationsService;


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

        // Portlet context
        PortletContext portletContext = this.getPortletContext();

        // Bundle factory
        IInternationalizationService internationalizationService = (IInternationalizationService) portletContext
                .getAttribute(Constants.INTERNATIONALIZATION_SERVICE_NAME);
        this.bundleFactory = internationalizationService.getBundleFactory(this.getClass().getClassLoader());

        // Notification service
        this.notificationsService = (INotificationsService) portletContext.getAttribute(Constants.NOTIFICATIONS_SERVICE_NAME);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void processAction(ActionRequest request, ActionResponse response) throws IOException, PortletException {
        // Action name
        String action = request.getParameter(ActionRequest.ACTION_NAME);

        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(request, response, this.getPortletContext());
        // Portal controller context
        PortalControllerContext portalControllerContext = nuxeoController.getPortalCtx();
        // Bundle
        Bundle bundle = this.bundleFactory.getBundle(request.getLocale());


        if (PortletMode.VIEW.equals(request.getPortletMode())) {
            // View

            if ("drop".equals(action)) {
                // Drop action

                // Source
                String sourceId = request.getParameter("sourceId");
                // Target
                String targetId = request.getParameter("targetId");
                String targetPath = this.getAuxiliaryPath(nuxeoController, targetId);

                // Move document command
                INuxeoCommand command = new MoveDocumentCommand(sourceId, targetId);
                try {
                    nuxeoController.executeNuxeoCommand(command);

                    // Refresh navigation
                    request.setAttribute(Constants.PORTLET_ATTR_UPDATE_CONTENTS, Constants.PORTLET_VALUE_ACTIVATE);

                    // Update public render parameter for associated portlets refresh
                    response.setRenderParameter("dnd-update", String.valueOf(System.currentTimeMillis()));

                    // Notification
                    String message = bundle.getString("MESSAGE_MOVE_SUCCESS");
                    this.notificationsService.addSimpleNotification(portalControllerContext, message, NotificationsType.SUCCESS);
                } catch (NuxeoException e) {
                    // Notification
                    String message = bundle.getString("MESSAGE_MOVE_ERROR");
                    this.notificationsService.addSimpleNotification(portalControllerContext, message, NotificationsType.ERROR);
                }


                if (targetPath != null) {
                    response.setRenderParameter("auxiliaryPath", targetPath);
                }
            }

        } else if ("admin".equals(request.getPortletMode().toString())) {
            // Admin

            if ("save".equals(action)) {
                // Save action

                // Current window
                PortalWindow window = WindowFactory.getWindow(request);


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

                // Template
                String template = request.getParameter("template");
                window.setProperty(TEMPLATE_WINDOW_PROPERTY, template);

                // Force navigation
                String forceNavigation = request.getParameter("forceNavigation");
                window.setProperty(FORCE_NAVIGATION_WINDOW_PROPERTY, forceNavigation);
            }

            response.setPortletMode(PortletMode.VIEW);
            response.setWindowState(WindowState.NORMAL);
        }
    }


    /**
     * Get auxiliary path form target identifier.
     *
     * @param nuxeoController Nuxeo controller
     * @param id target identifier
     * @return auxiliary path
     */
    private String getAuxiliaryPath(NuxeoController nuxeoController, String id) {
        // CMS service
        ICMSService cmsService = NuxeoController.getCMSService();
        // CMS context
        CMSServiceCtx cmsContext = this.getMenuCMSContext(nuxeoController);

        // Path
        String path;
        try {
            // Fetch content
            CMSItem item = cmsService.getContent(cmsContext, id);

            path = item.getPath();
        } catch (CMSException e) {
            path = null;
        }
        return path;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(request, response, this.getPortletContext());
        // CMS service
        ICMSService cmsService = NuxeoController.getCMSService();

        if ("lazyLoading".equals(request.getParameter("action"))) {
            // Lazy loading

            // Navigation display item children
            List<NavigationDisplayItem> children = null;

            // Path
            String path = request.getParameter("path");
            if (StringUtils.isNotEmpty(path)) {
                // CMS context
                CMSServiceCtx cmsContext = this.getMenuCMSContext(nuxeoController);
                // Menu options
                MenuOptions options = this.getMenuOptions(nuxeoController);


                try {
                    // Navigation items
                    List<CMSItem> items = cmsService.getPortalNavigationSubitems(cmsContext, options.getBasePath(), path);
                    children = new ArrayList<NavigationDisplayItem>(items.size());

                    for (CMSItem item : items) {
                        if ("1".equals(item.getProperties().get("menuItem"))) {
                            // Nuxeo document
                            Document document = (Document) item.getNativeItem();
                            // Nuxeo document link
                            Link link = nuxeoController.getLink(document, "menu");

                            NavigationDisplayItem navigationDisplayItemChild = new NavigationDisplayItem(document, link, false, false, item);
                            children.add(navigationDisplayItemChild);
                        }
                    }

                    Comparator<NavigationDisplayItem> comparator = new MenuComparator(nuxeoController);
                    Collections.sort(children, comparator);
                } catch (CMSException e) {
                    // Do nothing
                }
            }

            // Content type
            response.setContentType("application/json");

            // Content
            PrintWriter printWriter = new PrintWriter(response.getPortletOutputStream());
            printWriter.write("[ ");
            if (CollectionUtils.isNotEmpty(children)) {
                boolean firstItem = true;
                for (NavigationDisplayItem child : children) {
                    if (firstItem) {
                        firstItem = false;
                    } else {
                        printWriter.write(", ");
                    }

                    this.writeNavigationDisplayItem(printWriter, child);
                }
            }
            printWriter.write(" ]");
            printWriter.close();
        } else {
            super.serveResource(request, response);
        }
    }


    /**
     * Write navigation display item in portlet output stream.
     *
     * @param printWriter print writer on portlet output stream
     * @param item navigation display item
     */
    private void writeNavigationDisplayItem(PrintWriter printWriter, NavigationDisplayItem item) {
        boolean browsable = false;
        String glyph = null;
        if ((item.getNavItem() != null) && (item.getNavItem().getType() != null)) {
            CMSItemType cmsItemType = item.getNavItem().getType();
            browsable = cmsItemType.isBrowsable();
            glyph = cmsItemType.getGlyph();
        }


        printWriter.write("{ ");

        // Title
        printWriter.write("\"title\" : \"");
        printWriter.write(item.getTitle());
        printWriter.write("\", ");

        // Link
        printWriter.write("\"href\" : \"");
        printWriter.write(item.getUrl());
        printWriter.write("\", ");

        // Browsable
        printWriter.write("\"folder\" : ");
        printWriter.write(String.valueOf(browsable));
        printWriter.write(", \"lazy\" : ");
        printWriter.write(String.valueOf(browsable));
        printWriter.write(", ");

        // Id
        printWriter.write("\"id\" : \"");
        printWriter.write(item.getId());
        printWriter.write("\", ");

        // Path
        printWriter.write("\"path\" : \"");
        printWriter.write(item.getNavItem().getPath());
        printWriter.write("\", ");

        // Glyph
        if ((glyph != null) && (!glyph.contains("folder"))) {
            printWriter.write("\"iconclass\" : \"");
            printWriter.write(glyph);
            printWriter.write("\", ");
        }

        // Extra classes
        printWriter.write("\"extraClasses\" : \"text-muted\"");

        printWriter.write(" }");
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
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(request, response, this.getPortletContext());
        // CMS customizer
        INuxeoCustomizer customizer = nuxeoController.getNuxeoCMSService().getCMSCustomizer();
        // Current window
        PortalWindow window = WindowFactory.getWindow(request);


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

        // Templates
        Map<String, String> templates = customizer.getMenuTemplates(request.getLocale());
        request.setAttribute("templates", templates);
        String selectedTemplate = window.getProperty(TEMPLATE_WINDOW_PROPERTY);
        request.setAttribute("selectedTemplate", selectedTemplate);

        // Force navigation
        boolean forceNavigation = BooleanUtils.toBoolean(window.getProperty(FORCE_NAVIGATION_WINDOW_PROPERTY));
        request.setAttribute("forceNavigation", forceNavigation);


        response.setContentType("text/html");
        this.getPortletContext().getRequestDispatcher(PATH_ADMIN).include(request, response);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        try {
            // Nuxeo controller
            NuxeoController nuxeoController = new NuxeoController(request, response, this.getPortletContext());
            // CMS context
            CMSServiceCtx cmsContext = this.getMenuCMSContext(nuxeoController);
            // Menu options
            MenuOptions options = this.getMenuOptions(nuxeoController);
            // Current window
            PortalWindow window = WindowFactory.getWindow(request);

            // Template
            String template = window.getProperty(TEMPLATE_WINDOW_PROPERTY);

            // Start level
            request.setAttribute("startLevel", options.getStartLevel());
            // Open levels
            request.setAttribute("openLevels", options.getOpenLevels());

            if (options.getBasePath() != null) {
                // Navigation display item
                NavigationDisplayItem displayItem = this.getNavigationDisplayItem(nuxeoController, cmsContext, options);
                if (displayItem != null) {
                    if (displayItem.getTitle() != null) {
                        response.setTitle(displayItem.getTitle());
                    }
                    request.setAttribute("displayItem", displayItem);
                }
            }

            response.setContentType("text/html");

            // Dispatcher
            String requestDispatcherPath;
            if (StringUtils.isEmpty(template)) {
                requestDispatcherPath = PATH_VIEW_PREFIX + ".jsp";
            } else {
                requestDispatcherPath = PATH_VIEW_PREFIX + "-" + template + ".jsp";
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
     * @param options menu options
     * @return navigation display item
     * @throws CMSException
     */
    private NavigationDisplayItem getNavigationDisplayItem(NuxeoController nuxeoController, CMSServiceCtx cmsContext, MenuOptions options) throws CMSException {
        // CMS service
        ICMSService cmsService = NuxeoController.getCMSService();

        // Navigation item
        CMSItem navigationItem = cmsService.getPortalNavigationItem(cmsContext, options.getBasePath(), options.getBasePath());

        // Navigation display item
        NavigationDisplayItem navigationDisplayItem;
        if (navigationItem == null) {
            navigationDisplayItem = null;
        } else {
            // Lazy loading indicator
            boolean lazy = "1".equals(navigationItem.getProperties().get("partialLoading"));
            options.setLazy(lazy);

            navigationDisplayItem = this.getNavigationDisplayItem(nuxeoController, cmsContext, options, navigationItem, 0);
        }
        return navigationDisplayItem;
    }


    /**
     * Get navigation display item.
     *
     * @param nuxeoController Nuxeo controller
     * @param cmsContext CMS context
     * @param options menu options
     * @param navigationItem recursive navigation item
     * @param level recursive level
     * @return navigation display item
     * @throws CMSException
     */
    private NavigationDisplayItem getNavigationDisplayItem(NuxeoController nuxeoController, CMSServiceCtx cmsContext, MenuOptions options,
            CMSItem navigationItem, int level) throws CMSException {
        // Nuxeo document
        Document document = (Document) navigationItem.getNativeItem();
        // Nuxeo document link
        Link link = nuxeoController.getLink(document, "menu");

        // Selected item indicator
        boolean selected = false;
        // Current item indicator
        boolean current = false;

        if (StringUtils.startsWith(options.getCurrentPath(), navigationItem.getPath())) {
            selected = true;

            if (StringUtils.equals(options.getCurrentPath(), navigationItem.getPath())) {
                current = true;
            }
        } else if (StringUtils.equals(options.getCurrentPath(), document.getPath())) {
            selected = true;
            current = true;
        }

        if (StringUtils.startsWith(options.getAuxiliaryPath(), navigationItem.getPath())
                && !StringUtils.equals(options.getAuxiliaryPath(), navigationItem.getPath())) {
            selected = true;
        }


        // Navigation display item
        NavigationDisplayItem navigationDisplayItem;

        if ((level + 1) >= options.getStartLevel()) {
            navigationDisplayItem = new NavigationDisplayItem(document, link, selected, current, navigationItem);

            // Add children
            List<NavigationDisplayItem> navigationDisplayChildren = this.getNavigationDisplayItemChildren(nuxeoController, cmsContext, options, navigationItem,
                    level, selected);
            navigationDisplayItem.getChildren().addAll(navigationDisplayChildren);
        } else if (selected) {
            navigationDisplayItem = null;

            // Search selected child
            List<NavigationDisplayItem> navigationDisplayChildren = this.getNavigationDisplayItemChildren(nuxeoController, cmsContext, options, navigationItem,
                    level, selected);

            for (NavigationDisplayItem displayItemChild : navigationDisplayChildren) {
                if (displayItemChild.isSelected()) {
                    navigationDisplayItem = displayItemChild;
                    break;
                }
            }

            if ((navigationDisplayItem == null) && (level == 0)) {
                navigationDisplayItem = new NavigationDisplayItem(document, link, selected, current, navigationItem);
            }
        } else {
            navigationDisplayItem = null;
        }

        return navigationDisplayItem;
    }


    /**
     * Get navigation display item children.
     *
     * @param nuxeoController Nuxeo controller
     * @param cmsContext CMS context
     * @param options menu options
     * @param navigationItem recursive navigation item
     * @param level recursive level
     * @param selected selected item indicator
     * @return navigation display item children
     * @throws CMSException
     */
    private List<NavigationDisplayItem> getNavigationDisplayItemChildren(NuxeoController nuxeoController, CMSServiceCtx cmsContext, MenuOptions options,
            CMSItem navigationItem, int level, boolean selected) throws CMSException {
        // CMS service
        ICMSService cmsService = NuxeoController.getCMSService();
        // Nuxeo document
        Document document = (Document) navigationItem.getNativeItem();

        // Navigation display item children
        List<NavigationDisplayItem> navigationDisplayItemChildren;

        if ((!options.isLazy() && (level < options.getMaxLevels())) || (options.isLazy() && selected)) {
            List<CMSItem> navigationItemChildren = cmsService.getPortalNavigationSubitems(cmsContext, options.getBasePath(), navigationItem.getPath());
            navigationDisplayItemChildren = new ArrayList<NavigationDisplayItem>(navigationItemChildren.size());

            for (CMSItem navigationItemChild : navigationItemChildren) {
                if ("1".equals(navigationItemChild.getProperties().get("menuItem"))) {
                    NavigationDisplayItem navigationDisplayItemChild = this.getNavigationDisplayItem(nuxeoController, cmsContext, options, navigationItemChild,
                            level + 1);

                    if (navigationDisplayItemChild != null) {
                        navigationDisplayItemChildren.add(navigationDisplayItemChild);
                    }
                }
            }
        } else {
            navigationDisplayItemChildren = new ArrayList<NavigationDisplayItem>(0);
        }

        // v2.0.9 : sort for consistent view with file browser
        CMSItemType cmsItemType = nuxeoController.getCMSItemTypes().get(document.getType());
        if ((cmsItemType == null) || !cmsItemType.isOrdered()) {
            Comparator<NavigationDisplayItem> comparator = new MenuComparator(nuxeoController);
            Collections.sort(navigationDisplayItemChildren, comparator);
        }

        return navigationDisplayItemChildren;
    }


    /**
     * Get menu CMS context.
     *
     * @param nuxeoController Nuxeo controller
     * @return CMS context
     */
    private CMSServiceCtx getMenuCMSContext(NuxeoController nuxeoController) {
        // Portal controller context
        PortalControllerContext portalControllerContext = nuxeoController.getPortalCtx();
        // Controller context
        ControllerContext controllerContext = ControllerContextAdapter.getControllerContext(portalControllerContext);

        // CMS version
        String cmsVersion = CmsPermissionHelper.getCurrentCmsVersion(controllerContext);

        // CMS context
        CMSServiceCtx cmsContext = new CMSServiceCtx();
        cmsContext.setPortalControllerContext(nuxeoController.getPortalCtx());
        cmsContext.setScope(nuxeoController.getNavigationScope());
        if (CmsPermissionHelper.CMS_VERSION_PREVIEW.equals(cmsVersion)) {
            cmsContext.setDisplayLiveVersion("1");
        }
        return cmsContext;
    }


    /**
     * Get menu options.
     *
     * @param nuxeoController Nuxeo controller
     * @return menu options
     */
    private MenuOptions getMenuOptions(NuxeoController nuxeoController) {
        // Request
        PortletRequest request = nuxeoController.getRequest();
        // Current window
        PortalWindow window = WindowFactory.getWindow(request);

        // Base path
        String basePath;
        boolean forceNavigation = BooleanUtils.toBoolean(window.getProperty(FORCE_NAVIGATION_WINDOW_PROPERTY));
        if (forceNavigation || (nuxeoController.getMenuRootPath() == null)) {
            basePath = nuxeoController.getBasePath();
        } else {
            basePath = nuxeoController.getMenuRootPath();
        }

        // Current path
        String currentPath = nuxeoController.getItemNavigationPath();

        // Auxiliary path
        String auxiliaryPath = request.getParameter("auxiliaryPath");

        // Open levels
        int openLevels = DEFAULT_OPEN_LEVELS;
        String openLevelsWindowProperty = window.getProperty(OPEN_LEVELS_WINDOW_PROPERTY);
        if (StringUtils.isNotBlank(openLevelsWindowProperty)) {
            openLevels = NumberUtils.toInt(openLevelsWindowProperty);
        }

        // Start level
        int startLevel = DEFAULT_START_LEVEL;
        String startLevelWindowProperty = window.getProperty(START_LEVEL_WINDOW_PROPERTY);
        if (StringUtils.isNotBlank(startLevelWindowProperty)) {
            startLevel = NumberUtils.toInt(startLevelWindowProperty);
        }

        // Max levels
        int maxLevels = DEFAULT_MAX_LEVELS;
        String maxLevelWindowProperty = window.getProperty(MAX_LEVELS_WINDOW_PROPERTY);
        if (StringUtils.isNotBlank(maxLevelWindowProperty)) {
            maxLevels = NumberUtils.toInt(maxLevelWindowProperty);
        }

        return new MenuOptions(basePath, currentPath, auxiliaryPath, openLevels, startLevel, maxLevels);
    }

}
