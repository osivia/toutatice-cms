package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.commons.lang.StringUtils;
import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.PortalObjectPath;
import org.jboss.portal.core.model.portal.Window;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.menubar.MenubarItem;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.cms.CMSServiceCtx;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.services.DocTypeDefinition;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoConnectionProperties;
import fr.toutatice.portail.cms.nuxeo.portlets.bridge.PortletHelper;
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

    CMSService CMSService;
    IPortalUrlFactory urlFactory;
    DefaultCMSCustomizer customizer;
    PortletContext portletCtx;


    public IPortalUrlFactory getPortalUrlFactory() throws Exception {
        if (urlFactory == null)
            urlFactory = (IPortalUrlFactory) portletCtx.getAttribute("UrlService");

        return urlFactory;
    }

    public MenuBarFormater(PortletContext portletCtx, DefaultCMSCustomizer customizer, CMSService cmsService) {
        super();
        CMSService = cmsService;
        this.portletCtx = portletCtx;
        this.customizer = customizer;

    };

    public void formatContentMenuBar(CMSServiceCtx cmsCtx) throws Exception {

        if (cmsCtx.getDoc() == null && cmsCtx.getCreationPath() == null)
            return;

        PortletRequest request = cmsCtx.getRequest();

        List<MenubarItem> menuBar = (List<MenubarItem>) request.getAttribute("osivia.menuBar");

        // Menu bar

        try {
            if (cmsCtx.getDoc() != null)
                getPermaLinkLink(cmsCtx, menuBar);
            if (cmsCtx.getDoc() != null)
                getContextualizationLink(cmsCtx, menuBar);
            if (cmsCtx.getDoc() != null)
                getEditLink(cmsCtx, menuBar);

            getCreateLink(cmsCtx, menuBar);
            if (cmsCtx.getDoc() != null)
                getDeleteLink(cmsCtx, menuBar);


            if (cmsCtx.getDoc() != null)
                getAdministrationLink(cmsCtx, menuBar);
        } catch (CMSException e) {
            if (e.getErrorCode() == CMSException.ERROR_FORBIDDEN || e.getErrorCode() == CMSException.ERROR_NOTFOUND) {
                // On ne fait rien : le document n'existe pas ou je n'ai pas
                // les droits
            } else
                throw e;
        }
    }


    protected void addAdministrationLinkItem(List<MenubarItem> menuBar, String url) throws Exception {

        MenubarItem item = new MenubarItem("MANAGE", "Gérer", MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 3, url, null, "portlet-menuitem-nuxeo-manage", "nuxeo");
        item.setAjaxDisabled(true);
        menuBar.add(item);

    }


    protected void getAdministrationLink(CMSServiceCtx cmsCtx, List<MenubarItem> menuBar) throws Exception {

        if (cmsCtx.getRequest().getRemoteUser() == null)
            return;


        CMSPublicationInfos pubInfos = (CMSPublicationInfos) CMSService.getPublicationInfos(cmsCtx, (((Document) (cmsCtx.getDoc())).getPath()));


        if (pubInfos.isEditableByUser() && pubInfos.isLiveSpace() && PortletHelper.isInContextualizedMode(cmsCtx)) {

            String url = null;

            Document doc = (Document) cmsCtx.getDoc();

            url = customizer.getNuxeoConnectionProps().getPublicBaseUri().toString() + "/nxdoc/default/" + pubInfos.getLiveId() + "/view_documents";

            addAdministrationLinkItem(menuBar, url);

        }
    }


    protected void addEditLinkItem(List<MenubarItem> menuBar, String onClick, String url) throws Exception {

        MenubarItem item = new MenubarItem("EDIT", "Modifier", MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 2, url, onClick, "fancyframe_refresh edition", "nuxeo");
        item.setAjaxDisabled(true);
        menuBar.add(item);

    }


    protected void getEditLink(CMSServiceCtx cmsCtx, List<MenubarItem> menuBar) throws Exception {

        if (cmsCtx.getRequest().getRemoteUser() == null)
            return;


        CMSPublicationInfos pubInfos = (CMSPublicationInfos) CMSService.getPublicationInfos(cmsCtx, (((Document) (cmsCtx.getDoc())).getPath()));

        if (pubInfos.isEditableByUser()) {


            if (pubInfos.isLiveSpace() && PortletHelper.isInContextualizedMode(cmsCtx)) {


                Document doc = (Document) cmsCtx.getDoc();


                DocTypeDefinition docTypeDef = customizer.getDocTypeDefinitions(cmsCtx).get(doc.getType());

                if (docTypeDef != null && docTypeDef.isSupportingPortalForm()) {

                    // Refresh all page

                    String callBackURL = getPortalUrlFactory().getRefreshPageUrl(
                            new PortalControllerContext(cmsCtx.getPortletCtx(), cmsCtx.getRequest(), cmsCtx.getResponse()));

                    String onClick = "setCallbackParams(null, '" + callBackURL + "')";

                    addEditLinkItem(menuBar, onClick, customizer.getNuxeoConnectionProps().getPublicBaseUri().toString() + "/nxpath/default" + doc.getPath()
                            + "@toutatice_edit");


                }


            }
        }
    }


    protected void addCreateLinkItem(List<MenubarItem> menuBar, String onClick, String url) throws Exception {

        // MenubarItem item = new MenubarItem("EDIT", "Ajouter ", MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 1, "#" + response.getNamespace() + fancyID,
        // onClick, "fancybox_inline fancybox-no-title portlet-menuitem-nuxeo-add", "nuxeo");


        MenubarItem item = new MenubarItem("ADD", "Ajouter", MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 2, url, onClick,
                "fancybox_inline fancybox-no-title portlet-menuitem-nuxeo-add", "nuxeo");
        item.setAjaxDisabled(true);
        menuBar.add(item);

    }


    protected void getCreateLink(CMSServiceCtx cmsCtx, List<MenubarItem> menuBar) throws Exception {

        if (cmsCtx.getRequest().getRemoteUser() == null)
            return;


        String creationType = cmsCtx.getCreationType();
        String creationPath = cmsCtx.getCreationPath();

        Document parentDoc = (Document) cmsCtx.getDoc();

        if (parentDoc == null)
            parentDoc = (Document) CMSService.getContent(cmsCtx, creationPath).getNativeItem();

        /* Contextualisation */


        CMSPublicationInfos pubInfos = (CMSPublicationInfos) CMSService.getPublicationInfos(cmsCtx, parentDoc.getPath());


        if (pubInfos.isLiveSpace()) {


            Map<String, String> subTypes = pubInfos.getSubTypes();

            List<SubType> portalDocsToCreate = new ArrayList<SubType>();
            Map<String, DocTypeDefinition> managedTypes = customizer.getDocTypeDefinitions(cmsCtx);


            DocTypeDefinition containerDocType = managedTypes.get(parentDoc.getType());

            if (containerDocType != null) {

                for (String docType : subTypes.keySet()) {

                    // is this type managed at portal level ?

                    if (containerDocType.getPortalFormSubTypes().contains(docType) && (creationType == null || creationType.equals(docType))) {

                        DocTypeDefinition docTypeDef = managedTypes.get(docType);

                        if (docTypeDef != null && docTypeDef.isSupportingPortalForm()) {

                            SubType subType = new SubType();

                            subType.setDocType(docType);
                            subType.setName(subTypes.get(docType));
                            subType.setUrl(customizer.getNuxeoConnectionProps().getPublicBaseUri().toString() + "/nxpath/default" + parentDoc.getPath()
                                    + "@toutatice_create?type=" + docType);
                            portalDocsToCreate.add(subType);
                        }
                    }

                }
            }


            // Refresh all page

            String callBackURL = getPortalUrlFactory().getRefreshPageUrl(
                    new PortalControllerContext(cmsCtx.getPortletCtx(), cmsCtx.getRequest(), cmsCtx.getResponse()));


            String onClick = "setCallbackParams(null, '" + callBackURL + "')";


            if (portalDocsToCreate.size() == 1) {
                // Pas de fancybox

                String url = NuxeoConnectionProperties.getPublicBaseUri().toString() + "/nxpath/default" + parentDoc.getPath() + "@toutatice_create?type="
                        + portalDocsToCreate.get(0).getDocType();

                MenubarItem add = new MenubarItem("CREATE", "Ajouter", MenubarItem.ORDER_PORTLET_SPECIFIC_CMS, url, onClick,
                        "fancyframe_refresh portlet-menuitem-nuxeo-add", "nuxeo");
                menuBar.add(add);

                return;

            }


            /* Lien de création */

            String fancyID = null;
            if (portalDocsToCreate.size() > 0)
                fancyID = "_PORTAL_CREATE";

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

                fancyContent.append("           </div>");
                fancyContent.append("        </div>");
                fancyContent.append("   </div>");
                fancyContent.append("</div>");


                MenubarItem item = new MenubarItem("EDIT", "Ajouter ", MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 1, "#" + cmsCtx.getResponse().getNamespace()
                        + fancyID, onClick, "fancybox_inline fancybox-no-title portlet-menuitem-nuxeo-add", "nuxeo");

                item.setAjaxDisabled(true);
                item.setAssociatedHtml(fancyContent.toString());

                menuBar.add(item);

            }
        }

    }


    protected void addDeleteLinkItem(List<MenubarItem> menuBar, String onClick, String url, String fancyContent) throws Exception {

        MenubarItem delete = new MenubarItem("DELETE", "Supprimer", MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 4, url, null,
                "fancybox_inline portlet-menuitem-nuxeo-delete", null);
        delete.setAjaxDisabled(true);
        delete.setAssociatedHtml(fancyContent);
        menuBar.add(delete);

    }


    protected void getDeleteLink(CMSServiceCtx cmsCtx, List<MenubarItem> menuBar) throws Exception {

        if (cmsCtx.getRequest().getRemoteUser() == null)
            return;


        Document doc = (Document) cmsCtx.getDoc();


        /* Contextualisation */


        CMSPublicationInfos pubInfos = (CMSPublicationInfos) CMSService.getPublicationInfos(cmsCtx, doc.getPath());


        if (pubInfos.isLiveSpace() && pubInfos.isDeletableByUser()) {


            DocTypeDefinition docTypeDef = customizer.getDocTypeDefinitions(cmsCtx).get(doc.getType());

            if (docTypeDef != null && docTypeDef.isSupportingPortalForm()) {


                /* Lien de création */

                String fancyID = "_PORTAL_DELETE";


                // fancybox div

                StringBuffer fancyContent = new StringBuffer();


                fancyContent.append(" <div id=\"" + cmsCtx.getResponse().getNamespace() + fancyID + "\" class=\"fancybox-content\">");

                String putInTrashUrl = getPortalUrlFactory().getPutDocumentInTrashUrl(
                        new PortalControllerContext(cmsCtx.getPortletCtx(), cmsCtx.getRequest(), cmsCtx.getResponse()), doc.getId(), doc.getPath());


                fancyContent.append("   <form method=\"post\" action=\"" + putInTrashUrl + "\">");
                fancyContent.append("       <div>Confirmez-vous la suppression de l'élément ?</div><br/>");
                fancyContent.append("       <input id=\"currentDocId\" type=\"hidden\" name=\"docId\" value=\"" + doc.getId() + "\"/>");
                fancyContent.append("       <input id=\"currentDocPath\" type=\"hidden\" name=\"docPath\" value=\"" + doc.getPath() + "\"/>");
                fancyContent.append("       <input type=\"submit\" name=\"deleteDoc\"  value=\"Confirmer\">");
                fancyContent.append("       <input type=\"reset\" name=\"noDeleteDoc\"  value=\"Annuler\" onclick=\"closeFancyBox();\">");
                fancyContent.append("   </form>");
                fancyContent.append(" </div>");


                addDeleteLinkItem(menuBar, null, "#" + cmsCtx.getResponse().getNamespace() + fancyID, fancyContent.toString());

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

        if (!WindowState.MAXIMIZED.equals(cmsCtx.getRequest().getWindowState()))
            return;

        PortalControllerContext portalCtx = new PortalControllerContext(cmsCtx.getPortletCtx(), cmsCtx.getRequest(), cmsCtx.getResponse());

        Page currentPage = null;

        Window window = (Window) cmsCtx.getRequest().getAttribute("osivia.window");
        if (window != null)
            currentPage = window.getPage();

        // On regarde dans quelle page le contenu ext contextualisé

        Page page = getPortalUrlFactory().getPortalCMSContextualizedPage(portalCtx, (((Document) (cmsCtx.getDoc())).getPath()));

        // Si la page correspond à la page courant on affiche pas le lien
        if (page == null || !page.getId().equals(currentPage.getId())) {

            // On détermine le nom de l'espace

            String spaceDisplayName = null;

            if (page != null) {
                // Soit le nom de la page
                Locale locale = Locale.FRENCH;
                spaceDisplayName = page.getDisplayName().getString(locale, true);
                if (spaceDisplayName == null)
                    spaceDisplayName = page.getName();

            } else {
                // Soit le nom de l'espace de publication
                CMSPublicationInfos pubInfos = (CMSPublicationInfos) CMSService.getPublicationInfos(cmsCtx, ((Document) (cmsCtx.getDoc())).getPath());

                if (pubInfos.getPublishSpacePath() != null) {
                    CMSItem pubConfig = CMSService.getSpaceConfig(cmsCtx, pubInfos.getPublishSpacePath());
                    if ("1".equals(pubConfig.getProperties().get("contextualizeInternalContents")))
                        spaceDisplayName = pubInfos.getPublishSpaceDisplayName();

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


                String url = getPortalUrlFactory().getCMSUrl(new PortalControllerContext(cmsCtx.getPortletCtx(), cmsCtx.getRequest(), cmsCtx.getResponse()),
                        currentPage.getId().toString(PortalObjectPath.CANONICAL_FORMAT), (((Document) (cmsCtx.getDoc())).getPath()), null,
                        IPortalUrlFactory.CONTEXTUALIZATION_PORTAL, null, null, null, null, null);

                addContextualizationLinkItem(menuBar, spaceDisplayName, url);

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

        if (!WindowState.MAXIMIZED.equals(cmsCtx.getRequest().getWindowState()))
            return;


        String permaLinkURL = getPortalUrlFactory().getPermaLink(
                new PortalControllerContext(cmsCtx.getPortletCtx(), cmsCtx.getRequest(), cmsCtx.getResponse()), null, null,
                ((Document) (cmsCtx.getDoc())).getPath(), IPortalUrlFactory.PERM_LINK_TYPE_CMS);

        if (permaLinkURL != null) {
            addPermaLinkItem(menuBar, permaLinkURL);
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
