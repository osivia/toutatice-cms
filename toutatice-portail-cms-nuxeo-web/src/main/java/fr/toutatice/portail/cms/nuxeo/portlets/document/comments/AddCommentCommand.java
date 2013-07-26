package fr.toutatice.portail.cms.nuxeo.portlets.document.comments;

import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

public class AddCommentCommand implements INuxeoCommand {
	
	private Document document;
	private String commentContent;
	
	public AddCommentCommand(Document document, String commentContent){
		this.document = document;
		this.commentContent = commentContent;
	}

	public Object execute(Session nuxeoSession) throws Exception {
		OperationRequest request = nuxeoSession.newRequest("Document.AddComment");
		request.set("commentableDoc", document.getId());
		commentContent = HTMLCommentsTreeBuilder.storeNewLines(commentContent);
		request.set("comment", commentContent);
		return request.execute();
	}

	public String getId() {
		return "Document.AddComment: " + document.getTitle();
	}

}
