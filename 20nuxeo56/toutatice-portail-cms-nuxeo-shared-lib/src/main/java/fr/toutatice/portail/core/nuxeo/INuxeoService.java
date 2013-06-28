package fr.toutatice.portail.core.nuxeo;

import org.nuxeo.ecm.automation.client.Session;
import org.osivia.portal.core.cms.spi.ICMSIntegration;


public interface INuxeoService extends ICMSIntegration {
	
	public Session createUserSession(String userId) throws Exception ;
	
	public void registerCMSCustomizer( INuxeoCustomizer linkManager);

	public INuxeoCustomizer getCMSCustomizer();

}
