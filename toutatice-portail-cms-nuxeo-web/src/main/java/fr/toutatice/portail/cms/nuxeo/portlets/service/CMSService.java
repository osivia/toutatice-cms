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

import fr.toutatice.portail.cms.nuxeo.api.*;
import fr.toutatice.portail.cms.nuxeo.api.cms.NuxeoDocumentContext;
import fr.toutatice.portail.cms.nuxeo.api.domain.EditableWindow;
import fr.toutatice.portail.cms.nuxeo.api.domain.EditableWindowHelper;
import fr.toutatice.portail.cms.nuxeo.api.domain.INavigationAdapterModule;
import fr.toutatice.portail.cms.nuxeo.api.forms.IFormsService;
import fr.toutatice.portail.cms.nuxeo.api.services.*;
import fr.toutatice.portail.cms.nuxeo.portlets.binaries.FetchByShareLinkCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.cms.ExtendedDocumentInfos;
import fr.toutatice.portail.cms.nuxeo.portlets.cms.NuxeoDocumentContextImpl;
import fr.toutatice.portail.cms.nuxeo.portlets.commands.DocumentFetchPublishedCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.commands.NuxeoCommandDelegate;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.CustomizationPluginMgr;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.DefaultCMSCustomizer;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.BrowserAdapter;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.WebConfigurationHelper;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.WebConfigurationQueryCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.WebConfigurationQueryCommand.WebConfigurationType;
import fr.toutatice.portail.cms.nuxeo.portlets.document.*;
import fr.toutatice.portail.cms.nuxeo.portlets.document.helpers.DocumentHelper;
import fr.toutatice.portail.cms.nuxeo.portlets.forms.ViewProcedurePortlet;
import fr.toutatice.portail.cms.nuxeo.portlets.move.MoveDocumentPortlet;
import fr.toutatice.portail.cms.nuxeo.portlets.publish.RequestPublishStatus;
import fr.toutatice.portail.cms.nuxeo.portlets.reorder.ReorderDocumentsPortlet;
import fr.toutatice.portail.cms.nuxeo.portlets.sharing.ResolveSharingLinkCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.statistics.StatisticsCmsServiceDelegation;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.portal.common.invocation.Scope;
import org.jboss.portal.core.aspects.server.UserInterceptor;
import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.core.model.portal.Portal;
import org.jboss.portal.core.model.portal.PortalObjectPath;
import org.jboss.portal.identity.User;
import org.jboss.portal.server.ServerInvocation;
import org.jboss.portal.theme.ThemeConstants;
import org.jboss.portal.theme.impl.render.dynamic.DynaRenderOptions;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.cache.services.CacheInfo;
import org.osivia.portal.api.cache.services.ICacheService;
import org.osivia.portal.api.cms.*;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.directory.v2.DirServiceFactory;
import org.osivia.portal.api.directory.v2.model.Group;
import org.osivia.portal.api.directory.v2.service.GroupService;
import org.osivia.portal.api.directory.v2.service.PersonService;
import org.osivia.portal.api.ecm.EcmCommand;
import org.osivia.portal.api.ecm.EcmViews;
import org.osivia.portal.api.editor.EditorModule;
import org.osivia.portal.api.editor.EditorService;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.menubar.MenubarModule;
import org.osivia.portal.api.page.PageParametersEncoder;
import org.osivia.portal.api.panels.PanelPlayer;
import org.osivia.portal.api.player.Player;
import org.osivia.portal.api.statistics.SpaceStatistics;
import org.osivia.portal.api.taskbar.*;
import org.osivia.portal.api.tasks.TaskModule;
import org.osivia.portal.api.theming.TabGroup;
import org.osivia.portal.api.theming.TemplateAdapter;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.api.urls.Link;
import org.osivia.portal.api.urls.PortalUrlType;
import org.osivia.portal.core.cms.*;
import org.osivia.portal.core.constants.InternalConstants;
import org.osivia.portal.core.context.ControllerContextAdapter;
import org.osivia.portal.core.page.PageProperties;
import org.osivia.portal.core.portalobjects.PortalObjectUtils;
import org.osivia.portal.core.profils.IProfilManager;
import org.osivia.portal.core.utils.URLUtils;
import org.osivia.portal.core.web.IWebIdService;

import javax.naming.Name;
import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.ResourceRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 * CMS service Toutatice implementation.
 *
 * @see ICMSService
 */
public class CMSService implements ICMSService {

    /**
     * Extended document informations request attribute prefix.
     */
    private static final String EXTENDED_DOCUMENT_INFOS_ATTRIBUTE_PREFIX = "osivia.cms.extendedDocumentInfos.";


    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(CMSService.class);

    /**
     * Slash separator.
     */
    private static final String SLASH = "/";


    /**
     * Portlet context.
     */
    private final PortletContext portletCtx;
    /**
     * Taskbar service.
     */
    private final ITaskbarService taskbarService;
    /**
     * Forms service.
     */
    private final IFormsService formsService;
    /**
     * Person service.
     */
    private final PersonService personService;
    /**
     * Directory group service.
     */
    private final GroupService groupService;
    /**
     * Documents discovery service.
     */
    private final DocumentsDiscoveryService documentsDiscoveryService;
    /**
     * Statistics CMS service delegation.
     */
    private final StatisticsCmsServiceDelegation statisticsServiceDelegation;
    /**
     * Statistics CMS service delegation.
     */
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

        this.taskbarService = Locator.findMBean(ITaskbarService.class, ITaskbarService.MBEAN_NAME);
        this.formsService = NuxeoServiceFactory.getFormsService();
        this.personService = DirServiceFactory.getService(PersonService.class);
        this.groupService = DirServiceFactory.getService(GroupService.class);
        this.documentsDiscoveryService = DocumentsDiscoveryService.getInstance(this);
        this.statisticsServiceDelegation = new StatisticsCmsServiceDelegation();
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
     * @param cmsCtx      CMS context
     * @param path        CMS path
     * @param displayName display name
     * @param doc         Nuxeo document
     * @return CMS item
     * @throws CMSException
     */
    public CMSItem createItem(CMSServiceCtx cmsCtx, String path, String displayName, Document doc) throws CMSException {
        // CMS item properties
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("displayName", displayName);
        properties.put("type", doc.getType());
        if (BooleanUtils.toBoolean(doc.getString("ttc:showInMenu"))) {
            properties.put("menuItem", "1");
        }

        // Domain ID & web ID
        String domainId = doc.getString("ttc:domainID");
        String webId = doc.getString("ttc:webid");

        // For selectors saved in the doc
        if (doc.getString("ttc:selectors") != null) {
            try {
                PageParametersEncoder.decodeProperties(doc.getString("ttc:selectors"));
                properties.put("selectors", doc.getString("ttc:selectors"));
            } catch (Throwable t) {
                final Locale locale = cmsCtx.getServerInvocation().getServerContext().getClientRequest().getLocale();
                final String warnMsgselectors = getCustomizer().getBundleFactory().getBundle(locale).getString("WARN_MSG_TTC_SELECTORS");
                LOG.warn(warnMsgselectors, t);
            }
        }


        // CMS item
        CMSItem cmsItem = new CMSItem(path, domainId, webId, properties, doc);
        cmsItem.setCmsPath(doc.getPath());

        // CMS item type
        DocumentType type = this.customizer.getCMSItemTypes().get(doc.getType());
        cmsItem.setType(type);

        return cmsItem;
    }


    /**
     * Create CMS item.
     *
     * @param cmsContext       CMS context
     * @param path             path
     * @param displayName      display name
     * @param document         document
     * @param publicationInfos publications infos
     * @return CMS item
     * @throws CMSException
     */
    public CMSItem createItem(CMSServiceCtx cmsContext, String path, String displayName, Document document, CMSPublicationInfos publicationInfos)
            throws CMSException {
        // CMS item
        CMSItem cmsItem = this.createItem(cmsContext, path, displayName, document);

        // Publication infos
        if (publicationInfos != null) {
            cmsItem.setPublished(publicationInfos.isPublished());
            cmsItem.setBeingModified(publicationInfos.isBeingModified());
        }

        return cmsItem;
    }


    /**
     * Create CMS navigation item.
     *
     * @param cmsCtx           CMS context
     * @param path             CMS path
     * @param displayName      display name
     * @param document         Nuxeo document
     * @param publishSpacePath publish space path
     * @return CMS navigation item
     * @throws CMSException
     */
    public CMSItem createNavigationItem(CMSServiceCtx cmsCtx, String path, String displayName, Document document, String publishSpacePath) throws CMSException {
        CMSItem cmsItem = this.createItem(cmsCtx, path, displayName, document);
        CMSItem publishSpaceItem = null;

        if ((publishSpacePath != null) && !path.equals(publishSpacePath)) {
            publishSpaceItem = this.getPortalNavigationItem(cmsCtx, publishSpacePath, publishSpacePath);
        } else {
            publishSpaceItem = cmsItem;
        }

        this.getCustomizer().getNavigationItemAdapter().adaptPublishSpaceNavigationItem(cmsItem, publishSpaceItem);

        // Plugin manager
        CustomizationPluginMgr pluginManager = this.customizer.getPluginManager();

        List<INavigationAdapterModule> navigationAdapters = pluginManager.customizeNavigationAdapters();
        if (CollectionUtils.isNotEmpty(navigationAdapters)) {
            PortalControllerContext portalControllerContext = new PortalControllerContext(cmsCtx.getControllerContext());

            for (INavigationAdapterModule navigationAdapter : navigationAdapters) {
                navigationAdapter.adaptNavigationItem(portalControllerContext, cmsItem);
            }
        }

        return cmsItem;
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
                } else if ("anonymous".equals(scope)) {
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

        commandCtx.setSatellite(cmsCtx.getSatellite());


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


    /**
     * Fetch content.
     *
     * @param cmsContext CMS context
     * @param path       path
     * @return CMS item
     * @throws Exception
     */
    private CMSItem fetchContent(CMSServiceCtx cmsContext, String path) throws Exception {
        // CMS item
        CMSItem cmsItem;

        // Saved scope
        String savedScope = cmsContext.getScope();
        try {
            boolean saveAsync = cmsContext.isAsyncCacheRefreshing();

            cmsContext.setAsyncCacheRefreshing(false);

            // Publication infos
            CMSPublicationInfos publicationInfos = this.getPublicationInfos(cmsContext, path);
            path = publicationInfos.getDocumentPath();

            cmsContext.setAsyncCacheRefreshing(saveAsync);

            Satellite savedSatellite = cmsContext.getSatellite();
            try {

                Satellite satellite = publicationInfos.getSatellite();
                cmsContext.setSatellite(satellite);

                boolean haveToGetLive = "1".equals(cmsContext.getDisplayLiveVersion());

                if (publicationInfos.getDocumentPath().equals(cmsContext.getForcedLivePath())
                        || publicationInfos.getLiveId().equals(cmsContext.getForcedLivePath())) {
                    haveToGetLive = true;
                }

                // Document non publié et rattaché à un workspace
                if ((!publicationInfos.isPublished() && StringUtils.isNotEmpty(publicationInfos.getPublishSpacePath())
                        && publicationInfos.isLiveSpace())) {
                    haveToGetLive = true;
                }

                // Ajout JSS 20130122
                // Document non publié et non rattaché à un espace : usage collaboratif
                if (!publicationInfos.isPublished() && (publicationInfos.getPublishSpacePath() == null)) {
                    haveToGetLive = true;
                }

                cmsContext.setScope("superuser_context");

                // Nuxeo command
                INuxeoCommand nuxeoCommand;
                if (haveToGetLive) {
                    nuxeoCommand = new DocumentFetchLiveCommand(path, "Read");
                } else {
                    nuxeoCommand = new DocumentFetchPublishedCommand(path);
                }

                // Document
                Document document = (Document) this.executeNuxeoCommand(cmsContext, nuxeoCommand);
                // CMS item
                cmsItem = this.createItem(cmsContext, path, document.getTitle(), document, publicationInfos);
                
 
           
                
                
            } finally {
                cmsContext.setSatellite(savedSatellite);
            }
        } finally {
            cmsContext.setScope(savedScope);
        }

        return cmsItem;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public CMSItem getContent(CMSServiceCtx cmsContext, String path) throws CMSException {
        // Content
        CMSItem content = null;

        try {
            // Fetch content
            content = this.fetchContent(cmsContext, path);

            // Force portal contextualization indicator
            DocumentType type = content.getType();
            if ((type != null) && type.isForceContextualization()) {
                content.getProperties().put("supportsOnlyPortalContextualization", "1");
            }
            
            
            // In many UI (move, more criterias, filebrowser, task, ...)
            // the /documents folder's document title is supposed to contain "Mes documents"
            // So we use the navigation adapter ....
            
            if (BooleanUtils.toBoolean(System.getProperty("osivia.services.userWorkSpace.adaptDocumentFolder"))) {

                // Not supported yet for web-service
                if (cmsContext.getControllerContext() != null) {
                    
                    if (content != null && content.getNativeItem() instanceof Document) {
                        Document doc = (Document) content.getNativeItem();


                        if ((content.getType() != null && "Folder".equals(content.getType().getName())
                                && StringUtils.endsWith(content.getCmsPath(), "/documents"))) {

                            if (!BooleanUtils.toBoolean(doc.getProperties().getString("ttc:adaptedContent"))) {
                                try {
                                    // Plugin manager
                                    CustomizationPluginMgr pluginManager = this.customizer.getPluginManager();
                                    // Navigation adapters
                                    List<INavigationAdapterModule> navigationAdapters = pluginManager.customizeNavigationAdapters();

                                    PortalControllerContext portalControllerContext = new PortalControllerContext(cmsContext.getControllerContext());

                                    // Navigation adapters
                                    for (INavigationAdapterModule navigationAdapter : navigationAdapters) {
                                        navigationAdapter.adaptNavigationItem(portalControllerContext, content);
                                    }
                                } finally {
                                    doc.set("ttc:adaptedContent", "true");
                                }
                            }
                        }
                    }
                }
            }
            
             
            
            
        } catch (NuxeoException e) {
            e.rethrowCMSException();
        } catch (CMSException e) {
            throw e;
        } catch (Exception e) {
            throw new CMSException(e);
        }

        return content;
    }


    @Override
    public CMSItem getByShareId(CMSServiceCtx cmsContext, String shareId, boolean enabledLinkOnly) throws CMSException {
        // Content
        CMSItem content = null;
        // Saved scope
        String savedScope = cmsContext.getScope();

        boolean ignoreError = false;
        try {

            cmsContext.setScope("superuser_no_cache");

            // Document
            Documents docs = (Documents) this.executeNuxeoCommand(cmsContext, new FetchByShareLinkCommand(shareId, enabledLinkOnly));

            if (docs.size() > 1) {

                LOG.error(docs.size() + " documents whith share Id : " + shareId);

                if (docs.size() == 2) {
                    // A bad manipulation from the administrator who has duplicated the linkId
                    // We delete the link in the newer document ...
                    try {
                        LOG.info("deactivate link for " + docs.get(1).getPath());

                        List<String> propertiesToRemove = new ArrayList<>();
                        propertiesToRemove.add("rshr:linkId");
                        propertiesToRemove.add("rshr:enabledLink");
                        this.executeNuxeoCommand(cmsContext, (new DocumentRemovePropertyCommand(docs.get(1), propertiesToRemove)));

                        ignoreError = true;
                    } catch (Exception e) {
                        throw new CMSException(e);
                    }
                }
            }

            if (docs.size() != 1 && !ignoreError) {
                throw new NuxeoException(NuxeoException.ERROR_NOTFOUND);
            }

            content = new CMSItem(docs.get(0).getPath(), null, null, null, docs.get(0));


        } catch (NuxeoException e) {
            e.rethrowCMSException();
        } catch (CMSException e) {
            throw e;
        } catch (Exception e) {
            throw new CMSException(e);
        } finally {
            cmsContext.setScope(savedScope);
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

                Satellite savedSatellite = setSatelliteInContext(cmsCtx, docPath);

                try {

                    cmsCtx.setScope("superuser_context");

                    pictureContent = (CMSBinaryContent) this.executeNuxeoCommand(cmsCtx, (new InternalPictureCommand((Document) containerDoc.getNativeItem(),
                            pictureIndex)));
                } finally {
                    restoreSatelliteInContext(cmsCtx, savedSatellite);
                }
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


    private CMSBinaryContent fetchFileContent(CMSServiceCtx cmsContext, String path, String fieldName) throws Exception {
        CMSBinaryContent content;

        // Saved scope
        String savedScope = cmsContext.getScope();
        if (StringUtils.isNotEmpty(savedScope)) {
            cmsContext.setForcePublicationInfosScope(savedScope);
        }

        try {
            // Document
            CMSItem document;
            // Version
            Document nuxeoDocument = null;
            //try {
            if (!"downloadVersion".equals(cmsContext.getDisplayContext())) {
                document = this.fetchContent(cmsContext, path);
                if (document != null) {
                    // Nuxeo document
                    nuxeoDocument = (Document) document.getNativeItem();
                }
            } else {
                // Version
                FetchDocumentByUUIDCommand fetchVersion = new FetchDocumentByUUIDCommand(path);
                cmsContext.setScope("superuser_context");
                nuxeoDocument = (Document) this.executeNuxeoCommand(cmsContext, fetchVersion);
            }
            //} catch (Exception e) {
            //    LOG.error(e.getMessage(), e);
            //    throw e;
            //}


            // File content
            if (nuxeoDocument != null) {

                Satellite savedSatellite = setSatelliteInContext(cmsContext, path);

                try {

                    // Command
                    FileContentCommand command = new FileContentCommand(nuxeoDocument, fieldName);

                    cmsContext.setScope("superuser_context");

                    if (cmsContext.isStreamingSupport()) {

                        PropertyMap map = FileContentCommand.getFileMap(nuxeoDocument, fieldName);

                        if (map != null) {
                            Long length = map.getLong("length");
                            if ((length != null) && (length > (100 * 1024l))) {
                                command.setStreamingSupport(true);
                                cmsContext.setScope("superuser_no_cache");
                            }
                        }
                    }


                    content = (CMSBinaryContent) this.executeNuxeoCommand(cmsContext, command);
                } finally {
                    restoreSatelliteInContext(cmsContext, savedSatellite);
                }
            } else {
                content = null;
            }
        } finally {
            cmsContext.setScope(savedScope);
        }

        return content;
    }


    /**
     * Computes the current satellite and save it into context
     *
     * @param cmsContext
     * @param path
     * @return
     * @throws CMSException
     */

    private Satellite setSatelliteInContext(CMSServiceCtx cmsContext, String path) throws CMSException {
        CMSPublicationInfos publicationInfos = this.getPublicationInfos(cmsContext, path);

        Satellite savedSatellite = cmsContext.getSatellite();

        Satellite satellite = publicationInfos.getSatellite();
        cmsContext.setSatellite(satellite);
        return savedSatellite;
    }

    /**
     * Restore previous satellite into context.
     *
     * @param cmsContext CMS context
     */
    private void restoreSatelliteInContext(CMSServiceCtx cmsContext, Satellite oldSatellite) {
        cmsContext.setSatellite(oldSatellite);
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


    /**
     * {@inheritDoc}
     */
    @Override
    public Player getItemHandler(CMSServiceCtx cmsContext) throws CMSException {
        // Player
        Player player;
        try {
            if (!"detailedView".equals(cmsContext.getDisplayContext())) {
                player = this.customizer.getCMSPlayer(cmsContext);
            } else {
                player = this.customizer.getCMSDefaultPlayer(cmsContext);
            }
        } catch (NuxeoException e) {
            player = null;
            e.rethrowCMSException();
        } catch (CMSException e) {
            throw e;
        } catch (Exception e) {
            throw new CMSException(e);
        }

        return player;
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


            String cacheId = "partial_navigation_tree/" + publishSpaceConfig.getNavigationPath();
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


            boolean isParent = false;

            while (pathToCheck.contains(publishSpaceConfig.getNavigationPath())) {
                NavigationItem navItem = navItems.get(pathToCheck);


                if ((navItem != null) && ((fetchSubItems || isParent) && navItem.isUnfetchedChildren())) {
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

                isParent = true;

            }


            if ((idsToFetch.size() > 0) || fetchRoot) {
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


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public CMSItem getPortalNavigationItem(CMSServiceCtx cmsCtx, String publishSpacePath, String path) throws CMSException {
        String savedScope = cmsCtx.getScope();

        if ((cmsCtx.getScope() == null) || "__nocache".equals(cmsCtx.getScope())) {
            cmsCtx.setScope("user_session");
        }

        try {
            String livePath = DocumentHelper.computeNavPath(path);

            CMSItem publishSpaceConfig = this.getSpaceConfig(cmsCtx, publishSpacePath);

            if (publishSpaceConfig == null) {
                throw new CMSException(CMSException.ERROR_NOTFOUND);
            }


            Map<String, NavigationItem> navItems = null;

            boolean forceLiveVersion = false;
            if ("1".equals(cmsCtx.getDisplayLiveVersion()) || "1".equals(publishSpaceConfig.getProperties().get("displayLiveVersion"))) {
                forceLiveVersion = true;
            }


            if ("1".equals(publishSpaceConfig.getProperties().get("partialLoading"))) {
                navItems = this.loadPartialNavigationTree(cmsCtx, publishSpaceConfig, path, false);
            } else {
                // Plugin manager
                CustomizationPluginMgr pluginManager = this.customizer.getPluginManager();
                // Navigation adapters
                List<INavigationAdapterModule> navigationAdapters = pluginManager.customizeNavigationAdapters();

                // Nuxeo command
                INuxeoCommand command = new DocumentPublishSpaceNavigationCommand(cmsCtx, publishSpaceConfig, forceLiveVersion, navigationAdapters);

                navItems = (Map<String, NavigationItem>) this.executeNuxeoCommand(cmsCtx, command);
            }

            if (navItems != null) {
                NavigationItem navItem = navItems.get(livePath);
                if (navItem != null) {

                    CMSItem item = navItem.getAdaptedCMSItem();
                    if (item == null) {
                        if (navItem.getMainDoc() != null) {
                            navItem.setAdaptedCMSItem(this.createNavigationItem(cmsCtx, livePath, ((Document) navItem.getMainDoc()).getTitle(),
                                    (Document) navItem.getMainDoc(), publishSpaceConfig.getNavigationPath()));
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


    /**
     * {@inheritDoc}
     */
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

            if ("1".equals(publishSpaceConfig.getProperties().get("partialLoading"))) {
                navItems = this.loadPartialNavigationTree(cmsCtx, publishSpaceConfig, path, true);
            } else {
                // Plugin manager
                CustomizationPluginMgr pluginManager = this.customizer.getPluginManager();
                // Navigation adapters
                List<INavigationAdapterModule> navigationAdapters = pluginManager.customizeNavigationAdapters();

                // Nuxeo command
                INuxeoCommand command = new DocumentPublishSpaceNavigationCommand(cmsCtx, publishSpaceConfig, forceLiveVersion, navigationAdapters);

                navItems = (Map<String, NavigationItem>) this.executeNuxeoCommand(cmsCtx, command);
            }

            if (navItems != null) {
                NavigationItem navItem = navItems.get(path);
                if (navItem != null) {
                    List<CMSItem> childrens = new ArrayList<CMSItem>();


                    for (NavigationItem child : navItem.getChildren()) {
                        String childNavPath = child.getPath();

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
            // Parent identifier (necessary live to get children)
            String version = cmsContext.getDisplayLiveVersion();
            String parentId;
            try {
                cmsContext.setDisplayLiveVersion("1");
                Document parent = (Document) this.fetchContent(cmsContext, path).getNativeItem();
                parentId = parent.getId();
            } finally {
                cmsContext.setDisplayLiveVersion(version);
            }

            RequestPublishStatus publishStatus = RequestPublishStatus.setRequestPublishStatus(version);

            // Nuxeo command execution
            INuxeoCommand nuxeoCommand = new ListCMSSubitemsCommand(cmsContext, parentId, publishStatus);
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
            Satellite savedSatellite = ctx.getSatellite();

            if (savedSatellite == null) {
                Satellite satellite = this.documentsDiscoveryService.discoverLocation(path);
                ctx.setSatellite(satellite);
            }


            try {
                /*
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

                pubInfos = (CMSPublicationInfos) this.executeNuxeoCommand(ctx, (new PublishInfosCommand(ctx.getSatellite(), path)));

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
                ctx.setSatellite(savedSatellite);
            }
        } catch (NuxeoException e) {
            e.rethrowCMSException();
        } catch (CMSException e) {
            throw e;
        } catch (Exception e) {
            throw new CMSException(e);
        }

        return pubInfos;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<CMSItem> getWorkspaces(CMSServiceCtx cmsContext, boolean userWorkspaces, boolean administrator) throws CMSException {
        BrowserAdapter browserAdapter = this.customizer.getBrowserAdapter();

        List<CMSItem> workspaces;
        if (userWorkspaces) {
            workspaces = browserAdapter.getCurrentUserWorkspaces(cmsContext);
        } else {
            workspaces = browserAdapter.getWorkspaces(cmsContext, administrator);
        }
        return workspaces;
    }


    @Override
    public CMSItem getUserWorkspace(CMSServiceCtx cmsContext) throws CMSException {
        // User workspaces
        List<CMSItem> userWorkspaces = this.getWorkspaces(cmsContext, true, false);

        // User workspace
        CMSItem userWorkspace;
        if ((userWorkspaces != null) && (userWorkspaces.size() == 1)) {
            userWorkspace = userWorkspaces.get(0);
        } else {
            userWorkspace = null;
        }

        return userWorkspace;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, NavigationItem> getFullLoadedPortalNavigationItems(CMSServiceCtx cmsContext, String basePath) throws CMSException {
        // Live version indicator
        boolean liveVersion = "1".equals(cmsContext.getDisplayLiveVersion());

        // Navigation items
        Map<String, NavigationItem> navigationItems;

        // Portlet request
        PortletRequest request = cmsContext.getRequest();
        if (request == null) {
            navigationItems = null;
        } else {
            // Request attribute name
            String name = FullLoadedNavigationItems.getRequestAttributeName(basePath);
            // Get full loaded navigation items in request
            FullLoadedNavigationItems fullLoadedNavigationItems = (FullLoadedNavigationItems) request.getAttribute(name);

            if (fullLoadedNavigationItems == null) {
                // Nuxeo command
                INuxeoCommand command = new FullLoadedNavigationCommand(basePath, liveVersion);

                try {
                    // Documents
                    Documents documents = (Documents) this.executeNuxeoCommand(cmsContext, command);

                    navigationItems = new LinkedHashMap<>(documents.size());

                    for (Document document : documents.list()) {
                        String path = StringUtils.removeEnd(document.getPath(), ".proxy");

                        // Navigation item
                        NavigationItem navigationItem = this.getNavigationItem(navigationItems, path);
                        navigationItem.setMainDoc(document);
                        navigationItem.setPath(path);

                        // CMS item
                        CMSItem cmsItem = this.createNavigationItem(cmsContext, path, document.getTitle(), document, basePath);
                        navigationItem.setAdaptedCMSItem(cmsItem);

                        // Parent path
                        PortalObjectPath objectPath = PortalObjectPath.parse(path, PortalObjectPath.CANONICAL_FORMAT);
                        PortalObjectPath parentObjectPath = objectPath.getParent();
                        String parentPath = parentObjectPath.toString(PortalObjectPath.CANONICAL_FORMAT);

                        if (StringUtils.startsWith(parentPath, basePath)) {
                            // Parent navigation item
                            NavigationItem parentNavigationItem = this.getNavigationItem(navigationItems, parentPath);
                            parentNavigationItem.getChildren().add(navigationItem);
                        }
                    }

                    // Save full loaded navigations items in request
                    fullLoadedNavigationItems = new FullLoadedNavigationItems(basePath, navigationItems);
                    request.setAttribute(name, fullLoadedNavigationItems);
                } catch (NuxeoException e) {
                    navigationItems = null;
                    e.rethrowCMSException();
                } catch (CMSException e) {
                    throw e;
                } catch (Exception e) {
                    throw new CMSException(e);
                }
            } else {
                navigationItems = fullLoadedNavigationItems.getNavigationItems();
            }
        }

        return navigationItems;
    }


    /**
     * Get navigation item.
     *
     * @param items navigation items
     * @param path  navigation item path
     * @return navigation item
     */
    private NavigationItem getNavigationItem(Map<String, NavigationItem> items, String path) {
        // Navigation item
        NavigationItem item = items.get(path);

        if (item == null) {
            item = new NavigationItem();
            items.put(path, item);
        }

        return item;
    }


    /**
     * Get extended document informations.
     *
     * @param cmsContext CMS context
     * @param path       document path
     * @return extended document informations
     * @throws CMSException
     */
    public ExtendedDocumentInfos getExtendedDocumentInfos(CMSServiceCtx cmsContext, String path) throws CMSException {
        // HTTP servlet request
        HttpServletRequest request = cmsContext.getServletRequest();

        // Request attribute name
        String attributeName = EXTENDED_DOCUMENT_INFOS_ATTRIBUTE_PREFIX + StringEscapeUtils.escapeHtml(path);

        // Get extended document informations in request
        ExtendedDocumentInfos infos = (ExtendedDocumentInfos) request.getAttribute(attributeName);

        if (infos == null) {
            infos = new ExtendedDocumentInfos();

            // Saved satellite
            Satellite savedSatellite = cmsContext.getSatellite();
            if (savedSatellite == null) {
                Satellite satellite = this.documentsDiscoveryService.discoverLocation(path);
                cmsContext.setSatellite(satellite);
            }

            try {
                if (NuxeoCompatibility.isVersionGreaterOrEqualsThan(NuxeoCompatibility.VERSION_60)) {
                    // Nuxeo command
                    INuxeoCommand command = new ExtendedDocumentInfosCommand(path);

                    infos = (ExtendedDocumentInfos) this.executeNuxeoCommand(cmsContext, command);
                }
            } catch (NuxeoException e) {
                e.rethrowCMSException();
            } catch (CMSException e) {
                throw e;
            } catch (Exception e) {
                throw new CMSException(e);
            } finally {
                cmsContext.setSatellite(savedSatellite);
            }

            request.setAttribute(attributeName, infos);
        }

        return infos;
    }


    /**
     * {@inheritDoc}
     */
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

                // Plugin manager
                CustomizationPluginMgr pluginManager = this.customizer.getPluginManager();

                List<INavigationAdapterModule> navigationAdapters = pluginManager.customizeNavigationAdapters();
                if (CollectionUtils.isNotEmpty(navigationAdapters)) {
                    PortalControllerContext portalControllerContext = new PortalControllerContext(cmsCtx.getControllerContext());

                    for (INavigationAdapterModule navigationAdapter : navigationAdapters) {
                        navigationAdapter.adaptNavigationItem(portalControllerContext, configItem);
                    }
                }

                portalRequest.setAttribute(requestKey, configItem);

            } finally {
                cmsCtx.setScope(savedScope);
                // cmsCtx.setAsyncCacheRefreshing(false);
                cmsCtx.setForcePublicationInfosScope(savedPubInfosScope);
            }

        } catch (CMSException e) {
            throw e;
        } catch (NuxeoException e) {
            if (e.getErrorCode() == NuxeoException.ERROR_NOTFOUND) {
                return null;
            } else {
                throw new CMSException(e);
            }
        } catch (Exception e) {
            throw new CMSException(e);
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


    /**
     * {@inheritDoc}
     */
    @Override
    public String adaptWebPathToCms(CMSServiceCtx cmsContext, String requestPath) throws CMSException {
        try {
            // Publication infos
            CMSPublicationInfos pubInfos = this.getPublicationInfos(cmsContext, requestPath);

            return pubInfos.getDocumentPath();
        } catch (NuxeoException e) {
            if (e.getErrorCode() == NuxeoException.ERROR_NOTFOUND) {
                return null;
            } else {
                throw new CMSException(e);
            }
        } catch (CMSException e) {
            throw e;
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
    public List<CMSEditableWindow> getEditableWindows(CMSServiceCtx cmsContext, String path, String publishSpacePath, String sitePath, String navigationScope, Boolean isSpaceSite)
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


        // Inherited regions, useful in space site to propagate default regions like menus, banner
        // not used in desktop
        Map<String, List<CMSEditableWindow>> inheritedRegions = new HashMap<String, List<CMSEditableWindow>>();
        int windowsCount = 0;
        if (isSpaceSite) {
            inheritedRegions = this.getInheritedRegions(cmsContext, workingPath, publishSpacePath, sitePath, navigationScope,
                    editionMode);
            for (List<CMSEditableWindow> inheritedWindows : inheritedRegions.values()) {
                if (CollectionUtils.isNotEmpty(inheritedWindows)) {
                    // Add inherited region windows
                    windows.addAll(inheritedWindows);
                }
            }
            windowsCount = windows.size();
        }


        try {
            // Get navigation item to convert virtual navigation path to real path
            CMSItem navigationItem;
            if (publishSpacePath == null) {
                navigationItem = null;
            } else {
                navigationItem = this.getPortalNavigationItem(cmsContext, publishSpacePath, workingPath);
            }

            if (navigationItem != null) {
                Document navigationDocument = (Document) navigationItem.getNativeItem();

                // Fetch document
                CMSItem pageItem = this.fetchContent(cmsContext, navigationDocument.getPath());
                Document document = (Document) pageItem.getNativeItem();

                if (publishSpacePath != null) {
                    // Fragments
                    PropertyList fragments = document.getProperties().getList(EditableWindowHelper.SCHEMA_FRAGMENTS);
                    if ((fragments != null) && !fragments.isEmpty()) {
                        Map<String, EditableWindow> editableWindows = this.customizer
                                .getEditableWindows(cmsContext.getServerInvocation().getRequest().getLocales()[0]);

                        // Region windows count
                        int regionWindowsCount = 0;

                        // Loop
                        for (int i = 0; i < fragments.size(); i++) {
                            PropertyMap fragment = fragments.getMap(i);
                            String regionId = fragment.getString(EditableWindowHelper.REGION_IDENTIFIER);

                            if (inheritedRegions.get(regionId) == null) {
                                String category = fragment.getString(EditableWindowHelper.FGT_TYPE);

                                EditableWindow editableWindow = editableWindows.get(category);
                                if (editableWindow != null) {
                                    // Window creation
                                    int windowId = windowsCount + regionWindowsCount;
                                    Map<String, String> properties = editableWindow.fillProps(document, fragment, editionMode);
                                    CMSEditableWindow window = editableWindow.createNewEditabletWindow(windowId, properties);
                                    windows.add(window);
                                    regionWindowsCount++;
                                } else {
                                    // Si type de portlet non trouvé, erreur.
                                    LOG.warn("Type de fragment " + category + " non géré");
                                }
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
     * @param cmsContext       CMS context
     * @param path             current page path
     * @param publishSpacePath publish space path
     * @param sitePath         site path
     * @param navigationScope  navigation scope
     * @param editionMode      edition mode
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

        // Overrided regions
        Set<String> overridedRegions = this.getPageOverridedRegions(cmsContext, path, publishSpacePath);

        // Window identifier
        int windowId = 0;


        String parentPath = CMSObjectPath.parse(path).getParent().toString();
        while (StringUtils.startsWith(parentPath, publishSpacePath)) {
            Map<String, List<CMSEditableWindow>> pagePropagatedRegions = this.getPagePropagatedRegions(navCMSContext, overridedRegions, windowId, parentPath, publishSpacePath, editionMode);
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
            Map<String, List<CMSEditableWindow>> pagePropagatedRegions = this.getPagePropagatedRegions(navCMSContext, overridedRegions, windowId, sitePath, sitePath, editionMode);
            inheritedRegions.putAll(pagePropagatedRegions);
        }

        return inheritedRegions;
    }


    /**
     * Get page overrided regions.
     *
     * @param cmsContext       CMS context
     * @param path             current path
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
     * @param cmsContext       cmsContext
     * @param overridedRegions overrided regions
     * @param windowsCount     windows count
     * @param path             current path
     * @param publishSpacePath publish space path
     * @param editionMode      edition mode indicator
     * @return page propagated regions
     */
    private Map<String, List<CMSEditableWindow>> getPagePropagatedRegions(CMSServiceCtx cmsContext, Set<String> overridedRegions, int windowsCount,
                                                                          String path, String publishSpacePath, boolean editionMode) {
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

                                Map<String, EditableWindow> editableWindows = this.customizer.getEditableWindows(cmsContext.getServerInvocation().getRequest().getLocales()[0]);

                                EditableWindow editableWindow = editableWindows.get(category);

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
    public Map<String, CMSConfigurationItem> getCmsRegionsInheritedLayout(CMSServiceCtx cmsContext, String basePath, String path,
                                                                          Set<CMSConfigurationItem> configuredLayouts)
            throws CMSException {
        // Inherited layouts
        Map<String, CMSConfigurationItem> inheritedLayouts;

        if (StringUtils.isEmpty(basePath) || StringUtils.isEmpty(path) || CollectionUtils.isEmpty(configuredLayouts)) {
            inheritedLayouts = new HashMap<>(0);
        } else {
            inheritedLayouts = new HashMap<>();

            // Propagated regions
            Set<String> propagatedRegions = new HashSet<>();

            // Navigation path
            String navigationPath = path;

            while (StringUtils.startsWith(navigationPath, basePath)) {
                // Navigation item
                CMSItem navigationItem = this.getPortalNavigationItem(cmsContext, basePath, navigationPath);

                if (navigationItem != null) {
                    Document document = (Document) navigationItem.getNativeItem();

                    // Document regions loop
                    PropertyList regions = document.getProperties().getList(EditableWindowHelper.SCHEMA_REGIONS);
                    if (regions != null) {
                        for (int i = 0; i < regions.size(); i++) {
                            // Region properties
                            PropertyMap region = regions.getMap(i);
                            // Region identifier
                            String regionId = region.getString(EditableWindowHelper.REGION_IDENTIFIER);

                            if (!propagatedRegions.contains(regionId)) {
                                // Layout code
                                String layoutCode = region.getString(EditableWindowHelper.REGION_LAYOUT);
                                // Layout
                                CMSConfigurationItem layout = null;

                                if (StringUtils.isNotEmpty(layoutCode)) {
                                    Iterator<CMSConfigurationItem> iterator = configuredLayouts.iterator();
                                    while ((layout == null) && iterator.hasNext()) {
                                        CMSConfigurationItem configuredLayout = iterator.next();

                                        if (StringUtils.equals(layoutCode, configuredLayout.getCode())) {
                                            layout = configuredLayout;
                                        }
                                    }
                                }

                                if (layout != null) {
                                    inheritedLayouts.put(regionId, layout);
                                }

                                propagatedRegions.add(regionId);
                            }
                        }
                    }
                }

                // Loop on parent path
                navigationPath = CMSObjectPath.parse(navigationPath).getParent().toString();
            }
        }

        return inheritedLayouts;
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

                    Map<String, EditableWindow> editableWindows = this.customizer.getEditableWindows(cmsCtx.getServerInvocation().getRequest().getLocales()[0]);
                    EditableWindow ew = editableWindows.get(fragmentCategory);

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
                PageProperties.getProperties().setRefreshingPage(true);
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

        // #1421 - If not specified, use current request url insteaod of nuxeo.url
        String fqdn = NuxeoConnectionProperties.getPublicDomainUri().toString();

        if (StringUtils.isBlank(fqdn) && cmsCtx != null && cmsCtx.getServletRequest() != null) {

            String vhost = null;

            // should check vhost in https instead of current url modified by the reverse proxy
            if (cmsCtx.getServletRequest() != null) {
                vhost = cmsCtx.getServletRequest().getHeader(URLUtils.VIRTUAL_HOST_REQUEST_HEADER);
            }


            if (StringUtils.isNotBlank(vhost)) {
                return vhost;
            } else {
                return cmsCtx.getRequest().getScheme() + "://" + cmsCtx.getRequest().getServerName();
            }


        } else {
            return fqdn;
        }
    }


    @Override
    public String getEcmUrl(CMSServiceCtx cmsCtx, EcmViews command, String path, Map<String, String> requestParameters) throws CMSException {
        // Satellite
        Satellite satellite = cmsCtx.getSatellite();
        if (satellite == null) {
            satellite = Satellite.MAIN;
        }

        NuxeoSatelliteConnectionProperties connectionProperties = NuxeoSatelliteConnectionProperties.getConnectionProperties(satellite);

        // get the default domain and app name
        String uri = connectionProperties.getPublicBaseUri().toString();

        if (requestParameters == null) {
            requestParameters = new HashMap<String, String>();
        }

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
        } else if (command == EcmViews.editAttachments) {
            url = uri.toString() + "/nxpath/default" + path + "@osivia_edit_attachments?";
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
        } else if (command == EcmViews.startValidationWf) {
            url = uri.toString() + "/nxpath/default" + path + "@choose_wf?";
        } else if (command == EcmViews.followWfValidation) {
            url = uri.toString() + "/nxpath/default" + path + "@current_task?";
        } else if (command == EcmViews.remotePublishing) {
            url = uri.toString() + "/nxpath/default" + path + "@remote_publishing?";
        } else if (command == EcmViews.validateRemotePublishing) {
            url = uri.toString() + "/nxpath/default" + path + "@validate_remote_publishing?";
        } else if (command == EcmViews.globalAdministration) {
            url = uri.toString() + "/nxadmin/default@view_admin?";
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
        } else if (EcmViews.RELOAD.equals(command)) {
            url = uri.toString() + "/nxpath/default@refresh_principal";
        }

        // params are used with fancyboxes
        if (!EcmViews.gotoMediaLibrary.equals(command) && !EcmViews.RELOAD.equals(command)) {
            PortalControllerContext portalControllerContext = new PortalControllerContext(cmsCtx.getControllerContext());
            String portalUrl = this.getPortalUrlFactory().getBasePortalUrl(portalControllerContext);
            requestParameters.put("fromUrl", portalUrl);

            if ((command == EcmViews.editPage) || (command == EcmViews.editDocument)) {
                // If in web mode, we pass portal web URL to to editPage
                Portal portal = PortalObjectUtils.getPortal(ControllerContextAdapter.getControllerContext(portalControllerContext));
                if (PortalObjectUtils.isSpaceSite(portal)) {

                    Document currentDoc = (Document) cmsCtx.getDoc();
                    if (currentDoc != null) {
                        String webId = (String) currentDoc.getProperties().get(DocumentsMetadataImpl.WEB_ID_PROPERTY);

                        String webPath = this.getPortalUrlFactory().getCMSUrl(portalControllerContext, null,
                                IWebIdService.CMS_PATH_PREFIX.concat("/").concat(webId), null, null, cmsCtx.getDisplayContext(), cmsCtx.getHideMetaDatas(),
                                null, cmsCtx.getDisplayLiveVersion(), null);
                        if (StringUtils.isNotBlank(webPath)) {
                            webPath = StringUtils.substringBeforeLast(webPath, "/").concat("/");
                        }

                        requestParameters.put("portalWebPath", webPath);
                    }
                }
            }

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
                PageProperties.getProperties().setRefreshingPage(true);
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
        } catch (CMSException e) {
            throw e;
        } catch (Exception e) {
            throw new CMSException(e);
        }

    }


    @Override
    public void unpublishDocument(CMSServiceCtx cmsCtx, String pagePath) throws CMSException {
        String reloadPagePath = pagePath;

        // To consider remote proxy case
        cmsCtx.setDisplayLiveVersion("0");
        CMSItem cmsPublishedItem = this.getContent(cmsCtx, pagePath);
        Document publishedDoc = (Document) cmsPublishedItem.getNativeItem();
        cmsCtx.setDisplayLiveVersion("1");


        Document inputDoc;
        boolean isRemoteProxy = false;

        PropertyList facetsProp = publishedDoc.getFacets();
        for (Object facet : facetsProp.list()) {
            if (facet.toString().equals("isRemoteProxy")) {
                isRemoteProxy = true;
            }
        }

        if (isRemoteProxy) {
            // Remote proxy
            inputDoc = publishedDoc;
            reloadPagePath = StringUtils.substringBeforeLast(pagePath, "/");
            cmsCtx.setDisplayLiveVersion("0");
        } else {
            // local proxy
            CMSItem cmsItem = this.getContent(cmsCtx, pagePath);
            inputDoc = (Document) cmsItem.getNativeItem();

        }

        try {
            this.executeNuxeoCommand(cmsCtx, new SetOffLineCommand(inputDoc));

            // On force le rechargement du cache de la page
            cmsCtx.setForceReload(true);
            this.getContent(cmsCtx, reloadPagePath);
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
    public Map<String, String> getNxPathParameters(String cmsPath) {
        Map<String, String> parameters = new HashMap<String, String>(0);

        if (StringUtils.contains(cmsPath, "?")) {
            String params = StringUtils.substringAfter(cmsPath, "?");

            if (StringUtils.isNotBlank(params)) {
                String[] keysValues = StringUtils.split(params, "&");

                for (String keyValue : keysValues) {
                    String[] keyNValue = StringUtils.split(keyValue, "=");

                    parameters.put(keyNValue[0], keyNValue[1]);
                }

            }

        }

        return parameters;
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
    public void executeEcmCommand(CMSServiceCtx cmsCtx, EcmCommand command, String cmsPath) throws CMSException {
        // Controller context
        ControllerContext controllerContext = cmsCtx.getControllerContext();

        cmsCtx.setDisplayLiveVersion("1");

        CMSItem cmsItem = this.getContent(cmsCtx, cmsPath);
        Document doc = (Document) cmsItem.getNativeItem();

        try {

            this.executeNuxeoCommand(cmsCtx, new NuxeoCommandDelegate(command, doc));

            // On force le rechargement du cache de la page
            String refreshCmsPath = (String) controllerContext.getAttribute(Scope.SESSION_SCOPE, EcmCommand.REDIRECTION_PATH_ATTRIBUTE);
            cmsCtx.setDisplayLiveVersion("0");
            cmsCtx.setForceReload(true);
            this.getContent(cmsCtx, refreshCmsPath);
            cmsCtx.setForceReload(false);

        } catch (Exception e) {
            throw new CMSException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParentPath(String documentPath) {

        if (StringUtils.endsWith(documentPath, SLASH)) {
            documentPath = StringUtils.removeEnd(documentPath, SLASH);
        }

        return StringUtils.substringBeforeLast(documentPath, SLASH);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public TaskbarItems getTaskbarItems(CMSServiceCtx cmsContext) throws CMSException {
        // Plugin manager
        CustomizationPluginMgr pluginManager = this.customizer.getPluginManager();

        return pluginManager.customizeTaskbarItems();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<TaskbarTask> getTaskbarTasks(CMSServiceCtx cmsContext, String basePath, boolean navigation) throws CMSException {
        // Space shortname
        CMSItem spaceCmsItem = this.getSpaceConfig(cmsContext, basePath);
        Document spaceDocument = (Document) spaceCmsItem.getNativeItem();
        String shortname = spaceDocument.getString("webc:url");

        // Publication infos
        CMSPublicationInfos publicationInfos = null;

        // WebId prefix
        String webIdPrefix = ITaskbarService.WEBID_PREFIX + shortname + "_";

        // CMS items
        List<CMSItem> cmsItems;
        if (navigation) {
            cmsItems = this.getPortalNavigationSubitems(cmsContext, basePath, basePath);
        } else {
            cmsItems = this.getChildren(cmsContext, spaceDocument.getId(), NuxeoQueryFilterContext.STATE_LIVE);
        }

        // Taskbar items
        TaskbarItems taskbarItems = this.getTaskbarItems(cmsContext);

        // Factory
        TaskbarFactory factory = this.taskbarService.getFactory();

        // Tasks
        List<TaskbarTask> tasks = new ArrayList<TaskbarTask>(cmsItems.size());
        for (CMSItem cmsItem : cmsItems) {
            // Document
            Document document = (Document) cmsItem.getNativeItem();
            // Type
            DocumentType type = cmsItem.getType();
            // WebId
            String webId = document.getString("ttc:webid");
            // Disabled indicator
            boolean disabled = !"1".equals(cmsItem.getProperties().get("menuItem"));

            // Taskbar item
            TaskbarItem taskbarItem = null;
            for (TaskbarItem item : taskbarItems.getAll()) {
                if (TaskbarItemType.STAPLED.equals(item.getType()) || document.getType().equals(item.getDocumentType())) {
                    String expectedWebId = webIdPrefix + StringUtils.lowerCase(item.getId());
                    if (expectedWebId.equals(webId)) {
                        taskbarItem = item;
                        break;
                    }
                }
            }


            // Task
            TaskbarTask task;
            if ((taskbarItem == null) && (type != null)) {
                String taskPath = document.getPath();
                // Virtual Staple need a virtual CMS link
                if (navigation && StringUtils.isNotEmpty(cmsItem.getNavigationPath()) && StringUtils.isNotEmpty(VirtualNavigationUtils.getWebId(cmsItem.getNavigationPath()))) {
                    taskPath = cmsItem.getNavigationPath();
                }

                task = factory.createTaskbarTask(document.getId(), document.getTitle(), type.getIcon(), taskPath, type.getName(), disabled);
            } else if (taskbarItem != null) {
                // Restriction
                TaskbarItemRestriction restriction = taskbarItem.getRestriction();

                // Check if item access is granted
                boolean granted;
                if (restriction == null) {
                    granted = true;
                } else {
                    if (publicationInfos == null) {
                        publicationInfos = this.getPublicationInfos(cmsContext, basePath);
                    }

                    if (TaskbarItemRestriction.EDITION.equals(restriction)) {
                        // Check if editable by user
                        granted = publicationInfos.isEditableByUser();
                    } else if (TaskbarItemRestriction.MANAGEMENT.equals(restriction)) {
                        // Check if manageable by user
                        granted = publicationInfos.isManageableByUser();
                    } else {
                        // Unknown case, deny access
                        granted = false;
                    }
                }

                if (granted) {
                    task = factory.createTaskbarTask(taskbarItem, document.getTitle(), document.getPath(), disabled);
                } else {
                    task = null;
                }
            } else {
                task = null;
            }
            if (task != null) {
                tasks.add(task);
            }
        }

        return tasks;
    }


    /**
     * Get children CMS items.
     *
     * @param cmsContext CMS context
     * @param parentId   parent Nuxeo document identifier
     * @param state      Nuxeo query filter context state
     * @return CMS items
     * @throws CMSException
     */
    private List<CMSItem> getChildren(CMSServiceCtx cmsContext, String parentId, int state) throws CMSException {
        // Nuxeo documents
        Documents documents;
        try {
            // Nuxeo command
            INuxeoCommand command = new GetChildrenCommand(parentId, state);
            documents = (Documents) this.executeNuxeoCommand(cmsContext, command);
        } catch (CMSException e) {
            throw e;
        } catch (Exception e) {
            throw new CMSException(e);
        }

        // CMS items
        List<CMSItem> cmsItems = new ArrayList<CMSItem>(documents.size());
        for (Document document : documents) {
            // CMS item
            CMSItem cmsItem = this.createItem(cmsContext, document.getPath(), document.getTitle(), document);

            cmsItems.add(cmsItem);
        }

        return cmsItems;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public PanelPlayer getNavigationPanelPlayer(String instance) {
        return this.customizer.getNavigationPanelPlayers().get(instance);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getMoveUrl(CMSServiceCtx cmsContext) throws CMSException {
        // URL
        String url = null;

        // Portal controller context
        PortalControllerContext portalControllerContext = new PortalControllerContext(cmsContext.getControllerContext());

        // Document
        Document document = (Document) cmsContext.getDoc();
        // Publication infos
        CMSPublicationInfos publicationInfos = this.getPublicationInfos(cmsContext, document.getPath());
        // Type
        DocumentType cmsItemType = this.customizer.getCMSItemTypes().get(document.getType());

        if ((cmsItemType != null) && cmsItemType.isEditable()) {
            // Properties
            Map<String, String> properties = new HashMap<String, String>();
            properties.put(MoveDocumentPortlet.DOCUMENT_PATH_WINDOW_PROPERTY, publicationInfos.getDocumentPath());
            properties.put(MoveDocumentPortlet.CMS_BASE_PATH_WINDOW_PROPERTY, publicationInfos.getPublishSpacePath());
            properties.put(MoveDocumentPortlet.ACCEPTED_TYPES_WINDOW_PROPERTY, cmsItemType.getName());

            try {
                url = this.urlFactory.getStartPortletUrl(portalControllerContext, "toutatice-portail-cms-nuxeo-move-portlet-instance", properties,
                        PortalUrlType.POPUP);
            } catch (PortalException e) {
                throw new CMSException(e);
            }
        }

        return url;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getReorderUrl(CMSServiceCtx cmsContext) throws CMSException {
        // URL
        String url = null;

        // Portal controller context
        PortalControllerContext portalControllerContext = new PortalControllerContext(cmsContext.getControllerContext());

        // Document
        Document document = (Document) cmsContext.getDoc();
        // Publication infos
        CMSPublicationInfos publicationInfos = this.getPublicationInfos(cmsContext, document.getPath());

        if (NuxeoCompatibility.isVersionGreaterOrEqualsThan(NuxeoCompatibility.VERSION_62)
                || (!DocumentHelper.isRemoteProxy(cmsContext, publicationInfos) && publicationInfos.isLiveSpace())) {
            // Properties
            Map<String, String> properties = new HashMap<String, String>();
            properties.put(ReorderDocumentsPortlet.PATH_WINDOW_PROPERTY, document.getPath());

            try {
                url = this.urlFactory.getStartPortletUrl(portalControllerContext, "toutatice-portail-cms-nuxeo-reorder-portlet-instance", properties,
                        PortalUrlType.POPUP);
            } catch (PortalException e) {
                throw new CMSException(e);
            }
        }

        return url;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getAdaptedNavigationPath(CMSServiceCtx cmsContext) throws CMSException {
        // Portal controller context
        PortalControllerContext portalControllerContext = new PortalControllerContext(cmsContext.getControllerContext());
        // Plugin manager
        CustomizationPluginMgr pluginManager = this.customizer.getPluginManager();
        // Document
        Document document = (Document) cmsContext.getDoc();
        // Document path
        String path = document.getPath();

        // Draft case
        CMSPublicationInfos publicationInfos = getPublicationInfos(cmsContext, path);
        if (publicationInfos.isDraft()) {
            return publicationInfos.getDraftContextualizationPath();
        }

        // Adapted navigation path
        String navigationPath = null;

        // Navigation adapters
        List<INavigationAdapterModule> adapters = pluginManager.customizeNavigationAdapters();

        if (CollectionUtils.isNotEmpty(adapters)) {
            Iterator<INavigationAdapterModule> adaptersIterator = adapters.iterator();

            while (StringUtils.isEmpty(navigationPath) && adaptersIterator.hasNext()) {
                INavigationAdapterModule adapter = adaptersIterator.next();

                // Adapt navigation path
                navigationPath = adapter.adaptNavigationPath(portalControllerContext, document);

                if (StringUtils.isEmpty(navigationPath)) {
                    // Symlinks
                    Symlinks symlinks = adapter.getSymlinks(portalControllerContext);

                    if ((symlinks != null) && CollectionUtils.isNotEmpty(symlinks.getLinks())) {
                        Iterator<Symlink> symlinksIterator = symlinks.getLinks().iterator();

                        while (StringUtils.isEmpty(navigationPath) && symlinksIterator.hasNext()) {
                            Symlink symlink = symlinksIterator.next();

                            if (StringUtils.startsWith(path, symlink.getTargetPath())) {
                                navigationPath = symlink.getNavigationPath();
                            }
                        }
                    }
                }
            }
        }

        return navigationPath;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public DomainContextualization getDomainContextualization(CMSServiceCtx cmsContext, String domainPath) {
        // Portal controller context
        PortalControllerContext portalControllerContext = new PortalControllerContext(cmsContext.getControllerContext());
        // Plugin manager
        CustomizationPluginMgr pluginManager = this.customizer.getPluginManager();

        // Domain contextualizations
        List<DomainContextualization> domainContextualizations = pluginManager.customizeDomainContextualization();

        DomainContextualization result = null;
        for (DomainContextualization domainContextualization : domainContextualizations) {
            if (domainContextualization.contextualize(portalControllerContext, domainPath)) {
                result = domainContextualization;
                break;
            }
        }

        return result;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public DocumentMetadata getDocumentMetadata(CMSServiceCtx cmsContext) throws CMSException {
        // Document metadata
        DocumentMetadata metadata = new DocumentMetadata();

        // Document
        Document document = (Document) cmsContext.getDoc();
        if (document != null) {
            // Title
            metadata.setTitle(document.getTitle());

            // SEO properties
            Map<String, String> seo = metadata.getSeo();
            // Description
            String description = document.getString("dc:description");
            if (StringUtils.isNotBlank(description)) {
                seo.put("description", description);
            }
            // Author
            String author = document.getString("dc:creator");
            if (StringUtils.isNotBlank(author)) {
                seo.put("author", author);
            }
            // Keywords
            String keywordsProperty = System.getProperty("nuxeo.keywords.property", "ttc:keywords");
            PropertyList keywords = document.getProperties().getList(keywordsProperty);
            if ((keywords != null) && !keywords.isEmpty()) {
                seo.put("keywords", StringUtils.join(keywords.list(), ", "));
            }
        }

        return metadata;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public DocumentsMetadata getDocumentsMetadata(CMSServiceCtx cmsContext, String basePath, Long timestamp) throws CMSException {
        // Portal controller context
        PortalControllerContext portalControllerContext = new PortalControllerContext(cmsContext.getControllerContext());
        // Plugin manager
        CustomizationPluginMgr pluginManager = this.customizer.getPluginManager();
        // Navigation adapters
        List<INavigationAdapterModule> adapters = pluginManager.customizeNavigationAdapters();


        // Version
        RequestPublishStatus version;
        if ("1".equals(cmsContext.getDisplayLiveVersion())) {
            version = RequestPublishStatus.live;
        } else {
            version = RequestPublishStatus.published;
        }

        // Symlinks
        Symlinks symlinks = null;
        for (INavigationAdapterModule adapter : adapters) {
            Symlinks adapterSymlinks = adapter.getSymlinks(portalControllerContext);
            if (adapterSymlinks != null) {
                if (symlinks == null) {
                    symlinks = adapterSymlinks;
                } else {
                    symlinks.addAll(adapterSymlinks);
                }
            }
        }


        // Nuxeo command
        INuxeoCommand command = new DocumentsMetadataCommand(basePath, version, symlinks, timestamp);

        // Super-user scope
        String savedScope = cmsContext.getScope();
        cmsContext.setScope("superuser_no_cache");

        // Metadata
        DocumentsMetadata metadata;
        try {
            metadata = (DocumentsMetadata) this.executeNuxeoCommand(cmsContext, command);
        } catch (CMSException e) {
            throw e;
        } catch (Exception e) {
            throw new CMSException(e);
        } finally {
            cmsContext.setScope(savedScope);
        }

        return metadata;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, TabGroup> getTabGroups(CMSServiceCtx cmsContext) {
        // Plugin manager
        CustomizationPluginMgr pluginManager = this.customizer.getPluginManager();

        return pluginManager.customizeTabGroups();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<MenubarModule> getMenubarModules(CMSServiceCtx cmsContext) {
        // Plugin manager
        CustomizationPluginMgr pluginManager = this.customizer.getPluginManager();

        return pluginManager.customizeMenubarModules();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public NuxeoDocumentContext getDocumentContext(CMSServiceCtx cmsContext, String path) throws CMSException {
        return NuxeoDocumentContextImpl.getDocumentContext(cmsContext, path);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public <D extends DocumentContext> D getDocumentContext(CMSServiceCtx cmsContext, String path, Class<D> expectedType) throws CMSException {
        NuxeoDocumentContext nuxeoDocumentContext = this.getDocumentContext(cmsContext, path);

        // Expected document context
        D expectedDocumentContext;
        if (expectedType.isInstance(nuxeoDocumentContext)) {
            expectedDocumentContext = expectedType.cast(nuxeoDocumentContext);
        } else {
            expectedDocumentContext = null;
        }

        return expectedDocumentContext;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<TemplateAdapter> getTemplateAdapters(CMSServiceCtx cmsContext) {
        // Plugin manager
        CustomizationPluginMgr pluginManager = this.customizer.getPluginManager();

        return pluginManager.customizeTemplateAdapters();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<EcmDocument> getTasks(CMSServiceCtx cmsContext, String user) throws CMSException {
        // Task actors
        Set<String> actors = this.getTaskActors(user, false);

        // Task directives
        Set<String> directives = new HashSet<>(TaskDirective.values().length);
        for (TaskDirective directive : TaskDirective.values()) {
            directives.add(directive.getId());
        }

        // Nuxeo command
        INuxeoCommand command = new GetTasksCommand(actors, true, directives);

        // Documents
        Documents documents;
        try {
            documents = (Documents) this.executeNuxeoCommand(cmsContext, command);
        } catch (CMSException e) {
            throw e;
        } catch (Exception e) {
            throw new CMSException(e);
        }

        return new ArrayList<EcmDocument>(documents.list());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public EcmDocument getTask(CMSServiceCtx cmsContext, String user, String path, UUID uuid) throws CMSException {
        // Task actors
        Set<String> actors = this.getTaskActors(user, true);

        // Nuxeo command
        INuxeoCommand command = new GetTasksCommand(actors, path, uuid);

        // Documents
        Documents documents;
        try {
            documents = (Documents) this.executeNuxeoCommand(cmsContext, command);
        } catch (CMSException e) {
            throw e;
        } catch (Exception e) {
            throw new CMSException(e);
        }

        // Task
        Document task;

        if (documents.size() == 1) {
            task = documents.get(0);
        } else {
            task = null;
        }

        return task;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> updateTask(CMSServiceCtx cmsContext, UUID uuid, String actionId, Map<String, String> variables) throws CMSException {
        // Controller context
        ControllerContext controllerContext = cmsContext.getControllerContext();
        // Portal controller context
        PortalControllerContext portalControllerContext = new PortalControllerContext(controllerContext);


        // Updated variables
        Map<String, String> updatedVariables;

        try {

            CMSItem task = getTask(cmsContext, uuid);
            if (task != null) {
                // Proceed
                updatedVariables = this.formsService.proceed(portalControllerContext, (Document) task.getNativeItem(), actionId, variables);
            } else
                updatedVariables = null;

        } catch (CMSException e) {
            throw e;
        } catch (Exception e) {
            throw new CMSException(e);
        }

        return updatedVariables;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public CMSItem getTask(CMSServiceCtx cmsContext, UUID uuid) throws CMSException {
        // Controller context
        ControllerContext controllerContext = cmsContext.getControllerContext();
        // Portal controller context
        PortalControllerContext portalControllerContext = new PortalControllerContext(controllerContext);

        // Plugin manager
        CustomizationPluginMgr pluginManager = this.customizer.getPluginManager();

        // User
        String user = controllerContext.getServerInvocation().getServerContext().getClientRequest().getRemoteUser();

        // #1964 - tasks url may be done with anonymous user id
        Set<String> actors = null;
        if (StringUtils.isNotEmpty(user)) {
            // Task actors
            actors = this.getTaskActors(user, true);
        }

        // Nuxeo command
        INuxeoCommand command = new GetTasksCommand(actors, null, uuid);

        // Task CMS item
        CMSItem cmsItem;
        try {
            // Documents
            Documents documents = (Documents) this.executeNuxeoCommand(cmsContext, command);
            if (documents.size() == 1) {
                // Task document
                Document document = documents.get(0);
                // Task CMS item
                cmsItem = this.createItem(cmsContext, document.getPath(), document.getTitle(), document, null);

                // Task modules
                List<TaskModule> taskModules = pluginManager.getTaskModules();
                if (CollectionUtils.isNotEmpty(taskModules)) {
                    for (TaskModule taskModule : taskModules) {
                        taskModule.adaptTaskItem(portalControllerContext, document, cmsItem.getProperties());
                    }
                }
            } else {
                cmsItem = null;
            }
        } catch (CMSException e) {
            throw e;
        } catch (Exception e) {
            throw new CMSException(e);
        }

        return cmsItem;
    }


    /**
     * Get task actors.
     *
     * @param user user
     * @return actors
     */
    private Set<String> getTaskActors(String user, boolean includeAnonymous) {
        // User DN
        Name dn = this.personService.getEmptyPerson().buildDn(user);

        // Search user groups
        Group criteria = this.groupService.getEmptyGroup();
        criteria.setMembers(Arrays.asList(new Name[]{dn}));
        List<Group> groups = this.groupService.search(criteria);

        // Actors
        Set<String> actors = new HashSet<>((groups.size() + 1) * 2);

        actors.add(user);
        actors.add(IFormsService.ACTOR_USER_PREFIX + user);

        for (Group group : groups) {
            if (group != null) {
                // Group CN
                String cn = group.getCn();

                actors.add(cn);
                actors.add(IFormsService.ACTOR_GROUP_PREFIX + cn);
            }
        }

        if (includeAnonymous) {

            String anonymousUser = System.getProperty("user.anonymous");
            if (StringUtils.isNotEmpty(anonymousUser))
                actors.add("user:" + anonymousUser);
        }


        return actors;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void reloadSession(CMSServiceCtx cmsContext) throws CMSException {
        // Controller context
        ControllerContext controllerContext = cmsContext.getControllerContext();
        // HTTP servlet request
        HttpServletRequest servletRequest = controllerContext.getServerInvocation().getServerContext().getClientRequest();
        // HTTP session
        HttpSession session = servletRequest.getSession();

        // Reload Nuxeo Automation session
        INuxeoCommand command = new ReloadNuxeoSessionCommand();
        try {
            this.executeNuxeoCommand(cmsContext, command);
        } catch (CMSException e) {
            throw e;
        } catch (Exception e) {
            throw new CMSException(e);
        }

        // Reload Nuxeo web session
        session.setAttribute(Constants.SESSION_RELOAD_ATTRIBUTE, true);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<EcmDocument> getUserSubscriptions(CMSServiceCtx cmsContext) throws CMSException {
        // Saved scope
        String savedScope = cmsContext.getScope();

        // Documents
        Documents documents;

        try {
            cmsContext.setScope("user_session");

            // Nuxeo command
            INuxeoCommand command = new GetUserSubscriptionsCommand();
            documents = (Documents) this.executeNuxeoCommand(cmsContext, command);
        } catch (CMSException e) {
            throw e;
        } catch (Exception e) {
            throw new CMSException(e);
        } finally {
            cmsContext.setScope(savedScope);
        }


        // Subscriptions
        List<EcmDocument> subscriptions = new ArrayList<>(documents.size());
        subscriptions.addAll(documents.list());

        return subscriptions;
    }

    @Override
    public Map<String, String> getTitleMetadataProperties(CMSServiceCtx cmsContext, String path) throws CMSException {
        // Window properties
        Map<String, String> windowProperties = new HashMap<>();

        // CMS item
        CMSItem cmsItem = this.getContent(cmsContext, path);
        // Nuxeo document
        Document document = (Document) cmsItem.getNativeItem();

        // Description
        String description = document.getString("dc:description");
        windowProperties.put(InternalConstants.PROP_WINDOW_SUB_TITLE, description);

        // Vignette
        PropertyMap vignetteMap = document.getProperties().getMap("ttc:vignette");
        if ((vignetteMap != null) && !vignetteMap.isEmpty()) {
            BinaryDescription binary = new BinaryDescription(BinaryDescription.Type.FILE, document.getPath());
            binary.setFieldName("ttc:vignette");
            binary.setDocument(document);
            Link vignetteLink = this.getBinaryResourceURL(cmsContext, binary);
            String vignetteUrl = vignetteLink.getUrl();
            windowProperties.put(InternalConstants.PROP_WINDOW_VIGNETTE_URL, vignetteUrl);
        }

        return windowProperties;
    }


    @Override
    public List<CMSEditableWindow> getProcedureDashboards(CMSServiceCtx cmsContext, String path) throws CMSException {

        List<CMSEditableWindow> procedureDashboards = new ArrayList<CMSEditableWindow>();
        try {

            String user = cmsContext.getControllerContext().getServerInvocation().getServerContext().getClientRequest().getRemoteUser();

            List<Name> userProfiles = personService.getPerson(user).getProfiles();

            // Fetch document
            CMSItem pageItem = this.fetchContent(cmsContext, path);

            Document document = (Document) pageItem.getNativeItem();

            PropertyList dashboards = document.getProperties().getList("pcd:dashboards");

            String webid = document.getProperties().getString("ttc:webid");

            if (dashboards != null) {
                CMSEditableWindow ew;
                for (Object dashboardO : dashboards.list()) {
                    PropertyMap dashboardM = (PropertyMap) dashboardO;

                    String name = dashboardM.getString("name");
                    List<Object> groupsList = dashboardM.getList("groups").list();

                    // contrôle des droits
                    if (isAuthorised(userProfiles, groupsList)) {
                        Map<String, String> applicationProperties = new HashMap<String, String>(11);

                        applicationProperties.put(ViewProcedurePortlet.PROCEDURE_MODEL_ID_WINDOW_PROPERTY, webid);
                        applicationProperties.put(ViewProcedurePortlet.DASHBOARD_ID_WINDOW_PROPERTY, name);
                        applicationProperties.put("osivia.services.procedure.webid", webid);
                        applicationProperties.put("osivia.services.procedure.uuid", document.getId());
                        applicationProperties.put("osivia.doctype", document.getType());
                        applicationProperties.put("osivia.hideDecorators", "1");
                        applicationProperties.put(DynaRenderOptions.PARTIAL_REFRESH_ENABLED, Constants.PORTLET_VALUE_ACTIVATE);
                        applicationProperties.put("osivia.ajaxLink", "1");
                        applicationProperties.put(Constants.WINDOW_PROP_VERSION, "1");

                        applicationProperties.put("osivia.title", name);
                        applicationProperties.put(ThemeConstants.PORTAL_PROP_ORDER, String.valueOf(procedureDashboards.size()));


                        ew = new CMSEditableWindow(name, "toutatice-portail-cms-nuxeo-viewProcedurePortletInstance", applicationProperties);
                        procedureDashboards.add(ew);
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

        return procedureDashboards;
    }

    /**
     * Checks if one of the userProfile is part of a group List
     *
     * @param userProfiles
     * @param groupsList
     * @return true if authorised
     */
    private boolean isAuthorised(List<Name> userProfiles, List<Object> groupsList) {
        if (CollectionUtils.isEmpty(groupsList)) {
            return true;
        }
        for (Object group : groupsList) {
            for (Name userProfile : userProfiles) {
                Enumeration<String> groupName = userProfile.getAll();
                while (groupName.hasMoreElements()) {
                    String nextElement = groupName.nextElement();
                    String userGroup = StringUtils.split(nextElement, '=')[1];
                    if (StringUtils.equals(userGroup, (String) group)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<SpaceStatistics> getSpaceStatistics(CMSServiceCtx cmsContext, Set<String> paths) throws CMSException {
        if (cmsContext.getPortletCtx() == null) {
            cmsContext.setPortletCtx(this.portletCtx);
        }

        return this.statisticsServiceDelegation.getSpaceStatistics(cmsContext, paths);
    }


    @Override
    public void incrementsStatistics(CMSServiceCtx cmsContext, HttpSession httpSession, String path) throws CMSException {
        if (cmsContext.getPortletCtx() == null) {
            cmsContext.setPortletCtx(this.portletCtx);
        }

        this.statisticsServiceDelegation.increments(cmsContext, path);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void updateStatistics(CMSServiceCtx cmsContext, HttpSession httpSession, List<SpaceStatistics> spaceStatistics) throws CMSException {
        if (cmsContext.getPortletCtx() == null) {
            cmsContext.setPortletCtx(this.portletCtx);
        }

        this.statisticsServiceDelegation.updateStatistics(cmsContext, httpSession, spaceStatistics);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Satellite> getSatellites() throws CMSException {
        String[] ids = StringUtils.split(System.getProperty("nuxeo.satellites"), ",");

        Set<Satellite> satellites;
        if (ArrayUtils.isEmpty(ids)) {
            satellites = null;
        } else {
            satellites = new LinkedHashSet<>(ids.length);
            for (String id : ids) {
                Satellite satellite = new Satellite(id);
                satellite.setLabel(StringUtils.defaultIfBlank(System.getProperty("nuxeo.satellite." + id + ".label"), id));
                satellite.setPublicHost(System.getProperty("nuxeo.satellite." + id + ".publicHost"));
                satellite.setPublicPort(System.getProperty("nuxeo.satellite." + id + ".publicPort"));
                satellite.setPrivateHost(System.getProperty("nuxeo.satellite." + id + ".privateHost"));
                satellite.setPrivatePort(System.getProperty("nuxeo.satellite." + id + ".privatePort"));

                // Paths
                String[] paths = StringUtils.split(System.getProperty("nuxeo.satellite." + id + ".paths"), ",");
                if (ArrayUtils.isNotEmpty(paths)) {
                    List<Pattern> patterns = new ArrayList<>(paths.length);
                    for (String path : paths) {
                        Pattern pattern = Pattern.compile(path, Pattern.CASE_INSENSITIVE);
                        patterns.add(pattern);
                    }
                    satellite.setPaths(patterns);
                }

                satellites.add(satellite);
            }
        }

        return satellites;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public CMSItem getSharingRoot(CMSServiceCtx cmsContext) throws CMSException {
        // Controller context
        ControllerContext controllerContext = cmsContext.getControllerContext();
        // HTTP servlet request
        HttpServletRequest servletRequest = controllerContext.getServerInvocation().getServerContext().getClientRequest();
        // Current user
        String user = servletRequest.getRemoteUser();

        // Native item
        Object nativeItem = cmsContext.getDoc();

        // Sharing root
        CMSItem root;

        if (StringUtils.isNotEmpty(user) && (nativeItem != null) && (nativeItem instanceof Document)) {
            Document document = (Document) nativeItem;
            String path = document.getPath();

            // Nuxeo command
            INuxeoCommand command = new GetAncestorsCommand(path);

            // Ancestors
            Documents ancestors;
            try {
                ancestors = (Documents) this.executeNuxeoCommand(cmsContext, command);

                if ((ancestors == null) || ancestors.isEmpty()) {
                    root = null;
                } else {
                    // Ancestors sorted by path
                    Map<String, Document> map = new HashMap<>(ancestors.size());
                    for (Document ancestor : ancestors) {
                        map.put(ancestor.getPath(), ancestor);
                    }

                    String rootPath = null;

                    // Loop on ancestors
                    Document ancestor = map.get(path);
                    while ((ancestor != null) && (StringUtils.countMatches(path, "/") > 1)) {
                        // Enabled sharing indicator
                        boolean enabled = false;
                        PropertyList facets = ancestor.getFacets();
                        if (facets != null) {
                            int i = 0;
                            while (!enabled && (i < facets.size())) {
                                String facet = facets.getString(i);
                                enabled = "Sharing".equals(facet);
                                i++;
                            }
                        }

                        if (enabled) {
                            // Update sharing root path
                            rootPath = ancestor.getPath();
                        }

                        // Parent
                        path = StringUtils.substringBeforeLast(path, "/");
                        ancestor = map.get(path);
                    }

                    if (rootPath == null) {
                        root = null;
                    } else {
                        root = new CMSItem(rootPath, null, null, null, map.get(rootPath));
                    }
                }
            } catch (CMSException e) {
                throw e;
            } catch (Exception e) {
                throw new CMSException(e);
            }
        } else {
            root = null;
        }

        return root;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String resolveLinkSharing(CMSServiceCtx cmsContext, String linkId) throws CMSException {
        // Controller context
        ControllerContext controllerContext = cmsContext.getControllerContext();
        // HTTP servlet request
        HttpServletRequest servletRequest = controllerContext.getServerInvocation().getServerContext().getClientRequest();
        // Current user
        String user = servletRequest.getRemoteUser();

        // Sharing link target document path
        String path;

        if (StringUtils.isBlank(linkId) || StringUtils.isEmpty(user)) {
            path = null;
        } else {
            // Nuxeo command
            INuxeoCommand command = new ResolveSharingLinkCommand(linkId, user);

            // Saved CMS context scope
            String savedScope = cmsContext.getScope();

            // Sharing link target document
            Document document;
            try {
                // Super-user context
                cmsContext.setScope("superuser_context");

                document = (Document) this.executeNuxeoCommand(cmsContext, command);
            } catch (CMSException e) {
                throw e;
            } catch (Exception e) {
                throw new CMSException(e);
            } finally {
                cmsContext.setScope(savedScope);
            }

            if (document == null) {
                path = null;
            } else {
                path = document.getPath();
            }
        }

        return path;
    }


    @Override
    public List<EditorModule> getEditorModules(CMSServiceCtx cmsContext) {
        // Plugin manager
        CustomizationPluginMgr pluginManager = this.customizer.getPluginManager();

        return pluginManager.getEditorModules();
    }


    @Override
    public Map<String, String> getEditorWindowBaseProperties(CMSServiceCtx cmsContext) {
        // Portlet request
        PortletRequest request = cmsContext.getRequest();
        // Portlet response
        PortletResponse response = cmsContext.getResponse();
        // Portlet context
        PortletContext portletContext = cmsContext.getPortletCtx();

        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(request, response, portletContext);

        // Window properties
        Map<String, String> properties = new HashMap<>();
        // Base path
        properties.put(EditorService.WINDOW_PROPERTY_PREFIX + "basePath", nuxeoController.getBasePath());
        // Current document path
        properties.put(EditorService.WINDOW_PROPERTY_PREFIX + "path", nuxeoController.getContentPath());

        return properties;
    }

}
