package fr.toutatice.portail.cms.nuxeo.api.domain;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.windows.PortalWindow;

/**
 * Template module interface.
 *
 * @author Cédric Krommenhoek
 */
public interface ITemplateModule {

    /**
     * Portlet view.
     *
     * @param portalControllerContext portal controller context
     * @param window current window
     * @param request render request
     * @param response render response
     * @throws PortletException
     */
    void doView(PortalControllerContext portalControllerContext, PortalWindow window, RenderRequest request, RenderResponse response) throws PortletException;


    /**
     * Portlet process action.
     *
     * @param portalControllerContext portal controller context
     * @param window current window
     * @param request action request
     * @param response action response
     * @throws PortletException
     */
    void processAction(PortalControllerContext portalControllerContext, PortalWindow window, ActionRequest request, ActionResponse response)
            throws PortletException;

}
