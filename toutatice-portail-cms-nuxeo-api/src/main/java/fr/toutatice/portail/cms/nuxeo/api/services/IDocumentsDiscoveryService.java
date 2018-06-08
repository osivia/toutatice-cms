package fr.toutatice.portail.cms.nuxeo.api.services;

import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.Satellite;

/**
 * Documents discovery service interface.
 * 
 * @author ckrommenhoek
 */
public interface IDocumentsDiscoveryService {

    /**
     * Discover document location.
     * 
     * @param cmsContext CMS context
     * @param path document path
     * @return satellites
     * @throws CMSException
     */
    Satellite discoverLocation(CMSServiceCtx cmsContext, String path) throws CMSException;

}
