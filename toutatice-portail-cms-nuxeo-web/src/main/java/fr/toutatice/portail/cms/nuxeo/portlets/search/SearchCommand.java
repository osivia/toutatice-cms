package fr.toutatice.portail.cms.nuxeo.portlets.search;

import org.nuxeo.ecm.automation.client.jaxrs.Constants;
import org.nuxeo.ecm.automation.client.jaxrs.OperationRequest;
import org.nuxeo.ecm.automation.client.jaxrs.Session;
import org.nuxeo.ecm.automation.client.jaxrs.model.Documents;
import org.nuxeo.ecm.automation.client.jaxrs.model.PaginableDocuments;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

public class SearchCommand implements INuxeoCommand{
	
	String path;
	String keywords;
	int pageNumber;
	
	public SearchCommand( String path, String keywords, int pageNumber) {
		super();
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
		
		if( keywords != null)
			searchQuery = addClause(searchQuery, "ecm:fulltext = '"+keywords+"'" );
		
			
		request.set("query", "SELECT * FROM Document " +searchQuery);

		request.set("pageSize", 5);
		request.set("page", pageNumber);
		
		//TODO : dublincore
		
		request.setHeader(Constants.HEADER_NX_SCHEMAS, "dublincore,common");

		PaginableDocuments result = (PaginableDocuments) request.execute();
		
		return result;
/*
		
		OperationRequest request = session.newRequest("Document.Query");
		String searchQuery = "";
		
		if( path != null && path.length() > 0)
			searchQuery = addClause(searchQuery, "ecm:path STARTSWITH '"+path+"'");
		
		if( keywords != null)
			searchQuery = addClause(searchQuery, "ecm:fulltext = '"+keywords+"'" );
		
			
		request.set("query", "SELECT * FROM Document " +searchQuery);
		request.setHeader(Constants.HEADER_NX_SCHEMAS, "*");
		
		Documents results = (Documents) request.execute();
	     return results;
*/	     
	
	}

	public String getId() {

		return "SearchCommand/"+path+"/"+ keywords;
	};		

}
