package fr.toutatice.portail.core.nuxeo;

import java.util.List;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderResponse;

import org.nuxeo.ecm.automation.client.jaxrs.model.Document;


import fr.toutatice.portail.api.menubar.MenubarItem;
import fr.toutatice.portail.api.urls.Link;
import fr.toutatice.portail.core.cms.CMSHandlerProperties;
import fr.toutatice.portail.core.cms.CMSItem;
import fr.toutatice.portail.core.cms.CMSPage;
import fr.toutatice.portail.core.cms.CMSServiceCtx;
import fr.toutatice.portail.core.dynamic.DynamicPageBean;


public interface INuxeoCustomizer {
	
	/* Lien par defaut d'accès à un contenu */

	public CMSHandlerProperties getCMSPlayer( CMSServiceCtx ctx) throws Exception ;
	
	
	/* renvoie un lien si le contenu est affiché directement par le portlet (contextual link, download filecontent) ... 

	et non par une CMSCommand
    */	

	
	public Link createCustomLink( CMSServiceCtx ctx) throws Exception ;
	
	
	/* Barre de menu des portlets d'affichage de contenu */

	
	public void formatContentMenuBar(CMSServiceCtx ctx) throws Exception ;
	
	
	
	/* Calcul des pages au login de l'utilisateur */
	
	public List<CMSPage> computeUserPreloadedPages(CMSServiceCtx cmsCtx)  throws Exception ;
	
	


}
