package fr.toutatice.portail.cms.nuxeo.portlets.portalsite;

import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;

public class PortalSiteBean {
	
	Document portalDocument;
	Documents children;
	public Documents getChildren() {
		return children;
	}
	public Document getPortalDocument() {
		return portalDocument;
	}
	public PortalSiteBean(Document portalDocument, Documents children) {
		super();
		this.portalDocument = portalDocument;
		this.children = children;
	}
	
	

}
