package fr.toutatice.portail.cms.nuxeo.jbossportal;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import org.jboss.portal.common.invocation.Scope;
import org.jboss.portal.core.aspects.controller.PageCustomizerInterceptor;
import org.jboss.portal.core.controller.ControllerContext;

import fr.toutatice.portail.api.cache.services.CacheInfo;

import fr.toutatice.portail.core.profils.ProfilBean;

public class NuxeoCommandContext {

	public static int AUTH_TYPE_ANONYMOUS = 0; 
	public static int AUTH_TYPE_USER = 1;	
	public static int AUTH_TYPE_PROFIL = 2; 
	public static int AUTH_TYPE_SUPERUSER = 3;
	
	private Object request;
	
	private ControllerContext controllerCtx;

	
	public boolean asynchronousUpdates = false;
	
	private boolean administrator = false;

	public boolean isAdministrator() {
		return administrator;
	}

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

	public Object getRequest() 	{
		return request;
	}


	public PortletContext getPortletContext() {
		return ctx;
	}
	
	
	public NuxeoCommandContext(PortletContext ctx) {
		super();
		this.ctx = ctx;

	}
	
	public NuxeoCommandContext(PortletContext ctx, PortletRequest request) {
		super();
		this.ctx = ctx;
		this.request = request;
		
		if( request instanceof PortletRequest)	{
			controllerCtx =  (ControllerContext) ((PortletRequest) getRequest()).getAttribute("pia.controller");
			administrator = "true".equals(     (((PortletRequest) getRequest()).getAttribute("pia.isAdministrator")))		;	
		}
	}
	
	public NuxeoCommandContext(PortletContext ctx, ControllerContext controllerCtx) {
		super();
		this.ctx = ctx;
		this.controllerCtx =  controllerCtx;
		this.request = controllerCtx.getServerInvocation().getServerContext().getClientRequest();
		
		
		Boolean isAdmin = (Boolean) controllerCtx.getAttribute(Scope.PRINCIPAL_SCOPE, "pia.isAdmin");
		
		if( Boolean.TRUE.equals(isAdmin))
			administrator = true;
	}
	

	
	public ControllerContext getControlerContext()	{
		return controllerCtx;

	}

}
