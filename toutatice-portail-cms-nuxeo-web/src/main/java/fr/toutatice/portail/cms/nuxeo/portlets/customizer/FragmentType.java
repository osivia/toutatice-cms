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
package fr.toutatice.portail.cms.nuxeo.portlets.customizer;

public class FragmentType {
	
	private String key;
	private String label;
	public  String adminJspName;
	public  String viewJspName;
	IFragmentModule module;

	
	
	public IFragmentModule getModule() {
		return module;
	}
	public void setModule(IFragmentModule module) {
		this.module = module;
	}
	public String getAdminJspName() {
		return adminJspName;
	}
	public void setAdminJspName(String adminJspName) {
		this.adminJspName = adminJspName;
	}
	public String getViewJspName() {
		return viewJspName;
	}
	public void setViewJspName(String viewJspName) {
		this.viewJspName = viewJspName;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public FragmentType(String key, String label, IFragmentModule module, String viewJspName, String adminJspName) {
		super();
		this.key = key;
		this.label = label;
		this.module = module;
		this.adminJspName = adminJspName;
		this.viewJspName = viewJspName;
	}



}
