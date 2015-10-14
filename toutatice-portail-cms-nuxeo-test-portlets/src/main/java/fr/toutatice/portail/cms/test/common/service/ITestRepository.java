package fr.toutatice.portail.cms.test.common.service;

import org.osivia.portal.api.context.PortalControllerContext;

import fr.toutatice.portail.cms.test.common.model.Configuration;

/**
 * Test repository interface.
 *
 * @author Cédric Krommenhoek
 */
public interface ITestRepository {

    /**
     * Get configuration.
     *
     * @param portalControllerContext portal controller context
     * @return configuration
     */
    Configuration getConfiguration(PortalControllerContext portalControllerContext);


    /**
     * Set configuration.
     *
     * @param portalControllerContext portal controller context
     * @param configuration
     */
    void setConfiguration(PortalControllerContext portalControllerContext, Configuration configuration);

}
