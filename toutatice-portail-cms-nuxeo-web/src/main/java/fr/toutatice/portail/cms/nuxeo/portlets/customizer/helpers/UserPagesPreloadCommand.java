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
package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoCompatibility;


/**
 * Return all the navigation items.
 * 
 * @author jeanseb
 * @see INuxeoCommand
 */
public class UserPagesPreloadCommand implements INuxeoCommand {

    /** Indicates if request is executed with ElasicSearch */
    private final boolean useES;
    /** User domains, may be null. */
    private final List<String> domains;


    /**
     * Constructor.
     * 
     * @param domains user domains, may be null
     */
    public UserPagesPreloadCommand(List<String> domains) {
        super();
        this.useES = NuxeoCompatibility.canUseES();
        this.domains = domains;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Session session) throws Exception {
        // Operation request
        OperationRequest request;
        if (this.useES) {
            request = getESRequest(session);
        } else {
            request = getVCSRequest(session);
        }

        // Username
        String username = session.getLogin().getUsername();

        // Query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT * FROM Document ");
        queryBuilder.append("WHERE ecm:mixinType = 'Space' ");
        queryBuilder.append("AND ttc:isPreloadedOnLogin = 1 ");
        queryBuilder.append("AND (ecm:isProxy = 1 OR (ecm:isProxy = 0 AND ecm:mixinType <> 'WebView')) ");
        queryBuilder.append("AND ecm:currentLifeCycleState <> 'deleted' ");
        queryBuilder.append("AND ecm:isCheckedInVersion = 0 ");
        queryBuilder.append("AND (NOT ecm:path STARTSWITH '/default-domain/UserWorkspaces' OR dc:creator = '").append(username).append("') ");
        if (CollectionUtils.isNotEmpty(this.domains)) {
            queryBuilder.append("AND (");
            boolean first = true;
            for (String domain : this.domains) {
                if (first) {
                    first = false;
                } else {
                    queryBuilder.append(" OR ");
                }
                queryBuilder.append("ecm:path STARTSWITH '").append(domain).append("'");
            }
            queryBuilder.append(") ");
        }
        queryBuilder.append("ORDER BY ttc:tabOrder ASC");
        
        request.set("query", queryBuilder.toString());

        return request.execute();
    }


    protected OperationRequest getESRequest(Session session) throws Exception {
        OperationRequest request = session.newRequest("Document.QueryES");
        request.set(Constants.HEADER_NX_SCHEMAS, "dublincore,common,toutatice");
        return request;
    }


    protected OperationRequest getVCSRequest(Session session) throws Exception {
        OperationRequest request = session.newRequest("Document.Query");
        request.setHeader(Constants.HEADER_NX_SCHEMAS, "dublincore,common,toutatice");
        return request;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return "UserPagesPreloadCommand";
    }

}
