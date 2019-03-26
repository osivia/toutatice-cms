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
package fr.toutatice.portail.cms.nuxeo.portlets.forms;

import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.osivia.portal.core.constants.InternalConstants;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilter;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilterContext;

/**
 * Get document command (for portal task).
 *
 * @author Loïc Billon
 * @see INuxeoCommand
 */
public class GetDocumentCommand implements INuxeoCommand {

    /** Document identifier. */
    private String path;


    /**
     * Constructor.
     *
     * @param path identifier
     */
    public GetDocumentCommand(String path) {
        super();
        this.path = path;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Session session) throws Exception {
        // Nuxeo request
        StringBuilder nuxeoRequest = new StringBuilder();
        nuxeoRequest.append("ecm:path = '").append(path).append("' ");
        nuxeoRequest.append("ORDER BY ecm:pos ASC");

        // Query filter
        NuxeoQueryFilterContext queryFilterContext = new NuxeoQueryFilterContext(NuxeoQueryFilterContext.STATE_LIVE,
                InternalConstants.PORTAL_CMS_REQUEST_FILTERING_POLICY_NO_FILTER);
        String filteredRequest = NuxeoQueryFilter.addPublicationFilter(queryFilterContext, nuxeoRequest.toString());

        // Operation request
        OperationRequest operationRequest = session.newRequest("Document.QueryES");
        operationRequest.setHeader(Constants.HEADER_NX_SCHEMAS, "dublincore, common, toutatice, file, clink");
        operationRequest.set("query", "SELECT * FROM Document WHERE " + filteredRequest);

        return operationRequest.execute();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getSimpleName());
        builder.append("/");
        builder.append(this.path);
        return builder.toString();
    };

}
