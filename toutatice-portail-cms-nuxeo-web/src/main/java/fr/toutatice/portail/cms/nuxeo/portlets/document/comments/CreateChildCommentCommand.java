package fr.toutatice.portail.cms.nuxeo.portlets.document.comments;

import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

public class CreateChildCommentCommand implements INuxeoCommand {
	
	private Document document;
	private String commentId;
	private String childCommentContent;
	
	public CreateChildCommentCommand(Document document, String commentId, String childCommentContent){
		this.document = document;
		this.commentId = commentId;
		this.childCommentContent = childCommentContent;
	}

	public Object execute(Session nuxeoSession) throws Exception {
		OperationRequest request = nuxeoSession.newRequest("Document.CreateChildComment");
		request.set("commentableDoc", document.getId());
		request.set("comment", commentId);
		childCommentContent = HTMLCommentsTreeBuilder.storeNewLines(childCommentContent);
		request.set("childComment", childCommentContent);
		return request.execute();
	}

	public String getId() {
		return "Document.CreateChildComment: " + document.getTitle();
	}

}
