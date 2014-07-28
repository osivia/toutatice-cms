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

import org.jboss.portal.common.invocation.Scope;
import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.server.ServerInvocation;
import org.osivia.portal.api.cache.services.CacheInfo;
import org.osivia.portal.core.profils.ProfilBean;


/**
 * The Class NuxeoCommandContext.
 */
public class NuxeoCommandContext {

	/** The auth type anonymous. */
	public static int AUTH_TYPE_ANONYMOUS = 0;

	/** The auth type user. */
	public static int AUTH_TYPE_USER = 1;

	/** The auth type profil. */
	public static int AUTH_TYPE_PROFIL = 2;

	/** The auth type superuser. */
	public static int AUTH_TYPE_SUPERUSER = 3;

	/** The request. */
	private Object request;

	/** The controller ctx. */
	private ControllerContext controllerCtx;

	/** The server invocation. */
	private ServerInvocation serverInvocation;


	/**
	 * Gets the server invocation.
	 *
	 * @return the server invocation
	 */
	public ServerInvocation getServerInvocation() {
		return this.serverInvocation;
	}

	/**
	 * Sets the server invocation.
	 *
	 * @param serverInvocation the new server invocation
	 */
	public void setServerInvocation(ServerInvocation serverInvocation) {
		this.serverInvocation = serverInvocation;
	}

	/**
	 * Asynchronous loadinf policy
	 */
	private boolean isAsyncCacheRefreshing = false;

	/** The asynchronous updates. */
	public boolean asynchronousUpdates = false;

	/** The administrator. */
	private boolean administrator = false;


	/** The force reload. */
	private boolean forceReload = false;

	/**
	 * Checks if is force reload.
	 *
	 * @return the forceReload
	 */
	public boolean isForceReload() {
		return this.forceReload;
	}

	/**
	 * Sets the force reload.
	 *
	 * @param forceReload the forceReload to set
	 */
	public void setForceReload(boolean forceReload) {
		this.forceReload = forceReload;
	}

	/**
	 * Checks if is administrator.
	 *
	 * @return true, if is administrator
	 */
	public boolean isAdministrator() {
		return this.administrator;
	}

	/**
	 * Checks if is asynchronous updates.
	 *
	 * @return true, if is asynchronous updates
	 */
	public boolean isAsynchronousUpdates() {
		return this.asynchronousUpdates;
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
		return this.cacheTimeOut;
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
		return this.cacheType;
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
	long cacheTimeOut = -1;

	/**
	 * Gets the auth type.
	 *
	 * @return the auth type
	 */
	public int getAuthType() {
		return this.authType;
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
		return this.profil;
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
	public Object getRequest() 	{
		return this.request;
	}


	/**
	 * Gets the portlet context.
	 *
	 * @return the portlet context
	 */
	public PortletContext getPortletContext() {
		return this.ctx;
	}

	/**
	 * Checks if is async cache refreshing.
	 *
	 * @return true, if is async cache refreshing
	 */
	public boolean isAsyncCacheRefreshing() {
		return this.isAsyncCacheRefreshing;
	}

	/**
	 * Sets the async cache refreshing.
	 *
	 * @param isAsyncCacheRefreshing the new async cache refreshing
	 */
	public void setAsyncCacheRefreshing(boolean isAsyncCacheRefreshing) {
		this.isAsyncCacheRefreshing = isAsyncCacheRefreshing;
	}

	/**
	 * Instantiates a new nuxeo command context.
	 *
	 * @param ctx the ctx
	 */
	public NuxeoCommandContext(PortletContext ctx) {
		super();
		this.ctx = ctx;

	}

	/**
	 * Instantiates a new nuxeo command context.
	 *
	 * @param ctx the ctx
	 * @param request the request
	 */
	public NuxeoCommandContext(PortletContext ctx, PortletRequest request) {
		super();
		this.ctx = ctx;
		this.request = request;

		if( request instanceof PortletRequest)	{
			this.controllerCtx =  (ControllerContext) ((PortletRequest) this.getRequest()).getAttribute("osivia.controller");
            this.administrator = Boolean.TRUE.equals(     (((PortletRequest) this.getRequest()).getAttribute("osivia.isAdministrator")))        ;
		}
	}



	/**
	 * Instantiates a new nuxeo command context.
	 *
	 * @param ctx the ctx
	 * @param serverInvocation the server invocation
	 */
	public NuxeoCommandContext(PortletContext ctx, ServerInvocation serverInvocation) {
		super();
		this.ctx = ctx;
		this.serverInvocation =  serverInvocation;

		this.request = serverInvocation.getServerContext().getClientRequest();


		Boolean isAdmin = (Boolean) serverInvocation.getAttribute(Scope.PRINCIPAL_SCOPE, "osivia.isAdmin");

		if( Boolean.TRUE.equals(isAdmin))
			this.administrator = true;
	}


	/**
	 * Gets the controler context.
	 *
	 * @return the controler context
	 */
	public ControllerContext getControlerContext()	{
		return this.controllerCtx;

	}

}
