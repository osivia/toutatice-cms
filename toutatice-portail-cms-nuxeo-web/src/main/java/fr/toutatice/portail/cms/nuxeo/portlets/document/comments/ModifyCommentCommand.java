package fr.toutatice.portail.cms.nuxeo.portlets.document.comments;


import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.adapters.DocumentService;
import org.nuxeo.ecm.automation.client.model.DocRef;
import org.nuxeo.ecm.automation.client.model.IdRef;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

public class ModifyCommentCommand implements INuxeoCommand {
	
	private String commentId;
	private String content;
	
	public ModifyCommentCommand(String commentId, String content){
		this.commentId = commentId;
		this.content = content;
	}

	public Object execute(Session nuxeoSession) throws Exception {
		DocumentService service = nuxeoSession.getAdapter(DocumentService.class);
		DocRef commentRef = new IdRef(commentId);
		service.setProperty(commentRef, "comment:text", content);
		return commentRef;
	}

	public String getId() {
		return "Upadate.Comment: " + commentId;
	}

}
