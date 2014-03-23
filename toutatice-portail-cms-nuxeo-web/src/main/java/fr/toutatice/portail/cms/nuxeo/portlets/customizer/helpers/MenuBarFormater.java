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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.WindowState;

import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.PortalObjectPath;
import org.jboss.portal.core.model.portal.Window;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.contribution.IContributionService;
import org.osivia.portal.api.contribution.IContributionService.EditionState;
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
 * Menu bar associée aux contenus
 *
 * Techniquement cette classe est intéressante car elle montre comment on peut
 * déployer à chaud des fonctionnalités partagées entre les portlets
 *
 * Les fonctions du NuxeoController pourront donc etre basculées petit à petit
 * dans le CMSCustomizer
 *
 * A PACKAGER pour la suite
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

    };



    public void formatContentMenuBar(CMSServiceCtx cmsCtx) throws Exception {

        if ((cmsCtx.getDoc() == null) && (cmsCtx.getCreationPath() == null)) {
            return;
        }

        PortletRequest request = cmsCtx.getRequest();

        List<MenubarItem> menuBar = (List<MenubarItem>) request.getAttribute("osivia.menuBar");

        // Menu bar

        try {


            if (cmsCtx.getDoc() != null) {
                this.getPermaLinkLink(cmsCtx, menuBar);
            }

            if (cmsCtx.getDoc() != null) {
                this.getContextualizationLink(cmsCtx, menuBar);
            }

            if (cmsCtx.getDoc() != null) {
                this.getChangeModeLink(cmsCtx, menuBar);
            }

            if (cmsCtx.getDoc() != null) {
                this.getEditLink(cmsCtx, menuBar);
            }

            this.getCreateLink(cmsCtx, menuBar);

            if (cmsCtx.getDoc() != null) {
                this.getDeleteLink(cmsCtx, menuBar);
            }


            if (cmsCtx.getDoc() != null) {
                this.getAdministrationLink(cmsCtx, menuBar);
            }



        } catch (CMSException e) {
            if ((e.getErrorCode() == CMSException.ERROR_FORBIDDEN) || (e.getErrorCode() == CMSException.ERROR_NOTFOUND)) {
                // On ne fait rien : le document n'existe pas ou je n'ai pas
                // les droits
            } else  {
                throw e;
            }
        }
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
        
        if( pubInfos.isPublished() && !isInLiveMode(cmsCtx, pubInfos)){
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

        MenubarItem item = new MenubarItem("MANAGE", "Gérer", MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 3, url, null, "portlet-menuitem-nuxeo-manage", "nuxeo");
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
        
        
        // Do not delete remote proxy
        if( isRemoteProxy(cmsCtx, pubInfos))
            return;
        
        
        if (pubInfos.isEditableByUser() && ContextualizationHelper.isCurrentDocContextualized(cmsCtx)) {
            String url = NuxeoConnectionProperties.getPublicBaseUri().toString() + "/nxdoc/default/" + pubInfos.getLiveId() + "/view_documents";
            this.addAdministrationLinkItem(menuBar, url);

        }
    }




    protected void addChangeModeLinkItem(List<MenubarItem> menuBar, String url, EditionState newState) throws Exception {
        if(EditionState.CONTRIBUTION_MODE_EDITION.equals(newState.getContributionMode()))   {
            MenubarItem liveView = new MenubarItem(null, "Voir la version de travail", MenubarItem.ORDER_PORTLET_SPECIFIC_CMS, url, null, "live", "");
            liveView.setAjaxDisabled(true);
            liveView.setDropdownItem(true);
            menuBar.add(liveView);
        }   else    {



            MenubarItem publishedView = new MenubarItem(null, "Voir la version publiée", MenubarItem.ORDER_PORTLET_SPECIFIC_CMS, url, null, "published", "");
            publishedView.setAjaxDisabled(true);
            publishedView.setDropdownItem(true);
            menuBar.add(publishedView);
        }
    }

    
    

    protected void getChangeModeLink(CMSServiceCtx cmsCtx, List<MenubarItem> menuBar) throws Exception {

        if (cmsCtx.getRequest().getRemoteUser() == null) {
            return;
        }


        Document document = (Document) (cmsCtx.getDoc());
        String path = document.getPath();
        CMSPublicationInfos pubInfos = this.cmsService.getPublicationInfos(cmsCtx, path);


        if (pubInfos.isEditableByUser() && !pubInfos.isLiveSpace() && ContextualizationHelper.isCurrentDocContextualized(cmsCtx)) {

            EditionState newState = new EditionState( EditionState.CONTRIBUTION_MODE_EDITION, path);


            PortalControllerContext portalCtx = new PortalControllerContext(cmsCtx.getPortletCtx(), cmsCtx.getRequest(), cmsCtx.getResponse());
            


            if( this.isInLiveMode(cmsCtx, pubInfos))  {
                newState =  new EditionState( EditionState.CONTRIBUTION_MODE_ONLINE, path);
                
                MenubarItem liveIndicator = new MenubarItem(null, "Version de travail", MenubarItem.ORDER_PORTLET_SPECIFIC_CMS, null, null,
                        "portlet-menuitem-edition live", null);
                liveIndicator.setStateItem(true);
                menuBar.add(liveIndicator);                
            }   else    {
                // Forget old state
                this.getContributionService().removeWindowEditionState(portalCtx) ;

            }


            // Do not insert any action for remote proxy
            if( isRemoteProxy(cmsCtx, pubInfos))
                return;
           
            /* Publish link */
            String url = this.getContributionService().getChangeEditionStateUrl( portalCtx, newState);
            if(! EditionState.CONTRIBUTION_MODE_EDITION.equals(newState.getContributionMode()))   {
                String publishUrl = this.getContributionService().getPublishContributionUrl(portalCtx, pubInfos.getDocumentPath());
            

                MenubarItem publishAction = new MenubarItem(null, "Publier", MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 12, publishUrl, null, "publish-action", "");
                publishAction.setAjaxDisabled(true);
                publishAction.setDropdownItem(true);
                menuBar.add(publishAction);
            }
            
            // No publication yet ! can't change edition mode
            if( this.isInLiveMode(cmsCtx, pubInfos) && !pubInfos.isPublished()) 
                return;

            
            /* Change mode link */
            this.addChangeModeLinkItem(menuBar, url, newState);

            CMSItemType cmsItemType = this.customizer.getCMSItemTypes().get(document.getType());

            if( (cmsItemType != null) && cmsItemType.isFolderish()){

                // Live content browser popup link
                Map<String, String> properties = new HashMap<String, String>(1);
                properties.put("osivia.browser.path", pubInfos.getDocumentPath());
                Map<String, String> parameters = new HashMap<String, String>(0);
                String browserUrl = this.getPortalUrlFactory().getStartPortletUrl(portalCtx, "osivia-portal-browser-portlet-instance", properties, parameters, true);
                MenubarItem browserItem = new MenubarItem(null, "Parcourir les versions de travail", MenubarItem.ORDER_PORTLET_SPECIFIC_CMS, browserUrl, null,
                        "browser live fancyframe_refresh", "");
                browserItem.setAjaxDisabled(true);
                browserItem.setDropdownItem(true);
                menuBar.add(browserItem);
            }
        }
    }


    protected void addEditLinkItem(List<MenubarItem> menuBar, String onClick, String url) throws Exception {

        MenubarItem item = new MenubarItem("EDIT", "Modifier", MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 2, url, onClick, "fancyframe_refresh edition", "nuxeo");
        item.setAjaxDisabled(true);
        item.setDropdownItem(true);
        menuBar.add(item);

    }


    protected void getEditLink(CMSServiceCtx cmsCtx, List<MenubarItem> menuBar) throws Exception {

        if (cmsCtx.getRequest().getRemoteUser() == null) {
            return;
        }


        CMSPublicationInfos pubInfos = this.cmsService.getPublicationInfos(cmsCtx, (((Document) (cmsCtx.getDoc())).getPath()));

        if (pubInfos.isEditableByUser()) {
            if ((pubInfos.isLiveSpace() || (this.isInLiveMode(cmsCtx, pubInfos))) && ContextualizationHelper.isCurrentDocContextualized(cmsCtx)) {
                Document doc = (Document) cmsCtx.getDoc();

                CMSItemType cmsItemType = this.customizer.getCMSItemTypes().get(doc.getType());
                if ((cmsItemType != null) && cmsItemType.isSupportsPortalForms()) {
                    String callBackURL = this.getPortalUrlFactory().getRefreshPageUrl(
                            new PortalControllerContext(cmsCtx.getPortletCtx(), cmsCtx.getRequest(), cmsCtx.getResponse()));

                    String onClick = "setCallbackParams(null, '" + callBackURL + "')";

                    this.addEditLinkItem(menuBar, onClick, NuxeoConnectionProperties.getPublicBaseUri().toString() + "/nxpath/default" + pubInfos.getDocumentPath()
                            + "@toutatice_edit");
                }
            }
        }
    }




    protected void getCreateLink(CMSServiceCtx cmsCtx, List<MenubarItem> menuBar) throws Exception {

        if (cmsCtx.getRequest().getRemoteUser() == null)    {
            return;
        }


        String creationType = cmsCtx.getCreationType();
        String creationPath = cmsCtx.getCreationPath();

        Document parentDoc = (Document) cmsCtx.getDoc();

        if (creationPath != null) {
            parentDoc = (Document) this.cmsService.getContent(cmsCtx, creationPath).getNativeItem();
        }

        /* Contextualisation */


        CMSPublicationInfos pubInfos = this.cmsService.getPublicationInfos(cmsCtx, parentDoc.getPath());

        if ((creationPath != null) || ContextualizationHelper.isCurrentDocContextualized(cmsCtx)) {

            Map<String, String> subTypes = pubInfos.getSubTypes();

            List<SubType> portalDocsToCreate = new ArrayList<SubType>();
            Map<String, CMSItemType> managedTypes = this.customizer.getCMSItemTypes();
            CMSItemType containerDocType = managedTypes.get(parentDoc.getType());
            if (containerDocType != null) {

                for (String docType : subTypes.keySet()) {

                    // is this type managed at portal level ?

                    if (containerDocType.getPortalFormSubTypes().contains(docType) && ((creationType == null) || creationType.equals(docType))) {

                        CMSItemType docTypeDef = managedTypes.get(docType);

                        if ((docTypeDef != null) && docTypeDef.isSupportsPortalForms()) {

                            SubType subType = new SubType();


                            subType.setDocType(docType);
                            subType.setName(subTypes.get(docType));
                            subType.setUrl(NuxeoConnectionProperties.getPublicBaseUri().toString() + "/nxpath/default" + pubInfos.getDocumentPath()
                                    + "@toutatice_create?type=" + docType);
                            portalDocsToCreate.add(subType);
                        }
                    }
                }
            }


            // Refresh all page

            String callBackURL = this.getPortalUrlFactory().getRefreshPageUrl(
                    new PortalControllerContext(cmsCtx.getPortletCtx(), cmsCtx.getRequest(), cmsCtx.getResponse()));


            String onClick = "setCallbackParams(null, '" + callBackURL + "')";


            if (portalDocsToCreate.size() == 1) {
                // Pas de fancybox

                String url = NuxeoConnectionProperties.getPublicBaseUri().toString() + "/nxpath/default" + pubInfos.getDocumentPath() + "@toutatice_create?type="
                        + portalDocsToCreate.get(0).getDocType();

                MenubarItem add = new MenubarItem("CREATE", "Ajouter", MenubarItem.ORDER_PORTLET_SPECIFIC_CMS, url, onClick,
                        "fancyframe_refresh portlet-menuitem-nuxeo-add", "nuxeo");

                add.setDropdownItem(true);
                add.setAjaxDisabled(true);
                menuBar.add(add);

                return;

            }


            /* Lien de création */

            String fancyID = null;
            if (portalDocsToCreate.size() > 0) {
                fancyID = "_PORTAL_CREATE";
            }

            if (fancyID != null) {


                // fancybox div

                StringBuffer fancyContent = new StringBuffer();

                fancyContent.append("<div class=\"fancybox-content\">");
                fancyContent.append("   <div id=\"" + cmsCtx.getResponse().getNamespace() + "_PORTAL_CREATE\" class=\"document-types\">");
                fancyContent.append("       <div class=\"main-doc-types\" id=\"" + cmsCtx.getResponse().getNamespace() + "_MAIN\">");
                fancyContent.append("           <div class=\"doc-type-title\">Ajouter un contenu</div>");

                int index = 1;
                int nbSubDocs = portalDocsToCreate.size();
                for (SubType subDoc : portalDocsToCreate) {

                    fancyContent.append("<div class=\"doc-type-detail\">");
                    fancyContent.append("   <div class=\"vignette\">");
                    fancyContent.append("       <a class=\"fancyframe_refresh\" href=\"" + subDoc.getUrl() + "\">");
                    fancyContent.append("           <img src=\"/toutatice-portail-cms-nuxeo/img/icons/" + subDoc.getDocType().toLowerCase() + "_100.png\"> ");
                    fancyContent.append("       </a>");
                    fancyContent.append("   </div>");
                    fancyContent.append("   <div class=\"main\">");
                    fancyContent.append("       <div class=\"title\">");
                    fancyContent.append("           <a class=\"fancyframe_refresh\" href=\"" + subDoc.getUrl() + "\">" + subDoc.getName() + "</a>");
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


                MenubarItem item = new MenubarItem("EDIT", "Ajouter ", MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 1, "#" + cmsCtx.getResponse().getNamespace()
                        + fancyID, onClick, "fancybox_inline fancybox-no-title portlet-menuitem-nuxeo-add", "nuxeo");

                item.setAjaxDisabled(true);
                item.setAssociatedHtml(fancyContent.toString());
                item.setDropdownItem(true);

                menuBar.add(item);

            }
        }

    }


    protected void addDeleteLinkItem(List<MenubarItem> menuBar, String onClick, String url, String fancyContent) throws Exception {

        MenubarItem delete = new MenubarItem("DELETE", "Supprimer", MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 20, url, null,
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
        if( isRemoteProxy(cmsCtx, pubInfos))
            return;
        

        if (pubInfos.isDeletableByUser() && !isRemoteProxy(cmsCtx, pubInfos)) {
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


    protected void addPermaLinkItem(List<MenubarItem> menuBar, String url) throws Exception {
        MenubarItem item = new MenubarItem("PERMLINK", "Permalink", MenubarItem.ORDER_PORTLET_SPECIFIC_CMS, url, null, "portlet-menuitem-permalink", null);

        item.setAjaxDisabled(true);
        menuBar.add(item);

    }

    protected void getPermaLinkLink(CMSServiceCtx cmsCtx, List<MenubarItem> menuBar) throws Exception {

        if (!WindowState.MAXIMIZED.equals(cmsCtx.getRequest().getWindowState())) {
            return;
        }


        String permaLinkURL = this.getPortalUrlFactory().getPermaLink(
                new PortalControllerContext(cmsCtx.getPortletCtx(), cmsCtx.getRequest(), cmsCtx.getResponse()), null, null,
                ((Document) (cmsCtx.getDoc())).getPath(), IPortalUrlFactory.PERM_LINK_TYPE_CMS);

        if (permaLinkURL != null) {
            this.addPermaLinkItem(menuBar, permaLinkURL);
        }

    }

    /** Identifiant MenuBar */
    public static final String MENU_BAR = "osivia.menuBar";

    /**
     * Méthode utilitaire permettant de récupérer un item par son "type".
     *
     * @param type (permalien, ...)
     * @param request requête permettant d'accéder à la MenuBar
     * @return l'item de "type" donné
     */
    public static MenubarItem getItemByType(String type, PortletRequest request) {
        MenubarItem searchItem = null;
        List<MenubarItem> menuBar = (List<MenubarItem>) request.getAttribute(MENU_BAR);
        boolean itemFound = false;
        Iterator<MenubarItem> items = menuBar.iterator();
        while (items.hasNext() && !itemFound) {
            MenubarItem item = items.next();
            if (type.equalsIgnoreCase(item.getId())) {
                itemFound = true;
                searchItem = item;
            }
        }
        return searchItem;
    }


}
