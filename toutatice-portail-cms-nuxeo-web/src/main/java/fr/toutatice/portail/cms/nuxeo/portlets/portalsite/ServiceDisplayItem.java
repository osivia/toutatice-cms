package fr.toutatice.portail.cms.nuxeo.portlets.portalsite;

public class ServiceDisplayItem {
	/** The display title of the  link. */
	private String title;
	
	/** The absolute URL of the  link. */
	private String url;
	
	private boolean external;
	
	public boolean isExternal() {
		return external;
	}

	public void setExternal(boolean external) {
		this.external = external;
	}

	/** Default constructor. */
	public ServiceDisplayItem() {
		this(null, "/", false);
	}
	
	/** Canonical constructor.*/
	public ServiceDisplayItem(String aTitle, String anUrl, boolean anExternal) {
		super();
		title = aTitle;
		url = anUrl;
		external = anExternal;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String aTitle) {
		this.title = aTitle;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String anUrl) {
		this.url = anUrl;
	}
	
	public String toString() {
		return super.toString() + " {" + title + ": " + url + "}";
	}		

}
