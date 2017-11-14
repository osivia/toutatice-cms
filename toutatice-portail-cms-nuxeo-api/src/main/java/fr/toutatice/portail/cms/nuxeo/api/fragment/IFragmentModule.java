package fr.toutatice.portail.cms.nuxeo.api.fragment;

import fr.toutatice.portail.cms.nuxeo.api.portlet.IPortletModule;

/**
 * Fragment module interface.
 *
 * @author Cédric Krommenhoek
 */
public interface IFragmentModule extends IPortletModule {

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
