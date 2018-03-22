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

import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.cache.services.IServiceInvoker;

/**
 * Just to mark that publish site is not fetchable for this publication
 * 
 * @author jeanseb
 *
 */
public class FetchableContentInvoker implements IServiceInvoker {

	private static final long serialVersionUID = -4271471756834717062L;
	private boolean resolvable = false;

	
	public FetchableContentInvoker(boolean resolvable) {

		super();

		this.resolvable = true;
	}



	public Object invoke() throws PortalException {
		return new Boolean(resolvable);
	}

}
