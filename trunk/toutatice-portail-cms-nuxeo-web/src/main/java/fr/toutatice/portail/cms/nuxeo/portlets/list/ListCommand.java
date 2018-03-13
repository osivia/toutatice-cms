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
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilter;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilterContext;
import fr.toutatice.portail.cms.nuxeo.portlets.commands.CommandConstants;

/**
 * List command.
 *
 * @see INuxeoCommand
 */
public class ListCommand implements INuxeoCommand {

    /** Header flag to force request in VCS. */
    public static final String VCS_FORCE_FLAG = "nx_querying_vcs_force";

    /** Nuxeo request. */
    private final String nuxeoRequest;
    /** Current page number. */
    private final int pageNumber;
    /** Page size. */
    private final int pageSize;
    /** Schemas. */
    private final String schemas;
    /** Version. */
    private final String version;
    /** Filter policy. */
    private final String portalPolicyFilter;
    /** Force request on VCS. */
    private boolean forceVCS;


    /**
     * Constructor.
     *
     * @param nuxeoRequest Nuxeo request
     * @param version version
     * @param pageNumber current page number
     * @param pageSize page size
     * @param schemas schemas
     * @param portalPolicyFilter filter policy
     * @param forceVCS request on VCS indicator
     */
    public ListCommand(String nuxeoRequest, String version, int pageNumber, int pageSize, String schemas, String portalPolicyFilter) {
        super();
        this.nuxeoRequest = nuxeoRequest;
        this.version = version;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.schemas = schemas;
        this.portalPolicyFilter = portalPolicyFilter;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Session nuxeoSession) throws Exception {

        OperationRequest request;
        if (this.isForceVCS()) {
            request = this.generateVCSRequest(nuxeoSession);
        } else {
            request = this.generateESRequest(nuxeoSession);
        }

        // Insertion du filtre sur les élements
        int state = NuxeoQueryFilter.getState(this.version);
        NuxeoQueryFilterContext queryFilter = new NuxeoQueryFilterContext(state, this.portalPolicyFilter);

        String filteredRequest = NuxeoQueryFilter.addPublicationFilter(queryFilter, this.nuxeoRequest);

        request.set("query", "SELECT * FROM Document WHERE " + filteredRequest);

        return request.execute();
    }


    protected OperationRequest generateESRequest(Session session) throws Exception {
        OperationRequest request = session.newRequest("Document.QueryES");
        request.set("pageSize", this.pageSize);
        request.set("currentPageIndex", this.pageNumber);
        request.set(Constants.HEADER_NX_SCHEMAS, this.schemas);
        return request;
    }


    protected OperationRequest generateVCSRequest(Session session) throws Exception {
        OperationRequest request = session.newRequest("Document.PageProvider");
        request.setHeader(VCS_FORCE_FLAG, "true");
        request.set("pageSize", this.pageSize);
        request.set("page", this.pageNumber);
        request.setHeader(Constants.HEADER_NX_SCHEMAS, this.schemas);
        request.set("maxResults", CommandConstants.PAGE_PROVIDER_UNLIMITED_MAX_RESULTS);
        return request;
    }


    /**
     * @return the forceVCS
     */
    public boolean isForceVCS() {
        return forceVCS;
    }


    /**
     * @param forceVCS the forceVCS to set
     */
    public void setForceVCS(boolean forceVCS) {
        this.forceVCS = forceVCS;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getSimpleName());
        builder.append("/");
        builder.append(this.version);
        builder.append("/");
        builder.append(this.pageSize);
        builder.append("/");
        builder.append(this.pageNumber);
        builder.append("/");
        builder.append(this.nuxeoRequest);
        return builder.toString();
    };


}
