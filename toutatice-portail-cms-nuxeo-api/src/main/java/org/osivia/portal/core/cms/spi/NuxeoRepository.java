package org.osivia.portal.core.cms.spi;

import org.osivia.portal.api.cms.exception.CMSException;
import org.osivia.portal.api.cms.service.NativeRepository;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

/**
 * Nuxeo specific operations on reprository
 */
public interface NuxeoRepository extends NativeRepository {
    
    /**
     * Gets the internal id.
     *
     * @param path the path
     * @return the internal id
     * @throws CMSException the CMS exception
     */
    public String getInternalId( String path) throws CMSException;

    /**
     * Gets the path.
     *
     * @param internalId the internal id
     * @return the path
     * @throws CMSException the CMS exception
     */
    public String getPath(String internalId) throws CMSException;


}
