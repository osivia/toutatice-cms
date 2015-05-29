/*
 * (C) Copyright 2015 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
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

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.nuxeo.ecm.automation.client.AutomationException;
import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.ecm.EcmCommand;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

/**
 * Execute an ECM command describe as a EcmCommand
 * @author loic
 *
 */
public class NuxeoCommandDelegate implements INuxeoCommand {
	
	EcmCommand command;
	
	Document doc;

	public NuxeoCommandDelegate(EcmCommand command, Document doc) {
		super();
		this.command = command;
		this.doc = doc;

	}

	public Object execute(Session session) throws Exception {

		String nxRealCommand = command.getRealCommand();
		
		Map<String, Object> parameters = command.getRealCommandParameters();


		if (nxRealCommand != null) {

			OperationRequest operationRequest = session.newRequest(nxRealCommand)
					.setHeader(Constants.HEADER_NX_SCHEMAS, "*")
					.setInput(doc);
			
			if(MapUtils.isNotEmpty(parameters)) {
				for(Map.Entry<String, Object> entry : parameters.entrySet()) {
					operationRequest.set(entry.getKey(), entry.getValue());
				}
			}
			
			doc = (Document) operationRequest.execute();
			


		} else {
			throw new AutomationException("L'opération" + command.getCommandName() + " n'est pas reconnue.");
		}


		return doc;
	}

	public String getId() {
		
		return command.getCommandName();
		
	};

}
