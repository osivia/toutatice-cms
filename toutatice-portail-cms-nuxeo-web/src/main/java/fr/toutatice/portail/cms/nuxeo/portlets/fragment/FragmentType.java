package fr.toutatice.portail.cms.nuxeo.portlets.fragment;

public class FragmentType {
	
	private String key;
	private String label;
	public  String adminJspName;
	public  String viewJspName;

	
	
	public String getAdminJspName() {
		return adminJspName;
	}
	public void setAdminJspName(String adminJspName) {
		this.adminJspName = adminJspName;
	}
	public String getViewJspName() {
		return viewJspName;
	}
	public void setViewJspName(String viewJspName) {
		this.viewJspName = viewJspName;
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
	public FragmentType(String key, String label, String viewJspName, String adminJspName) {
		super();
		this.key = key;
		this.label = label;
		this.adminJspName = adminJspName;
		this.viewJspName = viewJspName;
	}



}
