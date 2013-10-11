package fr.toutatice.portail.cms.nuxeo.portlets.service;

import java.util.Map;

import org.osivia.portal.api.cache.services.IServiceInvoker;
import org.osivia.portal.core.cms.NavigationItem;

/**
 * Just to mark that publish site is not fetchable for this publication
 * 
 * @author jeanseb
 * 
 */
public class PartialNavigationInvoker implements IServiceInvoker {

    private static final long serialVersionUID = -4271471756834717062L;

    private Map<String, NavigationItem> navItems;

    public PartialNavigationInvoker(Map<String, NavigationItem> navItems) {
        super();
        this.navItems = navItems;

    }

    public Object invoke() throws Exception {
        return navItems;
    }


}
