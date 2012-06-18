package fr.toutatice.portail.cms.nuxeo.jbossportal;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;


public class AsyncCommandBean {
	
	private NuxeoCommandContext ctx;
	private INuxeoCommand command;
	
	protected AsyncCommandBean( NuxeoCommandContext origCtx, INuxeoCommand cmd){
		
		ctx = new NuxeoCommandContext(origCtx.getPortletContext());
		
		ctx.setAuthType(origCtx.getAuthType());
		ctx.setAuthProfil(origCtx.getAuthProfil());
		command = cmd;
	}
	
	
	 public NuxeoCommandContext getCtx() {
		return ctx;
	}


	public INuxeoCommand getCommand() {
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
