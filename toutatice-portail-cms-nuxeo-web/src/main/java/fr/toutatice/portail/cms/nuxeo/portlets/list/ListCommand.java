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
import org.osivia.portal.core.constants.InternalConstants;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilter;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilterContext;
import fr.toutatice.portail.cms.nuxeo.portlets.commands.CommandConstants;


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
		request.set("maxResults", CommandConstants.PAGE_PROVIDER_UNLIMITED_MAX_RESULTS);
		
		// Insertion du filtre sur les élements publiés
		NuxeoQueryFilterContext queryFilter = new NuxeoQueryFilterContext( displayLiveVersion ? NuxeoQueryFilterContext.STATE_LIVE : NuxeoQueryFilterContext.STATE_DEFAULT, portalPolicyFilter);
		
		String filteredRequest = NuxeoQueryFilter.addPublicationFilter(queryFilter, nuxeoRequest);

		
		request.set("query", "SELECT * FROM Document WHERE "  + filteredRequest);
	
		request.setHeader(Constants.HEADER_NX_SCHEMAS, schemas);

		
		return request.execute();
	}

	public String getId() {
		return "ListCommand/"+displayLiveVersion+"/"+pageSize+"/"+pageNumber+"/"+nuxeoRequest;
	};
	


}
