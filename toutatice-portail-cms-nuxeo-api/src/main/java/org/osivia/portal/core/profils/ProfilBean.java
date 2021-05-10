/*
 * (C) Copyright 2014 OSIVIA (http://www.osivia.com) 
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
 */
package org.osivia.portal.core.profils;

import java.io.Serializable;

public class ProfilBean implements Serializable {

	private static final long serialVersionUID = -5878083215376307378L;
	private String roleName = "";
	private String name = "";
	private String nuxeoVirtualUser = "";
	private String defaultPageName = "";	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ProfilBean(String name, String roleName, String defaultPageName, String nuxeoVirtualUser) {
		super();
		this.name = name;
		this.roleName = roleName;
		this.defaultPageName = defaultPageName;
		this.nuxeoVirtualUser = nuxeoVirtualUser;
	}
	public String getRoleName() {
		return roleName;
	}
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	
	public String getDefaultPageName() {
		return defaultPageName;
	}
	public void setDefaultPageName(String defaultPageName) {
		this.defaultPageName = defaultPageName;
	}
	
	public String getNuxeoVirtualUser() {
		return nuxeoVirtualUser;
	}
	public void setNuxeoVirtualUser(String nuxeoVirtualUser) {
		this.nuxeoVirtualUser = nuxeoVirtualUser;
	}


}
