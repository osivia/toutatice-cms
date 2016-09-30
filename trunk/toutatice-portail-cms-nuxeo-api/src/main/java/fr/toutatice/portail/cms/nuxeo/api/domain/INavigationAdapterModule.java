package fr.toutatice.portail.cms.nuxeo.api.domain;

import java.util.List;

import org.osivia.portal.api.cms.EcmDocument;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.core.cms.CMSException;

/**
 * Navigation adapter module interface.
 *
 * @author Cédric Krommenhoek
 */
public interface INavigationAdapterModule {

    /**
     * Adapt navigation path.
     *
     * @param portalControllerContext portal controller context
     * @param document document
     * @return adapted navigation path
     * @throws CMSException
     */
    String adaptNavigationPath(PortalControllerContext portalControllerContext, EcmDocument document) throws CMSException;

    
    /**
     * Get symlinks.
     * 
     * @param portalControllerContext portal controller context
     * @return symlinks
     * @throws CMSException
     */
    List<Symlink> getSymlinks(PortalControllerContext portalControllerContext) throws CMSException;
    
}
