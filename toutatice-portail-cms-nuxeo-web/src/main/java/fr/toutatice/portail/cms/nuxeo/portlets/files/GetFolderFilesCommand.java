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
package fr.toutatice.portail.cms.nuxeo.portlets.files;

import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilter;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilterContext;

/**
 * Get folder files command.
 *
 * @author Cédric Krommenhoek
 * @see INuxeoCommand
 */
public class GetFolderFilesCommand implements INuxeoCommand {

    /** Folder identifier. */
    private String folderId;


    /**
     * Constructor.
     *
     * @param folderId folder identifier
     * @param folderPath folder path
     */
    public GetFolderFilesCommand(String folderId) {
        super();
        this.folderId = folderId;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Session session) throws Exception {
        // Nuxeo request
        StringBuilder nuxeoRequest = new StringBuilder();
        nuxeoRequest.append("ecm:parentId = '").append(this.folderId).append("' ");
        nuxeoRequest.append("AND ecm:primaryType != 'Workspace' ");
        nuxeoRequest.append("AND ecm:primaryType != 'WorkspaceRoot' ");
        nuxeoRequest.append("AND ecm:primaryType != 'PortalSite' ");
        nuxeoRequest.append("AND ecm:primaryType != 'Favorites' ");
        nuxeoRequest.append("ORDER BY ecm:pos ASC");

        // Query filter
        NuxeoQueryFilterContext queryFilterContext = new NuxeoQueryFilterContext(NuxeoQueryFilterContext.STATE_LIVE);
        String filteredRequest = NuxeoQueryFilter.addPublicationFilter(queryFilterContext, nuxeoRequest.toString());

        // Operation request
        OperationRequest operationRequest = session.newRequest("Document.QueryES");
        operationRequest.setHeader(Constants.HEADER_NX_SCHEMAS, "dublincore, common, toutatice, file, ottcCheckined, resourceSharing");
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
        builder.append(this.folderId);
        return builder.toString();
    };

}
