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
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSServiceCtx;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.IFragmentModule;

/**
 * Fragment permettant l'affichage d'une MenuBar pour un espace (page)
 * --- appliqué pour le moment aux workspaces ---
 * 
 * @author dchevrier
 * 
 */
public class SpaceMenuBarFragmentModule implements IFragmentModule {


    /*
     * (non-Javadoc)
     * 
     * @see fr.toutatice.portail.cms.nuxeo.portlets.customizer.IFragmentModule#injectViewAttributes(fr.toutatice.portail.cms.nuxeo.api.NuxeoController,
     * org.osivia.portal.api.windows.PortalWindow, javax.portlet.PortletRequest, javax.portlet.RenderResponse)
     */
    public void injectViewAttributes(NuxeoController ctrl, PortalWindow window, PortletRequest request, RenderResponse response) throws Exception {


        String spacePath = window.getPageProperty("osivia.cms.basePath");
        CMSItem publishSpaceConfig = null;
        if (spacePath != null) {
            publishSpaceConfig = ctrl.getCMSService().getSpaceConfig(ctrl.getCMSCtx(), spacePath);
            Document doc = (Document) publishSpaceConfig.getNativeItem();

            ctrl.setCurrentDoc(doc);
            ctrl.insertContentMenuBarItems();


            List<MenubarItem> menuBar = (List<MenubarItem>) request.getAttribute("osivia.menuBar");

            String url = this.getPortalUrlFactory(ctrl.getPortletCtx()).getPermaLink(new PortalControllerContext(ctrl.getPortletCtx(), request, response),
                    null, null, publishSpaceConfig.getPath(), IPortalUrlFactory.PERM_LINK_TYPE_CMS);

            MenubarItem item = new MenubarItem("PERMLINK", "Permalink", MenubarItem.ORDER_PORTLET_SPECIFIC_CMS, url, null, "portlet-menuitem-permalink", null);

            item.setAjaxDisabled(true);
            menuBar.add(item);


        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.toutatice.portail.cms.nuxeo.portlets.customizer.IFragmentModule#injectAdminAttributes(fr.toutatice.portail.cms.nuxeo.api.NuxeoController,
     * org.osivia.portal.api.windows.PortalWindow, javax.portlet.PortletRequest, javax.portlet.RenderResponse)
     */
    public void injectAdminAttributes(NuxeoController ctx, PortalWindow window, PortletRequest request, RenderResponse response) throws Exception {

    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.toutatice.portail.cms.nuxeo.portlets.customizer.IFragmentModule#processAdminAttributes(fr.toutatice.portail.cms.nuxeo.api.NuxeoController,
     * org.osivia.portal.api.windows.PortalWindow, javax.portlet.ActionRequest, javax.portlet.ActionResponse)
     */
    public void processAdminAttributes(NuxeoController ctx, PortalWindow window, ActionRequest request, ActionResponse response) throws Exception {

        // get current doc


    }

    /**
     * Récupération du service d'URL.
     */
    public IPortalUrlFactory getPortalUrlFactory(PortletContext portletCtx) throws Exception {
        return (IPortalUrlFactory) portletCtx.getAttribute("UrlService");

    }

}
