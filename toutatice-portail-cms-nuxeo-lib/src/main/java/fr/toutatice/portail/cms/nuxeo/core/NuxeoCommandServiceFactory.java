package fr.toutatice.portail.cms.nuxeo.core;

import javax.portlet.PortletContext;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommandService;
import fr.toutatice.portail.cms.nuxeo.jbossportal.NuxeoCommandService;


/**
 * Permet d'executer les commandes Nuxeo
 * 
 * Atention, dans le portlet la méthode destroy doit être surchargée pour nettoyer
 * les ressources du service (et notamment stopper les threads)
 * 
 * 
 * @author jeanseb
 *
 */
public class NuxeoCommandServiceFactory {
	
	public static void startNuxeoCommandService(PortletContext ctx) throws Exception	{
    	INuxeoCommandService nuxeoService =  new NuxeoCommandService();
    	ctx.setAttribute("nuxeoService", nuxeoService);
	}
	
	
	public static INuxeoCommandService getNuxeoCommandService(PortletContext ctx) throws Exception	{
		INuxeoCommandService nuxeoService =  (INuxeoCommandService) ctx.getAttribute("nuxeoService");
		return nuxeoService;
	}
	
	public static void stopNuxeoCommandService(PortletContext ctx) throws Exception	{
		INuxeoCommandService nuxeoService =  (INuxeoCommandService) ctx.getAttribute("nuxeoService");
		
		if( nuxeoService != null)
			nuxeoService.destroy();

	}
	

}
