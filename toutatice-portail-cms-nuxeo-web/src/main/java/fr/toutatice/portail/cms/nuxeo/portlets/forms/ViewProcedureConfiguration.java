package fr.toutatice.portail.cms.nuxeo.portlets.forms;

import fr.toutatice.portail.cms.nuxeo.portlets.list.ListConfiguration;


/**
 * Configuration de ViewProcedurePortlet
 * 
 * @author Dorian Licois
 */
public class ViewProcedureConfiguration extends ListConfiguration {

    /** procedureModelId */
    private String procedureModelId;

    /** DashboardId */
    private String dashboardId;


    public ViewProcedureConfiguration(ListConfiguration listConfiguration) {
        setBeanShell(listConfiguration.isBeanShell());
        setContentFilter(listConfiguration.getContentFilter());
        setCreationContentType(listConfiguration.getCreationContentType());
        setCreationParentPath(listConfiguration.getCreationContentType());
        setMaximizedPagination(listConfiguration.getMaximizedPagination());
        setMetadataDisplay(listConfiguration.isMetadataDisplay());
        setNormalPagination(listConfiguration.getNormalPagination());
        setNuxeoRequest(listConfiguration.getNuxeoRequest());
        setNuxeoRequestDisplay(listConfiguration.isNuxeoRequestDisplay());
        setPermalinkReference(listConfiguration.getPermalinkReference());
        setResultsLimit(listConfiguration.getResultsLimit());
        setRssReference(listConfiguration.getRssReference());
        setRssTitle(listConfiguration.getRssTitle());
        setScope(listConfiguration.getScope());
        setTemplate(listConfiguration.getTemplate());
        setUseES(listConfiguration.isUseES());
        setVersion(listConfiguration.getVersion());
    }

    /**
     * Getter for procedureModelId.
     * 
     * @return the procedureModelId
     */
    public String getProcedureModelId() {
        return procedureModelId;
    }


    /**
     * Setter for procedureModelId.
     * 
     * @param procedureModelId the procedureModelId to set
     */
    public void setProcedureModelId(String procedureModelId) {
        this.procedureModelId = procedureModelId;
    }


    /**
     * Getter for DashboardId.
     * 
     * @return the dashboardId
     */
    public String getDashboardId() {
        return dashboardId;
    }


    /**
     * Setter for DashboardId.
     * 
     * @param dashboardId the dashboardId to set
     */
    public void setDashboardId(String dashboardId) {
        dashboardId = dashboardId;
    }
}
