package fr.toutatice.portail.cms.nuxeo.portlets.commands;

import org.nuxeo.ecm.automation.client.jaxrs.Constants;
import org.nuxeo.ecm.automation.client.jaxrs.Session;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
import org.nuxeo.ecm.automation.client.jaxrs.model.Documents;

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
