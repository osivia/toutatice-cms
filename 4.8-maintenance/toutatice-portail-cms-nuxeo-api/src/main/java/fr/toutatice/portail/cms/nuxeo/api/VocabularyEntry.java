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
package fr.toutatice.portail.cms.nuxeo.api;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.osivia.portal.api.cache.services.IGlobalParameters;


/**
 * The Class VocabularyEntry.
 * 
 * Bean designed for nuxeo vocabulary representation
 */
public class VocabularyEntry implements IGlobalParameters {
	
	/** The id. */
	private String id;
	
	/** The label. */
	private String label;
	
	/**
	 * Gets the label.
	 *
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/** The children. */
	private Map<String, VocabularyEntry> children;
	
	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public String getId() {
		return id;
	}


	/**
	 * Gets the children.
	 *
	 * @return the children
	 */
	public Map<String, VocabularyEntry> getChildren() {
		return children;
	}

	
	/**
	 * Gets the child.
	 *
	 * @param childId the child id
	 * @return the child
	 */
	public  VocabularyEntry getChild( String childId) {
		return getChildren().get(childId);
	}


	/**
	 * Instantiates a new vocabulary entry.
	 *
	 * @param id the id
	 * @param label the label
	 */
	public VocabularyEntry(String id, String label) {
		super();
		this.id = id;
		this.label = label;
		this.children = Collections.synchronizedMap(new LinkedHashMap<String, VocabularyEntry>());
	}
	
	
	

}
