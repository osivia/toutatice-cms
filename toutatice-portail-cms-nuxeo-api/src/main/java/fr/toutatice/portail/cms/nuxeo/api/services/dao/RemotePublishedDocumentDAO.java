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
 */
package fr.toutatice.portail.cms.nuxeo.api.services.dao;

import org.osivia.portal.api.context.PortalControllerContext;

import fr.toutatice.portail.cms.nuxeo.api.domain.RemotePublishedDocumentDTO;
import net.sf.json.JSONObject;


/**
 * @author David Chevrier.
 *
 */
public class RemotePublishedDocumentDAO implements IDAO<JSONObject, RemotePublishedDocumentDTO> {
    
    /** Singleton instance. */
    private static RemotePublishedDocumentDAO instance;
    
    /**
     * Private constructor.
     */
    private RemotePublishedDocumentDAO() {
        super();
    }


    /**
     * Get singleton instance.
     *
     * @return singleton instance
     */
    public static RemotePublishedDocumentDAO getInstance() {
        if (instance == null) {
            instance = new RemotePublishedDocumentDAO();
        }
        return instance;
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public RemotePublishedDocumentDTO toDTO(JSONObject jsonObject) {
        return this.toDTO(null, jsonObject);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public RemotePublishedDocumentDTO toDTO(PortalControllerContext portalControllerContext, JSONObject jsonObject) {
        RemotePublishedDocumentDTO publishedDocDTO = new RemotePublishedDocumentDTO();
        
        publishedDocDTO.setNxUrl(jsonObject.getString("url"));
        publishedDocDTO.setSectionTitle(jsonObject.getString("sectionTitle"));
        publishedDocDTO.setVersionLabel(jsonObject.getString("versionLabel"));
        
        return publishedDocDTO;
    }

}
