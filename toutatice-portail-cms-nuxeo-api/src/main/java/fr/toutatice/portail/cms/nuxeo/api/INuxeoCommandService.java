package fr.toutatice.portail.cms.nuxeo.api;

import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.core.profils.IProfilManager;

import fr.toutatice.portail.cms.nuxeo.jbossportal.NuxeoCommandContext;

public interface INuxeoCommandService {
	
	public  Object executeCommand(NuxeoCommandContext ctx,	INuxeoCommand command) throws Exception ;
	public  void destroy() throws Exception ;
	
	public IProfilManager getProfilManager(NuxeoCommandContext ctx) throws Exception;
	public IPortalUrlFactory getPortalUrlFactory(NuxeoCommandContext ctx)  throws Exception;
	

}
