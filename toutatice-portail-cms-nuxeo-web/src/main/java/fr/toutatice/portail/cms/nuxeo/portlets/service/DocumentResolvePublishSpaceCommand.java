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
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.core.cms.NavigationItem;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;


/**
 * Return all the navigation items.
 *
 * @author jeanseb
 * @see INuxeoCommand
 */
public class DocumentResolvePublishSpaceCommand implements INuxeoCommand {

    private final String path;

    public DocumentResolvePublishSpaceCommand(String path) {
        super();
        this.path = path;
    }

    @Override
    public Object execute(Session session) throws Exception {
        OperationRequest request = session.newRequest("Document.FetchPublishSpace");
        request.setHeader(Constants.HEADER_NX_SCHEMAS, "dublincore,common, toutatice");
        request.set("value", this.path);
        Document publishSpace = (Document) request.execute();

        NavigationItem navItem = new NavigationItem();
        navItem.setMainDoc(publishSpace);
        navItem.setPath(this.path);

        return navItem;

    }

    @Override
    public String getId() {
        return "ResolvePublishSpaceCommand/" + this.path;
    };

}
