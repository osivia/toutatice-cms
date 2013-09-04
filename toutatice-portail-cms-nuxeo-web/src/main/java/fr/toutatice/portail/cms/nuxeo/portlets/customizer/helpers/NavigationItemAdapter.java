package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import java.util.Map;

import javax.portlet.PortletContext;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.core.cms.CMSItem;

import fr.toutatice.portail.cms.nuxeo.portlets.customizer.DefaultCMSCustomizer;
import fr.toutatice.portail.cms.nuxeo.portlets.service.CMSService;

public class NavigationItemAdapter {
	
	CMSService CMSService;
	DefaultCMSCustomizer customizer;
	PortletContext portletCtx;
	
	public NavigationItemAdapter(PortletContext portletCtx, DefaultCMSCustomizer customizer, CMSService cmsService) {
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
		
		if (doc.getType().equals("PortalPage") || (doc.getType().equals("Folder"))	|| ((doc.getType().equals("OrderedFolder")))  || ((doc.getType().equals("DocumentUrlContainer")))  || ((doc.getType().equals("AnnonceFolder"))) || ((doc.getType().equals("PortalVirtualPage"))) 
				||(doc.getType().equals("SimplePage")))
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
		
		if (doc.getType().equals("PortalPage") ||(doc.getType().equals("SimplePage"))) 
			return true;
		
		return false;
	}
	
	
	/**
	 * Cet element doit-il être affiché systématiquement dans le menu
	 * 
	 * @param doc
	 * @return
	 */
	protected boolean isShowInMenu(Document doc)	{
		
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
		
        /* titre */
        String title = (String) doc.getProperties().get("dc:title");
        if (title != null) {
            properties.put("title", title);
        }

		/* Template */
		
		String pageTemplate =  (String) doc.getProperties().get("ttc:pageTemplate");
		boolean defaultTemplate = false;
		
		if (publishSpaceNavigationItem.getPath().equals(publishSpaceItem.getPath())) {
			
			// template par défaut pour le publishspace
			
			if( pageTemplate == null || pageTemplate.length() == 0)	{
				pageTemplate =  getDefaultPageTemplate(doc);
				defaultTemplate = true;
			}
			
			
			properties.put("navigationElement", "1");
            properties.put("pageDisplayMode", "1");
			
			
		}
		
		if( pageTemplate != null && pageTemplate.length() > 0)	{
			if( defaultTemplate)
				properties.put("defaultTemplate", "1");

			properties.put("pageTemplate", pageTemplate);
		}
		
		/* Template des sous-espaces de navigation de "publishSpaceNavigationItem" */
		String childrenPageTemplate =  (String) doc.getProperties().get("ttc:childrenPageTemplate");
		if(StringUtils.isNotEmpty(childrenPageTemplate)){
			properties.put("childrenPageTemplate", childrenPageTemplate);
		}

			
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
		
		
		// Désactivation du hideInNavigation
		

		String hideInNavigation =  (String) doc.getProperties().get("ttc:hideInNavigation");
		if( hideInNavigation != null && "true".equals(hideInNavigation) )
			properties.put("hideInNavigation", "1");			

	
			
		if (isNavigable( doc)) {
			if (!"1".equals(properties.get("hideInNavigation")))	{
				properties.put("navigationElement", "1");
			}
		}
		
		
		
		/* Menus */
		
		String showInMenu =  (String) doc.getProperties().get("ttc:showInMenu");
		if( isShowInMenu(doc) || (showInMenu != null && "true".equals(showInMenu) ))
			properties.put("menuItem", "1");
		
	
		
		
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
		
		
	/* Workspace et UserWorkspaces*/
		
		if("Workspace".equals(doc.getType()))	{
			properties.put("contextualizeInternalContents", "1");
			properties.put("contextualizeExternalContents", "1");
			if(StringUtils.isEmpty(pageTemplate))
				properties.put("pageTemplate", "/default/templates/workspace");
			properties.put("displayLiveVersion", "1");
		}
		
		if("UserWorkspace".equals(doc.getType()))	{
			properties.put("contextualizeInternalContents", "1");
			properties.put("contextualizeExternalContents", "1");
			properties.put("pageTemplate", "/default/templates/userworkspace");
			properties.put("displayLiveVersion", "1");
		}


		if("Workspace".equals(doc.getType()))	{
			properties.put("partialLoading", "1");
		}

		
		if(("WebSite".equals(doc.getType())) || ("BlogSite".equals(doc.getType()))){
			properties.put("contextualizeInternalContents", "1");
			properties.put("contextualizeExternalContents", "1");
		}
		
		
		/*
		if( publishSpaceItem != null && "Workspace".equals(((Document) publishSpaceItem.getNativeItem()).getType()))	{
			// Tous les sous-items d'un workspace sont navigables
			properties.put("menuItem", "1");
		}
		*/
	}

}
