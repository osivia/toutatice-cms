package fr.toutatice.portail.cms.nuxeo.portlets.commands;

import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

public class FolderGetChildrenCommand implements INuxeoCommand {
	
	Document folder;
	
	public FolderGetChildrenCommand(Document folder) {
		super();
		this.folder = folder;
	}
	
	public Object execute( Session session)	throws Exception {
		
		Documents children = (Documents) session.newRequest("Document.GetChildren").setHeader(
                Constants.HEADER_NX_SCHEMAS, "*").setInput(folder).execute();
		
	
	     return children;
	}

	public String getId() {
		return "FolderGetChildrenCommand/" + folder.getPath();
	};		

}
