package fr.toutatice.portail.cms.nuxeo.api.forms;

import java.util.Map;


/**
 * The Class FormFilterContext.
 */
public class FormFilterContext {
    
    /** The variables. */
    private Map<String, String> variables;

    
    /** The filters params. */
    private Map<String, Map<String, String>> filtersParams;
    

    /** The actors. */
    FormActors actors ;
    
    /** The action id. */
    String actionId;
    
    /**
     * Instantiates a new form filter context.
     */
    public FormFilterContext(Map<String, Map<String, String>> filtersParams) {
        super();
        this.filtersParams = filtersParams;
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
    public String getParamValue(FormFilter filter, String paramKey) {
        if (filter instanceof FormFilterInstance) {
            Map<String, String> paramsMap = filtersParams.get(((FormFilterInstance) filter).getName());
            if (paramsMap != null) {
                return paramsMap.get(paramKey);
            }
        }
        return null;
    }
}
