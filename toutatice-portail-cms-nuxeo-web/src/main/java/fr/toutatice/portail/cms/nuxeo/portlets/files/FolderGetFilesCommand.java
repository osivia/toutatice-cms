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
package fr.toutatice.portail.cms.nuxeo.portlets.files;

import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.osivia.portal.core.constants.InternalConstants;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilter;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilterContext;


public class FolderGetFilesCommand implements INuxeoCommand {
	
	String folderId;
	String folderPath;


	
	public FolderGetFilesCommand(String folderPath, String folderId) {
		super();
		this.folderId = folderId;
		this.folderPath = folderPath;

		
	}
	
	public Object execute( Session session)	throws Exception {
	
	     
			OperationRequest request;
			

			request =  session.newRequest("Document.Query");

		     // TODO: déporter le filtre sur les types dans le Customizer
            String nuxeoRequest = "ecm:parentId = '" + folderId + "' ";
            nuxeoRequest += " AND  (ecm:primaryType != 'Workspace' AND ecm:primaryType != 'WorkspaceRoot' AND ecm:primaryType != 'PortalSite') ";
           nuxeoRequest += " ORDER BY ecm:pos ";
 			
			
			// Insertion du filtre sur les élements publiés
			String filteredRequest = NuxeoQueryFilter.addPublicationFilter(new NuxeoQueryFilterContext( NuxeoQueryFilterContext.STATE_LIVE, InternalConstants.PORTAL_CMS_REQUEST_FILTERING_POLICY_NO_FILTER), nuxeoRequest);

			
			request.set("query", "SELECT * FROM Document WHERE "  + filteredRequest);
		
			request.setHeader(Constants.HEADER_NX_SCHEMAS, "dublincore,common, toutatice, file");

			Documents children = (Documents) request.execute();	     
			
			return children;
	     
	}

	public String getId() {
		return "FolderGetFilesCommand/" +folderPath;
	};		

}
