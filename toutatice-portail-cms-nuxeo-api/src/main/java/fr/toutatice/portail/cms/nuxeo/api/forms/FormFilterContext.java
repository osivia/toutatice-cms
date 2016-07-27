package fr.toutatice.portail.cms.nuxeo.api.forms;

import java.util.Map;

import org.osivia.portal.api.context.PortalControllerContext;


/**
 * The Class FormFilterContext.
 */
public class FormFilterContext {
    
    /** The variables. */
    private Map<String, String> variables;

    /** The filters params. */
    private Map<String, Map<String, String>> filtersParams;
    
    /** portalControllerContext */
    private PortalControllerContext portalControllerContext;

    /** The actors. */
    private FormActors actors;
    
    /** The action id. */
    private String actionId;

    /** initiator */
    private String initiator;
    
    /**
     * Instantiates a new form filter context.
     */
    public FormFilterContext(Map<String, Map<String, String>> filtersParams, String initiator) {
        super();
        this.filtersParams = filtersParams;
        this.initiator = initiator;
    }

    /**
     * Gets the variables.
     *
     * @return the variables
     */
    public Map<String, String> getVariables() {
        return variables;
    }
    
    /**
     * Sets the variables.
     *
     * @param variables the variables
     */
    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }
    
    /**
     * Gets the actors.
     *
     * @return the actors
     */
    public FormActors getActors() {
        return actors;
    }
    
    /**
     * Sets the actors.
     *
     * @param actors the new actors
     */
    public void setActors(FormActors actors) {
        this.actors = actors;
    }
    
    /**
     * Gets the action id.
     *
     * @return the action id
     */
    public String getActionId() {
        return actionId;
    }
    
    /**
     * Sets the action id.
     *
     * @param actionId the new action id
     */
    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    /**
     * returns the parameter value of a filter
     * 
     * @param filter
     * @param paramKey
     * @return
     */
    public String getParamValue(FormFilterExecutor executor, String paramKey) {
        Map<String, String> paramsMap = filtersParams.get(executor.getCurrentFilterInstanceId());
        if (paramsMap != null) {
            return paramsMap.get(paramKey);
        }
        return null;
    }

    /**
     * Getter for portalControllerContext.
     * @return the portalControllerContext
     */
    public PortalControllerContext getPortalControllerContext() {
        return portalControllerContext;
    }

    /**
     * Setter for portalControllerContext.
     * @param portalControllerContext the portalControllerContext to set
     */
    public void setPortalControllerContext(PortalControllerContext portalControllerContext) {
        this.portalControllerContext = portalControllerContext;
    }

    /**
     * Getter for initiator.
     * @return the initiator
     */
    public String getInitiator() {
        return initiator;
    }
}
