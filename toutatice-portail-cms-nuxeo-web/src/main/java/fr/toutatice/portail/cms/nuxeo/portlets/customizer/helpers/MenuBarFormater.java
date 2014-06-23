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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.WindowState;

import org.apache.commons.lang.StringUtils;
import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.PortalObjectPath;
import org.jboss.portal.core.model.portal.Window;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.contribution.IContributionService;
import org.osivia.portal.api.contribution.IContributionService.EditionState;
import org.osivia.portal.api.internationalization.Bundle;
import org.osivia.portal.api.internationalization.IBundleFactory;
import org.osivia.portal.api.internationalization.IInternationalizationService;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.menubar.MenubarItem;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSItemType;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.cms.CMSServiceCtx;

import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoConnectionProperties;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.DefaultCMSCustomizer;
import fr.toutatice.portail.cms.nuxeo.portlets.files.SubType;
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
    public void formatContentMenuBar(CMSServiceCtx cmsCtx) throws Exception {
        if ((cmsCtx.getDoc() == null) && (cmsCtx.getCreationPath() == null)) {
            return;
        }

        PortletRequest request = cmsCtx.getRequest();

        // Menubar
        List<MenubarItem> menubar = (List<MenubarItem>) request.getAttribute("osivia.menuBar");

        // Check if web page mode layout contains CMS regions and content supports fragments
        // Edition mode is supported by the webpage menu
        boolean webPageFragment = false;
        if( cmsCtx.getDoc() != null ) {
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
        } catch (CMSException e) {
            if ((e.getErrorCode() == CMSException.ERROR_FORBIDDEN) || (e.getErrorCode() == CMSException.ERROR_NOTFOUND)) {
                // On ne fait rien : le document n'existe pas ou je n'ai pas les droits
            } else  {
                throw e;
            }
        }
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
                liveIndicator.setGlyphicon("halflings eye-open");
                liveIndicator.setStateItem(true);
                menubar.add(liveIndicator);
            } else {
                editionState = new EditionState(EditionState.CONTRIBUTION_MODE_EDITION, path);

                // Forget old state
                this.contributionService.removeWindowEditionState(portalControllerContext);
            }

            // Do not insert any action for remote proxy
            if (!this.isRemoteProxy(cmsCtx, pubInfos)) {
                if (this.isInLiveMode(cmsCtx, pubInfos)) {
                    // Publish menubar item
                    String publishURL = this.contributionService.getPublishContributionURL(portalControllerContext, pubInfos.getDocumentPath());
                    MenubarItem publishItem = new MenubarItem("PUBLISH", bundle.getString("PUBLISH"), MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 12, publishURL,
                            null, null, null);
                    publishItem.setGlyphicon("halflings ok-circle");
                    publishItem.setAjaxDisabled(true);
                    publishItem.setDropdownItem(true);
                    menubar.add(publishItem);

                    // Go to proxy menubar item
                    if( pubInfos.isPublished()){
                        String proxyURL = this.contributionService.getChangeEditionStateUrl(portalControllerContext, editionState);
                        MenubarItem proxyItem = new MenubarItem("PROXY_RETURN", bundle.getString("PROXY_RETURN"), MenubarItem.ORDER_PORTLET_SPECIFIC_CMS, proxyURL,
                            null, null, null);
                        proxyItem.setGlyphicon("halflings eye-close");
                        proxyItem.setAjaxDisabled(true);
                        proxyItem.setDropdownItem(true);
                        menubar.add(proxyItem);
                    }
                } else {
                    // Unpublish menubar item
                    String unpublishURL = this.contributionService.getUnpublishContributionURL(portalControllerContext, pubInfos.getDocumentPath());
                    MenubarItem unpublishItem = new MenubarItem("UNPUBLISH", bundle.getString("UNPUBLISH"), MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 12,
                            unpublishURL, null, null, null);
                    unpublishItem.setGlyphicon("halflings remove-circle");
                    unpublishItem.setAjaxDisabled(true);
                    unpublishItem.setDropdownItem(true);
                    menubar.add(unpublishItem);

                    if (pubInfos.isBeingModified()) {
                        // Current modification indicator
                        MenubarItem modificationIndicator = new MenubarItem("MODIFICATION_MESSAGE", bundle.getString("MODIFICATION_MESSAGE"),
                                MenubarItem.ORDER_PORTLET_SPECIFIC_CMS, null, null, null, null);
                        modificationIndicator.setGlyphicon("halflings warning-sign");
                        modificationIndicator.setStateItem(true);
                        modificationIndicator.setDropdownItem(true);
                        menubar.add(modificationIndicator);

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
            Map<String, String> parameters = new HashMap<String, String>(0);
            String browserUrl = this.urlFactory.getStartPortletUrl(portalControllerContext, "osivia-portal-browser-portlet-instance", properties, parameters,
                    true);

            MenubarItem browserItem = new MenubarItem("BROWSE_LIVE_CONTENT", bundle.getString("BROWSE_LIVE_CONTENT"), MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 4,
                    browserUrl, null, "fancyframe_refresh", null);
            browserItem.setGlyphicon("halflings search");
            browserItem.setAjaxDisabled(true);
            browserItem.setDropdownItem(true);
            menubar.add(browserItem);
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
                    String portalBaseURL = this.urlFactory.getBasePortalUrl(portalControllerContext);
                    // ECM base URL
                    String ecmBaseURL = this.cmsService.getEcmDomain(cmsCtx);

                    // URL
                    StringBuilder url = new StringBuilder();
                    url.append(NuxeoConnectionProperties.getPublicBaseUri().toString());
                    url.append("/nxpath/default").append(pubInfos.getDocumentPath());
                    url.append("@toutatice_edit?fromUrl=").append(portalBaseURL);

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
                    MenubarItem item = new MenubarItem("EDIT", editLabel, MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 2, url.toString(), onClick.toString(),
                            "fancyframe_refresh", null);
                    item.setGlyphicon("halflings edit");
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
        if( this.isRemoteProxy(cmsCtx, pubInfos)) {
            return;
        }

        if ((creationPath != null) || ContextualizationHelper.isCurrentDocContextualized(cmsCtx)) {
            // Internationalization bundle
            Bundle bundle = this.bundleFactory.getBundle(cmsCtx.getRequest().getLocale());

            // Portal controller context
            PortalControllerContext portalControllerContext = new PortalControllerContext(cmsCtx.getPortletCtx(), cmsCtx.getRequest(), cmsCtx.getResponse());
            // Callback URL
            String callbackURL = this.urlFactory.getCMSUrl(portalControllerContext, null, "_NEWID_", null, null, "_LIVE_", null, null, null, null);
            // Portal base URL
            String portalBaseURL = this.urlFactory.getBasePortalUrl(portalControllerContext);
            // ECM base URL
            String ecmBaseURL = this.cmsService.getEcmDomain(cmsCtx);
            // On click action
            StringBuilder onClick = new StringBuilder();
            onClick.append("javascript:setCallbackFromEcmParams('");
            onClick.append(callbackURL);
            onClick.append("', '");
            onClick.append(ecmBaseURL);
            onClick.append("');");

            // Sub-types
            Map<String, String> subTypes = pubInfos.getSubTypes();
            List<SubType> portalDocsToCreate = new ArrayList<SubType>();
            Map<String, CMSItemType> managedTypes = this.customizer.getCMSItemTypes();
            CMSItemType containerDocType = managedTypes.get(parentDoc.getType());
            if (containerDocType != null) {
                for (String docType : subTypes.keySet()) {
                    // Is this type managed at portal level ?
                    if (containerDocType.getPortalFormSubTypes().contains(docType) && ((creationType == null) || creationType.equals(docType))) {
                        CMSItemType docTypeDef = managedTypes.get(docType);
                        if ((docTypeDef != null) && docTypeDef.isSupportsPortalForms()) {
                            // Sub-type URL
                            StringBuilder url = new StringBuilder();
                            url.append(NuxeoConnectionProperties.getPublicBaseUri().toString());
                            url.append("/nxpath/default").append(pubInfos.getDocumentPath());
                            url.append("@toutatice_create?type=").append(docType);
                            url.append("&fromUrl=").append(portalBaseURL);

                            // Sub-type
                            SubType subType = new SubType();
                            subType.setDocType(docType);
                            subType.setName(bundle.getString(docType.toUpperCase()));
                            subType.setUrl(url.toString());
                            portalDocsToCreate.add(subType);
                        }
                    }
                }
            }


            if (portalDocsToCreate.size() == 1) {
                // No fancybox
                StringBuilder url = new StringBuilder();
                url.append(NuxeoConnectionProperties.getPublicBaseUri().toString());
                url.append("/nxpath/default").append(pubInfos.getDocumentPath());
                url.append("@toutatice_create?type=").append(portalDocsToCreate.get(0).getDocType());
                url.append("&fromUrl=").append(portalBaseURL);

                // Menubar item
                MenubarItem item = new MenubarItem("ADD", bundle.getString("ADD"), MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 6, url.toString(), onClick.toString(),
                        "fancyframe_refresh portlet-menuitem-edition add", "nuxeo");
                item.setGlyphicon("halflings plus");
                item.setDropdownItem(true);
                item.setAjaxDisabled(true);
                menubar.add(item);
            } else if (portalDocsToCreate.size() > 0) {
                // Fancybox
                String fancyId = "_PORTAL_CREATE";
                StringBuilder fancyContent = new StringBuilder();

                fancyContent.append("<div class=\"fancybox-content\">");
                fancyContent.append("   <div id=\"" + cmsCtx.getResponse().getNamespace() + "_PORTAL_CREATE\" class=\"document-types\">");
                fancyContent.append("       <div class=\"main-doc-types\" id=\"" + cmsCtx.getResponse().getNamespace() + "_MAIN\">");
                fancyContent.append("           <div class=\"doc-type-title\">Ajouter un contenu</div>");

                int index = 1;
                int nbSubDocs = portalDocsToCreate.size();
                for (SubType subDoc : portalDocsToCreate) {
                    fancyContent.append("<div class=\"doc-type-detail\">");
                    fancyContent.append("   <div class=\"vignette\">");
                    fancyContent.append("       <a class=\"fancyframe_refresh\" href=\"" + subDoc.getUrl() + "\" onclick=\"" + onClick.toString() + "\">");
                    fancyContent.append("           <img src=\"/toutatice-portail-cms-nuxeo/img/icons/" + subDoc.getDocType().toLowerCase() + "_100.png\"> ");
                    fancyContent.append("       </a>");
                    fancyContent.append("   </div>");
                    fancyContent.append("   <div class=\"main\">");
                    fancyContent.append("       <div class=\"title\">");
                    fancyContent.append("           <a class=\"fancyframe_refresh\" href=\"" + subDoc.getUrl() + "\" onclick=\"" + onClick.toString() + "\">"
                            + subDoc.getName() + "</a>");
                    fancyContent.append("       </div>");
                    fancyContent.append("   </div>");
                    fancyContent.append("</div>");

                    if (index < nbSubDocs) {
                        fancyContent.append("<div class=\"vertical-separator\"></div>");
                    }
                    index++;
                }

                fancyContent.append("        </div>");
                fancyContent.append("   </div>");
                fancyContent.append("</div>");


                // Fancybox callback URL : refresh all page
                String fancyCallbackURL = this.urlFactory.getRefreshPageUrl(portalControllerContext);
                // Fancybox onclick action
                String fancyOnClick = "setCallbackParams(null, '" + fancyCallbackURL + "')";

                // Menubar item
                MenubarItem item = new MenubarItem("ADD", bundle.getString("ADD"), MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 6, "#"
                        + cmsCtx.getResponse().getNamespace() + fancyId, fancyOnClick, "fancybox_inline fancybox-no-title", null);
                item.setGlyphicon("halflings plus");
                item.setAjaxDisabled(true);
                item.setAssociatedHtml(fancyContent.toString());
                item.setDropdownItem(true);
                menubar.add(item);
            }
        }
    }


    /**
     * Get delete link.
     *
     * @param cmsCtx CMS context
     * @param menubar menubar
     * @throws Exception
     */
    protected void getDeleteLink(CMSServiceCtx cmsCtx, List<MenubarItem> menubar) throws Exception {
        if (cmsCtx.getRequest().getRemoteUser() == null) {
            return;
        }

        Document doc = (Document) cmsCtx.getDoc();

        // Contextualization
        CMSPublicationInfos pubInfos = this.cmsService.getPublicationInfos(cmsCtx, doc.getPath());

        // Do not delete remote proxy
        if( this.isRemoteProxy(cmsCtx, pubInfos)) {
            return;
        }

        // Do not delete published elements
        if (pubInfos.isDeletableByUser() && (pubInfos.isLiveSpace() ||  this.isInLiveMode(cmsCtx, pubInfos))) {
            if (ContextualizationHelper.isCurrentDocContextualized(cmsCtx)) {
                CMSItemType docTypeDef = this.customizer.getCMSItemTypes().get(doc.getType());
                if ((docTypeDef != null) && docTypeDef.isSupportsPortalForms()) {
                    // Internationalization bundle
                    Bundle bundle = this.bundleFactory.getBundle(cmsCtx.getRequest().getLocale());

                    // Lien de création
                    String fancyID = "_PORTAL_DELETE";

                    // fancybox div
                    StringBuilder fancyContent = new StringBuilder();
                    fancyContent.append(" <div id=\"" + cmsCtx.getResponse().getNamespace() + fancyID + "\">");

                    String putInTrashUrl = this.urlFactory.getPutDocumentInTrashUrl(
                            new PortalControllerContext(cmsCtx.getPortletCtx(), cmsCtx.getRequest(), cmsCtx.getResponse()), pubInfos.getLiveId(),
                            pubInfos.getDocumentPath());

                    fancyContent.append("   <form method=\"post\" action=\"" + putInTrashUrl + "\">");
                    fancyContent.append("       <div>Confirmez-vous la suppression de l'élément ?</div><br/>");
                    fancyContent.append("       <input id=\"currentDocId\" type=\"hidden\" name=\"docId\" value=\"" + doc.getId() + "\"/>");
                    fancyContent.append("       <input id=\"currentDocPath\" type=\"hidden\" name=\"docPath\" value=\"" + doc.getPath() + "\"/>");
                    fancyContent.append("       <input type=\"submit\" name=\"deleteDoc\"  value=\"Confirmer\">");
                    fancyContent.append("       <input type=\"reset\" name=\"noDeleteDoc\"  value=\"Annuler\" onclick=\"closeFancyBox();\">");
                    fancyContent.append("   </form>");
                    fancyContent.append(" </div>");

                    // URL
                    String url = "#" + cmsCtx.getResponse().getNamespace() + fancyID;

                    // Menubar item
                    MenubarItem item = new MenubarItem("DELETE", bundle.getString("DELETE"), MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 20, url, null,
                            "fancybox_inline", null);
                    item.setGlyphicon("halflings remove");
                    item.setAjaxDisabled(true);
                    item.setAssociatedHtml(fancyContent.toString());
                    item.setDropdownItem(true);
                    menubar.add(item);
                }
            }
        }
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


    protected void addPermaLinkItem(List<MenubarItem> menuBar, String url) throws Exception {
        MenubarItem item = new MenubarItem("PERMLINK", "Permalink", MenubarItem.ORDER_PORTLET_SPECIFIC_CMS, url, null, null, null);
        item.setGlyphicon("halflings link");
        item.setAjaxDisabled(true);
        menuBar.add(item);
    }


    protected void getPermaLinkLink(CMSServiceCtx cmsCtx, List<MenubarItem> menuBar) throws Exception {

        if (!WindowState.MAXIMIZED.equals(cmsCtx.getRequest().getWindowState())) {
            return;
        }

        String permLinkPath = this.customizer.getContentWebIdPath(cmsCtx);

        String permaLinkURL = this.urlFactory.getPermaLink(
                new PortalControllerContext(cmsCtx.getPortletCtx(), cmsCtx.getRequest(), cmsCtx.getResponse()), null, null,
                permLinkPath, IPortalUrlFactory.PERM_LINK_TYPE_CMS);

        if (permaLinkURL != null) {
            this.addPermaLinkItem(menuBar, permaLinkURL);
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
