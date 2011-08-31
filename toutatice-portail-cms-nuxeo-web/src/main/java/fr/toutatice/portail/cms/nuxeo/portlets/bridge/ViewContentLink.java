package fr.toutatice.portail.cms.nuxeo.portlets.bridge;

public class ViewContentLink {
	
	private String url;
	private boolean external = false;
	
	public boolean isExternal() {
		return external;
	}

	public void setExternal(boolean external) {
		this.external = external;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public ViewContentLink(String url, boolean external) {
		super();
		this.url = url;
		this.external = external;
	}
	
	
	
	

}
