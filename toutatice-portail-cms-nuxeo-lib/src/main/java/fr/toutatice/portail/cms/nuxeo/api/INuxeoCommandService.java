package fr.toutatice.portail.cms.nuxeo.api;

import fr.toutatice.portail.cms.nuxeo.jbossportal.NuxeoCommandContext;
import fr.toutatice.portail.core.profils.IProfilManager;

public interface INuxeoCommandService {
	
	public  Object executeCommand(NuxeoCommandContext ctx,	INuxeoCommand command) throws Exception ;
	public  void destroy() throws Exception ;
	
	public IProfilManager getProfilManager() throws Exception;
	

}
