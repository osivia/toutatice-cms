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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.portlet.MimeResponse;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.ResourceURL;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.Portal;
import org.jboss.portal.core.model.portal.PortalObjectPath;
import org.jboss.portal.core.model.portal.Window;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.cache.services.CacheInfo;
import org.osivia.portal.api.cms.DocumentContext;
import org.osivia.portal.api.cms.DocumentState;
import org.osivia.portal.api.cms.DocumentType;
import org.osivia.portal.api.cms.impl.BasicPermissions;
import org.osivia.portal.api.cms.impl.BasicPublicationInfos;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.contribution.IContributionService.EditionState;
import org.osivia.portal.api.directory.IDirectoryService;
import org.osivia.portal.api.directory.IDirectoryServiceLocator;
import org.osivia.portal.api.directory.entity.DirectoryPerson;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.urls.ExtendedParameters;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.api.urls.Link;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;
import org.osivia.portal.core.cms.BinaryDescription;
import org.osivia.portal.core.cms.CMSBinaryContent;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSObjectPath;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.ICMSService;
import org.osivia.portal.core.cms.ICMSServiceLocator;
import org.osivia.portal.core.constants.InternalConstants;
import org.osivia.portal.core.context.ControllerContextAdapter;
import org.osivia.portal.core.formatters.IFormatter;
import org.osivia.portal.core.portalobjects.PortalObjectUtils;
import org.osivia.portal.core.profils.IProfilManager;
import org.osivia.portal.core.profils.ProfilBean;
import org.osivia.portal.core.web.IWebIdService;

import fr.toutatice.portail.cms.nuxeo.api.cms.NuxeoDocumentContext;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCommandService;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCommentsService;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCustomizer;
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

    /** Request attribute name. */
    public static final String REQUEST_ATTRIBUTE = NuxeoController.class.getSimpleName();
    /** Slash separator. */
    private static final String SLASH = "/";
    /** Dot separator. */
    private static final String DOT = ".";
    /** Prefix used to query document in the ECM. */
    private static final String FETCH_PATH_PREFIX = "webId:";
    /** Prefix for CMS path. */
    private static final String CMS_PATH_PREFIX = "/_id";

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


    /** The domain path. */
    String domainPath;

    HttpServletRequest servletRequest;

    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }


    public void setServletRequest(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }

    /** Directory service */
    private IDirectoryServiceLocator directoryServiceLocator;

    private IDirectoryService directoryService;

    private IDirectoryService getDirectoryService() {
        if (directoryService == null) {

            directoryServiceLocator = Locator.findMBean(IDirectoryServiceLocator.class, IDirectoryServiceLocator.MBEAN_NAME);
            directoryService = directoryServiceLocator.getDirectoryService();
        }
        return directoryService;
    }


    /**
     * Gets the domain path.
     *
     * @return the domain path
     */
    public String getDomainPath() {
        return domainPath;
    }

    /**
     * Gets the parent path to create.
     *
     * @return the parent path to create
     */
    public String getParentPathToCreate() {
        return parentPathToCreate;
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
        return menuRootPath;
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

    String forcedLivePath = null;


    public String getForcedLivePath() {
        return forcedLivePath;
    }


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
        if (webIdService == null) {
            webIdService = (IWebIdService) getPortalCtx().getPortletCtx().getAttribute("webIdService");
        }

        return webIdService;
    }

    /** The cms service locator. */
    private static ICMSServiceLocator cmsServiceLocator;

    /**
     * Gets the current doc.
     *
     * @return the current doc
     */
    public Document getCurrentDoc() {
        return currentDoc;
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
        return spacePath;
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

        return basePath;
    }

    /**
     * Gets the navigation path.
     *
     * @return the navigation path
     */
    public String getNavigationPath() {
        return navigationPath;
    }

    /**
     * path reel de contenu.
     *
     * @return the content path
     */
    public String getContentPath() {
        return contentPath;
    }


    /**
     * path de navigation du contenu.
     *
     * @return the item navigation path
     */
    public String getItemNavigationPath() {
        return itemNavigationPath;
    }


    /**
     * Gets the display context.
     *
     * @return the display context
     */
    public String getDisplayContext() {
        return displayContext;
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
        return hideMetaDatas;
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
        return displayLiveVersion;
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
        if ("1".equals(displayLiveVersion)) {
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
        return forcePublicationInfosScope;
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
        return scope;
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
        return cacheType;
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
        return scopeProfil;
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
        return asynchronousUpdates;
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
        return cacheTimeOut;
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
        setAuthType(NuxeoCommandContext.AUTH_TYPE_USER);
        setCacheType(CacheInfo.CACHE_SCOPE_NONE);

        if ("anonymous".equals(scope)) {
            setAuthType(NuxeoCommandContext.AUTH_TYPE_ANONYMOUS);
            setCacheType(CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);
            /*
             * } else if ("__nocache".equals(scope)) {
             * setAuthType( NuxeoCommandContext.AUTH_TYPE_ANONYMOUS);
             * setCacheType( CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);
             */
        } else if ((scope != null) && !"__nocache".equals(scope)) {
            setAuthType(NuxeoCommandContext.AUTH_TYPE_PROFIL);
            setScopeProfil(getProfilManager().getProfil(scope));
            setCacheType(CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);
        }

        this.scope = scope;
    }

    /**
     * Gets the auth type.
     *
     * @return the auth type
     */
    public int getAuthType() {
        return authType;
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
        if (nuxeoConnection == null) {
            nuxeoConnection = new NuxeoConnectionProperties();
        }
        return nuxeoConnection;
    }

    /**
     * Gets the portal ctx.
     *
     * @return the portal ctx
     */
    public PortalControllerContext getPortalCtx() {

        if (portalCtx == null) {
            portalCtx = new PortalControllerContext(getPortletCtx(), request, response);
        }

        return portalCtx;
    }

    /**
     * Gets the request.
     *
     * @return the request
     */
    public PortletRequest getRequest() {
        return request;
    }

    /**
     * Gets the response.
     *
     * @return the response
     */
    public PortletResponse getResponse() {
        return response;
    }

    /**
     * Gets the portlet ctx.
     *
     * @return the portlet ctx
     */
    public PortletContext getPortletCtx() {
        return portletCtx;
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

            portalCtx = new PortalControllerContext(portletCtx, request, response);

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
                    setForcePublicationInfosScope(forcePublicationScope);
                }
            }

            navigationScope = window.getPageProperty("osivia.cms.navigationScope");


            spacePath = window.getPageProperty("osivia.cms.basePath");
            CMSItem publishSpaceConfig = null;
            if (spacePath != null) {
                publishSpaceConfig = getCMSService().getSpaceConfig(getCMSCtx(), spacePath);
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


            setScope(scope);
            setDisplayLiveVersion(displayLiveVersion);

            // Hide metadata indicator
            setHideMetaDatas(window.getProperty(InternalConstants.METADATA_WINDOW_PROPERTY));

            setDocTypeToCreate(window.getProperty("osivia.createDocType"));
            String parentPathToCreate = window.getProperty("osivia.createParentPath");
            setParentPathToCreate(parentPathToCreate);


            setPageMarker((String) request.getAttribute("osivia.pageMarker"));


            /* computes root path */

            Window jbpWindow = (Window) request.getAttribute("osivia.window");
            Page page = (Page) jbpWindow.getParent();
            Portal portal = page.getPortal();
            if (InternalConstants.PORTAL_TYPE_SPACE.equals(portal.getDeclaredProperty("osivia.portal.portalType"))) {
                menuRootPath = portal.getDefaultPage().getDeclaredProperty("osivia.cms.basePath");
            }


            basePath = window.getPageProperty("osivia.cms.basePath");

            if (basePath != null) {
                String[] parts = basePath.split("/");
                if ((parts != null) && (parts.length > 0)) {
                    domainPath = "/" + parts[1];
                }
            }


            navigationPath = request.getParameter("osivia.cms.path");

            if ((spacePath != null) && (request.getParameter("osivia.cms.itemRelPath") != null)) {
                itemNavigationPath = spacePath + request.getParameter("osivia.cms.itemRelPath");
            }

            contentPath = request.getParameter("osivia.cms.contentPath");


            if (request instanceof ResourceRequest) {
                if (request.getParameter("refresh") != null) {
                    reloadResource = true;
                }
            }


            // Preview mode
            if (getRequest() != null) {

                EditionState editionState = (EditionState) getRequest().getAttribute("osivia.editionState");
                if ((editionState != null) && EditionState.CONTRIBUTION_MODE_EDITION.equals(editionState.getContributionMode())) {
                    forcedLivePath = editionState.getDocPath();
                } else {


                    // mode web page
                    String webPageEditionPath = (String) getRequest().getAttribute("osivia.cms.webPageEditionPath");
                    if (webPageEditionPath != null) {
                        forcedLivePath = editionState.getDocPath();
                    }
                }


            }


            // Inject Nuxeo controller into request
            request.setAttribute(REQUEST_ATTRIBUTE, this);
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
        } else {
            return new NuxeoException(e);
        }

    }


    /**
     * Sets the doc type to create.
     *
     * @param property the new doc type to create
     */
    public void setDocTypeToCreate(String property) {
        docTypeToCreate = property;
    }

    /**
     * Gets the navigation item.
     *
     * @return the navigation item
     * @throws Exception the exception
     * @deprecated use DocumentContext
     */
    @Deprecated
	public CMSItem getNavigationItem() {
        try {
            if (navItem == null) {
                if (getNavigationPath() != null) {
                    // Navigation context
                    CMSServiceCtx cmsReadNavContext = new CMSServiceCtx();
                    cmsReadNavContext.setControllerContext(ControllerContextAdapter.getControllerContext(getPortalCtx()));
                    cmsReadNavContext.setScope(getNavigationScope());

                    // TODO : factoriser dans NuxeoController

                    navItem = getCMSService().getPortalNavigationItem(cmsReadNavContext, getSpacePath(), getNavigationPath());
                }

            }

            return navItem;
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
        return navigationScope;
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
        if (urlFactory == null) {
            urlFactory = (IPortalUrlFactory) portletCtx.getAttribute("UrlService");
        }

        return urlFactory;
    }

    /**
     * Gets the nuxeo command service.
     *
     * @return the nuxeo command service
     * @throws Exception the exception
     */
    public INuxeoCommandService getNuxeoCommandService() {
        try {
            if (nuxeoCommandService == null) {
                nuxeoCommandService = NuxeoCommandServiceFactory.getNuxeoCommandService(portletCtx);
            }
            return nuxeoCommandService;
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
        if (profilManager == null) {
            profilManager = (IProfilManager) portletCtx.getAttribute(Constants.PROFILE_SERVICE_NAME);
        }


        return profilManager;
    }

    /**
     * Gets the formatter.
     *
     * @return the formatter
     * @throws Exception the exception
     */
    public IFormatter getFormatter() {
        if (formatter == null) {
            formatter = (IFormatter) portletCtx.getAttribute("FormatterService");
        }


        return formatter;
    }


    /**
     * Get Nuxeo CMS service instance.
     *
     * @return Nuxeo CMS service instance
     */
    public INuxeoService getNuxeoCMSService() {
        if (nuxeoCMSService == null) {
            nuxeoCMSService = (INuxeoService) getPortletCtx().getAttribute("NuxeoService");
            if (nuxeoCMSService == null) {
                nuxeoCMSService = Locator.findMBean(INuxeoService.class, "osivia:service=NuxeoService");
            }
        }
        return nuxeoCMSService;
    }


    /**
     * Gets the page id.
     *
     * @return the page id
     */
    public String getPageId() {
        if (pageId == null) {
            Window window = (Window) request.getAttribute("osivia.window");
            Page page = (Page) window.getParent();
            try {
                pageId = URLEncoder.encode(page.getId().toString(PortalObjectPath.SAFEST_FORMAT), "UTF-8");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
        return pageId;
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
                String path = getBasePath();
                if (path == null) {
                    path = "";
                }

                computedPath = computedPath.replaceAll("\\$\\{basePath\\}", path);
            }


            if (computedPath.contains("${spacePath}")) {
                String path = getSpacePath();
                if (path == null) {
                    path = "";
                }

                computedPath = computedPath.replaceAll("\\$\\{spacePath\\}", path);
            }


            if (computedPath.contains("${navigationPath}")) {

                String path = getNavigationPath();
                if (path == null) {
                    path = "";
                }

                computedPath = computedPath.replaceAll("\\$\\{navigationPath\\}", path);
            }

            if (computedPath.contains("${contentPath}")) {

                String path = getContentPath();
                if (path == null) {
                    path = "";
                }

                computedPath = computedPath.replaceAll("\\$\\{contentPath\\}", path);
            }

            if (computedPath.contains("${sitePath}")) {
                String path = getMenuRootPath();
                if (path == null) {
                    path = "";
                }

                computedPath = computedPath.replaceAll("\\$\\{sitePath\\}", path);
            }

            if (computedPath.contains("${domainPath}")) {
                String path = getDomainPath();
                if (path == null) {
                    path = "";
                }

                computedPath = computedPath.replaceAll("\\$\\{domainPath\\}", path);
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
            INuxeoService nuxeoService = getNuxeoCMSService();
            return nuxeoService.getCMSCustomizer().transformHTMLContent(getCMSCtx(), htmlContent);
        } catch (Exception e) {
            throw wrapNuxeoException(e);
        }
    }


    /**
     * Transform Nuxeo link URL
     *
     * @param link Nuxeo link URL
     * @return transformed Nuxeo link URL
     */
    public String transformNuxeoLink(String link) {
        try {
            INuxeoService nuxeoService = getNuxeoCMSService();
            return nuxeoService.getCMSCustomizer().transformLink(getCMSCtx(), link);
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

            Window window = (Window) request.getAttribute("osivia.window");

            return getFormatter().formatScopeList(window, "scope", selectedScope);
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

            Window window = (Window) request.getAttribute("osivia.window");

            return getFormatter().formatRequestFilteringPolicyList(window, "requestFilteringPolicy", selectedRequestFilteringPolicy);

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

            Window window = (Window) request.getAttribute("osivia.window");

            return getFormatter().formatDisplayLiveVersionList(getCMSCtx(), window, "displayLiveVersion", selectedVersion);
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
        if (response instanceof RenderResponse) {
            return ((RenderResponse) response).createResourceURL();
        } else if (response instanceof ResourceResponse) {
            return ((ResourceResponse) response).createResourceURL();
        }
        return null;
    }


    /**
     * Checks if current path is in edition state.
     *
     * @param path the path
     * @return true, if is in page edition state
     * @throws CMSException the CMS exception
     * @deprecated use DocumentContext
     */
    @Deprecated
	public boolean isIdOrPathInLiveState(String originalPath) {

        if (isDisplayingLiveVersion()) {
            return true;
        }

        String path = "";

        try {
            // Path might be an ID
            if (originalPath.startsWith("/")) {
                path = originalPath;
            } else {
                CMSPublicationInfos pubInfos = getCMSService().getPublicationInfos(getCMSCtx(), originalPath);
                path = pubInfos.getDocumentPath();
            }


        } catch (CMSException e) {
            throw new RuntimeException(e);
        }


        return isPathInPageEditionState(path);
    }


    /**
     * Generic binary URL
     *
     * @param path the path
     * @return true, if is in page edition state
     * @throws CMSException the CMS exception
     */


    public String getBinaryURL(BinaryDescription binary) {


        try {
            return getCMSService().getBinaryResourceURL(getCMSCtx(), binary).getUrl();
        } catch (CMSException e) {
            throw new RuntimeException(e);
        }

    }



    /**
     * Checks if current path is in page edition state (web page edition mode)
     *
     * @param path the path
     * @return true, if is in page edition state
     * @throws CMSException the CMS exception
     */
    @Deprecated
    public boolean isPathInPageEditionState(String path) {


        if (path.equals(getNavigationPath())) {
            // Uniquement en mode web page
            if (path.equals(getRequest().getAttribute("osivia.cms.webPageEditionPath"))) {
                return true;
            }
        }


        if (getRequest() != null) {
            EditionState editionState = (EditionState) getRequest().getAttribute("osivia.editionState");
            if ((editionState != null) && EditionState.CONTRIBUTION_MODE_EDITION.equals(editionState.getContributionMode())) {
                if( editionState.getDocPath().equals(path)) {
                    return true;
                }
            }
        }


        return false;
    }


    /**
     * Create file link URL from Nuxeo document path.
     *
     * @param path Nuxeo document path
     * @param fieldName field name
     * @param fileName file name
     * @return URL
     */
    public String createFileLink(String path, String fieldName, String fileName) {
        try {
            BinaryDescription binary = new BinaryDescription(BinaryDescription.Type.FILE, path);
            binary.setFieldName(fieldName);
            binary.setFileName(fileName);
            return getBinaryURL(binary);
        } catch (Exception e) {
            throw wrapNuxeoException(e);
        }
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
            BinaryDescription binary = new BinaryDescription(BinaryDescription.Type.FILE, doc.getPath());
            binary.setFieldName(fieldName);
            binary.setDocument(doc);
            return getBinaryURL(binary);
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

        ResourceURL resourceURL = createResourceURL();
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
        BinaryDescription binary = new BinaryDescription(BinaryDescription.Type.ATTACHED_FILE, path);
        binary.setIndex(fileIndex);
        return getBinaryURL(binary);

    }


    /**
     * Creates the attached blob link.
     *
     * @param path the path
     * @param blobIndex the blob index
     * @return the string
     */
    public String createAttachedBlobLink(String path, String blobIndex) {
        BinaryDescription binary = new BinaryDescription(BinaryDescription.Type.BLOB, path);
        binary.setIndex(blobIndex);
        return getBinaryURL(binary);

    }

    /**
     * Creates the attached picture link.
     *
     * @param path the path
     * @param fileIndex the file index
     * @return the string
     */


    public String createAttachedPictureLink(String path, String fileIndex) {
        try {
            BinaryDescription binary = new BinaryDescription(BinaryDescription.Type.ATTACHED_PICTURE, path);
            binary.setDocument(getCurrentDoc());
            binary.setIndex(fileIndex);
            return getBinaryURL(binary);
        } catch (Exception e) {
            throw wrapNuxeoException(e);
        }
    }

    /**
     * Creates the picture link.
     *
     * @param path the path
     * @param content the content
     * @return the string
     */
    public String createPictureLink(String path, String content) {
        BinaryDescription binary = new BinaryDescription(BinaryDescription.Type.PICTURE, path);
        binary.setContent(content);
        return getBinaryURL(binary);
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
            String permaLinkURL = getPortalUrlFactory().getPermaLink(getPortalCtx(), null, null, path, IPortalUrlFactory.PERM_LINK_TYPE_CMS);
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
        if (nuxeoBaseURI == null) {
            nuxeoBaseURI = NuxeoConnectionProperties.getPublicBaseUri();
        }

        return nuxeoBaseURI;
    }

    /**
     * Display nuxeo error messages
     *
     * @param e the e
     * @throws Exception the exception
     */
    public void handleErrors(NuxeoException e) throws PortletException {
        if (response instanceof RenderResponse) {
            PortletErrorHandler.handleGenericErrors((RenderResponse) response, e);
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

        NuxeoCommandContext ctx;
        if (request != null) {
            ctx = new NuxeoCommandContext(portletCtx, request);
        } else if (servletRequest != null) {
            ctx = new NuxeoCommandContext(portletCtx, servletRequest);
        } else {
            ctx = new NuxeoCommandContext(portletCtx);
        }

        ctx.setAuthType(getAuthType());
        ctx.setAuthProfil(getScopeProfil());
        ctx.setCacheTimeOut(cacheTimeOut);
        ctx.setCacheType(cacheType);
        ctx.setAsynchronousUpdates(asynchronousUpdates);

        try {

            return getNuxeoCommandService().executeCommand(ctx, new INuxeoServiceCommand() {

                @Override
                public String getId() {
                    return command.getId();
                }

                @Override
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
            NuxeoCommandServiceFactory.startNuxeoCommandService(getPortletCtx());
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
            NuxeoCommandServiceFactory.stopNuxeoCommandService(getPortletCtx());
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
     * Get portal link from Nuxeo or absolute URL.
     *
     * @param url Nuxeo or absolute URL
     * @return portal link
     */
    public Link getLinkFromNuxeoURL(String url) {
        // Nuxeo service
        INuxeoService nuxeoService = getNuxeoCMSService();
        // Nuxeo customizer
        INuxeoCustomizer nuxeoCustomizer = nuxeoService.getCMSCustomizer();

        // Customizer call
        return nuxeoCustomizer.getLinkFromNuxeoURL(getCMSCtx(), url);
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


        Window window = (Window) getPortalCtx().getRequest().getAttribute("osivia.window");
        Page page = window.getPage();

        Map<String, String> parameters = new HashMap<String, String>(0);

        // path can have parameters
        ExtendedParameters extendedParameters = null;
        Map<String, String> nxPathParameters = getCMSService().getNxPathParameters(path);
        if (MapUtils.isNotEmpty(nxPathParameters)) {
            path = StringUtils.substringBefore(path, "?");
            extendedParameters = new ExtendedParameters();
            extendedParameters.setAllParameters(nxPathParameters);
        }

        String url = StringUtils.EMPTY;
        if(extendedParameters != null){
            url = getPortalUrlFactory().getCMSUrl(portalCtx, page.getId().toString(PortalObjectPath.CANONICAL_FORMAT), path, parameters, null,
                displayContext, null, null, null, null, extendedParameters);
        } else {
            url = getPortalUrlFactory().getCMSUrl(portalCtx, page.getId().toString(PortalObjectPath.CANONICAL_FORMAT), path, parameters, null,
                    displayContext, null, null, null, null);
        }

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

            INuxeoService nuxeoService = getNuxeoCMSService();

            CMSServiceCtx handlerCtx = new CMSServiceCtx();
            handlerCtx.setControllerContext(ControllerContextAdapter.getControllerContext(new PortalControllerContext(getPortletCtx(), getRequest(),
                    getResponse())));
            handlerCtx.setPortletCtx(getPortletCtx());
            handlerCtx.setRequest(getRequest());
            if (response instanceof MimeResponse) {
                handlerCtx.setResponse((MimeResponse) response);
            }
            handlerCtx.setScope(getScope());
            handlerCtx.setDisplayLiveVersion(getDisplayLiveVersion());
            handlerCtx.setPageId(getPageId());
            handlerCtx.setDoc(doc);
            handlerCtx.setHideMetaDatas(getHideMetaDatas());
            handlerCtx.setDisplayContext(displayContext);


            // On regarde si le lien est géré par le portlet

            Link portletLink = nuxeoService.getCMSCustomizer().createCustomLink(handlerCtx);
            if (portletLink != null) {
                return portletLink;
            }


            // Sinon on passe par le gestionnaire de cms pour recontextualiser

            Window window = (Window) getPortalCtx().getRequest().getAttribute("osivia.window");
            Page page = window.getPage();

            Map<String, String> pageParams = new HashMap<String, String>();


            String displayLiveVersion = getDisplayLiveVersion();

            String path = doc.getPath();
            
            ExtendedParameters extendedParameters = null;
            if (PortalObjectUtils.isSpaceSite(page.getPortal())) {
                path = nuxeoService.getCMSCustomizer().getContentWebIdPath(handlerCtx);
                // CMS path can have parameters
                Map<String, String> nxPathParameters = getCMSService().getNxPathParameters(path);
                if (MapUtils.isNotEmpty(nxPathParameters)) {
                    path = StringUtils.substringBefore(path, "?");
                    extendedParameters = new ExtendedParameters();
                    extendedParameters.setAllParameters(nxPathParameters);
                }
            }

            String url = getPortalUrlFactory().getCMSUrl(portalCtx, page.getId().toString(PortalObjectPath.CANONICAL_FORMAT), path, pageParams,
                    localContextualization, displayContext, getHideMetaDatas(), null, displayLiveVersion, null, extendedParameters);


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
     * Gets the content web id path ( like /_id/domain-def-jss/publistatfaq.html)
     *
     * if no webId is defined, returns original path
     *
     * @return the content web id path
     */

    public String getContentWebIdPath() {

        INuxeoService nuxeoService = getNuxeoCMSService();

        String path = nuxeoService.getCMSCustomizer().getContentWebIdPath(getCMSCtx());
        return path;
    }

    /**
     * Insert content menubar items.
     *
     * @throws Exception the exception
     */
    public void insertContentMenuBarItems() {
        try {
            INuxeoService nuxeoService = getNuxeoCMSService();
            nuxeoService.getCMSCustomizer().formatContentMenuBar(getCMSCtx());
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
            INuxeoService nuxeoService = getNuxeoCMSService();
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
     * @deprecated unused ?
     */
    @Deprecated
	public String getCommentsHTMLContent() throws CMSException {
        try {
            CMSServiceCtx cmsContext = getCMSCtx();
            INuxeoService nuxeoService = getNuxeoCMSService();
            return nuxeoService.getCMSCustomizer().getCommentsHTMLContent(cmsContext, currentDoc);
        } catch (Exception e) {
            throw wrapNuxeoException(e);
        }
    }


    /**
     * Get CMS item types.
     *
     * @return CMS item types
     */
    public Map<String, DocumentType> getCMSItemTypes() {
        INuxeoService nuxeoService = getNuxeoCMSService();
        return nuxeoService.getCMSCustomizer().getCMSItemTypes();
    }


    /**
     * Fetch a document by its path.
     *
     * @param path the path
     * @param reload force reloading of the document (no cache)
     * @return the document
     * @throws Exception the exception
     * @deprecated use DocumentContext      *
     */
    @Deprecated
	public Document fetchDocument(String path, boolean reload) {


        try {
            CMSServiceCtx cmsCtx = getCMSCtx();


            // Prévisualisation des portlets définis au niveau du template
            if (isPathInPageEditionState(path)) {
                cmsCtx.setDisplayLiveVersion("1");
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
     * @deprecated use DocumentContext
     */
    @Deprecated
	public Document fetchDocument(String path) {
        return this.fetchDocument(path, false);
    }


    /**
     * Get the live identifier for a specified path.
     *
     * @param path path to fetch
     * @return nuxeo id of the document
     * @throws Exception the exception
     * @deprecated use DocumentContext
     */
    @Deprecated
	public String fetchLiveId(String path) {

        try {
            CMSPublicationInfos pubInfos = getCMSService().getPublicationInfos(getCMSCtx(), path);
            return pubInfos.getLiveId();

        } catch (Exception e) {
            throw wrapNuxeoException(e);
        }
    }


    /**
     * Get the query filter.
     *
     * @param path path to fetch
     * @return applie live filters to the query
     * @throws Exception the exception
     */
    public NuxeoQueryFilterContext getQueryFilterContextForPath(String path) {
        NuxeoQueryFilterContext queryCtx = new NuxeoQueryFilterContext();

        try {
            // CMS service
            ICMSService cmsService = getCMSService();
            // CMS context
            CMSServiceCtx cmsContext = getCMSCtx();
            setForcePublicationInfosScope("superuser_context");

            // Publication infos
            CMSPublicationInfos pubInfos = cmsService.getPublicationInfos(cmsContext, path);

            if (pubInfos.isLiveSpace()) {
                queryCtx.setState(NuxeoQueryFilterContext.STATE_LIVE);
            }

            return queryCtx;
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
     */
    public static String getParentPath(String path) {
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

    public static String getLivePath(String path) {
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


            return getCMSService().getBinaryContent(getCMSCtx(), "attachedPicture", docPath, pictureIndex);

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


            return getCMSService().getBinaryContent(getCMSCtx(), "picture", docPath, content);

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
            return getCMSService().getBinaryContent(getCMSCtx(), "file", docPath, fieldName);
        } catch (Exception e) {
            throw wrapNuxeoException(e);
        }
    }


    /**
     * Create URL from webId.
     *
     * @param webId webId
     * @param parameters request parameters
     * @return URL
     * @throws CMSException
     */
    public String createUrlFromWebId(String webId, Map<String, String> parameters) throws CMSException {
        // CMS service
        ICMSService cmsService = getCMSService();
        // WebId service
        IWebIdService webIdService = getWebIdService();
        // Nuxeo service
        INuxeoService nuxeoService = getNuxeoCMSService();
        // CMS customizer
        INuxeoCustomizer cmsCustomizer = nuxeoService.getCMSCustomizer();
        // Portal URL factory
        IPortalUrlFactory portalUrlFactory = getPortalUrlFactory();

        // Portal controller context
        PortalControllerContext portalControllerContext = getPortalCtx();

        // CMS context
        CMSServiceCtx cmsContext = new CMSServiceCtx();
        cmsContext.setPortalControllerContext(portalControllerContext);

        // WebId path
        String webIdPath = webIdService.webIdToFetchPath(webId);

        // Fetch document
        CMSItem content = cmsService.getContent(cmsContext, webIdPath);
        cmsContext.setDoc(content.getNativeItem());

        // Path
        String path = cmsCustomizer.getContentWebIdPath(cmsContext);

        // URL
        String url = portalUrlFactory.getCMSUrl(portalControllerContext, null, path, null, null, null, null, null, null, null);

        try {
            // Original URI
            URI originalUri = new URI(url);

            // Query
            String query = originalUri.getQuery();
            boolean firstParameter = true;
            StringBuilder builder = new StringBuilder();
            if (query != null) {
                builder.append(query);
                firstParameter = false;
            }
            if (parameters != null) {
                for (Entry<String, String> parameter : parameters.entrySet()) {
                    if (firstParameter) {
                        firstParameter = false;
                    } else {
                        builder.append("&");
                    }

                    builder.append(URLEncoder.encode(parameter.getKey(), CharEncoding.UTF_8));
                    builder.append("=");
                    builder.append(URLEncoder.encode(parameter.getValue(), CharEncoding.UTF_8));
                }
            }
            query = builder.toString();

            // Adapted URI
            URI adaptedUri = new URI(originalUri.getScheme(), originalUri.getAuthority(), originalUri.getPath(), query, originalUri.getFragment());

            return adaptedUri.toString();
        } catch (Exception e) {
            return url;
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
        try {
            // Parameterized permalinks indicator
            Boolean permalinks = (Boolean) request.getAttribute(InternalConstants.PARAMETERIZED_PERMALINKS_ATTRIBUTE);

            if ((getRequest().getUserPrincipal() == null) || BooleanUtils.isTrue(permalinks)) {
                // Serve anonymous resource by servlet
                StringBuilder url = new StringBuilder();
                if (BooleanUtils.isTrue(permalinks)) {
                    url.append(request.getScheme());
                    url.append("://");
                    url.append(request.getServerName());
                    url.append(":");
                    url.append(request.getServerPort());
                }

                url.append(getRequest().getContextPath());
                url.append("/sitepicture?path=");
                url.append(URLEncoder.encode(webid, "UTF-8"));

                if (content != null) {
                    url.append("&content=");
                    url.append(content);
                }

                return url.toString();
            }

            ResourceURL resourceURL = createResourceURL();

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

        } catch (Exception e) {
            throw wrapNuxeoException(e);
        }
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

        try {

            cmsCtx = new CMSServiceCtx();

            if (getRequest() != null) {

                cmsCtx.setRequest(getRequest());
                cmsCtx.setControllerContext(ControllerContextAdapter.getControllerContext(new PortalControllerContext(getPortletCtx(), getRequest(), getResponse())));
            }

            if (getServletRequest() != null) {
                cmsCtx.setServletRequest(servletRequest);
            } else {
                if (getRequest() != null) {
                    cmsCtx.setServletRequest((HttpServletRequest) getRequest().getAttribute(Constants.PORTLET_ATTR_HTTP_REQUEST));
                }
            }

            cmsCtx.setPortletCtx(getPortletCtx());

            if (response instanceof MimeResponse) {
                cmsCtx.setResponse((MimeResponse) response);
            }
            cmsCtx.setScope(getScope());
            cmsCtx.setForcePublicationInfosScope(getForcePublicationInfosScope());
            cmsCtx.setDisplayLiveVersion(getDisplayLiveVersion());


            cmsCtx.setForcedLivePath(getForcedLivePath());

            if (getRequest() != null) {
                cmsCtx.setPageId(getPageId());
            }
            cmsCtx.setDoc(getCurrentDoc());
            cmsCtx.setHideMetaDatas(getHideMetaDatas());
            cmsCtx.setDisplayContext(displayContext);

            cmsCtx.setCreationType(docTypeToCreate);
            if (parentPathToCreate != null) {
                cmsCtx.setCreationPath(getComputedPath(parentPathToCreate));
            }

            if (reloadResource) {
                cmsCtx.setForceReload(true);
            } else {
                // servlet ressource
                if (getServletRequest() != null) {
                    String refresh = getServletRequest().getParameter("refresh");
                    if (BooleanUtils.toBoolean(refresh)) {
                        cmsCtx.setForceReload(true);
                    }
                }

            }
            cmsCtx.setStreamingSupport(streamingSupport);


            return cmsCtx;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Gets the debug infos.
     *
     * @return the debug infos
     * @deprecated unused ?
     */
    @Deprecated
	public String getDebugInfos() {
        String output = "";

        if ("1".equals(System.getProperty("nuxeo.debugHtml"))) {

            output += "<p class=\"nuxeo-debug\" align=\"center\">";
            output += "scope : " + getScope();
            output += "</p>";

            return output;
        } else {

            output += "<!--";
            output += "scope : " + getScope();
            output += "-->";
        }

        return output;

    }

    /**
     * Get the user avatar (acceded from portlets).
     *
     * @param username
     * @return a link to the avatar
     * @throws CMSException
     */
    public Link getUserAvatar(String username) throws CMSException {
        return getCMSService().getUserAvatar(getCMSCtx(), username);

    }

    /**
     * Refresh the user avatar (acceded from portlets).
     *
     * @param username
     * @return a timestamp
     * @throws CMSException
     */
    public String refreshUserAvatar(String username) {
        return getCMSService().refreshUserAvatar(getCMSCtx(), username);
    }

    /**
     * Get a person
     *
     * @param uid of the person
     * @return the person
     * @deprecated use the PortalGenericPortlet
     */
    @Deprecated
	public DirectoryPerson getPerson(String uid) {
        IDirectoryService service = getDirectoryService();
        DirectoryPerson p = null;
        if (service != null) {
            p = service.getPerson(uid);
        }

        return p;
    }

    /**
     * Instantiates a new nuxeo controller.
     *
     * @param request the request
     * @param response the response
     * @param portletCtx the portlet ctx
     * @throws PortletException
     * @throws RuntimeException the runtime exception
     */
    public static NuxeoDocumentContext getDocumentContext(PortletRequest request, PortletResponse response, PortletContext portletCtx) throws PortletException{
    	return getDocumentContext(request, response, portletCtx, null);
    }

    /**
     * Instantiates a new nuxeo controller.
     *
     * @param request the request
     * @param response the response
     * @param portletCtx the portlet ctx
     * @param path a specific path
     * @throws PortletException
     * @throws RuntimeException the runtime exception
     */
    public static NuxeoDocumentContext getDocumentContext(PortletRequest request, PortletResponse response, PortletContext portletCtx, String path) throws PortletException{

    	NuxeoDocumentContext docContext = new NuxeoDocumentContext();

        NuxeoController nxCtl = new NuxeoController(request, response, portletCtx);

        PortalWindow window = WindowFactory.getWindow(request);


        BasicPublicationInfos navigationInfos = docContext.getPublicationInfos(BasicPublicationInfos.class);
		navigationInfos.setBasePath(window.getPageProperty(BasicPublicationInfos.BASE_PATH));

        navigationInfos.setNavigationPath(request.getParameter(BasicPublicationInfos.NAVIGATION_PATH));

        if(path == null) {
        	navigationInfos.setContentPath(request.getParameter(BasicPublicationInfos.CONTENT_PATH));
        }
        else {
        	navigationInfos.setContentPath(path);
        }


        if (request instanceof ResourceRequest) {
            if (request.getParameter(BasicPublicationInfos.RELOAD_RESOURCE) != null) {

                navigationInfos.setReloadResource(Boolean.TRUE);
            }
        }

        if(navigationInfos.getContentPath() != null) {
			CMSServiceCtx cmsCtx = nxCtl.getCMSCtx();

			ICMSService cmsService = getCMSService();

			try {
				addInfos(cmsCtx, navigationInfos.getContentPath(), docContext);


				String contextualizationBasePath = cmsCtx.getContextualizationBasePath();
				if(contextualizationBasePath != null) {
					List<CMSItem> portalNavigationSubitems = cmsService.getPortalNavigationSubitems(cmsCtx, contextualizationBasePath, getLivePath(navigationInfos.getContentPath()));

					if(portalNavigationSubitems.size() > 0) {
						navigationInfos.setHasSubItems(Boolean.TRUE);
					}
				}




			} catch (CMSException e) {
				throw new PortletException(e);
			}

            // Preview mode
            if (request != null) {

                EditionState editionState = (EditionState) request.getAttribute("osivia.editionState");
                if ((editionState != null) && EditionState.CONTRIBUTION_MODE_EDITION.equals(editionState.getContributionMode())) {
                    navigationInfos.setForcedLivePath(editionState.getDocPath());
                } else {

                    // mode web page
                    String webPageEditionPath = (String) request.getAttribute("osivia.cms.webPageEditionPath");
                    if (webPageEditionPath != null) {
                    	navigationInfos.setForcedLivePath(editionState.getDocPath());
                    }
                }


            }


			return docContext;
        } else {
            return null;
        }
    }


    /**
     * Instantiates a new nuxeo controller.
     *
     * @param request the request
     * @param response the response
     * @param portletCtx the portlet ctx
     * @throws PortletException
     * @throws RuntimeException the runtime exception
     */
    public static NuxeoDocumentContext getDocumentContext(CMSServiceCtx cmsCtx, String path) throws PortletException{

    	NuxeoDocumentContext docContext = null;

    	if(path != null) {

    		docContext = new NuxeoDocumentContext();

			try {
				addInfos(cmsCtx, path, docContext);

			} catch (CMSException e) {
				throw new PortletException(e);
			}
    	}

		return docContext;
    }


    /**
     * Instantiates a new nuxeo controller.
     *
     * @param request the request
     * @param response the response
     * @param portletCtx the portlet ctx
     * @throws PortletException
     * @throws RuntimeException the runtime exception
     */
    public static NuxeoDocumentContext getDocumentContext(PortalControllerContext portalCtx, String path) throws PortletException {

        // CMS context
        CMSServiceCtx cmsContext = new CMSServiceCtx();
        cmsContext.setPortalControllerContext(portalCtx);

        return getDocumentContext(cmsContext, path);
    }


    /**
     *
     * @param cmsCtx
     * @param path
     * @param docContext
     * @throws CMSException
     */
    private static void addInfos(CMSServiceCtx cmsCtx, String path, NuxeoDocumentContext docContext) throws CMSException {

        BasicPublicationInfos publicationInfos = docContext.getPublicationInfos(BasicPublicationInfos.class);
		BasicPermissions perms = docContext.getPermissions(BasicPermissions.class);


		ICMSService cmsService = getCMSService();

        // Nuxeo service
        INuxeoService nuxeoService = Locator.findMBean(INuxeoService.class, INuxeoService.MBEAN_NAME);
        // CMS customizer
        INuxeoCustomizer cmsCustomizer = nuxeoService.getCMSCustomizer();


        CMSPublicationInfos pub = cmsService.getPublicationInfos(cmsCtx, path);


		perms.setAnonymouslyReadable(pub.isAnonymouslyReadable());
		perms.setDeletableByUser(pub.isDeletableByUser());
		perms.setEditableByUser(pub.isEditableByUser());
		perms.setManageableByUser(pub.isManageableByUser());

		DocumentState version = DocumentState.parse(cmsCtx.getDisplayLiveVersion());
		publicationInfos.setContentPath(path);
		publicationInfos.setState(version);
		publicationInfos.setLiveId(pub.getLiveId());
		publicationInfos.setLiveSpace(pub.isLiveSpace());
		publicationInfos.setBasePath(pub.getPublishSpacePath());
		publicationInfos.setScope(cmsCtx.getScope());
		publicationInfos.setDisplayContext(cmsCtx.getDisplayContext());

		if(cmsCtx.getContextualizationBasePath()!=null) {
			publicationInfos.setContextualized(Boolean.TRUE);
		}

		CMSItem content = cmsService.getContent(cmsCtx, publicationInfos.getContentPath());
		Document nativeItem = (Document) content.getNativeItem();

		docContext.setDoc(nativeItem);


		DocumentType documentType = cmsCustomizer.getCMSItemTypes().get(nativeItem.getType());
		docContext.setDocumentType(documentType);



    }


    /**
     * Gére les folders 'hiddenInNavigation'.
     * Les fils d'un folder 'hiddenInNavigation' sont directement rattachés au parent.
     *
     * @param ctx CMS context
     * @param ordered ordered indicator
     * @return Nuxeo request
     * @throws CMSException
     */
    public static String createFolderRequest(DocumentContext<Document> docCtx, boolean ordered)  {
        String nuxeoRequest = null;

        Document doc = docCtx.getDoc();
        BasicPublicationInfos navigationInfos = docCtx.getPublicationInfos(BasicPublicationInfos.class);

        //CMSPublicationInfos pubInfos = this.cmsService.getPublicationInfos(ctx, doc.getPath());
//
//        List<CMSItem> navItems = null;

        if (navigationInfos.isContextualized()) {
            // Publication dans un environnement contextualisé
            // On se sert du menu de navigation et on décompose chaque niveau
//            navItems = this.cmsService.getPortalNavigationSubitems(ctx, ctx.getContextualizationBasePath(),
//                    DocumentPublishSpaceNavigationCommand.computeNavPath(doc.getPath()));
        }

        if (navigationInfos.isContextualized() && navigationInfos.isHasSubItems()) {
            // On exclut les folderish, car ils sont présentés dans le menu en mode contextualisé
            nuxeoRequest = "ecm:parentId = '" + navigationInfos.getLiveId() + "' AND ecm:mixinType != 'Folderish'";
            if (ordered) {
                nuxeoRequest += " order by ecm:pos";
            } else {
                nuxeoRequest += " order by dc:modified desc";
            }
        } else {
            nuxeoRequest = "ecm:path STARTSWITH '" + getLivePath(doc.getPath())
                    + "' AND ecm:mixinType != 'Folderish' ";

            if (ordered) {
                nuxeoRequest += " order by ecm:pos";
            } else {
                nuxeoRequest += " order by dc:modified desc";
            }
        }

        return nuxeoRequest;
    }

    /**
     * Convert webId to fetch publication infos path.
     *
     * @param webId webId
     * @return fetch publication infos path (e.g. webId:example)
     */
    public static String webIdToFetchPath(String webId) {
        String fetchPath;
        if (webId != null) {
            fetchPath = FETCH_PATH_PREFIX.concat(webId);
        } else {
            fetchPath = null;
        }
        return fetchPath;
    }


    /**
     * Convert CMS path to fetch publication infos path.
     *
     * @param cmsPath CMS path (e.g. /_id/example)
     * @return fetch publication infos path (e.g. webId:example)
     */
    public static String cmsPathToFetchPath(String cmsPath) {
        String webId = StringUtils.substringAfterLast(cmsPath, SLASH);
        webId = StringUtils.substringBefore(webId, DOT);
        return webIdToFetchPath(webId);
    }


    /**
     * Convert webId to CMS path.
     *
     * @param webId webId
     * @return CMS path (e.g. /_id/example)
     */
    public static String webIdToCmsPath(String webId) {
        StringBuilder path = new StringBuilder();
        path.append(CMS_PATH_PREFIX);
        path.append(SLASH);
        path.append(webId);
        return path.toString();
    }

}
