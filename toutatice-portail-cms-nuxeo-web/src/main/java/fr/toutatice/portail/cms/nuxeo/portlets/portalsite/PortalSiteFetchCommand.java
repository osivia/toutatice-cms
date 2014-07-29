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
package fr.toutatice.portail.cms.nuxeo.portlets.portalsite;

import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.osivia.portal.core.constants.InternalConstants;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilter;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilterContext;


public class PortalSiteFetchCommand implements INuxeoCommand {
	
	String path;

	
	public PortalSiteFetchCommand(String path) {
		super();
		this.path = path;

	}
	
	public Object execute( Session session)	throws Exception {
		
		Document doc = (org.nuxeo.ecm.automation.client.model.Document) session
		  .newRequest("Document.Fetch").setHeader(Constants.HEADER_NX_SCHEMAS, "*").set("value", path)
		  .execute();
		
	
		OperationRequest request;
		

		request =  session.newRequest("Document.Query");

		String nuxeoRequest = "ecm:parentId = '" + doc.getId()+ "' ORDER BY ecm:pos ";
		
		
		// Insertion du filtre sur les élements publiés
        NuxeoQueryFilterContext queryFilter = new NuxeoQueryFilterContext( NuxeoQueryFilterContext.STATE_DEFAULT, InternalConstants.PORTAL_CMS_REQUEST_FILTERING_POLICY_NO_FILTER);
		
		String filteredRequest = NuxeoQueryFilter.addPublicationFilter(queryFilter, nuxeoRequest);

		
		request.set("query", "SELECT * FROM Document WHERE "  + filteredRequest);
	
		request.setHeader(Constants.HEADER_NX_SCHEMAS, "dublincore,common, toutatice");

		Documents children = (Documents) request.execute();	     
				
		PortalSiteBean portalSite = new PortalSiteBean(doc, children);
		
		
	    return portalSite;
	
	}

	public String getId() {

		return "PortalSiteFetchCommand/"+ path;
	};		

}
