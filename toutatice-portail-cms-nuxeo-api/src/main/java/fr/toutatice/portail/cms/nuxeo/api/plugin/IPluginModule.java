package fr.toutatice.portail.cms.nuxeo.api.plugin;

import javax.portlet.PortletContext;

/**
 * Plugin module interface.
 *
 * @author Cédric Krommenhoek
 */
public interface IPluginModule {

    /**
     * Get class loader.
     *
     * @return class loader
     */
    ClassLoader getClassLoader();


    /**
     * Get portlet context.
     *
     * @return portlet context
     */
    PortletContext getPortletContext();

}
