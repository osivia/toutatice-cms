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
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.windows.PortalWindow;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.IFragmentModule;

public class DocumentPictureFragmentModule implements IFragmentModule {

	
	public void injectViewAttributes(NuxeoController ctx, PortalWindow window, PortletRequest request, RenderResponse response)
			throws Exception {


	
		String nuxeoPath = null;
		boolean emptyContent = true;

		nuxeoPath = window.getProperty(Constants.WINDOW_PROP_URI);

		if (StringUtils.isNotEmpty(nuxeoPath)) {

			nuxeoPath = ctx.getComputedPath(nuxeoPath);

			Document doc = ctx.fetchDocument(nuxeoPath);

			if (doc.getTitle() != null)
				response.setTitle(doc.getTitle());

			String propertyName = window.getProperty("osivia.propertyName");

			if (StringUtils.isNotEmpty(propertyName)) {

			
				PropertyMap map = doc.getProperties().getMap(propertyName);
				
				if( map != null)	{

				String pathFile = map.getString("data");

				if (pathFile != null) {
					
					ctx.setCurrentDoc(doc);
					
					request.setAttribute("pictureDocument", doc);
					request.setAttribute("ctx", ctx);
					request.setAttribute("propertyName", window.getProperty("osivia.propertyName"));
					
	                   
                    String targetPath =  window.getProperty("osivia.targetPath");
                    
                    if( targetPath != null)    {
                        targetPath = ctx.getComputedPath(targetPath);
                        request.setAttribute("targetPath", targetPath);
                    }
                    


					emptyContent = false;
				}
			}
			}
		}

		if (emptyContent)
			request.setAttribute("osivia.emptyResponse", "1");
		
	}

	public void injectAdminAttributes(NuxeoController ctx, PortalWindow window, PortletRequest request, RenderResponse response)
            throws Exception {

        String nuxeoPath = window.getProperty("osivia.nuxeoPath");
        if (nuxeoPath == null)
            nuxeoPath = "";
        request.setAttribute("nuxeoPath", nuxeoPath);

        String propertyName = window.getProperty("osivia.propertyName");
        if (propertyName == null)
            propertyName = "";
        request.setAttribute("propertyName", propertyName);


          // 2.0.22 : ajout scope 
        String scope = window.getProperty("osivia.cms.forcePublicationScope");
        request.setAttribute("scope", scope);

        
        String displayLiveVersion = window.getProperty("osivia.cms.displayLiveVersion");
        if (displayLiveVersion == null)
            displayLiveVersion = "";
        request.setAttribute("displayLiveVersion", displayLiveVersion);
        
        
        String targetPath = window.getProperty("osivia.targetPath");
        if (targetPath == null)
            targetPath = "";
        request.setAttribute("targetPath", targetPath);


    }

    public void processAdminAttributes(NuxeoController ctx, PortalWindow window, ActionRequest request, ActionResponse res)
            throws Exception {


        if (request.getParameter("nuxeoPath") != null) {
                if (request.getParameter("nuxeoPath").length() > 0)
                    window.setProperty("osivia.nuxeoPath", request.getParameter("nuxeoPath"));
                else if (window.getProperty("osivia.nuxeoPath") != null)
                    window.setProperty("osivia.nuxeoPath", null);
        }   
        
        
        if (request.getParameter("propertyName") != null) {
            if (request.getParameter("propertyName").length() > 0)
                window.setProperty("osivia.propertyName", request.getParameter("propertyName"));
            else if (window.getProperty("osivia.propertyName") != null)
                window.setProperty("osivia.propertyName", null);
        }
        
          // 2.0.22 : ajout scope 
        if (request.getParameter("scope") != null && request.getParameter("scope").length() > 0)    {
            window.setProperty("osivia.cms.forcePublicationScope", request.getParameter("scope"));
        }
        else if (window.getProperty("osivia.cms.forcePublicationScope") != null)
            window.setProperty("osivia.cms.forcePublicationScope", null);


        if (request.getParameter("displayLiveVersion") != null) {

            if ("1".equals(request.getParameter("displayLiveVersion")))
                window.setProperty("osivia.cms.displayLiveVersion", "1");
            else if (window.getProperty("osivia.cms.displayLiveVersion") != null)
                window.setProperty("osivia.cms.displayLiveVersion", null);
        }

        
          if (request.getParameter("targetPath") != null) {
              if (request.getParameter("targetPath").length() > 0)
                  window.setProperty("osivia.targetPath", request.getParameter("targetPath"));
              else if (window.getProperty("osivia.targetPath") != null)
                  window.setProperty("osivia.targetPath", null);
      }   

    }

}
