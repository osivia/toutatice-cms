package fr.toutatice.portail.cms.nuxeo.api.forms;

import java.util.HashMap;
import java.util.Map;


/**
 * The Class FormFilterContext.
 */
public class FormFilterContext {
    
    /** The variables. */
    Map<String, String> variables ;
    
    /** The filter params. */
    Map<String, String> filterParams ;
    
    
    /**
     * Gets the filter params.
     *
     * @return the filter params
     */
    public Map<String, String> getFilterParams() {
        return filterParams;
    }

    
    /**
     * Sets the filter params.
     *
     * @param filterParams the filter params
     */
    public void setFilterParams(Map<String, String> filterParams) {
        this.filterParams = filterParams;
    }

    /** The actors. */
    FormActors actors ;
    
    /** The action id. */
    String actionId;
    
    /**
     * Instantiates a new form filter context.
     */
    public FormFilterContext() {
        super();
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

}
