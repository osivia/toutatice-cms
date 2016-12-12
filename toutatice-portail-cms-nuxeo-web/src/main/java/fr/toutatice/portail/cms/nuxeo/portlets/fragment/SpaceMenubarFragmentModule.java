/**
 *
 */
package fr.toutatice.portail.cms.nuxeo.portlets.fragment;

import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.context.PortalControllerContext;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.cms.NuxeoDocumentContext;
import fr.toutatice.portail.cms.nuxeo.api.fragment.FragmentModule;

/**
 * Fragment permettant l'affichage d'une Menubar pour un espace (page).
 * --- appliqué pour le moment aux workspaces ---
 *
 * @author David Chevrier
 * @see FragmentModule
 */
public class SpaceMenubarFragmentModule extends FragmentModule {

    /** Space menubar fragment identifier. */
    public static final String ID = "space_menubar";


    /**
     * Constructor.
     *
     * @param portletContext portlet context
     */
    public SpaceMenubarFragmentModule(PortletContext portletContext) {
        super(portletContext);
    }


    /**
     * {@inheritDoc}
     */
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
            // Nuxeo document
            NuxeoDocumentContext documentContext = nuxeoController.getDocumentContext(navigationPath);
            Document document = documentContext.getDoc();
            nuxeoController.setCurrentDoc(document);

            request.setAttribute("osivia.cms.menuBar.forceContextualization", true);
            nuxeoController.insertContentMenuBarItems();

            request.setAttribute("osivia.emptyResponse", "1");
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
    public void processAction(PortalControllerContext portalControllerContext) throws PortletException {
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
