package fr.toutatice.portail.cms.nuxeo.portlets.list;

import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.core.NuxeoQueryFilter;

public class ListCommand implements INuxeoCommand {
	
	String nuxeoRequest;
	int pageNumber;
	int pageSize;
	String schemas;
	boolean displayLiveVersion;

	
	public ListCommand( String nuxeoRequest, boolean displayLiveVersion, int pageNumber, int pageSize, String schemas) {
		super();
		this.nuxeoRequest = nuxeoRequest;
		this.displayLiveVersion = displayLiveVersion;
		this.pageNumber = pageNumber;
		this.pageSize = pageSize;
		this.schemas = schemas;

	}
	
	public Object execute( Session nuxeoSession)	throws Exception {
		
		OperationRequest request;
		

		request =  nuxeoSession.newRequest("Document.PageProvider");
		request.set("pageSize", pageSize);
		request.set("page", pageNumber);
		
		// Insertion du filtre sur les élements publiés
		String filteredRequest = NuxeoQueryFilter.addPublicationFilter(nuxeoRequest, displayLiveVersion);

		
		request.set("query", "SELECT * FROM Document WHERE "  + filteredRequest);
	
		request.setHeader(Constants.HEADER_NX_SCHEMAS, schemas);

		
		return request.execute();
	}

	public String getId() {
		return "ListCommand/"+displayLiveVersion+"/"+pageSize+"/"+pageNumber+"/"+nuxeoRequest;
	};
	


}
