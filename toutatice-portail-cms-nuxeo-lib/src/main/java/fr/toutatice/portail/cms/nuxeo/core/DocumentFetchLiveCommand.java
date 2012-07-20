package fr.toutatice.portail.cms.nuxeo.core;

import org.nuxeo.ecm.automation.client.jaxrs.Constants;
import org.nuxeo.ecm.automation.client.jaxrs.Session;

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
		
		org.nuxeo.ecm.automation.client.jaxrs.model.Document doc = (org.nuxeo.ecm.automation.client.jaxrs.model.Document) session
		.newRequest("Document.FetchLiveDocument").setHeader(Constants.HEADER_NX_SCHEMAS, "*").set("value", path).set("permission", permission)
		.execute();

	     return doc;
	
	}

	public String getId() {
		return "FetchLiveDocument/" + path;
	};		

}
