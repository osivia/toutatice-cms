package fr.toutatice.portail.cms.nuxeo.portlets.service;

import java.util.Map;

import org.osivia.portal.core.cms.NavigationItem;

/**
 * Full loaded navigation items.
 * 
 * @author Cédric Krommenhoek
 */
public class FullLoadedNavigationItems {

    /** Base path. */
    private final String basePath;
    /** Navigation items. */
    private final Map<String, NavigationItem> navigationItems;


    /**
     * Constructor.
     * 
     * @param basePath base path
     * @param navigationItems navigation items
     */
    public FullLoadedNavigationItems(String basePath, Map<String, NavigationItem> navigationItems) {
        super();
        this.basePath = basePath;
        this.navigationItems = navigationItems;
    }


    /**
     * Get request attribute name.
     * 
     * @param basePath base path
     * @return request attribute name
     */
    public static String getRequestAttributeName(String basePath) {
        StringBuilder builder = new StringBuilder();
        builder.append(FullLoadedNavigationItems.class.getName());
        builder.append(basePath);
        return builder.toString();
    }


    /**
     * Getter for basePath.
     * 
     * @return the basePath
     */
    public String getBasePath() {
        return basePath;
    }

    /**
     * Getter for navigationItems.
     * 
     * @return the navigationItems
     */
    public Map<String, NavigationItem> getNavigationItems() {
        return navigationItems;
    }

}
