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

import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.urls.Link;
import org.osivia.portal.core.cms.CMSItem;

/**
 * Navigation display item java-bean.
 */
public class NavigationDisplayItem {

    /** Item identifier. */
    private final String id;
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
    /** Navigable item indicator. */
    private final boolean navigable;
    /** CMS navigation item. */
    private final CMSItem navItem;
    /** Children. */
    private final List<NavigationDisplayItem> children;


    /**
     * Constructor.
     *
     * @param document Nuxeo document
     * @param link link
     * @param selected selected item indicator
     * @param current current item indicator
     * @param navigable navigable item indicator
     * @param navItem CMS navigation item
     */
    public NavigationDisplayItem(Document document, Link link, boolean selected, boolean current, boolean navigable, CMSItem navItem) {
        super();
        this.id = document.getId();
        this.title = document.getTitle();
        this.url = link.getUrl();
        this.external = link.isExternal();
        this.selected = selected;
        this.current = current;
        this.navigable = navigable;
        this.navItem = navItem;
        this.children = new ArrayList<NavigationDisplayItem>();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("NavigationDisplayItem [title=");
        builder.append(this.title);
        builder.append(", url=");
        builder.append(this.url);
        builder.append("]");
        return builder.toString();
    }


    /**
     * Getter for id.
     *
     * @return the id
     */
    public String getId() {
        return this.id;
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
     * Getter for navigable.
     *
     * @return the navigable
     */
    public boolean isNavigable() {
        return this.navigable;
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
