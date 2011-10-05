package fr.toutatice.portail.core.nuxeo;

import java.util.List;

import org.nuxeo.ecm.automation.client.jaxrs.Session;

public interface INuxeoService {
	
	public Session createUserSession(String userId) throws Exception ;
	
	public void registerLinkHandler( INuxeoLinkHandler linkManager);

	public INuxeoLinkHandler getLinkHandler();
	
	public void registerListTemplates( List<ListTemplate> templates);
	
	public List<ListTemplate> getListTemplates();

}
