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
package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.WindowState;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.Portal;
import org.jboss.portal.core.model.portal.PortalObjectPath;
import org.jboss.portal.core.model.portal.Window;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.contribution.IContributionService;
import org.osivia.portal.api.contribution.IContributionService.EditionState;
import org.osivia.portal.api.html.AccessibilityRoles;
import org.osivia.portal.api.html.DOM4JUtils;
import org.osivia.portal.api.html.HTMLConstants;
import org.osivia.portal.api.internationalization.Bundle;
import org.osivia.portal.api.internationalization.IBundleFactory;
import org.osivia.portal.api.internationalization.IInternationalizationService;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.menubar.MenubarItem;
import org.osivia.portal.api.urls.EcmCommand;
import org.osivia.portal.api.urls.EcmFilesCommand;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSItemType;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.cms.CMSPublicationInfos.SubscriptionStatus;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.portalobjects.PortalObjectUtils;

import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoConnectionProperties;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.DefaultCMSCustomizer;
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
     * @param cmsCtx CMS context
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public void formatDefaultContentMenuBar(CMSServiceCtx cmsCtx) throws Exception {
        if ((cmsCtx.getDoc() == null) && (cmsCtx.getCreationPath() == null)) {
            return;
        }

        PortletRequest request = cmsCtx.getRequest();

        // Menubar
        List<MenubarItem> menubar = (List<MenubarItem>) request.getAttribute(Constants.PORTLET_ATTR_MENU_BAR);

        // Current portal
        Portal portal = PortalObjectUtils.getPortal(cmsCtx.getControllerContext());

        // Check if web page mode layout contains CMS regions and content supports fragments
        // Edition mode is supported by the webpage menu
        boolean webPageFragment = false;
        if (PortalObjectUtils.isSpaceSite(portal) && (cmsCtx.getDoc() != null)) {
            String webPagePath = (String) request.getAttribute("osivia.cms.webPagePath");

            String docLivePath = ContextualizationHelper.getLivePath(((Document) (cmsCtx.getDoc())).getPath());
            if (StringUtils.equals(docLivePath, webPagePath)) {
                    webPageFragment = true;
            }
        }

        try {
            if ((cmsCtx.getDoc() != null) && !webPageFragment) {
                this.getPermaLinkLink(cmsCtx, menubar);
            }
            if ((cmsCtx.getDoc() != null)  && !webPageFragment) {
                this.getContextualizationLink(cmsCtx, menubar);
            }
            if ((cmsCtx.getDoc() != null)  && !webPageFragment) {
                this.getChangeModeLink(cmsCtx, menubar);
            }

            this.getLiveContentBrowserLink(cmsCtx, menubar);

            if ((cmsCtx.getDoc() != null)  && !webPageFragment) {
                this.getEditLink(cmsCtx, menubar);
            }

            this.getCreateLink(cmsCtx, menubar);

            if ((cmsCtx.getDoc() != null)  && !webPageFragment) {
                this.getDeleteLink(cmsCtx, menubar);
            }
            if ((cmsCtx.getDoc() != null)  && !webPageFragment) {
                this.getAdministrationLink(cmsCtx, menubar);
            }

//            if ((cmsCtx.getDoc() != null)  && !webPageFragment) {
//                this.getBackLink( cmsCtx,  menubar);
//            }

            if ((cmsCtx.getDoc() != null) && !webPageFragment) {
				this.getDriveEditUrl(cmsCtx, menubar);
                this.getSynchronizeLink(cmsCtx, menubar);

                // follow
				this.getSubscribeLink(cmsCtx, menubar);
            }

        } catch (CMSException e) {
            if ((e.getErrorCode() == CMSException.ERROR_FORBIDDEN) || (e.getErrorCode() == CMSException.ERROR_NOTFOUND)) {
                // On ne fait rien : le document n'existe pas ou je n'ai pas les droits
            } else  {
                throw e;
            }
        }
    }


    @SuppressWarnings("unchecked")
    protected void adaptDropdowMenu(CMSServiceCtx cmsCtx) throws Exception {
        PortletRequest request = cmsCtx.getRequest();
        List<MenubarItem> menubar = (List<MenubarItem>) request.getAttribute(Constants.PORTLET_ATTR_MENU_BAR);

        // Duplication bouton ajouter
        MenubarItem duplicateItem = null;
        boolean otherItem = false;
        for (Object object : menubar) {
            MenubarItem menuItem = (MenubarItem) object;
            if (menuItem.isDropdownItem()) {
                otherItem = true;
            } else if ("ADD".equals(menuItem.getId())) {
                duplicateItem = menuItem.clone();
                duplicateItem.setDropdownItem(true);
            }
        }

        if ((duplicateItem != null) && (otherItem)) {
            menubar.add(duplicateItem);
        }

    }


    public void formatContentMenuBar(CMSServiceCtx cmsCtx) throws Exception {
        this.formatDefaultContentMenuBar(cmsCtx);
        this.adaptDropdowMenu(cmsCtx);
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
    protected boolean isRemoteProxy(CMSServiceCtx cmsCtx, CMSPublicationInfos pubInfos){

        if( cmsCtx.getDoc() == null) {
            return false;
        }

        if( pubInfos.isPublished() && !this.isInLiveMode(cmsCtx, pubInfos)){
            String docPath = (((Document) (cmsCtx.getDoc())).getPath());

            // Pour un proxy distant, le documentPath du pubInfos est égal au docPath
            if( pubInfos.getDocumentPath().equals(docPath)) {
                return true;
            }
        }

        return false;
    }


    /**
     * Get optional Nuxeo administration link.
     *
     * @param cmsCtx CMS service context
     * @param menuBar current menu to edit
     * @throws Exception
     */
    protected void getAdministrationLink(CMSServiceCtx cmsCtx, List<MenubarItem> menuBar) throws Exception {
        if (cmsCtx.getRequest().getRemoteUser() == null) {
            return;
        }

        CMSPublicationInfos pubInfos = this.cmsService.getPublicationInfos(cmsCtx, (((Document) (cmsCtx.getDoc())).getPath()));

        // Do not manage remote proxy
        if( this.isRemoteProxy(cmsCtx, pubInfos)) {
            return;
        }

        if (pubInfos.isEditableByUser() && ContextualizationHelper.isCurrentDocContextualized(cmsCtx)) {
            // Internationalization bundle
            Bundle bundle = this.bundleFactory.getBundle(cmsCtx.getRequest().getLocale());

            String url = NuxeoConnectionProperties.getPublicBaseUri().toString() + "/nxdoc/default/" + pubInfos.getLiveId() + "/view_documents";

            // Menubar item
            MenubarItem item = new MenubarItem("MANAGE", bundle.getString("MANAGE"), MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 3, url, null, null, "nuxeo");
            item.setGlyphicon("halflings new_window");
            item.setAjaxDisabled(true);
            item.setDropdownItem(true);
            menuBar.add(item);
        }
    }


    /**
     * Get change mode link.
     *
     * @param cmsCtx CMS context
     * @param menubar menubar
     * @throws Exception
     */
    protected void getChangeModeLink(CMSServiceCtx cmsCtx, List<MenubarItem> menubar) throws Exception {
        if (cmsCtx.getRequest().getRemoteUser() == null) {
            return;
        }

        // Current document
        Document document = (Document) (cmsCtx.getDoc());
        String path = document.getPath();
        CMSPublicationInfos pubInfos = this.cmsService.getPublicationInfos(cmsCtx, path);

        if (pubInfos.isEditableByUser() && !pubInfos.isLiveSpace() && ContextualizationHelper.isCurrentDocContextualized(cmsCtx)) {
            // Internationalization bundle
            Bundle bundle = this.bundleFactory.getBundle(cmsCtx.getRequest().getLocale());
            // Portal controller context
            PortalControllerContext portalControllerContext = new PortalControllerContext(cmsCtx.getPortletCtx(), cmsCtx.getRequest(), cmsCtx.getResponse());

            // Edition state
            EditionState editionState;

            if (this.isInLiveMode(cmsCtx, pubInfos)) {
                editionState = new EditionState(EditionState.CONTRIBUTION_MODE_ONLINE, path);

                // Live version indicator menubar item
                MenubarItem liveIndicator = new MenubarItem("LIVE_VERSION", bundle.getString("LIVE_VERSION"), MenubarItem.ORDER_PORTLET_SPECIFIC_CMS, null,
                        null, null, null);
                liveIndicator.setStateItem(true);
                menubar.add(liveIndicator);

                if (pubInfos.isOnLinePending()) {
                    // OnLIne workflow pending indicator menubar item
                    MenubarItem pendingIndicator = new MenubarItem("ON_LINE_WF_PENDING", bundle.getString("ON_LINE_WF_PENDING"),
                            MenubarItem.ORDER_PORTLET_SPECIFIC_CMS, null, null, null, null);
                    pendingIndicator.setGlyphicon("history");
                    pendingIndicator.setStateItem(true);
                    menubar.add(pendingIndicator);
                }

            }

            else {
                editionState = new EditionState(EditionState.CONTRIBUTION_MODE_EDITION, path);

                // Forget old state
                //this.contributionService.removeWindowEditionState(portalControllerContext);
            }

            // Do not insert any action for remote proxy
            if (!this.isRemoteProxy(cmsCtx, pubInfos)) {
                if (this.isInLiveMode(cmsCtx, pubInfos)) {
                    if (pubInfos.isUserCanValidate()) {

                        if (pubInfos.isOnLinePending()) {
                            // OnLine workflow validation items
                            this.addValidatePublishingItems(portalControllerContext, cmsCtx, menubar, pubInfos);
                        } else {
                            if(pubInfos.isBeingModified()){
                                // Publish menubar item
                                String publishURL = this.contributionService.getPublishContributionURL(portalControllerContext, pubInfos.getDocumentPath());
                                MenubarItem publishItem = new MenubarItem("PUBLISH", bundle.getString("PUBLISH"), MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 12, publishURL,
                                        null, null, null);
                                publishItem.setGlyphicon("halflings ok-circle");
                                publishItem.setAjaxDisabled(true);
                                publishItem.setDropdownItem(true);
                                menubar.add(publishItem);
                            }
                        }
                    } else{
                        if(!pubInfos.isOnLinePending() && pubInfos.isBeingModified()){
                            // Ask Publication (workflow) item
                            String askPublishURL = this.getContributionService().getAskPublishContributionURL(portalControllerContext, pubInfos.getDocumentPath());
                            MenubarItem askPublishItem = new MenubarItem("ASK_PUBLISH", bundle.getString("ASK_PUBLISH"), MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 12,
                                    askPublishURL, null, null, null);
                            askPublishItem.setGlyphicon("chat");
                            askPublishItem.setAjaxDisabled(true);
                            askPublishItem.setDropdownItem(true);
                            menubar.add(askPublishItem);
                        }
                        if(pubInfos.isOnLinePending() && pubInfos.isUserOnLineInitiator()){
                            // Cancel publishing ask (workflow) item
                            String cancelAskPublishURL = this.getContributionService().getCancelPublishingAskContributionURL(portalControllerContext, pubInfos.getDocumentPath());
                            MenubarItem cancelAskPublishItem = new MenubarItem("CANCEL_ASK_PUBLISH", bundle.getString("CANCEL_ASK_PUBLISH"), MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 12,
                                    cancelAskPublishURL, null, null, null);
                            cancelAskPublishItem.setGlyphicon("halflings remove-circle");
                            cancelAskPublishItem.setAjaxDisabled(true);
                            cancelAskPublishItem.setDropdownItem(true);
                            menubar.add(cancelAskPublishItem);
                        }
                    }

                    // Go to proxy menubar item
                    if (pubInfos.isPublished()) {
                        String proxyURL = this.getContributionService().getChangeEditionStateUrl(portalControllerContext, editionState);
                        MenubarItem proxyItem = new MenubarItem("PROXY_RETURN", bundle.getString("PROXY_RETURN"), MenubarItem.ORDER_PORTLET_SPECIFIC_CMS, proxyURL,
                            null, null, null);
                        proxyItem.setGlyphicon("halflings eye-close");
                        proxyItem.setAjaxDisabled(true);
                        proxyItem.setDropdownItem(true);
                        menubar.add(proxyItem);
                    }
                } else {
                    if (pubInfos.isUserCanValidate()) {
                        // Unpublish menubar item
                        String unpublishURL = this.contributionService.getUnpublishContributionURL(portalControllerContext, pubInfos.getDocumentPath());
                        MenubarItem unpublishItem = new MenubarItem("UNPUBLISH", bundle.getString("UNPUBLISH"), MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 12,
                                unpublishURL, null, null, null);
                        unpublishItem.setGlyphicon("halflings remove-circle");
                        unpublishItem.setAjaxDisabled(true);
                        unpublishItem.setDropdownItem(true);
                        menubar.add(unpublishItem);
                    }

                    if (pubInfos.isBeingModified()) {
                        // Current modification indicator
                        MenubarItem modificationIndicator = new MenubarItem("MODIFICATION_MESSAGE", bundle.getString("MODIFICATION_MESSAGE"),
                                MenubarItem.ORDER_PORTLET_SPECIFIC_CMS, null, null, null, null);
                        modificationIndicator.setGlyphicon("halflings warning-sign");
                        modificationIndicator.setStateItem(true);
                        modificationIndicator.setDropdownItem(true);
                        menubar.add(modificationIndicator);
                    }

                    // Go to preview menubar item
                    String previewURL = this.contributionService.getChangeEditionStateUrl(portalControllerContext, editionState);
                    MenubarItem previewItem = new MenubarItem("LIVE_PREVIEW", bundle.getString("LIVE_PREVIEW"), MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 1,
                            previewURL, null, null, null);
                    previewItem.setGlyphicon("halflings eye-open");
                    previewItem.setAjaxDisabled(true);
                    previewItem.setDropdownItem(true);
                    menubar.add(previewItem);
                }
            }
        }
    }

    /**
     * Generate validate or recject OnLine workflow items..
     *
     * @param menubar
     * @throws Exception
     */
    protected void addValidatePublishingItems(PortalControllerContext portalControllerContext, CMSServiceCtx cmsCtx, List<MenubarItem> menubar, CMSPublicationInfos pubInfos) throws Exception {
        Bundle bundle = this.bundleFactory.getBundle(cmsCtx.getRequest().getLocale());

        String validateUrl = this.getContributionService().getValidatePublishContributionURL(portalControllerContext, pubInfos.getDocumentPath());
        MenubarItem validateItem = new MenubarItem("ONLINE_WF_VALIDATE", bundle.getString("VALIDATE_PUBLISH"), MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 12, validateUrl,
                null, null, null);
        validateItem.setGlyphicon("halflings ok-circle");
        validateItem.setAjaxDisabled(true);
        validateItem.setDropdownItem(true);
        menubar.add(validateItem);

        String rejectUrl = this.getContributionService().getRejectPublishContributionURL(portalControllerContext, pubInfos.getDocumentPath());
        MenubarItem rejectItem = new MenubarItem("ONLINE_WF_REJECT", bundle.getString("REJECT_PUBLISH"), MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 13, rejectUrl,
                null, null, null);
        rejectItem.setGlyphicon("halflings remove-circle");
        rejectItem.setAjaxDisabled(true);
        rejectItem.setDropdownItem(true);
        menubar.add(rejectItem);
    }


    /**
     * Get live content browser link.
     *
     * @param cmsCtx CMS context
     * @param menubar menubar
     * @throws Exception
     */
    protected void getLiveContentBrowserLink(CMSServiceCtx cmsCtx, List<MenubarItem> menubar) throws Exception {
        if (cmsCtx.getRequest().getRemoteUser() == null) {
            return;
        }

        if ( !ContextualizationHelper.isCurrentDocContextualized(cmsCtx))    {
            return;
        }

        // Portal controller context
        PortalControllerContext portalControllerContext = new PortalControllerContext(cmsCtx.getPortletCtx(), cmsCtx.getRequest(), cmsCtx.getResponse());

        // Current document
        Document document = (Document) cmsCtx.getDoc();
        String path;
        boolean folderish;
        if (document == null) {
            path = cmsCtx.getCreationPath();

            // Items with creation path are presumed folderish
            folderish = (path != null);
        } else {
            path = document.getPath();

            CMSItemType cmsItemType = this.customizer.getCMSItemTypes().get(document.getType());
            folderish = (cmsItemType != null) && cmsItemType.isFolderish();
        }

        CMSPublicationInfos pubInfos = this.cmsService.getPublicationInfos(cmsCtx, path);


        // Do not browse into remote proxy
        if( this.isRemoteProxy(cmsCtx, pubInfos)) {
            return;
        }

        if (!pubInfos.isLiveSpace() && !pubInfos.getSubTypes().isEmpty() && folderish) {
            // Internationalization bundle
            Bundle bundle = this.bundleFactory.getBundle(cmsCtx.getRequest().getLocale());

            // Live content browser popup link
            Map<String, String> properties = new HashMap<String, String>(1);
            properties.put("osivia.browser.path", path);
            String browserUrl = this.urlFactory.getStartPortletUrl(portalControllerContext, "osivia-portal-browser-portlet-instance", properties, true);

            MenubarItem browserItem = new MenubarItem("BROWSE_LIVE_CONTENT", bundle.getString("BROWSE_LIVE_CONTENT"), MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 4,
                    browserUrl, null, "fancyframe_refresh", null);
            browserItem.setGlyphicon("halflings search");
            browserItem.setAjaxDisabled(true);
            browserItem.setDropdownItem(true);
            menubar.add(browserItem);
        }
    }

    /**
     *
     * @param cmsCtx
     * @param menubar
     * @throws CMSException
     */
	protected void getDriveEditUrl(CMSServiceCtx cmsCtx, List<MenubarItem> menubar) throws CMSException {
		if (cmsCtx.getRequest().getRemoteUser() == null) {
			return;
		}

		if (!ContextualizationHelper.isCurrentDocContextualized(cmsCtx)) {
			return;
		}

		// Current document
		Document document = (Document) cmsCtx.getDoc();
		String path = document.getPath();
		CMSPublicationInfos pubInfos = this.cmsService.getPublicationInfos(cmsCtx, path);

		if (pubInfos.getDriveEditURL() != null) {
			// Internationalization bundle
			Bundle bundle = this.bundleFactory.getBundle(cmsCtx.getRequest().getLocale());

			MenubarItem driveEditItem = new MenubarItem("DRIVE_EDIT", bundle.getString("DRIVE_EDIT"), MenubarItem.ORDER_PORTLET_GENERIC + 4,
					pubInfos.getDriveEditURL(), null, null, null);
			driveEditItem.setGlyphicon("halflings play");
			driveEditItem.setAjaxDisabled(true);
			driveEditItem.setDropdownItem(true);
			menubar.add(driveEditItem);
		}
	}

    /**
     *
     * @param cmsCtx
     * @param menubar
     * @throws CMSException
     */
    protected void getSynchronizeLink(CMSServiceCtx cmsCtx, List<MenubarItem> menubar) throws CMSException {
        if (cmsCtx.getRequest().getRemoteUser() == null) {
            return;
        }

        if (!ContextualizationHelper.isCurrentDocContextualized(cmsCtx)) {
            return;
        }

        // Internationalization bundle
        Bundle bundle = this.bundleFactory.getBundle(cmsCtx.getRequest().getLocale());


        // Current document
        Document document = (Document) cmsCtx.getDoc();
        String path = document.getPath();
        CMSPublicationInfos pubInfos = this.cmsService.getPublicationInfos(cmsCtx, path);

        Boolean enableParamter = null;
        String command = null;
        String icon = null;
        EcmFilesCommand ecmAction = null;

        if (pubInfos.isCanSynchronize()) {
            enableParamter = true;
            command = "SYNCHRONIZE";
            icon = "refresh";
            ecmAction = EcmFilesCommand.synchronizeFolder;
        }
        else if(pubInfos.isCanUnsynchronize()) {
            enableParamter = false;
            command = "UNSYNCHRONIZE";
            icon = "ban-circle";
            ecmAction = EcmFilesCommand.unsynchronizeFolder;
        }

        if (enableParamter != null) {




			String synchronizeUrl = this.urlFactory.getEcmFilesManagementUrl(
new PortalControllerContext(cmsCtx.getPortletCtx(), cmsCtx.getRequest(),
                    cmsCtx.getResponse()), pubInfos.getDocumentPath(), ecmAction);


            MenubarItem synchronizeItem = new MenubarItem(command, bundle.getString(command), MenubarItem.ORDER_PORTLET_GENERIC + 5,
                    synchronizeUrl, null, null, null);
            synchronizeItem.setGlyphicon("halflings "+icon);
            synchronizeItem.setAjaxDisabled(true);
            synchronizeItem.setDropdownItem(true);
            menubar.add(synchronizeItem);
        } else if (pubInfos.getSynchronizationRootPath() != null) {

            String rootUrl = this.urlFactory.getCMSUrl(new PortalControllerContext(cmsCtx.getPortletCtx(), cmsCtx.getRequest(), cmsCtx.getResponse()), null,
                    pubInfos.getSynchronizationRootPath(), null, null, null, null, null, null, null);

            MenubarItem rootUrlItem = new MenubarItem("SYNCHRO_ROOT_URL", bundle.getString("SYNCHRO_ROOT_URL"), MenubarItem.ORDER_PORTLET_GENERIC + 5, rootUrl,
                    null, null, null);
            rootUrlItem.setGlyphicon("halflings backward");
            rootUrlItem.setAjaxDisabled(true);
            rootUrlItem.setDropdownItem(true);
            menubar.add(rootUrlItem);

        }

    }

	protected void getSubscribeLink(CMSServiceCtx cmsCtx, List<MenubarItem> menubar) throws CMSException {
		
        // Current document
        Document document = (Document) cmsCtx.getDoc();
        String path = document.getPath();
		CMSPublicationInfos pubInfos = this.cmsService.getPublicationInfos(cmsCtx, path);
		SubscriptionStatus subscriptionStatus = pubInfos.getSubscriptionStatus();
		
		if(subscriptionStatus != SubscriptionStatus.no_subscriptions) {
	        // Internationalization bundle
	        Bundle bundle = this.bundleFactory.getBundle(cmsCtx.getRequest().getLocale());
			
	    	String url = "";
			MenubarItem subscribeItem = new MenubarItem("SUBSCRIBE_URL", null, MenubarItem.ORDER_PORTLET_GENERIC + 10, url, null, null, null);

			subscribeItem.setAjaxDisabled(true);
			subscribeItem.setDropdownItem(false);
			
			

			if(subscriptionStatus == SubscriptionStatus.can_subscribe) {

				url = this.urlFactory.getSubscriptionUrl(new PortalControllerContext(cmsCtx.getPortletCtx(), cmsCtx.getRequest(), cmsCtx.getResponse()), path,
						false);

				subscribeItem.setUrl(url);
				subscribeItem.setGlyphicon("flag");
				subscribeItem.setTitle(bundle.getString("SUBSCRIBE_ACTION"));
			}
			else if(subscriptionStatus == SubscriptionStatus.can_unsubscribe) {

				url = this.urlFactory.getSubscriptionUrl(new PortalControllerContext(cmsCtx.getPortletCtx(), cmsCtx.getRequest(), cmsCtx.getResponse()), path,
						true);

				subscribeItem.setUrl(url);
				subscribeItem.setGlyphicon("flag");
				subscribeItem.setTitle(bundle.getString("UNSUBSCRIBE_ACTION"));
			}
			else if(subscriptionStatus == SubscriptionStatus.has_inherited_subscriptions) {
				subscribeItem.setGlyphicon("flag");
				subscribeItem.setTitle(bundle.getString("INHERITED_SUBSCRIPTION"));
				subscribeItem.setClassName("disabled");
			}

			menubar.add(subscribeItem);
		}
    }

    /**
     * Get edit CMS content link.
     *
     * @param cmsCtx CMS context
     * @param menubar menubar
     * @throws Exception
     */
    protected void getEditLink(CMSServiceCtx cmsCtx, List<MenubarItem> menubar) throws Exception {
        if (cmsCtx.getRequest().getRemoteUser() == null) {
            return;
        }

        // Publication infos
        CMSPublicationInfos pubInfos = this.cmsService.getPublicationInfos(cmsCtx, (((Document) (cmsCtx.getDoc())).getPath()));

        if (this.isRemoteProxy(cmsCtx, pubInfos)) {
            return;
        }

        if (pubInfos.isEditableByUser()) {
            if ( ContextualizationHelper.isCurrentDocContextualized(cmsCtx)) {
                Document doc = (Document) cmsCtx.getDoc();

                CMSItemType cmsItemType = this.customizer.getCMSItemTypes().get(doc.getType());
                if ((cmsItemType != null) && cmsItemType.isSupportsPortalForms()) {
                    // Internationalization bundle
                    Bundle bundle = this.bundleFactory.getBundle(cmsCtx.getRequest().getLocale());

                    // Portal controller context
                    PortalControllerContext portalControllerContext = new PortalControllerContext(cmsCtx.getPortletCtx(), cmsCtx.getRequest(),
                            cmsCtx.getResponse());
                    // Callback URL
                    String callbackURL = this.urlFactory.getCMSUrl(portalControllerContext, null, "_NEWID_", null, null, "_LIVE_", null, null, null, null);
                    // Portal base URL
                    // String portalBaseURL = this.urlFactory.getBasePortalUrl(portalControllerContext);
                    // ECM base URL
                    String ecmBaseURL = this.cmsService.getEcmDomain(cmsCtx);

                    Map<String, String> requestParameters = new HashMap<String, String>();
                    String url = this.cmsService.getEcmUrl(cmsCtx, EcmCommand.editDocument, pubInfos.getDocumentPath(), requestParameters);

                    // URL
                    // StringBuilder url = new StringBuilder();
                    // url.append(NuxeoConnectionProperties.getPublicBaseUri().toString());
                    // url.append("/nxpath/default").append(pubInfos.getDocumentPath());
                    // url.append("@toutatice_edit?fromUrl=").append(portalBaseURL);

                    // On click action
                    StringBuilder onClick = new StringBuilder();
                    onClick.append("javascript:setCallbackFromEcmParams('");
                    onClick.append(callbackURL);
                    onClick.append("', '");
                    onClick.append(ecmBaseURL);
                    onClick.append("');");

                    String editLabel = null;
                    if( !pubInfos.isLiveSpace() && !this.isInLiveMode( cmsCtx, pubInfos) && pubInfos.isBeingModified()) {
                        editLabel = bundle.getString("EDIT_LIVE_VERSION");
                    } else {
                        editLabel = bundle.getString("EDIT");
                    }

                    // Menubar item
                    MenubarItem item = new MenubarItem("EDIT", editLabel, MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 2, url, onClick.toString(),
                            "fancyframe_refresh", null);
                    item.setGlyphicon("halflings pencil");
                    item.setAjaxDisabled(true);
                    item.setDropdownItem(true);
                    menubar.add(item);
                }
            }
        }
    }


    /**
     * Get create CMS content link.
     *
     * @param cmsCtx CMS context
     * @param menubar menubar
     * @throws Exception
     */
    protected void getCreateLink(CMSServiceCtx cmsCtx, List<MenubarItem> menubar) throws Exception {
        if (cmsCtx.getRequest().getRemoteUser() == null) {
            return;
        }

        // Creation type
        String creationType = cmsCtx.getCreationType();
        // Creation path
        String creationPath = cmsCtx.getCreationPath();
        // Parent document
        Document parentDoc = (Document) cmsCtx.getDoc();
        if (creationPath != null) {
            parentDoc = (Document) this.cmsService.getContent(cmsCtx, creationPath).getNativeItem();
        }

        // Contextualization
        CMSPublicationInfos pubInfos = this.cmsService.getPublicationInfos(cmsCtx, parentDoc.getPath());

        // Do not add into remote proxy
        if (this.isRemoteProxy(cmsCtx, pubInfos)) {
            return;
        }

        if ((creationPath != null) || ContextualizationHelper.isCurrentDocContextualized(cmsCtx)) {
            // Internationalization bundle
            Bundle bundle = this.bundleFactory.getBundle(cmsCtx.getRequest().getLocale());

            // Portal controller context
            PortalControllerContext portalControllerContext = new PortalControllerContext(cmsCtx.getPortletCtx(), cmsCtx.getRequest(), cmsCtx.getResponse());
            // Callback URL
            // String callbackURL = this.urlFactory.getCMSUrl(portalControllerContext, null, "_NEWID_", null, null, "_LIVE_", null, null, null, null);
            // Test ergo JSS
            String callbackURL = this.urlFactory.getRefreshPageUrl(portalControllerContext);

            // Portal base URL
            // String portalBaseURL = this.urlFactory.getBasePortalUrl(portalControllerContext);
            // ECM base URL
            String ecmBaseURL = this.cmsService.getEcmDomain(cmsCtx);
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
            Map<CMSItemType, String> creationTypes = new HashMap<CMSItemType, String>(subTypes.size());

            Map<String, CMSItemType> managedTypes = this.customizer.getCMSItemTypes();
            CMSItemType containerDocType = managedTypes.get(parentDoc.getType());
            if (containerDocType != null) {
                for (String docType : subTypes.keySet()) {
                    // Is this type managed at portal level ?
                    if (containerDocType.getPortalFormSubTypes().contains(docType) && ((creationType == null) || creationType.equals(docType))) {
                        CMSItemType docTypeDef = managedTypes.get(docType);
                        if ((docTypeDef != null) && docTypeDef.isSupportsPortalForms()) {
                            Map<String, String> requestParameters = new HashMap<String, String>();
                            requestParameters.put("type", docType);
                            String url = this.cmsService.getEcmUrl(cmsCtx, EcmCommand.createDocument, pubInfos.getDocumentPath(), requestParameters);

                            // CMS item type
                            CMSItemType cmsItemType = managedTypes.get(docType);
                            if (cmsItemType != null) {
                                creationTypes.put(cmsItemType, url);
                            }
                        }
                    }
                }
            }



            if (creationTypes.size() == 1) {
                // No fancybox

                Entry<CMSItemType, String> entry = creationTypes.entrySet().iterator().next();
                String url = entry.getValue();

                // Menubar item
                MenubarItem item = new MenubarItem("ADD", bundle.getString("ADD"), MenubarItem.ORDER_PORTLET_GENERIC, url, onclick,
                        "fancyframe_refresh", "nuxeo");
                item.setGlyphicon("halflings plus");
                item.setDropdownItem(false);
                item.setAjaxDisabled(true);
                menubar.add(item);
            } else if (creationTypes.size() > 0) {
                // Namespace
                String namespace = cmsCtx.getResponse().getNamespace();

                // Fancybox identifier
                String fancyboxId = namespace + "_PORTAL_CREATE";


                // Container
                Element container = DOM4JUtils.generateDivElement("container-fluid");
                DOM4JUtils.addAttribute(container, HTMLConstants.ID, fancyboxId);

                // Title
                Element title = DOM4JUtils.generateElement(HTMLConstants.P, "h3", bundle.getString("ADD_CONTENT"));
                container.add(title);

                // List group
                Element listGroup = DOM4JUtils.generateDivElement("list-group");
                container.add(listGroup);

                for (Entry<CMSItemType, String> entry : creationTypes.entrySet()) {
                    CMSItemType cmsItemType = entry.getKey();

                    // Type name
                    String name = bundle.getString(StringUtils.upperCase(cmsItemType.getName()));

                    // List group item
                    Element listGroupItem = DOM4JUtils.generateLinkElement(entry.getValue(), null, onclick, "list-group-item fancyframe_refresh", name,
                            cmsItemType.getGlyph());
                    listGroup.add(listGroupItem);
                }


                // Fancybox callback URL : refresh all page
                String fancyCallbackURL = this.urlFactory.getRefreshPageUrl(portalControllerContext);
                // Fancybox onclick action
                String fancyOnClick = "setCallbackParams(null, '" + fancyCallbackURL + "')";

                // Menubar item
                MenubarItem item = new MenubarItem("ADD", bundle.getString("ADD"), MenubarItem.ORDER_PORTLET_GENERIC, "#" + fancyboxId, fancyOnClick,
                        "fancybox_inline", null);
                item.setGlyphicon("halflings plus");
                item.setAjaxDisabled(true);
                item.setAssociatedHtml(DOM4JUtils.write(container));
                item.setDropdownItem(false);
                menubar.add(item);
            }
        }
    }


    /**
     * Get delete link.
     *
     * @param cmsContext CMS context
     * @param menubar menubar
     * @throws Exception
     */
    protected void getDeleteLink(CMSServiceCtx cmsContext, List<MenubarItem> menubar) throws Exception {
        if (cmsContext.getRequest().getRemoteUser() == null) {
            return;
        }

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
                    // Fancybox properties
                    Map<String, String> properties = new HashMap<String, String>();
                    properties.put("docId", document.getId());
                    properties.put("docPath", document.getPath());

                    // Internationalization bundle
                    Bundle bundle = this.bundleFactory.getBundle(cmsContext.getRequest().getLocale());

                    // Fancybox identifier
                    String fancyboxId = cmsContext.getResponse().getNamespace() + "_PORTAL_DELETE";

                    // Fancybox delete action URL
                    String putInTrashUrl = this.urlFactory.getPutDocumentInTrashUrl(
                            new PortalControllerContext(cmsContext.getPortletCtx(), cmsContext.getRequest(), cmsContext.getResponse()), pubInfos.getLiveId(),
                            pubInfos.getDocumentPath());

                    // Fancybox HTML data
                    String fancybox = this.generateDeleteConfirmationFancybox(properties, bundle, fancyboxId, putInTrashUrl);


                    // URL
                    String url = "#" + fancyboxId;

                    // Menubar item
                    MenubarItem item = new MenubarItem("DELETE", bundle.getString("DELETE"), MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 20, url, null,
                            "fancybox_inline", null);
                    item.setGlyphicon("halflings trash");
                    item.setAjaxDisabled(true);
                    item.setAssociatedHtml(fancybox);
                    item.setDropdownItem(true);
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
        // Container
        Element container = DOM4JUtils.generateDivElement("container-fluid");
        DOM4JUtils.addAttribute(container, HTMLConstants.ID, fancyboxId);

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
        Element okButton = DOM4JUtils.generateElement(HTMLConstants.BUTTON, "btn btn-default btn-warning", bundle.getString("YES"), "halflings warning-sign",
                null);
        DOM4JUtils.addAttribute(okButton, HTMLConstants.TYPE, HTMLConstants.INPUT_TYPE_SUBMIT);
        form.add(okButton);

        // Cancel button
        Element cancelButton = DOM4JUtils.generateElement(HTMLConstants.BUTTON, "btn btn-default", bundle.getString("NO"));
        DOM4JUtils.addAttribute(cancelButton, HTMLConstants.TYPE, HTMLConstants.INPUT_TYPE_BUTTON);
        DOM4JUtils.addAttribute(cancelButton, HTMLConstants.ONCLICK, "closeFancybox()");
        form.add(cancelButton);

        return DOM4JUtils.write(container);
    }


    protected void addContextualizationLinkItem(List<MenubarItem> menuBar, String displayName, String url) throws Exception {
        MenubarItem item = new MenubarItem("CONTEXTUALIZE", "Espace " + displayName, MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 1, url, null, null, null);
        item.setGlyphicon("halflings share");
        item.setAjaxDisabled(true);
        menuBar.add(item);
    }


    /**
     * Affiche un lien de recontextualisation explicite
     * (dans une page existante ou une nouvelle page)
     *
     * @param cmsCtx
     * @param menuBar
     * @throws Exception
     */
    protected void getContextualizationLink(CMSServiceCtx cmsCtx, List<MenubarItem> menuBar) throws Exception {

        if (!WindowState.MAXIMIZED.equals(cmsCtx.getRequest().getWindowState())) {
            return;
        }

        PortalControllerContext portalCtx = new PortalControllerContext(cmsCtx.getPortletCtx(), cmsCtx.getRequest(), cmsCtx.getResponse());

        Page currentPage = null;

        Window window = (Window) cmsCtx.getRequest().getAttribute("osivia.window");
        if (window != null) {
            currentPage = window.getPage();
        }

        // On regarde dans quelle page le contenu ext contextualisé

        Page page = this.urlFactory.getPortalCMSContextualizedPage(portalCtx, (((Document) (cmsCtx.getDoc())).getPath()));

        // Si la page correspond à la page courant on affiche pas le lien
        if ((page == null) || !page.getId().equals(currentPage.getId())) {

            // On détermine le nom de l'espace

            String spaceDisplayName = null;

            if (page != null) {
                // Soit le nom de la page
                Locale locale = Locale.FRENCH;
                spaceDisplayName = page.getDisplayName().getString(locale, true);
                if (spaceDisplayName == null) {
                    spaceDisplayName = page.getName();
                }
            } else {
                // Soit le nom de l'espace de publication
                CMSPublicationInfos pubInfos = this.cmsService.getPublicationInfos(cmsCtx, ((Document) (cmsCtx.getDoc())).getPath());

                if (pubInfos.getPublishSpacePath() != null) {
                    CMSItem pubConfig = this.cmsService.getSpaceConfig(cmsCtx, pubInfos.getPublishSpacePath());
                    if ("1".equals(pubConfig.getProperties().get("contextualizeInternalContents"))) {
                        spaceDisplayName = pubInfos.getPublishSpaceDisplayName();
                    }

                } /*
                 * TOCHECK:
                 * else {
                 * if (pubInfos.getWorkspacePath() != null && pubInfos.isWorkspaceInContextualization()) {
                 * spaceDisplayName = pubInfos.getWorkspaceDisplayName();
                 * }
                 * }
                 */
            }

            if (spaceDisplayName != null) {


                String url = this.urlFactory.getCMSUrl(new PortalControllerContext(cmsCtx.getPortletCtx(), cmsCtx.getRequest(), cmsCtx.getResponse()),
                        currentPage.getId().toString(PortalObjectPath.CANONICAL_FORMAT), (((Document) (cmsCtx.getDoc())).getPath()), null,
                        IPortalUrlFactory.CONTEXTUALIZATION_PORTAL, null, null, null, null, null);

                this.addContextualizationLinkItem(menuBar, spaceDisplayName, url);

            }

        }

        return;
    }


    /**
     * Add permalink item.
     *
     * @param cmsContext CMS context
     * @param menubar current menubar
     * @param url permalink URL
     * @throws Exception
     */
    protected void addPermaLinkItem(CMSServiceCtx cmsContext, List<MenubarItem> menubar, String url) throws Exception {
        // Bundle
        Bundle bundle = this.bundleFactory.getBundle(cmsContext.getRequest().getLocale());

        MenubarItem item = new MenubarItem("PERMALINK", bundle.getString("PERMALINK"), MenubarItem.ORDER_PORTLET_GENERIC + 2, url, null, null, null);
        item.setGlyphicon("halflings link");
        item.setAjaxDisabled(true);
        menubar.add(item);
    }





    protected String computePermaLinkUrl(CMSServiceCtx cmsCtx, List<MenubarItem> menuBar) throws Exception {

        String permLinkPath = this.customizer.getContentWebIdPath(cmsCtx);

        String permaLinkURL = this.getUrlFactory().getPermaLink(
                new PortalControllerContext(cmsCtx.getPortletCtx(), cmsCtx.getRequest(), cmsCtx.getResponse()), null, null,
                permLinkPath, IPortalUrlFactory.PERM_LINK_TYPE_CMS);

        permaLinkURL = ContextualizationHelper.getLivePath(permaLinkURL);

        return permaLinkURL;
    }


    protected boolean mustDisplayPermalink(CMSServiceCtx cmsCtx, List<MenubarItem> menuBar) throws Exception {

        boolean displayPermaLink = false;

        if (ContextualizationHelper.isCurrentDocContextualized(cmsCtx)) {
            displayPermaLink = true;
        }

        // Current portal
        Portal portal = PortalObjectUtils.getPortal(cmsCtx.getControllerContext());
        // Space site indicator
        boolean spaceSite = PortalObjectUtils.isSpaceSite(portal);
        if (spaceSite) {
            displayPermaLink = false;
        }


        return displayPermaLink;
    }
    /**
     * Get permalink link.
     *
     * @param cmsContext CMS context
     * @param menubar current menubar
     * @throws Exception
     */
    protected void getPermaLinkLink(CMSServiceCtx cmsContext, List<MenubarItem> menubar) throws Exception {

        if (!this.mustDisplayPermalink(cmsContext, menubar)) {
            return;
        }


        String permaLinkURL = this.computePermaLinkUrl(cmsContext, menubar);

        if (permaLinkURL != null) {
            this.addPermaLinkItem(cmsContext, menubar, permaLinkURL);
        }


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
