/*
 * (C) Copyright 2014 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 *
 *    
 */
package fr.toutatice.portail.cms.nuxeo.service.editablewindow;

import java.io.Serializable;

/**
 * Zoom.
 * 
 * @author lbi
 * 
 */
public class Link implements Serializable, Comparable<Link> {

    /** title const. */
    public static final String TITLE = "title";

    /** href const. */
    public static final String HREF = "href";

    /** vignette const. */
    public static final String ICON = "icon";

//    /** desc const. */
//    public static final String DESCRIPTION = "desc";

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
    
    /** icon. */
    private String icon;
    
//    /** description. */
//    private String description = "";

    /** order. */
    private Integer order;

    /**
     * compareTo.
     * 
     * @param o the other zoom
     * @return order.compareTo
     */
    public int compareTo(Link o) {
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





    public String getIcon() {
		return icon;
	}


	public void setIcon(String icon) {
		this.icon = icon;
	}


//	/**
//     * @return the description
//     */
//    public String getDescription() {
//        return description;
//    }


//    /**
//     * @param description the description to set
//     */
//    public void setDescription(String description) {
//        this.description = description;
//    }


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
