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
package fr.toutatice.portail.cms.nuxeo.api;

import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.ResourceURL;

import org.apache.commons.lang.StringUtils;
import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.Portal;
import org.jboss.portal.core.model.portal.PortalObjectPath;
import org.jboss.portal.core.model.portal.Window;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.cache.services.CacheInfo;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.contribution.IContributionService.EditionState;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.api.urls.Link;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;
import org.osivia.portal.core.cms.CMSBinaryContent;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSItemType;
import org.osivia.portal.core.cms.CMSObjectPath;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.ICMSService;
import org.osivia.portal.core.cms.ICMSServiceLocator;
import org.osivia.portal.core.constants.InternalConstants;
import org.osivia.portal.core.context.ControllerContextAdapter;
import org.osivia.portal.core.formatters.IFormatter;
import org.osivia.portal.core.page.PageProperties;
import org.osivia.portal.core.profils.IProfilManager;
import org.osivia.portal.core.profils.ProfilBean;
import org.osivia.portal.core.security.CmsPermissionHelper;
import org.osivia.portal.core.security.CmsPermissionHelper.Level;
import org.osivia.portal.core.web.IWebIdService;

import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCommandService;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCommentsService;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoService;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoServiceCommand;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandContext;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandServiceFactory;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoConnectionProperties;


/**
 * Nuxeo controller.
 * 
 * Main entry point for nuxeo calls / data manipulation from within a cms portlet
 * 
 */
public class NuxeoController {

    /** The request. */
    PortletRequest request;

    /** The response. */
    PortletResponse response;

    /** The portlet ctx. */
    PortletContext portletCtx;

    /** The url factory. */
    IPortalUrlFactory urlFactory;

    /** The nuxeo cms service. */
    INuxeoService nuxeoCMSService;

    /** The page id. */
    String pageId;

    /** The nuxeo base uri. */
    URI nuxeoBaseURI;

    /** The nuxeo connection. */
    NuxeoConnectionProperties nuxeoConnection;

    /** The profil manager. */
    IProfilManager profilManager;

    /** The formatter. */
    IFormatter formatter;

    /** The scope. */
    String scope;

    /** The display live version. */
    String displayLiveVersion;

    /** The base path. */
    String basePath;

    /** The navigation path. */
    String navigationPath;

    /** The item navigation path. */
    String itemNavigationPath;

    /** The doc type to create. */
    String docTypeToCreate;

    /** The parent path to create. */
    String parentPathToCreate;

    /**
     * Gets the parent path to create.
     * 
     * @return the parent path to create
     */
    public String getParentPathToCreate() {
        return this.parentPathToCreate;
    }

    /**
     * Sets the parent path to create.
     * 
     * @param parentPathToCreate the new parent path to create
     */
    public void setParentPathToCreate(String parentPathToCreate) {
        this.parentPathToCreate = parentPathToCreate;
    }

    /** The content path. */
    String contentPath;

    /** The space path. */
    String spacePath;

    /** The force publication infos scope. */
    String forcePublicationInfosScope;

    /** The menu root path. */
    String menuRootPath;


    /**
     * Gets the menu root path.
     * 
     * @return the menu root path
     */
    public String getMenuRootPath() {
        return this.menuRootPath;
    }

    /** The hide meta datas. */
    String hideMetaDatas;

    /** The display context. */
    String displayContext;

    /** The navigation scope. */
    String navigationScope = null;

    /** The nav item. */
    CMSItem navItem;

    /** The cms ctx. */
    CMSServiceCtx cmsCtx;

    boolean reloadResource = false;


    boolean streamingSupport = false;


    public boolean isStreamingSupport() {
        return streamingSupport;
    }

    public void setStreamingSupport(boolean streamingSupport) {
        this.streamingSupport = streamingSupport;
    }


    /** The current doc. */
    Document currentDoc;

    /** The portal ctx. */
    PortalControllerContext portalCtx;

    IWebIdService webIdService;

    /**
     * WebId service used to transform urls
     * 
     * @return the service
     */
    public IWebIdService getWebIdService() {
        if (this.webIdService == null) {
            this.webIdService = (IWebIdService) getPortalCtx().getPortletCtx().getAttribute("webIdService");
        }

        return this.webIdService;
    }

    /** The cms service locator. */
    private static ICMSServiceLocator cmsServiceLocator;

    /**
     * Gets the current doc.
     * 
     * @return the current doc
     */
    public Document getCurrentDoc() {
        return this.currentDoc;
    }

    /**
     * Sets the current doc.
     * 
     * @param currentDoc the new current doc
     */
    public void setCurrentDoc(Document currentDoc) {
        this.currentDoc = currentDoc;
    }


    /**
     * Gets the space path.
     * 
     * @return the space path
     */
    public String getSpacePath() {
        return this.spacePath;
    }

    /**
     * Sets the space path.
     * 
     * @param spacePath the new space path
     */
    public void setSpacePath(String spacePath) {
        this.spacePath = spacePath;
    }

    /**
     * Gets the base path.
     * 
     * @return the base path
     */
    public String getBasePath() {

        return this.basePath;
    }

    /**
     * Gets the navigation path.
     * 
     * @return the navigation path
     */
    public String getNavigationPath() {
        return this.navigationPath;
    }

    /**
     * path reel de contenu.
     * 
     * @return the content path
     */
    public String getContentPath() {
        return this.contentPath;
    }


    /**
     * path de navigation du contenu.
     * 
     * @return the item navigation path
     */
    public String getItemNavigationPath() {
        return this.itemNavigationPath;
    }


    /**
     * Gets the display context.
     * 
     * @return the display context
     */
    public String getDisplayContext() {
        return this.displayContext;
    }

    /**
     * Sets the display context.
     * 
     * @param displayContext the new display context
     */
    public void setDisplayContext(String displayContext) {
        this.displayContext = displayContext;
    }


    /**
     * Gets the hide meta datas.
     * 
     * @return the hide meta datas
     */
    public String getHideMetaDatas() {
        return this.hideMetaDatas;
    }

    /**
     * Sets the hide meta datas.
     * 
     * @param hideMetaDatas the new hide meta datas
     */
    public void setHideMetaDatas(String hideMetaDatas) {
        this.hideMetaDatas = hideMetaDatas;
    }

    /** The page marker. */
    String pageMarker;

    /**
     * Sets the page marker.
     * 
     * @param pageMarker the new page marker
     */
    public void setPageMarker(String pageMarker) {
        this.pageMarker = pageMarker;
    }

    /**
     * Gets the display live version.
     * 
     * @return the display live version
     */
    public String getDisplayLiveVersion() {
        return this.displayLiveVersion;
    }

    /**
     * Sets the display live version.
     * 
     * @param displayLiveVersion the new display live version
     */
    public void setDisplayLiveVersion(String displayLiveVersion) {
        this.displayLiveVersion = displayLiveVersion;
    }

    /**
     * Checks if is displaying live version.
     * 
     * @return true, if is displaying live version
     */
    public boolean isDisplayingLiveVersion() {
        boolean fDisplayLiveVersion = false;
        if ("1".equals(this.displayLiveVersion)) {
            // Il faut récupérer les proxys
            fDisplayLiveVersion = true;
        }
        return fDisplayLiveVersion;
    }

    /**
     * Gets the force publication infos scope.
     * 
     * @return the force publication infos scope
     */
    public String getForcePublicationInfosScope() {
        return this.forcePublicationInfosScope;
    }

    /**
     * Sets the force publication infos scope.
     * 
     * @param forcePublicationInfosScope the new force publication infos scope
     */
    public void setForcePublicationInfosScope(String forcePublicationInfosScope) {
        this.forcePublicationInfosScope = forcePublicationInfosScope;
    }


    /**
     * Gets the scope.
     * 
     * @return the scope
     */
    public String getScope() {
        return this.scope;
    }

    /** The auth type. */
    int authType = NuxeoCommandContext.AUTH_TYPE_USER;

    /** The scope profil. */
    private ProfilBean scopeProfil = null;


    /** The cache type. */
    int cacheType = CacheInfo.CACHE_SCOPE_NONE;


    /**
     * Gets the cache type.
     * 
     * @return the cache type
     */
    public int getCacheType() {
        return this.cacheType;
    }

    /**
     * Sets the cache type.
     * 
     * @param cacheType the new cache type
     */
    public void setCacheType(int cacheType) {
        this.cacheType = cacheType;
    }

    /**
     * Gets the scope profil.
     * 
     * @return the scope profil
     */
    private ProfilBean getScopeProfil() {
        return this.scopeProfil;
    }

    /**
     * Sets the scope profil.
     * 
     * @param scopeProfil the new scope profil
     */
    private void setScopeProfil(ProfilBean scopeProfil) {
        this.scopeProfil = scopeProfil;
    }

    /** The nuxeo command service. */
    INuxeoCommandService nuxeoCommandService;

    /** The cache time out. */
    private long cacheTimeOut = -1;

    /** The asynchronous updates. */
    public boolean asynchronousUpdates = false;

    /**
     * Checks if is asynchronous updates.
     * 
     * @return true, if is asynchronous updates
     */
    public boolean isAsynchronousUpdates() {
        return this.asynchronousUpdates;
    }

    /**
     * Sets the asynchronous updates.
     * 
     * @param asynchronousUpdates the new asynchronous updates
     */
    public void setAsynchronousUpdates(boolean asynchronousUpdates) {
        this.asynchronousUpdates = asynchronousUpdates;
    }

    /**
     * Gets the cache time out.
     * 
     * @return the cache time out
     */
    public long getCacheTimeOut() {
        return this.cacheTimeOut;
    }

    /**
     * Sets the cache time out.
     * 
     * @param cacheTimeOut the new cache time out
     */
    public void setCacheTimeOut(long cacheTimeOut) {
        this.cacheTimeOut = cacheTimeOut;
    }


    /**
     * Set the current scope for furthers nuxeo requests
     * 
     * 
     * @param scope the new scope
     * @throws Exception the exception
     */

    public void setScope(String scope) {

        // Par défaut
        this.setAuthType(NuxeoCommandContext.AUTH_TYPE_USER);
        this.setCacheType(CacheInfo.CACHE_SCOPE_NONE);

        if ("anonymous".equals(scope)) {
            this.setAuthType(NuxeoCommandContext.AUTH_TYPE_ANONYMOUS);
            this.setCacheType(CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);
            /*
             * } else if ("__nocache".equals(scope)) {
             * setAuthType( NuxeoCommandContext.AUTH_TYPE_ANONYMOUS);
             * setCacheType( CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);
             */
        } else if ((scope != null) && !"__nocache".equals(scope)) {
            this.setAuthType(NuxeoCommandContext.AUTH_TYPE_PROFIL);
            this.setScopeProfil(this.getProfilManager().getProfil(scope));
            this.setCacheType(CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);
        }

        this.scope = scope;
    }

    /**
     * Gets the auth type.
     * 
     * @return the auth type
     */
    public int getAuthType() {
        return this.authType;
    }

    /**
     * Sets the auth type.
     * 
     * @param authType the new auth type
     */
    public void setAuthType(int authType) {
        this.authType = authType;
    }

    /**
     * Gets the nuxeo connection props.
     * 
     * @return the nuxeo connection props
     */
    public NuxeoConnectionProperties getNuxeoConnectionProps() {
        if (this.nuxeoConnection == null) {
            this.nuxeoConnection = new NuxeoConnectionProperties();
        }
        return this.nuxeoConnection;
    }

    /**
     * Gets the portal ctx.
     * 
     * @return the portal ctx
     */
    public PortalControllerContext getPortalCtx() {

        if (this.portalCtx == null) {
            this.portalCtx = new PortalControllerContext(this.getPortletCtx(), this.request, this.response);
        }

        return this.portalCtx;
    }

    /**
     * Gets the request.
     * 
     * @return the request
     */
    public PortletRequest getRequest() {
        return this.request;
    }

    /**
     * Gets the response.
     * 
     * @return the response
     */
    public PortletResponse getResponse() {
        return this.response;
    }

    /**
     * Gets the portlet ctx.
     * 
     * @return the portlet ctx
     */
    public PortletContext getPortletCtx() {
        return this.portletCtx;
    }


    /**
     * Instantiates a new nuxeo controller.
     * 
     * @param request the request
     * @param response the response
     * @param portletCtx the portlet ctx
     * @throws RuntimeException the runtime exception
     */
    public NuxeoController(PortletRequest request, PortletResponse response, PortletContext portletCtx) throws RuntimeException {
        super();
        this.request = request;
        this.response = response;
        this.portletCtx = portletCtx;

        try {
            PortalWindow window = WindowFactory.getWindow(request);

            this.portalCtx = new PortalControllerContext(portletCtx, request, response);

            // v2 : Ajout héritage
            String scope = window.getProperty(Constants.WINDOW_PROP_SCOPE);
            if ("__inherited".equals(scope)) {
                // scope de contextualisation

                // TODO : ajouter sur le path
                scope = request.getParameter("osivia.cms.pageScope");
                if (scope == null) {
                    scope = window.getPageProperty(Constants.WINDOW_PROP_SCOPE);
                }
            }

            // Pour les fragments, le cache doit également concerner les PublicationInfos
            // D'où l'utilisattoin du forcePublicationScope
            String forcePublicationScope = window.getProperty("osivia.cms.forcePublicationScope");
            if (forcePublicationScope != null) {
                // Fragments

                if ("__inherited".equals(forcePublicationScope)) {
                    forcePublicationScope = request.getParameter("osivia.cms.pageScope");
                    if (forcePublicationScope == null) {
                        forcePublicationScope = window.getPageProperty(Constants.WINDOW_PROP_SCOPE);
                    }
                }

                if (forcePublicationScope != null) {

                    scope = forcePublicationScope;
                    this.setForcePublicationInfosScope(forcePublicationScope);
                }
            }

            this.navigationScope = window.getPageProperty("osivia.cms.navigationScope");


            this.spacePath = window.getPageProperty("osivia.cms.basePath");
            CMSItem publishSpaceConfig = null;
            if (this.spacePath != null) {
                publishSpaceConfig = getCMSService().getSpaceConfig(this.getCMSCtx(), this.spacePath);
            }


            String displayLiveVersion = window.getProperty(Constants.WINDOW_PROP_VERSION);


            if ("__inherited".equals(displayLiveVersion)) {

                if (publishSpaceConfig != null) {
                    displayLiveVersion = publishSpaceConfig.getProperties().get("displayLiveVersion");
                } else {
                    displayLiveVersion = window.getPageProperty(Constants.WINDOW_PROP_VERSION);
                }
            }


            String displayLiveVersionParam = request.getParameter("displayLiveVersion");
            if (displayLiveVersionParam != null) {
                displayLiveVersion = displayLiveVersionParam;
            }


            String hideMetadatas = window.getProperty("osivia.cms.hideMetaDatas");

            this.setScope(scope);
            this.setDisplayLiveVersion(displayLiveVersion);
            this.setHideMetaDatas(hideMetadatas);

            this.setDocTypeToCreate(window.getProperty("osivia.createDocType"));
            String parentPathToCreate = window.getProperty("osivia.createParentPath");
            this.setParentPathToCreate(parentPathToCreate);


            this.setPageMarker((String) request.getAttribute("osivia.pageMarker"));


            /* computes root path */

            Window jbpWindow = (Window) request.getAttribute("osivia.window");
            Page page = (Page) jbpWindow.getParent();
            Portal portal = page.getPortal();
            if (InternalConstants.PORTAL_TYPE_SPACE.equals(portal.getDeclaredProperty("osivia.portal.portalType"))) {
                this.menuRootPath = portal.getDefaultPage().getDeclaredProperty("osivia.cms.basePath");
            }


            this.basePath = window.getPageProperty("osivia.cms.basePath");

            this.navigationPath = request.getParameter("osivia.cms.path");

            if ((this.spacePath != null) && (request.getParameter("osivia.cms.itemRelPath") != null)) {
                this.itemNavigationPath = this.spacePath + request.getParameter("osivia.cms.itemRelPath");
            }

            this.contentPath = request.getParameter("osivia.cms.contentPath");


            if (request instanceof ResourceRequest) {
                if (request.getParameter("refresh") != null) {
                    reloadResource = true;
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public NuxeoException wrapNuxeoException(Exception e) {

        if (e instanceof CMSException) {
            CMSException cmsExc = (CMSException) e;

            if (cmsExc.getErrorCode() == CMSException.ERROR_NOTFOUND) {
                return new NuxeoException(NuxeoException.ERROR_NOTFOUND);
            }
            if (cmsExc.getErrorCode() == CMSException.ERROR_FORBIDDEN) {
                return new NuxeoException(NuxeoException.ERROR_FORBIDDEN);
            }
            return new NuxeoException(NuxeoException.ERROR_UNAVAILAIBLE, cmsExc.getCause());
        } else
            return new NuxeoException(e.getCause());

    }


    /**
     * Sets the doc type to create.
     * 
     * @param property the new doc type to create
     */
    public void setDocTypeToCreate(String property) {
        this.docTypeToCreate = property;
    }

    /**
     * Gets the navigation item.
     * 
     * @return the navigation item
     * @throws Exception the exception
     */
    public CMSItem getNavigationItem() {
        try {
            if (this.navItem == null) {
                if (this.getNavigationPath() != null) {
                    // Navigation context
                    CMSServiceCtx cmsReadNavContext = new CMSServiceCtx();
                    cmsReadNavContext.setControllerContext(ControllerContextAdapter.getControllerContext(this.getPortalCtx()));
                    cmsReadNavContext.setScope(this.getNavigationScope());

                    // TODO : factoriser dans NuxeoController

                    this.navItem = getCMSService().getPortalNavigationItem(cmsReadNavContext, this.getSpacePath(), this.getNavigationPath());
                }

            }

            return this.navItem;
        } catch (Exception e) {
            throw wrapNuxeoException(e);
        }
    }


    /**
     * Gets the navigation scope.
     * 
     * @return the navigation scope
     */
    public String getNavigationScope() {
        return this.navigationScope;
    }

    /**
     * Instantiates a new nuxeo controller.
     * 
     * @param portletCtx the portlet ctx
     */
    public NuxeoController(PortletContext portletCtx) {
        super();
        this.portletCtx = portletCtx;
    }


    /**
     * Gets the portal url factory.
     * 
     * @return the portal url factory
     */
    public IPortalUrlFactory getPortalUrlFactory() {
        if (this.urlFactory == null) {
            this.urlFactory = (IPortalUrlFactory) this.portletCtx.getAttribute("UrlService");
        }

        return this.urlFactory;
    }

    /**
     * Gets the nuxeo command service.
     * 
     * @return the nuxeo command service
     * @throws Exception the exception
     */
    public INuxeoCommandService getNuxeoCommandService() {
        try {
            if (this.nuxeoCommandService == null) {
                this.nuxeoCommandService = NuxeoCommandServiceFactory.getNuxeoCommandService(this.portletCtx);
            }
            return this.nuxeoCommandService;
        } catch (Exception e) {
            throw wrapNuxeoException(e);
        }
    }

    /**
     * Gets the profil manager.
     * 
     * @return the profil manager
     * @throws Exception the exception
     */
    public IProfilManager getProfilManager() {
        if (this.profilManager == null) {
            this.profilManager = (IProfilManager) this.portletCtx.getAttribute(Constants.PROFILE_SERVICE_NAME);
        }


        return this.profilManager;
    }

    /**
     * Gets the formatter.
     * 
     * @return the formatter
     * @throws Exception the exception
     */
    public IFormatter getFormatter() {
        if (this.formatter == null) {
            this.formatter = (IFormatter) this.portletCtx.getAttribute("FormatterService");
        }


        return this.formatter;
    }


    /**
     * Get Nuxeo CMS service instance.
     * 
     * @return Nuxeo CMS service instance
     */
    public INuxeoService getNuxeoCMSService() {
        if (this.nuxeoCMSService == null) {
            this.nuxeoCMSService = (INuxeoService) this.getPortletCtx().getAttribute("NuxeoService");
            if (this.nuxeoCMSService == null) {
                this.nuxeoCMSService = Locator.findMBean(INuxeoService.class, "osivia:service=NuxeoService");
            }
        }
        return this.nuxeoCMSService;
    }


    /**
     * Gets the page id.
     * 
     * @return the page id
     */
    public String getPageId() {
        if (this.pageId == null) {
            Window window = (Window) this.request.getAttribute("osivia.window");
            Page page = (Page) window.getParent();
            try {
                this.pageId = URLEncoder.encode(page.getId().toString(PortalObjectPath.SAFEST_FORMAT), "UTF-8");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
        return this.pageId;
    }


    /**
     * Gets the computed path.
     * 
     * @param portletPath the portlet path
     * @return the computed path
     */
    public String getComputedPath(String portletPath) {

        String computedPath = null;

        if (portletPath == null) {
            computedPath = "";
        } else {
            computedPath = portletPath;

            if (computedPath.contains("${basePath}")) {
                String path = this.getBasePath();
                if (path == null) {
                    path = "";
                }

                computedPath = computedPath.replaceAll("\\$\\{basePath\\}", path);
            }


            if (computedPath.contains("${spacePath}")) {
                String path = this.getSpacePath();
                if (path == null) {
                    path = "";
                }

                computedPath = computedPath.replaceAll("\\$\\{spacePath\\}", path);
            }


            if (computedPath.contains("${navigationPath}")) {

                String path = this.getNavigationPath();
                if (path == null) {
                    path = "";
                }

                computedPath = computedPath.replaceAll("\\$\\{navigationPath\\}", path);
            }

            if (computedPath.contains("${contentPath}")) {

                String path = this.getContentPath();
                if (path == null) {
                    path = "";
                }

                computedPath = computedPath.replaceAll("\\$\\{contentPath\\}", path);
            }

            if (computedPath.contains("${sitePath}")) {
                String path = this.getMenuRootPath();
                if (path == null) {
                    path = "";
                }

                computedPath = computedPath.replaceAll("\\$\\{sitePath\\}", path);
            }


        }

        return computedPath;

    }


    /**
     * Transform html content from nuxeo (note:note)
     * 
     * @param htmlContent the html content
     * @return the string
     * @throws Exception the exception
     */
    public String transformHTMLContent(String htmlContent) {
        try {
            INuxeoService nuxeoService = this.getNuxeoCMSService();
            return nuxeoService.getCMSCustomizer().transformHTMLContent(this.getCMSCtx(), htmlContent);
        } catch (Exception e) {
            throw wrapNuxeoException(e);
        }
    }


    /**
     * Format scope list (for user interface)
     * 
     * @param selectedScope the selected scope
     * @return the string
     * @throws Exception the exception
     */
    public String formatScopeList(String selectedScope) {
        try {

            Window window = (Window) this.request.getAttribute("osivia.window");

            return this.getFormatter().formatScopeList(window, "scope", selectedScope);
        } catch (Exception e) {
            throw wrapNuxeoException(e);
        }

    }


    /**
     * Format request filtering policy list.
     * 
     * @param selectedRequestFilteringPolicy the selected request filtering policy
     * @return the string
     * @throws Exception the exception
     */
    public String formatRequestFilteringPolicyList(String selectedRequestFilteringPolicy) {
        try {

            Window window = (Window) this.request.getAttribute("osivia.window");

            return this.getFormatter().formatRequestFilteringPolicyList(window, "requestFilteringPolicy", selectedRequestFilteringPolicy);

        } catch (Exception e) {
            throw wrapNuxeoException(e);
        }

    }

    /**
     * Format display live version list.
     * 
     * @param selectedVersion the selected version
     * @return the string
     * @throws Exception the exception
     */
    public String formatDisplayLiveVersionList(String selectedVersion) {
        try {

            Window window = (Window) this.request.getAttribute("osivia.window");

            return this.getFormatter().formatDisplayLiveVersionList(this.getCMSCtx(), window, "displayLiveVersion", selectedVersion);
        } catch (Exception e) {
            throw wrapNuxeoException(e);
        }

    }

    /**
     * Creates the resource url.
     * 
     * @return the resource url
     */
    private ResourceURL createResourceURL() {
        if (this.response instanceof RenderResponse) {
            return ((RenderResponse) this.response).createResourceURL();
        } else if (this.response instanceof ResourceResponse) {
            return ((ResourceResponse) this.response).createResourceURL();
        }
        return null;
    }

    /**
     * Creates the file link.
     * 
     * @param doc the doc
     * @param fieldName the field name
     * @return the string
     * @throws Exception the exception
     */
    public String createFileLink(Document doc, String fieldName) {
        try {

            if ("ttc:vignette".equals(fieldName)) {
                String url = this.getRequest().getContextPath() + "/thumbnail?" + "path=" + URLEncoder.encode(doc.getPath(), "UTF-8");
                return url;

            } else {

                ResourceURL resourceURL = this.createResourceURL();
                resourceURL.setResourceID(doc.getId() + "/" + fieldName);
                resourceURL.setParameter("type", "file");
                resourceURL.setParameter("docPath", doc.getPath());
                resourceURL.setParameter("fieldName", fieldName);


                if (this.isDisplayingLiveVersion()) {
                    resourceURL.setParameter("displayLiveVersion", "1");

                }

                // Force to reload resources
                if (PageProperties.getProperties().isRefreshingPage()) {
                    resourceURL.setParameter("refresh", "" + System.currentTimeMillis());
                }

                // ne marche pas : bug JBP
                // resourceURL.setCacheability(ResourceURL.PORTLET);
                resourceURL.setCacheability(ResourceURL.PAGE);

                return resourceURL.toString();
            }
        } catch (Exception e) {
            throw wrapNuxeoException(e);
        }
    }

    /**
     * Creates the external link.
     * 
     * @param doc the doc
     * @return the string
     */
    public String createExternalLink(Document doc) {

        ResourceURL resourceURL = this.createResourceURL();
        resourceURL.setResourceID(doc.getId());
        resourceURL.setParameter("type", "link");
        // ne marche pas : bug JBP
        // resourceURL.setCacheability(ResourceURL.PORTLET);

        return resourceURL.toString();
    }

    /**
     * Creates the attached file link.
     * 
     * @param path the path
     * @param fileIndex the file index
     * @return the string
     */
    public String createAttachedFileLink(String path, String fileIndex) {

        ResourceURL resourceURL = this.createResourceURL();
        resourceURL.setResourceID(path + "/" + fileIndex);

        resourceURL.setParameter("type", "attachedFile");
        resourceURL.setParameter("fileIndex", fileIndex);
        resourceURL.setParameter("docPath", path);

        if (this.isDisplayingLiveVersion()) {
            resourceURL.setParameter("displayLiveVersion", "1");
        }

        // Force to reload resources
        if (PageProperties.getProperties().isRefreshingPage()) {
            resourceURL.setParameter("refresh", "" + System.currentTimeMillis());
        }

        // ne marche pas : bug JBP
        // resourceURL.setCacheability(ResourceURL.PORTLET);
        resourceURL.setCacheability(ResourceURL.PAGE);

        return resourceURL.toString();
    }


    /**
     * Creates the attached blob link.
     * 
     * @param path the path
     * @param blobIndex the blob index
     * @return the string
     */
    public String createAttachedBlobLink(String path, String blobIndex) {

        ResourceURL resourceURL = this.createResourceURL();
        resourceURL.setResourceID(path + "/" + blobIndex);

        resourceURL.setParameter("type", "blob");
        resourceURL.setParameter("blobIndex", blobIndex);
        resourceURL.setParameter("docPath", path);

        // Force to reload resources
        if (PageProperties.getProperties().isRefreshingPage()) {
            resourceURL.setParameter("refresh", "" + System.currentTimeMillis());
        }


        // ne marche pas : bug JBP
        // resourceURL.setCacheability(ResourceURL.PORTLET);
        resourceURL.setCacheability(ResourceURL.PAGE);

        return resourceURL.toString();
    }

    /**
     * Creates the attached picture link.
     * 
     * @param path the path
     * @param fileIndex the file index
     * @return the string
     */
    public String createAttachedPictureLink(String path, String fileIndex) {

        ResourceURL resourceURL = this.createResourceURL();
        resourceURL.setResourceID(path + "/" + fileIndex);

        resourceURL.setParameter("type", "attachedPicture");
        resourceURL.setParameter("pictureIndex", fileIndex);
        resourceURL.setParameter("docPath", path);

        if (this.isDisplayingLiveVersion()) {
            resourceURL.setParameter("displayLiveVersion", "1");
        }


        // Force to reload resources
        if (PageProperties.getProperties().isRefreshingPage()) {
            resourceURL.setParameter("refresh", "" + System.currentTimeMillis());
        }


        // ne marche pas : bug JBP
        // resourceURL.setCacheability(ResourceURL.PORTLET);
        resourceURL.setCacheability(ResourceURL.PAGE);

        return resourceURL.toString();
    }

    /**
     * Creates the picture link.
     * 
     * @param path the path
     * @param content the content
     * @return the string
     */
    public String createPictureLink(String path, String content) {

        ResourceURL resourceURL = this.createResourceURL();
        resourceURL.setResourceID(path + "/" + content);

        resourceURL.setParameter("type", "picture");
        resourceURL.setParameter("content", content);
        resourceURL.setParameter("docPath", path);

        // Force to reload resources
        if (PageProperties.getProperties().isRefreshingPage()) {
            resourceURL.setParameter("refresh", "" + System.currentTimeMillis());
        }


        // ne marche pas : bug JBP
        // resourceURL.setCacheability(ResourceURL.PORTLET);
        resourceURL.setCacheability(ResourceURL.PAGE);

        return resourceURL.toString();
    }


    /**
     * Creates the permalink.
     * 
     * @param path the path
     * @return the string
     * @throws Exception the exception
     */
    public String createPermalink(String path) {
        try {
            String permaLinkURL = this.getPortalUrlFactory().getPermaLink(this.getPortalCtx(), null, null, path, IPortalUrlFactory.PERM_LINK_TYPE_CMS);
            return permaLinkURL;
        } catch (Exception e) {
            throw wrapNuxeoException(e);
        }
    }

    /**
     * Gets the nuxeo public base uri.
     * 
     * @return the nuxeo public base uri
     */
    public URI getNuxeoPublicBaseUri() {
        if (this.nuxeoBaseURI == null) {
            this.nuxeoBaseURI = NuxeoConnectionProperties.getPublicBaseUri();
        }

        return this.nuxeoBaseURI;
    }

    /**
     * Display nuxeo error messages
     * 
     * @param e the e
     * @throws Exception the exception
     */
    public void handleErrors(NuxeoException e) throws PortletException {
        if (this.response instanceof RenderResponse) {
            PortletErrorHandler.handleGenericErrors((RenderResponse) this.response, e);
        }
    }

    /**
     * Execute a nuxeo command.
     * 
     * @param command the command
     * @return the object returned by the command
     * @throws Exception the exception
     */

    public Object executeNuxeoCommand(final INuxeoCommand command) {

        NuxeoCommandContext ctx = new NuxeoCommandContext(this.portletCtx, this.request);

        ctx.setAuthType(this.getAuthType());
        ctx.setAuthProfil(this.getScopeProfil());
        ctx.setCacheTimeOut(this.cacheTimeOut);
        ctx.setCacheType(this.cacheType);
        ctx.setAsynchronousUpdates(this.asynchronousUpdates);

        try {

            return this.getNuxeoCommandService().executeCommand(ctx, new INuxeoServiceCommand() {

                public String getId() {
                    return command.getId();
                }

                public Object execute(Session nuxeoSession) throws Exception {
                    return command.execute(nuxeoSession);
                }
            });


        } catch (Exception e) {
            throw wrapNuxeoException(e);
        }


    }


    /**
     * Start nuxeo service.
     * Must be called during portlet initialization
     * 
     * @throws Exception the exception
     */
    public void startNuxeoService() {
        try {
            NuxeoCommandServiceFactory.startNuxeoCommandService(this.getPortletCtx());
        } catch (Exception e) {
            throw wrapNuxeoException(e);
        }
    }

    /**
     * Stop nuxeo service.
     * 
     * @throws Exception the exception
     */
    public void stopNuxeoService() {
        try {
            NuxeoCommandServiceFactory.stopNuxeoCommandService(this.getPortletCtx());
        } catch (Exception e) {
            throw wrapNuxeoException(e);
        }
    }

    /**
     * Gets the link.
     * 
     * @param doc the doc
     * @return the link
     * @throws Exception the exception
     */
    public Link getLink(Document doc) {

        return this.getLink(doc, null);

    }

    /**
     * Gets the link.
     * 
     * @param doc the doc
     * @param displayContext the display context
     * @return the link
     * @throws Exception the exception
     */
    public Link getLink(Document doc, String displayContext) {
        return this.getLink(doc, displayContext, null);
    }


    /**
     * Generates a link to the target path.
     * 
     * @param path location of the target document
     * @param displayContext associates specific behaviour to the link
     * @return the CMS link by path
     * @throws Exception the exception
     */
    public Link getCMSLinkByPath(String path, String displayContext) {


        Window window = (Window) this.getPortalCtx().getRequest().getAttribute("osivia.window");
        Page page = window.getPage();

        Map<String, String> pageParams = new HashMap<String, String>();


        String url = this.getPortalUrlFactory().getCMSUrl(this.portalCtx, page.getId().toString(PortalObjectPath.CANONICAL_FORMAT), path, pageParams, null,
                displayContext, null, null, null, null);

        if (url != null) {

            Link link = new Link(url, false);
            return link;
        }

        return null;
    }


    /**
     * Generates a link to the target document.
     * 
     * @param doc nuxeo target document
     * @param displayContext specific behaviour
     * @param linkContextualization type of contextualisation {@link IPortalUrlFactory}
     * @return link
     * @throws Exception the exception
     */
    public Link getLink(Document doc, String displayContext, String linkContextualization) {

        try {
            String localContextualization = linkContextualization;

            INuxeoService nuxeoService = this.getNuxeoCMSService();

            CMSServiceCtx handlerCtx = new CMSServiceCtx();
            handlerCtx.setControllerContext(ControllerContextAdapter.getControllerContext(new PortalControllerContext(this.getPortletCtx(), this.getRequest(),
                    this.getResponse())));
            handlerCtx.setPortletCtx(this.getPortletCtx());
            handlerCtx.setRequest(this.getRequest());
            if (this.response instanceof RenderResponse) {
                handlerCtx.setResponse((RenderResponse) this.response);
            }
            handlerCtx.setScope(this.getScope());
            handlerCtx.setDisplayLiveVersion(this.getDisplayLiveVersion());
            handlerCtx.setPageId(this.getPageId());
            handlerCtx.setDoc(doc);
            handlerCtx.setHideMetaDatas(this.getHideMetaDatas());
            handlerCtx.setDisplayContext(displayContext);


            // On regarde si le lien est géré par le portlet

            Link portletLink = nuxeoService.getCMSCustomizer().createCustomLink(handlerCtx);
            if (portletLink != null) {
                return portletLink;
            }


            // Sinon on passe par le gestionnaire de cms pour recontextualiser

            Window window = (Window) this.getPortalCtx().getRequest().getAttribute("osivia.window");
            Page page = window.getPage();

            Map<String, String> pageParams = new HashMap<String, String>();


            // v2.0-rc7 : suppression du scope
            // String url = getPortalUrlFactory().getCMSUrl(portalCtx,
            // page.getId().toString(PortalObjectPath.CANONICAL_FORMAT), doc.getPath(), pageParams, localContextualization, displayContext, getHideMetaDatas(),
            // getScope(), getDisplayLiveVersion(), null);
            String path = doc.getPath();

            String webid = doc.getString("ttc:webid");

            if (StringUtils.isNotEmpty(webid)) {
                
                String domainId = doc.getString("ttc:domainID");
                String explicitUrl = doc.getString("ttc:explicitUrl");
                String extension = doc.getString("ttc:extensionUrl");
                
                
                Map<String, String> properties = new HashMap<String, String>();
                if (domainId != null) {
                    properties.put(IWebIdService.DOMAIN_ID, domainId);
                }
                if (explicitUrl != null) {
                    properties.put(IWebIdService.EXPLICIT_URL, explicitUrl);
                }
                if (extension != null) {
                    properties.put(IWebIdService.EXTENSION_URL, extension);
                }
                CMSItem cmsItem = new CMSItem(path, webid, properties, doc);
                
                path = getWebIdService().itemToPageUrl(cmsItem);

            }


            String url = this.getPortalUrlFactory().getCMSUrl(this.portalCtx, page.getId().toString(PortalObjectPath.CANONICAL_FORMAT), path,
                    pageParams, localContextualization, displayContext, this.getHideMetaDatas(), null, this.getDisplayLiveVersion(), null);


            if (url != null) {

                Link link = new Link(url, false);
                return link;
            }

            return null;

        } catch (Exception e) {
            throw wrapNuxeoException(e);
        }

    }


    /**
     * Insert content menubar items.
     * 
     * @throws Exception the exception
     */
    public void insertContentMenuBarItems() {
        try {
            INuxeoService nuxeoService = this.getNuxeoCMSService();
            nuxeoService.getCMSCustomizer().formatContentMenuBar(this.getCMSCtx());
        } catch (Exception e) {
            throw wrapNuxeoException(e);
        }
    }


    /**
     * Get Nuxeo comments service instance.
     * 
     * @return Nuxeo comments service instance
     */
    public INuxeoCommentsService getNuxeoCommentsService() {
        try {
            INuxeoService nuxeoService = this.getNuxeoCMSService();
            return nuxeoService.getCMSCustomizer().getNuxeoCommentsService();
        } catch (Exception e) {
            throw wrapNuxeoException(e);
        }
    }


    /**
     * Get Nuxeo document comments HTML formatted content.
     * 
     * @return comments HTML formatted content
     * @throws CMSException the CMS exception
     */
    public String getCommentsHTMLContent() throws CMSException {
        try {
            CMSServiceCtx cmsContext = this.getCMSCtx();
            INuxeoService nuxeoService = this.getNuxeoCMSService();
            return nuxeoService.getCMSCustomizer().getCommentsHTMLContent(cmsContext, this.currentDoc);
        } catch (Exception e) {
            throw wrapNuxeoException(e);
        }
    }


    /**
     * Get CMS item types.
     * 
     * @return CMS item types
     */
    public Map<String, CMSItemType> getCMSItemTypes() {
        INuxeoService nuxeoService = this.getNuxeoCMSService();
        return nuxeoService.getCMSCustomizer().getCMSItemTypes();
    }


    /**
     * Fetch a document by its path.
     * 
     * @param path the path
     * @param reload force reloading of the document (no cache)
     * @return the document
     * @throws Exception the exception
     */
    public Document fetchDocument(String path, boolean reload) {


        try {
            CMSServiceCtx cmsCtx = this.getCMSCtx();
            // Prévisualisation des portlets définis au niveau du template
            if (path.equals(this.getNavigationPath())) {
                if (CmsPermissionHelper.getCurrentPageSecurityLevel(cmsCtx.getControllerContext(), path) == Level.allowPreviewVersion) {
                    cmsCtx.setDisplayLiveVersion("1");
                }

            }

            if (reload) {
                cmsCtx.setForceReload(true);
            }


            CMSItem cmsItem = getCMSService().getContent(cmsCtx, path);
            return (Document) cmsItem.getNativeItem();

        } catch (Exception e) {
            throw wrapNuxeoException(e);
        }
    }


    /**
     * Fetch a document by its path.
     * 
     * @param path the path
     * @return the fetched document
     * @throws Exception the exception
     */
    public Document fetchDocument(String path) {
        return this.fetchDocument(path, false);
    }


    /**
     * Get the live identifier for a specified path.
     * 
     * @param path path to fetch
     * @return nuxeo id of the document
     * @throws Exception the exception
     */
    public String fetchLiveId(String path) {

        try {
            CMSPublicationInfos pubInfos = getCMSService().getPublicationInfos(this.getCMSCtx(), path);
            return pubInfos.getLiveId();

        } catch (Exception e) {
            throw wrapNuxeoException(e);
        }
    }


    /**
     * Computes the parent path for a specifed path
     * 
     * 
     * @param path the path
     * @return the parent path
     * @throws Exception the exception
     */
    public String getParentPath(String path)  {
        // One level up
        CMSObjectPath parentPath = CMSObjectPath.parse(path).getParent();
        return parentPath.toString();
    }


    /**
     * Computes live path for current document
     * (may differs from original path in case of a proxy).
     * 
     * @param path the path
     * @return the live path
     */

    public String getLivePath(String path) {
        String result = path;
        if (path.endsWith(".proxy")) {
            result = result.substring(0, result.length() - 6);
        }
        return result;
    }


    /**
     * Fetch attached picture .
     * 
     * @param docPath path of the document
     * @param pictureIndex picture range
     * @return the CMS binary content
     */

    public CMSBinaryContent fetchAttachedPicture(String docPath, String pictureIndex) {
        try {


            return getCMSService().getBinaryContent(this.getCMSCtx(), "attachedPicture", docPath, pictureIndex);

        } catch (Exception e) {
            throw wrapNuxeoException(e);
        }
    }


    /**
     * Fetch picture.
     * 
     * @param docPath the doc path
     * @param content the content
     * @return the CMS binary content
     */
    public CMSBinaryContent fetchPicture(String docPath, String content) {
        try {


            return getCMSService().getBinaryContent(this.getCMSCtx(), "picture", docPath, content);

        } catch (Exception e) {
            throw wrapNuxeoException(e);
        }
    }

    /**
     * Fetch file content.
     * 
     * @param docPath the doc path
     * @param fieldName the field name
     * @return the CMS binary content
     */
    public CMSBinaryContent fetchFileContent(String docPath, String fieldName) {
        try {


            return getCMSService().getBinaryContent(this.getCMSCtx(), "file", docPath, fieldName);

        } catch (Exception e) {
            throw wrapNuxeoException(e);
        }
    }

    /**
     * Fetch web url.
     * 
     * @param webid the web id
     * @param content some options
     * @return the resource
     */
    public String createWebIdLink(String webid, String content) {

        ResourceURL resourceURL = this.createResourceURL();

        resourceURL.setResourceID(webid);
        if (content != null) {
            resourceURL.setParameter("type", "picture");
            resourceURL.setParameter("docPath", webid);
            resourceURL.setParameter("content", content);
        }
        //

        // ne marche pas : bug JBP
        // resourceURL.setCacheability(ResourceURL.PORTLET);
        resourceURL.setCacheability(ResourceURL.PAGE);

        return resourceURL.toString();
    }

    /**
     * Gets the CMS service.
     * 
     * @return the CMS service
     */
    public static ICMSService getCMSService() {

        if (cmsServiceLocator == null) {
            cmsServiceLocator = Locator.findMBean(ICMSServiceLocator.class, "osivia:service=CmsServiceLocator");
        }

        return cmsServiceLocator.getCMSService();

    }


    /**
     * Gets the CMS ctx.
     * 
     * @return the CMS ctx
     */
    public CMSServiceCtx getCMSCtx() {

        this.cmsCtx = new CMSServiceCtx();

        this.cmsCtx.setControllerContext(ControllerContextAdapter.getControllerContext(new PortalControllerContext(this.getPortletCtx(), this.getRequest(),
                this.getResponse())));


        this.cmsCtx.setPortletCtx(this.getPortletCtx());
        this.cmsCtx.setRequest(this.getRequest());
        if (this.response instanceof RenderResponse) {
            this.cmsCtx.setResponse((RenderResponse) this.response);
        }
        this.cmsCtx.setScope(this.getScope());
        this.cmsCtx.setForcePublicationInfosScope(this.getForcePublicationInfosScope());
        this.cmsCtx.setDisplayLiveVersion(this.getDisplayLiveVersion());


        // Preview mode
        EditionState editionState = (EditionState) this.getRequest().getAttribute("osivia.editionState");
        if ((editionState != null) && EditionState.CONTRIBUTION_MODE_EDITION.equals(editionState.getContributionMode())) {
            this.cmsCtx.setPreviewVersionPath(editionState.getDocPath());
        }

        this.cmsCtx.setPageId(this.getPageId());
        this.cmsCtx.setDoc(this.getCurrentDoc());
        this.cmsCtx.setHideMetaDatas(this.getHideMetaDatas());
        this.cmsCtx.setDisplayContext(this.displayContext);

        this.cmsCtx.setCreationType(this.docTypeToCreate);
        if (this.parentPathToCreate != null) {
            this.cmsCtx.setCreationPath(this.getComputedPath(this.parentPathToCreate));
        }

        if (reloadResource)
            cmsCtx.setForceReload(true);
        cmsCtx.setStreamingSupport(streamingSupport);

        return this.cmsCtx;
    }


    /**
     * Gets the document configuration.
     * 
     * @param doc the doc
     * @return the document configuration
     * @throws Exception the exception
     */
    public Map<String, String> getDocumentConfiguration(Document doc) {
        try {
            INuxeoService nuxeoService = this.getNuxeoCMSService();
            return nuxeoService.getCMSCustomizer().getDocumentConfiguration(this.getCMSCtx(), doc);
        } catch (Exception e) {
            throw wrapNuxeoException(e);
        }
    }

    /**
     * Gets the debug infos.
     * 
     * @return the debug infos
     */
    public String getDebugInfos() {
        String output = "";

        if ("1".equals(System.getProperty("nuxeo.debugHtml"))) {

            output += "<p class=\"nuxeo-debug\" align=\"center\">";
            output += "scope : " + this.getScope();
            output += "</p>";

            return output;
        } else {

            output += "<!--";
            output += "scope : " + this.getScope();
            output += "-->";
        }

        return output;

    }

}
