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

import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

public class DocumentFetchLiveCommand implements INuxeoCommand {
	
	String path;
	String permission;
	
	public DocumentFetchLiveCommand(String path, String permission) {
		super();
		this.path = path;
		this.permission = permission;
	}
	
	public Object execute( Session session)	throws Exception {
		
        Document doc = (Document) session
		.newRequest("Document.FetchLiveDocument").setHeader(Constants.HEADER_NX_SCHEMAS, "*").set("value", path).set("permission", permission)
		.execute();

	     return doc;
	
	}

	public String getId() {
		return "FetchLiveDocument/" + path;
	};		

}
