package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import java.util.Map;

import javax.portlet.PortletContext;

import org.nuxeo.ecm.automation.client.jaxrs.model.Document;

import fr.toutatice.portail.cms.nuxeo.portlets.customizer.DefaultCMSCustomizer;
import fr.toutatice.portail.cms.nuxeo.portlets.service.CMSService;
import fr.toutatice.portail.core.cms.CMSItem;

public class NavigationItemAdaptor {
	
	CMSService CMSService;
	DefaultCMSCustomizer customizer;
	PortletContext portletCtx;
	
	public NavigationItemAdaptor(PortletContext portletCtx, DefaultCMSCustomizer customizer, CMSService cmsService) {
		super();
		CMSService = cmsService;
		this.portletCtx = portletCtx;
		this.customizer = customizer;

	};
	
	
	
	/**
	 * Définition d'un template par défaut (uniquement pour les publishSite)
	 * 
	 * @param doc
	 * @return
	 */
	
	protected String getDefaultPageTemplate(Document doc)	{
		
		return  "/default/templates/BLOG_TMPL1";
		
	}
	
	
	/**
	 * Cet element doit-il être géré dans la navigation
	 * 
	 * @param doc
	 * @return
	 */
	
	protected boolean isNavigable(Document doc)	{
		
		if (doc.getType().equals("PortalPage") || (doc.getType().equals("Folder"))	|| ((doc.getType().equals("OrderedFolder")))  || ((doc.getType().equals("DocumentUrlContainer")))  || ((doc.getType().equals("AnnonceFolder")))  )
			return true;
		
		return false;
	}
	
	
	
	/**
	 * Cet element doit-il être affiché dans une page (ou par défaut via un player spécifique)
	 * 
	 * @param doc
	 * @return
	 */
	protected boolean isDisplayedAsAPage(Document doc)	{
		
		if (doc.getType().equals("PortalPage")) 
			return true;
		
		return false;
	}
	
	

	/*
	 * Personnalisation des propriétés des éléments de publishSpace
	 * 
	 * - pageTemplate par défaut 
	 * - navigationElement : les navigationElements sont considérés comme des rubriques de publication. La navigation à travers ces éléments est gérée par le portail
	 *   (breacrumb, menu de publication)
	 *   A l'intérieur des navigationElement, il y des documents dont l'affichage et la navigation sont gérés directement par les portlets (ex: faq, forums, ...) 
	 * - menuItem
	 * - hiddenInNavgigation
	 * - pageDisplayMode
	 */

	public void adaptPublishSpaceNavigationItem(CMSItem publishSpaceNavigationItem, CMSItem publishSpaceItem) {

		Document doc = (Document) publishSpaceNavigationItem.getNativeItem();
		
		Map<String, String> properties = publishSpaceNavigationItem.getProperties();
		

		/* Template */
		
		String pageTemplate =  (String) doc.getProperties().get("ttc:pageTemplate");
		
		if (doc.getPath().equals(publishSpaceItem.getPath())) {
			
			// template par défaut pour le publishspace
			
			if( pageTemplate == null || pageTemplate.length() == 0)
				pageTemplate =  getDefaultPageTemplate(doc);
			
			
			properties.put("navigationElement", "1");
			
			
			
		}
		
		if( pageTemplate != null && pageTemplate.length() > 0)
		
			properties.put("pageTemplate", pageTemplate);

			
		/* scope */

		String pageScope =  (String) doc.getProperties().get("ttc:pageScope");
		if( pageScope != null && pageScope.length() > 0)
			properties.put("pageScope", pageScope);
		
	
		/* 
		 * 
		 * Navigation
		 * 
		 *  - hiddenInNavigation
		 *  - navigationElement
		 *  - menuItem
		 *  
		 *  */
		
		String hiddenInNavigation =  (String) doc.getProperties().get("ttc:hiddenInNavigation");
		if( hiddenInNavigation != null && "true".equals(hiddenInNavigation) )
			properties.put("hiddenInNavigation", "1");			
		
	
			
		if (isNavigable( doc)) {
			if (!"1".equals(properties.get("hiddenInNavigation")))	{
				properties.put("navigationElement", "1");
			}
		}
		
		
		
		/* Menus */
		
		String showInMenu =  (String) doc.getProperties().get("ttc:showInMenu");
		if( showInMenu != null && "true".equals(showInMenu) )
			properties.put("menuItem", "1");
		
		if( "1".equals(properties.get("navigationElement")))	{
			properties.put("menuItem", "1");
		}
		
		
		/*
		 * Affichage en mode page
		 * 
		 */
		
	
		if (isDisplayedAsAPage(doc)) {
			properties.put("pageDisplayMode", "1");
		}
		
		/*
		 * Contextualisation
		 * 
		 */
		
		String contextualizeInternalContents =  (String) doc.getProperties().get("ttc:contextualizeInternalContents");
		if( contextualizeInternalContents != null && "true".equals(contextualizeInternalContents) )
			properties.put("contextualizeInternalContents", "1");
		
		String contextualizeExternalContents =  (String) doc.getProperties().get("ttc:contextualizeExternalContents");
		if( contextualizeExternalContents != null && "true".equals(contextualizeExternalContents) )
			properties.put("contextualizeExternalContents", "1");		

		

	}

}
