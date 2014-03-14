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
import org.nuxeo.ecm.automation.client.model.Blob;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.FileBlob;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

public class CreateChildCommentCommand implements INuxeoCommand {

    private Document document;
    private String commentId;
    private String childCommentContent;
    private String childCommentTitle;
    private File file;

    public CreateChildCommentCommand(Document document, String commentId, String childCommentContent, String childCommentTitle, File file) {
        this.document = document;
        this.commentId = commentId;
        this.childCommentContent = childCommentContent;
        this.childCommentTitle = childCommentTitle;
        this.file = file;
    }

    public Object execute(Session nuxeoSession) throws Exception {
        boolean hasFile = file != null;
        OperationRequest request = nuxeoSession.newRequest("Document.CreateChildComment");
        request.set("commentableDoc", document.getId());
        request.set("comment", commentId);
        childCommentContent = HTMLCommentsTreeBuilder.storeNewLines(childCommentContent);
        request.set("childComment", childCommentContent);
        request.set("childCommentTitle", childCommentTitle);
        if (hasFile) {
            request.set("fileName", file.getName());
        }
        Blob commentIdBinary = (Blob) request.execute();
        if (hasFile) {
            String commentId = IOUtils.toString(commentIdBinary.getStream(), "UTF-8");
            AddCommentCommand.setBlob(nuxeoSession, commentId, file);
        }
        return document;
    }

    public String getId() {
        return "Document.CreateChildComment: " + document.getTitle();
    }

}
