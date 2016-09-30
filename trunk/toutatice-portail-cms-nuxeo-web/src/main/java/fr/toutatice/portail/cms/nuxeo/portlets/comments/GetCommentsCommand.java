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
package fr.toutatice.portail.cms.nuxeo.portlets.comments;

import net.sf.json.JSONArray;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharEncoding;
import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Blob;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.FileBlob;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

/**
 * Get comments command.
 *
 * @author David Chevrier
 * @author Cédric Krommenhoek
 * @see INuxeoCommand
 */
public class GetCommentsCommand implements INuxeoCommand {

    /** Nuxeo document. */
    private final Document document;


    /**
     * Constructor.
     *
     * @param document Nuxeo document
     */
    public GetCommentsCommand(Document document) {
        this.document = document;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public JSONArray execute(Session nuxeoSession) throws Exception {
        OperationRequest request = nuxeoSession.newRequest("Fetch.DocumentComments");
        request.setHeader(Constants.HEADER_NX_SCHEMAS, "*");
        request.set("commentableDoc", this.document.getId());
        Blob commentsBlob = (Blob) request.execute();
        if (commentsBlob != null) {
            String fileContent = IOUtils.toString(commentsBlob.getStream(), CharEncoding.UTF_8);
            JSONArray jsonComments = JSONArray.fromObject(fileContent);

            // v2.0.21 : suppression des files
            if (commentsBlob instanceof FileBlob) {
                ((FileBlob) commentsBlob).getFile().delete();
            }

            return jsonComments;
        } else {
            return new JSONArray();
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return "Fetch.DocumentComments: " + this.document.getId();
    }

}
