package fr.toutatice.portail.cms.nuxeo.api.domain;

import java.io.File;

/**
 * Forum thread post java-bean.
 *
 * @author Cédric Krommenhoek
 * @see Comment
 */
public class ThreadPost extends Comment {

    /** Thread title. */
    private String title;
    /** File name. */
    private String filename;
    /** Attachment. */
    private File attachment;


    /**
     * Default constructor.
     */
    public ThreadPost() {
        super();
    }


    /**
     * Getter for title.
     *
     * @return the title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Setter for title.
     *
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Getter for filename.
     *
     * @return the filename
     */
    public String getFilename() {
        return this.filename;
    }

    /**
     * Setter for filename.
     *
     * @param filename the filename to set
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * Getter for attachment.
     * 
     * @return the attachment
     */
    public File getAttachment() {
        return this.attachment;
    }

    /**
     * Setter for attachment.
     * 
     * @param attachment the attachment to set
     */
    public void setAttachment(File attachment) {
        this.attachment = attachment;
    }

}
