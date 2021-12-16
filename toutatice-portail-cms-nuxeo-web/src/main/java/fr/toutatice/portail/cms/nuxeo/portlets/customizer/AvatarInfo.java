package fr.toutatice.portail.cms.nuxeo.portlets.customizer;

import java.util.Date;

import org.osivia.portal.core.cms.CMSBinaryContent;

/**
 * Keep informations on user avatar
 */
public class AvatarInfo {
    
	private final String   timeStamp;
	private Boolean  genericResource =null;
	private String   digest=null;
	private CMSBinaryContent binaryContent = null;
	


    


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
	protected void setGenericResource(Boolean genericResource) {
		this.genericResource = genericResource;
	}
	


	protected Boolean isFetched() {
        return fetched;
    }
    
    protected void setFetched(Boolean fetched) {
        this.fetched = fetched;
    }
    
    
    public String getDigest() {
        return digest;
    }
    
    public void setDigest(String digest) {
        this.digest = digest;
    }

    public CMSBinaryContent getBinaryContent() {
        return binaryContent;
    }
    
    protected void setBinaryContent(CMSBinaryContent binaryContent) {
        this.binaryContent = binaryContent;
    }
    
}
