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

import org.nuxeo.ecm.automation.client.Session;
import org.osivia.portal.api.cms.UniversalID;

/**
 * The Interface INuxeoServiceCommand.
 */
public interface INuxeoServiceCommand {
	
	/**
	 * Execute.
	 *
	 * @param nuxeoSession the nuxeo session
	 * @return the object
	 * @throws Exception the exception
	 */
	public Object execute( Session nuxeoSession) throws Exception;
	
	
	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public String getId();
	
	

}
