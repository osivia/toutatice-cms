package fr.toutatice.portail.cms.nuxeo.portlets.service;

import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

public class DuplicateFragmentCommand implements INuxeoCommand {

	private Document doc;
	private String refURI;

	public DuplicateFragmentCommand(Document doc, String refURI) {
		this.doc = doc;
		this.refURI = refURI;
	}

	@Override
	public Object execute(Session nuxeoSession) throws Exception {

		OperationRequest request = nuxeoSession.newRequest("Document.DuplicateEditableWindow").setInput(doc);
		request.set("fromUri", refURI);
		
		return nuxeoSession.execute(request);
	}

	@Override
	public String getId() {
		return null;
	}

}
