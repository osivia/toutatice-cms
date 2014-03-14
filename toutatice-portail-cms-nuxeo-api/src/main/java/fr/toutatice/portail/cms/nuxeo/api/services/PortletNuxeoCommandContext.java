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



/**
 * The Class PortletNuxeoCommandContext.
 */
public class PortletNuxeoCommandContext {

	/** The auth type anonymous. */
	public static int AUTH_TYPE_ANONYMOUS = 0; 
	
	/** The auth type user. */
	public static int AUTH_TYPE_USER = 1;	
	
	/** The auth type profil. */
	public static int AUTH_TYPE_PROFIL = 2; 
	
	/** The auth type superuser. */
	public static int AUTH_TYPE_SUPERUSER = 3;

	
	/** The asynchronous updates. */
	public boolean asynchronousUpdates = false;

	/**
	 * Checks if is asynchronous updates.
	 *
	 * @return true, if is asynchronous updates
	 */
	public boolean isAsynchronousUpdates() {
		return asynchronousUpdates;
	}

	/**
	 * Sets the asynchronous updates.
	 *
	 * @param asynchronousUpdates the new asynchronous updates
	 */
	public void setAsynchronousUpdates(boolean asynchronousUpdates) {
		this.asynchronousUpdates = asynchronousUpdates;
	}

	/**
	 * Gets the cache time out.
	 *
	 * @return the cache time out
	 */
	public long getCacheTimeOut() {
		return cacheTimeOut;
	}

	/**
	 * Sets the cache time out.
	 *
	 * @param cacheTimeOut the new cache time out
	 */
	public void setCacheTimeOut(long cacheTimeOut) {
		this.cacheTimeOut = cacheTimeOut;
	}

	/** The ctx. */
	PortletContext ctx;
	
	/** The request. */
	PortletRequest request;

	/** The auth type. */
	int authType = AUTH_TYPE_USER;
	
	/** The cache type. */
	int cacheType = CacheInfo.CACHE_SCOPE_NONE;
	
	/**
	 * Gets the cache type.
	 *
	 * @return the cache type
	 */
	public int getCacheType() {
		return cacheType;
	}

	/**
	 * Sets the cache type.
	 *
	 * @param cacheType the new cache type
	 */
	public void setCacheType(int cacheType) {
		this.cacheType = cacheType;
	}

	/** The profil. */
	ProfilBean profil = null;
	
	/** The cache time out. */
	long cacheTimeOut = 50 * 1000;

	/**
	 * Gets the auth type.
	 *
	 * @return the auth type
	 */
	public int getAuthType() {
		return authType;
	}

	/**
	 * Sets the auth type.
	 *
	 * @param authType the new auth type
	 */
	public void setAuthType(int authType) {
		this.authType = authType;
	}

	/**
	 * Gets the auth profil.
	 *
	 * @return the auth profil
	 */
	public ProfilBean getAuthProfil() {
		return profil;
	}

	/**
	 * Sets the auth profil.
	 *
	 * @param profil the new auth profil
	 */
	public void setAuthProfil(ProfilBean profil) {
		this.profil = profil;
	}

	/**
	 * Gets the request.
	 *
	 * @return the request
	 */
	public PortletRequest getRequest() {
		return request;
	}


	/**
	 * Gets the portlet context.
	 *
	 * @return the portlet context
	 */
	public PortletContext getPortletContext() {
		return ctx;
	}

	/**
	 * Instantiates a new portlet nuxeo command context.
	 *
	 * @param ctx the ctx
	 * @param request the request
	 */
	public PortletNuxeoCommandContext(PortletContext ctx, PortletRequest request) {
		super();
		this.ctx = ctx;
		this.request = request;

	}

}
