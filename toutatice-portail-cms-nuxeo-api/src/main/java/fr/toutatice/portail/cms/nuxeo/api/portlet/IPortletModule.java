package fr.toutatice.portail.cms.nuxeo.api.portlet;

import java.io.IOException;

import javax.portlet.PortletException;

import org.osivia.portal.api.context.PortalControllerContext;

import fr.toutatice.portail.cms.nuxeo.api.plugin.IPluginModule;

/**
 * Portlet module plugin.
 *
 * @author Cédric Krommenhoek
 * @see IPluginModule
 */
public interface IPortletModule extends IPluginModule {

    /**
     * Render view.
     *
     * @param portalControllerContext portal controller context
     * @throws PortletException
     * @throws IOException
     */
    void doView(PortalControllerContext portalControllerContext) throws PortletException, IOException;


    /**
     * Render admin.
     *
     * @param portalControllerContext portal controller context
     * @throws PortletException
     * @throws IOException
     */
    void doAdmin(PortalControllerContext portalControllerContext) throws PortletException, IOException;


    /**
     * Process action.
     *
     * @param portalControllerContext portal controller context
     * @throws PortletException
     * @throws IOException
     */
    void processAction(PortalControllerContext portalControllerContext) throws PortletException, IOException;


    /**
     * Serve resource.
     *
     * @param portalControllerContext portal controller context
     * @throws PortletException
     * @throws IOException
     */
    void serveResource(PortalControllerContext portalControllerContext) throws PortletException, IOException;

}
