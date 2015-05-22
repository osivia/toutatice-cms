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

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.portlet.PortletContext;
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
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.contribution.IContributionService;
import org.osivia.portal.api.contribution.IContributionService.EditionState;
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
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSExtendedDocumentInfos;
import org.osivia.portal.core.cms.CMSExtendedDocumentInfos.LockStatus;
import org.osivia.portal.core.cms.CMSExtendedDocumentInfos.SubscriptionStatus;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSItemType;
import org.osivia.portal.core.cms.CMSItemTypeComparator;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.portalobjects.PortalObjectUtils;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoCompatibility;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.PageSelectors;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoConnectionProperties;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.DefaultCMSCustomizer;
import fr.toutatice.portail.cms.nuxeo.portlets.move.MoveDocumentPortlet;
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
	
    /** Menubar CMS edition dropdown menu identifier. */
    private static final String CMS_EDITION_DROPDOWN_MENU_ID = "CMS_EDITION";
    /** Menubar share dropdown menu identifier. */
    private static final String SHARE_DROPDOWN_MENU_ID = "SHARE";

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
        // Bundle factory
        IInternationalizationService internationalizationService = (IInternationalizationService) portletCtx
                .getAttribute(Constants.INTERNATIONALIZATION_SERVICE_NAME);
        this.bundleFactory = internationalizationService.getBundleFactory(this.getClass().getClassLoader());
    }


    /**
     * Format content menubar.
     *
     * @param cmsContext CMS context
     * @throws PortalException 
     */
    @SuppressWarnings("unchecked")
    public void formatDefaultContentMenuBar(CMSServiceCtx cmsContext) throws CMSException {
        if ((cmsContext.getDoc() == null) && (cmsContext.getCreationPath() == null)) {
            return;
        }

        // Request
        PortletRequest request = cmsContext.getRequest();
        // Portal controller context
        PortalControllerContext portalControllerContext = new PortalControllerContext(cmsContext.getPortletCtx(), request, cmsContext.getResponse());
        // Bundle
        Bundle bundle = this.bundleFactory.getBundle(cmsContext.getRequest().getLocale());

        // Extended infos
        CMSExtendedDocumentInfos extendedInfos = new CMSExtendedDocumentInfos();

        if (NuxeoCompatibility.isVersionGreaterOrEqualsThan(NuxeoCompatibility.VERSION_60)) {
            if (cmsContext.getDoc() != null) {
                if (ContextualizationHelper.isCurrentDocContextualized(cmsContext)) {
                    if (cmsContext.getRequest().getRemoteUser() != null) {
                        extendedInfos = this.cmsService.getExtendedDocumentInfos(cmsContext, (((Document) (cmsContext.getDoc())).getPath()));

                    }
                }
            }
        }

        request.setAttribute("osivia.extendedInfos", extendedInfos);

        // Menubar
        List<MenubarItem> menubar = (List<MenubarItem>) request.getAttribute(Constants.PORTLET_ATTR_MENU_BAR);

        // Current portal
        Portal portal = PortalObjectUtils.getPortal(cmsContext.getControllerContext());

        // Check if web page mode layout contains CMS regions and content supports fragments
        // Edition mode is supported by the webpage menu
        boolean webPageFragment = false;
        if (PortalObjectUtils.isSpaceSite(portal) && (cmsContext.getDoc() != null)) {
            String webPagePath = (String) request.getAttribute("osivia.cms.webPagePath");

            String docLivePath = ContextualizationHelper.getLivePath(((Document) (cmsContext.getDoc())).getPath());
            if (StringUtils.equals(docLivePath, webPagePath)) {
                webPageFragment = true;
            }
        }

        try {
            // Live version browser
            this.getLiveContentBrowserLink(portalControllerContext, cmsContext, menubar, bundle);

            // Creation
            this.getCreateLink(portalControllerContext, cmsContext, menubar, bundle);

            if ((cmsContext.getDoc() != null) && !webPageFragment) {
                // Permalink
                this.getPermaLinkLink(portalControllerContext, cmsContext, menubar, bundle);

                // Contextualization
                this.getContextualizationLink(portalControllerContext, cmsContext, menubar, bundle);

                // Change edition mode
                this.getChangeModeLink(portalControllerContext, cmsContext, menubar, bundle);
                // Edition
                this.getEditLink(portalControllerContext, cmsContext, menubar, bundle);
                // Move
                this.getMoveLink(portalControllerContext, cmsContext, menubar, bundle);
                // Suppression
                this.getDeleteLink(portalControllerContext, cmsContext, menubar, bundle);

                // Nuxeo administration
                this.getAdministrationLink(portalControllerContext, cmsContext, menubar, bundle);

                // Nuxeo drive edit
                this.getDriveEditUrl(portalControllerContext, cmsContext, menubar, bundle, extendedInfos);
                // Nuxeo synchronize
                this.getSynchronizeLink(portalControllerContext, cmsContext, menubar, bundle, extendedInfos);
                
                // Follow
                this.getSubscribeLink(portalControllerContext, cmsContext, menubar, bundle, extendedInfos);
                
                // Lock
                this.getLockLink(portalControllerContext, cmsContext, menubar, bundle, extendedInfos);
                
                // Manage
                this.getManageLink(portalControllerContext, cmsContext, menubar, bundle);
                
                // Validation workflow(s)
                this.getValidationWfLink(portalControllerContext, cmsContext, menubar, bundle, extendedInfos);
                
            }
        } catch (CMSException e) {
            if ((e.getErrorCode() == CMSException.ERROR_FORBIDDEN) || (e.getErrorCode() == CMSException.ERROR_NOTFOUND)) {
                // On ne fait rien : le document n'existe pas ou je n'ai pas les droits
            } else {
                throw e;
            }
	    } 
    }


	/**
     * Format content menubar.
     *
     * @param cmsCtx
     * @throws Exception
     */
    public void formatContentMenuBar(CMSServiceCtx cmsCtx) throws Exception {
        this.formatDefaultContentMenuBar(cmsCtx);
    }


    /**
     * Controls if live mode is associated with the current document.
     *
     * @param cmsCtx CMS context
     * @param pubInfos CMS publication informations
     * @return true if in live mode
     */
    protected boolean isInLiveMode(CMSServiceCtx cmsCtx, CMSPublicationInfos pubInfos) {
        boolean liveMode = false;

        EditionState curState = (EditionState) cmsCtx.getRequest().getAttribute("osivia.editionState");
        if ((curState != null) && curState.getContributionMode().equals(EditionState.CONTRIBUTION_MODE_EDITION)) {
            if (curState.getDocPath().equals(pubInfos.getDocumentPath())) {
                liveMode = true;
            }
        }
        return liveMode;
    }


    /**
     * Check if current document is a remote proxy.
     *
     * @param cmsCtx CMS context
     * @param pubInfos CMS publication informations
     * @return true if current document is a remote proxy
     */
    protected boolean isRemoteProxy(CMSServiceCtx cmsCtx, CMSPublicationInfos pubInfos) {
        if (cmsCtx.getDoc() == null) {
            return false;
        }

        if (pubInfos.isPublished() && !this.isInLiveMode(cmsCtx, pubInfos)) {
            // Document
            Document document = (Document) cmsCtx.getDoc();
            // Path
            String path = document.getPath();

            // Pour un proxy distant, le documentPath du pubInfos est égal au docPath
            if (pubInfos.getDocumentPath().equals(path)) {
                return true;
            }
        }

        return false;
    }


    /**
     * Get menubar CMS edition dropdown menu.
     *
     * @param portalControllerContext portal controller context
     * @param bundle internationalization bundle
     * @return menubar dropdown menu
     */
    protected MenubarDropdown getCMSEditionDropdown(PortalControllerContext portalControllerContext, Bundle bundle) {
        MenubarDropdown dropdown = this.menubarService.getDropdown(portalControllerContext, CMS_EDITION_DROPDOWN_MENU_ID);

        if (dropdown == null) {
            dropdown = new MenubarDropdown(CMS_EDITION_DROPDOWN_MENU_ID, bundle.getString("CMS_EDITION"), "halflings halflings-pencil", MenubarGroup.CMS, 3);
            dropdown.setReducible(false);
            this.menubarService.addDropdown(portalControllerContext, dropdown);
        }

        return dropdown;
    }


    /**
     * Get menubar share dropdown menu.
     *
     * @param portalControllerContext portal controller context
     * @param bundle internationalization bundle
     * @return menubar dropdown menu
     */
    protected MenubarDropdown getShareDropdown(PortalControllerContext portalControllerContext, Bundle bundle) {
        MenubarDropdown dropdown = this.menubarService.getDropdown(portalControllerContext, SHARE_DROPDOWN_MENU_ID);

        if (dropdown == null) {
            dropdown = new MenubarDropdown(SHARE_DROPDOWN_MENU_ID, bundle.getString("SHARE"), "glyphicons glyphicons-share-alt", MenubarGroup.GENERIC, 8);
            this.menubarService.addDropdown(portalControllerContext, dropdown);
        }

        return dropdown;
    }


    /**
     * Get optional Nuxeo administration link.
     *
     * @param portalControllerContext portal controller context
     * @param cmsContext CMS service context
     * @param menubar menubar items
     * @param bundle internationalization bundle
     */
    protected void getAdministrationLink(PortalControllerContext portalControllerContext, CMSServiceCtx cmsContext, List<MenubarItem> menubar, Bundle bundle)
            throws CMSException {
        if (cmsContext.getRequest().getRemoteUser() == null) {
            return;
        }

        // Document
        Document document = (Document) cmsContext.getDoc();

        // Publication infos
        CMSPublicationInfos pubInfos = this.cmsService.getPublicationInfos(cmsContext, document.getPath());

        // Do not manage remote proxy
        if (this.isRemoteProxy(cmsContext, pubInfos)) {
            return;
        }

        if (pubInfos.isEditableByUser() && ContextualizationHelper.isCurrentDocContextualized(cmsContext)) {
            // URL
            String url = NuxeoConnectionProperties.getPublicBaseUri().toString() + "/nxdoc/default/" + pubInfos.getLiveId() + "/view_documents";

            // Parent
            MenubarDropdown parent = this.getCMSEditionDropdown(portalControllerContext, bundle);

            // Menubar item
            MenubarItem item = new MenubarItem("MANAGE", bundle.getString("MANAGE_IN_NUXEO"), null, parent, 4, url, "nuxeo", null, null);
            item.setAjaxDisabled(true);

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
     */
    protected void getChangeModeLink(PortalControllerContext portalControllerContext, CMSServiceCtx cmsContext, List<MenubarItem> menubar, Bundle bundle)
            throws CMSException {
        if (cmsContext.getRequest().getRemoteUser() == null) {
            return;
        }

        // Current document
        Document document = (Document) (cmsContext.getDoc());
        String path = document.getPath();

        // Publication infos
        CMSPublicationInfos pubInfos = this.cmsService.getPublicationInfos(cmsContext, path);
        // Extended infos
        CMSExtendedDocumentInfos extendedInfos = (CMSExtendedDocumentInfos) cmsContext.getRequest().getAttribute("osivia.extendedInfos");

        // CMS item type
        Map<String, CMSItemType> managedTypes = this.customizer.getCMSItemTypes();
        CMSItemType containerDocType = managedTypes.get(document.getType());

        if (pubInfos.isEditableByUser() && !pubInfos.isLiveSpace() && ContextualizationHelper.isCurrentDocContextualized(cmsContext)) {
            // Edition state
            EditionState editionState;

            MenubarDropdown parent = this.getCMSEditionDropdown(portalControllerContext, bundle);

            if (this.isInLiveMode(cmsContext, pubInfos)) {
                editionState = new EditionState(EditionState.CONTRIBUTION_MODE_ONLINE, path);

                // Live version indicator menubar item
                MenubarItem liveIndicator = new MenubarItem("LIVE_VERSION", bundle.getString("LIVE_VERSION"), MenubarGroup.CMS, -2, "label label-info");
                liveIndicator.setGlyphicon("halflings halflings-pencil visible-xs-inline-block");
                liveIndicator.setState(true);

                menubar.add(liveIndicator);

                if ((extendedInfos != null) && extendedInfos.isOnlineTaskPending()) {
                    // Online workflow pending indicator menubar item
                    MenubarItem pendingIndicator = new MenubarItem("ON_LINE_WF_PENDING", bundle.getString("ON_LINE_WF_PENDING"), MenubarGroup.CMS, -1,
                            "label label-warning");
                    pendingIndicator.setGlyphicon("glyphicons glyphicons-history");
                    pendingIndicator.setState(true);

                    menubar.add(pendingIndicator);
                }
            } else {
                editionState = new EditionState(EditionState.CONTRIBUTION_MODE_EDITION, path);

                // Forget old state
                this.contributionService.removeWindowEditionState(portalControllerContext);
            }

            // Do not insert any action for remote proxy
            if (!this.isRemoteProxy(cmsContext, pubInfos)) {
                if (this.isInLiveMode(cmsContext, pubInfos)) {
                    if (extendedInfos.isOnlineTaskPending()) {
                        if (extendedInfos.canUserValidateOnlineTask()) {
                            // Online workflow validation items
                            this.addValidatePublishingItems(portalControllerContext, cmsContext, pubInfos, menubar, parent, bundle);
                        } else if (extendedInfos.isUserOnlineTaskInitiator()) {
                            // Cancel publishing ask (workflow) item
                            String cancelAskPublishURL = this.getContributionService().getCancelPublishingAskContributionURL(portalControllerContext,
                                    pubInfos.getDocumentPath());

                            MenubarItem cancelAskPublishItem = new MenubarItem("CANCEL_ASK_PUBLISH", bundle.getString("CANCEL_ASK_PUBLISH"), null,
                                    MenubarGroup.CMS, 12, cancelAskPublishURL, null, null, null);
                            cancelAskPublishItem.setAjaxDisabled(true);

                            menubar.add(cancelAskPublishItem);
                        }
                    } else {
                        if (pubInfos.isBeingModified()) {
                            if (pubInfos.isUserCanValidate()) {
                                // Publish menubar item
                                String publishURL = this.contributionService.getPublishContributionURL(portalControllerContext, pubInfos.getDocumentPath());

                                MenubarItem publishItem = new MenubarItem("PUBLISH", bundle.getString("PUBLISH"), "halflings halflings-ok", parent, 12,
                                        publishURL, null, null, null);
                                publishItem.setAjaxDisabled(true);
                                publishItem.setDivider(true);

                                menubar.add(publishItem);
                            } else {
                                // Ask Publication (workflow) item
                                String askPublishURL = this.getContributionService().getAskPublishContributionURL(portalControllerContext,
                                        pubInfos.getDocumentPath());

                                MenubarItem askPublishItem = new MenubarItem("ASK_PUBLISH", bundle.getString("ASK_PUBLISH"), null, parent, 12, askPublishURL,
                                        null, null, null);
                                askPublishItem.setAjaxDisabled(true);

                                menubar.add(askPublishItem);
                            }
                        }
                    }

                    // Go to proxy menubar item
                    if (pubInfos.isPublished()) {
                        String proxyURL = this.getContributionService().getChangeEditionStateUrl(portalControllerContext, editionState);

                        MenubarItem proxyItem = new MenubarItem("PROXY_RETURN", bundle.getString("PROXY_RETURN"), "halflings halflings-eye-close", parent, 1,
                                proxyURL, null, null, null);
                        proxyItem.setAjaxDisabled(true);

                        menubar.add(proxyItem);
                    }
                } else {
                    if (pubInfos.isUserCanValidate()) {
                        // user can not unpublish root documents like portalsite, blogsite, website, ...
                        MenubarItem unpublishItem = new MenubarItem("UNPUBLISH", bundle.getString("UNPUBLISH"), parent, 12, null);
                        unpublishItem.setAjaxDisabled(true);
                        unpublishItem.setDivider(true);

                        if (!containerDocType.isRootType()) {
                            // Unpublish menubar item
                            String unpublishURL = this.contributionService.getUnpublishContributionURL(portalControllerContext, pubInfos.getDocumentPath());

                            unpublishItem.setUrl(unpublishURL);
                        } else {
                            unpublishItem.setUrl("#");
                            unpublishItem.setDisabled(true);
                            unpublishItem.setTooltip(bundle.getString("CANNOT_UNPUBLISH_ROOT"));
                        }

                        menubar.add(unpublishItem);
                    }

                    if (pubInfos.isBeingModified()) {
                        // Current modification indicator
                        MenubarItem modificationIndicator = new MenubarItem("MODIFICATION_MESSAGE", bundle.getString("MODIFICATION_MESSAGE"), parent, 0, null);
                        modificationIndicator.setGlyphicon("halflings halflings-alert");

                        menubar.add(modificationIndicator);
                    }

                    // Go to preview menubar item
                    String previewURL = this.contributionService.getChangeEditionStateUrl(portalControllerContext, editionState);

                    MenubarItem previewItem = new MenubarItem("LIVE_PREVIEW", bundle.getString("LIVE_PREVIEW"), "halflings halflings-eye-open", parent, 1,
                            previewURL, null, null, null);
                    previewItem.setAjaxDisabled(true);

                    menubar.add(previewItem);
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
        String validateURL = this.getContributionService().getValidatePublishContributionURL(portalControllerContext, pubInfos.getDocumentPath());
        MenubarItem validateItem = new MenubarItem("ONLINE_WF_VALIDATE", bundle.getString("VALIDATE_PUBLISH"), "halflings halflings-ok", parent, 13,
                validateURL, null, null, null);
        validateItem.setAjaxDisabled(true);
        menubar.add(validateItem);

        // Reject
        String rejectURL = this.getContributionService().getRejectPublishContributionURL(portalControllerContext, pubInfos.getDocumentPath());
        MenubarItem rejectItem = new MenubarItem("ONLINE_WF_REJECT", bundle.getString("REJECT_PUBLISH"), "halflings halflings-remove", parent, 14, rejectURL,
                null, null, null);
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
    protected void getLiveContentBrowserLink(PortalControllerContext portalControllerContext, CMSServiceCtx cmsContext, List<MenubarItem> menubar, Bundle bundle)
            throws CMSException {
        if (cmsContext.getRequest().getRemoteUser() == null) {
            return;
        }

        if (!ContextualizationHelper.isCurrentDocContextualized(cmsContext)) {
            return;
        }

        // Current document
        Document document = (Document) cmsContext.getDoc();
        String path;
        boolean folderish;
        if (document == null) {
            path = cmsContext.getCreationPath();

            // Items with creation path are presumed folderish
            folderish = (path != null);
        } else {
            path = document.getPath();

            CMSItemType cmsItemType = this.customizer.getCMSItemTypes().get(document.getType());
            folderish = (cmsItemType != null) && cmsItemType.isFolderish();
        }

        CMSPublicationInfos pubInfos = this.cmsService.getPublicationInfos(cmsContext, path);

        // Do not browse into remote proxy
        if (this.isRemoteProxy(cmsContext, pubInfos)) {
            return;
        }

        if (!pubInfos.isLiveSpace() && !pubInfos.getSubTypes().isEmpty() && folderish) {
            // Live content browser popup link
            String browserURL;
            try {
                Map<String, String> properties = new HashMap<String, String>(1);
                properties.put("osivia.browser.path", path);
                browserURL = this.urlFactory.getStartPortletUrl(portalControllerContext, "osivia-portal-browser-portlet-instance", properties, true);
            } catch (PortalException e) {
                browserURL = "#";
            }

            MenubarDropdown parent = this.getCMSEditionDropdown(portalControllerContext, bundle);

            MenubarItem browserItem = new MenubarItem("BROWSE_LIVE_CONTENT", bundle.getString("BROWSE_LIVE_CONTENT"), "halflings halflings-search", parent, 3,
                    browserURL, null, null, "fancyframe_refresh");
            browserItem.setAjaxDisabled(true);

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
    protected void getDriveEditUrl(PortalControllerContext portalControllerContext, CMSServiceCtx cmsContext, List<MenubarItem> menubar, Bundle bundle, CMSExtendedDocumentInfos extendedInfos)
            throws CMSException {
        if (cmsContext.getRequest().getRemoteUser() == null) {
            return;
        }

        if (!ContextualizationHelper.isCurrentDocContextualized(cmsContext)) {
            return;
        }

        if (extendedInfos.getDriveEditURL() != null) {
            MenubarDropdown parent = this.getCMSEditionDropdown(portalControllerContext, bundle);

            MenubarItem driveEditItem = new MenubarItem("DRIVE_EDIT", bundle.getString("DRIVE_EDIT"), "halflings halflings-play", parent, 5,
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
    protected void getSynchronizeLink(PortalControllerContext portalControllerContext, CMSServiceCtx cmsContext, List<MenubarItem> menubar, 
    		Bundle bundle, CMSExtendedDocumentInfos extendedInfos)
            throws CMSException {
        if (cmsContext.getRequest().getRemoteUser() == null) {
            return;
        }

        if (!ContextualizationHelper.isCurrentDocContextualized(cmsContext)) {
            return;
        }

        // Current document
        Document document = (Document) cmsContext.getDoc();
        String path = document.getPath();

        Boolean active = null;
        String command = null;
        String icon = null;
        EcmCommonCommands ecmAction = null;

        if (extendedInfos.isCanSynchronize()) {
            active = true;
            command = "SYNCHRONIZE";
            icon = "halflings halflings-refresh";
            ecmAction = EcmCommonCommands.synchronizeFolder;
        } else if (extendedInfos.isCanUnsynchronize()) {
            active = false;
            command = "UNSYNCHRONIZE";
            icon = "halflings halflings-ban-circle";
            ecmAction = EcmCommonCommands.unsynchronizeFolder;
        }

        MenubarDropdown parent = this.getCMSEditionDropdown(portalControllerContext, bundle);

        if (active != null) {
			try {
				String synchronizeURL = this.urlFactory.getEcmCommandUrl(portalControllerContext, path, ecmAction);
				
	            MenubarItem synchronizeItem = new MenubarItem(command, bundle.getString(command), icon, parent, 5, synchronizeURL, null, null, null);
	            synchronizeItem.setAjaxDisabled(true);
	            synchronizeItem.setActive(active);

	            menubar.add(synchronizeItem);
	            
				
			} catch (PortalException e) {
				LOGGER.warn(e.getMessage());
			}


        } else if (extendedInfos.getSynchronizationRootPath() != null) {
            String rootURL = this.urlFactory.getCMSUrl(portalControllerContext, null, extendedInfos.getSynchronizationRootPath(), null, null, null, null, null,
                    null, null);

            MenubarItem rootURLItem = new MenubarItem("SYNCHRO_ROOT_URL", bundle.getString("SYNCHRO_ROOT_URL"), null, parent, 5, rootURL, null, null, null);
            rootURLItem.setAjaxDisabled(true);

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
    protected void getSubscribeLink(PortalControllerContext portalControllerContext, CMSServiceCtx cmsContext, List<MenubarItem> menubar, 
    		Bundle bundle, CMSExtendedDocumentInfos extendedInfos)
            throws CMSException {
        // Current document
        Document document = (Document) cmsContext.getDoc();
        String path = document.getPath();


        SubscriptionStatus subscriptionStatus = extendedInfos.getSubscriptionStatus();

        if ((subscriptionStatus != null) && (subscriptionStatus != SubscriptionStatus.no_subscriptions)) {
            String url = "";

            try {
            	MenubarDropdown parent = this.getCMSEditionDropdown(portalControllerContext, bundle);
	            MenubarItem subscribeItem = new MenubarItem("SUBSCRIBE_URL", null, null, parent, 10, url, null, null, null);
	            subscribeItem.setAjaxDisabled(true);
	
	            if (subscriptionStatus == SubscriptionStatus.can_subscribe) {
	                url = this.urlFactory.getEcmCommandUrl(portalControllerContext, path, EcmCommonCommands.subscribe);
	
	                subscribeItem.setUrl(url);
	                subscribeItem.setGlyphicon("halflings halflings-flag");
	                subscribeItem.setTitle(bundle.getString("SUBSCRIBE_ACTION"));
	            } else if (subscriptionStatus == SubscriptionStatus.can_unsubscribe) {
	                url = this.urlFactory.getEcmCommandUrl(portalControllerContext, path, EcmCommonCommands.unsubscribe);
	
	                subscribeItem.setUrl(url);
	                subscribeItem.setGlyphicon("halflings halflings-flag");
	                subscribeItem.setTitle(bundle.getString("UNSUBSCRIBE_ACTION"));
	                subscribeItem.setActive(true);
	            } else if (subscriptionStatus == SubscriptionStatus.has_inherited_subscriptions) {
	                subscribeItem.setUrl("#");
	                subscribeItem.setGlyphicon("halflings halflings-flag");
	                subscribeItem.setTitle(bundle.getString("INHERITED_SUBSCRIPTION"));
	                //subscribeItem.setActive(true);
	                subscribeItem.setDisabled(true);
	            }
	
	            menubar.add(subscribeItem);
	            
            }
            catch(PortalException ex) {
            	LOGGER.warn(ex.getMessage());
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
	private void getLockLink(PortalControllerContext portalControllerContext,
			CMSServiceCtx cmsContext, List<MenubarItem> menubar, Bundle bundle,
			CMSExtendedDocumentInfos extendedInfos) {
		
        // Current document
        Document document = (Document) cmsContext.getDoc();
        String path = document.getPath();


        LockStatus lockStatus = extendedInfos.getLockStatus();

        if ((lockStatus != null) && (lockStatus != LockStatus.no_lock)) {
            String url = "";

            try {
            	MenubarDropdown parent = this.getCMSEditionDropdown(portalControllerContext, bundle);
	            MenubarItem lockItem = new MenubarItem("LOCK_URL", null, null, parent, 12, url, null, null, null);
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
	                lockItem.setActive(true);
	            } else if (lockStatus == LockStatus.locked) {
	                lockItem.setUrl("#");
	                lockItem.setGlyphicon("glyphicons glyphicons-lock");
	                lockItem.setTitle(bundle.getString("LOCKED"));
	                //lockItem.setActive(true);
	                lockItem.setDisabled(true);
	            }
	
	            menubar.add(lockItem);
	            
            }
            catch(PortalException ex) {
            	LOGGER.warn(ex.getMessage());
            }
        }
		
	}


	/**
	 * Get Administrations links 
	 * @param portalControllerContext
	 * @param cmsContext
	 * @param menubar
	 * @param bundle
	 * @throws CMSException 
	 */
	private void getManageLink(PortalControllerContext portalControllerContext,
			CMSServiceCtx cmsContext, List<MenubarItem> menubar, Bundle bundle) throws CMSException {
		
        // Current document
        Document document = (Document) cmsContext.getDoc();
        String path = document.getPath();
        // Publication infos
        CMSPublicationInfos pubInfos = this.cmsService.getPublicationInfos(cmsContext, path);
        
        
        if(document.getType().equals("Workspace") && pubInfos.isManageableByUser()) {
        	
            try {
            	
        		Map<String, String> windowProperties = new HashMap<String, String>();
        		windowProperties.put("osivia.ajaxLink", "1");
        		windowProperties.put("theme.dyna.partial_refresh_enabled", "true");
        		windowProperties.put("action", "consulterRole");
        		windowProperties.put("workspacePath", document.getPath());
        		
        		String url = urlFactory.getStartPortletUrl(portalControllerContext, "toutatice-workspace-gestionworkspace-portailPortletInstance", windowProperties, false);
            	
            	MenubarDropdown parent = this.getCMSEditionDropdown(portalControllerContext, bundle);

				MenubarItem manageMembersItem = new MenubarItem("MANAGE_MEMBERS", null, null, parent, 15, url, null, null, null);
	            manageMembersItem.setAjaxDisabled(true);

                manageMembersItem.setGlyphicon("glyphicons glyphicons-group");
                manageMembersItem.setTitle(bundle.getString("MANAGE_MEMBERS_ACTION"));
           
	            menubar.add(manageMembersItem);
	            
            }
            catch(PortalException ex) {
            	LOGGER.warn(ex.getMessage());
            }
        }

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
    private void getValidationWfLink(PortalControllerContext portalControllerContext, CMSServiceCtx cmsContext, List<MenubarItem> menubar, Bundle bundle,
            CMSExtendedDocumentInfos extendedInfos) throws CMSException {
        
        Document document = (Document) cmsContext.getDoc();
        String path = document.getPath();
        
        CMSPublicationInfos pubInfos = this.cmsService.getPublicationInfos(cmsContext, path);
        
        if(pubInfos.isEditableByUser() && pubInfos.isLiveSpace() && ContextualizationHelper.isCurrentDocContextualized(cmsContext)){
        
            Boolean isValidationWfRunning = extendedInfos.getIsValidationWorkflowRunning();
            String url = StringUtils.EMPTY;
            
            MenubarDropdown parent = this.getCMSEditionDropdown(portalControllerContext, bundle);
            MenubarItem validationWfItem = new MenubarItem("VALIDATION_WF_URL", null, null, parent, 13, url, null, null, "fancyframe_refresh");
            
            if(BooleanUtils.isTrue(isValidationWfRunning)){
                // Access to current validation workflow task
                Map<String, String> requestParameters = new HashMap<String, String>();
                String followWfURL = this.cmsService.getEcmUrl(cmsContext, EcmViews.followWfValidation, pubInfos.getDocumentPath(), requestParameters);
                
                validationWfItem.setUrl(followWfURL);
                validationWfItem.setTitle(bundle.getString("FOLLOW_VALIDATION_WF"));
                
            } else {
                // We can start a validation workflow
                Map<String, String> requestParameters = new HashMap<String, String>();
                String startWfURL = this.cmsService.getEcmUrl(cmsContext, EcmViews.startValidationWf, pubInfos.getDocumentPath(), requestParameters);
                
                validationWfItem.setUrl(startWfURL);
                validationWfItem.setTitle(bundle.getString("START_VALIDATION_WF"));
            }
            
            menubar.add(validationWfItem);
        
        }
        
    }
    
    /**
     * Get edit CMS content link.
     *
     * @param portalControllerContext portal controller context
     * @param cmsContext CMS service context
     * @param bundle internationalization bundle
     */
    protected void getEditLink(PortalControllerContext portalControllerContext, CMSServiceCtx cmsContext, List<MenubarItem> menubar, Bundle bundle)
            throws CMSException {
        if (cmsContext.getRequest().getRemoteUser() == null) {
            return;
        }

        // Current document
        Document document = (Document) cmsContext.getDoc();
        // Publication infos
        CMSPublicationInfos pubInfos = this.cmsService.getPublicationInfos(cmsContext, document.getPath());

        if (this.isRemoteProxy(cmsContext, pubInfos)) {
            return;
        }

        if (pubInfos.isEditableByUser()) {
            if (ContextualizationHelper.isCurrentDocContextualized(cmsContext)) {
                CMSItemType cmsItemType = this.customizer.getCMSItemTypes().get(document.getType());
                if ((cmsItemType != null) && cmsItemType.isSupportsPortalForms()) {
                    // Callback URL
                    String callbackURL = this.urlFactory.getCMSUrl(portalControllerContext, null, "_NEWID_", null, null, "_LIVE_", null, null, null, null);
                    // ECM base URL
                    String ecmBaseURL = this.cmsService.getEcmDomain(cmsContext);

                    Map<String, String> requestParameters = new HashMap<String, String>();
                    String url = this.cmsService.getEcmUrl(cmsContext, EcmViews.editDocument, pubInfos.getDocumentPath(), requestParameters);

                    // On click action
                    StringBuilder onClick = new StringBuilder();
                    onClick.append("javascript:setCallbackFromEcmParams('");
                    onClick.append(callbackURL);
                    onClick.append("', '");
                    onClick.append(ecmBaseURL);
                    onClick.append("');");

                    String editLabel = null;
                    if (!pubInfos.isLiveSpace() && !this.isInLiveMode(cmsContext, pubInfos) && pubInfos.isBeingModified()) {
                        editLabel = bundle.getString("EDIT_LIVE_VERSION");
                    } else {
                        editLabel = bundle.getString("EDIT");
                    }

                    MenubarDropdown parent = this.getCMSEditionDropdown(portalControllerContext, bundle);

                    // Menubar item
                    MenubarItem item = new MenubarItem("EDIT", editLabel, "halflings halflings-pencil", parent, 1, url, null, onClick.toString(),
                            "fancyframe_refresh");
                    item.setAjaxDisabled(true);

                    menubar.add(item);
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
    protected void getMoveLink(PortalControllerContext portalControllerContext, CMSServiceCtx cmsContext, List<MenubarItem> menubar, Bundle bundle)
            throws CMSException {
        if (cmsContext.getRequest().getRemoteUser() == null) {
            return;
        }

        // Current document
        Document document = (Document) cmsContext.getDoc();
        if (document != null) {
            // Publication infos
            CMSPublicationInfos pubInfos = this.cmsService.getPublicationInfos(cmsContext, document.getPath());

            if (this.isRemoteProxy(cmsContext, pubInfos)) {
                return;
            }

            if (pubInfos.isLiveSpace() && pubInfos.isEditableByUser() && ContextualizationHelper.isCurrentDocContextualized(cmsContext)) {
                // Nuxeo controller
                NuxeoController nuxeoController = new NuxeoController(portalControllerContext.getRequest(), portalControllerContext.getResponse(),
                        portalControllerContext.getPortletCtx());

                // CMS item type
                CMSItemType cmsItemType = this.customizer.getCMSItemTypes().get(document.getType());

                if ((cmsItemType != null) && cmsItemType.isSupportsPortalForms()) {
                    // Move document popup URL
                    String moveDocumentURL;
                    try {
                        Map<String, String> properties = new HashMap<String, String>();
                        properties.put(MoveDocumentPortlet.DOCUMENT_PATH_WINDOW_PROPERTY, document.getPath());
                        properties.put(MoveDocumentPortlet.CMS_BASE_PATH_WINDOW_PROPERTY, nuxeoController.getBasePath());
                        properties.put(MoveDocumentPortlet.ACCEPTED_TYPE_WINDOW_PROPERTY, cmsItemType.getName());

                        moveDocumentURL = this.urlFactory.getStartPortletUrl(portalControllerContext, "toutatice-portail-cms-nuxeo-move-portlet-instance",
                                properties, true);
                    } catch (PortalException e) {
                        moveDocumentURL = null;
                    }

                    if (moveDocumentURL != null) {
                        MenubarDropdown parent = this.getCMSEditionDropdown(portalControllerContext, bundle);

                        MenubarItem item = new MenubarItem("MOVE", bundle.getString("MOVE"), "halflings halflings-move", parent, 2, moveDocumentURL, null,
                                null, "fancyframe_refresh");
                        item.setAjaxDisabled(true);

                        menubar.add(item);
                    }
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
    protected void getCreateLink(PortalControllerContext portalControllerContext, CMSServiceCtx cmsContext, List<MenubarItem> menubar, Bundle bundle)
            throws CMSException {
        if (cmsContext.getRequest().getRemoteUser() == null) {
            return;
        }

        // Creation type
        String creationType = cmsContext.getCreationType();
        // Creation path
        String creationPath = cmsContext.getCreationPath();
        // Parent document
        Document parentDoc = (Document) cmsContext.getDoc();
        if (creationPath != null) {
            parentDoc = (Document) this.cmsService.getContent(cmsContext, creationPath).getNativeItem();
        }

        // Contextualization
        CMSPublicationInfos pubInfos = this.cmsService.getPublicationInfos(cmsContext, parentDoc.getPath());

        // Do not add into remote proxy
        if (this.isRemoteProxy(cmsContext, pubInfos)) {
            return;
        }

        if ((creationPath != null) || ContextualizationHelper.isCurrentDocContextualized(cmsContext)) {
            // Test ergo JSS
            String callbackURL = this.urlFactory.getRefreshPageUrl(portalControllerContext, true);

            // ECM base URL
            String ecmBaseURL = this.cmsService.getEcmDomain(cmsContext);
            // On click action
            StringBuilder builder = new StringBuilder();
            builder.append("javascript:setCallbackFromEcmParams('");
            builder.append(callbackURL);
            builder.append("', '");
            builder.append(ecmBaseURL);
            builder.append("');");
            String onclick = builder.toString();

            // Sub-types
            Map<String, String> subTypes = pubInfos.getSubTypes();
            Comparator<CMSItemType> comparator = new CMSItemTypeComparator(bundle);
            SortedMap<CMSItemType, String> folderishTypes = new TreeMap<CMSItemType, String>(comparator);
            SortedMap<CMSItemType, String> notFolderishTypes = new TreeMap<CMSItemType, String>(comparator);

            Map<String, CMSItemType> managedTypes = this.customizer.getCMSItemTypes();
            CMSItemType containerDocType = managedTypes.get(parentDoc.getType());
            if (containerDocType != null) {
                for (String docType : subTypes.keySet()) {
                    // Is this type managed at portal level ?
                    if (containerDocType.getPortalFormSubTypes().contains(docType) && ((creationType == null) || creationType.equals(docType))) {
                        CMSItemType docTypeDef = managedTypes.get(docType);
                        if ((docTypeDef != null) && docTypeDef.isSupportsPortalForms()) {
                            // CMS item type
                            CMSItemType cmsItemType = managedTypes.get(docType);
                            if (cmsItemType != null) {
                                // URL
                                Map<String, String> requestParameters = new HashMap<String, String>();
                                requestParameters.put("type", docType);
                                String url = this.cmsService.getEcmUrl(cmsContext, EcmViews.createDocument, pubInfos.getDocumentPath(), requestParameters);

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

            int size = folderishTypes.size() + notFolderishTypes.size();
            if (size == 1) {
                // Direct link

                Entry<CMSItemType, String> entry;
                if (folderishTypes.size() == 1) {
                    entry = folderishTypes.entrySet().iterator().next();
                } else {
                    entry = notFolderishTypes.entrySet().iterator().next();
                }

                String url = entry.getValue();

                // Menubar item
                MenubarItem item = new MenubarItem("ADD", bundle.getString("ADD"), "halflings halflings-plus", MenubarGroup.CMS, 2, url, null, onclick,
                        "fancyframe_refresh");
                item.setAjaxDisabled(true);

                menubar.add(item);
            } else if (size > 0) {
                // Dropdown menu
                MenubarDropdown dropdown = new MenubarDropdown("ADD", bundle.getString("ADD"), "halflings halflings-plus", MenubarGroup.CMS, 2);
                this.menubarService.addDropdown(portalControllerContext, dropdown);

                int order = 1;
                boolean divider = false;

                for (Entry<CMSItemType, String> entry : folderishTypes.entrySet()) {
                    CMSItemType cmsItemType = entry.getKey();
                    String url = entry.getValue();

                    // Type name
                    String typeName = StringUtils.upperCase(cmsItemType.getName());
                    String name = bundle.getString(typeName);

                    // Menubar item
                    MenubarItem item = new MenubarItem("ADD_" + typeName, name, cmsItemType.getGlyph(), dropdown, order, url, null, onclick,
                            "fancyframe_refresh");
                    item.setAjaxDisabled(true);

                    menubar.add(item);

                    order++;
                    divider = true;
                }

                for (Entry<CMSItemType, String> entry : notFolderishTypes.entrySet()) {
                    CMSItemType cmsItemType = entry.getKey();
                    String url = entry.getValue();

                    // Type name
                    String typeName = StringUtils.upperCase(cmsItemType.getName());
                    String name = bundle.getString(typeName);

                    // Menubar item
                    MenubarItem item = new MenubarItem("ADD_" + typeName, name, cmsItemType.getGlyph(), dropdown, order, url, null, onclick,
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
    protected void getDeleteLink(PortalControllerContext portalControllerContext, CMSServiceCtx cmsContext, List<MenubarItem> menubar, Bundle bundle)
            throws CMSException {
        if (cmsContext.getRequest().getRemoteUser() == null) {
            return;
        }

        // Document
        Document document = (Document) cmsContext.getDoc();

        // Contextualization
        CMSPublicationInfos pubInfos = this.cmsService.getPublicationInfos(cmsContext, document.getPath());

        // Do not delete remote proxy
        if (this.isRemoteProxy(cmsContext, pubInfos)) {
            return;
        }

        // Do not delete published elements
        if (pubInfos.isDeletableByUser() && (pubInfos.isLiveSpace() || this.isInLiveMode(cmsContext, pubInfos))) {
            if (ContextualizationHelper.isCurrentDocContextualized(cmsContext)) {
                CMSItemType docTypeDef = this.customizer.getCMSItemTypes().get(document.getType());
                if ((docTypeDef != null) && docTypeDef.isSupportsPortalForms()) {
                    MenubarDropdown parent = this.getCMSEditionDropdown(portalControllerContext, bundle);

                    // Menubar item
                    MenubarItem item = new MenubarItem("DELETE", bundle.getString("DELETE"), "halflings halflings-trash", parent, 20, null, null, null, null);
                    item.setAjaxDisabled(true);
                    item.setDivider(true);

                    if (docTypeDef.isRootType()) {
                        item.setUrl("#");
                        item.setDisabled(true);
                        item.setTooltip(bundle.getString("CANNOT_DELETE_ROOT"));
                    } else {
                        // Fancybox properties
                        Map<String, String> properties = new HashMap<String, String>();
                        properties.put("docId", document.getId());
                        properties.put("docPath", document.getPath());

                        // Fancybox identifier
                        String fancyboxId = cmsContext.getResponse().getNamespace() + "_PORTAL_DELETE";

                        // Fancybox delete action URL
                        String putInTrashUrl = this.urlFactory.getPutDocumentInTrashUrl(portalControllerContext, pubInfos.getLiveId(),
                                pubInfos.getDocumentPath());

                        // Fancybox HTML data
                        String fancybox = this.generateDeleteConfirmationFancybox(properties, bundle, fancyboxId, putInTrashUrl);
                        item.setAssociatedHTML(fancybox);

                        // URL
                        String url = "#" + fancyboxId;
                        item.setUrl(url);

                        item.setHtmlClasses("fancybox_inline");
                    }

                    menubar.add(item);
                }
            }
        }
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
        Element fancyboxContainer = DOM4JUtils.generateDivElement("hidden");

        // Container
        Element container = DOM4JUtils.generateDivElement("container-fluid");
        DOM4JUtils.addAttribute(container, HTMLConstants.ID, fancyboxId);
        fancyboxContainer.add(container);

        // Form
        Element form = DOM4JUtils.generateElement(HTMLConstants.FORM, "text-center", null, null, AccessibilityRoles.FORM);
        DOM4JUtils.addAttribute(form, HTMLConstants.ACTION, actionURL);
        DOM4JUtils.addAttribute(form, HTMLConstants.METHOD, HTMLConstants.FORM_METHOD_POST);
        container.add(form);

        // Message
        Element message = DOM4JUtils.generateElement(HTMLConstants.P, null, bundle.getString("CMS_DELETE_CONFIRM_MESSAGE"));
        form.add(message);

        // Hidden fields
        for (Entry<String, String> property : properties.entrySet()) {
            Element hidden = DOM4JUtils.generateElement(HTMLConstants.INPUT, null, null);
            DOM4JUtils.addAttribute(hidden, HTMLConstants.TYPE, HTMLConstants.INPUT_TYPE_HIDDEN);
            DOM4JUtils.addAttribute(hidden, HTMLConstants.NAME, property.getKey());
            DOM4JUtils.addAttribute(hidden, HTMLConstants.VALUE, property.getValue());
            form.add(hidden);
        }

        // OK button
        Element okButton = DOM4JUtils.generateElement(HTMLConstants.BUTTON, "btn btn-default btn-warning", bundle.getString("YES"),
                "halflings halflings-alert", null);
        DOM4JUtils.addAttribute(okButton, HTMLConstants.TYPE, HTMLConstants.INPUT_TYPE_SUBMIT);
        form.add(okButton);

        // Cancel button
        Element cancelButton = DOM4JUtils.generateElement(HTMLConstants.BUTTON, "btn btn-default", bundle.getString("NO"));
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
            Bundle bundle, String displayName,
            String url) throws CMSException {
        MenubarItem item = new MenubarItem("CONTEXTUALIZE", bundle.getString("CONTEXTUALIZE_SPACE", displayName), "halflings halflings-level-up",
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
    protected void getContextualizationLink(PortalControllerContext portalControllerContext, CMSServiceCtx cmsContext, List<MenubarItem> menubar, Bundle bundle)
            throws CMSException {
        // Request
        PortletRequest request = cmsContext.getRequest();

        if (!WindowState.MAXIMIZED.equals(request.getWindowState())) {
            return;
        }

        Page currentPage = null;
        Window window = (Window) request.getAttribute("osivia.window");
        if (window != null) {
            currentPage = window.getPage();
        }

        // Document
        Document document = (Document) cmsContext.getDoc();

        // On regarde dans quelle page le contenu ext contextualisé
        Page page;
        try {
            page = this.urlFactory.getPortalCMSContextualizedPage(portalControllerContext, document.getPath());
        } catch (PortalException e) {
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
                // Soit le nom de l'espace de publication
                CMSPublicationInfos pubInfos = this.cmsService.getPublicationInfos(cmsContext, document.getPath());

                if (pubInfos.getPublishSpacePath() != null) {
                    CMSItem pubConfig = this.cmsService.getSpaceConfig(cmsContext, pubInfos.getPublishSpacePath());
                    if ("1".equals(pubConfig.getProperties().get("contextualizeInternalContents"))) {
                        spaceDisplayName = pubInfos.getPublishSpaceDisplayName();
                    }

                }
            }

            if (spaceDisplayName != null) {
                String url = this.urlFactory.getCMSUrl(portalControllerContext, currentPage.getId().toString(PortalObjectPath.CANONICAL_FORMAT),
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
        String id = cmsContext.getResponse().getNamespace() + "_PERMALINK_DISPLAY";

        // Fancybox HTML content
        String htmlContent = this.generatePermalinkFancybox(bundle, id, url);


        // Parent dropdown menu
        MenubarDropdown parent = this.getShareDropdown(portalControllerContext, bundle);

        // Menubar item
        MenubarItem item = new MenubarItem("PERMALINK", bundle.getString("PERMALINK"), "halflings halflings-link", parent, 1, "#" + id, null, null,
                "fancybox_inline");
        item.setAjaxDisabled(true);
        item.setAssociatedHTML(htmlContent);

        menubar.add(item);
    }


    /**
     * Compute permalink URL.
     *
     * @param portalControllerContext portal controller context
     * @param cmsContext CMS service context
     * @param bundle internationalization bundle
     * @return permalink URL
     */
    protected String computePermaLinkUrl(PortalControllerContext portalControllerContext, CMSServiceCtx cmsContext, List<MenubarItem> menubar, Bundle bundle)
            throws CMSException {
        // Request
        PortletRequest request = cmsContext.getRequest();

        
        // Selectors parameters
        Map<String, String> parameters = null;
        String selectors = request.getParameter("selectors");
        if (selectors != null) {
            parameters = new HashMap<String, String>();
            Map<String, List<String>> decodedSelectors = PageSelectors.decodeProperties(selectors);
            parameters.put("selectors", PageSelectors.encodeProperties(decodedSelectors));
        }
        

        
        // Document
        Document document = (Document) cmsContext.getDoc();
        String path = document.getPath();        
        CMSPublicationInfos pubInfos = this.cmsService.getPublicationInfos(cmsContext, document.getPath());
        
        // On ne sait pas gérer les webid des proxys distants (comment résoudre l'ambiguité si plusieurs proxys)
        // Du coup, on ne les gère que pour les portails web
        // A revoir dans la version 4.2
        
        if( !this.isRemoteProxy(cmsContext, pubInfos ))
              path =  this.customizer.getContentWebIdPath(cmsContext);

        

        // URL
        String url;
        try {
            url = this.getUrlFactory().getPermaLink(portalControllerContext, null, parameters, path, IPortalUrlFactory.PERM_LINK_TYPE_CMS);
        } catch (PortalException e) {
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
            // Maximized document
            displayPermalink = true;
        } else {
            if (ContextualizationHelper.isCurrentDocContextualized(cmsContext)) {
                displayPermalink = true;
            }

            // Current portal
            Portal portal = PortalObjectUtils.getPortal(cmsContext.getControllerContext());
            // Space site indicator
            boolean spaceSite = PortalObjectUtils.isSpaceSite(portal);
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
    protected void getPermaLinkLink(PortalControllerContext portalControllerContext, CMSServiceCtx cmsContext, List<MenubarItem> menubar, Bundle bundle)
            throws CMSException {
        if (!this.mustDisplayPermalink(portalControllerContext, cmsContext, menubar, bundle)) {
            return;
        }

        String url = this.computePermaLinkUrl(portalControllerContext, cmsContext, menubar, bundle);
        if (url != null) {
            this.addPermaLinkItem(portalControllerContext, cmsContext, menubar, bundle, url);
        }
    }


    /**
     * Generate permalink fancybox HTML content.
     *
     * @param bundle internationalization bundle
     * @param id fancybox identifier
     * @param url permalink URL
     * @return HTML content
     */
    private String generatePermalinkFancybox(Bundle bundle, String id, String url) {
        // Fancybox container
        Element fancyboxContainer = DOM4JUtils.generateDivElement("hidden");

        // Container
        Element container = DOM4JUtils.generateDivElement("container-fluid");
        DOM4JUtils.addAttribute(container, HTMLConstants.ID, id);
        fancyboxContainer.add(container);

        // Form
        Element form = DOM4JUtils.generateDivElement("form-horizontal");
        container.add(form);

        // Form group #1
        Element formGroup1 = DOM4JUtils.generateDivElement("form-group");
        form.add(formGroup1);

        // Label
        Element label = DOM4JUtils.generateElement(HTMLConstants.LABEL, "control-label col-sm-2", bundle.getString("PERMALINK"));
        formGroup1.add(label);

        // Link col
        Element linkCol = DOM4JUtils.generateDivElement("col-sm-10");
        formGroup1.add(linkCol);

        // Form control static
        Element formControlStatic = DOM4JUtils.generateElement(HTMLConstants.P, "form-control-static", null);
        linkCol.add(formControlStatic);

        // Link
        Element link = DOM4JUtils.generateLinkElement(url, null, null, "no-ajax-link", url);
        formControlStatic.add(link);

        // Form group #2
        Element formGroup2 = DOM4JUtils.generateDivElement("form-group");
        form.add(formGroup2);

        // Button col
        Element buttonCol = DOM4JUtils.generateDivElement("col-sm-offset-2 col-sm-10");
        formGroup2.add(buttonCol);

        // Close button
        Element button = DOM4JUtils.generateElement(HTMLConstants.BUTTON, "btn btn-default", bundle.getString("CLOSE"));
        DOM4JUtils.addAttribute(button, HTMLConstants.TYPE, HTMLConstants.INPUT_TYPE_BUTTON);
        DOM4JUtils.addAttribute(button, HTMLConstants.ONCLICK, "closeFancybox()");
        buttonCol.add(button);

        return DOM4JUtils.write(fancyboxContainer);
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
