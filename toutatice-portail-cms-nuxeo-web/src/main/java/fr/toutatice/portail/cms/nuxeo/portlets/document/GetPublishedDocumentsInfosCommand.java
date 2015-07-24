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
package fr.toutatice.portail.cms.nuxeo.portlets.document;

import net.sf.json.JSONArray;

import org.apache.commons.io.IOUtils;
import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Blob;
import org.nuxeo.ecm.automation.client.model.Document;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;


/**
 * @author David Chevrier.
 *
 */
public class GetPublishedDocumentsInfosCommand implements INuxeoCommand {

    Document document;
    Boolean readFilter;

    public GetPublishedDocumentsInfosCommand(Document document, Boolean readFilter) {
        this.document = document;
        this.readFilter = readFilter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONArray execute(Session session) throws Exception {
        
        OperationRequest request = session.newRequest("Document.GetPublishedDocumentsInfos");
        request.setHeader(Constants.HEADER_NX_SCHEMAS, "*");
        request.set("readFilter", readFilter);
        request.setInput(document);
        
        Blob publishedDocsInfos = (Blob) request.execute();
        if(publishedDocsInfos != null){
            String fileContent = IOUtils.toString(publishedDocsInfos.getStream(), "UTF-8");
            return JSONArray.fromObject(fileContent);
        } else {
            return new JSONArray();
        }
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return new StringBuffer().append(GetPublishedDocumentsInfosCommand.class.toString()).append("|").append(document.getPath()).toString();
    }

}
