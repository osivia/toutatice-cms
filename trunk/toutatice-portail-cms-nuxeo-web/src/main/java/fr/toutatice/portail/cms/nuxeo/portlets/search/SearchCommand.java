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
import fr.toutatice.portail.cms.nuxeo.api.NuxeoCompatibility;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilter;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilterContext;
import fr.toutatice.portail.cms.nuxeo.portlets.commands.CommandConstants;
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
		
		
        OperationRequest request;

        if (NuxeoCompatibility.canUseES()) {
            request = generateESRequest(nuxeoSession);
        } else {
            request = generateVCSRequest(nuxeoSession);
        }
		
		String searchQuery = "";
		
		if( path != null && path.length() > 0)
			searchQuery = addClause(searchQuery, "ecm:path STARTSWITH '" + path + "'");

		String searchKeywords = keywords;
		if( searchKeywords == null)
			searchKeywords = "";
		
		searchKeywords += " -noindex";
		
		searchQuery = addClause(searchQuery, "ecm:fulltext = '" + searchKeywords + "'" );
		


		// Insertion du filtre sur les élements publiés
		//String filteredRequest = NuxeoQueryFilter.addPublicationFilter(queryCtx, searchQuery);
		
		// Filter on publish spaces lives.
		String filteredRequest = NuxeoQueryFilter.addSearchFilter(queryCtx, searchQuery);
		request.set("query", "SELECT * FROM Document " + filteredRequest);
		PaginableDocuments result = (PaginableDocuments) request.execute();
		
		return result;

	}
	
	   protected OperationRequest generateESRequest(Session session) throws Exception {
	        OperationRequest request = session.newRequest("Document.QueryES");
	        request.set("pageSize", CommandConstants.PAGE_PROVIDER_DEFAULT_PAGE_SIZE);
	        request.set("currentPageIndex", pageNumber);
	        request.set(Constants.HEADER_NX_SCHEMAS, CMSCustomizer.getSearchSchema());
	        return request;		   
	   }
	   
	    protected OperationRequest generateVCSRequest(Session session) throws Exception {
			OperationRequest request =  session.newRequest("Document.PageProvider");	    	
			request.set("pageSize", CommandConstants.PAGE_PROVIDER_DEFAULT_PAGE_SIZE);
			request.set("page", pageNumber);	
			request.setHeader(Constants.HEADER_NX_SCHEMAS, CMSCustomizer.getSearchSchema());		
			
			if( NuxeoCompatibility.isVersionGreaterOrEqualsThan(NuxeoCompatibility.VERSION_60))
			    request.set("maxResults", CommandConstants.PAGE_PROVIDER_UNLIMITED_MAX_RESULTS);
			
			 return request;		
	    	
	    }

	public String getId() {

		return "SearchCommand"+path+"/"+ keywords;
	};		

}
