package fr.toutatice.portail.cms.nuxeo.portlets.document;

import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

public class DocumentFetchLiveCommand implements INuxeoCommand {
	
	String path;
	String permission;
	
	public DocumentFetchLiveCommand(String path, String permission) {
		super();
		this.path = path;
		this.permission = permission;
	}
	
	public Object execute( Session session)	throws Exception {
		
        Document doc = (Document) session
		.newRequest("Document.FetchLiveDocument").setHeader(Constants.HEADER_NX_SCHEMAS, "*").set("value", path).set("permission", permission)
		.execute();

	     return doc;
	
	}

	public String getId() {
		return "FetchLiveDocument/" + path;
	};		

}
