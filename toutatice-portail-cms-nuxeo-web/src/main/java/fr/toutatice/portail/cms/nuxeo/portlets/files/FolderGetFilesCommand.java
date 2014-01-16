package fr.toutatice.portail.cms.nuxeo.portlets.files;

import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Documents;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.core.NuxeoQueryFilter;

public class FolderGetFilesCommand implements INuxeoCommand {
	
	String folderId;
	String folderPath;
	boolean displayLiveVersion;	

	
	public FolderGetFilesCommand(String folderPath, String folderId,  boolean displayLiveVersion) {
		super();
		this.folderId = folderId;
		this.folderPath = folderPath;
		this.displayLiveVersion = displayLiveVersion;
		
	}
	
	public Object execute( Session session)	throws Exception {

			 OperationRequest request =  session.newRequest("Document.Query");

        // v2.0.9 : On reaffiche tous les folders
        // TODO: déporter le filtre sur les types dans le Customizer
			String nuxeoRequest = "ecm:parentId = '" + folderId + "' ";
			nuxeoRequest += " AND  (ecm:primaryType != 'Workspace' AND ecm:primaryType != 'WorkspaceRoot' AND ecm:primaryType != 'PortalSite') ";
			nuxeoRequest += " ORDER BY ecm:pos ";
						
			// Insertion du filtre sur les élements publiés
			String filteredRequest = NuxeoQueryFilter.addPublicationFilter(nuxeoRequest, displayLiveVersion);
			
			request.set("query", "SELECT * FROM Document WHERE "  + filteredRequest);	
			request.setHeader(Constants.HEADER_NX_SCHEMAS, "dublincore,common, toutatice, file");

			Documents children = (Documents) request.execute();	     
			
			return children;
	     
	}

	public String getId() {
		return "FolderGetFilesCommand/" + displayLiveVersion + "/" +folderPath;
	};		

}
