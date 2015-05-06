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
package fr.toutatice.portail.cms.nuxeo.portlets.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.portlet.PortletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.portal.common.invocation.Scope;
import org.jboss.portal.core.aspects.server.UserInterceptor;
import org.jboss.portal.core.model.portal.Portal;
import org.jboss.portal.identity.User;
import org.jboss.portal.server.ServerInvocation;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.cache.services.CacheInfo;
import org.osivia.portal.api.cache.services.ICacheService;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.ecm.EcmCommand;
import org.osivia.portal.api.ecm.EcmViews;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.api.urls.Link;
import org.osivia.portal.core.cms.BinaryDelegation;
import org.osivia.portal.core.cms.BinaryDescription;
import org.osivia.portal.core.cms.CMSBinaryContent;
import org.osivia.portal.core.cms.CMSConfigurationItem;
import org.osivia.portal.core.cms.CMSEditableWindow;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSExtendedDocumentInfos;
import org.osivia.portal.core.cms.CMSHandlerProperties;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSItemType;
import org.osivia.portal.core.cms.CMSObjectPath;
import org.osivia.portal.core.cms.CMSPage;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.ICMSService;
import org.osivia.portal.core.cms.NavigationItem;
import org.osivia.portal.core.cms.RegionInheritance;
import org.osivia.portal.core.constants.InternalConstants;
import org.osivia.portal.core.page.PageProperties;
import org.osivia.portal.core.portalobjects.PortalObjectUtils;
import org.osivia.portal.core.profils.IProfilManager;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoCompatibility;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCommandService;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoService;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoServiceCommand;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandContext;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandServiceFactory;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoConnectionProperties;
import fr.toutatice.portail.cms.nuxeo.portlets.commands.DocumentFetchPublishedCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.commands.NuxeoCommandDelegate;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.DefaultCMSCustomizer;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.EditableWindowAdapter;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.WebConfigurationHelper;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.WebConfigurationQueryCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.WebConfigurationQueryCommand.WebConfigurationType;
import fr.toutatice.portail.cms.nuxeo.portlets.document.DocumentFetchLiveCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.document.FileContentCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.document.InternalPictureCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.document.PictureContentCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.document.PutInTrashDocumentCommand;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.AskSetOnLineCommand;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.CancelWorkflowCommand;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.DocumentAddComplexPropertyCommand;
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

        // Domain ID & web ID
        String domainId = doc.getString("ttc:domainID");
        String webId = doc.getString("ttc:webid");

        // CMS item
        CMSItem cmsItem = new CMSItem(path, domainId, webId, properties, doc);

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

        NuxeoCommandContext commandCtx = null;

        if (cmsCtx.getServerInvocation() != null) {
            commandCtx = new NuxeoCommandContext(this.portletCtx, cmsCtx.getServerInvocation());
        } else if (cmsCtx.getServletRequest() != null) {
            commandCtx = new NuxeoCommandContext(this.portletCtx, cmsCtx.getServletRequest());
        }

        if (commandCtx == null) {
            commandCtx = new NuxeoCommandContext(this.portletCtx);
        }

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
                    } else if ("superuser_no_cache".equals(scope)) {
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

            @Override
            public String getId() {
                return command.getId();
            }

            @Override
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


    @Override
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

    @Override
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
                if ((e instanceof NuxeoException) && (((NuxeoException) e).getErrorCode() == NuxeoException.ERROR_NOTFOUND)) {
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
            /*
             * Si un scope a été posé dans la portlet appelant la resource,
             * on applique celui-ci.
             */
            if (StringUtils.isNotEmpty(savedScope)) {
                cmsCtx.setForcePublicationInfosScope(savedScope);
            }
            CMSItem document = this.fetchContent(cmsCtx, docPath);

            if (document != null) {

                cmsCtx.setScope("superuser_context");

                FileContentCommand cmd = new FileContentCommand((Document) document.getNativeItem(), fieldName);

                if (cmsCtx.isStreamingSupport()) {
                    PropertyMap map = ((Document) document.getNativeItem()).getProperties().getMap("file:content");
                    if ((map != null) && !map.isEmpty()) {
                        String size = map.getString("length");


                        if ((size != null) && (Long.parseLong(size) > 100000L)) {
                            // Activation du mode streaming
                            cmd.setStreamingSupport(true);
                            // Pas de cache en mode streaming
                            cmsCtx.setScope("superuser_no_cache");
                        }
                    }
                }

                content = (CMSBinaryContent) this.executeNuxeoCommand(cmsCtx, (cmd));


            }
        } finally {
            cmsCtx.setScope(savedScope);
        }
        return content;
    }


    @Override
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


    @Override
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


    @SuppressWarnings("unchecked")
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


            while (pathToCheck.contains(publishSpaceConfig.getPath())) {
                NavigationItem navItem = navItems.get(pathToCheck);

                if ((navItem != null) && (fetchSubItems && navItem.isUnfetchedChildren())) {
                    Document doc = (Document) this.executeNuxeoCommand(cmsCtx, (new DocumentFetchLiveCommand(pathToCheck, "Read")));

                    if (!idsToFetch.contains(doc.getId())) {
                        idsToFetch.add(doc.getId());
                    }
                }

                  if (navItem == null) {
                    Document doc = (Document) this.executeNuxeoCommand(cmsCtx, (new DocumentFetchLiveCommand(pathToCheck, "Read")));
                    if (!idsToFetch.contains(doc.getId())) {
                        idsToFetch.add(doc.getId());
                    }
                }

                  CMSObjectPath parentPath = CMSObjectPath.parse(pathToCheck).getParent();
                  pathToCheck = parentPath.toString();

            }




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


    @SuppressWarnings("unchecked")
    @Override
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

            boolean forceLiveVersion = false;
            if ("1".equals(cmsCtx.getDisplayLiveVersion()) || "1".equals(publishSpaceConfig.getProperties().get("displayLiveVersion"))) {
                forceLiveVersion = true;
            }

            boolean canUseES = NuxeoCompatibility.canUseES() && BooleanUtils.toBoolean(publishSpaceConfig.getProperties().get("useES"));
            boolean refreshing = PageProperties.getProperties().isRefreshingPage();

            if ((!canUseES || (canUseES && refreshing)) && "1".equals(publishSpaceConfig.getProperties().get("partialLoading"))) {
                navItems = this.loadPartialNavigationTree(cmsCtx, publishSpaceConfig, path, false);
            } else {
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

    @SuppressWarnings("unchecked")
    @Override
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

            boolean forceLiveVersion = false;
            if ("1".equals(cmsCtx.getDisplayLiveVersion()) || "1".equals(publishSpaceConfig.getProperties().get("displayLiveVersion"))) {
                forceLiveVersion = true;
            }

            boolean canUseES = NuxeoCompatibility.canUseES() && BooleanUtils.toBoolean(publishSpaceConfig.getProperties().get("useES"));
            boolean refreshing = PageProperties.getProperties().isRefreshingPage();

            if ((!canUseES || (canUseES && refreshing)) && "1".equals(publishSpaceConfig.getProperties().get("partialLoading"))) {
                navItems = this.loadPartialNavigationTree(cmsCtx, publishSpaceConfig, path, true);
            } else {
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
                                item = this.createNavigationItem(cmsCtx, childNavPath, ((Document) navChild.getMainDoc()).getTitle(),
                                        (Document) navChild.getMainDoc(), publishSpacePath);
                                navChild.setAdaptedCMSItem(item);
                            }
                        }
                        if (item != null) {
                            item.getProperties().put("unfetchedChildren", BooleanUtils.toStringTrueFalse(navChild.isUnfetchedChildren()));
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
    @SuppressWarnings("unchecked")
    @Override
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
    @Override
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
                  * un autre mode par l'intermédiaire d'une vairiable du CMS Service
                  * Context (cas des méthodes getAnonymousContent(), getAttachedPicture()).
                  */
                if (StringUtils.isNotEmpty(ctx.getForcePublicationInfosScope())) {
                    ctx.setScope(ctx.getForcePublicationInfosScope());
                } else {
                    // In anonymous mode, publicationsInfos are shared
                    if (ctx.getServerInvocation() != null) {
                        ServerInvocation invocation = ctx.getServerInvocation();
                        User user = (User) invocation.getAttribute(Scope.PRINCIPAL_SCOPE, UserInterceptor.USER_KEY);
                        if (user == null) {
                            ctx.setScope("anonymous");
                        } else {
                            ctx.setScope("user_session");
                        }
                    }
                }

                String cmsReferrerNavigationPath = ctx.getCmsReferrerNavigationPath();
                String displayLiveVersion = ctx.getDisplayLiveVersion();

                pubInfos = (CMSPublicationInfos) this.executeNuxeoCommand(ctx, (new PublishInfosCommand(cmsReferrerNavigationPath, path, displayLiveVersion)));

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

    /**
     * {@inheritDoc}
     */
    @Override
    public CMSExtendedDocumentInfos getExtendedDocumentInfos(CMSServiceCtx ctx, String path) throws CMSException {

        CMSExtendedDocumentInfos docInfos = new CMSExtendedDocumentInfos();

        try {

            docInfos = (CMSExtendedDocumentInfos) this.executeNuxeoCommand(ctx, new ExtendedDocInfosCommand(path));

        } catch (NuxeoException e) {
            e.rethrowCMSException();
        } catch (Exception e) {

            if (!(e instanceof CMSException)) {
                throw new CMSException(e);
            } else {
                throw (CMSException) e;
            }
        }


        return docInfos;
    }

    @Override
    public CMSItem getSpaceConfig(CMSServiceCtx cmsCtx, String publishSpacePath) throws CMSException {
        CMSItem configItem = null;

        HttpServletRequest portalRequest = cmsCtx.getServerInvocation().getServerContext().getClientRequest();

        boolean forceLiveVersion = false;
        if ("1".equals(cmsCtx.getDisplayLiveVersion())) {
            forceLiveVersion = true;
        }

        String requestKey = "osivia.cache.spaceConfig." + publishSpacePath + "." + forceLiveVersion;


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
                // cmsCtx.setAsyncCacheRefreshing(true);
                cmsCtx.setForcePublicationInfosScope("superuser_context");

                configItem = this.fetchContent(cmsCtx, publishSpacePath);

                this.getCustomizer().getNavigationItemAdapter().adaptPublishSpaceNavigationItem(configItem, configItem);

                portalRequest.setAttribute(requestKey, configItem);

            } finally {
                cmsCtx.setScope(savedScope);
                // cmsCtx.setAsyncCacheRefreshing(false);
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

    @Override
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


    @Override
    public String adaptWebPathToCms(CMSServiceCtx cmsCtx, String requestPath) throws CMSException {
        try {

            // LBI : no need of customization

            CMSItem content = this.getContent(cmsCtx, requestPath);
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


    @Override
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
     * {@inheritDoc}
     */
    @Override
    public List<CMSEditableWindow> getEditableWindows(CMSServiceCtx cmsContext, String path, String publishSpacePath, String sitePath, String navigationScope)
            throws CMSException {
        List<CMSEditableWindow> windows = new ArrayList<CMSEditableWindow>();

        // Working path
        String workingPath;
        if (path == null) {
            workingPath = sitePath;
        } else {
            workingPath = path;
        }


        boolean editionMode = false;
        if ("1".equals(cmsContext.getDisplayLiveVersion())) {
            editionMode = true;
        }


        // Inherited regions
        Map<String, List<CMSEditableWindow>> inheritedRegions = this.getInheritedRegions(cmsContext, workingPath, publishSpacePath, sitePath, navigationScope,
                editionMode);
        for (List<CMSEditableWindow> inheritedWindows : inheritedRegions.values()) {
            if (CollectionUtils.isNotEmpty(inheritedWindows)) {
                // Add inherited region windows
                windows.addAll(inheritedWindows);
            }
        }
        int windowsCount = windows.size();


        try {
            // Fetch document
            CMSItem pageItem = this.fetchContent(cmsContext, workingPath);
            Document document = (Document) pageItem.getNativeItem();

            if (publishSpacePath != null) {
                // Fragments
                PropertyList fragments = document.getProperties().getList(EditableWindowHelper.SCHEMA_FRAGMENTS);
                if ((fragments != null) && !fragments.isEmpty()) {
                    EditableWindowAdapter adapter = this.customizer.getEditableWindowAdapter();

                    // Region windows count
                    int regionWindowsCount = 0;

                    // Loop
                    for (int i = 0; i < fragments.size(); i++) {
                        PropertyMap fragment = fragments.getMap(i);
                        String regionId = fragment.getString(EditableWindowHelper.REGION_IDENTIFIER);

                        if (inheritedRegions.get(regionId) == null) {
                            String category = fragment.getString(EditableWindowHelper.FGT_TYPE);

                            EditableWindow editableWindow = adapter.getType(category);
                            if (editableWindow != null) {
                                // Window creation
                                int windowId = windowsCount + regionWindowsCount;
                                Map<String, String> properties = editableWindow.fillProps(document, fragment, editionMode);
                                CMSEditableWindow window = editableWindow.createNewEditabletWindow(windowId, properties);
                                windows.add(window);
                                regionWindowsCount++;
                            } else {
                                // Si type de portlet non trouvé, erreur.
                                logger.warn("Type de fragment " + category + " non géré");
                            }
                        }
                    }
                }
            }
        } catch (CMSException e) {
            if (e.getErrorCode() == CMSException.ERROR_FORBIDDEN) {
                // Do nothing
            } else {
                throw e;
            }
        } catch (Exception e) {
            throw new CMSException(e);
        }

        return windows;
    }


    /**
     * Get inherited regions.
     *
     * @param cmsContext CMS context
     * @param path current page path
     * @param publishSpacePath publish space path
     * @param sitePath site path
     * @param navigationScope navigation scope
     * @param editionMode edition mode
     * @return inherited regions
     */
    private Map<String, List<CMSEditableWindow>> getInheritedRegions(CMSServiceCtx cmsContext, String path, String publishSpacePath, String sitePath,
            String navigationScope, boolean editionMode) {
        Map<String, List<CMSEditableWindow>> inheritedRegions = new HashMap<String, List<CMSEditableWindow>>();

        // CMS context
        CMSServiceCtx navCMSContext = new CMSServiceCtx();
        if (cmsContext.getControllerContext() != null) {
            navCMSContext.setControllerContext(cmsContext.getControllerContext());
        } else if (cmsContext.getServerInvocation() != null) {
            navCMSContext.setServerInvocation(cmsContext.getServerInvocation());
        }
        navCMSContext.setScope(navigationScope);

        // Window adapter
        EditableWindowAdapter adapter = this.customizer.getEditableWindowAdapter();


        // Overrided regions
        Set<String> overridedRegions = this.getPageOverridedRegions(cmsContext, path, publishSpacePath);

        // Window identifier
        int windowId = 0;


        String parentPath = CMSObjectPath.parse(path).getParent().toString();
        while (StringUtils.startsWith(parentPath, publishSpacePath)) {
            Map<String, List<CMSEditableWindow>> pagePropagatedRegions = this.getPagePropagatedRegions(navCMSContext, overridedRegions, windowId, adapter,
                    parentPath, publishSpacePath, editionMode);
            inheritedRegions.putAll(pagePropagatedRegions);
            overridedRegions.addAll(pagePropagatedRegions.keySet());

            // Window identifier increment
            for (List<CMSEditableWindow> windows : pagePropagatedRegions.values()) {
                if (CollectionUtils.isNotEmpty(windows)) {
                    windowId += windows.size();
                }
            }

            // Loop on parent path
            parentPath = CMSObjectPath.parse(parentPath).getParent().toString();
        }


        boolean directInheritance = (publishSpacePath != null) && (StringUtils.startsWith(path, sitePath));
        if (!directInheritance) {
            // Add defaut page propagated region windows in case of indirect inheritance
            Map<String, List<CMSEditableWindow>> pagePropagatedRegions = this.getPagePropagatedRegions(navCMSContext, overridedRegions, windowId, adapter,
                    sitePath, sitePath, editionMode);
            inheritedRegions.putAll(pagePropagatedRegions);
        }

        return inheritedRegions;
    }


    /**
     * Get page overrided regions.
     *
     * @param cmsContext CMS context
     * @param path current path
     * @param publishSpacePath publish space path
     * @return page overrided regions
     */
    private Set<String> getPageOverridedRegions(CMSServiceCtx cmsContext, String path, String publishSpacePath) {
        Set<String> overridedRegions = new TreeSet<String>();

        try {
            // Get navigation item
            CMSItem navItem = this.getPortalNavigationItem(cmsContext, publishSpacePath, path);

            if (navItem != null) {
                Map<String, RegionInheritance> regionsInheritance = this.getCMSRegionsInheritance(navItem);
                for (Entry<String, RegionInheritance> region : regionsInheritance.entrySet()) {
                    if (!RegionInheritance.DEFAULT.equals(region.getValue())) {
                        overridedRegions.add(region.getKey());
                    }
                }
            }
        } catch (Exception e) {
            // Do nothing
        }

        return overridedRegions;
    }


    /**
     * Get page propagated regions.
     *
     * @param cmsContext cmsContext
     * @param overridedRegions overrided regions
     * @param windowsCount windows count
     * @param adapter editable window adapter
     * @param path current path
     * @param publishSpacePath publish space path
     * @param editionMode edition mode indicator
     * @return page propagated regions
     */
    private Map<String, List<CMSEditableWindow>> getPagePropagatedRegions(CMSServiceCtx cmsContext, Set<String> overridedRegions, int windowsCount,
            EditableWindowAdapter adapter, String path, String publishSpacePath, boolean editionMode) {
        Map<String, List<CMSEditableWindow>> pagePropagatedRegions = new HashMap<String, List<CMSEditableWindow>>();

        try {
            // Get navigation item
            CMSItem navItem = this.getPortalNavigationItem(cmsContext, publishSpacePath, path);

            if (navItem != null) {
                Set<String> propagatedRegions = new HashSet<String>();
                Set<String> lockedRegions = new HashSet<String>();

                // Fetched document
                Document document = null;
                boolean fetched = false;

                // Current portal
                Portal portal = PortalObjectUtils.getPortal(cmsContext.getControllerContext());
                if (PortalObjectUtils.isSpaceSite(portal)) {
                    Map<String, RegionInheritance> inheritance = this.getCMSRegionsInheritance(navItem);

                    // Document regions loop
                    for (Entry<String, RegionInheritance> region : inheritance.entrySet()) {
                        if (RegionInheritance.LOCKED.equals(region.getValue())) {
                            // Locked
                            pagePropagatedRegions.put(region.getKey(), new ArrayList<CMSEditableWindow>());
                            propagatedRegions.add(region.getKey());
                            lockedRegions.add(region.getKey());
                        } else if (!overridedRegions.contains(region.getKey())) {
                            if (RegionInheritance.NO_INHERITANCE.equals(region.getValue())) {
                                // No inheritance
                                pagePropagatedRegions.put(region.getKey(), null);
                            } else if (RegionInheritance.PROPAGATED.equals(region.getValue())) {
                                // Propagation
                                pagePropagatedRegions.put(region.getKey(), new ArrayList<CMSEditableWindow>());
                                propagatedRegions.add(region.getKey());
                            }
                        }
                    }
                } else {
                    // Fetch
                    CMSItem item = this.fetchContent(cmsContext, path);
                    fetched = true;

                    if (item != null) {
                        document = (Document) item.getNativeItem();

                        // Document fragments loop
                        PropertyList fragments = document.getProperties().getList(EditableWindowHelper.SCHEMA_FRAGMENTS);
                        for (int i = 0; i < fragments.size(); i++) {
                            PropertyMap fragment = fragments.getMap(i);
                            String regionId = fragment.getString(EditableWindowHelper.REGION_IDENTIFIER);

                            if (!propagatedRegions.contains(regionId)) {
                                // Propagation
                                pagePropagatedRegions.put(regionId, new ArrayList<CMSEditableWindow>());
                                propagatedRegions.add(regionId);
                            }
                        }
                    }
                }


                if (!propagatedRegions.isEmpty()) {
                    if (!fetched) {
                        // Fetch
                        CMSItem item = this.fetchContent(cmsContext, path);
                        if (item != null) {
                            document = (Document) item.getNativeItem();
                        }
                    }

                    if (document != null) {
                        // Region windows count
                        int regionWindowsCount = 0;

                        // Document fragments loop
                        PropertyList fragments = document.getProperties().getList(EditableWindowHelper.SCHEMA_FRAGMENTS);
                        for (int i = 0; i < fragments.size(); i++) {
                            PropertyMap fragment = fragments.getMap(i);
                            String regionId = fragment.getString(EditableWindowHelper.REGION_IDENTIFIER);

                            if (propagatedRegions.contains(regionId)) {
                                String category = fragment.getString(EditableWindowHelper.FGT_TYPE);

                                EditableWindow editableWindow = adapter.getType(category);
                                if (editableWindow != null) {
                                    List<CMSEditableWindow> windows = pagePropagatedRegions.get(regionId);

                                    // Window creation
                                    int windowId = windowsCount + regionWindowsCount;
                                    Map<String, String> properties = editableWindow.fillProps(document, fragment, editionMode);
                                    properties.put(InternalConstants.INHERITANCE_INDICATOR_PROPERTY, String.valueOf(true));
                                    if (lockedRegions.contains(regionId)) {
                                        properties.put(InternalConstants.INHERITANCE_LOCKED_INDICATOR_PROPERTY, String.valueOf(true));
                                    }
                                    CMSEditableWindow window = editableWindow.createNewEditabletWindow(windowId, properties);
                                    windows.add(window);
                                    regionWindowsCount++;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Do nothing
        }

        return pagePropagatedRegions;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, RegionInheritance> getCMSRegionsInheritance(CMSItem item) {
        Map<String, RegionInheritance> regionsInheritance = new HashMap<String, RegionInheritance>();

        if (item != null) {
            Document document = (Document) item.getNativeItem();

            // Document regions loop
            PropertyList regions = document.getProperties().getList(EditableWindowHelper.SCHEMA_REGIONS);
            if (regions != null) {
                for (int i = 0; i < regions.size(); i++) {
                    PropertyMap region = regions.getMap(i);
                    String id = region.getString(EditableWindowHelper.REGION_IDENTIFIER);
                    RegionInheritance inheritance = RegionInheritance.fromValue(region.getString(EditableWindowHelper.INHERITANCE));

                    regionsInheritance.put(id, inheritance);
                }
            }
        }

        return regionsInheritance;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void saveCMSRegionInheritance(CMSServiceCtx cmsContext, String path, String regionName, RegionInheritance inheritance) throws CMSException {
        cmsContext.setDisplayLiveVersion("1");

        try {
            CMSItem cmsItem = this.getContent(cmsContext, path);
            Document document = (Document) cmsItem.getNativeItem();

            PropertyList regions = document.getProperties().getList(EditableWindowHelper.SCHEMA_REGIONS);
            if (regions != null) {
                // Updated properties
                List<String> properties = new ArrayList<String>();

                for (int i = 0; i < regions.size(); i++) {
                    PropertyMap region = regions.getMap(i);

                    if (StringUtils.equals(regionName, region.getString(EditableWindowHelper.REGION_IDENTIFIER))) {
                        StringBuilder builder = new StringBuilder();
                        builder.append(EditableWindowHelper.SCHEMA_REGIONS);
                        builder.append("/");
                        builder.append(i);
                        builder.append("/");
                        builder.append(EditableWindowHelper.INHERITANCE);
                        builder.append("=");
                        builder.append(StringUtils.trimToEmpty(inheritance.getValue()));

                        properties.add(builder.toString());

                        break;
                    }
                }

                // Nuxeo command
                INuxeoCommand command;
                if (properties.isEmpty()) {
                    // Add new property
                    Map<String, String> value = new HashMap<String, String>();
                    value.put(EditableWindowHelper.REGION_IDENTIFIER, regionName);
                    value.put(EditableWindowHelper.INHERITANCE, StringUtils.trimToEmpty(inheritance.getValue()));
                    command = new DocumentAddComplexPropertyCommand(document, EditableWindowHelper.SCHEMA_REGIONS, value);
                } else {
                    // Update existing property
                    command = new DocumentUpdatePropertiesCommand(document, properties);
                }

                this.executeNuxeoCommand(cmsContext, command);
            }
        } catch (CMSException e) {
            throw e;
        } catch (Exception e) {
            throw new CMSException(e);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Set<CMSConfigurationItem> getCMSRegionLayoutsConfigurationItems(CMSServiceCtx cmsContext) throws CMSException {
        // Domain path
        String domainPath = WebConfigurationHelper.getDomainPath(cmsContext);

        Set<CMSConfigurationItem> configurationItems = null;
        if (domainPath != null) {
            // Nuxeo command
            WebConfigurationQueryCommand command = new WebConfigurationQueryCommand(domainPath, WebConfigurationType.REGION_LAYOUT);

            try {
                Documents documents = WebConfigurationHelper.executeWebConfigCmd(cmsContext, this, command);

                configurationItems = new HashSet<CMSConfigurationItem>(documents.size());
                for (Document document : documents) {
                    PropertyMap properties = document.getProperties();
                    String code = properties.getString(WebConfigurationHelper.CODE);
                    String additionalCode = properties.getString(WebConfigurationHelper.ADDITIONAL_CODE);

                    CMSConfigurationItem configurationItem = new CMSConfigurationItem(document.getTitle(), code);
                    configurationItem.setAdditionalCode(additionalCode);
                    configurationItems.add(configurationItem);
                }
            } catch (Exception e) {
                // Do nothing
            }
        }
        return configurationItems;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, CMSConfigurationItem> getCMSRegionsSelectedLayout(CMSItem item, Set<CMSConfigurationItem> regionLayouts) throws CMSException {
        Map<String, CMSConfigurationItem> regionsLayout = new HashMap<String, CMSConfigurationItem>();

        if (item != null) {
            Document document = (Document) item.getNativeItem();

            // Document regions loop
            PropertyList regions = document.getProperties().getList(EditableWindowHelper.SCHEMA_REGIONS);
            if (regions != null) {
                for (int i = 0; i < regions.size(); i++) {
                    PropertyMap region = regions.getMap(i);
                    String id = region.getString(EditableWindowHelper.REGION_IDENTIFIER);
                    String regionLayoutCode = region.getString(EditableWindowHelper.REGION_LAYOUT);

                    // Find selected region layout
                    CMSConfigurationItem selectedRegionLayout = null;
                    for (CMSConfigurationItem regionLayout : regionLayouts) {
                        if (StringUtils.equals(regionLayout.getCode(), regionLayoutCode)) {
                            selectedRegionLayout = regionLayout;
                            break;
                        }
                    }

                    regionsLayout.put(id, selectedRegionLayout);
                }
            }
        }

        return regionsLayout;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void saveCMSRegionSelectedLayout(CMSServiceCtx cmsContext, String path, String regionName, String regionLayoutName) throws CMSException {
        cmsContext.setDisplayLiveVersion("1");

        try {
            CMSItem cmsItem = this.getContent(cmsContext, path);
            Document document = (Document) cmsItem.getNativeItem();

            PropertyList regions = document.getProperties().getList(EditableWindowHelper.SCHEMA_REGIONS);
            if (regions != null) {
                // Updated properties
                List<String> properties = new ArrayList<String>();

                for (int i = 0; i < regions.size(); i++) {
                    PropertyMap region = regions.getMap(i);

                    if (StringUtils.equals(regionName, region.getString(EditableWindowHelper.REGION_IDENTIFIER))) {
                        StringBuilder builder = new StringBuilder();
                        builder.append(EditableWindowHelper.SCHEMA_REGIONS);
                        builder.append("/");
                        builder.append(i);
                        builder.append("/");
                        builder.append(EditableWindowHelper.REGION_LAYOUT);
                        builder.append("=");
                        builder.append(StringUtils.trimToEmpty(regionLayoutName));

                        properties.add(builder.toString());

                        break;
                    }
                }

                // Nuxeo command
                INuxeoCommand command;
                if (properties.isEmpty()) {
                    // Add new property
                    Map<String, String> value = new HashMap<String, String>();
                    value.put(EditableWindowHelper.REGION_IDENTIFIER, regionName);
                    value.put(EditableWindowHelper.REGION_LAYOUT, StringUtils.trimToEmpty(regionLayoutName));
                    command = new DocumentAddComplexPropertyCommand(document, EditableWindowHelper.SCHEMA_REGIONS, value);
                } else {
                    // Update existing property
                    command = new DocumentUpdatePropertiesCommand(document, properties);
                }

                this.executeNuxeoCommand(cmsContext, command);
            }
        } catch (CMSException e) {
            throw e;
        } catch (Exception e) {
            throw new CMSException(e);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteFragment(CMSServiceCtx cmsCtx, String pagePath, String refURI) throws CMSException {

        cmsCtx.setDisplayLiveVersion("1");

        CMSItem cmsItem = this.getContent(cmsCtx, pagePath);
        Document doc = (Document) cmsItem.getNativeItem();

        // Propriétés générales des fragments
        PropertyList fragments = doc.getProperties().getList(EditableWindowHelper.SCHEMA_FRAGMENTS);

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

                this.executeNuxeoCommand(cmsCtx, (new DocumentRemovePropertyCommand(doc, propertiesToRemove)));

                // On force le rechargement du cache
                cmsCtx.setForceReload(true);
                this.getContent(cmsCtx, pagePath);
                cmsCtx.setForceReload(false);
            }
        } catch (Exception e) {
            throw new CMSException(e);
        }


    }


    @Override
    public String getEcmDomain(CMSServiceCtx cmsCtx) {
        return NuxeoConnectionProperties.getPublicDomainUri().toString();
    }


    @Override
    public String getEcmUrl(CMSServiceCtx cmsCtx, EcmViews command, String path, Map<String, String> requestParameters) throws CMSException {
        // get the défault domain and app name
        String uri = NuxeoConnectionProperties.getPublicBaseUri().toString();

        String url = "";

        if (command == EcmViews.createPage) {
            url = uri.toString() + "/nxpath/default" + path + "@osivia_create_document?";
            requestParameters.put("type", "PortalPage");
        } else if (command == EcmViews.createDocument) {
            url = uri.toString() + "/nxpath/default" + path + "@toutatice_create?";
        } else if (command == EcmViews.editDocument) {
            url = uri.toString() + "/nxpath/default" + path + "@toutatice_edit?";
        } else if (command == EcmViews.editPage) {
            url = uri.toString() + "/nxpath/default" + path + "@osivia_edit_document?";
        } else if (command == EcmViews.createFgtInRegion) {
            url = uri.toString() + "/nxpath/default" + path + "@osivia_create_fragment?";
        } else if (command == EcmViews.createFgtBelowWindow) {
            url = uri.toString() + "/nxpath/default" + path + "@osivia_create_fragment?";
        } else if (command == EcmViews.editFgt) {
            url = uri.toString() + "/nxpath/default" + path + "@osivia_edit_fragment?";
        } else if (command == EcmViews.viewSummary) {
            url = uri.toString() + "/nxpath/default" + path + "@view_documents?";
        } else if (command == EcmViews.shareDocument) {
        	url = uri.toString() + "/nxpath/default" + path + "@send_notification_email?";
        } else if (command == EcmViews.gotoMediaLibrary) {

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
        if (command != EcmViews.gotoMediaLibrary) {
            PortalControllerContext portalControllerContext = new PortalControllerContext(cmsCtx.getControllerContext());
            String portalUrl = this.getPortalUrlFactory().getBasePortalUrl(portalControllerContext);
            requestParameters.put("fromUrl", portalUrl);

            for (Map.Entry<String, String> param : requestParameters.entrySet()) {
                url = url.concat(param.getKey()).concat("=").concat(param.getValue()).concat("&");
            }
        }

        return url;
    }

    @Override
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

                // Reload content
                this.getContent(cmsCtx, pagePath);
            }
        } catch (Exception e) {
            throw new CMSException(e);
        } finally {
            cmsCtx.setForceReload(false);
        }
    }

    @Override
    public boolean isCmsWebPage(CMSServiceCtx cmsCtx, String cmsPath) throws CMSException {

        // Une webpage CMS est porteuse du schéma fragment sous Nuxeo
        CMSItem content = this.getContent(cmsCtx, cmsPath);
        Document nativeItem = (Document) content.getNativeItem();
        PropertyList list = nativeItem.getProperties().getList(EditableWindowHelper.SCHEMA_FRAGMENTS);

        if (list != null) {
            return true;
        } else {
            return false;
        }


    }

    @Override
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

            // force reload ressources
            this.refreshBinaryResource(cmsCtx, pagePath);


        } catch (Exception e) {
            throw new CMSException(e);
        }

    }


    @Override
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

    @Override
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

    @Override
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

    @Override
    public void validatePublicationOfDocument(CMSServiceCtx cmsCtx, String pagePath) throws CMSException {
        this.callValidationCommand(cmsCtx, pagePath, true);
    }

    @Override
    public void rejectPublicationOfDocument(CMSServiceCtx cmsCtx, String pagePath) throws CMSException {
        this.callValidationCommand(cmsCtx, pagePath, false);
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


    /**
     * {@inheritDoc}
     */
    @Override
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


    /**
     * {@inheritDoc}
     */
    @Override
    public void putDocumentInTrash(CMSServiceCtx cmsCtx, String docId) throws CMSException {
        try {
            this.executeNuxeoCommand(cmsCtx, new PutInTrashDocumentCommand(docId));
        } catch (Exception e) {
            throw new CMSException(e);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Link getUserAvatar(CMSServiceCtx cmsCtx, String username) throws CMSException {
        return this.customizer.getUserAvatar(cmsCtx, username);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String refreshUserAvatar(CMSServiceCtx cmsCtx, String username) {
        return this.customizer.refreshUserAvatar(cmsCtx, username);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Link getBinaryResourceURL(CMSServiceCtx cmsCtx, BinaryDescription binary) throws CMSException {
        return this.customizer.getBinaryResourceURL(cmsCtx, binary);
    }


    @Override
    public BinaryDelegation validateBinaryDelegation(CMSServiceCtx cmsCtx, String path) {
        return this.customizer.validateBinaryDelegation(cmsCtx, path);

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String refreshBinaryResource(CMSServiceCtx cmsCtx, String path) {
        return this.customizer.refreshBinaryResource(cmsCtx, path);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void executeEcmCommand(CMSServiceCtx cmsCtx, EcmCommand command, String cmsPath) throws CMSException {

        cmsCtx.setDisplayLiveVersion("1");

        CMSItem cmsItem = this.getContent(cmsCtx, cmsPath);
        Document doc = (Document) cmsItem.getNativeItem();

        try {
        	
        	this.executeNuxeoCommand(cmsCtx, new NuxeoCommandDelegate(command, doc));


            // On force le rechargement du cache de la page
            cmsCtx.setDisplayLiveVersion("0");
            cmsCtx.setForceReload(true);
            this.getContent(cmsCtx, cmsPath);
            cmsCtx.setForceReload(false);


        } catch (Exception e) {
            throw new CMSException(e);
        }
    }



}
