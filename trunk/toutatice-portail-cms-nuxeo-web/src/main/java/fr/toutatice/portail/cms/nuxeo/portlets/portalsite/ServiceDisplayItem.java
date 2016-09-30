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
package fr.toutatice.portail.cms.nuxeo.portlets.portalsite;

public class ServiceDisplayItem {
	/** The display title of the  link. */
	private String title;
	
	/** The absolute URL of the  link. */
	private String url;
	
	private boolean external;
	
	public boolean isExternal() {
		return external;
	}

	public void setExternal(boolean external) {
		this.external = external;
	}

	/** Default constructor. */
	public ServiceDisplayItem() {
		this(null, "/", false);
	}
	
	/** Canonical constructor.*/
	public ServiceDisplayItem(String aTitle, String anUrl, boolean anExternal) {
		super();
		title = aTitle;
		url = anUrl;
		external = anExternal;
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
	
	public String toString() {
		return super.toString() + " {" + title + ": " + url + "}";
	}		

}
