package fr.toutatice.portail.cms.nuxeo.api.domain;

import javax.portlet.PortletContext;

import org.osivia.portal.api.locator.Locator;

import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCustomizer;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoService;


public class PluginModule {
    


    private ClassLoader cl;
    private PortletContext portletContext;
    

    
    public ClassLoader getCl() {
        return cl;
    }


    
    public PortletContext getPortletContext() {
        return portletContext;
    }




    public PluginModule(PortletContext portletContext) {
        super();
        this.cl = Thread.currentThread().getContextClassLoader();
        this.portletContext = portletContext;
    }
    
    
    protected INuxeoCustomizer getNuxeoCustomizer() {
        // Nuxeo service
        INuxeoService nuxeoService = Locator.findMBean(INuxeoService.class, INuxeoService.MBEAN_NAME);
        INuxeoCustomizer cmsCustomizer = nuxeoService.getCMSCustomizer();
        return cmsCustomizer;
    }
    
    
    
    
     

}
