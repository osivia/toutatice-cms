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

import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletContext;

import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSPage;
import org.osivia.portal.core.cms.CMSServiceCtx;

import fr.toutatice.portail.cms.nuxeo.portlets.customizer.DefaultCMSCustomizer;
import fr.toutatice.portail.cms.nuxeo.portlets.service.CMSService;
import fr.toutatice.portail.cms.nuxeo.portlets.service.DocumentPublishSpaceNavigationCommand;

/**
 * Préchargement des pages au login de l'utilisateur
 * 
 * Pour l'instant, traitement minimimaliste ("ttc:isPreloadedOnLogin = 1";)
 * 
 * A sous-classer pour spécificités
 * 
 * @author jeanseb
 *
 */
public class UserPagesLoader { 

	CMSService CMSService;

	public UserPagesLoader(PortletContext portletCtx, DefaultCMSCustomizer customizer, CMSService cmsService) {
		super();
		CMSService = cmsService;
	}

	public List<CMSPage> computeUserPreloadedPages(CMSServiceCtx cmsCtx)  throws Exception {	


		Documents children = (Documents) CMSService.executeNuxeoCommand(cmsCtx, new UserPagesPreloadCommand());

		// Conversion en CMSItem
		List<CMSPage> pages = new ArrayList<CMSPage>();
		
		for (Document child : children) {
			String spacePath = DocumentPublishSpaceNavigationCommand.computeNavPath(child.getPath());
			
			CMSItem publishSpace = CMSService.createNavigationItem(cmsCtx, spacePath, child.getTitle(), child, spacePath);
			
			CMSPage userPage = new CMSPage();
			userPage.setPublishSpace(publishSpace);
			
			//userPage.setParentPath("/default/multi2");
			
			pages.add(userPage);
		}

		return pages;
	}

}
