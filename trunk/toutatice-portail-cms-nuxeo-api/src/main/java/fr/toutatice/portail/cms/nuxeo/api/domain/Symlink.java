package fr.toutatice.portail.cms.nuxeo.api.domain;

import org.apache.commons.lang.StringUtils;

/**
 * Symlink java-bean.
 *
 * @author Cédric Krommenhoek
 */
public class Symlink {

    /** Symlink parent document path. */
    private final String parentPath;
    /** Symlink web URL segment. */
    private final String segment;
    /** Symlink target document path. */
    private final String targetPath;
    /** Symlink target document webId. */
    private final String targetWebId;
    /** Symlink virtual path. */
    private final String virtualPath;


    /**
     * Constructor.
     *
     * @param parentPath symlink parent document path
     * @param segment symlink web URL segment
     * @param targetPath symlink target document path
     * @param targetWebId symlink target document webId
     */
    public Symlink(String parentPath, String segment, String targetPath, String targetWebId) {
        super();
        this.parentPath = StringUtils.removeEnd(parentPath, ".proxy");
        this.segment = segment;
        this.targetPath = targetPath;
        this.targetWebId = targetWebId;
        this.virtualPath = this.parentPath + "/symlink_" + segment;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Symlink [parentPath=");
        builder.append(this.parentPath);
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
     * Getter for parentPath.
     *
     * @return the parentPath
     */
    public String getParentPath() {
        return this.parentPath;
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
     * Getter for targetPath.
     *
     * @return the targetPath
     */
    public String getTargetPath() {
        return this.targetPath;
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
     * Getter for virtualPath.
     *
     * @return the virtualPath
     */
    public String getVirtualPath() {
        return this.virtualPath;
    }

}
