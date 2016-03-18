package fr.toutatice.portail.cms.nuxeo.api.domain;

import java.util.List;

import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSServiceCtx;

/**
 * Navigation adapter module interface.
 *
 * @author Cédric Krommenhoek
 */
public interface INavigationAdapterModule {

    /**
     * Adapt navigation path.
     *
     * @param cmsContext CMS context
     * @return adapted navigation path
     * @throws CMSException
     */
    String adaptNavigationPath(CMSServiceCtx cmsContext) throws CMSException;

    
    /**
     * Get symlinks.
     * 
     * @param cmsContext CMS context
     * @return symlinks
     * @throws CMSException
     */
    List<Symlink> getSymlinks(CMSServiceCtx cmsContext) throws CMSException;
    
}
