package fr.toutatice.portail.cms.nuxeo.api.workspace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    INVITATION("glyphicons glyphicons-shield", "danger"),
    /** Unchangeable workspace. */
    UNCHANGEABLE("glyphicons glyphicons-government", "default", true);

    
    /** Identifier. */
    private final String id;
    /** Internationalization key. */
    private final String key;
    /** Icon. */
    private final String icon;
    /** Color. */
    private final String color;
    /** Portal administrator restriction indicator. */
    private final boolean portalAdministratorRestriction;


    /**
     * Constructor.
     * 
     * @param icon icon
     * @param color color
     */
    private WorkspaceType(String icon, String color) {
        this(icon, color, false);
    }

    /**
     * Constructor.
     * 
     * @param icon icon
     * @param color color
     * @param portalAdministratorRestriction portal administrator restriction indicator
     */
    private WorkspaceType(String icon, String color, boolean portalAdministratorRestriction) {
        this.id = this.name();
        this.key = "WORKSPACE_TYPE_" + StringUtils.upperCase(this.name());
        this.icon = icon;
        this.color = color;
        this.portalAdministratorRestriction = portalAdministratorRestriction;
    }


    /**
     * Get workspace types values.
     * 
     * @param admin portal administrator indicator
     * @return workspace types
     */
    public static List<WorkspaceType> list(boolean admin) {
        // Values
        WorkspaceType[] values = WorkspaceType.values();
        
        // Results
        List<WorkspaceType> results;
        if (admin) {
            results = new ArrayList<>(Arrays.asList(values));
        } else {
            results = new ArrayList<>(values.length);
            for (WorkspaceType value : values) {
                if (!value.portalAdministratorRestriction) {
                    results.add(value);
                }
            }
        }
        
        return results;
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

    /**
     * Getter for portalAdministratorRestriction.
     * 
     * @return the portalAdministratorRestriction
     */
    public boolean isPortalAdministratorRestriction() {
        return portalAdministratorRestriction;
    }

}
