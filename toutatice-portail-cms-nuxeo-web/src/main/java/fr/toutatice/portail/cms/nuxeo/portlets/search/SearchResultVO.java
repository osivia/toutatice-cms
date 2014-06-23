package fr.toutatice.portail.cms.nuxeo.portlets.search;

import org.osivia.portal.api.urls.Link;

/**
 * Search result view object.
 *
 * @author Cédric Krommenhoek
 */
public class SearchResultVO {

    /** Result title. */
    private String title;
    /** Result icon path. */
    private String icon;
    /** Result link. */
    private Link link;
    /** Result description. */
    private String description;


    /**
     * Default constructor.
     */
    public SearchResultVO() {
        super();
    }

    /**
     * Constructor using fields.
     * 
     * @param title result title
     * @param icon result icon path, may be null
     * @param link result link
     * @param description result description, may be null
     */
    public SearchResultVO(String title, String icon, Link link, String description) {
        super();
        this.title = title;
        this.icon = icon;
        this.link = link;
        this.description = description;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "SearchResultBean [title=" + this.title + "]";
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
     * Getter for icon.
     *
     * @return the icon
     */
    public String getIcon() {
        return this.icon;
    }

    /**
     * Setter for icon.
     *
     * @param icon the icon to set
     */
    public void setIcon(String icon) {
        this.icon = icon;
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
     * Getter for description.
     *
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Setter for description.
     *
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

}
