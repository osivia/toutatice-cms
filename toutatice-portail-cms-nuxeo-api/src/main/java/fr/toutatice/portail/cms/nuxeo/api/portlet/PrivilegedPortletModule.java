package fr.toutatice.portail.cms.nuxeo.api.portlet;

import javax.portlet.PortletContext;

import org.osivia.portal.api.cache.services.CacheInfo;
import org.osivia.portal.api.context.PortalControllerContext;

import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandContext;

/**
 * Privileged portlet module default implementation.
 * 
 * @author Cédric Krommenhoek
 * @see PortletModule
 * @see IPrivilegedModule
 */
public abstract class PrivilegedPortletModule extends PortletModule implements IPrivilegedModule {

    /** No results request filter. */
    protected static final String FILTER_NO_RESULTS = "1 = 0";


    /**
     * Constructor.
     * 
     * @param portletContext portlet context
     */
    public PrivilegedPortletModule(PortletContext portletContext) {
        super(portletContext);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int getAuthType() {
        return NuxeoCommandContext.AUTH_TYPE_USER;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int getCacheType() {
        return CacheInfo.CACHE_SCOPE_NONE;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getFilter(PortalControllerContext portalControllerContext) {
        return null;
    }

}
