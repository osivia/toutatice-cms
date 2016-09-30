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

import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

public class FolderGetParentCommand implements INuxeoCommand {
	
	Document folder;
	
	public FolderGetParentCommand(Document folder) {
		super();
		this.folder = folder;
	}
	
	public Object execute( Session session)	throws Exception {
		
  	 Document parent = (Document) session.newRequest("Document.GetParent").setHeader(
                Constants.HEADER_NX_SCHEMAS, "*").setInput(folder).execute();
	
      return parent;
	}

	public String getId() {
		return "FolderGetParentCommand/" + folder.getPath();
	};		

}
