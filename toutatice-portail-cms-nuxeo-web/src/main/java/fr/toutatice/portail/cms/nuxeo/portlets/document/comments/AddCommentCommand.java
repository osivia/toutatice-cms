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
package fr.toutatice.portail.cms.nuxeo.portlets.document.comments;

import java.io.File;

import org.apache.commons.io.IOUtils;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.adapters.DocumentService;
import org.nuxeo.ecm.automation.client.model.Blob;
import org.nuxeo.ecm.automation.client.model.DocRef;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.FileBlob;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

public class AddCommentCommand implements INuxeoCommand {

    private Document document;
    private String commentContent;
    private String commentTitle;
    private File file;

    public AddCommentCommand(Document document, String commentContent, String commentTitle, File file) {
        this.document = document;
        this.commentContent = commentContent;
        this.commentTitle = commentTitle;
        this.file = file;
    }

    public Object execute(Session nuxeoSession) throws Exception {
        boolean hasFile = file != null;
        OperationRequest request = nuxeoSession.newRequest("Document.AddComment");
        request.set("commentableDoc", document.getId());
        commentContent = HTMLCommentsTreeBuilder.storeNewLines(commentContent);
        request.set("comment", commentContent);
        request.set("title", commentTitle);
        if (hasFile) {
            request.set("fileName", file.getName());
        }
        Blob commentIdBinary = (Blob) request.execute();
        if (hasFile) {
            String commentId = IOUtils.toString(commentIdBinary.getStream(), "UTF-8");
            setBlob(nuxeoSession, commentId, file);
        }
        return document;
    }

    public static void setBlob(Session nuxeoSession, String commentId, File file) throws Exception {
        DocumentService rs = nuxeoSession.getAdapter(DocumentService.class);
        DocRef commentRef = new DocRef(commentId);
        Blob blob = new FileBlob(file);
        rs.setBlob(commentRef, blob, "post:fileContent");
    }

    public String getId() {
        return "Document.AddComment: " + document.getTitle();
    }

}
