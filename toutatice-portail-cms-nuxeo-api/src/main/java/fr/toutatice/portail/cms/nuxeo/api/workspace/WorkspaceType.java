package fr.toutatice.portail.cms.nuxeo.api.workspace;

import org.apache.commons.lang.StringUtils;

/**
 * Workspace types enumeration.
 * 
 * @author Cédric Krommenhoek
 */
public enum WorkspaceType {

    /** Public workspace. */
    PUBLIC("glyphicons glyphicons-unlock", "success", true),
    /** Invitation only public workspace. */
    PUBLIC_INVITATION(PUBLIC, false),
    /** Private workspace. */
    PRIVATE("glyphicons glyphicons-lock", "warning", true),
    /** Invitation only private workspace. */
    INVITATION(PRIVATE, false);

    
    /** Identifier. */
    private final String id;
    /** Internationalization key. */
    private final String key;
    /** Icon. */
    private final String icon;
    /** Color. */
    private final String color;
    /** Allowed invitation requests indicator. */
    private final boolean allowedInvitationRequests;
    

    /**
     * Constructor.
     * 
     * @param icon icon
     * @param color color
     * @param allowedInvitationRequests allowed invitation requests indicator
     */
    private WorkspaceType(String icon, String color, boolean allowedInvitationRequests) {
        this.id = this.name();
        this.key = "WORKSPACE_TYPE_" + StringUtils.upperCase(this.name());
        this.icon = icon;
        this.color = color;
        this.allowedInvitationRequests = allowedInvitationRequests;
    }

    /**
     * Constructor.
     * 
     * @param primaryType primary workspace type
     * @param allowedInvitationRequests allowed invitation requests indicator
     */
    private WorkspaceType(WorkspaceType primaryType, boolean allowedInvitationRequests) {
        this.id = this.name();
        this.key = primaryType.key;
        this.icon = primaryType.icon;
        this.color = primaryType.color;
        this.allowedInvitationRequests = allowedInvitationRequests;
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
     * Getter for allowedInvitationRequests.
     * 
     * @return the allowedInvitationRequests
     */
    public boolean isAllowedInvitationRequests() {
        return allowedInvitationRequests;
    }

}
