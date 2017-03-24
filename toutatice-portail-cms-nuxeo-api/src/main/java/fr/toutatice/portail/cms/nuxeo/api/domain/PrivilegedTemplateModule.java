package fr.toutatice.portail.cms.nuxeo.api.domain;

import org.osivia.portal.api.cache.services.CacheInfo;

import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandContext;


/**
 * Privileged portlet module default implementation.
 *
 * @author Cédric Krommenhoek
 * @see PortletModule
 * @see IPrivilegedModule
 */
public abstract class PrivilegedTemplateModule implements ITemplateModule, IPrivilegedModule {

    /** No results request filter. */
    protected static final String FILTER_NO_RESULTS = "1 = 0";

    @Override
    public int getAuthType() {
        return NuxeoCommandContext.AUTH_TYPE_USER;
    }

    @Override
    public int getCacheType() {
        return CacheInfo.CACHE_SCOPE_NONE;
    }

}
