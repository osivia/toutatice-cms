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

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilter;


public class ListCommand implements INuxeoCommand {
	
	String nuxeoRequest;
	int pageNumber;
	int pageSize;
	String schemas;
	boolean displayLiveVersion;
	String portalPolicyFilter;

	
	public ListCommand( String nuxeoRequest, boolean displayLiveVersion, int pageNumber, int pageSize, String schemas, String portalPolicyFilter) {
		super();
		this.nuxeoRequest = nuxeoRequest;
		this.displayLiveVersion = displayLiveVersion;
		this.pageNumber = pageNumber;
		this.pageSize = pageSize;
		this.schemas = schemas;
		this.portalPolicyFilter = portalPolicyFilter;

	}
	
	public Object execute( Session nuxeoSession)	throws Exception {
		
		OperationRequest request;
		

		request =  nuxeoSession.newRequest("Document.PageProvider");
		request.set("pageSize", pageSize);
		request.set("page", pageNumber);
		
		// Insertion du filtre sur les élements publiés
		String filteredRequest = NuxeoQueryFilter.addPublicationFilter(nuxeoRequest, displayLiveVersion, portalPolicyFilter);

		
		request.set("query", "SELECT * FROM Document WHERE "  + filteredRequest);
	
		request.setHeader(Constants.HEADER_NX_SCHEMAS, schemas);

		
		return request.execute();
	}

	public String getId() {
		return "ListCommand/"+displayLiveVersion+"/"+pageSize+"/"+pageNumber+"/"+nuxeoRequest;
	};
	


}
