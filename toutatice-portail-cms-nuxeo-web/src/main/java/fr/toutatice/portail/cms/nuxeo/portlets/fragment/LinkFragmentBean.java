package fr.toutatice.portail.cms.nuxeo.portlets.fragment;

import org.osivia.portal.api.urls.Link;

/**
 * Link fragment java-bean
 *
 * @author Cédric Krommenhoek
 * @see Link
 */
public class LinkFragmentBean extends Link {

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
     * @param url link URL
     * @param external external link indicator
     */
    public LinkFragmentBean(String url, boolean external) {
        super(url, external);
    }

    /**
     * Constructor.
     * 
     * @param link link
     */
    public LinkFragmentBean(Link link) {
        this(link.getUrl(), link.isExternal());
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
