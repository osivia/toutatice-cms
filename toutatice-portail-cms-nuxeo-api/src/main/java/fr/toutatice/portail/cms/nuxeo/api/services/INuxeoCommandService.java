package fr.toutatice.portail.cms.nuxeo.api.services;

import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.core.profils.IProfilManager;


public interface INuxeoCommandService {
	
	public  Object executeCommand(NuxeoCommandContext commandCtx,	INuxeoServiceCommand command) throws Exception ;
	public  void destroy() throws Exception ;
	
	public IProfilManager getProfilManager(NuxeoCommandContext ctx) throws Exception;
	public IPortalUrlFactory getPortalUrlFactory(NuxeoCommandContext ctx)  throws Exception;
	

}
