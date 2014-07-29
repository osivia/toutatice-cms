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
package fr.toutatice.portail.cms.nuxeo.portlets.search;

import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.PaginableDocuments;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilter;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilterContext;

import fr.toutatice.portail.cms.nuxeo.portlets.customizer.CMSCustomizer;

public class SearchCommand implements INuxeoCommand{
	
	String path;
	String keywords;
	int pageNumber;

	NuxeoQueryFilterContext queryCtx;
	
	public SearchCommand( NuxeoQueryFilterContext queryCtx, String path,String keywords, int pageNumber) {
		super();
        this.queryCtx = queryCtx;
		this.path = path;
		this.keywords = keywords;
		this.pageNumber = pageNumber;

	}
	
	private String addClause(String request, String clause)	{
		String result = request;
		
		if( request.length() == 0)
			result ="WHERE ";
		else
			result += " AND ";
			
		result += clause;
		
		return result;
	}
	
	public Object execute( Session nuxeoSession)	throws Exception {
		
		
		OperationRequest request =  nuxeoSession.newRequest("Document.PageProvider");
		
		String searchQuery = "";
		
		if( path != null && path.length() > 0)
			searchQuery = addClause(searchQuery, "ecm:path STARTSWITH '"+path+"'");

		String searchKeywords = keywords;
		if( searchKeywords == null)
			searchKeywords = "";
		
		searchKeywords += " -noindex";
		
		searchQuery = addClause(searchQuery, "ecm:fulltext = '"+searchKeywords+"'" );
		


		// Insertion du filtre sur les élements publiés
		String filteredRequest = NuxeoQueryFilter.addPublicationFilter(queryCtx, searchQuery);

			
		request.set("query", "SELECT * FROM Document " + filteredRequest);

		request.set("pageSize", 5);
		request.set("page", pageNumber);
		

		request.setHeader(Constants.HEADER_NX_SCHEMAS, CMSCustomizer.getSearchSchema());

		PaginableDocuments result = (PaginableDocuments) request.execute();
		
		return result;

	}

	public String getId() {

		return "SearchCommand"+path+"/"+ keywords;
	};		

}
