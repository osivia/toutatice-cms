package fr.toutatice.portail.core.nuxeo;

import java.util.List;


public class DocTypeDefinition extends Object {
    

    private String name;
    private String displayName;
    private boolean supportsPortalForms;
    
    // v2.0.21 : ajout sous-types
    private List<String> portalFormSubTypes;
    
    public List<String> getPortalFormSubTypes() {
		return portalFormSubTypes;
	}


	public void setPortalFormSubTypes(List<String> portalFormSubTypes) {
		this.portalFormSubTypes = portalFormSubTypes;
	}


	public boolean isSupportingPortalForm() {
        return supportsPortalForms;
    }

    
    public void setSupportingPortalForm(boolean supportsPortalForms) {
        this.supportsPortalForms = supportsPortalForms;
    }

    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
   
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public DocTypeDefinition() {
        super();
    }
  
}
