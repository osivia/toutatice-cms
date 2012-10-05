package fr.toutatice.portail.cms.nuxeo.vocabulary;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import fr.toutatice.portail.api.cache.services.IGlobalParameters;

public class VocabularyEntry implements IGlobalParameters{
	
	private String id;
	private String label;
	
	public String getLabel() {
		return label;
	}

	private Map<String, VocabularyEntry> children;
	
	public String getId() {
		return id;
	}


	public Map<String, VocabularyEntry> getChildren() {
		return children;
	}

	
	public  VocabularyEntry getChild( String childId) {
		return getChildren().get(childId);
	}


	public VocabularyEntry(String id, String label) {
		super();
		this.id = id;
		this.label = label;
		this.children = Collections.synchronizedMap(new LinkedHashMap<String, VocabularyEntry>());
	}
	
	
	

}
