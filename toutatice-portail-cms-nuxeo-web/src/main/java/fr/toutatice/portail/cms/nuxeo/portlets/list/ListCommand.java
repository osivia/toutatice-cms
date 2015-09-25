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
package fr.toutatice.portail.cms.nuxeo.portlets.list;

import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoCompatibility;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilter;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilterContext;
import fr.toutatice.portail.cms.nuxeo.portlets.commands.CommandConstants;


public class ListCommand implements INuxeoCommand {

    String nuxeoRequest;
    int pageNumber;
    int pageSize;
    String schemas;
    String version;
    String portalPolicyFilter;
    boolean useES;


    public ListCommand(String nuxeoRequest, String version, int pageNumber, int pageSize, String schemas, String portalPolicyFilter, boolean useES) {
        super();
        this.nuxeoRequest = nuxeoRequest;
        this.version = version;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.schemas = schemas;
        this.portalPolicyFilter = portalPolicyFilter;
        this.useES = NuxeoCompatibility.canUseES() && useES;
    }

    public Object execute(Session nuxeoSession) throws Exception {

        OperationRequest request;

        if (this.useES) {
            request = generateESRequest(nuxeoSession);
        } else {
            request = generateVCSRequest(nuxeoSession);
        }

        // Insertion du filtre sur les élements 
        
        int state = NuxeoQueryFilter.getState(version);
        NuxeoQueryFilterContext queryFilter = new NuxeoQueryFilterContext(state, portalPolicyFilter);
        
        String filteredRequest = NuxeoQueryFilter.addPublicationFilter(queryFilter, nuxeoRequest);

        request.set("query", "SELECT * FROM Document WHERE " + filteredRequest);

        return request.execute();
    }

    protected OperationRequest generateESRequest(Session session) throws Exception {
        OperationRequest request = session.newRequest("Document.QueryES");
        request.set("pageSize", pageSize);
        request.set("currentPageIndex", pageNumber);
        request.set(Constants.HEADER_NX_SCHEMAS, schemas);
        return request;
    }

    protected OperationRequest generateVCSRequest(Session session) throws Exception {
        OperationRequest request = session.newRequest("Document.PageProvider");
        request.set("pageSize", pageSize);
        request.set("page", pageNumber);
        request.setHeader(Constants.HEADER_NX_SCHEMAS, schemas);
        if (NuxeoCompatibility.isVersionGreaterOrEqualsThan(NuxeoCompatibility.VERSION_60)) {
            request.set("maxResults", CommandConstants.PAGE_PROVIDER_UNLIMITED_MAX_RESULTS);
        }
        return request;
    }

    public String getId() {
        return "ListCommand/" + version + "/" + pageSize + "/" + pageNumber + "/" + nuxeoRequest;
    };


}
