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

import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

/**
 * Requête Nuxeo pour supprimer plusieurs propriétés.
 * 
 */
public class DocumentRemovePropertyCommand implements INuxeoCommand {

    /** Document courant */
    private Document inputDoc;

    /** liste de propriétés */
    private List<String> propertiesToRemove;

    public DocumentRemovePropertyCommand(Document inputDoc, List<String> propertiesToRemove) {
        this.inputDoc = inputDoc;
        this.propertiesToRemove = propertiesToRemove;
    }

    public Object execute(Session nuxeoSession) throws Exception {

        Object execute = null;
        for (String property : propertiesToRemove) {
            OperationRequest request = nuxeoSession.newRequest("Document.RemoveProperty").setInput(inputDoc);

            request.set("xpath", property);

            execute = request.execute();
        }
        return execute;
    }

    public String getId() {
        return this.getClass().getSimpleName().concat(" : ").concat(inputDoc.getPath());
    }


}
