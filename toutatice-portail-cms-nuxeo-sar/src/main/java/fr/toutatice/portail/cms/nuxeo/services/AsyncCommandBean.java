package fr.toutatice.portail.cms.nuxeo.services;

import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoServiceCommand;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandContext;




public class AsyncCommandBean {
	
	private NuxeoCommandContext ctx;
	private INuxeoServiceCommand command;
	
	protected AsyncCommandBean( NuxeoCommandContext origCtx, INuxeoServiceCommand cmd){
		
		ctx = new NuxeoCommandContext(origCtx.getPortletContext());
		
		ctx.setAuthType(origCtx.getAuthType());
		ctx.setAuthProfil(origCtx.getAuthProfil());
		command = cmd;
	}
	
	
	 public NuxeoCommandContext getCtx() {
		return ctx;
	}


	public INuxeoServiceCommand getCommand() {
		return command;
	}


	
	@Override
	 public int hashCode()	{
		 int hc =  command.getId().hashCode();
		 return hc;
	 }
	
	public boolean equals(Object obj) {
		 if(this == obj) {
		     return true;
		 }
		 
		 if( ! (obj instanceof AsyncCommandBean))
			 return false;
		 
		 return getCommand().getId().equals(((AsyncCommandBean) obj).getCommand().getId());
	}
}
