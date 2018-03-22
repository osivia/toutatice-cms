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
package fr.toutatice.portail.cms.nuxeo.portlets.list;

import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Documents;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

public class DocumentQueryCommand implements INuxeoCommand{
	
	String nuxeoRequest;
	
	public DocumentQueryCommand( String nuxeoRequest) {
		super();
		this.nuxeoRequest = nuxeoRequest;
	}
	
	public Object execute( Session session)	throws Exception {
		
		OperationRequest request = session.newRequest("Document.Query");
		request.set("query", "SELECT * FROM Document WHERE " +nuxeoRequest);
		//OperationRequest request = session.newRequest("Document.PageProvider");
		//request.set("pageSize", 4).set("page", 2);
		request.setHeader(Constants.HEADER_NX_SCHEMAS, "*");

		// Object result = request.execute();
		
		Documents results = (Documents) request.execute();
		
		/*
		
		Documents results = new Documents();
		
		for (Document doc : liste)	{
		
			if( doc.getType().equals("ContextualLink"))	{
				// Il nous faut l'url du lien, on refait un fetch
				// A OPTIMISER en spécialisant la requête Nuxeo
		
				org.nuxeo.ecm.automation.client.jaxrs.model.Document docLink = (org.nuxeo.ecm.automation.client.jaxrs.model.Document) session
				.newRequest("Document.Fetch").setHeader(Constants.HEADER_NX_SCHEMAS, "*").set("value", doc.getId())
				.execute();
				
				results.add( docLink);
			}	else	{
				results.add( doc);
			}
		}
		
		*/
		

	     return results;
	
	}

	public String getId() {
		return "DocumentQueryCommand/"+nuxeoRequest;
	};		

}
