package fr.toutatice.portail.cms.test.common.model;

/**
 * Test portlet configuration java-bean.
 *
 * @author Cédric Krommenhoek
 */
public class Configuration {

    /** Default tab. */
    private Tab defaultTab;
    /** Document path. */
    private String path;
    /** User name. */
    private String user;
    /** Selection identifier. */
    private String selectionId;


    /**
     * Constructor.
     */
    public Configuration() {
        super();
    }


    /**
     * Getter for defaultTab.
     *
     * @return the defaultTab
     */
    public Tab getDefaultTab() {
        return this.defaultTab;
    }

    /**
     * Setter for defaultTab.
     *
     * @param defaultTab the defaultTab to set
     */
    public void setDefaultTab(Tab defaultTab) {
        this.defaultTab = defaultTab;
    }

    /**
     * Getter for path.
     *
     * @return the path
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Setter for path.
     *
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Getter for user.
     *
     * @return the user
     */
    public String getUser() {
        return this.user;
    }

    /**
     * Setter for user.
     *
     * @param user the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * Getter for selectionId.
     * 
     * @return the selectionId
     */
    public String getSelectionId() {
        return this.selectionId;
    }

    /**
     * Setter for selectionId.
     * 
     * @param selectionId the selectionId to set
     */
    public void setSelectionId(String selectionId) {
        this.selectionId = selectionId;
    }

}
