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
package fr.toutatice.portail.cms.nuxeo.portlets.service;

import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Documents;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilter;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilterContext;

/**
 * Requête Nuxeo de recherche des Liens définis dans un dossier
 *
 */
public class DocumentGetMediaLibraryCommand implements INuxeoCommand {

	String path;
	
    public DocumentGetMediaLibraryCommand(String path) {
		super();
		this.path = path;
	}
	
	/**
	 * Construction de la requête
	 */
	public Object execute(Session session) throws Exception {
		OperationRequest request;
		
		request =  session.newRequest("Document.Query");
		
        String nuxeoRequest = "ecm:path STARTSWITH '" + path + "' AND ecm:primaryType = 'MediaLibrary' ORDER BY ecm:pos";
		

		// Insertion du filtre sur les élements publiés
        NuxeoQueryFilterContext queryFilter = new NuxeoQueryFilterContext( NuxeoQueryFilterContext.STATE_LIVE, "global" );

        
		String filteredRequest = NuxeoQueryFilter.addPublicationFilter(queryFilter, nuxeoRequest);
		
		request.set("query", "SELECT * FROM Document WHERE "  + filteredRequest);
		
		// On récupère seulement le schéma global et celui du contextualLink (pour l'url).
        request.setHeader(Constants.HEADER_NX_SCHEMAS, "dublincore");

		Documents children = (Documents) request.execute();	
		
        if (children.size() == 1)
            return children.get(0);
        else
            return null;
	}

	public String getId() {
        return "DocumentGetMediaLibraryCommand/" + path;
	}

	public String getPath() {
		return path;
	}
	
	

}
