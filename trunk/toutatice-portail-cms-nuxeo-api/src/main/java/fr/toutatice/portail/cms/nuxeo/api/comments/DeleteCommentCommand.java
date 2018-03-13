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
package fr.toutatice.portail.cms.nuxeo.api.comments;

import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

/**
 * Delete comment command.
 * 
 * @author David Chevrier
 * @author Cédric Krommenhoek
 * @see INuxeoCommand
 */
public class DeleteCommentCommand implements INuxeoCommand {

    /** Nuxeo document. */
    private final Document document;
    /** Comment identifier. */
    private final String id;


    /**
     * Constructor.
     *
     * @param document Nuxeo document
     * @param id comment identifier
     */
    public DeleteCommentCommand(Document document, String id) {
        super();
        this.document = document;
        this.id = id;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Session nuxeoSession) throws Exception {
        OperationRequest request = nuxeoSession.newRequest("Document.DeleteComment");
        request.set("commentableDoc", this.document.getId());
        request.set("comment", this.id);
        return request.execute();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return "Document.DeleteComment: " + this.document.getTitle();
    }

}
