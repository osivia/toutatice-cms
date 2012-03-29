package fr.toutatice.portail.core.nuxeo;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderResponse;

import org.nuxeo.ecm.automation.client.jaxrs.model.Document;


import fr.toutatice.portail.api.urls.Link;


public interface INuxeoLinkHandler {
	
	/* Lien par defaut d'accès à un contenu 
	 * (moteur de recherche, lien intercontenus)
	 * */
	public Link getLink( LinkHandlerCtx ctx) throws Exception ;
	
	/* Lien par défaut d'accès au service associé à un contenu
	 * (utilisé depuis de portal site)
	 * */
	 
	//public Link getServiceLink( LinkHandlerCtx ctx) throws Exception ;

	
	/* Lien vers un contenu recontextualise
	 * */
	 
	//public Link getContextualLink( LinkHandlerCtx ctx) throws Exception ;

}
