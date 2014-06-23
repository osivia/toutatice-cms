package fr.toutatice.portail.cms.nuxeo.portlets.files;

import org.osivia.portal.api.urls.Link;

/**
 * File browser document view-object.
 *
 * @author Cédric Krommenhoek
 */
public class FileBrowserDocumentVO {

    /** Document display title. */
    private String title;
    /** Document link. */
    private Link link;
    /** Document size. */
    private String size;
    /** Document icon source. */
    private String iconSource;
    /** Document icon alt. */
    private String iconAlt;
    /** Document download link. */
    private Link downloadLink;
    /** Document date. */
    private String date;
    /** Document last contributor. */
    private String lastContributor;


    /**
     * Default constructor.
     */
    public FileBrowserDocumentVO() {
        super();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "FileBrowserDocumentVO [title=" + this.title + "]";
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
     * Getter for link.
     *
     * @return the link
     */
    public Link getLink() {
        return this.link;
    }

    /**
     * Setter for link.
     *
     * @param link the link to set
     */
    public void setLink(Link link) {
        this.link = link;
    }

    /**
     * Getter for size.
     *
     * @return the size
     */
    public String getSize() {
        return this.size;
    }

    /**
     * Setter for size.
     *
     * @param size the size to set
     */
    public void setSize(String size) {
        this.size = size;
    }

    /**
     * Getter for iconSource.
     *
     * @return the iconSource
     */
    public String getIconSource() {
        return this.iconSource;
    }

    /**
     * Setter for iconSource.
     *
     * @param iconSource the iconSource to set
     */
    public void setIconSource(String iconSource) {
        this.iconSource = iconSource;
    }

    /**
     * Getter for iconAlt.
     *
     * @return the iconAlt
     */
    public String getIconAlt() {
        return this.iconAlt;
    }

    /**
     * Setter for iconAlt.
     *
     * @param iconAlt the iconAlt to set
     */
    public void setIconAlt(String iconAlt) {
        this.iconAlt = iconAlt;
    }

    /**
     * Getter for downloadLink.
     *
     * @return the downloadLink
     */
    public Link getDownloadLink() {
        return this.downloadLink;
    }

    /**
     * Setter for downloadLink.
     *
     * @param downloadLink the downloadLink to set
     */
    public void setDownloadLink(Link downloadLink) {
        this.downloadLink = downloadLink;
    }

    /**
     * Getter for date.
     *
     * @return the date
     */
    public String getDate() {
        return this.date;
    }

    /**
     * Setter for date.
     *
     * @param date the date to set
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Getter for lastContributor.
     *
     * @return the lastContributor
     */
    public String getLastContributor() {
        return this.lastContributor;
    }

    /**
     * Setter for lastContributor.
     *
     * @param lastContributor the lastContributor to set
     */
    public void setLastContributor(String lastContributor) {
        this.lastContributor = lastContributor;
    }

}
