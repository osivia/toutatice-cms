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

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.html.HTMLConstants;
import org.osivia.portal.api.windows.PortalWindow;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.IFragmentModule;

public class PropertyFragmentModule implements IFragmentModule {

	private static final String REF_URI = "refURI";

	public void injectViewAttributes(NuxeoController ctx, PortalWindow window,
			PortletRequest request, RenderResponse response) throws Exception {

		String nuxeoPath = null;
		boolean emptyContent = true;

		nuxeoPath = window.getProperty(Constants.WINDOW_PROP_URI);

		if (StringUtils.isNotEmpty(nuxeoPath)) {

			nuxeoPath = ctx.getComputedPath(nuxeoPath);

			Document doc = ctx.fetchDocument(nuxeoPath);

			if (doc.getTitle() != null)
				response.setTitle(doc.getTitle());

			String propertyName = window.getProperty("osivia.propertyName");
			String refURI = window.getProperty("osivia.refURI");

			if (StringUtils.isNotEmpty(propertyName)) {

				Object content = doc.getProperties().get(propertyName);
				
				// Si paramétrage de l'URI, propriétés du fragment attendues dans propertyName
				if (StringUtils.isNotEmpty(refURI)) {

					if (content instanceof PropertyList) {

						PropertyList dataContents = (PropertyList) content;

						if (dataContents != null && dataContents.size() > 0) {
							
							for(int index = 0; index < dataContents.size(); index++) {
								PropertyMap mProperty = dataContents.getMap(index);
								String refURIValue = (String) mProperty
										.get(REF_URI);
								
								if(refURI.equalsIgnoreCase(refURIValue)) {
									content = ((PropertyMap) dataContents
											.getMap(index)).getString("data");
									break;
								}
								content = "";
							}
							
						}
					}
					// Erreur si refUri renseigné et que la valeur n'est pas une propriété complexe
					else 
						content = "Paramétrage fragment incorrect";

				}
				String dataContent = (String) content;

				if (dataContent != null && dataContent.length() > 0) {

					ctx.setCurrentDoc(doc);
					request.setAttribute("doc", doc);
					request.setAttribute("ctx", ctx);
					request.setAttribute("dataContent", dataContent);

					emptyContent = false;
					
					if ("1".equals(window.getProperty("osivia.cms.menu"))) {
						ctx.insertContentMenuBarItems();
					}
				}
			}
		}

		if (emptyContent)
			request.setAttribute("osivia.emptyResponse", "1");

	}

	public void injectAdminAttributes(NuxeoController ctx, PortalWindow window,
			PortletRequest request, RenderResponse response) throws Exception {

		String nuxeoPath = window.getProperty(Constants.WINDOW_PROP_URI);
		if (nuxeoPath == null)
			nuxeoPath = "";
		request.setAttribute("nuxeoPath", nuxeoPath);

		String propertyName = window.getProperty("osivia.propertyName");
		if (propertyName == null)
			propertyName = "";
		request.setAttribute("propertyName", propertyName);

		String scope = window.getProperty("osivia.cms.forcePublicationScope");
        request.setAttribute("scope", scope);


		
		String displayCMSMenu = window.getProperty("osivia.cms.menu");
		if ("1".equals(displayCMSMenu)) {
			request.setAttribute("displayCMSMenu", HTMLConstants.CHECKED);
		}
	}

	public void processAdminAttributes(NuxeoController ctx,
			PortalWindow window, ActionRequest request, ActionResponse res)
			throws Exception {

		if (request.getParameter("nuxeoPath") != null)
			window.setProperty(Constants.WINDOW_PROP_URI,
					request.getParameter("nuxeoPath"));

		if (request.getParameter("propertyName") != null) {
			if (request.getParameter("propertyName").length() > 0)
				window.setProperty("osivia.propertyName",
						request.getParameter("propertyName"));
			else if (window.getProperty("osivia.propertyName") != null)
				window.setProperty("osivia.propertyName", null);
		}

	      if (request.getParameter("scope") != null && request.getParameter("scope").length() > 0)    {
	            window.setProperty("osivia.cms.forcePublicationScope", request.getParameter("scope"));
	        }
	        else if (window.getProperty("osivia.cms.forcePublicationScope") != null)
	            window.setProperty("osivia.cms.forcePublicationScope", null);

		
		// Display CMS menu indicator
		if ("1".equals(request.getParameter("displayCMSMenu"))) {
			window.setProperty("osivia.cms.menu", "1");
		} else if (window.getProperty("osivia.cms.menu") != null) {
			window.setProperty("osivia.cms.menu", null);
		}
	}

}
