package fr.toutatice.portail.cms.nuxeo.api.plugin;

import javax.portlet.PortletContext;

import org.osivia.portal.api.locator.Locator;

import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCustomizer;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoService;

/**
 * Plugin module.
 *
 * @author Jean-Sébastien Steux
 * @see IPluginModule
 */
public abstract class PluginModule implements IPluginModule {

    /** Class loader. */
    private final ClassLoader classLoader;
    /** Portlet context. */
    private final PortletContext portletContext;

    /** Nuxeo service. */
    private final INuxeoService nuxeoService;
    /** Nuxeo customizer. */
    private final INuxeoCustomizer nuxeoCustomizer;


    /**
     * Constructor.
     *
     * @param portletContext portlet context
     */
    public PluginModule(PortletContext portletContext) {
        super();
        this.classLoader = Thread.currentThread().getContextClassLoader();
        this.portletContext = portletContext;

        // Nuxeo service
        this.nuxeoService = Locator.findMBean(INuxeoService.class, INuxeoService.MBEAN_NAME);
        // Nuxeo customizer
        this.nuxeoCustomizer = this.nuxeoService.getCMSCustomizer();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ClassLoader getClassLoader() {
        return this.classLoader;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public PortletContext getPortletContext() {
        return this.portletContext;
    }


    /**
     * Getter for nuxeoService.
     *
     * @return the nuxeoService
     */
    public INuxeoService getNuxeoService() {
        return this.nuxeoService;
    }

    /**
     * Getter for nuxeoCustomizer.
     *
     * @return the nuxeoCustomizer
     */
    public INuxeoCustomizer getNuxeoCustomizer() {
        return this.nuxeoCustomizer;
    }

}
