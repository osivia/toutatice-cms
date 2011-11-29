package fr.toutatice.portail.core.nuxeo;

import java.util.List;

import org.nuxeo.ecm.automation.client.jaxrs.Session;

import fr.toutatice.portail.core.cms.spi.ICMSIntegration;

public interface INuxeoService extends ICMSIntegration{
	
	public Session createUserSession(String userId) throws Exception ;
	
	public void registerLinkHandler( INuxeoLinkHandler linkManager);

	public INuxeoLinkHandler getLinkHandler();
	
	public void registerListTemplates( List<ListTemplate> templates);
	
	public List<ListTemplate> getListTemplates();

}
