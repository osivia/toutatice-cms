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
public class DocumentCheckInCommand implements INuxeoCommand {

    /** Document courant */
    private Document inputDoc;

    private boolean keepLocalCopy = false;
    
    

    public DocumentCheckInCommand(Document inputDoc) {
        this.inputDoc = inputDoc;
    }
    
    public DocumentCheckInCommand(Document inputDoc, boolean keepLocalCopy) {
        this.inputDoc = inputDoc;
        this.keepLocalCopy = keepLocalCopy;
    }

    @Override
    public Object execute(Session nuxeoSession) throws Exception {
        Object execute = null;

        OperationRequest request = nuxeoSession.newRequest("ToutaticeDrive.CheckInDocument").setInput(inputDoc);

        request.set("keepLocalCopy", keepLocalCopy);
        execute = request.execute();

        return execute;
    }

    @Override
    public String getId() {
        return this.getClass().getSimpleName() + "/" + this;
    }

}
