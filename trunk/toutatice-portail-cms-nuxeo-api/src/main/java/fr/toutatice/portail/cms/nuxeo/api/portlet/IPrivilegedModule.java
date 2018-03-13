package fr.toutatice.portail.cms.nuxeo.api.portlet;

import org.osivia.portal.api.cache.services.CacheInfo;
import org.osivia.portal.api.context.PortalControllerContext;

import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandContext;

/**
 * Privileged module interface.
 * 
 * @author Cédric Krommenhoek
 */
public interface IPrivilegedModule {

    /**
     * Get auth type.
     * 
     * @see NuxeoCommandContext
     * @return auth type
     */
    int getAuthType();


    /**
     * Get cache type.
     * 
     * @see CacheInfo
     * @return cache type
     */
    int getCacheType();


    /**
     * Get request filter.
     * 
     * @param portalControllerContext portal controller context
     * @return filter
     */
    String getFilter(PortalControllerContext portalControllerContext);

}
