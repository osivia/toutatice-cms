package fr.toutatice.portail.cms.nuxeo.portlets.commands;

import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

public class FolderGetParentCommand implements INuxeoCommand {
	
	Document folder;
	
	public FolderGetParentCommand(Document folder) {
		super();
		this.folder = folder;
	}
	
	public Object execute( Session session)	throws Exception {
		
  	 Document parent = (Document) session.newRequest("Document.GetParent").setHeader(
                Constants.HEADER_NX_SCHEMAS, "*").setInput(folder).execute();
	
      return parent;
	}

	public String getId() {
		return "FolderGetParentCommand/" + folder.getPath();
	};		

}
