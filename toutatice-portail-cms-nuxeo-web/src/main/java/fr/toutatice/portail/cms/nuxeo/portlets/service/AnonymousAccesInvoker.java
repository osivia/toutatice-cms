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
package fr.toutatice.portail.cms.nuxeo.portlets.service;

import org.osivia.portal.api.cache.services.IServiceInvoker;

/**
 * Just to mark that publish site is not fetchable for this publication
 * 
 * @author jeanseb
 *
 */
public class AnonymousAccesInvoker implements IServiceInvoker {

	private static final long serialVersionUID = -4271471756834717062L;
	
	public static final int AUTHORIZED = 0;
	public static final int FORBIDDEN = 1;
	public static final int NOT_FOUND = 2;
	
	protected class AccesStatus {
		
		private boolean access = false;
		private int status = AUTHORIZED;
		
		public AccesStatus(boolean access, int status){
			this.access = access;
			this.status = status;
		}

		protected boolean isAccess() {
			return access;
		}

		protected void setAccess(boolean access) {
			this.access = access;
		}

		protected int getStatus() {
			return status;
		}

		protected void setStatus(int status) {
			this.status = status;
		}
		
	}
	
	private AccesStatus accesStatus;
	
	public AnonymousAccesInvoker(boolean access, int status) {
		super();
		this.accesStatus = new AccesStatus(access, status);

	}



	public Object invoke() throws Exception {
		return accesStatus;
	}



	protected AccesStatus getAccesStatus() {
		return accesStatus;
	}



	protected void setAccesStatus(AccesStatus accesStatus) {
		this.accesStatus = accesStatus;
	}

}
