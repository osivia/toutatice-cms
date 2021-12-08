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

import fr.toutatice.portail.cms.nuxeo.api.cms.NuxeoDocumentContext;
import fr.toutatice.portail.cms.nuxeo.api.cms.NuxeoPublicationInfos;
import fr.toutatice.portail.cms.nuxeo.api.services.*;


import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.PortalObjectPath;
import org.jboss.portal.core.model.portal.Window;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.cache.services.CacheInfo;
import org.osivia.portal.api.cms.CMSContext;
import org.osivia.portal.api.cms.CMSController;
import org.osivia.portal.api.cms.DocumentType;
import org.osivia.portal.api.cms.UniversalID;
import org.osivia.portal.api.cms.service.CMSService;
import org.osivia.portal.api.cms.service.CMSSession;
import org.osivia.portal.api.cms.service.SpaceCacheBean;
import org.osivia.portal.api.cms.service.UpdateInformations;
import org.osivia.portal.api.cms.service.UpdateScope;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.contribution.IContributionService.EditionState;
import org.osivia.portal.api.directory.IDirectoryService;
import org.osivia.portal.api.directory.IDirectoryServiceLocator;
import org.osivia.portal.api.directory.entity.DirectoryPerson;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.taskbar.ITaskbarService;
import org.osivia.portal.api.taskbar.TaskbarTask;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.api.urls.Link;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;
import org.osivia.portal.core.cms.*;
import org.osivia.portal.core.cms.spi.NuxeoRepository;
import org.osivia.portal.core.cms.spi.NuxeoRequest;
import org.osivia.portal.core.cms.spi.NuxeoResult;
import org.osivia.portal.core.constants.InternalConstants;
import org.osivia.portal.core.formatters.IFormatter;
import org.osivia.portal.core.page.PageProperties;
import org.osivia.portal.core.profils.IProfilManager;
import org.osivia.portal.core.profils.ProfilBean;
import org.osivia.portal.core.web.IWebIdService;

import javax.portlet.*;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URLEncoder;
import java.util.*;
import java.util.Map.Entry;


/**
 * Nuxeo controller.
 * <p>
 * Main entry point for nuxeo calls / data manipulation from within a cms portlet
 */
public class NuxeoController {

    public static final String NUXEO_REPOSITORY_NAME = "nx";
    /**
     * Slash separator.
     */
    private static final String SLASH = "/";
    /**
     * Dot separator.
     */
    private static final String DOT = ".";
    /**
     * Prefix used to query document in the ECM.
     */
    private static final String FETCH_PATH_PREFIX = "webId:";
    /**
     * Prefix for CMS path.
     */
    private static final String CMS_PATH_PREFIX = "/_id";
    /**
     * The cms service locator.
     */
    private static ICMSServiceLocator cmsServiceLocator;
    /**
     * Taskbar service.
     */
    private final ITaskbarService taskbarService;
    /**
     * The asynchronous updates.
     */
    public boolean asynchronousUpdates = false;
    /**
     * The request.
     */
    PortletRequest request;
    /**
     * The response.
     */
    PortletResponse response;
    /**
     * The portlet ctx.
     */
    PortletContext portletCtx;
    /**
     * The url factory.
     */
    IPortalUrlFactory urlFactory;
    /**
     * The nuxeo cms service.
     */
    INuxeoService nuxeoCMSService;
    /**
     * The page id.
     */
    String pageId;
    /**
     * The nuxeo base uri.
     */
    URI nuxeoBaseURI;
    /**
     * The nuxeo connection.
     */
    NuxeoConnectionProperties nuxeoConnection;
    
    /**
     * The formatter.
     */
    IFormatter formatter;
    
    /**
     * The profil manager.
     */
    IProfilManager profilManager;

    /**
     * The scope.
     */
    String scope;
    /**
     * The display live version.
     */
    String displayLiveVersion;
    /**
     * The base path.
     */
    String basePath;
    /**
     * The navigation path.
     */
    String navigationPath;
    /**
     * The item navigation path.
     */
    String itemNavigationPath;
    /**
     * The doc type to create.
     */
    String docTypeToCreate;
    /**
     * The parent path to create.
     */
    String parentPathToCreate;
    /**
     * The domain path.
     */
    String domainPath;
    HttpServletRequest servletRequest;
    /**
     * The content path.
     */
    String contentPath;
    /**
     * The space path.
     */
    String spacePath;
    /**
     * The force publication infos scope.
     */
    String forcePublicationInfosScope;
    /**
     * The menu root path.
     */
    String menuRootPath;
    /**
     * The hide meta datas.
     */
    String hideMetaDatas;
    /**
     * The display context.
     */
    String displayContext;
    /**
     * The navigation scope.
     */
    String navigationScope = null;
    /**
     * The nav item.
     */
    CMSItem navItem;
    /**
     * The cms ctx.
     */
    CMSServiceCtx cmsCtx;
    boolean reloadResource = false;
    boolean streamingSupport = false;
    String forcedLivePath = null;
    /**
     * The current doc.
     */
    Document currentDoc;
    /**
     * The portal ctx.
     */
    PortalControllerContext portalCtx;
    IWebIdService webIdService;
    /**
     * The page marker.
     */
    String pageMarker;
    /**
     * The auth type.
     */
    int authType = NuxeoCommandContext.AUTH_TYPE_USER;
    /**
     * The cache type.
     */
    int cacheType = CacheInfo.CACHE_SCOPE_NONE;
    /**
     * The nuxeo command service.
     */
    INuxeoCommandService nuxeoCommandService;
    

    
    /** The cms context name. */
    private static String cmsContextName = null;
    
    /**
     * Directory service
     */
    private IDirectoryServiceLocator directoryServiceLocator;
    private IDirectoryService directoryService;
    /**
     * Satellite.
     */
    private Satellite satellite;
    /**
     * Asynchronous command execution indicator.
     */
    private boolean asynchronousCommand;
    /**
     * The scope profil.
     */
    private ProfilBean scopeProfil = null;
    /**
     * The cache time out.
     */
    private long cacheTimeOut = -1;

    /**
     * Instantiates a new nuxeo controller.
     *
     * @param request    the request
     * @param response   the response
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


            
            String spaceId = window.getPageProperty("osivia.spaceId");
            if( spaceId != null)
            try {
                UniversalID id = new UniversalID(spaceId);
                if( id.getRepositoryName().equals(NUXEO_REPOSITORY_NAME)){
                    NuxeoRepository repository =  (NuxeoRepository) (Locator.getService(CMSService.class).getUserRepository(getCMSContext(), id.getRepositoryName()));
                    this.basePath = repository.getPath(id.getInternalID());
                    this.spacePath = repository.getPath(id.getInternalID());
                }
           } catch (Exception e) {
               throw this.wrapNuxeoException(e);
           }  
            

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


            String displayLiveVersionAttr = (String) request.getAttribute(Constants.REQUEST_ATTR_VERSION);
            if (displayLiveVersionAttr != null) {
                displayLiveVersion = displayLiveVersionAttr;
            }


            this.setScope(scope);
            this.setDisplayLiveVersion(displayLiveVersion);

            // Hide metadata indicator
            this.setHideMetaDatas(window.getProperty(InternalConstants.METADATA_WINDOW_PROPERTY));

            this.setDocTypeToCreate(window.getProperty("osivia.createDocType"));
            String parentPathToCreate = window.getProperty("osivia.createParentPath");
            this.setParentPathToCreate(parentPathToCreate);


            this.setPageMarker((String) request.getAttribute("osivia.pageMarker"));


            /* computes root path */


            if (this.basePath != null) {
                String[] parts = this.basePath.split("/");
                if ((parts != null) && (parts.length > 0)) {
                    this.domainPath = "/" + parts[1];
                }
            }

            String navigationId = window.getPageProperty("osivia.navigationId");
            if( navigationId != null)
            try {
                UniversalID id = new UniversalID(navigationId);
                if( id.getRepositoryName().equals(NUXEO_REPOSITORY_NAME)){
                    NuxeoRepository repository =  (NuxeoRepository) (Locator.getService(CMSService.class).getUserRepository(getCMSContext(), id.getRepositoryName()));
                    this.navigationPath = repository.getPath(id.getInternalID());
                    this.itemNavigationPath = this.navigationPath;
                }
           } catch (Exception e) {
               throw this.wrapNuxeoException(e);
           }  


            String contentId = window.getPageProperty("osivia.contentId");
            if( contentId != null)
            try {
                UniversalID id = new UniversalID(contentId);
                if( id.getRepositoryName().equals(NUXEO_REPOSITORY_NAME)){
                    NuxeoRepository repository =  (NuxeoRepository) (Locator.getService(CMSService.class).getUserRepository(getCMSContext(), id.getRepositoryName()));
                    this.contentPath = repository.getPath(id.getInternalID());
                }
           } catch (Exception e) {
               throw this.wrapNuxeoException(e);
           }  


            if (request instanceof ResourceRequest) {
                if (request.getParameter("refresh") != null) {
                    this.reloadResource = true;
                }
            }


            // Preview mode
            if (this.getRequest() != null) {

                EditionState editionState = (EditionState) this.getRequest().getAttribute("osivia.editionState");
                if ((editionState != null) && EditionState.CONTRIBUTION_MODE_EDITION.equals(editionState.getContributionMode())) {
                    this.forcedLivePath = editionState.getDocPath();
                } else {


                    // mode web page
                    String webPageEditionPath = (String) this.getRequest().getAttribute("osivia.cms.webPageEditionPath");
                    if (webPageEditionPath != null) {
                        this.forcedLivePath = editionState.getDocPath();
                    }
                }
            }


            // Satellite
            String satelliteName = window.getProperty("osivia.satellite");
            if (StringUtils.isNotEmpty(satelliteName)) {
                Set<Satellite> satellites = getCMSService().getSatellites();
                if (CollectionUtils.isNotEmpty(satellites)) {
                    Satellite satellite = null;
                    Iterator<Satellite> iterator = satellites.iterator();
                    while ((satellite == null) && iterator.hasNext()) {
                        Satellite next = iterator.next();
                        if (StringUtils.equals(satelliteName, next.getId())) {
                            satellite = next;
                        }
                    }
                    this.satellite = satellite;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        // Taskbar service
        this.taskbarService = Locator.findMBean(ITaskbarService.class, ITaskbarService.MBEAN_NAME);
    }

    /**
     * Constructor.
     *
     * @param portalControllerContext portal controller context
     */
    public NuxeoController(PortalControllerContext portalControllerContext) {
        this(portalControllerContext.getRequest(), portalControllerContext.getResponse(), portalControllerContext.getPortletCtx());
    }


    /**
     * Instantiates a new nuxeo controller.
     *
     * @param portletCtx the portlet ctx
     */
    public NuxeoController(PortletContext portletCtx) {
        super();
        this.portletCtx = portletCtx;

        // Taskbar service
        this.taskbarService = Locator.findMBean(ITaskbarService.class, ITaskbarService.MBEAN_NAME);
    }


    /**
     * Computes live path for current document
     * (may differs from original path in case of a proxy).
     *
     * @param path the path
     * @return the live path
     */

    public static String getLivePath(String path) {
        return StringUtils.removeEnd(path, ".proxy");
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
    
    /**
     * Update notification.
     *
     * @param cmsContext the cms context
     * @param path the path (or webId)
     * @return the document
     * @throws Exception the exception
     */
    public void notifyUpdate(String documentPath, String spacePath, UpdateScope scope, boolean async) throws PortletException {

        boolean superUser;
        
        if( "superuser_no_cache".equals(getScope()))
            superUser = true;
        else if( "superuser_context".equals(getScope()))
            superUser = true;
        else
            superUser= false;
        
        CMSController ctrl = new CMSController(getPortalCtx(), superUser);

        try {
            if (spacePath == null) {
                spacePath = getSpacePath();
            }
            if (spacePath != null) {
                UniversalID internalID, spaceID;

                if (documentPath != null) {
                        // Get content ID
                        NuxeoRepository nuxeoRepository = (NuxeoRepository) (Locator.getService(org.osivia.portal.api.cms.service.CMSService.class)
                                .getUserRepository(ctrl.getCMSContext(), "nx"));
                        internalID = new UniversalID("nx", nuxeoRepository.getInternalId(documentPath));
                } else
                    internalID = null;


               // Get content ID
                NuxeoRepository nuxeoRepository = (NuxeoRepository) (Locator.getService(org.osivia.portal.api.cms.service.CMSService.class)
                        .getUserRepository(ctrl.getCMSContext(), "nx"));
                spaceID = new UniversalID("nx", nuxeoRepository.getInternalId(spacePath));
                

                // Notify update
                CMSSession session = Locator.getService(org.osivia.portal.api.cms.service.CMSService.class).getCMSSession(ctrl.getCMSContext());
                UpdateInformations infos = new UpdateInformations(internalID, spaceID, scope, async);

                session.notifyUpdate(infos);
                
                // Further calls
                PageProperties.getProperties().setCheckingSpaceTS(System.currentTimeMillis());


                
                
            }
        } catch (Exception e) {
            throw new PortletException(e);
        }

    }
    
    public String getSpacePath(String documentPath) throws PortletException {
        
        CMSController ctrl = new CMSController(getPortalCtx());

        String spacePath;
        try {
            CMSSession session = Locator.getService(org.osivia.portal.api.cms.service.CMSService.class).getCMSSession(ctrl.getCMSContext());
            
            
            // Get content ID
            NuxeoRepository nuxeoRepository = (NuxeoRepository) (Locator.getService(org.osivia.portal.api.cms.service.CMSService.class)
                    .getUserRepository(ctrl.getCMSContext(), "nx"));
            UniversalID internalID = new UniversalID(NuxeoController.NUXEO_REPOSITORY_NAME, nuxeoRepository.getInternalId(documentPath));
            
            org.osivia.portal.api.cms.model.Document doc = session.getDocument(internalID);
            
            org.osivia.portal.api.cms.model.Document space = session.getDocument(doc.getSpaceId());
            
            spacePath = ((Document)space.getNativeItem()).getPath();
            
            
            
            return spacePath;
        } catch (Exception e) {
           throw new PortletException(e);
        }
    }

    
    /**
     * Gets the universal ID from path.
     *
     * @param path the path
     * @return the universal ID from path
     */
    public UniversalID getUniversalIDFromPath(String path) {
        try {
            return getCMSService().getUniversalIDFromPath(getCMSCtx(), path);
        } catch (Exception e) {
            throw this.wrapNuxeoException(e);
        }
    }

    /**
     * Gére les folders 'hiddenInNavigation'.
     * Les fils d'un folder 'hiddenInNavigation' sont directement rattachés au parent.
     *
     * @param documentContext document context
     * @param ordered         ordered indicator
     * @return Nuxeo request
     * @throws CMSException
     */
    public static String createFolderRequest(NuxeoDocumentContext documentContext, boolean ordered) {
        String nuxeoRequest = null;

        Document document = documentContext.getDocument();
        NuxeoPublicationInfos publicationInfos = documentContext.getPublicationInfos();

        if (documentContext.isContextualized()) {
            // On exclut les folderish, car ils sont présentés dans le menu en mode contextualisé
            nuxeoRequest = "ecm:parentId = '" + publicationInfos.getLiveId() + "' AND ecm:mixinType != 'Folderish'";
            if (ordered) {
                nuxeoRequest += " order by ecm:pos";
            } else {
                nuxeoRequest += " order by dc:modified desc";
            }
        } else {
            nuxeoRequest = "ecm:path STARTSWITH '" + getLivePath(document.getPath()) + "' AND ecm:mixinType != 'Folderish' ";

            if (ordered) {
                nuxeoRequest += " order by ecm:pos";
            } else {
                nuxeoRequest += " order by dc:modified desc";
            }
        }

        return nuxeoRequest;
    }

    public HttpServletRequest getServletRequest() {
        return this.servletRequest;
    }

    public void setServletRequest(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }

    private IDirectoryService getDirectoryService() {
        if (this.directoryService == null) {

            this.directoryServiceLocator = Locator.findMBean(IDirectoryServiceLocator.class, IDirectoryServiceLocator.MBEAN_NAME);
            this.directoryService = this.directoryServiceLocator.getDirectoryService();
        }
        return this.directoryService;
    }

    /**
     * Gets the domain path.
     *
     * @return the domain path
     */
    public String getDomainPath() {
        return this.domainPath;
    }

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

    /**
     * Gets the menu root path.
     *
     * @return the menu root path
     */
    public String getMenuRootPath() {
        return this.menuRootPath;
    }

    public String getForcedLivePath() {
        return this.forcedLivePath;
    }

    public boolean isStreamingSupport() {
        return this.streamingSupport;
    }

    public void setStreamingSupport(boolean streamingSupport) {
        this.streamingSupport = streamingSupport;
    }

    /**
     * WebId service used to transform urls
     *
     * @return the service
     */
    public IWebIdService getWebIdService() {
        if (this.webIdService == null) {
            this.webIdService = (IWebIdService) this.getPortalCtx().getPortletCtx().getAttribute("webIdService");
        }

        return this.webIdService;
    }

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

        if (currentDoc != null) {

        }
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

    /**
     * Set the current scope for furthers nuxeo requests
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
     * Gets the portal ctx.
     *
     * @return the portal ctx
     */
    public PortalControllerContext getPortalCtx() {

        if (this.portalCtx == null) {
            if( this.request != null)
                this.portalCtx = new PortalControllerContext(this.getPortletCtx(), this.request, this.response);
            else if( getServletRequest() != null) {
                this.portalCtx = new PortalControllerContext(getServletRequest());
            } else
                this.portalCtx = new PortalControllerContext(this.getPortletCtx());
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

    public NuxeoException wrapNuxeoException(Exception e) {

        if (e instanceof CMSException) {
            CMSException cmsExc = (CMSException) e;

            if (cmsExc.getErrorCode() == CMSException.ERROR_NOTFOUND) {
                return new NuxeoException(NuxeoException.ERROR_NOTFOUND);
            }
            if (cmsExc.getErrorCode() == CMSException.ERROR_FORBIDDEN) {
                return new NuxeoException(NuxeoException.ERROR_FORBIDDEN);
            }
            return new NuxeoException(NuxeoException.ERROR_UNAVAILAIBLE, cmsExc);
        } else if (e instanceof PortletException) {
            Throwable cause = e.getCause();
            if (cause != null && cause instanceof CMSException) {
                CMSException cmsException = (CMSException) cause;
                return this.wrapNuxeoException(cmsException);
            } else {
                return new NuxeoException(e);
            }
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
        this.docTypeToCreate = property;
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
            if (this.navItem == null) {
                if (this.getNavigationPath() != null) {
                    // Navigation context
                    CMSServiceCtx cmsReadNavContext = new CMSServiceCtx();
                    cmsReadNavContext.setPortalControllerContext(this.getPortalCtx());
                    cmsReadNavContext.setScope(this.getNavigationScope());

                    // TODO : factoriser dans NuxeoController

                    this.navItem = getCMSService().getPortalNavigationItem(cmsReadNavContext, this.getSpacePath(), this.getNavigationPath());
                }

            }

            return this.navItem;
        } catch (Exception e) {
            throw this.wrapNuxeoException(e);
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
     * Gets the portal url factory.
     *
     * @return the portal url factory
     */
    public IPortalUrlFactory getPortalUrlFactory() {
        if (this.urlFactory == null) {
            this.urlFactory = (IPortalUrlFactory) Locator.getService(IPortalUrlFactory.class);
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
            throw this.wrapNuxeoException(e);
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
        /*
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
        */
        //TODO FIXME
        return null;
    }
    
    /**
     * Gets the computed path.
     *
     * @param portletPath the portlet path
     * @return the computed path
     */
    public String getComputedPath(String portletPath) {
        String computedPath;

        if (portletPath == null) {
            computedPath = "";
        } else {
            computedPath = portletPath;
            
            if( computedPath.contains(":")) {
                try {
                    UniversalID id = new UniversalID(portletPath);
                    NuxeoRepository repository =  (NuxeoRepository) (Locator.getService(CMSService.class).getUserRepository(getCMSContext(), id.getRepositoryName()));
                    computedPath = repository.getPath(id.getInternalID());
               } catch (Exception e) {
                   throw this.wrapNuxeoException(e);
               }  
            }

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

            if (computedPath.contains("${domainPath}")) {
                String path = this.getDomainPath();
                if (path == null) {
                    path = "";
                }

                computedPath = computedPath.replaceAll("\\$\\{domainPath\\}", path);
            }

            if (computedPath.contains("${taskPath}")) {
                // Window
                PortalWindow window = WindowFactory.getWindow(this.request);

                // Linked taskbar item identifier
                String taskId = window.getProperty(ITaskbarService.LINKED_TASK_ID_WINDOW_PROPERTY);

                // Task path
                String path;
                if (StringUtils.isEmpty(taskId)) {
                    path = StringUtils.EMPTY;
                } else {
                    // Linked task
                    TaskbarTask linkedTask = null;

                    try {
                        // Tasks
                        List<TaskbarTask> tasks = this.taskbarService.getTasks(this.portalCtx, this.spacePath, true);
                        for (TaskbarTask task : tasks) {
                            if (taskId.equals(task.getId())) {
                                linkedTask = task;
                                break;
                            }
                        }
                    } catch (PortalException e) {
                        // Do nothing
                    }

                    if (linkedTask == null) {
                        path = StringUtils.EMPTY;
                    } else {
                        path = linkedTask.getPath();
                    }
                }

                computedPath = computedPath.replaceAll("\\$\\{taskPath\\}", path);
            }

            if (computedPath.contains("${userWorkspacePath}")) {
                // User workspaces
                List<CMSItem> userWorkspaces;
                try {
                    userWorkspaces = getCMSService().getWorkspaces(this.getCMSCtx(), true, false);
                } catch (CMSException e) {
                    userWorkspaces = null;
                }

                // User workspace
                CMSItem userWorkspace;
                if (CollectionUtils.isNotEmpty(userWorkspaces) && (userWorkspaces.size() == 1)) {
                    userWorkspace = userWorkspaces.get(0);
                } else {
                    userWorkspace = null;
                }

                if (userWorkspace != null) {
                    computedPath = computedPath.replaceAll("\\$\\{userWorkspacePath\\}", userWorkspace.getCmsPath());
                }
            }

            if (StringUtils.isNotEmpty(computedPath) && !computedPath.startsWith("/")) {
                computedPath = webIdToFetchPath(computedPath);
            }
        }

        return computedPath;

    }
    
    /**
     * Gets the user workspace path.
     *
     * @return the user workspace path (null if not connected)
     */
    public String getUserWorkspacePath()    {

        
        String userWorkspacePath;
        
        try {

            CMSItem userWorkspace = getCMSService().getUserWorkspace(getCMSCtx());
            
            if( userWorkspace != null)
                userWorkspacePath = userWorkspace.getCmsPath();
            else
                userWorkspacePath = null;

        } catch (Exception e) {
            throw this.wrapNuxeoException(e);
        }
        
        return userWorkspacePath;
        
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
            throw this.wrapNuxeoException(e);
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
            INuxeoService nuxeoService = this.getNuxeoCMSService();
            return nuxeoService.getCMSCustomizer().transformLink(this.getCMSCtx(), link);
        } catch (Exception e) {
            throw this.wrapNuxeoException(e);
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
            throw this.wrapNuxeoException(e);
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
            throw this.wrapNuxeoException(e);
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
            throw this.wrapNuxeoException(e);
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
     * Checks if current path is in edition state.
     *
     * @param path the path
     * @return true, if is in page edition state
     * @throws CMSException the CMS exception
     * @deprecated use DocumentContext
     */
    @Deprecated
    public boolean isIdOrPathInLiveState(String originalPath) {

        if (this.isDisplayingLiveVersion()) {
            return true;
        }

        String path = "";

        try {
            // Path might be an ID
            if (originalPath.startsWith("/")) {
                path = originalPath;
            } else {
                CMSPublicationInfos pubInfos = getCMSService().getPublicationInfos(this.getCMSCtx(), originalPath);
                path = pubInfos.getDocumentPath();
            }


        } catch (CMSException e) {
            throw new RuntimeException(e);
        }


        return this.isPathInPageEditionState(path);
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
            return getCMSService().getBinaryResourceURL(this.getCMSCtx(), binary).getUrl();
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


        if (path.equals(this.getNavigationPath())) {
            // Uniquement en mode web page
            if (path.equals(this.getRequest().getAttribute("osivia.cms.webPageEditionPath"))) {
                return true;
            }
        }


        if (this.getRequest() != null) {
            EditionState editionState = (EditionState) this.getRequest().getAttribute("osivia.editionState");
            if ((editionState != null) && EditionState.CONTRIBUTION_MODE_EDITION.equals(editionState.getContributionMode())) {
                if (editionState.getDocPath().equals(path)) {
                    return true;
                }
            }
        }


        return false;
    }

    /**
     * Create file link URL from Nuxeo document path.
     *
     * @param path      Nuxeo document path
     * @param fieldName field name
     * @param fileName  file name
     * @return URL
     */
    public String createFileLink(String path, String fieldName, String fileName) {
        try {
            BinaryDescription binary = new BinaryDescription(BinaryDescription.Type.FILE, path);
            binary.setDocument(this.getCurrentDoc());
            binary.setFieldName(fieldName);
            binary.setFileName(fileName);
            return this.getBinaryURL(binary);
        } catch (Exception e) {
            throw this.wrapNuxeoException(e);
        }
    }

    /**
     * Creates the file link.
     *
     * @param doc       the doc
     * @param fieldName the field name
     * @return the string
     * @throws Exception the exception
     */
    public String createFileLink(Document doc, String fieldName) {
        try {
            BinaryDescription binary = new BinaryDescription(BinaryDescription.Type.FILE, doc.getPath());
            binary.setFieldName(fieldName);
            binary.setDocument(doc);
            return this.getBinaryURL(binary);
        } catch (Exception e) {
            throw this.wrapNuxeoException(e);
        }
    }

    /**
     * Creates the file link of a version.
     *
     * @param version
     * @param fieldName
     * @return file link of version
     */
    public String createFileLinkOfVersion(Document version, String fieldName) {
        try {
            BinaryDescription binary = new BinaryDescription(BinaryDescription.Type.FILE_OF_VERSION, version.getPath());
            binary.setFieldName(fieldName);
            binary.setDocument(version);
            return this.getBinaryURL(binary);
        } catch (Exception e) {
            throw this.wrapNuxeoException(e);
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
     * @param path      the path
     * @param fileIndex the file index
     * @return the string
     */
    public String createAttachedFileLink(String path, String fileIndex) {
        BinaryDescription binary = new BinaryDescription(BinaryDescription.Type.ATTACHED_FILE, path);
        binary.setDocument(this.getCurrentDoc());
        binary.setIndex(fileIndex);
        return this.getBinaryURL(binary);

    }

    /**
     * Creates the attached blob link.
     *
     * @param path      the path
     * @param blobIndex the blob index
     * @param fileName  file name
     * @return the string
     */
    public String createAttachedBlobLink(String path, String blobIndex, String fileName) {
        BinaryDescription binary = new BinaryDescription(BinaryDescription.Type.BLOB, path);
        if ((this.currentDoc != null) && (StringUtils.equals(path, this.currentDoc.getPath()))) {
            binary.setDocument(this.currentDoc);
        }
        binary.setIndex(blobIndex);
        binary.setFileName(fileName);
        return this.getBinaryURL(binary);

    }

    /**
     * Creates the attached picture link.
     *
     * @param path     the path
     * @param index    the file index
     * @param fileName file name
     * @return the string
     */
    public String createAttachedPictureLink(String path, String index, String fileName) {
        try {
            BinaryDescription binary = new BinaryDescription(BinaryDescription.Type.ATTACHED_PICTURE, path);
            binary.setDocument(this.getCurrentDoc());
            binary.setIndex(index);
            binary.setFileName(fileName);
            return this.getBinaryURL(binary);
        } catch (Exception e) {
            throw this.wrapNuxeoException(e);
        }
    }

    /**
     * Creates the picture link.
     *
     * @param path    the path
     * @param content the content
     * @return the string
     */
    public String createPictureLink(String path, String content) {
        BinaryDescription binary = new BinaryDescription(BinaryDescription.Type.PICTURE, path);
        binary.setContent(content);
        if ((this.currentDoc != null) && (StringUtils.equals(path, this.currentDoc.getPath()))) {
            binary.setDocument(this.currentDoc);
        }
        return this.getBinaryURL(binary);
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
            throw this.wrapNuxeoException(e);
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

        // Nuxeo command context
        NuxeoCommandContext commandContext;
        PortalControllerContext portalCtx;
        if (this.request != null) {
            portalCtx = new PortalControllerContext(this.portletCtx, this.request, this.response);
        } else if (this.servletRequest != null) {
            portalCtx = new PortalControllerContext(servletRequest);
        } else
            portalCtx = null;
        
        if( portalCtx != null)
            commandContext = new NuxeoCommandContext(this.portletCtx, portalCtx);
        else {
            commandContext = new NuxeoCommandContext(this.portletCtx);
        }

        commandContext.setAuthType(this.getAuthType());
        commandContext.setAuthProfil(this.getScopeProfil());
        commandContext.setCacheTimeOut(this.cacheTimeOut);
        commandContext.setCacheType(this.cacheType);
        commandContext.setAsynchronousUpdates(this.asynchronousUpdates);
        commandContext.setAsynchronousCommand(this.asynchronousCommand);
        commandContext.setSatellite(this.satellite);


        
        try {
            CMSContext cmsContext = getCMSContext();
            if(   commandContext.getAuthType()==NuxeoCommandContext.AUTH_TYPE_SUPERUSER)
                cmsContext.setSuperUserMode(true);
                
                
            
             CMSSession cmsSession =  Locator.getService(CMSService.class).getCMSSession(cmsContext);
             return ((NuxeoResult) cmsSession.executeRequest(new NuxeoRequest(NUXEO_REPOSITORY_NAME,commandContext, command))).getResult();
        } catch (Exception e) {
            throw this.wrapNuxeoException(e);
        }     
        
    }
    
    public CMSContext getCMSContext() {

        PortalControllerContext ctx = getPortalCtx();
        CMSController ctrl = new CMSController(ctx);
        
        return ctrl.getCMSContext();
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
            throw this.wrapNuxeoException(e);
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
            throw this.wrapNuxeoException(e);
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
     * @param doc            the doc
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
        return getLinkFromNuxeoURL(url, null);
    }

    public Link getLinkFromNuxeoURL(String url, String displayContext) {
        // Nuxeo service
        INuxeoService nuxeoService = this.getNuxeoCMSService();
        // Nuxeo customizer
        INuxeoCustomizer nuxeoCustomizer = nuxeoService.getCMSCustomizer();

        // Customizer call
        return nuxeoCustomizer.getLinkFromNuxeoURL(this.getCMSCtx(), url, displayContext);
    }

    /**
     * Generates a link to the target path.
     *
     * @param path           location of the target document
     * @param displayContext associates specific behaviour to the link
     * @return the CMS link by path
     * @throws Exception the exception
     */
    public Link getCMSLinkByPath(String path, String displayContext) {


        Window window = (Window) this.getPortalCtx().getRequest().getAttribute("osivia.window");
        Page page = window.getPage();

        Map<String, String> parameters = new HashMap<String, String>(0);

        String url = this.getPortalUrlFactory().getCMSUrl(this.portalCtx, page.getId().toString(PortalObjectPath.CANONICAL_FORMAT), path, parameters, null,
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
     * @param doc                   nuxeo target document
     * @param displayContext        specific behaviour
     * @param linkContextualization type of contextualisation {@link IPortalUrlFactory}
     * @return link
     * @throws Exception the exception
     */
    public Link getLink(Document doc, String displayContext, String linkContextualization) {

        try {
            String localContextualization = linkContextualization;

            INuxeoService nuxeoService = this.getNuxeoCMSService();

            CMSServiceCtx handlerCtx = new CMSServiceCtx();
            handlerCtx.setPortalControllerContext(new PortalControllerContext(this.getPortletCtx(), this.getRequest(),
                    this.getResponse()));

            if (this.response instanceof MimeResponse) {
                handlerCtx.setResponse((MimeResponse) this.response);
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

            String url;
            
            if (doc.getPath().startsWith("/default-domain/communaute/") || doc.getFacets().list().contains("isRemoteProxy" )) {
                 // TODO : increase performance
                 UniversalID id = getUniversalIDFromPath( doc.getPath());
                 url = this.getPortalUrlFactory().getViewContentUrl(getPortalCtx(), getCMSContext(), id);               
            }   else    {
                // Best for performance at this time
                url = this.getPortalUrlFactory().getViewContentUrl(getPortalCtx(), getCMSContext(), new UniversalID("nx", doc.getProperties().getString("ttc:webid")));
            }


            

            if (url != null) {

                Link link = new Link(url, false);
                return link;
            }

            // Sinon on passe par le gestionnaire de cms pour recontextualiser
            /*
            Window window = (Window) this.getPortalCtx().getRequest().getAttribute("osivia.window");
            Page page = window.getPage();

            Map<String, String> pageParams = new HashMap<String, String>();


            String displayLiveVersion = this.getDisplayLiveVersion();

            String path = doc.getPath();

            if (PortalObjectUtils.isSpaceSite(page.getPortal())) {
                // Forcage de l'affichage en mode portlet (pas de contextualisation) pour certains contenus
                // (pour pallier à l'absence de navigation virtuelle)

                String detailedPaths = page.getProperty("osivia.cms.contextualization.portlet.paths");
                if (StringUtils.isNotEmpty(detailedPaths)) {
                    String paths[] = detailedPaths.split(",");
                    for (String detailedPath : paths) {
                        if (path.startsWith(detailedPath)) {
                            localContextualization = IPortalUrlFactory.CONTEXTUALIZATION_PORTLET;
                        }
                    }
                }


                path = nuxeoService.getCMSCustomizer().getContentWebIdPath(handlerCtx);
            }

            String url = this.getPortalUrlFactory().getCMSUrl(this.portalCtx, page.getId().toString(PortalObjectPath.CANONICAL_FORMAT), path, pageParams,
                    localContextualization, displayContext, this.getHideMetaDatas(), null, displayLiveVersion, null);


            if (url != null) {

                Link link = new Link(url, false);
                return link;
            }

            return null;
            */

        } catch (Exception e) {
            throw this.wrapNuxeoException(e);
        }

        
        return null;
    }

    /**
     * Gets the content web id path ( like /_id/domain-def-jss/publistatfaq.html)
     * <p>
     * if no webId is defined, returns original path
     *
     * @return the content web id path
     */

    public String getContentWebIdPath() {

        INuxeoService nuxeoService = this.getNuxeoCMSService();

        String path = nuxeoService.getCMSCustomizer().getContentWebIdPath(this.getCMSCtx());
        return path;
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
            throw this.wrapNuxeoException(e);
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
            throw this.wrapNuxeoException(e);
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
            CMSServiceCtx cmsContext = this.getCMSCtx();
            INuxeoService nuxeoService = this.getNuxeoCMSService();
            return nuxeoService.getCMSCustomizer().getCommentsHTMLContent(cmsContext, this.currentDoc);
        } catch (Exception e) {
            throw this.wrapNuxeoException(e);
        }
    }

    /**
     * Get CMS item types.
     *
     * @return CMS item types
     */
    public Map<String, DocumentType> getCMSItemTypes() {
        INuxeoService nuxeoService = this.getNuxeoCMSService();
        return nuxeoService.getCMSCustomizer().getCMSItemTypes();
    }

    /**
     * Fetch a document by its path.
     *
     * @param path   the path
     * @param reload force reloading of the document (no cache)
     * @return the document
     * @throws Exception the exception
     * @deprecated use DocumentContext      *
     */
    @Deprecated
    public Document fetchDocument(String path, boolean reload) {


        try {
            CMSServiceCtx cmsCtx = this.getCMSCtx();


            // Prévisualisation des portlets définis au niveau du template
            if (this.isPathInPageEditionState(path)) {
                cmsCtx.setDisplayLiveVersion("1");
            }

            if (reload) {
                cmsCtx.setForceReload(true);
            }


            CMSItem cmsItem = getCMSService().getContent(cmsCtx, path);
            return (Document) cmsItem.getNativeItem();

        } catch (Exception e) {
            throw this.wrapNuxeoException(e);
        }
    }

    /**
     * Fetch a document by its sharedID.
     *
     * @param String shareId   the shared identifier
     * @param boolean enabledLinkOnly     
     * @return the document
     * @throws Exception the exception
     */
    public Document fetchSharedDocument(String shareId, boolean enabledLinkOnly) {


        try {
            CMSServiceCtx cmsCtx = this.getCMSCtx();

            CMSItem cmsItem = getCMSService().getByShareId(cmsCtx, shareId, enabledLinkOnly);
            return (Document) cmsItem.getNativeItem();

        } catch (Exception e) {
            throw this.wrapNuxeoException(e);
        }
    }
    
    /**
     * Fetch a document by its sharedID.
     * @param String shareId   the shared identifier
      * @return the document
     * @throws Exception the exception
     */
    public Document fetchSharedDocument(String shareId) {


        try {
            CMSServiceCtx cmsCtx = this.getCMSCtx();

            CMSItem cmsItem = getCMSService().getByShareId(cmsCtx, shareId, true);
            return (Document) cmsItem.getNativeItem();

        } catch (Exception e) {
            throw this.wrapNuxeoException(e);
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
            CMSPublicationInfos pubInfos = getCMSService().getPublicationInfos(this.getCMSCtx(), path);
            return pubInfos.getLiveId();

        } catch (Exception e) {
            throw this.wrapNuxeoException(e);
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
            CMSServiceCtx cmsContext = this.getCMSCtx();
            cmsContext.setForcePublicationInfosScope("superuser_context");

            // Publication infos
            CMSPublicationInfos pubInfos = cmsService.getPublicationInfos(cmsContext, path);

            if (pubInfos.isLiveSpace()) {
                queryCtx.setState(NuxeoQueryFilterContext.STATE_LIVE);
            }

            return queryCtx;
        } catch (Exception e) {
            throw this.wrapNuxeoException(e);
        }
    }

    /**
     * Fetch attached picture .
     *
     * @param docPath      path of the document
     * @param pictureIndex picture range
     * @return the CMS binary content
     */

    public CMSBinaryContent fetchAttachedPicture(String docPath, String pictureIndex) {
        try {


            return getCMSService().getBinaryContent(this.getCMSCtx(), "attachedPicture", docPath, pictureIndex);

        } catch (Exception e) {
            throw this.wrapNuxeoException(e);
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
            throw this.wrapNuxeoException(e);
        }
    }

    /**
     * Fetch file content.
     *
     * @param docPath   the doc path
     * @param fieldName the field name
     * @return the CMS binary content
     */
    public CMSBinaryContent fetchFileContent(String docPath, String fieldName) {
        return this.fetchFileContent(docPath, fieldName, false);
    }

    /**
     * Fetch file content.
     *
     * @param docPath   the doc path
     * @param fieldName the field name
     * @param reload    force reload indicator
     * @return the CMS binary content
     */
    public CMSBinaryContent fetchFileContent(String docPath, String fieldName, boolean reload) {
        try {
            ICMSService cmsService = getCMSService();
            CMSServiceCtx cmsContext = this.getCMSCtx();
            cmsContext.setForceReload(reload);
            return cmsService.getBinaryContent(cmsContext, "file", docPath, fieldName);
        } catch (Exception e) {
            throw this.wrapNuxeoException(e);
        }
    }

    /**
     * Create URL from webId.
     *
     * @param webId      webId
     * @param parameters request parameters
     * @return URL
     * @throws CMSException
     */
    public String createUrlFromWebId(String webId, Map<String, String> parameters) throws CMSException {
        // CMS service
        ICMSService cmsService = getCMSService();
        // WebId service
        IWebIdService webIdService = this.getWebIdService();
        // Nuxeo service
        INuxeoService nuxeoService = this.getNuxeoCMSService();
        // CMS customizer
        INuxeoCustomizer cmsCustomizer = nuxeoService.getCMSCustomizer();
        // Portal URL factory
        IPortalUrlFactory portalUrlFactory = this.getPortalUrlFactory();

        // Portal controller context
        PortalControllerContext portalControllerContext = this.getPortalCtx();

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
     * @param webid   the web id
     * @param content some options
     * @return the resource
     */
    public String createWebIdLink(String webid, String content) {
        try {
            // Parameterized permalinks indicator
            Boolean permalinks = (Boolean) this.request.getAttribute(InternalConstants.PARAMETERIZED_PERMALINKS_ATTRIBUTE);

            if ((this.getRequest().getUserPrincipal() == null) || BooleanUtils.isTrue(permalinks)) {
                // Serve anonymous resource by servlet
                StringBuilder url = new StringBuilder();
                if (BooleanUtils.isTrue(permalinks)) {
                    url.append(this.request.getScheme());
                    url.append("://");
                    url.append(this.request.getServerName());
                    url.append(":");
                    url.append(this.request.getServerPort());
                }

                url.append(this.getRequest().getContextPath());
                url.append("/sitepicture?path=");
                url.append(URLEncoder.encode(webid, "UTF-8"));

                if (content != null) {
                    url.append("&content=");
                    url.append(content);
                }

                return url.toString();
            }

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

        } catch (Exception e) {
            throw this.wrapNuxeoException(e);
        }
    }

    /**
     * Gets the CMS ctx.
     *
     * @return the CMS ctx
     */
    public CMSServiceCtx getCMSCtx() {

        try {

            this.cmsCtx = new CMSServiceCtx();
            
            this.cmsCtx.setPortalControllerContext(getPortalCtx());


            if (this.response instanceof MimeResponse) {
                this.cmsCtx.setResponse((MimeResponse) this.response);
            }
            
            if(StringUtils.isNotEmpty(this.getScope()))
                    this.cmsCtx.setScope(this.getScope());
            else    {
/*                
                // Compatiblity with OSIVIA Connect
                String scope = null;
                if( getAuthType() == NuxeoCommandContext.AUTH_TYPE_SUPERUSER)   {
                    if( getCacheType() == CacheInfo.CACHE_SCOPE_NONE)  {
                        scope = "superuser_no_cache";
                    }   else
                        scope = "superuser_context";
                }
                this.cmsCtx.setScope(scope);
                this.cmsCtx.setForcePublicationInfosScope(scope);  
                String scope;
                */
                if ((NuxeoCommandContext.AUTH_TYPE_SUPERUSER == this.getAuthType()) && (CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT == this.getCacheType())) {
                    scope = "superuser_context";
                } else if ((NuxeoCommandContext.AUTH_TYPE_SUPERUSER == this.getAuthType()) && (CacheInfo.CACHE_SCOPE_NONE == this.getCacheType())) {
                    scope = "superuser_no_cache";
                } else if ((NuxeoCommandContext.AUTH_TYPE_USER == this.getAuthType()) && (CacheInfo.CACHE_SCOPE_PORTLET_SESSION == this.getCacheType())) {
                    scope = "user_session";
                } else if ((NuxeoCommandContext.AUTH_TYPE_ANONYMOUS == this.getAuthType()) && (CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT == this.getCacheType())) {
                    scope = "anonymous";
                } else {
                    scope = null;
                }
                this.cmsCtx.setScope(scope);
                this.cmsCtx.setForcePublicationInfosScope(scope);
            }
            
            
            this.cmsCtx.setForcePublicationInfosScope(this.getForcePublicationInfosScope());
            this.cmsCtx.setDisplayLiveVersion(this.getDisplayLiveVersion());


            this.cmsCtx.setForcedLivePath(this.getForcedLivePath());

            if (this.getRequest() != null) {
                this.cmsCtx.setPageId(this.getPageId());
            }
            this.cmsCtx.setDoc(this.getCurrentDoc());
            this.cmsCtx.setHideMetaDatas(this.getHideMetaDatas());
            this.cmsCtx.setDisplayContext(this.displayContext);

            this.cmsCtx.setCreationType(this.docTypeToCreate);
            if (this.parentPathToCreate != null) {
                this.cmsCtx.setCreationPath(this.getComputedPath(this.parentPathToCreate));
            }

            if (this.reloadResource) {
                this.cmsCtx.setForceReload(true);
            } else {
                // servlet ressource
                if (this.getServletRequest() != null) {
                    String refresh = this.getServletRequest().getParameter("refresh");
                    if (BooleanUtils.toBoolean(refresh)) {
                        this.cmsCtx.setForceReload(true);
                    }
                }

            }
            this.cmsCtx.setStreamingSupport(this.streamingSupport);

            this.cmsCtx.setSatellite(this.satellite);


            return this.cmsCtx;
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

    /**
     * Get the user avatar (acceded from portlets).
     *
     * @param username
     * @return a link to the avatar
     * @throws CMSException
     */
    public Link getUserAvatar(String username) throws CMSException {

        return getNuxeoCMSService().getCMSCustomizer().getUserAvatar(username);

    }

    /**
     * Refresh the user avatar (acceded from portlets).
     *
     * @param username
     * @return a timestamp
     * @throws CMSException
     */
    public String refreshUserAvatar(String username) {
        //Toolbar updated at layout phase
        getCMSCtx().getServletRequest().setAttribute("osivia.refreshPageLayout", Boolean.TRUE);
        
        return getCMSService().refreshUserAvatar(this.getCMSCtx(), username);
        
        
    }
    
    /**
     * Refresh the user infos
     *
     * @param username
     * @return a timestamp
     * @throws CMSException
     */
    public void refreshUserInfo(String username) {
        //Toolbar updated at layout phase
        getCMSCtx().getServletRequest().setAttribute("osivia.refreshPageLayout", Boolean.TRUE);
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
        IDirectoryService service = this.getDirectoryService();
        DirectoryPerson p = null;
        if (service != null) {
            p = service.getPerson(uid);
        }

        return p;
    }

    /**
     * Get current document context.
     *
     * @return document context
     */
    public NuxeoDocumentContext getCurrentDocumentContext() {
        // Window
        PortalWindow window;
        if (this.request == null) {
            window = null;
        } else {
            window = WindowFactory.getWindow(this.request);
        }

        // Current path
        String currentPath;
        if (window == null) {
            currentPath = null;
        } else {
            currentPath = window.getProperty(Constants.WINDOW_PROP_URI);
        }

        if (StringUtils.isEmpty(currentPath)) {
            currentPath = this.contentPath;
        }

        return this.getDocumentContext(currentPath);
    }

    /**
     * Get document context.
     *
     * @param path CMS path or webId
     * @return document context
     */
    public NuxeoDocumentContext getDocumentContext(String path) {
        // CMS service
        ICMSService cmsService = getCMSService();
        // CMS context
        CMSServiceCtx cmsContext = this.getCMSCtx();
/*
        // Scope
        if (StringUtils.isEmpty(cmsContext.getScope())) {
            String scope;
            if ((NuxeoCommandContext.AUTH_TYPE_SUPERUSER == this.getAuthType()) && (CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT == this.getCacheType())) {
                scope = "superuser_context";
            } else if ((NuxeoCommandContext.AUTH_TYPE_SUPERUSER == this.getAuthType()) && (CacheInfo.CACHE_SCOPE_NONE == this.getCacheType())) {
                scope = "superuser_no_cache";
            } else if ((NuxeoCommandContext.AUTH_TYPE_USER == this.getAuthType()) && (CacheInfo.CACHE_SCOPE_PORTLET_SESSION == this.getCacheType())) {
                scope = "user_session";
            } else if ((NuxeoCommandContext.AUTH_TYPE_ANONYMOUS == this.getAuthType()) && (CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT == this.getCacheType())) {
                scope = "anonymous";
            } else {
                scope = null;
            }
            cmsContext.setScope(scope);
            cmsContext.setForcePublicationInfosScope(scope);
        }
        */

        // Document context
        NuxeoDocumentContext documentContext;

        if (path == null) {
            documentContext = null;
        } else {
            try {
                documentContext = cmsService.getDocumentContext(cmsContext, path, NuxeoDocumentContext.class);
            } catch (CMSException e) {
                throw this.wrapNuxeoException(e);
            }
        }

        return documentContext;
    }

    /**
     * Getter for asynchronousCommand.
     *
     * @return the asynchronousCommand
     */
    public boolean isAsynchronousCommand() {
        return this.asynchronousCommand;
    }

    /**
     * Setter for asynchronousCommand.
     *
     * @param asynchronousCommand the asynchronousCommand to set
     */
    public void setAsynchronousCommand(boolean asynchronousCommand) {
        this.asynchronousCommand = asynchronousCommand;
    }

    
    /**
     * Gets the CMS nuxeo web context name.
     *
     * @return the CMS nuxeo web context name
     */
    public static String getCMSNuxeoWebContextName() {
        if (cmsContextName == null) {
            INuxeoService nuxeoService = Locator.findMBean(INuxeoService.class, "osivia:service=NuxeoService");
            return nuxeoService.getCMSCustomizer().getResourceContextPath();
        }
        return cmsContextName;
    }



}
