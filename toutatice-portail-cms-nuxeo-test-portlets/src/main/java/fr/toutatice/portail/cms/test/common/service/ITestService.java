package fr.toutatice.portail.cms.test.common.service;

import javax.portlet.PortletException;

import org.osivia.portal.api.context.PortalControllerContext;

import fr.toutatice.portail.cms.test.common.model.Configuration;

/**
 * Test service interface.
 *
 * @author Cédric Krommenhoek
 */
public interface ITestService {

    /**
     * Inject tags data
     *
     * @param portalControllerContext portal controller context
     * @param configuration configuration
     * @throws PortletException
     */
    void injectTagsData(PortalControllerContext portalControllerContext, Configuration configuration) throws PortletException;


    /**
     * Inject attributes storage data.
     *
     * @param portalControllerContext portal controller context
     * @param configuration configuration
     * @throws PortletException
     */
    void injectAttributesStorageData(PortalControllerContext portalControllerContext, Configuration configuration) throws PortletException;


    /**
     * Add to selection.
     *
     * @param portalControllerContext portal controller context
     * @param content selection content
     * @throws PortletException
     */
    void addToSelection(PortalControllerContext portalControllerContext, String content) throws PortletException;


    /**
     * Add to storage.
     *
     * @param portalControllerContext portal controller context
     * @param name attribute name
     * @param value attribute value
     * @throws PortletException
     */
    void addToStorage(PortalControllerContext portalControllerContext, String name, String value) throws PortletException;


    /**
     * Remove from storage.
     * 
     * @param portalControllerContext portal controller context
     * @param name attribute name
     * @throws PortletException
     */
    void removeFromStorage(PortalControllerContext portalControllerContext, String name) throws PortletException;

}
