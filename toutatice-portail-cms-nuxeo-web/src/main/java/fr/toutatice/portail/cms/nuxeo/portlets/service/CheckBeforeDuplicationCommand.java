package fr.toutatice.portail.cms.nuxeo.portlets.service;

import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Documents;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilter;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilterContext;

public class CheckBeforeDuplicationCommand implements INuxeoCommand {

	private String sourcePath;
	
	public CheckBeforeDuplicationCommand(String sourcePath) {
		this.sourcePath = sourcePath;
	
	}

	@Override
	public Object execute(Session nuxeoSession) throws Exception {
		
		OperationRequest request;
		
		request =  nuxeoSession.newRequest("Document.Query");
		
        String nuxeoRequest = "ecm:path STARTSWITH '" + sourcePath + "' AND ecm:path <> '"+sourcePath+"' AND ecm:primaryType = 'PortalPage'";
		

		// Insertion du filtre sur les élements publiés
        NuxeoQueryFilterContext queryFilter = new NuxeoQueryFilterContext( NuxeoQueryFilterContext.STATE_LIVE, "global" );

        
		String filteredRequest = NuxeoQueryFilter.addPublicationFilter(queryFilter, nuxeoRequest);
		
		request.set("query", "SELECT * FROM Document WHERE "  + filteredRequest);
		
		// On récupère seulement le schéma global et celui du contextualLink (pour l'url).
        request.setHeader(Constants.HEADER_NX_SCHEMAS, "dublincore");

		Documents children = (Documents) request.execute();	
		
        return children;
	}

	@Override
	public String getId() {
		return null;
	}

}
