package fr.toutatice.portail.cms.nuxeo.service.editablewindow;

import java.io.Serializable;

/**
 * Zoom.
 * 
 * @author lbi
 * 
 */
public class Zoom implements Serializable, Comparable<Zoom> {

    /** title const. */
    public static final String TITLE = "title";

    /** href const. */
    public static final String HREF = "href";

    /** vignette const. */
    public static final String VIGNETTE = "vignette";

    /** desc const. */
    public static final String DESCRIPTION = "desc";

    /** order const. */
    public static final String ORDER = "order";

    /**
     * 
     */
    private static final long serialVersionUID = 3077573162027726428L;

    /** title. */
    private String title;
    
    /** href. */
    private String href;
    
    /** imgSrc. */
    private String imgSrc;
    
    /** description. */
    private String description = "";

    /** order. */
    private Integer order;

    /**
     * compareTo.
     * 
     * @param o the other zoom
     * @return order.compareTo
     */
    public int compareTo(Zoom o) {
        return this.getOrder().compareTo(o.getOrder());
    }


    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }


    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }


    /**
     * @return the href
     */
    public String getHref() {
        return href;
    }


    /**
     * @param href the href to set
     */
    public void setHref(String href) {
        this.href = href;
    }


    /**
     * @return the imgSrc
     */
    public String getImgSrc() {
        return imgSrc;
    }


    /**
     * @param imgSrc the imgSrc to set
     */
    public void setImgSrc(String imgSrc) {
        this.imgSrc = imgSrc;
    }


    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }


    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * @return the order
     */
    public Integer getOrder() {
        return order;
    }


    /**
     * @param order the order to set
     */
    public void setOrder(Integer order) {
        this.order = order;
    }

    
}
