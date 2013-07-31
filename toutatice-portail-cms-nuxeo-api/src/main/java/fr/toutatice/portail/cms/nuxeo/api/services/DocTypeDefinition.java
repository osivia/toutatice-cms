package fr.toutatice.portail.cms.nuxeo.api.services;


public class DocTypeDefinition extends Object {
    

    private String name;
    private String displayName;
    private boolean supportsPortalForms;
    private boolean webPage;
     
    
    public boolean isSupportingPortalForm() {
        return supportsPortalForms;
    }

    
    public void setSupportingPortalForm(boolean supportsPortalForms) {
        this.supportsPortalForms = supportsPortalForms;
    }


    /**
     * @return the webPage
     */
    public boolean isWebPage() {
        return webPage;
    }

    /**
     * @param webPage the webPage to set
     */
    public void setWebPage(boolean webPage) {
        this.webPage = webPage;
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
