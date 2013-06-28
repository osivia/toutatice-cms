package fr.toutatice.portail.core.nuxeo;

import java.util.Map;

import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.urls.Link;
import org.osivia.portal.core.cms.CMSHandlerProperties;
import org.osivia.portal.core.cms.CMSServiceCtx;




public interface INuxeoCustomizer {
	
	/* Lien par defaut d'accès à un contenu */

	public CMSHandlerProperties getCMSPlayer( CMSServiceCtx ctx) throws Exception ;
	
	
	/* renvoie un lien si le contenu est affiché directement par le portlet (contextual link, download filecontent) ... 

	et non par une CMSCommand
    */	

	
	public Link createCustomLink( CMSServiceCtx ctx) throws Exception ;
	
	
	/* Barre de menu des portlets d'affichage de contenu */

	
	public void formatContentMenuBar(CMSServiceCtx ctx) throws Exception ;
	
	
	/* Adaptation des items CMS */
	
	public Map<String, String> getDocumentConfiguration(CMSServiceCtx ctx, Document doc) throws Exception ;
	
	
}
