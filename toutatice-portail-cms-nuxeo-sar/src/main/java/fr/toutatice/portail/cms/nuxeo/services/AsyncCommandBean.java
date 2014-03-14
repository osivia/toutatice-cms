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
