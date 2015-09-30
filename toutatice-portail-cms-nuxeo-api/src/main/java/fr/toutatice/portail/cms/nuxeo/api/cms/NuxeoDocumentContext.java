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
package fr.toutatice.portail.cms.nuxeo.api.cms;

import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.cms.impl.AbstractDocumentContext;


/**
 * Specialize the document context for Nuxeo.
 * @author Loïc Billon
 *
 */
public class NuxeoDocumentContext extends AbstractDocumentContext<Document> {

	
	/* (non-Javadoc)
	 * @see org.osivia.portal.api.cms.DocumentContext#getDocument()
	 */
	@Override
	public Document getDoc() {
		return document;
	}

	/* (non-Javadoc)
	 * @see org.osivia.portal.api.cms.DocumentContext#setDocument(org.osivia.portal.api.cms.EcmDocument)
	 */
	@Override
	public void setDoc(Document document) {
		this.document = document;
		
	}




}
