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

		this.ctx = new NuxeoCommandContext(origCtx.getPortletContext());

		this.ctx.setAuthType(origCtx.getAuthType());
		this.ctx.setAuthProfil(origCtx.getAuthProfil());
        this.ctx.setAsynchronousCommand(origCtx.isAsynchronousCommand());

		this.command = cmd;
	}


	 public NuxeoCommandContext getCtx() {
		return this.ctx;
	}


	public INuxeoServiceCommand getCommand() {
		return this.command;
	}



	@Override
	 public int hashCode()	{
		 int hc =  this.command.getId().hashCode();
		 return hc;
	 }

	@Override
    public boolean equals(Object obj) {
		 if(this == obj) {
		     return true;
		 }

		 if( ! (obj instanceof AsyncCommandBean)) {
            return false;
        }

		 return this.getCommand().getId().equals(((AsyncCommandBean) obj).getCommand().getId());
	}
}
