/*
 * (C) Copyright 2014 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 *
 *    
 */
package fr.toutatice.portail.cms.nuxeo.api.services;



/**
 * The Class AsyncCommandBean.
 */
public class AsyncCommandBean {
	
	/** The ctx. */
	private NuxeoCommandContext ctx;
	
	/** The command. */
	private INuxeoServiceCommand command;
	
	/**
	 * Instantiates a new async command bean.
	 *
	 * @param origCtx the orig ctx
	 * @param cmd the cmd
	 */
	protected AsyncCommandBean( NuxeoCommandContext origCtx, INuxeoServiceCommand cmd){
		
		ctx = new NuxeoCommandContext(origCtx.getPortletContext());
		
		ctx.setAuthType(origCtx.getAuthType());
		ctx.setAuthProfil(origCtx.getAuthProfil());
		command = cmd;
	}
	
	
	 /**
 	 * Gets the ctx.
 	 *
 	 * @return the ctx
 	 */
 	public NuxeoCommandContext getCtx() {
		return ctx;
	}


	/**
	 * Gets the command.
	 *
	 * @return the command
	 */
	public INuxeoServiceCommand getCommand() {
		return command;
	}


	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	 public int hashCode()	{
		 int hc =  command.getId().hashCode();
		 return hc;
	 }
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		 if(this == obj) {
		     return true;
		 }
		 
		 if( ! (obj instanceof AsyncCommandBean))
			 return false;
		 
		 return getCommand().getId().equals(((AsyncCommandBean) obj).getCommand().getId());
	}
}
