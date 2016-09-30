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
package fr.toutatice.portail.cms.nuxeo.service.editablewindow;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

/**
 * Requête Nuxeo pour modifier plusieurs propriétés.
 * 
 */
public class DocumentUpdatePropertiesCommand implements INuxeoCommand {

    protected static final Log logger = LogFactory.getLog(DocumentUpdatePropertiesCommand.class);

    /** Document courant */
    private Document inputDoc;

    /** liste de propriétés */
    private List<String> propertiesToUpdate;

    public DocumentUpdatePropertiesCommand(Document inputDoc, List<String> propertiesToUpdate) {
        this.inputDoc = inputDoc;
        this.propertiesToUpdate = propertiesToUpdate;
    }

    public Object execute(Session nuxeoSession) throws Exception {

        Object execute = null;

        OperationRequest request = nuxeoSession.newRequest("Document.Update").setInput(inputDoc);

        StringBuilder sb = new StringBuilder();
        for (String property : propertiesToUpdate) {
            sb.append(property);
            sb.append("\n");
        }

        request.set("properties", sb.toString());

        if (logger.isDebugEnabled()) {
            logger.debug(sb.toString());
        }

        execute = request.execute();

        return execute;
    }

    public String getId() {
        return this.getClass().getSimpleName().concat(" : ").concat(inputDoc.getPath());
    }


}
