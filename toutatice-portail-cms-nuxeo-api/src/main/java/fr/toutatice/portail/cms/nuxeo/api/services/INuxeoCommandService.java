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


public interface INuxeoCommandService {
	
	public  Object executeCommand(NuxeoCommandContext commandCtx,	INuxeoServiceCommand command) throws Exception ;
	public  void destroy() throws Exception ;
	
	public IProfilManager getProfilManager(NuxeoCommandContext ctx) throws Exception;
	public IPortalUrlFactory getPortalUrlFactory(NuxeoCommandContext ctx)  throws Exception;
	

}
