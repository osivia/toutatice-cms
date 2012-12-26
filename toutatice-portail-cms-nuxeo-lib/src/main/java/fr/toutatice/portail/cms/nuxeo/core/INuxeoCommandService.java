package fr.toutatice.portail.cms.nuxeo.core;

import org.osivia.portal.core.profils.IProfilManager;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.jbossportal.NuxeoCommandContext;

public interface INuxeoCommandService {
	
	public  Object executeCommand(NuxeoCommandContext ctx,	INuxeoCommand command) throws Exception ;
	public  void destroy() throws Exception ;
	
	public IProfilManager getProfilManager() throws Exception;
	

}
