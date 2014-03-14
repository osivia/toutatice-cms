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
package fr.toutatice.portail.cms.nuxeo.api.services;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;

import org.osivia.portal.api.cache.services.CacheInfo;
import org.osivia.portal.core.profils.ProfilBean;



public class PortletNuxeoCommandContext {

	public static int AUTH_TYPE_ANONYMOUS = 0; 
	public static int AUTH_TYPE_USER = 1;	
	public static int AUTH_TYPE_PROFIL = 2; 
	public static int AUTH_TYPE_SUPERUSER = 3;

	
	public boolean asynchronousUpdates = false;

	public boolean isAsynchronousUpdates() {
		return asynchronousUpdates;
	}

	public void setAsynchronousUpdates(boolean asynchronousUpdates) {
		this.asynchronousUpdates = asynchronousUpdates;
	}

	public long getCacheTimeOut() {
		return cacheTimeOut;
	}

	public void setCacheTimeOut(long cacheTimeOut) {
		this.cacheTimeOut = cacheTimeOut;
	}

	PortletContext ctx;
	PortletRequest request;

	int authType = AUTH_TYPE_USER;
	int cacheType = CacheInfo.CACHE_SCOPE_NONE;
	
	public int getCacheType() {
		return cacheType;
	}

	public void setCacheType(int cacheType) {
		this.cacheType = cacheType;
	}

	ProfilBean profil = null;
	long cacheTimeOut = 50 * 1000;

	public int getAuthType() {
		return authType;
	}

	public void setAuthType(int authType) {
		this.authType = authType;
	}

	public ProfilBean getAuthProfil() {
		return profil;
	}

	public void setAuthProfil(ProfilBean profil) {
		this.profil = profil;
	}

	public PortletRequest getRequest() {
		return request;
	}


	public PortletContext getPortletContext() {
		return ctx;
	}

	public PortletNuxeoCommandContext(PortletContext ctx, PortletRequest request) {
		super();
		this.ctx = ctx;
		this.request = request;

	}

}
