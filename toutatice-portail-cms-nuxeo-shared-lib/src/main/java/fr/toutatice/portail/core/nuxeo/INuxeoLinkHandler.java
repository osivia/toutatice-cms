package fr.toutatice.portail.core.nuxeo;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderResponse;

import org.nuxeo.ecm.automation.client.jaxrs.model.Document;


import fr.toutatice.portail.api.urls.Link;


public interface INuxeoLinkHandler {
	
	/* Lien par defaut d'accès à un contenu 
	 * (moteur de recherche)
	 * */
	public Link getLink( LinkHandlerCtx ctx) throws Exception ;
	
	/* Lien par défaut d'accès au service associé à un contenu
	 * (utilisé depuis de portal site, liens inter-contenus)
	 * */
	 
	public Link getServiceLink( LinkHandlerCtx ctx) throws Exception ;


}
