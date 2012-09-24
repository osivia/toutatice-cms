package fr.toutatice.portail.cms.nuxeo.portlets.customizer;

import javax.portlet.PortletContext;

import fr.toutatice.portail.core.cms.CMSHandlerProperties;
import fr.toutatice.portail.core.cms.CMSServiceCtx;


/**
 * Ce customizer permet de définir : 
 * 
 *    de nouveaux templates de listes
 *    le schéma du moteur de recherche
 *    les templates de contenu
 * 
 * Le template d'affichage par défaut est WEB-INF/jsp/liste/view-[nom-du-template].jsp
 * 
 * @author jeanseb
 *
 */
public class CMSCustomizer extends DefaultCMSCustomizer {
	
	public CMSCustomizer(PortletContext ctx) {
		super( ctx);
	
	}



}
