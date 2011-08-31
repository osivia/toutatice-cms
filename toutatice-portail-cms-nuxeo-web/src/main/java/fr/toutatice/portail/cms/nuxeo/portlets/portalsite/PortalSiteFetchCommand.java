package fr.toutatice.portail.cms.nuxeo.portlets.portalsite;

import org.nuxeo.ecm.automation.client.jaxrs.Constants;
import org.nuxeo.ecm.automation.client.jaxrs.Session;
import org.nuxeo.ecm.automation.client.jaxrs.model.Documents;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

public class PortalSiteFetchCommand implements INuxeoCommand {
	
	String path;
	
	public PortalSiteFetchCommand(String path) {
		super();
		this.path = path;
	}
	
	public Object execute( Session session)	throws Exception {
		
		org.nuxeo.ecm.automation.client.jaxrs.model.Document doc = (org.nuxeo.ecm.automation.client.jaxrs.model.Document) session
		.newRequest("Document.Fetch").setHeader(Constants.HEADER_NX_SCHEMAS, "*").set("value", path)
		.execute();
		
		Documents children = (Documents) session.newRequest("Document.GetChildren").setHeader(
                Constants.HEADER_NX_SCHEMAS, "*").setInput(doc).execute();
		
		PortalSiteBean portalSite = new PortalSiteBean(doc, children);
		
		
	    return portalSite;
	
	}

	public String getId() {

		return "PortalSiteFetchCommand" + path;
	};		

}
