package fr.toutatice.portail.core.nuxeo;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderResponse;

import org.nuxeo.ecm.automation.client.jaxrs.model.Document;


import fr.toutatice.portail.api.urls.Link;
import fr.toutatice.portail.core.cms.CMSHandlerProperties;
import fr.toutatice.portail.core.cms.CMSServiceCtx;


public interface INuxeoLinkHandler {
	
	/* Lien par defaut d'accès à un contenu */

	public CMSHandlerProperties getLink( CMSServiceCtx ctx) throws Exception ;
	
	
	/* renvoie un lien si le contenu est affiché directement par le portlet (contextual link, download filecontent) ... 

	et non par une CMSCommand
    */	

	
	public Link getPortletDelegatedLink( CMSServiceCtx ctx) throws Exception ;
	

}
