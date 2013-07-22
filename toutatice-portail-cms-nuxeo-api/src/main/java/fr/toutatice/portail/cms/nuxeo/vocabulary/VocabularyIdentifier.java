package fr.toutatice.portail.cms.nuxeo.vocabulary;

public class VocabularyIdentifier {
	
    private String vocabularies;
	private String id;
	
	public VocabularyIdentifier(String id, String vocabularies) {
		this.id = id;
		this.vocabularies = vocabularies;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getVocabularies() {
		return vocabularies;
	}
	public void setVocabularies(String vocabularies) {
		this.vocabularies = vocabularies;
	}
	
}
