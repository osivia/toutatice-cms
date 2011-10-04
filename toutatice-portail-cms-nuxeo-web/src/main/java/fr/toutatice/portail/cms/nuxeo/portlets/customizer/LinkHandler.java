package fr.toutatice.portail.cms.nuxeo.portlets.customizer;

import javax.portlet.PortletContext;

import org.nuxeo.ecm.automation.client.jaxrs.model.Document;

import fr.toutatice.portail.api.urls.Link;
import fr.toutatice.portail.core.nuxeo.LinkHandlerCtx;

public class LinkHandler extends DefaultLinkHandler {
	
	public LinkHandler(PortletContext ctx) {
		super( ctx);
	
	}	

	public Link getLink(LinkHandlerCtx ctx)	throws Exception  {
		
		return super.getLink(ctx);
	}
}
