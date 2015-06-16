package fr.toutatice.portail.cms.nuxeo.portlets.files;

/**
 * File browser views enumeration.
 *
 * @author Cédric Krommenhoek
 */
public enum FileBrowserView {

    /** Lines view. */
    LINES("lines", "glyphicons glyphicons-show-thumbnails-with-lines", true),
    /** Thumbnails view. */
    THUMBNAILS("thumbnails", "glyphicons glyphicons-show-thumbnails", false);


    /** Default view. */
    public static final FileBrowserView DEFAULT = LINES;


    /** View name. */
    private final String name;
    /** View icon. */
    private final String icon;
    /** Orderable view indicator. */
    private final boolean orderable;


    /**
     * Constructor.
     *
     * @param name view name
     * @param icon view icon
     * @param orderable orderable view indicator
     */
    private FileBrowserView(String name, String icon, boolean orderable) {
        this.name = name;
        this.icon = icon;
        this.orderable = orderable;
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

}
