package fr.toutatice.portail.cms.nuxeo.portlets.document.comments;



import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

public class DeleteCommentCommand implements INuxeoCommand {
	
	private Document document;
	private String commentId;
	
	public DeleteCommentCommand(Document document, String commentId){
		this.document = document;
		this.commentId = commentId;
	}

	public Object execute(Session nuxeoSession) throws Exception {
		OperationRequest request = nuxeoSession.newRequest("Document.DeleteComment");
		request.set("commentableDoc", document.getId());
		request.set("comment", commentId);
		return request.execute();
	}

	public String getId() {
		return "Document.DeleteComment: " + document.getTitle();
	}

}
