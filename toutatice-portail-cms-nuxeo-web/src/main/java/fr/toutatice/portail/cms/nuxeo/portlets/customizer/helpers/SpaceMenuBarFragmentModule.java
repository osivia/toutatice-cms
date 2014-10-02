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

        String navigationPath = nuxeoController.getNavigationPath();

        if (navigationPath != null) {
            Document doc = nuxeoController.fetchDocument(navigationPath);

            nuxeoController.setCurrentDoc(doc);
            request.setAttribute("osivia.cms.forcePermalinkDisplay", true);            
            nuxeoController.insertContentMenuBarItems();

            List<MenubarItem> menuBar = (List<MenubarItem>) request.getAttribute(Constants.PORTLET_ATTR_MENU_BAR);

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
