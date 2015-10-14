package fr.toutatice.portail.cms.test.common.model;

/**
 * Test portlet configuration java-bean.
 *
 * @author Cédric Krommenhoek
 */
public class Configuration {

    /** Document path. */
    private String path;
    /** User name. */
    private String user;


    /**
     * Constructor.
     */
    public Configuration() {
        super();
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

}
