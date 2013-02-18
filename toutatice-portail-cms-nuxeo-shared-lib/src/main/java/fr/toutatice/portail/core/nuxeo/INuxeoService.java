package fr.toutatice.portail.core.nuxeo;

import java.util.List;

import org.nuxeo.ecm.automation.client.jaxrs.Session;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
import org.osivia.portal.core.cms.ICMSService;
import org.osivia.portal.core.cms.spi.ICMSIntegration;


public interface INuxeoService extends ICMSIntegration {
	
	public Session createUserSession(String userId) throws Exception ;
	
	public void registerCMSCustomizer( INuxeoCustomizer linkManager);

	public INuxeoCustomizer getCMSCustomizer();
	
	public void registerCMSService( ICMSService cmsService);
	


}
