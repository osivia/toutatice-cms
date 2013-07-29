package fr.toutatice.portail.cms.nuxeo.portlets.files;

import org.nuxeo.ecm.automation.client.jaxrs.Constants;
import org.nuxeo.ecm.automation.client.jaxrs.OperationRequest;
import org.nuxeo.ecm.automation.client.jaxrs.Session;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
import org.nuxeo.ecm.automation.client.jaxrs.model.Documents;
import org.osivia.portal.core.constants.InternalConstants;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilter;


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
			
			
			
			// Insertion du filtre sur les élements publiés
			String filteredRequest = NuxeoQueryFilter.addPublicationFilter(nuxeoRequest, displayLiveVersion, InternalConstants.PORTAL_CMS_REQUEST_FILTERING_POLICY_NO_FILTER);

			
			request.set("query", "SELECT * FROM Document WHERE "  + filteredRequest);
		
			request.setHeader(Constants.HEADER_NX_SCHEMAS, "dublincore,common, toutatice, file");

			Documents children = (Documents) request.execute();	     
			
			return children;
	     
	}

	public String getId() {
		return "FolderGetFilesCommand/" + displayLiveVersion + "/" +folderPath;
	};		

}
