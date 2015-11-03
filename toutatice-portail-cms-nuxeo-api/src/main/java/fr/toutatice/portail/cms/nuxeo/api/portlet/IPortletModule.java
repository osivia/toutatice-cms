package fr.toutatice.portail.cms.nuxeo.api.portlet;

import java.io.IOException;

import javax.portlet.PortletException;

import org.osivia.portal.api.context.PortalControllerContext;

/**
 * Portlet module plugin.
 *
 * @author Cédric Krommenhoek
 */
public interface IPortletModule {

    /**
     * Render view.
     *
     * @param portalControllerContext portal controller context
     * @throws PortletException
     * @throws IOException
     */
    void doView(PortalControllerContext portalControllerContext) throws PortletException, IOException;


    /**
     * Process action.
     *
     * @param portalControllerContext portal controller context
     * @throws PortletException
     * @throws IOException
     */
    void processAction(PortalControllerContext portalControllerContext) throws PortletException, IOException;

}
