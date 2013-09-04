package fr.toutatice.portail.cms.nuxeo.api.services;

import javax.portlet.PortletContext;

import org.nuxeo.ecm.automation.client.Session;
import org.osivia.portal.core.cms.spi.ICMSIntegration;


public interface INuxeoService extends ICMSIntegration {
	
	public Session createUserSession(String userId) throws Exception ;
	
	public void registerCMSCustomizer( INuxeoCustomizer linkManager);

	public INuxeoCustomizer getCMSCustomizer();
	
	public INuxeoCommandService startNuxeoCommandService(PortletContext portletCtx)  throws Exception ;

}
