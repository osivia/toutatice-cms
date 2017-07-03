package fr.toutatice.portail.cms.nuxeo.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * File browser views enumeration.
 *
 * @author Cédric Krommenhoek
 */
public enum FileBrowserView {

    /** Lines view. */
    LINES("lines", "glyphicons glyphicons-show-thumbnails-with-lines", true, false, true),
    /** Thumbnails view. */
    THUMBNAILS("thumbnails", "glyphicons glyphicons-show-thumbnails", false, true, true, "thumbnails-reorganization"),
    /** Thumbnails reorganization view. */
    THUMBNAILS_REORGANIZATION("thumbnails-reorganization", null, true, false, false, "thumbnails");


    /** Default view. */
    public static final FileBrowserView DEFAULT = LINES;


    /** View name. */
    private final String name;
    /** View icon. */
    private final String icon;
    /** Orderable view indicator. */
    private final boolean orderable;
    /** Closed navigation panel indicator. */
    private final boolean closedNavigation;
    /** Menubar item indicator. */
    private final boolean menubarItem;
    /** Linked view names. */
    private final List<String> linkedViewNames;


    /**
     * Constructor.
     *
     * @param name view name
     * @param icon view icon
     * @param orderable orderable view indicator
     * @param closedNavigation closed navigation panel indicator
     * @param menubarItem menubar item indicator
     */
    private FileBrowserView(String name, String icon, boolean orderable, boolean closedNavigation, boolean menubarItem, String... linkedViewNames) {
        this.name = name;
        this.icon = icon;
        this.orderable = orderable;
        this.closedNavigation = closedNavigation;
        this.menubarItem = menubarItem;
        this.linkedViewNames = new ArrayList<>(Arrays.asList(linkedViewNames));
    }


    /**
     * Get view from his name.
     *
     * @param name view name
     * @return view
     */
    public static final FileBrowserView fromName(String name) {
        FileBrowserView result = DEFAULT;
        if (name != null) {
            for (FileBrowserView view : FileBrowserView.values()) {
                if (name.equals(view.name)) {
                    result = view;
                    break;
                }
            }
        }
        return result;
    }


    /**
     * Getter for name.
     *
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Getter for icon.
     *
     * @return the icon
     */
    public String getIcon() {
        return this.icon;
    }

    /**
     * Getter for orderable.
     *
     * @return the orderable
     */
    public boolean isOrderable() {
        return this.orderable;
    }

    /**
     * Getter for closedNavigation.
     * 
     * @return the closedNavigation
     */
    public boolean isClosedNavigation() {
        return this.closedNavigation;
    }

    /**
     * Getter for menubarItem.
     * 
     * @return the menubarItem
     */
    public boolean isMenubarItem() {
        return menubarItem;
    }

    /**
     * Getter for linkedViewNames.
     * 
     * @return the linkedViewNames
     */
    public List<String> getLinkedViewNames() {
        return linkedViewNames;
    }

}
