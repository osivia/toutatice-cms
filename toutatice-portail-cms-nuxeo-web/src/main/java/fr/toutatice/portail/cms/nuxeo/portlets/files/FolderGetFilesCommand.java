package fr.toutatice.portail.cms.nuxeo.portlets.files;

import org.nuxeo.ecm.automation.client.jaxrs.Constants;
import org.nuxeo.ecm.automation.client.jaxrs.Session;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
import org.nuxeo.ecm.automation.client.jaxrs.model.Documents;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

public class FolderGetFilesCommand implements INuxeoCommand {
	
	Document folder;
	
	public FolderGetFilesCommand(Document folder) {
		super();
		this.folder = folder;
	}
	
	public Object execute( Session session)	throws Exception {
		
		Documents children = (Documents) session.newRequest("Document.GetChildren").setHeader(
                Constants.HEADER_NX_SCHEMAS, "*").setInput(folder).execute();
		
	
	     return children;
	}

	public String getId() {
		return "FolderGetFilesCommand" + folder.getPath();
	};		

}
