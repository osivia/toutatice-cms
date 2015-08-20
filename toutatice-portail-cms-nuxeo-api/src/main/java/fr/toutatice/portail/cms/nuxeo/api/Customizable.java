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
package fr.toutatice.portail.cms.nuxeo.api;

/**
 * Listing of existing extension points and the property to access them.
 * @author lbillon
 *
 */
public enum Customizable {

	JSP("osivia.customizer.cms.jsp"),
	
	LIST_TEMPLATE("osivia.customizer.cms.template."),
	
	FRAGMENT("osivia.customizer.cms.fragments."),
	
	EDITABLE_WINDOW("osivia.customizer.cms.ew."),
	
	DOC_TYPE("osivia.customizer.cms.doctype"),
	
	PLAYER("osivia.customizer.cms.modules"),
	
	MENUBAR("osivia.customizer.cms.menubar");
	
	
	
	private String property;
	
	private Customizable(String property) {
		this.property = property;
	}
	
	@Override
	public String toString() {
		return property;
	}
}
