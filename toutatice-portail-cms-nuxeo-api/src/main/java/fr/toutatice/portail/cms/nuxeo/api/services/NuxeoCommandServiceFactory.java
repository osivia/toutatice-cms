package fr.toutatice.portail.cms.nuxeo.api.services;

import javax.portlet.PortletContext;



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
    	
    	INuxeoService nuxeoService = (INuxeoService)ctx.getAttribute("NuxeoService");
    	INuxeoCommandService nuxeoCommandService =  nuxeoService.startNuxeoCommandService(ctx);
     	ctx.setAttribute("nuxeoCommandService", nuxeoCommandService);
	}
	
	
	public static INuxeoCommandService getNuxeoCommandService(PortletContext ctx) throws Exception	{
		INuxeoCommandService nuxeoService =  (INuxeoCommandService) ctx.getAttribute("nuxeoCommandService");
		return nuxeoService;
	}
	
	public static void stopNuxeoCommandService(PortletContext ctx) throws Exception	{
		INuxeoCommandService nuxeoService =  (INuxeoCommandService) ctx.getAttribute("nuxeoCommandService");
		
		if( nuxeoService != null)
			nuxeoService.destroy();

	}
	

}
