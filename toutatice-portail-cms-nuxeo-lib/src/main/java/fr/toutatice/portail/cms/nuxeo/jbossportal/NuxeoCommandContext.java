package fr.toutatice.portail.cms.nuxeo.jbossportal;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;

import fr.toutatice.portail.cms.nuxeo.core.NuxeoProperties;
import fr.toutatice.portail.core.profils.ProfilBean;

public class NuxeoCommandContext {

	public static int SCOPE_TYPE_ANONYMOUS = 0;
	public static int SCOPE_TYPE_USER = 1;
	public static int SCOPE_TYPE_PROFIL = 2;
	
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

	int scopeType = SCOPE_TYPE_USER;
	ProfilBean profil = null;
	long cacheTimeOut = 50 * 1000;

	public int getScopeType() {
		return scopeType;
	}

	public void setScopeType(int scopeType) {
		this.scopeType = scopeType;
	}

	public ProfilBean getScopeProfil() {
		return profil;
	}

	public void setScopeProfil(ProfilBean profil) {
		this.profil = profil;
	}

	public PortletRequest getRequest() {
		return request;
	}

	NuxeoProperties nuxeoProperties = new NuxeoProperties();

	public NuxeoProperties getNuxeoProperties() {
		return nuxeoProperties;
	}

	public PortletContext getPortletContext() {
		return ctx;
	}

	public NuxeoCommandContext(PortletContext ctx, PortletRequest request) {
		super();
		this.ctx = ctx;
		this.request = request;

	}

}
