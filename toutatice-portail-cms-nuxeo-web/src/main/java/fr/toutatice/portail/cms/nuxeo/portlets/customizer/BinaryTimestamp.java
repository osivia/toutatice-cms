package fr.toutatice.portail.cms.nuxeo.portlets.customizer;

/**
 * Binary timestamp java-bean.
 * 
 * @author Cédric Krommenhoek
 */
public class BinaryTimestamp {

    /** Portal timestamp. */
    private long timestamp;
    /** Nuxeo document "dc:modified" timestamp. */
    private Long modified;
    /** Reloading required indicator. */
    private boolean reloadingRequired;


    /**
     * Constructor.
     */
    public BinaryTimestamp() {
        super();
    }


    /**
     * Getter for timestamp.
     * 
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Setter for timestamp.
     * 
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Getter for modified.
     * 
     * @return the modified
     */
    public Long getModified() {
        return modified;
    }

    /**
     * Setter for modified.
     * 
     * @param modified the modified to set
     */
    public void setModified(Long modified) {
        this.modified = modified;
    }

    /**
     * Getter for reloadingRequired.
     * 
     * @return the reloadingRequired
     */
    public boolean isReloadingRequired() {
        return reloadingRequired;
    }

    /**
     * Setter for reloadingRequired.
     * 
     * @param reloadingRequired the reloadingRequired to set
     */
    public void setReloadingRequired(boolean reloadingRequired) {
        this.reloadingRequired = reloadingRequired;
    }

}
