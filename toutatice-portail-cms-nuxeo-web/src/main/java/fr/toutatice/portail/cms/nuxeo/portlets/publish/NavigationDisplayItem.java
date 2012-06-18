package fr.toutatice.portail.cms.nuxeo.portlets.publish;

import java.util.ArrayList;
import java.util.List;

public class NavigationDisplayItem {
	/** The display title of the  link. */
	private String title;
	
	/** The absolute URL of the  link. */
	private String url;
	
	private boolean external;
	
	private boolean selected;
	
	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	private List<NavigationDisplayItem> childrens = new ArrayList<NavigationDisplayItem>();
	
	public List<NavigationDisplayItem> getChildrens() {
		return childrens;
	}

	public void setChildrens(List<NavigationDisplayItem> childrens) {
		this.childrens = childrens;
	}

	public boolean isExternal() {
		return external;
	}

	public void setExternal(boolean external) {
		this.external = external;
	}

	/** Default constructor. */
	public NavigationDisplayItem() {
		this(null, "/", false, false);
	}
	
	/** Canonical constructor.*/
	public NavigationDisplayItem(String aTitle, String anUrl, boolean anExternal, boolean anSelected) {
		super();
		title = aTitle;
		url = anUrl;
		external = anExternal;
		selected = anSelected;
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
