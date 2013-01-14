package fr.toutatice.portail.cms.nuxeo.portlets.service;

import org.nuxeo.ecm.automation.client.jaxrs.Constants;
import org.nuxeo.ecm.automation.client.jaxrs.Session;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

public class PublishConfigCommand implements INuxeoCommand {
	
	private String path;

	public PublishConfigCommand(String path) {
		this.path = path;
	}

	public Object execute(Session nuxeoSession) throws Exception {
		return (Document) nuxeoSession.newRequest("Document.Fetch").setHeader(Constants.HEADER_NX_SCHEMAS, "toutatice")
				.set("value", path).execute();
	}

	public String getId() {
		return "PublishConfigCommand-" + path;
	}

}
