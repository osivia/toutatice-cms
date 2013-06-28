package fr.toutatice.portail.cms.nuxeo.core;

import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.Session;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

public class DocumentFetchPublishedCommand implements INuxeoCommand {
	
	String path;
	
	public DocumentFetchPublishedCommand(String path) {
		super();
		this.path = path;
	}
	
	public Object execute( Session session)	throws Exception {
		
		org.nuxeo.ecm.automation.client.model.Document doc = (org.nuxeo.ecm.automation.client.model.Document) session
		.newRequest("Document.FetchPublished").setHeader(Constants.HEADER_NX_SCHEMAS, "*").set("value", path)
		.execute();

	     return doc;
	
	}

	public String getId() {
		return "DocumentFetchPublishedCommand/" + path;
	};		

}
