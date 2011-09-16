package fr.toutatice.portail.cms.nuxeo.portlets.list;

import org.nuxeo.ecm.automation.client.jaxrs.Constants;
import org.nuxeo.ecm.automation.client.jaxrs.OperationRequest;
import org.nuxeo.ecm.automation.client.jaxrs.Session;
import org.nuxeo.ecm.automation.client.jaxrs.model.Documents;
import org.nuxeo.ecm.automation.client.jaxrs.model.PaginableDocuments;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

public class ListCommand implements INuxeoCommand{
	
	String nuxeoRequest;
	int pageNumber;
	int pageSize;

	
	public ListCommand( String nuxeoRequest, int pageNumber, int pageSize) {
		super();
		this.nuxeoRequest = nuxeoRequest;
		this.pageNumber = pageNumber;
		this.pageSize = pageSize;

	}
	
	public Object execute( Session nuxeoSession)	throws Exception {
		
		OperationRequest request;
		

		request =  nuxeoSession.newRequest("Document.PageProvider");
		request.set("pageSize", pageSize);
		request.set("page", pageNumber);
		
		request.set("query", "SELECT * FROM Document WHERE "  +nuxeoRequest);
	
		request.setHeader(Constants.HEADER_NX_SCHEMAS, "dublincore,common, toutatice");

		
		return request.execute();
	}

	public String getId() {
		return "ListCommand/"+pageSize+"/"+pageNumber+"/"+nuxeoRequest;
	};
	


}
