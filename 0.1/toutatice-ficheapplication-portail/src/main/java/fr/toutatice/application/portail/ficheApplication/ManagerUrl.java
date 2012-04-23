package fr.toutatice.application.portail.ficheApplication;





public class ManagerUrl implements Comparable{

	private String description;
	private String id;
	private String url;
	private String dn;
	private boolean clicable;
	private boolean self;
	
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String getDn() {
		return dn;
	}
	public void setDn(String dn) {
		this.dn = dn;
	}
	
	public boolean isClicable() {
		return clicable;
	}
	public void setClicable(boolean clicable) {
		this.clicable = clicable;
	}
	
	public boolean isSelf() {
		return self;
	}
	public void setSelf(boolean self) {
		this.self = self;
	}
	public ManagerUrl(String description, String id, String dn, String url, boolean clicable, boolean self) {
		super();
		this.description = description;
		this.id = id;
		this.url = url;
		this.dn = dn;
		this.clicable = clicable;
		this.self = self;
	}
	
	public int compareTo(Object obj) {
		ManagerUrl m = (ManagerUrl) obj;
		return this.getDescription().toLowerCase().compareTo(m.getDescription().toLowerCase());
	}
	
	
	
	
	
}
