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

import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.core.cms.NavigationItem;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;


/**
 * Return all the navigation items
 * 
 * @author jeanseb
 * 
 */
public class DocumentResolvePublishSpaceCommand implements INuxeoCommand {

	String path;

	public DocumentResolvePublishSpaceCommand(  String path) {
		super();
		this.path = path;
	}

	public Object execute(Session session) throws Exception {

	
		NavigationItem navItem = new NavigationItem();

		Document publishSpace = null;


/*		
 try	{ 
 */
		 publishSpace = (org.nuxeo.ecm.automation.client.model.Document) session
		.newRequest("Document.FetchPublishSpace").setHeader(Constants.HEADER_NX_SCHEMAS, "dublincore,common, toutatice")
		.set("value", path).execute();
		 
		 /*
		} catch (RemoteException e){
			if( e.getStatus() == 404){
				//String prefix = "/default-domain/workspaces/ac-rennes/";
				// Préfixe modifié pour acrennes
				String prefix = "/default/";
				
				
				if( path.startsWith(prefix))	{
					String sitePath = path;
					int end = path.indexOf("/", prefix.length());
					if( end != -1)
						sitePath = path.substring(0, end);
					publishSpace = (org.nuxeo.ecm.automation.client.jaxrs.model.Document) session
				.newRequest("Document.Fetch").setHeader(Constants.HEADER_NX_SCHEMAS, "dublincore,common, toutatice")
				.set("value",sitePath).execute();
					if( "deleted".equals(publishSpace.getState()))
							throw e;
				}
				
			} else
				throw e;
		}
*/


		navItem.setMainDoc(publishSpace);

		return navItem;

	}

	public String getId() {
		return "ResolvePublishSpaceCommand/" + path;
	};

}