/*
 * (C) Copyright 2014 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 *
 *
 */
package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.cms.DocumentType;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.web.IWebIdService;

import fr.toutatice.portail.cms.nuxeo.portlets.commands.CommandConstants;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.DefaultCMSCustomizer;

@Deprecated
public class NavigationItemAdapter {

    private DefaultCMSCustomizer customizer;


    public NavigationItemAdapter(DefaultCMSCustomizer customizer) {
		super();
		this.customizer = customizer;

	};



	/**
	 * Définition d'un template par défaut (uniquement pour les publishSite)
	 *
	 * @param doc
	 * @return
	 */

	protected String getDefaultPageTemplate(Document doc)	{

        return "/default/templates/portalSitePublish";

	}


	/**
	 * Cet element doit-il être géré dans la navigation
	 *
	 * @param doc
	 * @return
	 */

	protected boolean isNavigable(Document doc)	{
		DocumentType cmsItemType = this.customizer.getCMSItemTypes().get(doc.getType());
        return ((cmsItemType != null) && (cmsItemType.isNavigable()));
	}



	/**
     * Cet element doit-il être affiché dans une page (ou par défaut via un player spécifique).
     *
     * @param document Nuxeo document
     * @return
     */
	protected boolean isDisplayedAsAPage(Document document)	{
        String type = document.getType();
        return ("PortalPage".equals(type) || "SimplePage".equals(type) || "Staple".equals(type) || "Folder".equals(type));
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
		DocumentType cmsItemType = this.customizer.getCMSItemTypes().get(doc.getType());

		Map<String, String> properties = publishSpaceNavigationItem.getProperties();

        /* titre */
        String title = (String) doc.getProperties().get("dc:title");
        if (title != null) {
            properties.put("title", title);
        }

		/* Template */

		String pageTemplate =  (String) doc.getProperties().get("ttc:pageTemplate");

		if (StringUtils.isBlank(pageTemplate)) {
            if ((cmsItemType != null) && StringUtils.isNotBlank(cmsItemType.getTemplate())) {
                pageTemplate = cmsItemType.getTemplate();
				properties.put("defaultTemplate", "1");
			} else if (publishSpaceNavigationItem.getPath().equals(publishSpaceItem.getPath())) {
				pageTemplate = this.getDefaultPageTemplate(doc);
				properties.put("defaultTemplate", "1");
            } else if ("Staple".equals(doc.getType())) {
                properties.put("staple", String.valueOf(true));
			}
        } else if (StringUtils.startsWith(pageTemplate, "/templates/") && !StringUtils.startsWith(pageTemplate, "/templates/templates/")) {
            // Compatibilité 4.1 : propriété ttc:pageTemplate relative à l'espace
            pageTemplate = "/default" + pageTemplate;
		}

		if (StringUtils.isNotBlank(pageTemplate)) {
			properties.put("pageTemplate", pageTemplate);
		}


		if (publishSpaceNavigationItem.getPath().equals(publishSpaceItem.getPath())) {
			properties.put("navigationElement", "1");

            properties.put("pageDisplayMode", "1");
		}


        // Theme
        String theme = (String) doc.getProperties().get("ttc:theme");
        if (StringUtils.isNotEmpty(theme)) {
            properties.put("theme", theme);
        }

		/* Template des sous-espaces de navigation de "publishSpaceNavigationItem" */
		String childrenPageTemplate =  (String) doc.getProperties().get("ttc:childrenPageTemplate");
		if(StringUtils.isNotEmpty(childrenPageTemplate)){
			properties.put("childrenPageTemplate", childrenPageTemplate);
		}


		/* scope */

		String pageScope =  (String) doc.getProperties().get("ttc:pageScope");
		if( (pageScope != null) && (pageScope.length() > 0)) {
            properties.put("pageScope", pageScope);
        }


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
		if( (hideInNavigation != null) && "true".equals(hideInNavigation) ) {
            properties.put("hideInNavigation", "1");
        }



		if (this.isNavigable( doc)) {
			if (!"1".equals(properties.get("hideInNavigation")))	{
				properties.put("navigationElement", "1");
			}
		}



		/* Menus */

		String showInMenu =  (String) doc.getProperties().get("ttc:showInMenu");
		if( this.isShowInMenu(doc) || ((showInMenu != null) && "true".equals(showInMenu) )) {
            properties.put("menuItem", "1");
        }




		/*
		 * Affichage en mode page
		 *
		 */


		if (this.isDisplayedAsAPage(doc)) {
			properties.put("pageDisplayMode", "1");
		}

		/*
		 * Contextualisation
		 *
		 */
		if (publishSpaceNavigationItem.getPath().equals(publishSpaceItem.getPath()) && !"PortalSite".equals(doc.getType())) {
			properties.put("contextualizeInternalContents", "1");
			properties.put("contextualizeExternalContents", "1");
		} else {
			String contextualizeInternalContents =  (String) doc.getProperties().get("ttc:contextualizeInternalContents");
			if( (contextualizeInternalContents != null) && "true".equals(contextualizeInternalContents) ) {
	            properties.put("contextualizeInternalContents", "1");
	        }

			String contextualizeExternalContents =  (String) doc.getProperties().get("ttc:contextualizeExternalContents");
			if( (contextualizeExternalContents != null) && "true".equals(contextualizeExternalContents) ) {
	            properties.put("contextualizeExternalContents", "1");
	        }
		}

		/*
		 * Use of ElasticSearch on PublishSpaces
		 * and possibility to comment inside
		 */
		if (this.isCurrentDocPublishSpace(publishSpaceNavigationItem, publishSpaceItem, doc)) {
		    Boolean useES = doc.getProperties().getBoolean("ttc:useES");
		    properties.put("useES", String.valueOf(useES));

		    Boolean spaceCommentable = doc.getProperties().getBoolean("ttcs:spaceCommentable");
		    properties.put("spaceCommentable", String.valueOf(spaceCommentable));
		}


		/* Workspace et UserWorkspaces*/

        if ("Workspace".equals(doc.getType()) || "Room".equals(doc.getType())) {
			properties.put("displayLiveVersion", "1");
            properties.put("partialLoading", "1");
			properties.put("useES", String.valueOf(Boolean.TRUE));
		}

        /* explicitUrl */

        String explicitUrl = (String) doc.getProperties().get("ttc:explicitUrl");
        if (StringUtils.isNotEmpty(explicitUrl)) {
            properties.put(IWebIdService.EXPLICIT_URL, explicitUrl);
        }
        String extensionUrl = (String) doc.getProperties().get("ttc:extensionUrl");
        if (StringUtils.isNotEmpty(extensionUrl)) {
            properties.put(IWebIdService.EXTENSION_URL, extensionUrl);
        }
		/*
		if( publishSpaceItem != null && "Workspace".equals(((Document) publishSpaceItem.getNativeItem()).getType()))	{
			// Tous les sous-items d'un workspace sont navigables
			properties.put("menuItem", "1");
		}
		*/
	}

	/**
	 * @param publishSpaceNavigationItem
	 * @param publishSpaceItem
	 * @param doc
	 * @return true if current document is a publish space.
	 */
	public boolean isCurrentDocPublishSpace(CMSItem publishSpaceNavigationItem, CMSItem publishSpaceItem, Document doc){
	    boolean isPublishSpace = publishSpaceNavigationItem.getPath().equals(publishSpaceItem.getPath());
        isPublishSpace &= (doc.getFacets() != null) && (doc.getFacets().list().contains(CommandConstants.PUBLISH_SPACE_CHARACTERISTIC));
	    return isPublishSpace;
	}

}
