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
package fr.toutatice.portail.cms.nuxeo.portlets.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.portal.common.invocation.Scope;
import org.jboss.portal.core.aspects.server.UserInterceptor;
import org.jboss.portal.identity.User;
import org.jboss.portal.server.ServerInvocation;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.cache.services.CacheInfo;
import org.osivia.portal.api.cache.services.ICacheService;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.urls.EcmCommand;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.api.urls.Link;
import org.osivia.portal.core.cms.CMSBinaryContent;
import org.osivia.portal.core.cms.CMSEditableWindow;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSHandlerProperties;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSItemType;
import org.osivia.portal.core.cms.CMSObjectPath;
import org.osivia.portal.core.cms.CMSPage;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.ICMSService;
import org.osivia.portal.core.cms.NavigationItem;
import org.osivia.portal.core.page.PageProperties;
import org.osivia.portal.core.profils.IProfilManager;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCommandService;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoService;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoServiceCommand;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandContext;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandServiceFactory;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoConnectionProperties;
import fr.toutatice.portail.cms.nuxeo.portlets.commands.DocumentFetchPublishedCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.DefaultCMSCustomizer;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.EditableWindowAdapter;
import fr.toutatice.portail.cms.nuxeo.portlets.document.DocumentFetchLiveCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.document.FileContentCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.document.InternalPictureCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.document.PictureContentCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.document.PutInTrashDocumentCommand;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.AskSetOnLineCommand;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.CancelWorkflowCommand;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.DocumentDeleteCommand;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.DocumentRemovePropertyCommand;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.DocumentUpdatePropertiesCommand;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.EditableWindow;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.EditableWindowHelper;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.SetOffLineCommand;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.SetOnLineCommand;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.ValidationPublishCommand;

/**
 * CMS service Toutatice implementation.
 * 
 * @see ICMSService
 */
public class CMSService implements ICMSService {

    /** Logger. */
    protected static final Log logger = LogFactory.getLog(CMSService.class);

    /** Portlet context. */
    private final PortletContext portletCtx;
    private INuxeoCommandService nuxeoCommandService;
    private INuxeoService nuxeoService;
    private IProfilManager profilManager;
    private ICacheService serviceCache;
    private DefaultCMSCustomizer customizer;
    private IPortalUrlFactory urlFactory;

    /**
     * Constructor.
     * 
     * @param portletCtx portlet context
     */
    public CMSService(PortletContext portletCtx) {
        super();
        this.portletCtx = portletCtx;
    }

    public DefaultCMSCustomizer getCustomizer() {
        return this.customizer;
    }

    public void setCustomizer(DefaultCMSCustomizer customizer) {
        this.customizer = customizer;
    }


    /**
     * Create CMS item.
     * 
     * @param cmsCtx CMS context
     * @param path CMS path
     * @param displayName display name
     * @param doc Nuxeo document
     * @return CMS item
     * @throws CMSException
     */
    public CMSItem createItem(CMSServiceCtx cmsCtx, String path, String displayName, Document doc) throws CMSException {
        // CMS item properties
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("displayName", displayName);
        properties.put("type", doc.getType());

        CMSItem cmsItem = new CMSItem(path, doc.getString("ttc:webid"), properties, doc);

        // CMS item type
        CMSItemType type = this.customizer.getCMSItemTypes().get(doc.getType());
        cmsItem.setType(type);

        return cmsItem;
    }


    /**
     * Create CMS navigation item.
     * 
     * @param cmsCtx CMS context
     * @param path CMS path
     * @param displayName display name
     * @param doc Nuxeo document
     * @param publishSpacePath publish space path
     * @return CMS navigation item
     * @throws CMSException
     */
    public CMSItem createNavigationItem(CMSServiceCtx cmsCtx, String path, String displayName, Document doc, String publishSpacePath) throws CMSException {
        CMSItem cmsItem = this.createItem(cmsCtx, path, displayName, doc);
        CMSItem publishSpaceItem = null;

        if ((publishSpacePath != null) && !path.equals(publishSpacePath)) {
            publishSpaceItem = this.getPortalNavigationItem(cmsCtx, publishSpacePath, publishSpacePath);
        } else {
            publishSpaceItem = cmsItem;
        }

        this.getCustomizer().getNavigationItemAdapter().adaptPublishSpaceNavigationItem(cmsItem, publishSpaceItem);

        return cmsItem;
    }


    public List<CMSItem> getChildren(CMSServiceCtx ctx, String path) throws CMSException {

        return new ArrayList<CMSItem>();
    }

    public IProfilManager getProfilManager() throws Exception {
        if (this.profilManager == null) {
            this.profilManager = (IProfilManager) this.portletCtx.getAttribute(Constants.PROFILE_SERVICE_NAME);
        }

        return this.profilManager;
    }

    public INuxeoService getNuxeoService() throws Exception {

        if (this.nuxeoService == null) {
            this.nuxeoService = (INuxeoService) this.portletCtx.getAttribute("NuxeoService");
        }

        return this.nuxeoService;

    }

    public ICacheService getCacheService() throws Exception {

        if (this.serviceCache == null) {
            this.serviceCache = (ICacheService) this.portletCtx.getAttribute("CacheService");
        }

        return this.serviceCache;
    }

    public INuxeoCommandService getNuxeoCommandService() throws Exception {
        if (this.nuxeoCommandService == null) {
            this.nuxeoCommandService = NuxeoCommandServiceFactory.getNuxeoCommandService(this.portletCtx);
        }
        return this.nuxeoCommandService;
    }

    public IPortalUrlFactory getPortalUrlFactory() {
        if (this.urlFactory == null) {
            this.urlFactory = (IPortalUrlFactory) this.portletCtx.getAttribute("UrlService");
        }
        return this.urlFactory;
    }


    public Object executeNuxeoCommand(CMSServiceCtx cmsCtx, final INuxeoCommand command) throws Exception {

        NuxeoCommandContext commandCtx = new NuxeoCommandContext(this.portletCtx, cmsCtx.getServerInvocation());
        /*
         * Transmission du mode asynchrone ou non de la mise en cache
         * du résultat de la commande.
         */
        commandCtx.setAsyncCacheRefreshing(cmsCtx.isAsyncCacheRefreshing());

        if (cmsCtx.isForceReload()) {
            commandCtx.setForceReload(true);
        }

        // pour debug
        // commandCtx.setCacheTimeOut(0);

        /*
         * ctx.setAuthType(getAuthType()); ctx.setAuthProfil(getScopeProfil());
         * ctx.setCacheTimeOut(cacheTimeOut); ctx.setCacheType(cacheType);
         * ctx.setAsynchronousUpdates(asynchronousUpdates);
         */

        String scope = cmsCtx.getScope();

        // Par défaut
        commandCtx.setAuthType(NuxeoCommandContext.AUTH_TYPE_USER);
        commandCtx.setCacheType(CacheInfo.CACHE_SCOPE_NONE);

        if (scope != null) {
            if (!"__nocache".equals(scope)) {

                // commandCtx.setAsynchronousUpdates(true);


                if ("user_session".equals(scope)) {
                    commandCtx.setAuthType(NuxeoCommandContext.AUTH_TYPE_USER);
                    commandCtx.setCacheType(CacheInfo.CACHE_SCOPE_PORTLET_SESSION);
                } else {
                    if ("anonymous".equals(scope)) {
                        commandCtx.setAuthType(NuxeoCommandContext.AUTH_TYPE_ANONYMOUS);
                        commandCtx.setCacheType(CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);
                    } else if ("superuser_context".equals(scope)) {
                        commandCtx.setAuthType(NuxeoCommandContext.AUTH_TYPE_SUPERUSER);
                        commandCtx.setCacheType(CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);
                    } else if("superuser_no_cache".equals(scope)){
                        commandCtx.setAuthType(NuxeoCommandContext.AUTH_TYPE_SUPERUSER);
                        commandCtx.setCacheType(CacheInfo.CACHE_SCOPE_NONE);
                    } else {
                        commandCtx.setAuthType(NuxeoCommandContext.AUTH_TYPE_PROFIL);
                        commandCtx.setAuthProfil(this.getProfilManager().getProfil(scope));
                        commandCtx.setCacheType(CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);
                    }
                }
            }
        }

        return this.getNuxeoCommandService().executeCommand(commandCtx, new INuxeoServiceCommand() {

            public String getId() {
                return command.getId();
            }

            public Object execute(Session nuxeoSession) throws Exception {
                return command.execute(nuxeoSession);
            }
        });


    }

    private CMSItem fetchContent(CMSServiceCtx cmsCtx, String path) throws Exception {

        String savedScope = cmsCtx.getScope();
        try {
            boolean saveAsync = cmsCtx.isAsyncCacheRefreshing();

            cmsCtx.setAsyncCacheRefreshing(false);
            CMSPublicationInfos pubInfos = this.getPublicationInfos(cmsCtx, path);
            path = pubInfos.getDocumentPath();

            cmsCtx.setAsyncCacheRefreshing(saveAsync);

            boolean haveToGetLive = "1".equals(cmsCtx.getDisplayLiveVersion());

            if (pubInfos.getDocumentPath().equals(cmsCtx.getForcedLivePath()) || pubInfos.getLiveId().equals(cmsCtx.getForcedLivePath())) {
                haveToGetLive = true;
            }

            // Document non publié et rattaché à un workspace
            if ((!pubInfos.isPublished() && StringUtils.isNotEmpty(pubInfos.getPublishSpacePath()) && pubInfos.isLiveSpace())) {
                haveToGetLive = true;
            }

            // Ajout JSS 20130122
            // Document non publié et non rattaché à un espace : usage collaboratif
            if (!pubInfos.isPublished() && (pubInfos.getPublishSpacePath() == null)) {
                haveToGetLive = true;
            }


            cmsCtx.setScope("superuser_context");

            if (haveToGetLive) {

                Document doc = (Document) this.executeNuxeoCommand(cmsCtx, (new DocumentFetchLiveCommand(path, "Read")));
                return this.createItem(cmsCtx, path, doc.getTitle(), doc);

            } else {

                Document doc = (Document) this.executeNuxeoCommand(cmsCtx, (new DocumentFetchPublishedCommand(path)));


                return this.createItem(cmsCtx, path, doc.getTitle(), doc);

            }
        } finally {
            cmsCtx.setScope(savedScope);
        }

    }


    public CMSItem getContent(CMSServiceCtx cmsCtx, String path) throws CMSException {


        CMSItem content = null;
        try {

            content = this.fetchContent(cmsCtx, path);

            this.getCustomizer().getCMSItemAdapter().adaptItem(cmsCtx, content);

        } catch (NuxeoException e) {
            e.rethrowCMSException();
        } catch (Exception e) {
            if (!(e instanceof CMSException)) {
                throw new CMSException(e);
            } else {
                throw (CMSException) e;
            }
        }
        return content;
    }

    public CMSBinaryContent getBinaryContent(CMSServiceCtx cmsCtx, String type, String docPath, String parameter) throws CMSException {
        CMSBinaryContent content = new CMSBinaryContent();

        if ("file".equals(type)) {
            content = this.getFileContent(cmsCtx, docPath, parameter);
        } else if ("attachedPicture".equals(type)) {
            content = this.getAttachedPicture(cmsCtx, docPath, parameter);
        } else if ("picture".equals(type)) {
            content = this.getPicture(cmsCtx, docPath, parameter);
        }

        return content;
    }

    public CMSBinaryContent getAttachedPicture(CMSServiceCtx cmsCtx, String docPath, String pictureIndex) throws CMSException {
        CMSBinaryContent cmsContent = null;
        try {

            cmsContent = this.fetchAttachedPicture(cmsCtx, docPath, pictureIndex);

        } catch (NuxeoException e) {
            e.rethrowCMSException();
        } catch (Exception e) {
            if (!(e instanceof CMSException)) {
                throw new CMSException(e);
            } else {
                throw (CMSException) e;
            }
        }

        return cmsContent;
    }

    private CMSBinaryContent fetchAttachedPicture(CMSServiceCtx cmsCtx, String docPath, String pictureIndex) throws Exception {
        CMSBinaryContent pictureContent = null;
        String savedScope = cmsCtx.getScope();
        try {

            CMSItem containerDoc = this.fetchContent(cmsCtx, docPath);

            if (containerDoc != null) {
                cmsCtx.setScope("superuser_context");

                pictureContent = (CMSBinaryContent) this.executeNuxeoCommand(cmsCtx, (new InternalPictureCommand((Document) containerDoc.getNativeItem(),
                        pictureIndex)));
            }

        } finally {
            cmsCtx.setScope(savedScope);
        }
        return pictureContent;
    }

    public CMSBinaryContent getPicture(CMSServiceCtx cmsCtx, String docPath, String content) throws CMSException {
        CMSBinaryContent cmsContent = null;

        try {

            cmsContent = this.fetchPicture(cmsCtx, docPath, content);

        } catch (NuxeoException e) {
            e.rethrowCMSException();
        } catch (Exception e) {
            if (!(e instanceof CMSException)) {
                throw new CMSException(e);
            } else {
                throw (CMSException) e;
            }
        }

        return cmsContent;
    }


    private CMSBinaryContent fetchPicture(CMSServiceCtx cmsCtx, String docPath, String content) throws Exception {

        CMSBinaryContent pictureContent = null;

        String savedScope = cmsCtx.getScope();
        String savedPubInfosScope = cmsCtx.getForcePublicationInfosScope();
        try {
            CMSItem picture;

            /* Lecture du document picture */

            // Accès en super-user pour voir si la picture est accessible en mode anonyme
            cmsCtx.setForcePublicationInfosScope("superuser_context");
            CMSPublicationInfos publiInfos = this.getPublicationInfos(cmsCtx, docPath);
            cmsCtx.setForcePublicationInfosScope(null);

            if (publiInfos.isAnonymouslyReadable()) {
                cmsCtx.setForcePublicationInfosScope("anonymous");
            }

            picture = this.fetchContent(cmsCtx, publiInfos.getDocumentPath());


            /* Lecture de la ressources binaire */

            // On a les droits, on accède à la ressource binaire en mode super user
            cmsCtx.setScope("superuser_context");

            pictureContent = (CMSBinaryContent) this.executeNuxeoCommand(cmsCtx, (new PictureContentCommand((Document) picture.getNativeItem(), content)));


        } catch (Exception e) {
            if (!(e instanceof CMSException)) {
                if (e instanceof NuxeoException && (((NuxeoException) e).getErrorCode() == NuxeoException.ERROR_NOTFOUND)) {
                    return null;
                } else {
                    throw new CMSException(e);
                }
            } else {

                throw (CMSException) e;
            }
        } finally {
            cmsCtx.setScope(savedScope);
            cmsCtx.setForcePublicationInfosScope(savedPubInfosScope);
        }
        return pictureContent;

    }

    public CMSBinaryContent getFileContent(CMSServiceCtx cmsCtx, String docPath, String fieldName) throws CMSException {
        CMSBinaryContent cmsContent = null;
        try {

            cmsContent = this.fetchFileContent(cmsCtx, docPath, fieldName);

        } catch (NuxeoException e) {
            e.rethrowCMSException();
        } catch (Exception e) {
            if (!(e instanceof CMSException)) {
                throw new CMSException(e);
            } else {
                throw (CMSException) e;
            }
        }

        return cmsContent;
    }



    private CMSBinaryContent fetchFileContent(CMSServiceCtx cmsCtx, String docPath, String fieldName) throws Exception {
        CMSBinaryContent content = null;
        String savedScope = cmsCtx.getScope();
        try {
            /* Si un scope a été posé dans la portlet appelant la resource,
             * on applique celui-ci.
             */
            if(StringUtils.isNotEmpty(savedScope)){
                cmsCtx.setForcePublicationInfosScope(savedScope);
            }
            CMSItem document = fetchContent(cmsCtx, docPath);
            
            if (document != null) {

                cmsCtx.setScope("superuser_context");
                
                FileContentCommand cmd = new FileContentCommand((Document) document.getNativeItem(), fieldName);
                
                if( cmsCtx.isStreamingSupport())    {
                    PropertyMap map = ((Document) document.getNativeItem()).getProperties().getMap("file:content");
                    if(map != null && !map.isEmpty()){
                        String size = map.getString("length");

                    
                        if(size != null && Long.parseLong(size)> 1000000L) {
                            //Activation du mode streaming
                            cmd.setStreamingSupport(true);
                            // Pas de cache en mode streaming                   
                            cmsCtx.setScope("superuser_no_cache");
                        }
                    }
                }

                content = (CMSBinaryContent) executeNuxeoCommand(cmsCtx,
                        (cmd));
                
                
                
            }
        } finally {
            cmsCtx.setScope(savedScope);
        }
        return content;
    }

    
    public boolean checkContentAnonymousAccess(CMSServiceCtx cmsCtx, String path) throws CMSException {

        try {
            CMSPublicationInfos pubInfos = this.getPublicationInfos(cmsCtx, path);

            return pubInfos.isAnonymouslyReadable();

        } catch (NuxeoException e) {
            e.rethrowCMSException();
        } catch (Exception e) {
            if (!(e instanceof CMSException)) {
                throw new CMSException(e);
            } else {
                throw (CMSException) e;
            }
        }

        // Ne passe jamamis
        return false;
    }


    public CMSHandlerProperties getItemHandler(CMSServiceCtx ctx) throws CMSException {
        // Document doc = ctx.g
        try {
            if (!"detailedView".equals(ctx.getDisplayContext())) {
                return this.getNuxeoService().getCMSCustomizer().getCMSPlayer(ctx);
            } else {
                return ((DefaultCMSCustomizer) this.getNuxeoService().getCMSCustomizer()).getCMSDefaultPlayer(ctx);
            }
        } catch (NuxeoException e) {
            e.rethrowCMSException();
        } catch (Exception e) {
            if (!(e instanceof CMSException)) {
                throw new CMSException(e);
            } else {
                throw (CMSException) e;
            }
        }

        // Not possible
        return null;
    }


    public Map<String, NavigationItem> loadPartialNavigationTree(CMSServiceCtx cmsCtx, CMSItem publishSpaceConfig, String path, boolean fetchSubItems)
            throws CMSException {

        String savedScope = cmsCtx.getScope();

        try {


            Map<String, NavigationItem> navItems = null;

            List<String> idsToFetch = new ArrayList<String>();
            boolean fetchRoot = false;

            /* On récupère le dernier arbre de publication partiel */


            String cacheId = "partial_navigation_tree/" + publishSpaceConfig.getPath();
            Object request = cmsCtx.getServerInvocation().getServerContext().getClientRequest();
            boolean refreshing = PageProperties.getProperties().isRefreshingPage();
            PartialNavigationInvoker partialNavInvoker = null;
            if (refreshing) {
                partialNavInvoker = (PartialNavigationInvoker) ((HttpServletRequest) request).getAttribute("partialNavInvoker");
            }
            CacheInfo cacheInfos = new CacheInfo(cacheId, CacheInfo.CACHE_SCOPE_PORTLET_SESSION, null, request, this.portletCtx, false);
            // délai d'une session
            cacheInfos.setExpirationDelay(200000);


            navItems = (Map<String, NavigationItem>) this.getCacheService().getCache(cacheInfos);


            if (navItems == null) {

                navItems = new HashMap<String, NavigationItem>();
                fetchRoot = true;
            }

            /*
             * Boucle sur l'arbo pour recuperer les ids à fetcher
             * (doc absents de l'arbre)
             */

            String pathToCheck = path;

            CMSServiceCtx superUserCtx = new CMSServiceCtx();
            superUserCtx.setControllerContext(cmsCtx.getControllerContext());
            cmsCtx.setScope("superuser_context");


            do {
                NavigationItem navItem = navItems.get(pathToCheck);

                if (navItem != null && (fetchSubItems && navItem.isUnfetchedChildren())) {
                    Document doc = (Document) this.executeNuxeoCommand(cmsCtx, (new DocumentFetchLiveCommand(pathToCheck, "Read")));

                    if (!idsToFetch.contains(doc.getId())) {
                        idsToFetch.add(doc.getId());
                    }
                }

                CMSObjectPath parentPath = CMSObjectPath.parse(pathToCheck).getParent();
                pathToCheck = parentPath.toString();

                if (navItem == null) {
                    Document doc = (Document) this.executeNuxeoCommand(cmsCtx, (new DocumentFetchLiveCommand(pathToCheck, "Read")));
                    if (!idsToFetch.contains(doc.getId())) {
                        idsToFetch.add(doc.getId());
                    }
                }


            } while (pathToCheck.contains(publishSpaceConfig.getPath()));


            if ((idsToFetch.size() > 0) || fetchRoot)

            {
                cmsCtx.setScope("__nocache");

                /* appel de la commande */

                navItems = (Map<String, NavigationItem>) this.executeNuxeoCommand(cmsCtx, (new PartialNavigationCommand(publishSpaceConfig, navItems,
                        idsToFetch, fetchRoot, path)));

                /* Stockage de l'arbre partiel */

                cacheInfos.setForceReload(true);
                cacheInfos.setForceNOTReload(false);
                partialNavInvoker = new PartialNavigationInvoker(navItems);
                if (refreshing) {
                    ((HttpServletRequest) request).setAttribute("partialNavInvoker", partialNavInvoker);
                }
                cacheInfos.setInvoker(partialNavInvoker);
                this.getCacheService().getCache(cacheInfos);

            }

            return navItems;
        } catch (Exception e) {
            if (!(e instanceof CMSException)) {
                throw new CMSException(e);
            } else {
                throw (CMSException) e;
            }
        } finally {
            cmsCtx.setScope(savedScope);
        }
    }


    public CMSItem getPortalNavigationItem(CMSServiceCtx cmsCtx, String publishSpacePath, String path) throws CMSException {

        String savedScope = cmsCtx.getScope();

        if ((cmsCtx.getScope() == null) || "__nocache".equals(cmsCtx.getScope())) {
            cmsCtx.setScope("user_session");
        }

        try {

            String livePath = DocumentPublishSpaceNavigationCommand.computeNavPath(path);

            CMSItem publishSpaceConfig = this.getSpaceConfig(cmsCtx, publishSpacePath);

            if (publishSpaceConfig == null) {
                throw new CMSException(CMSException.ERROR_NOTFOUND);
            }


            Map<String, NavigationItem> navItems = null;

            if ("1".equals(publishSpaceConfig.getProperties().get("partialLoading"))) {
                navItems = this.loadPartialNavigationTree(cmsCtx, publishSpaceConfig, path, false);
            } else {
                boolean forceLiveVersion = false;
                if ("1".equals(cmsCtx.getDisplayLiveVersion())) {
                    forceLiveVersion = true;
                }
                navItems = (Map<String, NavigationItem>) this.executeNuxeoCommand(cmsCtx, (new DocumentPublishSpaceNavigationCommand(publishSpaceConfig,
                        forceLiveVersion)));
            }

            if (navItems != null) {
                NavigationItem navItem = navItems.get(livePath);
                if (navItem != null) {

                    CMSItem item = navItem.getAdaptedCMSItem();
                    if (item == null) {
                        if (navItem.getMainDoc() != null) {
                            navItem.setAdaptedCMSItem(this.createNavigationItem(cmsCtx, livePath, ((Document) navItem.getMainDoc()).getTitle(),
                                    (Document) navItem.getMainDoc(), publishSpaceConfig.getPath()));
                        } else {
                            return null;
                        }
                    }

                    return navItem.getAdaptedCMSItem();
                }

            }

        } catch (NuxeoException e) {
            e.rethrowCMSException();
        } catch (Exception e) {
            if (!(e instanceof CMSException)) {
                throw new CMSException(e);
            } else {
                throw (CMSException) e;
            }
        } finally {
            cmsCtx.setScope(savedScope);
        }

        // Not possible
        return null;
    }

    public List<CMSItem> getPortalNavigationSubitems(CMSServiceCtx cmsCtx, String publishSpacePath, String path) throws CMSException {

        String savedScope = cmsCtx.getScope();

        if ((cmsCtx.getScope() == null) || "__nocache".equals(cmsCtx.getScope())) {
            cmsCtx.setScope("user_session");
        }
        try {

            CMSItem publishSpaceConfig = this.getSpaceConfig(cmsCtx, publishSpacePath);

            if (publishSpaceConfig == null) {
                throw new CMSException(CMSException.ERROR_NOTFOUND);
            }


            Map<String, NavigationItem> navItems = null;

            if ("1".equals(publishSpaceConfig.getProperties().get("partialLoading"))) {
                navItems = this.loadPartialNavigationTree(cmsCtx, publishSpaceConfig, path, true);
            } else {
                boolean forceLiveVersion = false;
                if ("1".equals(cmsCtx.getDisplayLiveVersion())) {
                    forceLiveVersion = true;
                }

                navItems = (Map<String, NavigationItem>) this.executeNuxeoCommand(cmsCtx, (new DocumentPublishSpaceNavigationCommand(publishSpaceConfig,
                        forceLiveVersion)));
            }

            if (navItems != null) {
                NavigationItem navItem = navItems.get(path);
                if (navItem != null) {
                    List<CMSItem> childrens = new ArrayList<CMSItem>();


                    for (Object child : navItem.getChildren()) {

                        Document docChild = (Document) child;

                        String childNavPath = DocumentPublishSpaceNavigationCommand.computeNavPath(docChild.getPath());

                        NavigationItem navChild = navItems.get(childNavPath);

                        CMSItem item = navChild.getAdaptedCMSItem();
                        if (item == null) {
                            if (navChild.getMainDoc() != null) {
                                navChild.setAdaptedCMSItem(this.createNavigationItem(cmsCtx, childNavPath, ((Document) navChild.getMainDoc()).getTitle(),
                                        (Document) navChild.getMainDoc(), publishSpacePath));
                            }
                        }

                        childrens.add(navChild.getAdaptedCMSItem());

                    }
                    return childrens;
                }
            }

        } catch (NuxeoException e) {
            e.rethrowCMSException();
        } catch (Exception e) {
            if (!(e instanceof CMSException)) {
                throw new CMSException(e);
            } else {
                throw (CMSException) e;
            }
        } finally {
            cmsCtx.setScope(savedScope);
        }


        // Not possible
        return null;
    }


    /**
     * {@inheritDoc}
     */
    public List<CMSItem> getPortalSubitems(CMSServiceCtx cmsContext, String path) throws CMSException {
        try {
            // Parent identifier
            Document parent = (Document) this.fetchContent(cmsContext, path).getNativeItem();
            String parentId = parent.getId();

            // Live content
            boolean liveContent = "1".equals(cmsContext.getDisplayLiveVersion());

            // Nuxeo command execution
            INuxeoCommand nuxeoCommand = new ListCMSSubitemsCommand(cmsContext, parentId, liveContent);
            return (List<CMSItem>) this.executeNuxeoCommand(cmsContext, nuxeoCommand);
            
        } catch (CMSException e) {
            throw e;
        } catch (Exception e) {
            throw new CMSException(e);
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.osivia.portal.core.cms.ICMSService#getPublicationInfos(org.osivia.portal.core.cms.CMSServiceCtx, java.lang.String)
     */
    public CMSPublicationInfos getPublicationInfos(CMSServiceCtx ctx, String path) throws CMSException {
        /* Instanciation pour que la méthode soit techniquement "null safe" */
        CMSPublicationInfos pubInfos = new CMSPublicationInfos();

        try {

            String savedScope = ctx.getScope();

            try {/*
                  * getPublicationInfos est toujours utilisé avec les droits de
                  * l'utilisateur (il remplit en ce sens un testeur de droits car
                  * les informations retournées sont faites selon ces derniers).
                  * Cependant, il est possible de forcer son exécution avec
                  * un autre modepar l'intermédiaire d'une vairiable du CMS Service
                  * Context (cas des méthodes getAnonymousContent(), getAttachedPicture()).
                  */
                if (StringUtils.isNotEmpty(ctx.getForcePublicationInfosScope())) {
                    ctx.setScope(ctx.getForcePublicationInfosScope());
                } else {
                    // In anonymous mode, publicationsInfos are shared

                    ServerInvocation invocation = ctx.getServerInvocation();
                    User user = (User) invocation.getAttribute(Scope.PRINCIPAL_SCOPE, UserInterceptor.USER_KEY);
                    if (user == null) {
                        ctx.setScope("anonymous");
                    } else {
                        ctx.setScope("user_session");
                    }
                }

                pubInfos = (CMSPublicationInfos) this.executeNuxeoCommand(ctx, (new PublishInfosCommand(path)));

                if (pubInfos != null) {
                    List<Integer> errors = pubInfos.getErrorCodes();
                    if (errors != null) {
                        if (errors.contains(CMSPublicationInfos.ERROR_CONTENT_FORBIDDEN)) {

                            throw new CMSException(CMSException.ERROR_FORBIDDEN);
                        }
                        if (errors.contains(CMSPublicationInfos.ERROR_CONTENT_NOT_FOUND)) {

                            throw new CMSException(CMSException.ERROR_NOTFOUND);
                        }
                    }

                }
            } finally {
                ctx.setScope(savedScope);

            }

        } catch (NuxeoException e) {

            e.rethrowCMSException();
        } catch (Exception e) {

            if (!(e instanceof CMSException)) {
                throw new CMSException(e);
            } else {
                throw (CMSException) e;
            }
        }

        return pubInfos;

    }

    public CMSItem getSpaceConfig(CMSServiceCtx cmsCtx, String publishSpacePath) throws CMSException {
        CMSItem configItem = null;

        HttpServletRequest portalRequest = cmsCtx.getServerInvocation().getServerContext().getClientRequest();
        String requestKey = "osivia.cache.spaceConfig." + publishSpacePath;

        try {

            CMSItem value = (CMSItem) portalRequest.getAttribute(requestKey);
            if (value != null) {
                // Has been reloaded since PageResfresh
                if (PageProperties.getProperties().isRefreshingPage()) {
                    if (portalRequest.getAttribute(requestKey + ".resfreshed") == null) {
                        portalRequest.setAttribute(requestKey + ".resfreshed", "1");
                        value = null;
                    }
                }
                if (value != null) {
                    return value;
                }
            }


            String savedScope = cmsCtx.getScope();
            String savedPubInfosScope = cmsCtx.getForcePublicationInfosScope();
            try {
                /*
                 * La mise en cache du résultat de cette méthode
                 * s'effectue de manière asynchrone.
                 */
                cmsCtx.setAsyncCacheRefreshing(true);
                cmsCtx.setForcePublicationInfosScope("superuser_context");

                configItem = this.fetchContent(cmsCtx, publishSpacePath);

                this.getCustomizer().getNavigationItemAdapter().adaptPublishSpaceNavigationItem(configItem, configItem);

                portalRequest.setAttribute(requestKey, configItem);

            } finally {
                cmsCtx.setScope(savedScope);
                cmsCtx.setAsyncCacheRefreshing(false);
                cmsCtx.setForcePublicationInfosScope(savedPubInfosScope);
            }

        } catch (Exception e) {
            if (!(e instanceof CMSException)) {
                if ((e instanceof NuxeoException) && (((NuxeoException) e).getErrorCode() == NuxeoException.ERROR_NOTFOUND)) {
                    return null;
                } else {
                    throw new CMSException(e);
                }
            } else {
                throw (CMSException) e;
            }
        }


        return configItem;
    }

    public Map<String, String> parseCMSURL(CMSServiceCtx cmsCtx, String requestPath, Map<String, String> requestParameters) throws CMSException {
        try {

            return this.customizer.parseCMSURL(cmsCtx, requestPath, requestParameters);
        } catch (Exception e) {
            if (!(e instanceof CMSException)) {
                if ((e instanceof NuxeoException) && (((NuxeoException) e).getErrorCode() == NuxeoException.ERROR_NOTFOUND)) {
                    return null;
                } else {
                    throw new CMSException(e);
                }
            } else {

                throw (CMSException) e;
            }
        }
    }



    public String adaptWebPathToCms(CMSServiceCtx cmsCtx, String requestPath) throws CMSException {
        try {

            // LBI : no need of customization

            CMSItem content = getContent(cmsCtx, requestPath);
            return content.getPath();

        } catch (Exception e) {
            if (!(e instanceof CMSException)) {
                if ((e instanceof NuxeoException) && (((NuxeoException) e).getErrorCode() == NuxeoException.ERROR_NOTFOUND)) {
                    return null;
                } else {
                    throw new CMSException(e);
                }
            } else {

                throw (CMSException) e;
            }
        }
    }


    public List<CMSPage> computeUserPreloadedPages(CMSServiceCtx cmsCtx) throws CMSException {
        try {

            return this.customizer.computeUserPreloadedPages(cmsCtx);
        } catch (Exception e) {
            if (!(e instanceof CMSException)) {
                if ((e instanceof NuxeoException) && (((NuxeoException) e).getErrorCode() == NuxeoException.ERROR_NOTFOUND)) {
                    return null;
                } else {
                    throw new CMSException(e);
                }
            } else {

                throw (CMSException) e;
            }
        }
    }

    /**
     * Création des fragments dans la page
     */
    public List<CMSEditableWindow> getEditableWindows(CMSServiceCtx cmsCtx, String pagePath) throws CMSException {
        try {

            CMSItem pageItem = this.fetchContent(cmsCtx, pagePath);

            List<CMSEditableWindow> windows = new ArrayList<CMSEditableWindow>();

            boolean editionMode = false;
            if ("1".equals(cmsCtx.getDisplayLiveVersion())) {
                editionMode = true;
            }

            Document doc = (Document) pageItem.getNativeItem();

            // Propriétés générales des fragments
            PropertyList pmFragmentsValues = doc.getProperties().getList(EditableWindowHelper.SCHEMA);

            if ((pmFragmentsValues != null) && !pmFragmentsValues.isEmpty()) {

                EditableWindowAdapter adapter = this.customizer.getEditableWindowAdapter();

                // Pour chaque fragment
                for (int fragmentIndex = 0; fragmentIndex < pmFragmentsValues.size(); fragmentIndex++) {

                    // Test de la catégorie
                    String fragmentCategory = (String) pmFragmentsValues.getMap(fragmentIndex).get(EditableWindowHelper.FGT_TYPE);
                    String uri = (String) pmFragmentsValues.getMap(fragmentIndex).get(EditableWindowHelper.FGT_URI);

                    EditableWindow ew = adapter.getType(fragmentCategory);

                    // EditableWindowTypeEnum type = EditableWindowTypeEnum.findByName(fragmentCategory);

                    if (ew != null) {

                        // Récupération d'une classe utilitaire se chargeant des traitements spécifiques à chaque fgt
                        // EditableWindowService ewService = type.getService();

                        // Valorisation des propriétés
                        Map<String, String> props = ew.fillProps(doc, pmFragmentsValues.getMap(fragmentIndex), editionMode);

                        // Construction de la window
                        windows.add(ew.createNewEditabletWindow(fragmentIndex, props));

                    }
                    // Si type de portlet non trouvé, erreur.
                    else {
                        logger.warn("Type de fragment " + fragmentCategory + " non géré");
                    }
                }
            }


            return windows;
        } catch (Exception e) {
            if (!(e instanceof CMSException)) {
                throw new CMSException(e);
            } else {

                throw (CMSException) e;
            }
        }

    }


    public void deleteFragment(CMSServiceCtx cmsCtx, String pagePath, String refURI) throws CMSException {

        cmsCtx.setDisplayLiveVersion("1");

        CMSItem cmsItem = this.getContent(cmsCtx, pagePath);
        Document doc = (Document) cmsItem.getNativeItem();

        // Propriétés générales des fragments
        PropertyList fragments = doc.getProperties().getList(EditableWindowHelper.SCHEMA);

        List<String> propertiesToRemove = null;
        if ((fragments != null) && !fragments.isEmpty()) {

            // Recherche du fragment
            for (int fragmentIndex = 0; fragmentIndex < fragments.size(); fragmentIndex++) {
                if (refURI.equals(fragments.getMap(fragmentIndex).get(EditableWindowHelper.FGT_URI))) {

                    String fragmentCategory = (String) fragments.getMap(fragmentIndex).get(EditableWindowHelper.FGT_TYPE);

                    EditableWindowAdapter adapter = this.customizer.getEditableWindowAdapter();
                    EditableWindow ew = adapter.getType(fragmentCategory);

                    if (ew != null) {

                        propertiesToRemove = ew.prepareDelete(doc, refURI);
                    }
                }
            }
        }


        try {
            if (propertiesToRemove != null) {

                Document docSaved = (Document) this.executeNuxeoCommand(cmsCtx, (new DocumentRemovePropertyCommand(doc, propertiesToRemove)));

                // On force le rechargement du cache
                cmsCtx.setForceReload(true);
                this.getContent(cmsCtx, pagePath);
                cmsCtx.setForceReload(false);
            }
        } catch (Exception e) {
            throw new CMSException(e);
        }


    }


    public String getEcmDomain(CMSServiceCtx cmsCtx) {
        return NuxeoConnectionProperties.getPublicDomainUri().toString();
    }


    public String getEcmUrl(CMSServiceCtx cmsCtx, EcmCommand command, String path, Map<String, String> requestParameters) throws CMSException {
        // get the défault domain and app name
        String uri = NuxeoConnectionProperties.getPublicBaseUri().toString();

        String url = "";

        if (command == EcmCommand.createPage) {
            url = uri.toString() + "/nxpath/default" + path + "@osivia_create_document?";
            requestParameters.put("type", "PortalPage");
        } else if (command == EcmCommand.createDocument) {
            url = uri.toString() + "/nxpath/default" + path + "@toutatice_create?";
        } else if (command == EcmCommand.editDocument) {
            url = uri.toString() + "/nxpath/default" + path + "@toutatice_edit?";
        } else if (command == EcmCommand.editPage) {
            url = uri.toString() + "/nxpath/default" + path + "@osivia_edit_document?";
        } else if (command == EcmCommand.createFgtInRegion) {
            url = uri.toString() + "/nxpath/default" + path + "@osivia_create_fragment?";
        } else if (command == EcmCommand.createFgtBelowWindow) {
            url = uri.toString() + "/nxpath/default" + path + "@osivia_create_fragment?";
        } else if (command == EcmCommand.editFgt) {
            url = uri.toString() + "/nxpath/default" + path + "@osivia_edit_fragment?";
        } else if (command == EcmCommand.viewSummary) {
            url = uri.toString() + "/nxpath/default" + path + "@view_documents?";
        } else if (command == EcmCommand.gotoMediaLibrary) {

            Document mediaLibrary;
            try {

                String baseDomainPath = "/".concat(path.split("/")[1]);
                mediaLibrary = (Document) this.executeNuxeoCommand(cmsCtx, (new DocumentGetMediaLibraryCommand(baseDomainPath)));


            } catch (Exception e) {
                throw new CMSException(e);
            }
            if (mediaLibrary != null) {
                url = uri.toString() + "/nxpath/default" + mediaLibrary.getPath() + "@view_documents?";
            } else {
                url = "";
            }
        }

        // params are used with fancyboxes
        if (command != EcmCommand.gotoMediaLibrary) {
            PortalControllerContext portalControllerContext = new PortalControllerContext(cmsCtx.getControllerContext());
            String portalUrl = this.getPortalUrlFactory().getBasePortalUrl(portalControllerContext);
            requestParameters.put("fromUrl", portalUrl);

            for (Map.Entry<String, String> param : requestParameters.entrySet()) {
                url = url.concat(param.getKey()).concat("=").concat(param.getValue()).concat("&");
            }
        }

        return url;
    }

    public void moveFragment(CMSServiceCtx cmsCtx, String pagePath, String fromRegion, Integer fromPos, String toRegion, Integer toPos, String refUri)
            throws CMSException {

        // On force le rechargement du cache
        cmsCtx.setForceReload(true);
        cmsCtx.setDisplayLiveVersion("1");

        CMSItem cmsItem = this.getContent(cmsCtx, pagePath);
        Document doc = (Document) cmsItem.getNativeItem();

        try {

            List<String> propertiesToUpdate = EditableWindowHelper.checkBeforeMove(doc, fromRegion, fromPos, refUri);
            if (propertiesToUpdate.size() > 0) {
                this.executeNuxeoCommand(cmsCtx, (new DocumentUpdatePropertiesCommand(doc, propertiesToUpdate)));

                CMSItem content = this.getContent(cmsCtx, pagePath);
                doc = (Document) content.getNativeItem();

            }


            propertiesToUpdate = EditableWindowHelper.prepareMove(doc, fromRegion, fromPos, toRegion, toPos, refUri);

            if (propertiesToUpdate.size() > 0) {

                this.executeNuxeoCommand(cmsCtx, (new DocumentUpdatePropertiesCommand(doc, propertiesToUpdate)));

                CMSItem content = this.getContent(cmsCtx, pagePath);
                Document docReloaded = (Document) content.getNativeItem();


            }
        } catch (Exception e) {
            throw new CMSException(e);
        } finally {
            cmsCtx.setForceReload(false);
        }
    }

    public boolean isCmsWebPage(CMSServiceCtx cmsCtx, String cmsPath) throws CMSException {

        // Une webpage CMS est porteuse du schéma fragment sous Nuxeo
        CMSItem content = this.getContent(cmsCtx, cmsPath);
        Document nativeItem = (Document) content.getNativeItem();
        PropertyList list = nativeItem.getProperties().getList(EditableWindowHelper.SCHEMA);

        if (list != null) {
            return true;
        } else {
            return false;
        }


    }

    public void publishDocument(CMSServiceCtx cmsCtx, String pagePath) throws CMSException {

        cmsCtx.setDisplayLiveVersion("1");

        CMSItem cmsItem = this.getContent(cmsCtx, pagePath);
        Document doc = (Document) cmsItem.getNativeItem();

        try {
            this.executeNuxeoCommand(cmsCtx, new SetOnLineCommand(doc));

            // On force le rechargement du cache de la page
            cmsCtx.setDisplayLiveVersion("0");
            cmsCtx.setForceReload(true);
            this.getContent(cmsCtx, pagePath);
            cmsCtx.setForceReload(false);


        } catch (Exception e) {
            throw new CMSException(e);
        }

    }


    public void unpublishDocument(CMSServiceCtx cmsCtx, String pagePath) throws CMSException {

        cmsCtx.setDisplayLiveVersion("1");

        CMSItem cmsItem = this.getContent(cmsCtx, pagePath);
        Document doc = (Document) cmsItem.getNativeItem();

        try {
            this.executeNuxeoCommand(cmsCtx, new SetOffLineCommand(doc));

            // On force le rechargement du cache de la page
            cmsCtx.setForceReload(true);
            this.getContent(cmsCtx, pagePath);
            cmsCtx.setForceReload(false);

        } catch (Exception e) {
            throw new CMSException(e);
        }

    }
    
    public void askToPublishDocument(CMSServiceCtx cmsCtx, String pagePath) throws CMSException {

        cmsCtx.setDisplayLiveVersion("1");

        CMSItem cmsItem = this.getContent(cmsCtx, pagePath);
        Document doc = (Document) cmsItem.getNativeItem();

        try {
            this.executeNuxeoCommand(cmsCtx, new AskSetOnLineCommand(doc));

         // On force le rechargement du cache de la page
            cmsCtx.setForceReload(true);
            this.getContent(cmsCtx, pagePath);
            cmsCtx.setForceReload(false);


        } catch (Exception e) {
            throw new CMSException(e);
        }

    }
    
    public void cancelPublishWorkflow(CMSServiceCtx cmsCtx, String pagePath) throws CMSException {

        cmsCtx.setDisplayLiveVersion("1");

        CMSItem cmsItem = this.getContent(cmsCtx, pagePath);
        Document doc = (Document) cmsItem.getNativeItem();

        try {
            this.executeNuxeoCommand(cmsCtx, new CancelWorkflowCommand(doc, "toutatice_online_approbation"));

         // On force le rechargement du cache de la page
            cmsCtx.setForceReload(true);
            this.getContent(cmsCtx, pagePath);
            cmsCtx.setForceReload(false);


        } catch (Exception e) {
            throw new CMSException(e);
        }

    }
    
    public void validatePublicationOfDocument(CMSServiceCtx cmsCtx, String pagePath) throws CMSException {
        callValidationCommand(cmsCtx, pagePath, true);
    }
    
    public void rejectPublicationOfDocument(CMSServiceCtx cmsCtx, String pagePath) throws CMSException {
        callValidationCommand(cmsCtx, pagePath, false);
    }

    /**
     * @param cmsCtx
     * @param pagePath
     * @throws CMSException
     */
    public void callValidationCommand(CMSServiceCtx cmsCtx, String pagePath, boolean accept) throws CMSException {
        cmsCtx.setDisplayLiveVersion("1");

        CMSItem cmsItem = this.getContent(cmsCtx, pagePath);
        Document doc = (Document) cmsItem.getNativeItem();

        try {
            this.executeNuxeoCommand(cmsCtx, new ValidationPublishCommand(doc, accept));

         // On force le rechargement du cache de la page
            cmsCtx.setForceReload(true);
            this.getContent(cmsCtx, pagePath);
            cmsCtx.setForceReload(false);


        } catch (Exception e) {
            throw new CMSException(e);
        }
    }

    public void deleteDocument(CMSServiceCtx cmsCtx, String pagePath) throws CMSException {

        cmsCtx.setDisplayLiveVersion("1");

        CMSItem cmsItem = this.getContent(cmsCtx, pagePath);
        Document doc = (Document) cmsItem.getNativeItem();

        try {
            this.executeNuxeoCommand(cmsCtx, new DocumentDeleteCommand(doc));

        } catch (Exception e) {
            throw new CMSException(e);
        }

    }

    public void putDocumentInTrash(CMSServiceCtx cmsCtx, String docId) throws CMSException {


        try {
            this.executeNuxeoCommand(cmsCtx, new PutInTrashDocumentCommand(docId));

        } catch (Exception e) {
            throw new CMSException(e);
        }

    }

    @Override
    public Link getUserAvatar(CMSServiceCtx cmsCtx, String username) throws CMSException {

        return customizer.getUserAvatar(cmsCtx, username);

    }

    @Override
    public String refreshUserAvatar(CMSServiceCtx cmsCtx, String username) {

        return customizer.refreshUserAvatar(cmsCtx, username);
    }

}
