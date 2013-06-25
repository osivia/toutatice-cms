package fr.toutatice.portail.cms.nuxeo.portlets.files;


public class SubDocumentType {
    
    private String url;
    private boolean portalView;
    private String name;
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public boolean isPortalView() {
        return portalView;
    }
    
    public void setPortalView(boolean portalView) {
        this.portalView = portalView;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public SubDocumentType() {
        super();
    }
    
    

}
