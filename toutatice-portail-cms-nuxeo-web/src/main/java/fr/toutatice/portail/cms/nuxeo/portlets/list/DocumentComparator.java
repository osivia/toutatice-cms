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
package fr.toutatice.portail.cms.nuxeo.portlets.list;

import java.util.Comparator;
import java.util.Map;

import org.nuxeo.ecm.automation.client.model.Document;

/**
 * Comparateur de documents Nuxeo en fnoction d'un ordre passé en paramètre
 *
 */
public class DocumentComparator implements Comparator<Document> {

	/** ordre défini par l'utilisateur */
	private Map<String, Integer> documentOrder;
	
	public DocumentComparator(Map<String, Integer> documentOrder) {
		this.documentOrder = documentOrder;
	}
	
	public int compare(Document docA, Document docB) {
		Integer a = documentOrder.get(docA.getPath());
		Integer b = documentOrder.get(docB.getPath());
		
		return a.compareTo(b);
	}
}
