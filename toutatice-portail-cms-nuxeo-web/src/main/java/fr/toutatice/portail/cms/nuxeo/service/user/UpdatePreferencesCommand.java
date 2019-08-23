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
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.adapters.DocumentService;
import org.nuxeo.ecm.automation.client.model.DocRef;
import org.nuxeo.ecm.automation.client.model.IdRef;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.user.UserPreferences;
import org.osivia.portal.api.user.UserSavedSearch;

import java.util.List;
import java.util.Map;

/**
 * Command to store user preferences in UserProfile document
 *
 * @author Loïc Billon
 * @author Cédric Krommenhoek
 * @see INuxeoCommand
 */
public class UpdatePreferencesCommand implements INuxeoCommand {

    /**
     * Folder preferences xpath.
     */
    public static final String METADATA_FOLDERS_PREFS = "ttc_userprofile:folders_prefs";
    /**
     * Folder identifier.
     */
    public static final String METADATA_FOLDERS_PREFS_ID = "webid";
    /**
     * Folder display value.
     */
    public static final String METADATA_FOLDERS_PREFS_VALUE = "display_style";

    /**
     * Saved searches xpath.
     */
    public static final String SAVED_SEARCHES_XPATH = "ttc_userprofile:savedSearches";
    /**
     * Saved search identifier.
     */
    public static final String SAVED_SEARCH_ID = "searchId";
    /**
     * Saved search display name.
     */
    public static final String SAVED_SEARCH_DISPLAY_NAME = "displayName";
    /**
     * Saved search order.
     */
    public static final String SAVED_SEARCH_ORDER = "order";
    /**
     * Saved search data.
     */
    public static final String SAVED_SEARCH_DATA = "data";


    /**
     * User preferences.
     */
    private final UserPreferences preferences;


    /**
     * Constructor.
     *
     * @param preferences user preferences
     */
    public UpdatePreferencesCommand(UserPreferences preferences) {
        super();
        this.preferences = preferences;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Session nuxeoSession) throws Exception {
        // Document service
        DocumentService documentService = nuxeoSession.getAdapter(DocumentService.class);

        PropertyMap properties = new PropertyMap();
        properties.set(METADATA_FOLDERS_PREFS, this.convertFolders(this.preferences.getFolderDisplays()));
        properties.set(SAVED_SEARCHES_XPATH, this.convertSavedSearches(this.preferences.getSavedSearches()));

        DocRef docRef = new IdRef(this.preferences.getDocId());

        return documentService.update(docRef, properties);
    }


    /**
     * Convert folders to JSON.
     *
     * @param folders folders
     * @return JSON
     */
    private String convertFolders(Map<String, String> folders) {
        JSONArray array = new JSONArray();

        for (Map.Entry<String, String> entry : folders.entrySet()) {
            JSONObject object = new JSONObject();
            object.put(METADATA_FOLDERS_PREFS_ID, entry.getKey());
            object.put(METADATA_FOLDERS_PREFS_VALUE, entry.getValue());

            array.add(object);
        }

        return array.toString();
    }


    /**
     * Convert saved searches to JSON.
     *
     * @param savedSearches saved searches
     * @return JSON
     */
    private String convertSavedSearches(List<UserSavedSearch> savedSearches) {
        String result;

        if (CollectionUtils.isEmpty(savedSearches)) {
            result = null;
        } else {
            JSONArray array = new JSONArray();

            for (UserSavedSearch savedSearch : savedSearches) {
                JSONObject object = new JSONObject();
                object.put(SAVED_SEARCH_ID, savedSearch.getId());
                object.put(SAVED_SEARCH_DISPLAY_NAME, savedSearch.getDisplayName());
                object.put(SAVED_SEARCH_ORDER, savedSearch.getOrder());
                object.put(SAVED_SEARCH_DATA, savedSearch.getData());

                array.add(object);
            }

            result = array.toString();
        }

        return result;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return null;
    }

}
