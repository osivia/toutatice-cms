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

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.portal.common.invocation.Scope;
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
import org.osivia.portal.core.constants.InternalConstants;

import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoConnectionProperties;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.DefaultCMSCustomizer;
import fr.toutatice.portail.cms.nuxeo.portlets.files.SubType;
import fr.toutatice.portail.cms.nuxeo.portlets.service.CMSService;


/**
 * Menu bar associée aux contenus
 *
 *
 * @author jeanseb
 *
 */
public class MenuBarFormater {

    CMSService cmsService;
    IPortalUrlFactory urlFactory;
    DefaultCMSCustomizer customizer;
    PortletContext portletCtx;
    IContributionService contributionService;

    /** Bundle factory. */
    private IBundleFactory bundleFactory;


    public IPortalUrlFactory getPortalUrlFactory() throws Exception {
        if (this.urlFactory == null) {
            this.urlFactory = (IPortalUrlFactory) this.portletCtx.getAttribute("UrlService");
        }

        return this.urlFactory;
    }






    public IContributionService getContributionService() throws Exception {
        if (this.contributionService == null) {
            this.contributionService = Locator.findMBean(IContributionService.class, IContributionService.MBEAN_NAME);
        }

        return this.contributionService;
    }

    public MenuBarFormater(PortletContext portletCtx, DefaultCMSCustomizer customizer, CMSService cmsService) {
        super();
        this.cmsService = cmsService;
        this.portletCtx = portletCtx;
        this.customizer = customizer;
        // Bundle factory
        IInternationalizationService internationalizationService = (IInternationalizationService) portletCtx
                .getAttribute(Constants.INTERNATIONALIZATION_SERVICE_NAME);
        this.bundleFactory = internationalizationService.getBundleFactory(this.getClass().getClassLoader());
    };



    @SuppressWarnings("unchecked")
    private void formatDefaultContentMenuBar(CMSServiceCtx cmsCtx) throws Exception {

        if ((cmsCtx.getDoc() == null) && (cmsCtx.getCreationPath() == null)) {
            return;
        }

        PortletRequest request = cmsCtx.getRequest();

        // Menubar
        List<MenubarItem> menubar = (List<MenubarItem>) request.getAttribute("osivia.menuBar");

        
        // Check if web page mode
        // layout contains CMS regions and content supports fragments
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


            if (cmsCtx.getDoc() != null && !webPageFragment) {
                this.getPermaLinkLink(cmsCtx, menubar);
            }

            if (cmsCtx.getDoc() != null  && !webPageFragment) {
                this.getContextualizationLink(cmsCtx, menubar);
            }

            if (cmsCtx.getDoc() != null  && !webPageFragment) {
                this.getChangeModeLink(cmsCtx, menubar);
            }

            this.getLiveContentBrowserLink(cmsCtx, menubar);

            if (cmsCtx.getDoc() != null  && !webPageFragment) {
                this.getEditLink(cmsCtx, menubar);
            }

            this.getCreateLink(cmsCtx, menubar);

            if (cmsCtx.getDoc() != null  && !webPageFragment) {
                this.getDeleteLink(cmsCtx, menubar);
            }


            if (cmsCtx.getDoc() != null  && !webPageFragment) {
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

    protected void adaptDropdowMenu(CMSServiceCtx cmsCtx) throws Exception {
        
        PortletRequest request = cmsCtx.getRequest();
        List<MenubarItem> menubar = (List<MenubarItem>) request.getAttribute("osivia.menuBar");
        
        // Duplication bouton ajouter

        MenubarItem duplicateItem = null;
        boolean otherItem = false;
        int indice = -1;
        int addIndice = -1;
        for(MenubarItem menuItem : menubar){
            indice++;
            if (menuItem.isDropdownItem()) {
                if ("ADD".equals(menuItem.getId())) {
                    duplicateItem = menuItem.clone();
                    duplicateItem.setDropdownItem(false);
                    addIndice = indice;
                } else {
                    otherItem = true;
                }
            }
        }
        
        if( duplicateItem != null)  {
            menubar.add(duplicateItem) ;

            // Si uniquement bouton ajouter dans le menu, le supprimer

            if( !otherItem)
                menubar.remove(addIndice) ;
        }
    }
    
    public void formatContentMenuBar(CMSServiceCtx cmsCtx) throws Exception {
        formatDefaultContentMenuBar(cmsCtx);
        adaptDropdowMenu(cmsCtx);
    }
    

    /**
     * Controls if live mode is associated with the current document
     *
     * @param cmsCtx
     * @return
     */
    protected boolean isInLiveMode(CMSServiceCtx cmsCtx, CMSPublicationInfos pubInfos){

        boolean liveMode = false;

        EditionState curState = (EditionState) cmsCtx.getRequest().getAttribute("osivia.editionState");

        if( (curState != null) && curState.getContributionMode().equals(EditionState.CONTRIBUTION_MODE_EDITION))  {
            if( curState.getDocPath().equals(pubInfos.getDocumentPath())) {
                liveMode = true;
            }
        }
        return liveMode;
    }


    protected boolean isRemoteProxy(CMSServiceCtx cmsCtx, CMSPublicationInfos pubInfos){
        
        if( cmsCtx.getDoc() == null)
            return false;

        if( pubInfos.isPublished() && !this.isInLiveMode(cmsCtx, pubInfos)){
            String docPath = (((Document) (cmsCtx.getDoc())).getPath());

            // Pour un proxy distant, le documentPath du pubInfos est égal au docPath
            if( pubInfos.getDocumentPath().equals(docPath))
                return true;
        }


        return false;
    }







    /**
     * Add Nuxeo administration link item.
     *
     * @param menuBar current menu bar to edit
     * @param url Nuxeo document URL
     * @throws Exception
     */
    protected void addAdministrationLinkItem(List<MenubarItem> menuBar, String url) throws Exception {

        MenubarItem item = new MenubarItem("MANAGE", "Gérer", MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 24, url, null, "portlet-menuitem-nuxeo-manage", "nuxeo");
        item.setAjaxDisabled(true);
        item.setDropdownItem(true);
        menuBar.add(item);

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
        if( this.isRemoteProxy(cmsCtx, pubInfos))
            return;


        if (pubInfos.isEditableByUser() && ContextualizationHelper.isCurrentDocContextualized(cmsCtx)) {
            String url = NuxeoConnectionProperties.getPublicBaseUri().toString() + "/nxdoc/default/" + pubInfos.getLiveId() + "/view_documents";
            this.addAdministrationLinkItem(menuBar, url);

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
                        null, "portlet-menuitem-edition live", null);
                liveIndicator.setStateItem(true);
                menubar.add(liveIndicator);
            } else {
                editionState = new EditionState(EditionState.CONTRIBUTION_MODE_EDITION, path);

                // Forget old state
                this.getContributionService().removeWindowEditionState(portalControllerContext);
            }

            // Do not insert any action for remote proxy
            if (!this.isRemoteProxy(cmsCtx, pubInfos)) {
                if (this.isInLiveMode(cmsCtx, pubInfos)) {
                	if (pubInfos.isUserCanValidate()) {
	                    // Publish menubar item
	                    String publishURL = this.getContributionService().getPublishContributionURL(portalControllerContext, pubInfos.getDocumentPath());
	                    MenubarItem publishItem = new MenubarItem("PUBLISH", bundle.getString("PUBLISH"), MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 12, publishURL,
	                            null, "publish-action", null);
	                    publishItem.setAjaxDisabled(true);
	                    publishItem.setDropdownItem(true);
	                    menubar.add(publishItem);
                	}

                    // Go to proxy menubar item
                    if( pubInfos.isPublished()){
                        String proxyURL = this.getContributionService().getChangeEditionStateUrl(portalControllerContext, editionState);
                        MenubarItem proxyItem = new MenubarItem("PROXY_RETURN", bundle.getString("PROXY_RETURN"), MenubarItem.ORDER_PORTLET_SPECIFIC_CMS, proxyURL,
                            null, "published", null);
                        proxyItem.setAjaxDisabled(true);
                        proxyItem.setDropdownItem(true);
                        menubar.add(proxyItem);
                    }
                } else {
                	if (pubInfos.isUserCanValidate()) {
	                    // Unpublish menubar item
	                    String unpublishURL = this.getContributionService().getUnpublishContributionURL(portalControllerContext, pubInfos.getDocumentPath());
	                    MenubarItem publishItem = new MenubarItem("UNPUBLISH", bundle.getString("UNPUBLISH"), MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 12,
	                            unpublishURL, null, "unpublish-action", null);
	                    publishItem.setAjaxDisabled(true);
	                    publishItem.setDropdownItem(true);
	                    menubar.add(publishItem);
                	}

                    if (pubInfos.isBeingModified()) {
                        // Go to preview menubar item
                        String previewURL = this.getContributionService().getChangeEditionStateUrl(portalControllerContext, editionState);
                        MenubarItem previewItem = new MenubarItem("LIVE_PREVIEW", bundle.getString("LIVE_PREVIEW"), MenubarItem.ORDER_PORTLET_SPECIFIC_CMS,
                                previewURL, null, "live", null);
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
            

        // Internationalization bundle
        Bundle bundle = this.bundleFactory.getBundle(cmsCtx.getRequest().getLocale());
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
        if( this.isRemoteProxy(cmsCtx, pubInfos))
            return;
        
        if (!pubInfos.isLiveSpace() && !pubInfos.getSubTypes().isEmpty() && folderish) {
            // Live content browser popup link
            Map<String, String> properties = new HashMap<String, String>(1);
            properties.put("osivia.browser.path", path);
            Map<String, String> parameters = new HashMap<String, String>(0);
            String browserUrl = this.getPortalUrlFactory()
                    .getStartPortletUrl(portalControllerContext, "osivia-portal-browser-portlet-instance", properties, parameters, true);
            MenubarItem browserItem = new MenubarItem("BROWSE_LIVE_CONTENT", bundle.getString("BROWSE_LIVE_CONTENT"), MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 4,
                    browserUrl, null, "browser live fancyframe_refresh", "");
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
        

        // Internationalization bundle
        Bundle bundle = this.bundleFactory.getBundle(cmsCtx.getRequest().getLocale());

        // Publication infos
        CMSPublicationInfos pubInfos = this.cmsService.getPublicationInfos(cmsCtx, (((Document) (cmsCtx.getDoc())).getPath()));
        
        
        if( this.isRemoteProxy(cmsCtx, pubInfos))
            return;

        
        if (pubInfos.isEditableByUser()) {
            if ( ContextualizationHelper.isCurrentDocContextualized(cmsCtx)) {
                Document doc = (Document) cmsCtx.getDoc();

                CMSItemType cmsItemType = this.customizer.getCMSItemTypes().get(doc.getType());
                if ((cmsItemType != null) && cmsItemType.isSupportsPortalForms()) {
                    // Portal controller context
                    PortalControllerContext portalControllerContext = new PortalControllerContext(cmsCtx.getPortletCtx(), cmsCtx.getRequest(),
                            cmsCtx.getResponse());
                    // Callback URL
                    String callbackURL = this.getPortalUrlFactory().getCMSUrl(portalControllerContext, null, "_NEWID_", null, null, "_LIVE_", null, null, null, null);
                    // Portal base URL
                    String portalBaseURL = this.getPortalUrlFactory().getBasePortalUrl(portalControllerContext);
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
                    if( !pubInfos.isLiveSpace() && !isInLiveMode( cmsCtx, pubInfos) && pubInfos.isBeingModified()) {
                        editLabel = bundle.getString("EDIT_LIVE_VERSION");
                    }   else
                        editLabel = bundle.getString("EDIT");
                    
                    // Menubar item
                    MenubarItem item = new MenubarItem("EDIT", editLabel, MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 2, url.toString(), onClick.toString(),
                            "fancyframe_refresh edition", "nuxeo");
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
        
        // Internationalization bundle
        Bundle bundle = this.bundleFactory.getBundle(cmsCtx.getRequest().getLocale());

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
        if( this.isRemoteProxy(cmsCtx, pubInfos))
            return;
        
        if ((creationPath != null) || ContextualizationHelper.isCurrentDocContextualized(cmsCtx)) {
            // Portal controller context
            PortalControllerContext portalControllerContext = new PortalControllerContext(cmsCtx.getPortletCtx(), cmsCtx.getRequest(), cmsCtx.getResponse());
            // Callback URL
            String callbackURL = this.getPortalUrlFactory().getCMSUrl(portalControllerContext, null, "_NEWID_", null, null, "_LIVE_", null, null, null, null);
            // Portal base URL
            String portalBaseURL = this.getPortalUrlFactory().getBasePortalUrl(portalControllerContext);
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
                String fancyCallbackURL = this.getPortalUrlFactory().getRefreshPageUrl(portalControllerContext);
                // Fancybox onclick action
                String fancyOnClick = "setCallbackParams(null, '" + fancyCallbackURL + "')";

                // Menubar item
                MenubarItem item = new MenubarItem("ADD", bundle.getString("ADD"), MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 6, "#"
                        + cmsCtx.getResponse().getNamespace() + fancyId, fancyOnClick, "fancybox_inline fancybox-no-title portlet-menuitem-edition add", "nuxeo");
                item.setAjaxDisabled(true);
                item.setAssociatedHtml(fancyContent.toString());
                item.setDropdownItem(true);
                menubar.add(item);
            }
        }
    }


    protected void addDeleteLinkItem(List<MenubarItem> menuBar, String onClick, String url, String fancyContent) throws Exception {

        MenubarItem delete = new MenubarItem("DELETE", "Supprimer", MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 10, url, null,
                "fancybox_inline portlet-menuitem-nuxeo-delete", null);
        delete.setAjaxDisabled(true);
        delete.setAssociatedHtml(fancyContent);
        delete.setDropdownItem(true);
        menuBar.add(delete);

    }


    protected void getDeleteLink(CMSServiceCtx cmsCtx, List<MenubarItem> menuBar) throws Exception {

        if (cmsCtx.getRequest().getRemoteUser() == null) {
            return;
        }


        Document doc = (Document) cmsCtx.getDoc();


        /* Contextualisation */


        CMSPublicationInfos pubInfos = this.cmsService.getPublicationInfos(cmsCtx, doc.getPath());

        // Do not delete remote proxy
        if( this.isRemoteProxy(cmsCtx, pubInfos))
            return;

        // Do not delete published elements

        if (pubInfos.isDeletableByUser() && (pubInfos.isLiveSpace() ||  this.isInLiveMode(cmsCtx, pubInfos))) {
            if (ContextualizationHelper.isCurrentDocContextualized(cmsCtx)) {
                CMSItemType docTypeDef = this.customizer.getCMSItemTypes().get(doc.getType());
                if ((docTypeDef != null) && docTypeDef.isSupportsPortalForms()) {
                    // Lien de création
                    String fancyID = "_PORTAL_DELETE";

                    // fancybox div
                    StringBuffer fancyContent = new StringBuffer();

                    fancyContent.append(" <div id=\"" + cmsCtx.getResponse().getNamespace() + fancyID + "\" class=\"fancybox-content\">");

                    String putInTrashUrl = this.getPortalUrlFactory().getPutDocumentInTrashUrl(
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

                    this.addDeleteLinkItem(menuBar, null, "#" + cmsCtx.getResponse().getNamespace() + fancyID, fancyContent.toString());
                }
            }
        }


    }


    protected void addContextualizationLinkItem(List<MenubarItem> menuBar, String displayName, String url) throws Exception {


        MenubarItem item = new MenubarItem("CONTEXTUALIZE", "Espace " + displayName, MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 1, url, null,
                "portlet-menuitem-contextualize", null);

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

        Page page = this.getPortalUrlFactory().getPortalCMSContextualizedPage(portalCtx, (((Document) (cmsCtx.getDoc())).getPath()));

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


                String url = this.getPortalUrlFactory().getCMSUrl(new PortalControllerContext(cmsCtx.getPortletCtx(), cmsCtx.getRequest(), cmsCtx.getResponse()),
                        currentPage.getId().toString(PortalObjectPath.CANONICAL_FORMAT), (((Document) (cmsCtx.getDoc())).getPath()), null,
                        IPortalUrlFactory.CONTEXTUALIZATION_PORTAL, null, null, null, null, null);

                this.addContextualizationLinkItem(menuBar, spaceDisplayName, url);

            }

        }

        return;
    }


    protected void addPermaLinkItem(CMSServiceCtx cmsCtx, List<MenubarItem> menuBar, String url) throws Exception {
        MenubarItem item = new MenubarItem("PERMLINK", "Permalink", MenubarItem.ORDER_PORTLET_SPECIFIC_CMS, url, null, "portlet-menuitem-permalink", null);

        item.setAjaxDisabled(true);
        menuBar.add(item);

    }
    
    
    protected String computePermaLinkUrl(CMSServiceCtx cmsCtx, List<MenubarItem> menuBar) throws Exception {
        
        String permLinkPath = this.customizer.getContentWebIdPath(cmsCtx);

        String permaLinkURL = this.getPortalUrlFactory().getPermaLink(
                new PortalControllerContext(cmsCtx.getPortletCtx(), cmsCtx.getRequest(), cmsCtx.getResponse()), null, null,
                permLinkPath, IPortalUrlFactory.PERM_LINK_TYPE_CMS);
        
        permaLinkURL = ContextualizationHelper.getLivePath(permaLinkURL);

        return permaLinkURL;
    }
    
    
    protected boolean mustDisplayPermalink(CMSServiceCtx cmsCtx, List<MenubarItem> menuBar) throws Exception {
        
        boolean displayPermaLink = false;

        if (WindowState.MAXIMIZED.equals(cmsCtx.getRequest().getWindowState())) {
            displayPermaLink = true;
        }
        
        // for spaceMenuBar fragment
        if( BooleanUtils.isTrue((Boolean) cmsCtx.getRequest().getAttribute("osivia.cms.forcePermalinkDisplay")))  {
            displayPermaLink = true;            
        }
        
        return displayPermaLink;     
    }
    
    
    protected void getPermaLinkLink(CMSServiceCtx cmsCtx, List<MenubarItem> menuBar) throws Exception {
        
        if( ! mustDisplayPermalink(cmsCtx, menuBar))
            return;
        
        String permaLinkURL = computePermaLinkUrl(cmsCtx, menuBar);

        if (permaLinkURL != null) {
            this.addPermaLinkItem(cmsCtx, menuBar, permaLinkURL);
        }

    }

}
