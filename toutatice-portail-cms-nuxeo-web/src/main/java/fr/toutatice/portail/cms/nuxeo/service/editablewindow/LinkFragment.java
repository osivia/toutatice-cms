package fr.toutatice.portail.cms.nuxeo.service.editablewindow;

import org.osivia.portal.api.urls.Link;

/**
 * Link fragment java-bean
 *
 * @author Cédric Krommenhoek
 * @see Link
 */
public class LinkFragment extends Link {

    /** Title property name. */
    public static final String TITLE_PROPERTY = "title";
    /** HREF property name. */
    public static final String HREF_PROPERTY = "href";
    /** Icon property name. */
    public static final String ICON_PROPERTY = "icon";


    /** Link title. */
    private String title;
    /** Link glyphicon. */
    private String glyphicon;


    /**
     * Constructor.
     *
     * @param link link
     */
    public LinkFragment(Link link) {
        super(link.getUrl(), link.isExternal());
    }

    /**
     * Constructor.
     *
     * @param url link URL
     * @param external external link indicator
     */
    public LinkFragment(String url, boolean external) {
        super(url, external);
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
     * Getter for glyphicon.
     *
     * @return the glyphicon
     */
    public String getGlyphicon() {
        return this.glyphicon;
    }

    /**
     * Setter for glyphicon.
     *
     * @param glyphicon the glyphicon to set
     */
    public void setGlyphicon(String glyphicon) {
        this.glyphicon = glyphicon;
    }

}
