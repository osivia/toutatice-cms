package fr.toutatice.portail.core.nuxeo;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderResponse;

import org.nuxeo.ecm.automation.client.jaxrs.model.Document;


import fr.toutatice.portail.api.urls.Link;


public interface INuxeoLinkHandler {
	
	
	public Link getLink( LinkHandlerCtx ctx) throws Exception ;
	public Link getDirectLink( LinkHandlerCtx ctx) throws Exception ;


}
