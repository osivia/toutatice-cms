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

import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.core.profils.IProfilManager;



/**
 * The Interface INuxeoCommandService.
 * 
 * 
 */
public interface INuxeoCommandService {
	
	/**
	 * Execute command.
	 *
	 * @param commandCtx the command ctx
	 * @param command the command
	 * @return the object
	 * @throws Exception the exception
	 */
	public  Object executeCommand(NuxeoCommandContext commandCtx,	INuxeoServiceCommand command) throws Exception ;
	
	/**
	 * Destroy.
	 *
	 * @throws Exception the exception
	 */
	public  void destroy() throws Exception ;
	
	/**
	 * Gets the profil manager.
	 *
	 * @param ctx the ctx
	 * @return the profil manager
	 * @throws Exception the exception
	 */
	public IProfilManager getProfilManager(NuxeoCommandContext ctx) throws Exception;
	
	/**
	 * Gets the portal url factory.
	 *
	 * @param ctx the ctx
	 * @return the portal url factory
	 * @throws Exception the exception
	 */
	public IPortalUrlFactory getPortalUrlFactory(NuxeoCommandContext ctx)  throws Exception;
	

}
