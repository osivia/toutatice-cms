package fr.toutatice.portail.core.nuxeo;

import java.util.List;

import org.nuxeo.ecm.automation.client.jaxrs.Session;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;

import fr.toutatice.portail.core.cms.ICMSService;
import fr.toutatice.portail.core.cms.spi.ICMSIntegration;

public interface INuxeoService extends ICMSIntegration, ICMSService {
	
	public Session createUserSession(String userId) throws Exception ;
	
	public void registerCMSCustomizer( INuxeoCustomizer linkManager);

	public INuxeoCustomizer getCMSCustomizer();
	
	public void registerCMSService( ICMSService cmsService);
	


}
