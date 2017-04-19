package fr.toutatice.portail.cms.test.plugin;

import org.osivia.portal.api.customization.CustomizationContext;

import fr.toutatice.portail.cms.nuxeo.api.domain.AbstractPluginPortlet;


/**
 * The Class JSPPlugin.
 * 
 * Illustrate the overriding of a JSP definied in a plugin
 */

public class JSPPlugin  extends AbstractPluginPortlet{

    /** Customizer name. */
    private static final String PLUGIN_NAME = "test.plugin";

    
    @Override
    protected String getPluginName() {
        return PLUGIN_NAME;
    }

    @Override
    protected void customizeCMSProperties(CustomizationContext context) {
        
    }
    @Override
    public int getOrder() {
       return DEFAULT_DEPLOYMENT_ORDER + 1;
    }

}
