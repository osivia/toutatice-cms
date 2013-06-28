package fr.toutatice.portail.cms.nuxeo.portlets.search;

import org.nuxeo.ecm.automation.client.jaxrs.Constants;
import org.nuxeo.ecm.automation.client.jaxrs.OperationRequest;
import org.nuxeo.ecm.automation.client.jaxrs.Session;
import org.nuxeo.ecm.automation.client.jaxrs.model.Documents;
import org.nuxeo.ecm.automation.client.jaxrs.model.PaginableDocuments;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.core.NuxeoQueryFilter;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.CMSCustomizer;

public class SearchCommand implements INuxeoCommand{
	
	String path;
	String keywords;
	int pageNumber;
	boolean displayLiveVersion;
	
	public SearchCommand( String path, boolean displayLiveVersion, String keywords, int pageNumber) {
		super();
		this.path = path;
		this.keywords = keywords;
		this.pageNumber = pageNumber;
		this.displayLiveVersion = displayLiveVersion;
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
		String filteredRequest = NuxeoQueryFilter.addPublicationFilter(searchQuery, displayLiveVersion);

			
		request.set("query", "SELECT * FROM Document " + filteredRequest);

		request.set("pageSize", 5);
		request.set("page", pageNumber);
		

		request.setHeader(Constants.HEADER_NX_SCHEMAS, CMSCustomizer.getSearchSchema());

		PaginableDocuments result = (PaginableDocuments) request.execute();
		
		return result;

	}

	public String getId() {

		return "SearchCommand/"+displayLiveVersion+"/"+path+"/"+ keywords;
	};		

}
