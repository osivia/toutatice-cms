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
 */
package fr.toutatice.portail.cms.nuxeo.portlets.publish;

import java.util.ArrayList;
import java.util.List;

import org.osivia.portal.core.cms.CMSItem;

/**
 * Navigation display item java-bean.
 */
public class NavigationDisplayItem {

    /** The display title of the link. */
    private final String title;
    /** The absolute URL of the link. */
    private final String url;
    /** External link indicator. */
    private final boolean external;
    /** Selected item indicator. */
    private final boolean selected;
    /** Current item indicator. */
    private final boolean current;
    /** CMS item. */
    private final CMSItem navItem;
    /** Children. */
    private final List<NavigationDisplayItem> children;


    /**
     * Default constructor.
     */
	public NavigationDisplayItem() {
        this(null, "/", false, false, false, null);
	}

    /**
     * Canonical constructor.
     *
     * @param title display title of the link
     * @param url absolute URL of the link
     * @param external external link indicator
     * @param selected selected item indicator
     * @param current current item indicator
     * @param navItem CMS item
     */
    public NavigationDisplayItem(String title, String url, boolean external, boolean selected, boolean current, CMSItem navItem) {
		super();
		this.title = title;
		this.url = url;
		this.external = external;
		this.selected = selected;
        this.current = current;
		this.navItem = navItem;
        this.children = new ArrayList<NavigationDisplayItem>();
	}


    /**
     * {@inheritDoc}
     */
    @Override
	public String toString() {
		return super.toString() + " {" + this.title + ": " + this.url + "}";
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
     * Getter for url.
     *
     * @return the url
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Getter for external.
     *
     * @return the external
     */
    public boolean isExternal() {
        return this.external;
    }

    /**
     * Getter for selected.
     *
     * @return the selected
     */
    public boolean isSelected() {
        return this.selected;
    }

    /**
     * Getter for current.
     * 
     * @return the current
     */
    public boolean isCurrent() {
        return this.current;
    }

    /**
     * Getter for navItem.
     * 
     * @return the navItem
     */
    public CMSItem getNavItem() {
        return this.navItem;
    }

    /**
     * Getter for children.
     *
     * @return the children
     */
    public List<NavigationDisplayItem> getChildren() {
        return this.children;
    }

}
