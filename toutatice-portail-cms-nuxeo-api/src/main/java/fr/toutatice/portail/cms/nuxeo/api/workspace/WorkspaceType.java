package fr.toutatice.portail.cms.nuxeo.api.workspace;

import org.apache.commons.lang.StringUtils;

/**
 * Workspace types enumeration.
 * 
 * @author Cédric Krommenhoek
 */
public enum WorkspaceType {

    /** Public workspace. */
    PUBLIC("glyphicons glyphicons-unlock", "success"),
    /** Private workspace. */
    PRIVATE("glyphicons glyphicons-lock", "warning"),
    /** Invitation only workspace. */
    INVITATION("glyphicons glyphicons-shield", "danger");

    
    /** Identifier. */
    private final String id;
    /** Internationalization key. */
    private final String key;
    /** Icon. */
    private final String icon;
    /** Color. */
    private final String color;
    

    /**
     * Constructor.
     * 
     * @param icon icon
     * @param color color
     */
    private WorkspaceType(String icon, String color) {
        this.id = this.name();
        this.key = "WORKSPACE_TYPE_" + StringUtils.upperCase(this.name());
        this.icon = icon;
        this.color = color;
    }


    /**
     * Getter for id.
     * 
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Getter for key.
     * 
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Getter for icon.
     * 
     * @return the icon
     */
    public String getIcon() {
        return icon;
    }

    /**
     * Getter for color.
     * 
     * @return the color
     */
    public String getColor() {
        return color;
    }

}
