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
package fr.toutatice.portail.cms.nuxeo.portlets.commands;

import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

/**
 * Enable or disable synchronization with local folder and nuxeo drive
 * 
 * @author lbi
 * 
 */
public class DocumentSetSynchronizationCommand implements INuxeoCommand {

    /** Document courant */
    private Document inputDoc;

    private Boolean enable;

    public DocumentSetSynchronizationCommand(Document inputDoc, Boolean enable) {
        this.inputDoc = inputDoc;
        this.enable = enable;
    }

    @Override
    public Object execute(Session nuxeoSession) throws Exception {
        Object execute = null;

        OperationRequest request = nuxeoSession.newRequest("NuxeoDrive.SetSynchronization").setInput(inputDoc);

        request.set("enable", enable);
        execute = request.execute();

        return execute;
    }

    @Override
    public String getId() {
        return "DocumentSetSynchronizationCommand/" + this;
    }

}