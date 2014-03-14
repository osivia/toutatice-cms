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
package fr.toutatice.portail.cms.nuxeo.portlets.document;

import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.adapters.DocumentService;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

public class DeleteDocumentCommand implements INuxeoCommand {
	
	private String docId;
	
	public DeleteDocumentCommand(String docId){
		super();
		this.docId = docId;
	}

	public Object execute(Session nuxeoSession) throws Exception {
		DocumentService service = nuxeoSession.getAdapter(DocumentService.class);
		service.remove(docId);
		return null;
	}

	public String getId() {
		return "Document.Remove: " + docId;
	}

}
