package fr.toutatice.portail.cms.nuxeo.api.fragment;

import javax.portlet.PortletContext;

import fr.toutatice.portail.cms.nuxeo.api.portlet.PortletModule;

/**
 * Fragment module.
 *
 * @author Cédric Krommenhoek
 * @see PortletModule
 * @see IFragmentModule
 */
public abstract class FragmentModule extends PortletModule implements IFragmentModule {

    /**
     * Constructor.
     *
     * @param portletContext portlet context
     */
    public FragmentModule(PortletContext portletContext) {
        super(portletContext);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDisplayedInAdmin() {
        return false;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public abstract String getViewJSPName();


    /**
     * {@inheritDoc}
     */
    @Override
    public String getAdminJSPName() {
        return null;
    }

}
