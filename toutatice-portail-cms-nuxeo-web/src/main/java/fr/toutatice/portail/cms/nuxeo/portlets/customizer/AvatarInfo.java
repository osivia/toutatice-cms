package fr.toutatice.portail.cms.nuxeo.portlets.customizer;

import java.util.Date;

/**
 * Keep informations on user avatar
 */
public class AvatarInfo {
    
	private final String   timeStamp;
	private Boolean  genericResource =null;
	private String   digest=null;
	


    private Boolean  fetched = false;

    

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
	


    public Boolean isFetched() {
        return fetched;
    }
    
    public void setFetched(Boolean fetched) {
        this.fetched = fetched;
    }
    
    
    public String getDigest() {
        return digest;
    }
    
    public void setDigest(String digest) {
        this.digest = digest;
    }

}
