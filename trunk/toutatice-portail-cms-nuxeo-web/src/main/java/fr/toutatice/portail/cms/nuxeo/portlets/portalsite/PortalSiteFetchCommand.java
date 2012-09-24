package fr.toutatice.portail.cms.nuxeo.portlets.portalsite;

import org.nuxeo.ecm.automation.client.jaxrs.Constants;
import org.nuxeo.ecm.automation.client.jaxrs.OperationRequest;
import org.nuxeo.ecm.automation.client.jaxrs.Session;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
import org.nuxeo.ecm.automation.client.jaxrs.model.Documents;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.core.NuxeoQueryFilter;

public class PortalSiteFetchCommand implements INuxeoCommand {
	
	String path;

	
	public PortalSiteFetchCommand(String path) {
		super();
		this.path = path;

	}
	
	public Object execute( Session session)	throws Exception {
		
		Document doc = (org.nuxeo.ecm.automation.client.jaxrs.model.Document) session
		  .newRequest("Document.Fetch").setHeader(Constants.HEADER_NX_SCHEMAS, "*").set("value", path)
		  .execute();
		
	
		OperationRequest request;
		

		request =  session.newRequest("Document.Query");

		String nuxeoRequest = "ecm:parentId = '" + doc.getId()+ "' ORDER BY ecm:pos ";
		
		
		// Insertion du filtre sur les élements publiés
		String filteredRequest = NuxeoQueryFilter.addPublicationFilter(nuxeoRequest, false);

		
		request.set("query", "SELECT * FROM Document WHERE "  + filteredRequest);
	
		request.setHeader(Constants.HEADER_NX_SCHEMAS, "dublincore,common, toutatice");

		Documents children = (Documents) request.execute();	     
				
		PortalSiteBean portalSite = new PortalSiteBean(doc, children);
		
		
	    return portalSite;
	
	}

	public String getId() {

		return "PortalSiteFetchCommand/"+ path;
	};		

}
