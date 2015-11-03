package fr.toutatice.portail.cms.nuxeo.api.domain;

import java.io.IOException;

import javax.portlet.PortletException;

import org.osivia.portal.api.context.PortalControllerContext;

import fr.toutatice.portail.cms.nuxeo.api.portlet.IPortletModule;

/**
 * Fragment module interface.
 *
 * @author Cédric Krommenhoek
 */
public interface IFragmentModule extends IPortletModule {

    /**
     * Render admin.
     *
     * @param portalControllerContext portal controller context
     * @throws PortletException
     * @throws IOException
     */
    void doAdmin(PortalControllerContext portalControllerContext) throws PortletException, IOException;


    /**
     * Get displayed in admin menu indicator.
     *
     * @return true if displayed in admin menu
     */
    boolean isDisplayedInAdmin();


    /**
     * Get view JSP name.
     *
     * @return view JSP name
     */
    String getViewJSPName();


    /**
     * Get admin JSP name.
     *
     * @return admin JSP name
     */
    String getAdminJSPName();

}
