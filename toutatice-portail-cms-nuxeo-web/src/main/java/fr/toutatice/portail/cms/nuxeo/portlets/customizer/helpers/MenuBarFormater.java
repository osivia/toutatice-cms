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
package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import java.io.UnsupportedEncodingException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.WindowState;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.Portal;
import org.jboss.portal.core.model.portal.PortalObjectPath;
import org.jboss.portal.core.model.portal.Window;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.cms.DocumentType;
import org.osivia.portal.api.cms.impl.BasicPermissions;
import org.osivia.portal.api.cms.impl.BasicPublicationInfos;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.contribution.IContributionService;
import org.osivia.portal.api.contribution.IContributionService.EditionState;
import org.osivia.portal.api.directory.v2.DirServiceFactory;
import org.osivia.portal.api.directory.v2.model.Person;
import org.osivia.portal.api.directory.v2.service.PersonService;
import org.osivia.portal.api.ecm.EcmCommonCommands;
import org.osivia.portal.api.ecm.EcmViews;
import org.osivia.portal.api.html.AccessibilityRoles;
import org.osivia.portal.api.html.DOM4JUtils;
import org.osivia.portal.api.html.HTMLConstants;
import org.osivia.portal.api.internationalization.Bundle;
import org.osivia.portal.api.internationalization.IBundleFactory;
import org.osivia.portal.api.internationalization.IInternationalizationService;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.menubar.IMenubarService;
import org.osivia.portal.api.menubar.MenubarContainer;
import org.osivia.portal.api.menubar.MenubarDropdown;
import org.osivia.portal.api.menubar.MenubarGroup;
import org.osivia.portal.api.menubar.MenubarItem;
import org.osivia.portal.api.menubar.MenubarModule;
import org.osivia.portal.api.taskbar.ITaskbarService;
import org.osivia.portal.api.taskbar.TaskbarItem;
import org.osivia.portal.api.taskbar.TaskbarItems;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSExtendedDocumentInfos;
import org.osivia.portal.core.cms.CMSExtendedDocumentInfos.LockStatus;
import org.osivia.portal.core.cms.CMSExtendedDocumentInfos.SubscriptionStatus;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSItemTypeComparator;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.portalobjects.PortalObjectUtils;
import org.osivia.portal.core.web.IWebIdService;

import fr.toutatice.portail.cms.nuxeo.api.ContextualizationHelper;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoCompatibility;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.PageSelectors;
import fr.toutatice.portail.cms.nuxeo.api.cms.NuxeoDocumentContext;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoConnectionProperties;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.DefaultCMSCustomizer;
import fr.toutatice.portail.cms.nuxeo.portlets.document.helpers.DocumentConstants;
import fr.toutatice.portail.cms.nuxeo.portlets.document.helpers.DocumentHelper;
import fr.toutatice.portail.cms.nuxeo.portlets.move.MoveDocumentPortlet;
import fr.toutatice.portail.cms.nuxeo.portlets.reorder.ReorderDocumentsPortlet;
import fr.toutatice.portail.cms.nuxeo.portlets.service.CMSService;

/**
 * Menubar associée aux contenus.
 *
 * Techniquement cette classe est intéressante car elle montre comment on peut déployer à chaud des fonctionnalités partagées entre les portlets.
 *
 * Les fonctions du NuxeoController pourront donc etre basculées petit à petit dans le CMSCustomizer.
 *
 * A PACKAGER pour la suite
 *
 * @author Jean-Sébastien Steux
 * @author Cédric Krommenhoek
 */
public class MenuBarFormater {

    /** Logger. */
    private static final Log LOGGER = LogFactory.getLog(MenuBarFormater.class);

    /** Menubar service. */
    private final IMenubarService menubarService;
    /** CMS service. */
    private final CMSService cmsService;
    /** Portal URL factory. */
    private final IPortalUrlFactory urlFactory;
    /** CMS customizer. */
    private final DefaultCMSCustomizer customizer;
    /** Contribution service. */
    private final IContributionService contributionService;
    /** Taskbar service. */
    private final ITaskbarService taskbarService;
    /** Bundle factory. */
    private final IBundleFactory bundleFactory;


    /**
     * Constructor.
     *
     * @param portletCtx portlet context
     * @param customizer CMS customizer
     * @param cmsService CMS service
     */
    public MenuBarFormater(PortletContext portletCtx, DefaultCMSCustomizer customizer, CMSService cmsService) {
        super();

        // Menubar service
        this.menubarService = Locator.findMBean(IMenubarService.class, IMenubarService.MBEAN_NAME);
        // CMS service
        this.cmsService = cmsService;
        // Portal URL factory
        this.urlFactory = (IPortalUrlFactory) portletCtx.getAttribute("UrlService");
        // CMS customizer
        this.customizer = customizer;
        // Contribution service
        this.contributionService = Locator.findMBean(IContributionService.class, IContributionService.MBEAN_NAME);
        // Taskbar service
        this.taskbarService = Locator.findMBean(ITaskbarService.class, ITaskbarService.MBEAN_NAME);
        // Bundle factory
        final IInternationalizationService internationalizationService = (IInternationalizationService) portletCtx
                .getAttribute(Constants.INTERNATIONALIZATION_SERVICE_NAME);
        this.bundleFactory = internationalizationService.getBundleFactory(this.getClass().getClassLoader());
    }


    /**
     * Format content menubar.
     *
     * @param cmsContext CMS context
     * @param extendedDocumentInfos
     * @param publicationInfos
     * @throws PortalException
     */
    @SuppressWarnings("unchecked")
    public void formatDefaultContentMenuBar(CMSServiceCtx cmsContext, CMSPublicationInfos pubInfos, CMSExtendedDocumentInfos extendedInfos)
            throws CMSException, PortalException {
        // Document
        Document document = (Document) cmsContext.getDoc();
        if ((document == null) && (cmsContext.getCreationPath() == null)) {
            return;
        }
        // Document context
        NuxeoDocumentContext documentContext;
        try {
            documentContext = NuxeoController.getDocumentContext(cmsContext, document.getPath());
        } catch (PortletException e) {
            throw new CMSException(e);
        }

        // Request
        final PortletRequest request = cmsContext.getRequest();
        // Portal controller context
        final PortalControllerContext portalControllerContext = new PortalControllerContext(cmsContext.getPortletCtx(), request, cmsContext.getResponse());
        // Bundle
        final Bundle bundle = this.bundleFactory.getBundle(cmsContext.getRequest().getLocale());

        // Menubar
        final List<MenubarItem> menubar = (List<MenubarItem>) request.getAttribute(Constants.PORTLET_ATTR_MENU_BAR);

        // Current portal
        final Portal portal = PortalObjectUtils.getPortal(cmsContext.getControllerContext());

        // Check if web page mode layout contains CMS regions and content supports fragments
        // Edition mode is supported by the webpage menu
        boolean webPageFragment = false;
        if (PortalObjectUtils.isSpaceSite(portal) && (cmsContext.getDoc() != null)) {
            final String webPagePath = (String) request.getAttribute("osivia.cms.webPagePath");

            final String docLivePath = ContextualizationHelper.getLivePath(((Document) (cmsContext.getDoc())).getPath());
            if (StringUtils.equals(docLivePath, webPagePath)) {
                webPageFragment = true;
            }
        }


        // Check if current is a workspace
        boolean isWorkspace = this.isWorkspace(document);
        // Check if current item is located inside a user workspace
        boolean insideUserWorkspace = isInUserWorkspace(cmsContext, document);
        // Check if current item is a taskbar item
        boolean isTaskbarItem = !isWorkspace && this.isTaskbarItem(portalControllerContext, cmsContext, documentContext);
        // Check if current document is inside a workspace and current user is an administrator of this workspace
        boolean isWorkspaceAdmin = (isWorkspace || isTaskbarItem) && this.isWorkspaceAdmin(cmsContext, documentContext);


        try {
            // Dropdown menus
            this.addShareDropdown(portalControllerContext, bundle);
            this.addOtherOptionsDropdown(portalControllerContext, bundle);

            // Creation
            if (!isWorkspace) {
                this.getCreateLink(portalControllerContext, cmsContext, pubInfos, menubar, bundle);
            }

            if ((cmsContext.getDoc() != null) && !webPageFragment) {
                // Document type
                DocumentType documentType = this.customizer.getCMSItemTypes().get(document.getType());

                // Edition dropdown menu
                this.addCMSEditionDropdown(portalControllerContext, documentType, bundle);


                // Permalink
                this.getPermaLinkLink(portalControllerContext, cmsContext, pubInfos, menubar, bundle);

                // Contextualization
                this.getContextualizationLink(portalControllerContext, cmsContext, pubInfos, menubar, bundle);

                if (!isWorkspace) {
                    if (!isTaskbarItem || isWorkspaceAdmin) {
                        // Reorder
                        this.getReorderLink(portalControllerContext, cmsContext, pubInfos, menubar, bundle);
                        // Delete
                        this.getDeleteLink(portalControllerContext, cmsContext, pubInfos, menubar, bundle);
                    }

                    if (!isTaskbarItem) {
                        // Change edition mode
                        this.getChangeModeLink(portalControllerContext, cmsContext, pubInfos, menubar, bundle, extendedInfos);
                        // Edition
                        this.getEditLink(portalControllerContext, cmsContext, pubInfos, menubar, bundle);
                        // Nuxeo drive edit
                        this.getDriveEditUrl(portalControllerContext, cmsContext, menubar, bundle, extendedInfos);
                        // Move
                        this.getMoveLink(portalControllerContext, cmsContext, pubInfos, menubar, bundle);
                    }
                }

                // === other tools
                // Live version browser
                this.getLiveContentBrowserLink(portalControllerContext, cmsContext, pubInfos, menubar, bundle);


                // Nuxeo synchronize
                this.getSynchronizeLink(portalControllerContext, cmsContext, menubar, bundle, extendedInfos);

                // Nuxeo administration
                this.getAdministrationLink(portalControllerContext, cmsContext, pubInfos, menubar, bundle);


                if (!insideUserWorkspace) {
                    // Follow
                    this.getSubscribeLink(portalControllerContext, cmsContext, menubar, bundle, extendedInfos);

                    if (!isWorkspace && !isTaskbarItem) {
                        // Lock
                        this.getLockLink(portalControllerContext, cmsContext, menubar, bundle, extendedInfos);

                        // Validation workflow(s)
                        this.getValidationWfLink(portalControllerContext, cmsContext, pubInfos, menubar, bundle, extendedInfos);
                        // Remote publishing
                        this.getRemotePublishingLink(portalControllerContext, cmsContext, pubInfos, menubar, bundle, extendedInfos);
                    }
                }
            }
        } catch (final CMSException e) {
            if ((e.getErrorCode() == CMSException.ERROR_FORBIDDEN) || (e.getErrorCode() == CMSException.ERROR_NOTFOUND)) {
                // On ne fait rien : le document n'existe pas ou je n'ai pas les droits
            } else {
                throw e;
            }
        }


        // Menubar modules
        List<MenubarModule> modules = this.customizer.getPluginMgr().customizeMenubarModules();
        for (MenubarModule module : modules) {
            module.customizeDocument(portalControllerContext, menubar, documentContext);
        }
    }


    /**
     * Check if current document is a workspace.
     * 
     * @param document current Nuxeo document
     * @return true if current document is a workspace
     */
    protected boolean isWorkspace(Document document) {
        String type = document.getType();
        return ("Workspace".equals(type) || "Room".equals(type));
    }


    /**
     * Check if current document is located in a user workspace.
     * 
     * @param cmsContext CMS context
     * @param document current Nuxeo document
     * @return true if current document is located in a user workspace
     * @throws CMSException
     */
    protected boolean isInUserWorkspace(CMSServiceCtx cmsContext, Document document) throws CMSException {
        boolean userWorkspace = false;
        if (document != null) {
            final String path = document.getPath() + "/";

            final List<CMSItem> userWorkspaces = this.cmsService.getWorkspaces(cmsContext, true, false);
            for (final CMSItem cmsItem : userWorkspaces) {
                if (StringUtils.startsWith(path, cmsItem.getPath() + "/")) {
                    userWorkspace = true;
                    break;
                }
            }
        }
        return userWorkspace;
    }


    /**
     * Check if current document is a taskbar item.
     * 
     * @param portalControllerContext portal controller controller
     * @param cmsContext CMS context
     * @param documentContext current Nuxeo document context
     * @return true if current document is a taskbar item
     * @throws CMSException
     * @throws PortalException
     */
    protected boolean isTaskbarItem(PortalControllerContext portalControllerContext, CMSServiceCtx cmsContext, NuxeoDocumentContext documentContext)
            throws CMSException, PortalException {
        // Document
        Document document = documentContext.getDoc();
        // Publication infos
        BasicPublicationInfos publicationInfos = documentContext.getPublicationInfos(BasicPublicationInfos.class);
        // Base path
        String basePath = publicationInfos.getBasePath();
        // Parent path
        String parentPath = StringUtils.substringBeforeLast(document.getPath(), "/");
        
        // Taskbar item indicator
        boolean taskbarItem;

        if (StringUtils.equals(basePath, parentPath)) {
            // Space config
            CMSItem spaceConfig = this.cmsService.getSpaceConfig(cmsContext, basePath);
            // Space document
            Document space = (Document) spaceConfig.getNativeItem();
            // Space shortname
            String shortname = space.getString("webc:url");

            // WebId
            String webId = document.getString("ttc:webid");
            // WebId prefix
            String prefix = ITaskbarService.WEBID_PREFIX + shortname + "_";

            if (StringUtils.startsWith(webId, prefix)) {
                // Task identifier
                String taskId = StringUtils.upperCase(StringUtils.removeStart(webId, prefix));

                // Taskbar items
                TaskbarItems items = this.taskbarService.getItems(portalControllerContext);
                TaskbarItem item = items.get(taskId);

                taskbarItem = (item != null);
            } else {
                taskbarItem = false;
            }
        } else {
            taskbarItem = false;
        }

        return taskbarItem;
    }


    /**
     * Check if current document is inside a workspace and if current user is an administrator of this workspace.
     * 
     * @param cmsContext CMS context
     * @param documentContext current document context
     * @return true if current document is inside a workspace and if current user is an administrator of this workspace.
     * @throws CMSException
     */
    protected boolean isWorkspaceAdmin(CMSServiceCtx cmsContext, NuxeoDocumentContext documentContext) throws CMSException {
        // Publication infos
        BasicPublicationInfos publicationInfos = documentContext.getPublicationInfos(BasicPublicationInfos.class);

        // Base path
        String basePath = publicationInfos.getBasePath();

        // Workspace document context
        NuxeoDocumentContext workspaceDocumentContext;

        if (StringUtils.equals(basePath, publicationInfos.getContentPath())) {
            workspaceDocumentContext = documentContext;
        } else {
            try {
                workspaceDocumentContext = NuxeoController.getDocumentContext(cmsContext, basePath);
            } catch (PortletException e) {
                throw new CMSException(e);
            }
        }

        // Check permissions
        BasicPermissions permissions = workspaceDocumentContext.getPermissions(BasicPermissions.class);

        return permissions.isManageableByUser();
    }


    /**
     * Format content menubar.
     *
     * @param cmsCtx
     * @param extendedDocumentInfos
     * @param publicationInfos
     * @throws Exception
     */
    public void formatContentMenuBar(CMSServiceCtx cmsCtx, CMSPublicationInfos publicationInfos, CMSExtendedDocumentInfos extendedDocumentInfos)
            throws Exception {
        this.formatDefaultContentMenuBar(cmsCtx, publicationInfos, extendedDocumentInfos);
    }


    /**
     * Checks if document in context has webId property.
     *
     * @param cmsCtx context
     * @return true if document in context has webId property.
     */
    public boolean hasWebId(CMSServiceCtx cmsCtx) {
        boolean has = false;

        final Document document = (Document) cmsCtx.getDoc();
        if (document != null) {
            final String webid = document.getString("ttc:webid");
            has = StringUtils.isNotBlank(webid);
        }

        return has;
    }


    /**
     * Get menubar CMS edition dropdown menu.
     *
     * @param portalControllerContext portal controller context
     * @param bundle internationalization bundle
     */
    protected void addCMSEditionDropdown(PortalControllerContext portalControllerContext, DocumentType type, Bundle bundle) {
        MenubarDropdown dropdown = new MenubarDropdown(MenubarDropdown.CMS_EDITION_DROPDOWN_MENU_ID, bundle.getString("CMS_EDITION"),
                "glyphicons glyphicons-pencil", MenubarGroup.CMS, 3, false, false);
        dropdown.setBreadcrumb((type != null) && (type.isFolderish()) && BooleanUtils.isNotTrue(type.getEditorialContent()));
        this.menubarService.addDropdown(portalControllerContext, dropdown);
    }


    /**
     * Add menubar share dropdown menu.
     *
     * @param portalControllerContext portal controller context
     * @param bundle internationalization bundle
     */
    protected void addShareDropdown(PortalControllerContext portalControllerContext, Bundle bundle) {
        MenubarDropdown dropdown = new MenubarDropdown(MenubarDropdown.SHARE_DROPDOWN_MENU_ID, bundle.getString("SHARE"), "glyphicons glyphicons-share-alt",
                MenubarGroup.GENERIC, 8);
        this.menubarService.addDropdown(portalControllerContext, dropdown);
    }


    /**
     * Add menubar other options dropdown menu.
     *
     * @param portalControllerContext portal controller context
     * @param bundle internationalization bundle
     */
    protected void addOtherOptionsDropdown(PortalControllerContext portalControllerContext, Bundle bundle) {
        MenubarDropdown dropdown = new MenubarDropdown(MenubarDropdown.OTHER_OPTIONS_DROPDOWN_MENU_ID, bundle.getString("OTHER_OPTIONS"),
                "glyphicons glyphicons-option-vertical", MenubarGroup.GENERIC, 40, false, false);
        this.menubarService.addDropdown(portalControllerContext, dropdown);
    }


    /**
     * Get optional Nuxeo administration link.
     *
     * @param portalControllerContext portal controller context
     * @param cmsContext CMS service context
     * @param menubar menubar items
     * @param bundle internationalization bundle
     */
    protected void getAdministrationLink(PortalControllerContext portalControllerContext, CMSServiceCtx cmsContext, CMSPublicationInfos pubInfos,
            List<MenubarItem> menubar, Bundle bundle) throws CMSException {
        if (cmsContext.getRequest().getRemoteUser() == null) {
            return;
        }

        // Do not manage remote proxy
        if (DocumentHelper.isRemoteProxy(cmsContext, pubInfos)) {
            return;
        }

        if (!pubInfos.isDraft() && pubInfos.isEditableByUser() && ContextualizationHelper.isCurrentDocContextualized(cmsContext)) {
            // URL
            final String url = NuxeoConnectionProperties.getPublicBaseUri().toString() + "/nxdoc/default/" + pubInfos.getLiveId() + "/view_documents";

            // Parent
            final MenubarDropdown parent = this.menubarService.getDropdown(portalControllerContext, MenubarDropdown.OTHER_OPTIONS_DROPDOWN_MENU_ID);

            // Menubar item
            final MenubarItem item = new MenubarItem("MANAGE", bundle.getString("MANAGE_IN_NUXEO"), null, parent, 21, url, "nuxeo", null, null);
            item.setAjaxDisabled(true);
            item.setDivider(true);

            menubar.add(item);
        }
    }


    /**
     * Get change mode link.
     *
     * @param portalControllerContext portal controller context
     * @param cmsContext CMS service context
     * @param menubar menubar items
     * @param bundle internationalization bundle
     * @param extendedInfos
     */
    protected void getChangeModeLink(PortalControllerContext portalControllerContext, CMSServiceCtx cmsContext, CMSPublicationInfos pubInfos,
            List<MenubarItem> menubar, Bundle bundle, CMSExtendedDocumentInfos extendedInfos) throws CMSException, PortalException {
        if ((cmsContext.getRequest().getRemoteUser() == null) || StringUtils.isBlank(pubInfos.getPublishSpacePath())) {
            // cas non authentifié et hors espace de publication
            return;
        }

        // Current document
        final Document document = (Document) (cmsContext.getDoc());
        final String path = document.getPath();

        // CMS item type
        final Map<String, DocumentType> managedTypes = this.customizer.getCMSItemTypes();
        final DocumentType documentType = managedTypes.get(document.getType());

        if ((documentType != null) && documentType.isSupportsPortalForms()) {
            if (pubInfos.isEditableByUser() && ContextualizationHelper.isCurrentDocContextualized(cmsContext)) {
                // Publish Spaces
                if(!pubInfos.isLiveSpace()){
                    // Edition state
                    EditionState editionState;
    
                    final MenubarDropdown parent = this.menubarService.getDropdown(portalControllerContext, MenubarDropdown.CMS_EDITION_DROPDOWN_MENU_ID);
    
                    if (!DocumentHelper.isRemoteProxy(cmsContext, pubInfos) && pubInfos.isBeingModified()) {
                        // Current modification indicator
                        final MenubarItem modificationIndicator = new MenubarItem("MODIFICATION_MESSAGE", null, MenubarGroup.CMS, -12, "label label-default");
                        modificationIndicator.setGlyphicon("halflings halflings-asterisk");
                        modificationIndicator.setTooltip(bundle.getString("MODIFICATION_MESSAGE"));
                        modificationIndicator.setState(true);
                        menubar.add(modificationIndicator);
                    }
    
                    if (DocumentHelper.isInLiveMode(cmsContext, pubInfos)) {
                        editionState = new EditionState(EditionState.CONTRIBUTION_MODE_ONLINE, path);
    
                        // Live version indicator menubar item
                        final MenubarItem liveIndicator = new MenubarItem("LIVE_VERSION", bundle.getString("LIVE_VERSION"), MenubarGroup.CMS, -12,
                                "label label-info");
                        liveIndicator.setGlyphicon("halflings halflings-pencil visible-xs-inline-block");
                        liveIndicator.setState(true);
    
                        menubar.add(liveIndicator);
    
    
                        if ((extendedInfos != null) && extendedInfos.isOnlineTaskPending()) {
                            // Online workflow pending indicator menubar item
                            final MenubarItem pendingIndicator = new MenubarItem("ON_LINE_WF_PENDING", bundle.getString("ON_LINE_WF_PENDING"), MenubarGroup.CMS,
                                    -11, "label label-warning");
                            pendingIndicator.setGlyphicon("glyphicons glyphicons-history");
                            pendingIndicator.setState(true);
    
                            menubar.add(pendingIndicator);
                        }
                    } else {
                        editionState = new EditionState(EditionState.CONTRIBUTION_MODE_EDITION, path);
    
                        // Forget old state
                        this.contributionService.removeWindowEditionState(portalControllerContext);
                    }
    
                    if (DocumentHelper.isInLiveMode(cmsContext, pubInfos)) {
                        if (extendedInfos.isOnlineTaskPending()) {
                            if (extendedInfos.canUserValidateOnlineTask()) {
    
                                if (DocumentConstants.VALIDATE_ONLINE_TASK_NAME.equals(extendedInfos.getTaskName())) {
                                    // Online workflow validation items
                                    this.addValidatePublishingItems(portalControllerContext, cmsContext, pubInfos, menubar, parent, bundle);
                                } else {
                                    final Map<String, String> requestParameters = new HashMap<String, String>();
                                    final String validateURL = this.cmsService.getEcmUrl(cmsContext, EcmViews.validateRemotePublishing, pubInfos.getDocumentPath(),
                                            requestParameters);
                                    final MenubarItem validateItem = new MenubarItem("REMOTE_ONLINE_WF_VALIDATE", bundle.getString("REMOTE_ONLINE_WF_VALIDATE"),
                                            null, parent, 14, validateURL, null, null, null);
                                    validateItem.setAjaxDisabled(true);
                                    menubar.add(validateItem);
                                }
    
                            } else if (extendedInfos.isUserOnlineTaskInitiator()) {
                                // Cancel publishing ask (workflow) item
                                final String cancelAskPublishURL = this.getContributionService().getCancelPublishingAskContributionURL(portalControllerContext,
                                        pubInfos.getDocumentPath());
    
                                final MenubarItem cancelAskPublishItem = new MenubarItem("CANCEL_ASK_PUBLISH", bundle.getString("CANCEL_ASK_PUBLISH"), null,
                                        MenubarGroup.CMS, 12, cancelAskPublishURL, null, null, null);
                                cancelAskPublishItem.setAjaxDisabled(true);
    
                                menubar.add(cancelAskPublishItem);
                            }
                        } else {
                            if (!DocumentHelper.isRemoteProxy(cmsContext, pubInfos) && pubInfos.isBeingModified()) {
    
                                if (pubInfos.isPublished()) {
                                    // Erase modifications
                                    String cmsEraseModificationURL = this.urlFactory.getEcmCommandUrl(portalControllerContext, path,
                                            EcmCommonCommands.eraseModifications);
                                    final MenubarItem eraseItem = new MenubarItem("ERASE", bundle.getString("ERASE"), "halflings halflings-erase", parent, 11,
                                            "#erase_cms_page", null, null, "fancybox_inline");
                                    eraseItem.setAssociatedHTML(this.generateEraseFancyBox(bundle, cmsEraseModificationURL));
                                    menubar.add(eraseItem);
                                }
                                if (pubInfos.isUserCanValidate()) {
                                    // Publish menubar item
                                    final String publishURL = this.contributionService.getPublishContributionURL(portalControllerContext,
                                            pubInfos.getDocumentPath());
    
                                    final MenubarItem publishItem = new MenubarItem("PUBLISH", bundle.getString("PUBLISH"), "glyphicons glyphicons-ok", parent,
                                            12, publishURL, null, null, null);
                                    publishItem.setAjaxDisabled(true);
                                    publishItem.setDivider(true);
    
                                    menubar.add(publishItem);
                                } else {
                                    // Ask Publication (workflow) item
                                    final String askPublishURL = this.getContributionService().getAskPublishContributionURL(portalControllerContext,
                                            pubInfos.getDocumentPath());
    
                                    final MenubarItem askPublishItem = new MenubarItem("ASK_PUBLISH", bundle.getString("ASK_PUBLISH"), null, parent, 12,
                                            askPublishURL, null, null, null);
                                    askPublishItem.setAjaxDisabled(true);
    
                                    menubar.add(askPublishItem);
                                }
                            }
                        }
    
                        // Go to proxy menubar item
                        if (pubInfos.isPublished()) {
                            final String proxyURL = this.getContributionService().getChangeEditionStateUrl(portalControllerContext, editionState);
    
                            final MenubarItem proxyItem = new MenubarItem("PROXY_RETURN", bundle.getString("PROXY_RETURN"), "halflings halflings-eye-close",
                                    parent, 1, proxyURL, null, null, null);
                            proxyItem.setAjaxDisabled(true);
    
                            menubar.add(proxyItem);
                        }
                    } else {
                        if (pubInfos.isUserCanValidate()) {
                            // user can not unpublish root documents like portalsite, blogsite, website, ...
                            final MenubarItem unpublishItem = new MenubarItem("UNPUBLISH", bundle.getString("UNPUBLISH"), parent, 12, null);
                            unpublishItem.setAjaxDisabled(true);
                            unpublishItem.setDivider(true);
    
                            if ((documentType == null) || !documentType.isRootType()) {
                                // Unpublish menubar item
                                final String unpublishURL = this.contributionService.getUnpublishContributionURL(portalControllerContext,
                                        pubInfos.getDocumentPath());
    
                                unpublishItem.setUrl(unpublishURL);
                            } else {
                                unpublishItem.setUrl("#");
                                unpublishItem.setDisabled(true);
                                unpublishItem.setTooltip(bundle.getString("CANNOT_UNPUBLISH_ROOT"));
                            }
    
                            menubar.add(unpublishItem);
                        }
    
                        if (DocumentHelper.isRemoteProxy(cmsContext, pubInfos)) {
                            // Go to live version
                            final String liveURL = this.urlFactory.getCMSUrl(portalControllerContext, null, pubInfos.getLiveId(), null, "1",
                                    IPortalUrlFactory.DISPLAYCTX_PREVIEW_LIVE_VERSION, null, null, null, null);
    
                            final MenubarItem liveItem = new MenubarItem("GO_TO_LIVE", bundle.getString("GO_TO_LIVE"), "halflings halflings-eye-open", parent, 1,
                                    liveURL, null, null, null);
                            liveItem.setAjaxDisabled(true);
    
                            menubar.add(liveItem);
    
                        } else {
                            // Go to preview menubar item
                            final String previewURL = this.contributionService.getChangeEditionStateUrl(portalControllerContext, editionState);
    
                            final MenubarItem previewItem = new MenubarItem("LIVE_PREVIEW", bundle.getString("LIVE_PREVIEW"), "halflings halflings-eye-open",
                                    parent, 1, previewURL, null, null, null);
                            previewItem.setAjaxDisabled(true);
    
                            menubar.add(previewItem);
                        }
                    }
                } else {
                    // Collaboratives Spaces
                    final MenubarDropdown parent = this.menubarService.getDropdown(portalControllerContext, MenubarDropdown.CMS_EDITION_DROPDOWN_MENU_ID);
                    
                    if (pubInfos.hasDraft()) {
                        // Go to draft menubar item
                        String draftURL = this.urlFactory.getCMSUrl(portalControllerContext, null, pubInfos.getDraftPath(), null, null, null, null, null, "1", null);
                        
                        final MenubarItem draftItem = new MenubarItem("GO_TO_DRAFT", bundle.getString("GO_TO_DRAFT"), "glyphicons glyphicons-notes", parent, 1,
                                draftURL, null, null, null);
                        draftItem.setAjaxDisabled(true);
                        
                        menubar.add(draftItem);
                        
                    } else if(pubInfos.isDraft()){
                        // Draft indicator menubar item
                        final MenubarItem draftIndicator = new MenubarItem("DRAFT", bundle.getString("DRAFT"), MenubarGroup.CMS, -12,
                                "label label-info");
                        draftIndicator.setGlyphicon("glyphicons glyphicons-notes visible-xs-inline-block");
                        draftIndicator.setState(true);

                        menubar.add(draftIndicator);
                        
                        // Go to public version
                        if(pubInfos.isNotOrphanDraft()){
                            String publicLiveId = DocumentHelper.getDocWebIdHavingDraft(document);
                            String cmsPublicLiveId = IWebIdService.CMS_PATH_PREFIX.concat("/").concat(publicLiveId);
                            String publicLiveURL = this.urlFactory.getCMSUrl(portalControllerContext, null, cmsPublicLiveId, null, null, null, null, null, "1", null);
                            
                            final MenubarItem publicLiveItem = new MenubarItem("PUBLIC_LIVE_RETURN", bundle.getString("PUBLIC_LIVE_RETURN"), "halflings halflings-eye-open",
                                    parent, 1, publicLiveURL, null, null, null);
                            publicLiveItem.setAjaxDisabled(true);
                            
                            menubar.add(publicLiveItem);
                        }
                    }
                }
            } 
        }
    }


    /**
     * Generate validate or recject OnLine workflow items..
     *
     * @param portalControllerContext portal controller context
     * @param cmsContext CMS context
     * @param pubInfos publication infos
     * @param menubar menubar items
     * @param parent menubar item parent
     * @param bundle internationalization bundle
     */
    protected void addValidatePublishingItems(PortalControllerContext portalControllerContext, CMSServiceCtx cmsContext, CMSPublicationInfos pubInfos,
            List<MenubarItem> menubar, MenubarContainer parent, Bundle bundle) throws CMSException {
        // Validate
        final String validateURL = this.getContributionService().getValidatePublishContributionURL(portalControllerContext, pubInfos.getDocumentPath());
        final MenubarItem validateItem = new MenubarItem("ONLINE_WF_VALIDATE", bundle.getString("VALIDATE_PUBLISH"), "glyphicons glyphicons-ok", parent, 13,
                validateURL, null, null, null);
        validateItem.setAjaxDisabled(true);
        menubar.add(validateItem);

        // Reject
        final String rejectURL = this.getContributionService().getRejectPublishContributionURL(portalControllerContext, pubInfos.getDocumentPath());
        final MenubarItem rejectItem = new MenubarItem("ONLINE_WF_REJECT", bundle.getString("REJECT_PUBLISH"), "glyphicons glyphicons-remove", parent, 14,
                rejectURL, null, null, null);
        rejectItem.setAjaxDisabled(true);
        menubar.add(rejectItem);
    }


    /**
     * Get live content browser link.
     *
     * @param portalControllerContext portal controller context
     * @param cmsContext CMS service context
     * @param bundle internationalization bundle
     */
    protected void getLiveContentBrowserLink(PortalControllerContext portalControllerContext, CMSServiceCtx cmsContext, CMSPublicationInfos pubInfos,
            List<MenubarItem> menubar, Bundle bundle) throws CMSException {
        if (cmsContext.getRequest().getRemoteUser() == null) {
            return;
        }

        if (!ContextualizationHelper.isCurrentDocContextualized(cmsContext)) {
            return;
        }

        // Do not browse into remote proxy
        if (DocumentHelper.isRemoteProxy(cmsContext, pubInfos)) {
            return;
        }

        // Nuxeo controller
        final NuxeoController nuxeoController = new NuxeoController(portalControllerContext.getRequest(), portalControllerContext.getResponse(),
                portalControllerContext.getPortletCtx());

        // Current document
        final Document document = (Document) cmsContext.getDoc();

        String navigationPath;
        boolean folderish;
        DocumentType cmsItemType;

        if (document == null) {
            navigationPath = cmsContext.getCreationPath();

            cmsItemType = null;

            // Items with creation path are presumed folderish
            folderish = (navigationPath != null);
        } else {
            navigationPath = nuxeoController.getContentPath();

            cmsItemType = this.customizer.getCMSItemTypes().get(document.getType());
            folderish = (cmsItemType != null) && cmsItemType.isFolderish();
        }

        if (!pubInfos.isLiveSpace() && !pubInfos.getSubTypes().isEmpty() && folderish) {
            // Current portal
            final Portal portal = PortalObjectUtils.getPortal(cmsContext.getControllerContext());
            // Space site indicator
            final boolean spaceSite = PortalObjectUtils.isSpaceSite(portal);

            // Live content browser popup link
            String browserURL;
            try {
                final Map<String, String> properties = new HashMap<String, String>(1);
                properties.put("osivia.browser.basePath", nuxeoController.getBasePath());
                properties.put("osivia.browser.navigationPath", navigationPath);
                properties.put("osivia.browser.space", String.valueOf(spaceSite));
                browserURL = this.urlFactory.getStartPortletUrl(portalControllerContext, "osivia-portal-browser-portlet-instance", properties, true);
            } catch (final PortalException e) {
                browserURL = "#";
            }

            final MenubarDropdown parent = this.menubarService.getDropdown(portalControllerContext, MenubarDropdown.CMS_EDITION_DROPDOWN_MENU_ID);

            final MenubarItem browserItem = new MenubarItem("BROWSE_LIVE_CONTENT", bundle.getString("BROWSE_LIVE_CONTENT"), "glyphicons glyphicons-book-open",
                    parent, 50, browserURL, null, null, "fancyframe_refresh");
            browserItem.setAjaxDisabled(true);
            browserItem.setDivider(true);
            browserItem.setBreadcrumb(true);

            menubar.add(browserItem);
        }
    }


    /**
     * Get Nuxeo Drive edit URL.
     *
     * @param portalControllerContext portal controller context
     * @param cmsContext CMS service context
     * @param bundle internationalization bundle
     */
    protected void getDriveEditUrl(PortalControllerContext portalControllerContext, CMSServiceCtx cmsContext, List<MenubarItem> menubar, Bundle bundle,
            CMSExtendedDocumentInfos extendedInfos) throws CMSException {
        if (cmsContext.getRequest().getRemoteUser() == null) {
            return;
        }

        if (!ContextualizationHelper.isCurrentDocContextualized(cmsContext)) {
            return;
        }

        final Document document = (Document) cmsContext.getDoc();

        final DocumentType cmsItemType = this.customizer.getCMSItemTypes().get(document.getType());

        if ((extendedInfos.getDriveEditURL() != null) && (cmsItemType != null) && cmsItemType.isLiveEditable()) {
            final MenubarDropdown parent = this.menubarService.getDropdown(portalControllerContext, MenubarDropdown.CMS_EDITION_DROPDOWN_MENU_ID);

            final MenubarItem driveEditItem = new MenubarItem("DRIVE_EDIT", bundle.getString("DRIVE_EDIT"), "glyphicons glyphicons-play", parent, 2,
                    extendedInfos.getDriveEditURL(), null, null, null);
            driveEditItem.setAjaxDisabled(true);

            menubar.add(driveEditItem);
        }
    }


    /**
     * Get synchronize link.
     *
     * @param portalControllerContext portal controller context
     * @param cmsContext CMS service context
     * @param bundle internationalization bundle
     * @throws PortalException
     */
    protected void getSynchronizeLink(PortalControllerContext portalControllerContext, CMSServiceCtx cmsContext, List<MenubarItem> menubar, Bundle bundle,
            CMSExtendedDocumentInfos extendedInfos) throws CMSException {
        if (cmsContext.getRequest().getRemoteUser() == null) {
            return;
        }

        if (!ContextualizationHelper.isCurrentDocContextualized(cmsContext)) {
            return;
        }

        // Current document
        final Document document = (Document) cmsContext.getDoc();
        final String path = document.getPath();

        String command = null;
        String icon = null;
        EcmCommonCommands ecmAction = null;

        if (extendedInfos.isCanSynchronize()) {
            command = "SYNCHRONIZE_ACTION";
            icon = "glyphicons glyphicons-synchronization";
            ecmAction = EcmCommonCommands.synchronizeFolder;
        } else if (extendedInfos.isCanUnsynchronize()) {
            command = "UNSYNCHRONIZE_ACTION";
            icon = "glyphicons glyphicons-synchronization-ban";
            ecmAction = EcmCommonCommands.unsynchronizeFolder;

            // Synchronized indicator menubar item
            final MenubarItem synchronizedIndicator = new MenubarItem("SYNCHRONIZED", null, MenubarGroup.CMS, -2, "label label-success");
            synchronizedIndicator.setGlyphicon("halflings halflings-refresh");
            synchronizedIndicator.setTooltip(bundle.getString("SYNCHRONIZED"));
            synchronizedIndicator.setState(true);
            menubar.add(synchronizedIndicator);
        }

        final MenubarDropdown parent = this.menubarService.getDropdown(portalControllerContext, MenubarDropdown.OTHER_OPTIONS_DROPDOWN_MENU_ID);

        if (command != null) {
            try {
                final String synchronizeURL = this.urlFactory.getEcmCommandUrl(portalControllerContext, path, ecmAction);

                final MenubarItem synchronizeItem = new MenubarItem(command, bundle.getString(command), icon, parent, 12, synchronizeURL, null, null, null);
                synchronizeItem.setAjaxDisabled(true);
                synchronizeItem.setDivider(true);

                menubar.add(synchronizeItem);
            } catch (final PortalException e) {
                LOGGER.warn(e.getMessage());
            }
        } else if (extendedInfos.getSynchronizationRootPath() != null) {
            final String rootURL = this.urlFactory.getCMSUrl(portalControllerContext, null, extendedInfos.getSynchronizationRootPath(), null, null, null, null,
                    null, null, null);

            final MenubarItem rootURLItem = new MenubarItem("SYNCHRO_ROOT_URL", bundle.getString("SYNCHRO_ROOT_URL"), null, parent, 12, rootURL, null, null,
                    null);
            rootURLItem.setAjaxDisabled(true);
            rootURLItem.setDivider(true);

            menubar.add(rootURLItem);
        }
    }


    /**
     * Get subscribe link.
     *
     * @param portalControllerContext portal controller context
     * @param cmsContext CMS service context
     * @param bundle internationalization bundle
     * @throws PortalException
     */
    protected void getSubscribeLink(PortalControllerContext portalControllerContext, CMSServiceCtx cmsContext, List<MenubarItem> menubar, Bundle bundle,
            CMSExtendedDocumentInfos extendedInfos) throws CMSException {
        // Current document
        final Document document = (Document) cmsContext.getDoc();
        final String path = document.getPath();

        if (!"Staple".equals(document.getType()) && !DocumentHelper.hasDraft(document)) {
            final SubscriptionStatus subscriptionStatus = extendedInfos.getSubscriptionStatus();

            if ((subscriptionStatus != null) && (subscriptionStatus != SubscriptionStatus.no_subscriptions)) {
                String url = "";

                try {
                    final MenubarDropdown parent = this.menubarService.getDropdown(portalControllerContext, MenubarDropdown.OTHER_OPTIONS_DROPDOWN_MENU_ID);
                    final MenubarItem subscribeItem = new MenubarItem("SUBSCRIBE_URL", null, null, parent, 11, url, null, null, null);
                    subscribeItem.setAjaxDisabled(true);
                    subscribeItem.setDivider(true);

                    if (subscriptionStatus == SubscriptionStatus.can_subscribe) {
                        url = this.urlFactory.getEcmCommandUrl(portalControllerContext, path, EcmCommonCommands.subscribe);

                        subscribeItem.setUrl(url);
                        subscribeItem.setGlyphicon("glyphicons glyphicons-flag");
                        subscribeItem.setTitle(bundle.getString("SUBSCRIBE_ACTION"));
                    } else if (subscriptionStatus == SubscriptionStatus.can_unsubscribe) {
                        url = this.urlFactory.getEcmCommandUrl(portalControllerContext, path, EcmCommonCommands.unsubscribe);

                        subscribeItem.setUrl(url);
                        subscribeItem.setGlyphicon("glyphicons glyphicons-ban-circle");
                        subscribeItem.setTitle(bundle.getString("UNSUBSCRIBE_ACTION"));

                        // Subscribed indicator menubar item
                        final MenubarItem subscribedIndicator = new MenubarItem("SUBSCRIBED", null, MenubarGroup.CMS, -3, "label label-success");
                        subscribedIndicator.setGlyphicon("halflings halflings-flag");
                        subscribedIndicator.setTooltip(bundle.getString("SUBSCRIBED"));
                        subscribedIndicator.setState(true);
                        menubar.add(subscribedIndicator);
                    } else if (subscriptionStatus == SubscriptionStatus.has_inherited_subscriptions) {
                        subscribeItem.setUrl("#");
                        subscribeItem.setGlyphicon("glyphicons glyphicons-flag");
                        subscribeItem.setTitle(bundle.getString("INHERITED_SUBSCRIPTION"));
                        subscribeItem.setDisabled(true);
                    }

                    menubar.add(subscribeItem);

                } catch (final PortalException ex) {
                    LOGGER.warn(ex.getMessage());
                }
            }
        }
    }

    /**
     * @param portalControllerContext
     * @param cmsContext
     * @param menubar
     * @param bundle
     * @param extendedInfos
     */
    private void getLockLink(PortalControllerContext portalControllerContext, CMSServiceCtx cmsContext, List<MenubarItem> menubar, Bundle bundle,
            CMSExtendedDocumentInfos extendedInfos) {
        
        // Current document
        final Document document = (Document) cmsContext.getDoc();
        final String path = document.getPath();


        final LockStatus lockStatus = extendedInfos.getLockStatus();

        if ((lockStatus != null) && (lockStatus != LockStatus.no_lock)) {
            String url = "";

            try {
                final MenubarDropdown parent = this.menubarService.getDropdown(portalControllerContext, MenubarDropdown.OTHER_OPTIONS_DROPDOWN_MENU_ID);
                final MenubarItem lockItem = new MenubarItem("LOCK_URL", null, null, parent, 14, url, null, null, null);
                lockItem.setAjaxDisabled(true);

                if (lockStatus == LockStatus.can_lock) {
                    url = this.urlFactory.getEcmCommandUrl(portalControllerContext, path, EcmCommonCommands.lock);

                    lockItem.setUrl(url);
                    lockItem.setGlyphicon("glyphicons glyphicons-lock");
                    lockItem.setTitle(bundle.getString("LOCK_ACTION"));
                } else if (lockStatus == LockStatus.can_unlock) {
                    url = this.urlFactory.getEcmCommandUrl(portalControllerContext, path, EcmCommonCommands.unlock);

                    lockItem.setUrl(url);
                    lockItem.setGlyphicon("glyphicons glyphicons-unlock");
                    lockItem.setTitle(bundle.getString("UNLOCK_ACTION"));
                    
                    menubar.add(makeLockedIndicator(cmsContext, bundle, extendedInfos));

                } else if (lockStatus == LockStatus.locked) {
                    lockItem.setUrl("#");
                    lockItem.setGlyphicon("glyphicons glyphicons-lock");
                    lockItem.setTitle(bundle.getString("INHERITED_LOCK"));
                    lockItem.setDisabled(true);
                    
                    menubar.add(makeLockedIndicator(cmsContext, bundle, extendedInfos));
                }

                menubar.add(lockItem);

            } catch (final PortalException ex) {
                LOGGER.warn(ex.getMessage());
            }
        }

    }


    /**
     * Makes MenuBar locked indicator item.
     * 
     * @param cmsContext
     * @param bundle
     * @param extendedInfos
     * @param lockedIndicator
     * 
     */
    protected MenubarItem makeLockedIndicator(CMSServiceCtx cmsContext, Bundle bundle, CMSExtendedDocumentInfos extendedInfos) {
     // Locked indicator menubar item
        final MenubarItem lockedIndicator = new MenubarItem("LOCKED", null, MenubarGroup.CMS, -1, "label label-warning");
        lockedIndicator.setGlyphicon("halflings halflings-lock");
        
        // Display name of lock owner
        String displayName = getUserDisplayName(extendedInfos);
        Object[] args = {displayName};
        String tooltip = bundle.getString("LOCKED_BY", args);
        
        lockedIndicator.setTooltip(tooltip);
        lockedIndicator.setState(true);
               
        String currentUser = cmsContext.getRequest().getRemoteUser();
        boolean currentUserIsOwner = StringUtils.equals(currentUser, extendedInfos.getLockOwner());
        if(currentUserIsOwner){
            lockedIndicator.setGlyphicon("glyphicons glyphicons-user-lock");
            lockedIndicator.setTooltip(bundle.getString("LOCKED"));
        }
        return lockedIndicator;
    }


    /**
     * Gets current user display name.
     * 
     * @param extendedInfos
     * @return current user display name
     */
    protected String getUserDisplayName(CMSExtendedDocumentInfos extendedInfos) {
        PersonService personService = DirServiceFactory.getService(PersonService.class);
        Person person = personService.getPerson(extendedInfos.getLockOwner());
        String displayName;
        if ((person != null) && StringUtils.isNotBlank(person.getDisplayName())) {
            displayName = person.getDisplayName();
        } else {
            displayName = extendedInfos.getLockOwner();
        }
        return displayName;
    }


    /**
     * Get link to validation workflow tasks.
     *
     * @param portalControllerContext
     * @param cmsContext
     * @param menubar
     * @param bundle
     * @param extendedInfos
     */
    protected void getValidationWfLink(PortalControllerContext portalControllerContext, CMSServiceCtx cmsContext, CMSPublicationInfos pubInfos,
            List<MenubarItem> menubar, Bundle bundle, CMSExtendedDocumentInfos extendedInfos) throws CMSException {
        // Document
        final Document document = (Document) cmsContext.getDoc();

        if (!DocumentHelper.isFolder(document)) {

            if (pubInfos.isLiveSpace() && !pubInfos.hasDraft() && ContextualizationHelper.isCurrentDocContextualized(cmsContext)) {

                final Boolean isValidationWfRunning = extendedInfos.getIsValidationWorkflowRunning();
                final String url = StringUtils.EMPTY;

                final MenubarDropdown parent = this.menubarService.getDropdown(portalControllerContext, MenubarDropdown.CMS_EDITION_DROPDOWN_MENU_ID);
                final MenubarItem validationWfItem = new MenubarItem("VALIDATION_WF_URL", null, null, parent, 13, url, null, null, "fancyframe_refresh");

                final String onClick = this.generateCallbackParams(portalControllerContext, cmsContext);
                validationWfItem.setOnclick(onClick);

                if (BooleanUtils.isTrue(isValidationWfRunning)) {
                    // Access to current validation workflow task
                    final Map<String, String> requestParameters = new HashMap<String, String>();
                    final String followWfURL = this.cmsService
                            .getEcmUrl(cmsContext, EcmViews.followWfValidation, pubInfos.getDocumentPath(), requestParameters);

                    validationWfItem.setUrl(followWfURL);
                    validationWfItem.setTitle(bundle.getString("FOLLOW_VALIDATION_WF"));
                    menubar.add(validationWfItem);

                } else if (!DocumentConstants.APPROVED_DOC_STATE.equals(document.getState()) && pubInfos.isEditableByUser()) {
                    // We can start a validation workflow
                    final Map<String, String> requestParameters = new HashMap<String, String>();
                    final String startWfURL = this.cmsService.getEcmUrl(cmsContext, EcmViews.startValidationWf, pubInfos.getDocumentPath(), requestParameters);

                    validationWfItem.setUrl(startWfURL);
                    validationWfItem.setTitle(bundle.getString("START_VALIDATION_WF"));
                    menubar.add(validationWfItem);
                }

            }

        }

    }


    /**
     * @param portalControllerContext
     * @param cmsContext
     * @return the on click action for a document refresh
     */
    private String generateCallbackParams(PortalControllerContext portalControllerContext, CMSServiceCtx cmsContext) {
        // Callback URL
        final String callbackURL = this.urlFactory.getCMSUrl(portalControllerContext, null, "_NEWID_", null, null, "_LIVE_", null, null, null, null);
        // ECM base URL
        final String ecmBaseURL = this.cmsService.getEcmDomain(cmsContext);

        // On click action
        final StringBuilder onClick = new StringBuilder();
        onClick.append("javascript:setCallbackFromEcmParams('");
        onClick.append(callbackURL);
        onClick.append("', '");
        onClick.append(ecmBaseURL);
        onClick.append("');");
        return onClick.toString();
    }

    /**
     * Get link to remote publishing tasks.
     *
     * @param portalControllerContext
     * @param cmsContext
     * @param menubar
     * @param bundle
     * @param extendedInfos
     * @throws CMSException
     */
    protected void getRemotePublishingLink(PortalControllerContext portalControllerContext, CMSServiceCtx cmsContext, CMSPublicationInfos pubInfos,
            List<MenubarItem> menubar, Bundle bundle, CMSExtendedDocumentInfos extendedInfos) throws CMSException {
        // Document
        final Document document = (Document) cmsContext.getDoc();

        if (!DocumentHelper.isFolder(document)) {

            // DCH: FIXME: state is "ExtendedInfo"...
            // DCH: FIXME: condition for collaboratives sapces?: config
            boolean isDraftActivated = pubInfos.hasDraft() || pubInfos.isDraft();
            if(!isDraftActivated){
                if (pubInfos.isRemotePublishable() && pubInfos.isLiveSpace() && ContextualizationHelper.isCurrentDocContextualized(cmsContext)) {
                    final String url = "#";
    
                    final MenubarDropdown parent = this.menubarService.getDropdown(portalControllerContext, MenubarDropdown.CMS_EDITION_DROPDOWN_MENU_ID);
                    final MenubarItem remotePubItem = new MenubarItem("REMOTE_PUBLISHING_URL", bundle.getString("REMOTE_PUBLISHING"), null, parent, 14, url, null,
                            null, null);
    
                    final Boolean isValidationWfRunning = extendedInfos.getIsValidationWorkflowRunning();
    
                    if (BooleanUtils.isFalse(isValidationWfRunning)) {
                        // We can publish remotly
                        final Map<String, String> requestParameters = new HashMap<String, String>();
                        final String remotePublishingURL = this.cmsService.getEcmUrl(cmsContext, EcmViews.remotePublishing, pubInfos.getDocumentPath(),
                                requestParameters);
    
                        remotePubItem.setUrl(remotePublishingURL);
                        remotePubItem.setHtmlClasses("fancyframe_refresh");
                        menubar.add(remotePubItem);
                    } else {
                        remotePubItem.setDisabled(true);
                        menubar.add(remotePubItem);
                    }
    
                }
            }
        }

    }

    /**
     * Get edit CMS content link.
     *
     * @param portalControllerContext portal controller context
     * @param cmsContext CMS service context
     * @param bundle internationalization bundle
     */
    protected void getEditLink(PortalControllerContext portalControllerContext, CMSServiceCtx cmsContext, CMSPublicationInfos pubInfos,
            List<MenubarItem> menubar, Bundle bundle) throws CMSException {
        if (cmsContext.getRequest().getRemoteUser() == null) {
            return;
        }

        // Current document
        final Document document = (Document) cmsContext.getDoc();

        if (DocumentHelper.isRemoteProxy(cmsContext, pubInfos)) {
            return;
        }

        if (pubInfos.isEditableByUser()) {
            if (ContextualizationHelper.isCurrentDocContextualized(cmsContext)) {
                final DocumentType cmsItemType = this.customizer.getCMSItemTypes().get(document.getType());
                if ((cmsItemType != null) && cmsItemType.isSupportsPortalForms()) {
                    // Callback URL
                    final String callbackURL = this.urlFactory
                            .getCMSUrl(portalControllerContext, null, "_NEWID_", null, null, "_LIVE_", null, null, null, null);
                    // ECM base URL
                    final String ecmBaseURL = this.cmsService.getEcmDomain(cmsContext);

                    final Map<String, String> requestParameters = new HashMap<String, String>();
                    String docPathToEdit = pubInfos.getDocumentPath();
                    if(pubInfos.hasDraft()){
                        docPathToEdit = pubInfos.getDraftPath();
                    }
                    final String url = this.cmsService.getEcmUrl(cmsContext, EcmViews.editDocument, docPathToEdit, requestParameters);

                    // On click action
                    final StringBuilder onClick = new StringBuilder();
                    onClick.append("javascript:setCallbackFromEcmParams('");
                    onClick.append(callbackURL);
                    onClick.append("', '");
                    onClick.append(ecmBaseURL);
                    onClick.append("');");

                    String editLabel = null;
                    if (StringUtils.isNotBlank(pubInfos.getPublishSpacePath())) {
                        if (!pubInfos.isLiveSpace() && !DocumentHelper.isInLiveMode(cmsContext, pubInfos) && pubInfos.isBeingModified()) {
                            editLabel = bundle.getString("EDIT_LIVE_VERSION");
                        } else if (pubInfos.isLiveSpace() && (pubInfos.isDraft() || pubInfos.hasDraft())) {
                            editLabel = bundle.getString("EDIT_DRAFT");
                        } else {
                            editLabel = bundle.getString("EDIT");
                        }
                    } else {
                        // si contextualisé mais non rattaché à un espace, on considère qu'il n'y a pas de cycle de vie
                        // cas greta où les stages sont contextualisés mais non rattachés au portalsite
                        editLabel = bundle.getString("EDIT");
                    }

                    final MenubarDropdown parent = this.menubarService.getDropdown(portalControllerContext, MenubarDropdown.CMS_EDITION_DROPDOWN_MENU_ID);

                    // Menubar item
                    final MenubarItem item = new MenubarItem("EDIT", editLabel, "glyphicons glyphicons-pencil", parent, 1, url, null, onClick.toString(),
                            "fancyframe_refresh");
                    item.setAjaxDisabled(true);

                    menubar.add(item);
                } else if (pubInfos.hasDraft()) {

                }
            }
        }
    }


    /**
     * Get move link.
     *
     * @param portalControllerContext portal controller context
     * @param cmsContext CMS context
     * @param menubar menubar
     * @param bundle internationalization bundle
     * @throws CMSException
     */
    protected void getMoveLink(PortalControllerContext portalControllerContext, CMSServiceCtx cmsContext, CMSPublicationInfos pubInfos,
            List<MenubarItem> menubar, Bundle bundle) throws CMSException {
        if (cmsContext.getRequest().getRemoteUser() == null) {
            return;
        }

        // Current document
        final Document document = (Document) cmsContext.getDoc();

        boolean authorizedSpace = true;
        if (!NuxeoCompatibility.isVersionGreaterOrEqualsThan(NuxeoCompatibility.VERSION_62)) {
            if (DocumentHelper.isRemoteProxy(cmsContext, pubInfos)) {
                return;
            }
            authorizedSpace = pubInfos.isLiveSpace();
        }

        if (authorizedSpace && pubInfos.isEditableByUser() && !pubInfos.isDraft() && ContextualizationHelper.isCurrentDocContextualized(cmsContext)) {
            // Nuxeo controller
            final NuxeoController nuxeoController = new NuxeoController(portalControllerContext.getRequest(), portalControllerContext.getResponse(),
                    portalControllerContext.getPortletCtx());

            // CMS item type
            final DocumentType cmsItemType = this.customizer.getCMSItemTypes().get(document.getType());

            if ((cmsItemType != null) && cmsItemType.isSupportsPortalForms()) {
                // We do not authorize remote proxies move to keep consistent refrences
                boolean isMovable = cmsItemType.isMovable();
                if (DocumentHelper.isRemoteProxy(cmsContext, pubInfos)) {
                    isMovable = false;
                }

                if (isMovable) {
                    // Move document popup URL
                    String moveDocumentURL;
                    try {
                        final Map<String, String> properties = new HashMap<String, String>();
                        properties.put(MoveDocumentPortlet.DOCUMENT_PATH_WINDOW_PROPERTY, document.getPath());
                        properties.put(MoveDocumentPortlet.CMS_BASE_PATH_WINDOW_PROPERTY, nuxeoController.getBasePath());
                        properties.put(MoveDocumentPortlet.ACCEPTED_TYPES_WINDOW_PROPERTY, cmsItemType.getName());

                        moveDocumentURL = this.urlFactory.getStartPortletUrl(portalControllerContext, "toutatice-portail-cms-nuxeo-move-portlet-instance",
                                properties, true);
                    } catch (final PortalException e) {
                        moveDocumentURL = null;
                    }

                    if (moveDocumentURL != null) {
                        final MenubarDropdown parent = this.menubarService.getDropdown(portalControllerContext, MenubarDropdown.CMS_EDITION_DROPDOWN_MENU_ID);

                        final MenubarItem item = new MenubarItem("MOVE", bundle.getString("MOVE"), "glyphicons glyphicons-move", parent, 2, moveDocumentURL,
                                null, null, "fancyframe_refresh");
                        item.setAjaxDisabled(true);

                        menubar.add(item);
                    }
                }
            }
        }

    }


    /**
     * Get reorder link.
     *
     * @param portalControllerContext portal controller context
     * @param cmsContext CMS context
     * @param menubar menubar
     * @param bundle internationalization bundle
     * @throws CMSException
     */
    protected void getReorderLink(PortalControllerContext portalControllerContext, CMSServiceCtx cmsContext, CMSPublicationInfos pubInfos,
            List<MenubarItem> menubar, Bundle bundle) throws CMSException {
        if (cmsContext.getRequest().getRemoteUser() == null) {
            return;
        }

        // Current document
        final Document document = (Document) cmsContext.getDoc();

        boolean authorizedSpace = true;
        if (!NuxeoCompatibility.isVersionGreaterOrEqualsThan(NuxeoCompatibility.VERSION_62)) {
            if (DocumentHelper.isRemoteProxy(cmsContext, pubInfos)) {
                return;
            }
            authorizedSpace = pubInfos.isLiveSpace();
        }

        if (authorizedSpace && pubInfos.isEditableByUser() && ContextualizationHelper.isCurrentDocContextualized(cmsContext)) {
            // CMS item type
            final DocumentType cmsItemType = this.customizer.getCMSItemTypes().get(document.getType());

            if ((cmsItemType != null) && cmsItemType.isOrdered()) {
                // Reorder documents popup URL
                String reorderDocumentsURL;
                try {
                    final Map<String, String> properties = new HashMap<String, String>();
                    properties.put(ReorderDocumentsPortlet.PATH_WINDOW_PROPERTY, document.getPath());

                    reorderDocumentsURL = this.urlFactory.getStartPortletUrl(portalControllerContext, "toutatice-portail-cms-nuxeo-reorder-portlet-instance",
                            properties, true);
                } catch (final PortalException e) {
                    reorderDocumentsURL = null;
                }

                if (reorderDocumentsURL != null) {
                    final MenubarDropdown parent = this.menubarService.getDropdown(portalControllerContext, MenubarDropdown.CMS_EDITION_DROPDOWN_MENU_ID);

                    final MenubarItem item = new MenubarItem("REORDER", bundle.getString("REORDER"), "glyphicons glyphicons-sorting", parent, 3,
                            reorderDocumentsURL, null, null, "fancyframe_refresh");
                    item.setAjaxDisabled(true);
                    item.setBreadcrumb(true);

                    menubar.add(item);
                }
            }
        }

    }


    /**
     * Get create CMS content link.
     *
     * @param portalControllerContext portal controller context
     * @param cmsContext CMS service context
     * @param bundle internationalization bundle
     */
    protected void getCreateLink(PortalControllerContext portalControllerContext, CMSServiceCtx cmsContext, CMSPublicationInfos pubInfos,
            List<MenubarItem> menubar, Bundle bundle) throws CMSException {
        if (cmsContext.getRequest().getRemoteUser() == null) {
            return;
        }

        // Creation type
        final String creationType = cmsContext.getCreationType();
        // Creation path
        final String creationPath = cmsContext.getCreationPath();
        // Parent document
        Document parentDoc = (Document) cmsContext.getDoc();
        if (creationPath != null) {
            parentDoc = (Document) this.cmsService.getContent(cmsContext, creationPath).getNativeItem();
        }

        // Do not add into remote proxy
        if (DocumentHelper.isRemoteProxy(cmsContext, pubInfos)) {
            return;
        }

        if ((creationPath != null) || ContextualizationHelper.isCurrentDocContextualized(cmsContext)) {
            // Test ergo JSS
            final String callbackURL = this.urlFactory.getRefreshPageUrl(portalControllerContext, true);

            // ECM base URL
            final String ecmBaseURL = this.cmsService.getEcmDomain(cmsContext);
            // On click action
            final StringBuilder builder = new StringBuilder();
            builder.append("javascript:setCallbackFromEcmParams('");
            builder.append(callbackURL);
            builder.append("', '");
            builder.append(ecmBaseURL);
            builder.append("');");
            final String onclick = builder.toString();

            // Sub-types
            final Map<String, String> subTypes = pubInfos.getSubTypes();
            final Comparator<DocumentType> comparator = new CMSItemTypeComparator(bundle);
            final SortedMap<DocumentType, String> folderishTypes = new TreeMap<DocumentType, String>(comparator);
            final SortedMap<DocumentType, String> notFolderishTypes = new TreeMap<DocumentType, String>(comparator);

            final Map<String, DocumentType> managedTypes = this.customizer.getCMSItemTypes();
            final DocumentType containerDocType = managedTypes.get(parentDoc.getType());
            if (containerDocType != null) {
                // Current portal
                final Portal portal = PortalObjectUtils.getPortal(cmsContext.getControllerContext());
                // Space site indicator
                final boolean spaceSite = PortalObjectUtils.isSpaceSite(portal);

                for (final String docType : subTypes.keySet()) {
                    // Is this type managed at portal level ?
                    if (containerDocType.getPortalFormSubTypes().contains(docType) && ((creationType == null) || creationType.equals(docType))) {
                        final DocumentType docTypeDef = managedTypes.get(docType);
                        if ((docTypeDef != null) && docTypeDef.isSupportsPortalForms()) {
                            // CMS item type
                            final DocumentType cmsItemType = managedTypes.get(docType);
                            if ((cmsItemType != null) && !(spaceSite && "PortalPage".equals(cmsItemType.getName()))) {
                                // URL
                                final Map<String, String> requestParameters = new HashMap<String, String>();
                                requestParameters.put("type", docType);
                                final String url = this.cmsService
                                        .getEcmUrl(cmsContext, EcmViews.createDocument, pubInfos.getDocumentPath(), requestParameters);

                                if (cmsItemType.isFolderish()) {
                                    folderishTypes.put(cmsItemType, url);
                                } else {
                                    notFolderishTypes.put(cmsItemType, url);
                                }
                            }
                        }
                    }
                }
            }

            final int size = folderishTypes.size() + notFolderishTypes.size();
            if (size == 1) {
                // Direct link

                Entry<DocumentType, String> entry;
                if (folderishTypes.size() == 1) {
                    entry = folderishTypes.entrySet().iterator().next();
                } else {
                    entry = notFolderishTypes.entrySet().iterator().next();
                }

                final String url = entry.getValue();

                // Menubar item
                final MenubarItem item = new MenubarItem("ADD", bundle.getString("ADD"), "halflings halflings-plus", MenubarGroup.CMS, 2, url, null, onclick,
                        "fancyframe_refresh");
                item.setAjaxDisabled(true);

                menubar.add(item);
            } else if (size > 0) {
                // Dropdown menu
                final MenubarDropdown dropdown = new MenubarDropdown("ADD", bundle.getString("ADD"), "halflings halflings-plus", MenubarGroup.CMS, 2);
                this.menubarService.addDropdown(portalControllerContext, dropdown);

                int order = 1;
                boolean divider = false;

                for (final Entry<DocumentType, String> entry : folderishTypes.entrySet()) {
                    final DocumentType cmsItemType = entry.getKey();
                    final String url = entry.getValue();

                    // Type name
                    final String typeName = StringUtils.upperCase(cmsItemType.getName());
                    final String displayName = bundle.getString(typeName, cmsItemType.getCustomizedClassLoader());

                    // Menubar item
                    final MenubarItem item = new MenubarItem("ADD_" + typeName, displayName, cmsItemType.getGlyph(), dropdown, order, url, null, onclick,
                            "fancyframe_refresh");
                    item.setAjaxDisabled(true);

                    menubar.add(item);

                    order++;
                    divider = true;
                }

                for (final Entry<DocumentType, String> entry : notFolderishTypes.entrySet()) {
                    final DocumentType cmsItemType = entry.getKey();
                    final String url = entry.getValue();

                    // Type name
                    final String typeName = StringUtils.upperCase(cmsItemType.getName());
                    final String displayName = bundle.getString(typeName, cmsItemType.getCustomizedClassLoader());

                    // Menubar item
                    final MenubarItem item = new MenubarItem("ADD_" + typeName, displayName, cmsItemType.getGlyph(), dropdown, order, url, null, onclick,
                            "fancyframe_refresh");
                    item.setAjaxDisabled(true);
                    item.setDivider(divider);

                    menubar.add(item);

                    order++;
                    divider = false;
                }
            }
        }
    }


    /**
     * Get delete link.
     *
     * @param portalControllerContext portal controller context
     * @param cmsContext CMS service context
     * @param bundle internationalization bundle
     */
    protected void getDeleteLink(PortalControllerContext portalControllerContext, CMSServiceCtx cmsContext, CMSPublicationInfos pubInfos,
            List<MenubarItem> menubar, Bundle bundle) throws CMSException, PortalException {
        if (cmsContext.getRequest().getRemoteUser() == null) {
            return;
        }

        // Document
        final Document document = (Document) cmsContext.getDoc();

        // Do not delete remote proxy
        if (DocumentHelper.isRemoteProxy(cmsContext, pubInfos)) {
            return;
        }

        // Do not delete published elements
        if (pubInfos.isDeletableByUser() && (pubInfos.isLiveSpace() || DocumentHelper.isInLiveMode(cmsContext, pubInfos))) {
            if (ContextualizationHelper.isCurrentDocContextualized(cmsContext)) {
                final DocumentType docTypeDef = this.customizer.getCMSItemTypes().get(document.getType());
                if ((docTypeDef != null) && docTypeDef.isSupportsPortalForms()) {
                    final MenubarDropdown parent = this.menubarService.getDropdown(portalControllerContext, MenubarDropdown.CMS_EDITION_DROPDOWN_MENU_ID);

                    // Menubar item
                    String itemLabel = bundle.getString("DELETE");
                    if (pubInfos.isDraft()) {
                        itemLabel = bundle.getString("DELETE_DRAFT");
                    }
                    final MenubarItem item = new MenubarItem("DELETE", itemLabel, "glyphicons glyphicons-bin", parent, 20, null, null, null, null);
                    item.setAjaxDisabled(true);
                    item.setDivider(true);

                    if (docTypeDef.isRootType()) {
                        item.setUrl("#");
                        item.setDisabled(true);
                        item.setTooltip(bundle.getString("CANNOT_DELETE_ROOT"));
                    } else {
                        // Fancybox properties
                        final Map<String, String> properties = new HashMap<String, String>();
                        properties.put("docId", document.getId());
                        properties.put("docPath", document.getPath());

                        // Fancybox identifier
                        final String fancyboxId = cmsContext.getResponse().getNamespace() + "_PORTAL_DELETE";

                        // Fancybox delete action URL
                        String removeURL = this.urlFactory.getPutDocumentInTrashUrl(portalControllerContext, pubInfos.getLiveId(), pubInfos.getDocumentPath());
                        
                        // Case of Draft document directly deleted
                        if (pubInfos.isDraft()) {
                            
                            String redirectionPath;
                            if(!pubInfos.isNotOrphanDraft()){
                                redirectionPath = pubInfos.getDraftContextualizationPath();
                            } else {
                                String hasDraftDocWebId = DocumentHelper.getDocWebIdHavingDraft(document);
                                redirectionPath = IWebIdService.FETCH_PATH_PREFIX.concat(hasDraftDocWebId);
                            }
                            removeURL = this.urlFactory.getEcmCommandUrl(portalControllerContext, document.getPath(), EcmCommonCommands.deleteDocument, redirectionPath);
                        }

                        // Fancybox HTML data
                        final String fancybox = this.generateDeleteConfirmationFancybox(properties, bundle, fancyboxId, removeURL);
                        item.setAssociatedHTML(fancybox);

                        // URL
                        final String url = "#" + fancyboxId;
                        item.setUrl(url);

                        item.setHtmlClasses("fancybox_inline");
                    }

                    menubar.add(item);
                }
            }
        }
    }

    /**
     * Generate erase confirmation fancybox.
     *
     * @param bundle bundle
     * @param urlDelete the command for delete
     * @return fancybox DOM element
     * @throws UnsupportedEncodingException
     */
    private String generateEraseFancyBox(Bundle bundle, String urlErase) {
        // Root
        Element root = DOM4JUtils.generateDivElement("hidden");

        // Container
        Element container = DOM4JUtils.generateDivElement("container-fluid text-center");
        DOM4JUtils.addAttribute(container, HTMLConstants.ID, "erase_cms_page");
        root.add(container);

        // Message
        Element message = DOM4JUtils.generateElement(HTMLConstants.P, null, bundle.getString("ERASE_CONFIRM_MESSAGE"));
        container.add(message);

        // OK button
        Element okButton = DOM4JUtils.generateLinkElement(urlErase, null, null, "btn btn-default btn-warning", bundle.getString("YES"),
                "halflings halflings-alert");
        container.add(okButton);

        // Cancel button
        Element cancelButton = DOM4JUtils.generateElement(HTMLConstants.BUTTON, "btn btn-default", bundle.getString("NO"));
        DOM4JUtils.addAttribute(cancelButton, HTMLConstants.TYPE, HTMLConstants.INPUT_TYPE_BUTTON);
        DOM4JUtils.addAttribute(cancelButton, HTMLConstants.ONCLICK, "closeFancybox()");
        container.add(cancelButton);

        return DOM4JUtils.write(root);
    }


    /**
     * Generate delete confirmation fancybox HTML data.
     *
     * @param properties fancybox properties
     * @param bundle internationalization bundle
     * @param fancyboxId fancybox identifier
     * @param actionURL delete action URL
     * @return fancybox HTML data
     */
    private String generateDeleteConfirmationFancybox(Map<String, String> properties, Bundle bundle, String fancyboxId, String actionURL) {
        // Fancybox container
        final Element fancyboxContainer = DOM4JUtils.generateDivElement("hidden");

        // Container
        final Element container = DOM4JUtils.generateDivElement(null);
        DOM4JUtils.addAttribute(container, HTMLConstants.ID, fancyboxId);
        fancyboxContainer.add(container);

        // Form
        final Element form = DOM4JUtils.generateElement(HTMLConstants.FORM, "text-center", null, null, AccessibilityRoles.FORM);
        DOM4JUtils.addAttribute(form, HTMLConstants.ACTION, actionURL);
        DOM4JUtils.addAttribute(form, HTMLConstants.METHOD, HTMLConstants.FORM_METHOD_POST);
        container.add(form);

        // Message
        final Element message = DOM4JUtils.generateElement(HTMLConstants.P, null, bundle.getString("CMS_DELETE_CONFIRM_MESSAGE"));
        form.add(message);

        // Hidden fields
        for (final Entry<String, String> property : properties.entrySet()) {
            final Element hidden = DOM4JUtils.generateElement(HTMLConstants.INPUT, null, null);
            DOM4JUtils.addAttribute(hidden, HTMLConstants.TYPE, HTMLConstants.INPUT_TYPE_HIDDEN);
            DOM4JUtils.addAttribute(hidden, HTMLConstants.NAME, property.getKey());
            DOM4JUtils.addAttribute(hidden, HTMLConstants.VALUE, property.getValue());
            form.add(hidden);
        }

        // OK button
        final Element okButton = DOM4JUtils
                .generateElement(HTMLConstants.BUTTON, "btn btn-warning", bundle.getString("YES"), "halflings halflings-alert", null);
        DOM4JUtils.addAttribute(okButton, HTMLConstants.TYPE, HTMLConstants.INPUT_TYPE_SUBMIT);
        form.add(okButton);

        // Cancel button
        final Element cancelButton = DOM4JUtils.generateElement(HTMLConstants.BUTTON, "btn btn-default", bundle.getString("NO"));
        DOM4JUtils.addAttribute(cancelButton, HTMLConstants.TYPE, HTMLConstants.INPUT_TYPE_BUTTON);
        DOM4JUtils.addAttribute(cancelButton, HTMLConstants.ONCLICK, "closeFancybox()");
        form.add(cancelButton);

        return DOM4JUtils.write(fancyboxContainer);
    }


    /**
     * Add contextualization link item.
     *
     * @param portalControllerContext portal controller context
     * @param cmsContext CMS service context
     * @param menubar menubar items
     * @param bundle internationalization bundle
     * @param displayName space display name
     * @param url contextualization link URL
     * @throws Exception
     */
    protected void addContextualizationLinkItem(PortalControllerContext portalControllerContext, CMSServiceCtx cmsContext, List<MenubarItem> menubar,
            Bundle bundle, String displayName, String url) throws CMSException {
        final MenubarItem item = new MenubarItem("CONTEXTUALIZE", bundle.getString("CONTEXTUALIZE_SPACE", displayName), "halflings halflings-level-up",
                MenubarGroup.SPECIFIC, 1, url, null, null, null);
        item.setAjaxDisabled(true);

        menubar.add(item);
    }


    /**
     * Affiche un lien de recontextualisation explicite (dans une page existante ou une nouvelle page).
     *
     * @param portalControllerContext portal controller context
     * @param cmsContext CMS service context
     * @param bundle internationalization bundle
     */
    protected void getContextualizationLink(PortalControllerContext portalControllerContext, CMSServiceCtx cmsContext, CMSPublicationInfos pubInfos,
            List<MenubarItem> menubar, Bundle bundle) throws CMSException {
        // Request
        final PortletRequest request = cmsContext.getRequest();

        if (!WindowState.MAXIMIZED.equals(request.getWindowState())) {
            return;
        }
        
        // Draft
        if(pubInfos.isDraft()){
            return;
        }

        Page currentPage = null;
        final Window window = (Window) request.getAttribute("osivia.window");
        if (window != null) {
            currentPage = window.getPage();
        }

        // Document
        final Document document = (Document) cmsContext.getDoc();

        // On regarde dans quelle page le contenu ext contextualisé
        Page page;
        try {
            page = this.urlFactory.getPortalCMSContextualizedPage(portalControllerContext, document.getPath());
        } catch (final PortalException e) {
            page = null;
        }

        // Si la page correspond à la page courant on affiche pas le lien
        if ((page == null) || !page.getId().equals(currentPage.getId())) {
            // On détermine le nom de l'espace
            String spaceDisplayName = null;

            if (page != null) {
                // Soit le nom de la page
                spaceDisplayName = PortalObjectUtils.getDisplayName(page, request.getLocale());
            } else {

                if (pubInfos.getPublishSpacePath() != null) {
                    final CMSItem pubConfig = this.cmsService.getSpaceConfig(cmsContext, pubInfos.getPublishSpacePath());
                    if ("1".equals(pubConfig.getProperties().get("contextualizeInternalContents"))) {
                        spaceDisplayName = pubInfos.getPublishSpaceDisplayName();
                    }

                }
            }

            if (spaceDisplayName != null) {
                final String url = this.urlFactory.getCMSUrl(portalControllerContext, currentPage.getId().toString(PortalObjectPath.CANONICAL_FORMAT),
                        document.getPath(), null, IPortalUrlFactory.CONTEXTUALIZATION_PORTAL, null, null, null, null, null);

                this.addContextualizationLinkItem(portalControllerContext, cmsContext, menubar, bundle, spaceDisplayName, url);
            }
        }
    }


    /**
     * Add permalink item.
     *
     * @param portalControllerContext portal controller context
     * @param cmsContext CMS service context
     * @param menubar menubar items
     * @param bundle internationalization bundle
     * @param url permalink URL
     */
    protected void addPermaLinkItem(PortalControllerContext portalControllerContext, CMSServiceCtx cmsContext, List<MenubarItem> menubar, Bundle bundle,
            String url) throws CMSException {
        // Fancybox identifier
        String id = cmsContext.getResponse().getNamespace() + "PermalinkModal";

        // Fancybox HTML content
        String htmlContent = this.generatePermalinkModal(bundle, id, url);


        // Parent dropdown menu
        MenubarDropdown parent = this.menubarService.getDropdown(portalControllerContext, MenubarDropdown.SHARE_DROPDOWN_MENU_ID);

        // Menubar item
        MenubarItem item = new MenubarItem("PERMALINK", bundle.getString("PERMALINK"), "glyphicons glyphicons-link", parent, 1, "#", null, null, null);
        item.getData().put("toggle", "modal");
        item.getData().put("target", "#" + id);
        item.getData().put("backdrop", String.valueOf(false));
        item.setAjaxDisabled(true);
        item.setAssociatedHTML(htmlContent);

        menubar.add(item);
    }

    /**
     * Add e-mail link.
     *
     * @param portalControllerContext portal controller context
     * @param cmsContext CMS context
     * @param menubar menubar items
     * @param bundle internationalization bundle
     * @throws CMSException
     */
    private void addEmailLink(PortalControllerContext portalControllerContext, CMSServiceCtx cmsContext, CMSPublicationInfos pubInfos,
            List<MenubarItem> menubar, Bundle bundle) throws CMSException {

        if (pubInfos.isLiveSpace() || (!pubInfos.isLiveSpace() && pubInfos.isPublished())) {

            final Map<String, String> requestParameters = new HashMap<String, String>();
            final String url = this.getCmsService().getEcmUrl(cmsContext, EcmViews.shareDocument, pubInfos.getDocumentPath(), requestParameters);

            // Callback URL
            final String callbackURL = this.getUrlFactory().getCMSUrl(portalControllerContext, null, "_NEWID_", null, null, "_LIVE_", null, null, null, null);
            // ECM base URL
            final String ecmBaseURL = this.getCmsService().getEcmDomain(cmsContext);

            // On click action
            final StringBuilder onClick = new StringBuilder();
            onClick.append("javascript:setCallbackFromEcmParams('");
            onClick.append(callbackURL);
            onClick.append("', '");
            onClick.append(ecmBaseURL);
            onClick.append("');");

            final MenubarDropdown parent = this.menubarService.getDropdown(portalControllerContext, MenubarDropdown.SHARE_DROPDOWN_MENU_ID);

            final MenubarItem item = new MenubarItem("SHARE_BY_EMAIL", bundle.getString("SHARE_EMAIL"), "social social-e-mail", parent, 2, url, null,
                    onClick.toString(), "fancyframe_refresh");
            item.setAjaxDisabled(true);

            menubar.add(item);
        }
    }


    /**
     * Compute permalink URL.
     *
     * @param portalControllerContext portal controller context
     * @param cmsContext CMS service context
     * @param bundle internationalization bundle
     * @return permalink URL
     */
    protected String computePermaLinkUrl(PortalControllerContext portalControllerContext, CMSServiceCtx cmsContext, CMSPublicationInfos pubInfos,
            List<MenubarItem> menubar, Bundle bundle) throws CMSException {
        // Request
        final PortletRequest request = cmsContext.getRequest();


        // Selectors parameters
        Map<String, String> parameters = null;
        final String selectors = request.getParameter("selectors");
        if (selectors != null) {
            parameters = new HashMap<String, String>();
            final Map<String, List<String>> decodedSelectors = PageSelectors.decodeProperties(selectors);
            parameters.put("selectors", PageSelectors.encodeProperties(decodedSelectors));
        }

        final String path = this.customizer.getContentWebIdPath(cmsContext);

        // URL
        String url;
        String permaLinkType = IPortalUrlFactory.PERM_LINK_TYPE_CMS;

        // url of type share for Workspaces and proxies
        if (this.hasWebId(cmsContext) && !DocumentHelper.isRemoteProxy(cmsContext, pubInfos)) {
            if (pubInfos.isLiveSpace() || (!pubInfos.isLiveSpace() && StringUtils.isNotBlank(pubInfos.getPublishSpacePath()))) {
                permaLinkType = IPortalUrlFactory.PERM_LINK_TYPE_SHARE;
            }
        }

        try {
            url = this.getUrlFactory().getPermaLink(portalControllerContext, null, parameters, path, permaLinkType);
        } catch (final PortalException e) {
            url = null;
        }
        return ContextualizationHelper.getLivePath(url);
    }


    /**
     * Get permalink display indicator.
     *
     * @param portalControllerContext portal controller context
     * @param cmsContext CMS service context
     * @param bundle internationalization bundle
     * @return true if permalink must be displayed
     */
    protected boolean mustDisplayPermalink(PortalControllerContext portalControllerContext, CMSServiceCtx cmsContext, List<MenubarItem> menubar, Bundle bundle)
            throws CMSException {
        boolean displayPermalink = false;

        if (WindowState.MAXIMIZED.equals(cmsContext.getRequest().getWindowState()) && (cmsContext.getDoc() != null)) {
            // Maximized document: we don't show permalink for lives of publish spaces
            // and Drafts of collaboratives spaces
            displayPermalink = !DocumentHelper.isLocalPublishLive((Document) cmsContext.getDoc())
                    && !DocumentHelper.isDraft((Document) cmsContext.getDoc());
        } else {
            if (ContextualizationHelper.isCurrentDocContextualized(cmsContext)) {
                displayPermalink = true;
            }

            // Current portal
            final Portal portal = PortalObjectUtils.getPortal(cmsContext.getControllerContext());
            // Space site indicator
            final boolean spaceSite = PortalObjectUtils.isSpaceSite(portal);
            if (spaceSite) {
                displayPermalink = false;
            }
        }

        return displayPermalink;
    }


    /**
     * Get permalink link.
     *
     * @param portalControllerContext portal controller context
     * @param cmsContext CMS service context
     * @param bundle internationalization bundle
     */
    protected void getPermaLinkLink(PortalControllerContext portalControllerContext, CMSServiceCtx cmsContext, CMSPublicationInfos pubInfos,
            List<MenubarItem> menubar, Bundle bundle) throws CMSException {
        if (!this.mustDisplayPermalink(portalControllerContext, cmsContext, menubar, bundle)) {
            return;
        }

        final String url = this.computePermaLinkUrl(portalControllerContext, cmsContext, pubInfos, menubar, bundle);
        if (url != null) {
            this.addPermaLinkItem(portalControllerContext, cmsContext, menubar, bundle, url);

            this.addEmailLink(portalControllerContext, cmsContext, pubInfos, menubar, bundle);
        }
    }


    /**
     * Generate permalink modal HTML content.
     * 
     * @param bundle internationalization bundle
     * @param id modal identifier
     * @param url permalink URL
     * @return HTML content
     */
    private String generatePermalinkModal(Bundle bundle, String id, String url) {
        // ARIA label identifier
        String labelId = id + "Label";
        
        // Modal container
        Element container = DOM4JUtils.generateDivElement("modal fade", AccessibilityRoles.DIALOG);
        DOM4JUtils.addAttribute(container, "id", id);
        DOM4JUtils.addAttribute(container, "tabindex", "-1");
        DOM4JUtils.addAriaAttribute(container, "labelledby", labelId);

        // Modal document
        Element document = DOM4JUtils.generateDivElement("modal-dialog", AccessibilityRoles.DOCUMENT);
        container.add(document);

        // Modal content
        Element content = DOM4JUtils.generateDivElement("modal-content");
        document.add(content);

        // Modal header
        Element header = DOM4JUtils.generateDivElement("modal-header");
        content.add(header);

        // Modal close button
        Element close = DOM4JUtils.generateElement("button", "close", null);
        DOM4JUtils.addAttribute(close, "type", "button");
        DOM4JUtils.addDataAttribute(close, "dismiss", "modal");
        DOM4JUtils.addAriaAttribute(close, "label", bundle.getString("CLOSE"));
        header.add(close);

        // Modal close button label
        Element closeLabel = DOM4JUtils.generateElement("span", null, "&times;");
        DOM4JUtils.addAriaAttribute(closeLabel, "hidden", String.valueOf(true));
        close.add(closeLabel);

        // Modal title
        Element title = DOM4JUtils.generateElement("h4", "modal-title", bundle.getString("PERMALINK"), "glyphicons glyphicons-link", null);
        DOM4JUtils.addAttribute(title, "id", labelId);
        header.add(title);

        // Modal body
        Element body = DOM4JUtils.generateDivElement("modal-body");
        content.add(body);

        // Modal body paragraph
        Element paragraph = DOM4JUtils.generateElement("p", null, null);
        body.add(paragraph);

        // Modal body link
        Element link = DOM4JUtils.generateLinkElement(url, null, null, null, url);
        paragraph.add(link);

        return DOM4JUtils.write(container);
    }


    /**
     * Getter for menubarService.
     *
     * @return the menubarService
     */
    public IMenubarService getMenubarService() {
        return this.menubarService;
    }

    /**
     * Getter for cmsService.
     *
     * @return the cmsService
     */
    public CMSService getCmsService() {
        return this.cmsService;
    }

    /**
     * Getter for urlFactory.
     *
     * @return the urlFactory
     */
    public IPortalUrlFactory getUrlFactory() {
        return this.urlFactory;
    }

    /**
     * Getter for customizer.
     *
     * @return the customizer
     */
    public DefaultCMSCustomizer getCustomizer() {
        return this.customizer;
    }

    /**
     * Getter for contributionService.
     *
     * @return the contributionService
     */
    public IContributionService getContributionService() {
        return this.contributionService;
    }

    /**
     * Getter for bundleFactory.
     *
     * @return the bundleFactory
     */
    public IBundleFactory getBundleFactory() {
        return this.bundleFactory;
    }

}
