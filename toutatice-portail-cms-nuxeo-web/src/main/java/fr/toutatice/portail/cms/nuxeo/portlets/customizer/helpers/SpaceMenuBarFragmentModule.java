/**
 *
 */
package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.RenderResponse;

import org.jboss.portal.core.model.portal.Portal;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.internationalization.Bundle;
import org.osivia.portal.api.internationalization.IBundleFactory;
import org.osivia.portal.api.internationalization.IInternationalizationService;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.menubar.MenubarItem;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.core.portalobjects.PortalObjectUtils;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.IFragmentModule;

/**
 * Fragment permettant l'affichage d'une MenuBar pour un espace (page).
 * --- appliqué pour le moment aux workspaces ---
 *
 * @author dchevrier
 * @see IFragmentModule
 */
public class SpaceMenuBarFragmentModule implements IFragmentModule {

    /** Portal URL factory. */
    private final IPortalUrlFactory urlFactory;
    /** Bundle factory. */
    private final IBundleFactory bundleFactory;


    /**
     * Constructor.
     */
    public SpaceMenuBarFragmentModule() {
        super();

        // Portal URL factory
        this.urlFactory = Locator.findMBean(IPortalUrlFactory.class, "osivia:service=UrlFactory");

        // Bundle factory
        IInternationalizationService internationalizationService = Locator.findMBean(IInternationalizationService.class,
                IInternationalizationService.MBEAN_NAME);
        this.bundleFactory = internationalizationService.getBundleFactory(this.getClass().getClassLoader());
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void injectViewAttributes(NuxeoController nuxeoController, PortalWindow window, PortletRequest request, RenderResponse response) throws Exception {
        Bundle bundle = this.bundleFactory.getBundle(request.getLocale());

        String navigationPath = nuxeoController.getNavigationPath();

        if (navigationPath != null) {
            Document doc = nuxeoController.fetchDocument(navigationPath);

            nuxeoController.setCurrentDoc(doc);
            nuxeoController.insertContentMenuBarItems();

            List<MenubarItem> menuBar = (List<MenubarItem>) request.getAttribute(Constants.PORTLET_ATTR_MENU_BAR);

            // Current portal
            Portal portal = PortalObjectUtils.getPortal(nuxeoController.getCMSCtx().getControllerContext());
            // Space site indicator
            boolean spaceSite = PortalObjectUtils.isSpaceSite(portal);

            if (!spaceSite) {
                // Permalink
                String permlinkPath = nuxeoController.getContentWebIdPath();
                String url = this.urlFactory.getPermaLink(new PortalControllerContext(nuxeoController.getPortletCtx(), request, response), null, null,
                        permlinkPath, IPortalUrlFactory.PERM_LINK_TYPE_CMS);
                MenubarItem item = new MenubarItem("PERMALINK", bundle.getString("PERMALINK"), MenubarItem.ORDER_PORTLET_SPECIFIC_CMS, url, null,
                        "portlet-menuitem-permalink", null);
                item.setGlyphicon("halflings link");
                item.setAjaxDisabled(true);
                menuBar.add(item);
            }

            if (menuBar.isEmpty()) {
                request.setAttribute("osivia.emptyResponse", "1");
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void injectAdminAttributes(NuxeoController ctx, PortalWindow window, PortletRequest request, RenderResponse response) throws Exception {
        // Do nothing
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void processAdminAttributes(NuxeoController ctx, PortalWindow window, ActionRequest request, ActionResponse response) throws Exception {
        // Do nothing
    }

}
