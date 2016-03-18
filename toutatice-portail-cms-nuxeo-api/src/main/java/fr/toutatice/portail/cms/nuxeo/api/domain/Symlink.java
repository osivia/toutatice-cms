package fr.toutatice.portail.cms.nuxeo.api.domain;

/**
 * Symlink java-bean.
 *
 * @author Cédric Krommenhoek
 */
public class Symlink {

    /** Symlink document path. */
    private String path;
    /** Symlink web URL segment. */
    private String segment;
    /** Symlink target path. */
    private String targetPath;
    /** Symlink target webId. */
    private String targetWebId;


    /**
     * Constructor.
     */
    public Symlink() {
        super();
    }


    /**
     * Constructor.
     *
     * @param path symlink document path
     * @param segment symlink web URL segment
     * @param targetPath symlink target path
     * @param targetWebId symlink target webId
     */
    public Symlink(String path, String segment, String targetPath, String targetWebId) {
        this();
        this.path = path;
        this.segment = segment;
        this.targetPath = targetPath;
        this.targetWebId = targetWebId;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Symlink [path=");
        builder.append(this.path);
        builder.append(", segment=");
        builder.append(this.segment);
        builder.append(", targetPath=");
        builder.append(this.targetPath);
        builder.append(", targetWebId=");
        builder.append(this.targetWebId);
        builder.append("]");
        return builder.toString();
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
     * Getter for segment.
     *
     * @return the segment
     */
    public String getSegment() {
        return this.segment;
    }

    /**
     * Setter for segment.
     *
     * @param segment the segment to set
     */
    public void setSegment(String segment) {
        this.segment = segment;
    }

    /**
     * Getter for targetPath.
     *
     * @return the targetPath
     */
    public String getTargetPath() {
        return this.targetPath;
    }

    /**
     * Setter for targetPath.
     *
     * @param targetPath the targetPath to set
     */
    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    /**
     * Getter for targetWebId.
     *
     * @return the targetWebId
     */
    public String getTargetWebId() {
        return this.targetWebId;
    }

    /**
     * Setter for targetWebId.
     *
     * @param targetWebId the targetWebId to set
     */
    public void setTargetWebId(String targetWebId) {
        this.targetWebId = targetWebId;
    }

}
