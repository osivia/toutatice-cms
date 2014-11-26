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
package fr.toutatice.portail.cms.nuxeo.portlets.publish;

import java.util.ArrayList;
import java.util.List;

import org.osivia.portal.core.cms.CMSItem;


public class NavigationDisplayItem {
	/** The display title of the  link. */
	private String title;
	
	/** The absolute URL of the  link. */
	private String url;
	
	private boolean external;
	
	private boolean selected;
	
	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	private List<NavigationDisplayItem> childrens = new ArrayList<NavigationDisplayItem>();
	
	public List<NavigationDisplayItem> getChildrens() {
		return childrens;
	}

	public void setChildrens(List<NavigationDisplayItem> childrens) {
		this.childrens = childrens;
	}

	public boolean isExternal() {
		return external;
	}

	public void setExternal(boolean external) {
		this.external = external;
	}

	/** Default constructor. */
	public NavigationDisplayItem() {
		this(null, "/", false, false, null);
	}
	
	/** Canonical constructor.*/
	public NavigationDisplayItem(String aTitle, String anUrl, boolean anExternal, boolean anSelected, CMSItem anNavItem) {
		super();
		title = aTitle;
		url = anUrl;
		external = anExternal;
		selected = anSelected;
		navItem = anNavItem;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String aTitle) {
		this.title = aTitle;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String anUrl) {
		this.url = anUrl;
	}
	
	public CMSItem navItem;
	

	public CMSItem getNavItem() {
		return navItem;
	}

	public void setNavItem(CMSItem navItem) {
		this.navItem = navItem;
	}

	public String toString() {
		return super.toString() + " {" + title + ": " + url + "}";
	}		

}
