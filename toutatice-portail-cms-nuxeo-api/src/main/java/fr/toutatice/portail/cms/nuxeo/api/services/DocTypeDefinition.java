package fr.toutatice.portail.cms.nuxeo.api.services;

import java.util.List;


public class DocTypeDefinition extends Object {
    

    private String name;
    private String displayName;
    private boolean supportsPortalForms;
    private List<String> portalFormSubTypes;    
     
    
    public boolean isSupportingPortalForm() {
        return supportsPortalForms;
    }

    
    public void setSupportingPortalForm(boolean supportsPortalForms) {
        this.supportsPortalForms = supportsPortalForms;
    }

    public List<String> getPortalFormSubTypes() {
        return portalFormSubTypes;
    }
    
    public void setPortalFormSubTypes(List<String> portalFormSubTypes) {
        this.portalFormSubTypes = portalFormSubTypes;
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
