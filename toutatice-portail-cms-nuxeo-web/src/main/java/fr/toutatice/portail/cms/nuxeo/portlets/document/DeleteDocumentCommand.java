package fr.toutatice.portail.cms.nuxeo.portlets.document;

import org.nuxeo.ecm.automation.client.jaxrs.Session;
import org.nuxeo.ecm.automation.client.jaxrs.adapters.DocumentService;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

public class DeleteDocumentCommand implements INuxeoCommand {
	
	private String docId;
	
	public DeleteDocumentCommand(String docId){
		super();
		this.docId = docId;
	}

	public Object execute(Session nuxeoSession) throws Exception {
		DocumentService service = nuxeoSession.getAdapter(DocumentService.class);
		service.remove(docId);
		return null;
	}

	public String getId() {
		return "Document.Remove: " + docId;
	}

}
