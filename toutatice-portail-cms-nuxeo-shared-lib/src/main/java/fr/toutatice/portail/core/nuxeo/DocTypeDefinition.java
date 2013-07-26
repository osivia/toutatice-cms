package fr.toutatice.portail.core.nuxeo;


public class DocTypeDefinition extends Object {
    

    private String name;
    private String displayName;
    private boolean supportsPortalForms;
     
    
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
