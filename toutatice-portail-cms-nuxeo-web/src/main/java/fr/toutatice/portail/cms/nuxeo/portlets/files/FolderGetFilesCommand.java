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
		/*
		Documents children = (Documents) session.newRequest("Document.GetChildren").setHeader(
                Constants.HEADER_NX_SCHEMAS, "*").setInput(folder).execute();
		*/
	
	     
			OperationRequest request;
			

			request =  session.newRequest("Document.Query");

			String nuxeoRequest = "ecm:parentId = '" + folderId + "' ORDER BY ecm:pos ";

// v2.0.9 : On reaffiche tous les folders			
//			String nuxeoRequest = "ecm:parentId = '" + folderId + "' ";
//			if( navigationFilter){
//				nuxeoRequest += " AND  (ecm:mixinType != 'Folderish' OR ttc:showInMenu = 1) ";
//			}
//			nuxeoRequest += " ORDER BY ecm:pos ";
			
			
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
