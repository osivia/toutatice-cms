package fr.toutatice.portail.cms.nuxeo.portlets.customizer;

public class ListTemplate {
	
	private String key;
	private String label;
	private String schemas;
	private ITemplateModule module;
	
	public ITemplateModule getModule() {
		return module;
	}
	public void setModule(ITemplateModule module) {
		this.module = module;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getSchemas() {
		return schemas;
	}
	public void setSchemas(String schemas) {
		this.schemas = schemas;
	}
	public ListTemplate(String key, String label, String schemas) {
		super();
		this.key = key;
		this.label = label;
		this.schemas = schemas;
	}
	

}
