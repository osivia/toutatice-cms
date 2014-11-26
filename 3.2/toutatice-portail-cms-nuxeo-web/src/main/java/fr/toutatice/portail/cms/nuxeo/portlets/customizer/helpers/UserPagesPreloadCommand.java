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
package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Documents;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;



/**
 * Return all the navigation items
 * 
 * @author jeanseb
 * 
 */
public class UserPagesPreloadCommand implements INuxeoCommand {



	public UserPagesPreloadCommand( ) {
		super();

	}

	public Object execute(Session session) throws Exception {

	
		OperationRequest request;

		request = session.newRequest("Document.Query");

  		
		//v2.0.9 : suite demande Marc
		String bufferedRequest = "ttc:isPreloadedOnLogin = 1 AND ecm:mixinType = 'Space' AND (ecm:isProxy = 1 OR (ecm:isProxy = 0 AND ecm:mixinType <> 'WebView')) AND (ecm:currentLifeCycleState <> 'deleted' AND ecm:isCheckedInVersion = 0 )";


		request.set("query", "SELECT * FROM Document WHERE " + bufferedRequest.toString() + " ORDER BY ttc:tabOrder");
		
	
		request.setHeader(Constants.HEADER_NX_SCHEMAS, "dublincore,common, toutatice");

		Documents children = (Documents) request.execute();


		return children;

	}

	public String getId() {
		return "UserPagesPreloadCommand";
	};

}
