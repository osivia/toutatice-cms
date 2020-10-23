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


import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;



public class GetUserWorkspacePathCommand  implements INuxeoCommand{
	
    private String username;

    
    public GetUserWorkspacePathCommand(String username) {
        this.username = username;
    }

	/**
	 * execution d'une requete nuxéo 
	 * @return 
	 */
	public Object execute(Session automationSession) throws Exception {
	
        OperationRequest newRequest = automationSession.newRequest("Services.GetToutaticeUserProfile");
        if (username != null) {
            newRequest.set("username", username);
        }
        Document document =  (Document) newRequest.execute();
        
        return document.getPath().substring(0, document.getPath().lastIndexOf('/')) ;

		
	}

	public String getId() {
		
        return "GetUserWorkspacePathCommand/".concat(username);
	}

}
