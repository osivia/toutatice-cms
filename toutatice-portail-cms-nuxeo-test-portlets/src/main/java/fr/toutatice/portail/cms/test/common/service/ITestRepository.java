package fr.toutatice.portail.cms.test.common.service;

import javax.portlet.PortletException;

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
     * @throws PortletException
     */
    Configuration getConfiguration(PortalControllerContext portalControllerContext) throws PortletException;


    /**
     * Set configuration.
     *
     * @param portalControllerContext portal controller context
     * @param configuration configuration
     * @throws PortletException
     */
    void setConfiguration(PortalControllerContext portalControllerContext, Configuration configuration) throws PortletException;

}
