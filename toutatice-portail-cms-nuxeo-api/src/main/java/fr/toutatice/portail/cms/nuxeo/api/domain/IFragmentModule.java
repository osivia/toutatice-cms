package fr.toutatice.portail.cms.nuxeo.api.domain;

import javax.portlet.PortletException;

import org.osivia.portal.api.context.PortalControllerContext;

/**
 * Fragment module interface.
 *
 * @author Cédric Krommenhoek
 */
public interface IFragmentModule {

    /**
     * Portlet view.
     *
     * @param portalControllerContext portal controller context
     * @throws PortletException
     */
    void doView(PortalControllerContext portalControllerContext) throws PortletException;


    /**
     * Portlet admin.
     *
     * @param portalControllerContext portal controller context
     * @throws PortletException
     */
    void doAdmin(PortalControllerContext portalControllerContext) throws PortletException;


    /**
     * Portlet process admin action.
     *
     * @param portalControllerContext portal controller context
     * @throws PortletException
     */
    void processAdminAction(PortalControllerContext portalControllerContext) throws PortletException;


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
