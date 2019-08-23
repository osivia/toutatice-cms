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

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
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
import org.osivia.portal.api.user.UserSavedSearch;
import org.osivia.portal.core.cms.CMSServiceCtx;

import javax.portlet.PortletContext;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User preferences delegation.
 *
 * @author Loïc Billon
 * @author Cédric Krommenhoek
 */
public class UserPreferencesDelegation {

    /**
     * Session attribute.
     */
    private static final String SESSION_ATTRIBUTE = "osivia.user.preferences";


    /**
     * Person service.
     */
    private final PersonService personService;


    /**
     * Constructor.
     */
    public UserPreferencesDelegation() {
        super();
        this.personService = DirServiceFactory.getService(PersonService.class);
    }


    /**
     * Get user preferences.
     *
     * @param portalControllerContext portal controller context
     * @return user preferences
     */
    public UserPreferences getPreferences(PortalControllerContext portalControllerContext) throws PortalException {
        // Session
        HttpSession httpSession = portalControllerContext.getHttpServletRequest().getSession();
        // Session attribute
        Object attribute = httpSession.getAttribute(SESSION_ATTRIBUTE);

        // User session
        UserPreferences userPreferences = null;
        if ((attribute == null) || !(attribute instanceof UserPreferences)) {
            // If not loaded, get it in nuxeo
            if (httpSession.getAttribute("PRINCIPAL_TOKEN") != null) {
                String user = (String) httpSession.getAttribute("PRINCIPAL_TOKEN");

                Person person = personService.getPerson(user);

                if (person != null) {
                    Object ecmProfile = personService.getEcmProfile(portalControllerContext, person);

                    if (ecmProfile instanceof Document) {
                        Document profile = (Document) ecmProfile;

                        userPreferences = this.toPreferencesDto(profile);
                    }

                    httpSession.setAttribute(SESSION_ATTRIBUTE, userPreferences);
                }
            }
        } else {
            userPreferences = (UserPreferences) attribute;
        }

        return userPreferences;
    }


    /**
     * Update method.
     *
     * @param cmsContext  CMS context
     * @param httpSession session
     */
    public void updateUserPreferences(CMSServiceCtx cmsContext, HttpSession httpSession) {
        Object attribute = httpSession.getAttribute(SESSION_ATTRIBUTE);

        if ((attribute != null) && attribute instanceof UserPreferences) {
            UserPreferences userPreferences = (UserPreferences) attribute;

            if (userPreferences.isUpdate()) {
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
        // User preferences
        UserPreferences userPreferences = new UserPreferences(profile.getId());

        // Folder preferences
        Map<String, String> folders = new HashMap<>();
        PropertyList foldersPropertyList = profile.getProperties().getList(UpdatePreferencesCommand.METADATA_FOLDERS_PREFS);
        if ((foldersPropertyList != null) && !foldersPropertyList.isEmpty()) {
            for (int i = 0; i < foldersPropertyList.size(); i++) {
                PropertyMap folderPropertyMap = foldersPropertyList.getMap(i);

                Object objectValue = folderPropertyMap.get(UpdatePreferencesCommand.METADATA_FOLDERS_PREFS_VALUE);
                Object objectId = folderPropertyMap.get(UpdatePreferencesCommand.METADATA_FOLDERS_PREFS_ID);

                if (objectId != null && objectValue != null) {
                    folders.put(objectId.toString(), objectValue.toString());
                }
            }
        }
        userPreferences.setFolderDisplays(folders);

        // Saved searches
        List<UserSavedSearch> savedSearches;
        PropertyList savedSearchesPropertyList = profile.getProperties().getList(UpdatePreferencesCommand.SAVED_SEARCHES_XPATH);
        if ((savedSearchesPropertyList == null) || savedSearchesPropertyList.isEmpty()) {
            savedSearches = null;
        } else {
            savedSearches = new ArrayList<>(savedSearchesPropertyList.size());

            for (int i = 0; i < savedSearchesPropertyList.size(); i++) {
                PropertyMap savedSearchPropertyMap = savedSearchesPropertyList.getMap(i);

                // Identifier
                int id = NumberUtils.toInt(savedSearchPropertyMap.getString(UpdatePreferencesCommand.SAVED_SEARCH_ID));
                // Saved search
                UserSavedSearch savedSearch = new UserSavedSearch(id);
                // Display name
                String displayName = savedSearchPropertyMap.getString(UpdatePreferencesCommand.SAVED_SEARCH_DISPLAY_NAME);
                savedSearch.setDisplayName(displayName);
                // Order
                Integer order = NumberUtils.createInteger(savedSearchPropertyMap.getString(UpdatePreferencesCommand.SAVED_SEARCH_ORDER));
                savedSearch.setOrder(order);
                // Data
                String data = savedSearchPropertyMap.getString(UpdatePreferencesCommand.SAVED_SEARCH_DATA);
                savedSearch.setData(data);

                savedSearches.add(savedSearch);
            }
        }
        userPreferences.setSavedSearches(savedSearches);

        return userPreferences;
    }

}

