package fr.toutatice.portail.cms.nuxeo.portlets.customizer;

import java.util.Date;

public class AvatarInfo {
	private final String   timeStamp;
	private Boolean  genericResource;
	
	
	public AvatarInfo() {
		timeStamp = Long.toString(new Date().getTime());
		genericResource = null;
	}
	public String getTimeStamp() {
		return timeStamp;
	}

	public Boolean getGenericResource() {
		return genericResource;
	}
	public void setGenericResource(Boolean genericResource) {
		this.genericResource = genericResource;
	}

}
