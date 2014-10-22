/**
 *
 */
package fr.toutatice.portail.cms.nuxeo.portlets.fragment;

import java.util.List;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.menubar.MenubarItem;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.IFragmentModule;

/**
 * Fragment permettant l'affichage d'une Menubar pour un espace (page).
 * --- appliqué pour le moment aux workspaces ---
 *
 * @author David Chevrier
 * @see IFragmentModule
 */
public class SpaceMenubarFragmentModule implements IFragmentModule {

    /** Space menubar fragment identifier. */
    public static final String ID = "space_menubar";

    /** Singleton instance. */
    private static IFragmentModule instance;


    /**
     * Private constructor.
     */
    private SpaceMenubarFragmentModule() {
        super();
    }


    /**
     * Get singleton instance.
     *
     * @return singleton instance
     */
    public static IFragmentModule getInstance() {
        if (instance == null) {
            instance = new SpaceMenubarFragmentModule();
        }
        return instance;
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void doView(PortalControllerContext portalControllerContext) throws PortletException {
        // Request
        RenderRequest request = (RenderRequest) portalControllerContext.getRequest();
        // Response
        RenderResponse response = (RenderResponse) portalControllerContext.getResponse();
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(request, response, portalControllerContext.getPortletCtx());

        String navigationPath = nuxeoController.getNavigationPath();

        if (navigationPath != null) {
            Document doc = nuxeoController.fetchDocument(navigationPath);

            nuxeoController.setCurrentDoc(doc);
            request.setAttribute("osivia.cms.forcePermalinkDisplay", true);
            nuxeoController.insertContentMenuBarItems();

            List<MenubarItem> menubar = (List<MenubarItem>) request.getAttribute(Constants.PORTLET_ATTR_MENU_BAR);

            if (menubar.isEmpty()) {
                request.setAttribute("osivia.emptyResponse", "1");
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void doAdmin(PortalControllerContext portalControllerContext) throws PortletException {
        // Do nothing
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void processAdminAction(PortalControllerContext portalControllerContext) throws PortletException {
        // Do nothing
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDisplayedInAdmin() {
        return true;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getViewJSPName() {
        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getAdminJSPName() {
        return null;
    }

}
