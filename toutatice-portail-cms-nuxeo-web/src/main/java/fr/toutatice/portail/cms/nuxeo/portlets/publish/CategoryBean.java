package fr.toutatice.portail.cms.nuxeo.portlets.publish;

import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
import org.nuxeo.ecm.automation.client.jaxrs.model.Documents;

public class CategoryBean {
	
	Document portalDocument;
	Documents children;
	public Documents getChildren() {
		return children;
	}
	public Document getPortalDocument() {
		return portalDocument;
	}
	public CategoryBean(Document portalDocument, Documents children) {
		super();
		this.portalDocument = portalDocument;
		this.children = children;
	}
	
	

}
