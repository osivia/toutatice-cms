/*
 * 
 */
package fr.toutatice.portail.cms.nuxeo.portlets.list;

// TODO: Auto-generated Javadoc
/**
 * List configuration java-bean.
 *
 * @author Cédric Krommenhoek
 */
public class ListConfiguration {

    /** Nuxeo request. */
    private String nuxeoRequest;
    /** BeanShell interpretation indicator. */
    private boolean beanShell;
    /** Version. */
    private String version;
    /** Content filter. */
    private String contentFilter;
    /** Scope. */
    private String scope;
    /** Metadata display indicator. */
    private boolean metadataDisplay;
    /** Nuxeo request display indicator. */
    private boolean nuxeoRequestDisplay;
    /** Results limit. */
    private Integer resultsLimit;
    /** Normal view pagination. */
    private Integer normalPagination;
    /** Maximized view pagination. */
    private Integer maximizedPagination;
    /** Template. */
    private String template;
    /** Permalink reference. */
    private String permalinkReference;
    /** RSS reference. */
    private String rssReference;
    /** RSS title. */
    private String rssTitle;
    /** Creation parent container path. */
    private String creationParentPath;
    /** Creation content type. */
    private String creationContentType;
    
    /**  Menu bar injection. */
    private boolean spaceMenuBar;

    



    
    /**
     * Gets the space menu bar.
     *
     * @return the space menu bar
     */
    public boolean isSpaceMenuBar() {
        return spaceMenuBar;
    }


    
    /**
     * Checks if is space menu bar.
     *
     * @param spaceMenuBar the space menu bar
     */
    public void setSpaceMenuBar(boolean spaceMenuBar) {
        this.spaceMenuBar = spaceMenuBar;
    }


    /**
     * Default constructor.
     */
    public ListConfiguration() {
        super();
    }


    /**
     * Getter for nuxeoRequest.
     *
     * @return the nuxeoRequest
     */
    public String getNuxeoRequest() {
        return this.nuxeoRequest;
    }

    /**
     * Setter for nuxeoRequest.
     *
     * @param nuxeoRequest the nuxeoRequest to set
     */
    public void setNuxeoRequest(String nuxeoRequest) {
        this.nuxeoRequest = nuxeoRequest;
    }

    /**
     * Getter for beanShell.
     *
     * @return the beanShell
     */
    public boolean isBeanShell() {
        return this.beanShell;
    }

    /**
     * Setter for beanShell.
     *
     * @param beanShell the beanShell to set
     */
    public void setBeanShell(boolean beanShell) {
        this.beanShell = beanShell;
    }

    /**
     * Getter for version.
     *
     * @return the version
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Setter for version.
     *
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Getter for contentFilter.
     *
     * @return the contentFilter
     */
    public String getContentFilter() {
        return this.contentFilter;
    }

    /**
     * Setter for contentFilter.
     *
     * @param contentFilter the contentFilter to set
     */
    public void setContentFilter(String contentFilter) {
        this.contentFilter = contentFilter;
    }

    /**
     * Getter for scope.
     *
     * @return the scope
     */
    public String getScope() {
        return this.scope;
    }

    /**
     * Setter for scope.
     *
     * @param scope the scope to set
     */
    public void setScope(String scope) {
        this.scope = scope;
    }

    /**
     * Getter for metadataDisplay.
     *
     * @return the metadataDisplay
     */
    public boolean isMetadataDisplay() {
        return this.metadataDisplay;
    }

    /**
     * Setter for metadataDisplay.
     *
     * @param metadataDisplay the metadataDisplay to set
     */
    public void setMetadataDisplay(boolean metadataDisplay) {
        this.metadataDisplay = metadataDisplay;
    }

    /**
     * Getter for nuxeoRequestDisplay.
     *
     * @return the nuxeoRequestDisplay
     */
    public boolean isNuxeoRequestDisplay() {
        return this.nuxeoRequestDisplay;
    }

    /**
     * Setter for nuxeoRequestDisplay.
     *
     * @param nuxeoRequestDisplay the nuxeoRequestDisplay to set
     */
    public void setNuxeoRequestDisplay(boolean nuxeoRequestDisplay) {
        this.nuxeoRequestDisplay = nuxeoRequestDisplay;
    }

    /**
     * Getter for resultsLimit.
     *
     * @return the resultsLimit
     */
    public Integer getResultsLimit() {
        return this.resultsLimit;
    }

    /**
     * Setter for resultsLimit.
     *
     * @param resultsLimit the resultsLimit to set
     */
    public void setResultsLimit(Integer resultsLimit) {
        this.resultsLimit = resultsLimit;
    }

    /**
     * Getter for normalPagination.
     *
     * @return the normalPagination
     */
    public Integer getNormalPagination() {
        return this.normalPagination;
    }

    /**
     * Setter for normalPagination.
     *
     * @param normalPagination the normalPagination to set
     */
    public void setNormalPagination(Integer normalPagination) {
        this.normalPagination = normalPagination;
    }

    /**
     * Getter for maximizedPagination.
     *
     * @return the maximizedPagination
     */
    public Integer getMaximizedPagination() {
        return this.maximizedPagination;
    }

    /**
     * Setter for maximizedPagination.
     *
     * @param maximizedPagination the maximizedPagination to set
     */
    public void setMaximizedPagination(Integer maximizedPagination) {
        this.maximizedPagination = maximizedPagination;
    }

    /**
     * Getter for template.
     *
     * @return the template
     */
    public String getTemplate() {
        return this.template;
    }

    /**
     * Setter for template.
     *
     * @param template the template to set
     */
    public void setTemplate(String template) {
        this.template = template;
    }

    /**
     * Getter for permalinkReference.
     *
     * @return the permalinkReference
     */
    public String getPermalinkReference() {
        return this.permalinkReference;
    }

    /**
     * Setter for permalinkReference.
     *
     * @param permalinkReference the permalinkReference to set
     */
    public void setPermalinkReference(String permalinkReference) {
        this.permalinkReference = permalinkReference;
    }

    /**
     * Getter for rssReference.
     *
     * @return the rssReference
     */
    public String getRssReference() {
        return this.rssReference;
    }

    /**
     * Setter for rssReference.
     *
     * @param rssReference the rssReference to set
     */
    public void setRssReference(String rssReference) {
        this.rssReference = rssReference;
    }

    /**
     * Getter for rssTitle.
     *
     * @return the rssTitle
     */
    public String getRssTitle() {
        return this.rssTitle;
    }

    /**
     * Setter for rssTitle.
     *
     * @param rssTitle the rssTitle to set
     */
    public void setRssTitle(String rssTitle) {
        this.rssTitle = rssTitle;
    }

    /**
     * Getter for creationParentPath.
     *
     * @return the creationParentPath
     */
    public String getCreationParentPath() {
        return this.creationParentPath;
    }

    /**
     * Setter for creationParentPath.
     *
     * @param creationParentPath the creationParentPath to set
     */
    public void setCreationParentPath(String creationParentPath) {
        this.creationParentPath = creationParentPath;
    }

    /**
     * Getter for creationContentType.
     *
     * @return the creationContentType
     */
    public String getCreationContentType() {
        return this.creationContentType;
    }

    /**
     * Setter for creationContentType.
     *
     * @param creationContentType the creationContentType to set
     */
    public void setCreationContentType(String creationContentType) {
        this.creationContentType = creationContentType;
    }

}
