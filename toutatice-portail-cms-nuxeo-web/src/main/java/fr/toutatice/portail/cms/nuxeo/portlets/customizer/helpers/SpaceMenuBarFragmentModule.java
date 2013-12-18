/**
 * 
 */
package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.menubar.MenubarItem;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.core.cms.CMSServiceCtx;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.IFragmentModule;

/**
 * Fragment permettant l'affichage d'une MenuBar pour un espace (page)
 * --- appliqué pour le moment aux workspaces ---
 * @author dchevrier
 *
 */
public class SpaceMenuBarFragmentModule implements IFragmentModule {
    
    /** Type d'item (donnée dans la requête) */
    public static final String ITEM_TYPE = "itemType";
    /** Type d'item (donnée dans la window) */
    public static final String WINDOW_ITEM_TYPE = "osivia.itemType";
    
    /** Nom de l'item (donnée dans la requête) */
    public static final String ITEM_NAME = "itemName";
    /** Nom de l'item (donnée dans la window) */
    public static final String WINDOW_ITEM_NAME = "osivia.itemName";
    
    /** Position de l'item dans la MenuBar (donnée dans la requête) */
    public static final String ITEM_POS = "itemPos";
    /** Position de l'item dans la MenuBar (donnée dans la window) */
    public static final String WINDOW_ITEM_POS = "osivia.itemPos";
    
    /** Action sur le click de l'item (donnée dans la requête) */
    public static final String ITEM_CLICK = "itemClick";
    /** Action sur le click de l'item (donnée dans la window) */
    public static final String WINDOW_ITEM_CLICK = "osivia.itemClick";
    
    /** Classes css appliquées à l'item (donnée dans la requête) */
    public static final String ITEM_CSS_CLASSES = "cssItemClass";
    /** Classes css appliquées à l'item (donnée dans la window) */
    public static final String WINDOW_ITEM_CSS_CLASSES = "osivia.cssItemClass";
    
    /** Identifiant MenuBar */
    public static final String MENU_BAR = "osivia.menuBar";
    /** Chemin de l'espace */
    public static final String SPACE_PATH = "${basePath}";
    /** Identifiant permalien */
    public static final String PERMLINK_VALUE = "permlinkValue";
    /** Identifiant items */
    public static final String ITEMS = "items";
    /** Status des checkboxes */
    public static final String CHECKED = "checked=\"checked\"";
    /** Identifiant checkboxes */
    public static final String CHECKBOXES = "checkboxes";
    /** Séparateur types item */
    public static final String SEPARATOR = ",";
    
    /** Types d'items possibles */
    public enum ItemTypes {
        EDIT, PERMLINK;
    }

    /* (non-Javadoc)
     * @see fr.toutatice.portail.cms.nuxeo.portlets.customizer.IFragmentModule#injectViewAttributes(fr.toutatice.portail.cms.nuxeo.api.NuxeoController, org.osivia.portal.api.windows.PortalWindow, javax.portlet.PortletRequest, javax.portlet.RenderResponse)
     */
    public void injectViewAttributes(NuxeoController ctrl, PortalWindow window,
            PortletRequest request, RenderResponse response) throws Exception {
        
        String itemsStr = (String) window.getProperty(ITEMS);
        String[] items = itemsStr.split(SEPARATOR);
        if(items != null){
            for (String item : items) {
    
                String itemName = window.getProperty(WINDOW_ITEM_NAME + item);
                String itemPosStr = window.getProperty(WINDOW_ITEM_POS + item);
                int itemPos = 0;
                if(itemPosStr != null){
                    itemPos = Integer.valueOf(itemPosStr).intValue();
                }
                String onClick = window.getProperty(WINDOW_ITEM_CLICK + item);
                String cssClasses = window.getProperty(WINDOW_ITEM_CSS_CLASSES + item);
    
                /** Création de la MenuBar */
                List<MenubarItem> menuBar = (List<MenubarItem>) request.getAttribute(MENU_BAR);
                String spacePath = ctrl.getComputedPath(SPACE_PATH);
    
                if (ItemTypes.EDIT.toString().equalsIgnoreCase(item)) {
    
                    Document space = ctrl.fetchDocument(spacePath);
                    String url = ctrl.getLink(space, "nuxeo-link").getUrl();
                    MenubarItem editItem = new MenubarItem(item, itemName, itemPos, url, onClick, cssClasses, "nuxeo");
                    editItem.setAjaxDisabled(true);
                    menuBar.add(editItem);
    
                } else if (ItemTypes.PERMLINK.toString().equalsIgnoreCase(item)) {
    
                    CMSServiceCtx cmsCtx = ctrl.getCMSCtx();
                    PortletContext portletCtx = cmsCtx.getPortletCtx();
                    IPortalUrlFactory urlFactory = getPortalUrlFactory(portletCtx);
                    if (urlFactory != null) {
                        String permaLinkURL = urlFactory.getPermaLink(new PortalControllerContext(portletCtx, request,
                                response), null, null, spacePath, IPortalUrlFactory.PERM_LINK_TYPE_CMS);
                        MenubarItem permlinkItem = new MenubarItem(item, itemName, itemPos, permaLinkURL, onClick,
                                cssClasses, null);
                        permlinkItem.setAjaxDisabled(true);
                        menuBar.add(permlinkItem);
                        /* Pour avoir accès de façon indépendante au permalien */
                        request.setAttribute(PERMLINK_VALUE, permaLinkURL);
                    } else {
                        throw new Exception("Le service UrlService n'est pas disponible");
                    }
    
                }   
            }
        }
    }

    /* (non-Javadoc)
     * @see fr.toutatice.portail.cms.nuxeo.portlets.customizer.IFragmentModule#injectAdminAttributes(fr.toutatice.portail.cms.nuxeo.api.NuxeoController, org.osivia.portal.api.windows.PortalWindow, javax.portlet.PortletRequest, javax.portlet.RenderResponse)
     */
    public void injectAdminAttributes(NuxeoController ctx, PortalWindow window, PortletRequest request,
            RenderResponse response) throws Exception {
        Map<String, String> itemsChecked = new HashMap<String, String>();
        String itemsStr = (String) window.getProperty(ITEMS);
        if (itemsStr != null) {
            String[] items = itemsStr.split(SEPARATOR);
            if (items != null) {
                for (String item : items) {
                    itemsChecked.put(item, CHECKED);

                    String itemName = window.getProperty(WINDOW_ITEM_NAME + item);
                    if (itemName == null) {
                        itemName = StringUtils.EMPTY;
                    }
                    request.setAttribute(ITEM_NAME + item, itemName);

                    String itemPos = window.getProperty(WINDOW_ITEM_POS + item);
                    if (itemPos == null) {
                        itemPos = StringUtils.EMPTY;
                    }
                    request.setAttribute(ITEM_POS + item, itemPos);

                    String itemClick = window.getProperty(WINDOW_ITEM_CLICK + item);
                    if (itemClick == null) {
                        itemClick = StringUtils.EMPTY;
                    }
                    request.setAttribute(ITEM_CLICK + item, itemClick);

                    String cssItemClass = window.getProperty(WINDOW_ITEM_CSS_CLASSES + item);
                    if (cssItemClass == null) {
                        cssItemClass = StringUtils.EMPTY;
                    }
                    request.setAttribute(ITEM_CSS_CLASSES + item, cssItemClass);
                }
            }
        }
        request.setAttribute(CHECKBOXES, itemsChecked);
    }

    /* (non-Javadoc)
     * @see fr.toutatice.portail.cms.nuxeo.portlets.customizer.IFragmentModule#processAdminAttributes(fr.toutatice.portail.cms.nuxeo.api.NuxeoController, org.osivia.portal.api.windows.PortalWindow, javax.portlet.ActionRequest, javax.portlet.ActionResponse)
     */
    public void processAdminAttributes(NuxeoController ctx,
            PortalWindow window, ActionRequest request, ActionResponse response)
            throws Exception {
        String[] items = request.getParameterValues(ITEMS);
        String savedItems = "";
        if(items != null){
            
            for(String item : items){
                savedItems += item + SEPARATOR;
                
                String itemName = request.getParameter(ITEM_NAME + item);
                if(itemName != null){
                    if(itemName.length() > 0){
                        window.setProperty(WINDOW_ITEM_NAME + item, itemName);
                    } else if(window.getProperty(WINDOW_ITEM_NAME + item) != null){
                        window.setProperty(WINDOW_ITEM_NAME + item, null);
                    }
                }
                
                String itemPos = request.getParameter(ITEM_POS + item);
                if(itemPos != null){
                    if(itemPos.length() > 0){
                        window.setProperty(WINDOW_ITEM_POS + item, itemPos);
                    } else if(window.getProperty(WINDOW_ITEM_POS + item) != null){
                        window.setProperty(WINDOW_ITEM_POS + item, null);
                    }
                }
                
                String itemClick = request.getParameter(ITEM_CLICK + item);
                if(itemClick != null){
                    if(itemClick.length() > 0){
                        window.setProperty(WINDOW_ITEM_CLICK + item, itemClick);
                    } else if(window.getProperty(WINDOW_ITEM_CLICK + item) != null){
                        window.setProperty(WINDOW_ITEM_CLICK + item, null);
                    }
                }
                
                String cssItemClass = request.getParameter(ITEM_CSS_CLASSES + item);
                if(cssItemClass != null){
                    if(cssItemClass.length() > 0){
                        window.setProperty(WINDOW_ITEM_CSS_CLASSES + item, cssItemClass);
                    } else if(window.getProperty(WINDOW_ITEM_CSS_CLASSES + item) != null){
                        window.setProperty(WINDOW_ITEM_CSS_CLASSES + item, null);
                    }
                }
            }
            /* Stockage persistant dans la window */
            window.setProperty(ITEMS, savedItems);
        }
    }
    
    /**
     * Récupération du service d'URL.
     */
    public IPortalUrlFactory getPortalUrlFactory(PortletContext portletCtx) throws Exception {
         return (IPortalUrlFactory) portletCtx.getAttribute("UrlService");

    }

}
