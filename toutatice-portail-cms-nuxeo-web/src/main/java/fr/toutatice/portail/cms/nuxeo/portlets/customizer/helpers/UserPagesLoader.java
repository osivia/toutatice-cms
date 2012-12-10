package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletContext;

import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
import org.nuxeo.ecm.automation.client.jaxrs.model.Documents;

import fr.toutatice.portail.cms.nuxeo.portlets.customizer.DefaultCMSCustomizer;
import fr.toutatice.portail.cms.nuxeo.portlets.service.CMSService;
import fr.toutatice.portail.cms.nuxeo.portlets.service.DocumentPublishSpaceNavigationCommand;
import fr.toutatice.portail.core.cms.CMSItem;
import fr.toutatice.portail.core.cms.CMSPage;
import fr.toutatice.portail.core.cms.CMSServiceCtx;

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
