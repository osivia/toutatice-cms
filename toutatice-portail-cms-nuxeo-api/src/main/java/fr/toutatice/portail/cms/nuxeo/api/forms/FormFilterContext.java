package fr.toutatice.portail.cms.nuxeo.api.forms;

import java.util.HashMap;
import java.util.Map;

import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.context.PortalControllerContext;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoServiceFactory;


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

    /** Forms service. */
    private final IFormsService formsService;


    /**
     * Instantiates a new form filter context.
     */
    public FormFilterContext(Map<String, Map<String, String>> filtersParams, String initiator) {
        super();
        this.filtersParams = filtersParams;
        this.initiator = initiator;

        // Forms service
        this.formsService = NuxeoServiceFactory.getFormsService();
    }

    /**
     * Gets the variables.
     *
     * @return the variables
     */
    public Map<String, String> getVariables() {
        return this.variables;
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
        return this.actors;
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
        return this.actionId;
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
        Map<String, String> paramsMap = this.filtersParams.get(executor.getCurrentFilterInstanceId());
        if (paramsMap != null) {
            String filterParameters = paramsMap.get(paramKey);
            try {
                Map<String, String> variables = new HashMap<String, String>(this.variables);
                variables.put("initiator", this.initiator);
                return this.formsService.transform(this.portalControllerContext, filterParameters, variables);
            } catch (PortalException e) {
                throw new NuxeoException(e);
            }
        }
        return null;
    }

    /**
     * Getter for portalControllerContext.
     * 
     * @return the portalControllerContext
     */
    public PortalControllerContext getPortalControllerContext() {
        return this.portalControllerContext;
    }

    /**
     * Setter for portalControllerContext.
     * 
     * @param portalControllerContext the portalControllerContext to set
     */
    public void setPortalControllerContext(PortalControllerContext portalControllerContext) {
        this.portalControllerContext = portalControllerContext;
    }

    /**
     * Getter for initiator.
     * 
     * @return the initiator
     */
    public String getInitiator() {
        return this.initiator;
    }
}
