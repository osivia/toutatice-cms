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

import java.util.Map;

import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.adapters.DocumentService;
import org.nuxeo.ecm.automation.client.model.IdRef;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.user.UserPreferences;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Command to store user preferences in UserProfile document
 * @author Loïc Billon
 * @see INuxeoCommand
 */
public class UpdatePreferencesCommand implements INuxeoCommand {

	/**
	 * 
	 */
	public static final String METADATA_FOLDERS_PREFS = "ttc_userprofile:folders_prefs";
	

	/**
	 * 
	 */
	public static final String METADATA_FOLDERS_PREFS_ID = "webid";
	

	/**
	 * 
	 */
	public static final String METADATA_FOLDERS_PREFS_VALUE = "display_style";
	
    /** Space statistics. */
    private final UserPreferences preferences;


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
        properties.set(METADATA_FOLDERS_PREFS,this.convertFolderPrefsJson(preferences.getFolderDisplays()));

        IdRef docRef = new IdRef(preferences.getDocId());
        documentService.update(docRef, properties);
    	
        return null;
    }


    /**
	 * @param folderDisplays
	 * @return
	 */
	private String convertFolderPrefsJson(Map<String, String> folderDisplays) {

		JSONArray array = new JSONArray();
		
		for(Map.Entry<String, String> entry : folderDisplays.entrySet()) {
			JSONObject object = new JSONObject();
			object.put(METADATA_FOLDERS_PREFS_ID, entry.getKey());
			object.put(METADATA_FOLDERS_PREFS_VALUE, entry.getValue());
			array.add(object);
			
		}
		
		return array.toString();
	}

	/**
     * {@inheritDoc}
     */
    @Override
    public String getId() {

        return null;
    }

}
