/*
* (C) Copyright 2018  Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
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
 */
package fr.toutatice.portail.cms.nuxeo.service.user;

import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletContext;
import javax.servlet.http.HttpSession;

import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.cache.services.CacheInfo;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.directory.v2.DirServiceFactory;
import org.osivia.portal.api.directory.v2.model.Person;
import org.osivia.portal.api.directory.v2.service.PersonService;
import org.osivia.portal.api.user.UserPreferences;
import org.osivia.portal.core.cms.CMSServiceCtx;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandContext;

/**
 * @author Loïc Billon
 *
 */
public class UserPreferencesDelegation {

		
	private static final String PREFS_SESSION_ATTRIBUTE = "osivia.user.preferences";
	private PersonService personService;

	public UserPreferencesDelegation() {
		personService = DirServiceFactory.getService(PersonService.class);
	}
	
	/**
	 * @param cmsContext
	 * @return
	 * @throws PortalException 
	 */
	public UserPreferences getPreferences(PortalControllerContext context) throws PortalException {
		
		// Get session attribute
		HttpSession httpSession = context.getHttpServletRequest().getSession();
        // Session attribute
        Object attribute = httpSession.getAttribute(PREFS_SESSION_ATTRIBUTE);

        // User session
        UserPreferences userPreferences = null;
        if ((attribute == null) || !(attribute instanceof UserPreferences)) {
        	
        	// If not loaded, get it in nuxeo
        	if(httpSession.getAttribute("PRINCIPAL_TOKEN") != null) {
	        	String user = (String) httpSession.getAttribute("PRINCIPAL_TOKEN");
	
	        	Person person = personService.getPerson(user);
	        	
	        	if(person != null) {
	        	
	        		Object ecmProfile = personService.getEcmProfile(context, person);
	        		
	        		if(ecmProfile instanceof Document) {
	        			Document profile = (Document) ecmProfile;
	        			
	        			userPreferences = toPreferencesDto(profile);
	        		}
	                httpSession.setAttribute(PREFS_SESSION_ATTRIBUTE, userPreferences);
	
	        	}
        	}
        	
        } else {
            userPreferences = (UserPreferences) attribute;
        }

        return userPreferences;
		
	}
	
	/**
	 * Update method
	 * @param cmsContext context
	 * @param httpSession the user session
	 */
	public void updateUserPreferences(CMSServiceCtx cmsContext, HttpSession httpSession) {
		
        Object attribute = httpSession.getAttribute(PREFS_SESSION_ATTRIBUTE);
        
        if ((attribute != null) && attribute instanceof UserPreferences) {
        	UserPreferences userPreferences = (UserPreferences) attribute;
        	
        	if(userPreferences.isUpdate()) {
	            // Portlet context
	            PortletContext portletContext = cmsContext.getPortletCtx();
        		
	            // Nuxeo controller
	            NuxeoController nuxeoController = new NuxeoController(portletContext);
	            nuxeoController.setAuthType(NuxeoCommandContext.AUTH_TYPE_SUPERUSER);
	            nuxeoController.setCacheType(CacheInfo.CACHE_SCOPE_NONE);
	
	            // Nuxeo command
	            INuxeoCommand command = new UpdatePreferencesCommand(userPreferences);
	            nuxeoController.executeNuxeoCommand(command);
        	}
        	
        }
	}


	/**
     * Convert from UserProfile document to UserPreferences pojo.
     * 
     * @param profile profile Nuxeo document
     * @return user preferences
     */
	private UserPreferences toPreferencesDto(Document profile) {
		Map<String, String> prefs = new HashMap<>();

		PropertyList list = profile.getProperties().getList(UpdatePreferencesCommand.METADATA_FOLDERS_PREFS);

		if ((list != null) && !list.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                PropertyMap map = list.getMap(i);

                Object objectValue = map.get(UpdatePreferencesCommand.METADATA_FOLDERS_PREFS_VALUE);
                Object objectId = map.get(UpdatePreferencesCommand.METADATA_FOLDERS_PREFS_ID);

                if (objectId != null && objectValue != null) {
                    prefs.put(objectId.toString(), objectValue.toString());
                }
            }
		}

		UserPreferences upf = new UserPreferences(profile.getId());
		upf.setFolderDisplays(prefs);

		return upf;
	}

}

