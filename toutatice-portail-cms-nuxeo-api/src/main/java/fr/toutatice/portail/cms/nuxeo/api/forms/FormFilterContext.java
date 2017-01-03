package fr.toutatice.portail.cms.nuxeo.api.forms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.context.PortalControllerContext;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoServiceFactory;


/**
 * The Class FormFilterContext.
 */
public class FormFilterContext {

    /** Portal controller context. */
    private PortalControllerContext portalControllerContext;

    /** The variables. */
    private Map<String, String> variables;
    /** The filters params. */
    private Map<String, Map<String, String>> filtersParams;

    /** Model webId. */
    private String modelWebId;
    /** Procedure instance UUID. */
    private String procedureInstanceUuid;
    /** The action id. */
    private String actionId;
    /** procedureInitiator */
    private String procedureInitiator;
    /** taskInitiator */
    private String taskInitiator;
    /** nextStep */
    private String nextStep;


    /** Actors. */
    private final Set<String> actors;
    /** Additional authorizations. */
    private final Set<String> additionalAuthorizations;


    /** Forms service. */
    private final IFormsService formsService;


    /**
     * Instantiates a new form filter context.
     */
    public FormFilterContext(Map<String, Map<String, String>> filtersParams, String procedureInitiator, String taskInitiator, String nextStep) {
        super();
        this.filtersParams = filtersParams;
        this.procedureInitiator = procedureInitiator;
        this.nextStep = nextStep;
        this.taskInitiator = taskInitiator;
        this.actors = new HashSet<>();
        this.additionalAuthorizations = new HashSet<>();

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
     * Getter for modelWebId.
     * 
     * @return the modelWebId
     */
    public String getModelWebId() {
        return modelWebId;
    }

    /**
     * Setter for modelWebId.
     * 
     * @param modelWebId the modelWebId to set
     */
    public void setModelWebId(String modelWebId) {
        this.modelWebId = modelWebId;
    }

    /**
     * Getter for procedureInstanceUuid.
     * 
     * @return the procedureInstanceUuid
     */
    public String getProcedureInstanceUuid() {
        return procedureInstanceUuid;
    }

    /**
     * Setter for procedureInstanceUuid.
     * 
     * @param procedureInstanceUuid the procedureInstanceUuid to set
     */
    public void setProcedureInstanceUuid(String procedureInstanceUuid) {
        this.procedureInstanceUuid = procedureInstanceUuid;
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
                Map<String, String> variables = new HashMap<String, String>(getVariables());
                variables.put("procedureInitiator", procedureInitiator);
                variables.put("taskInitiator", taskInitiator);
                return formsService.transform(portalControllerContext, filterParameters, variables);
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
     * Getter for filtersParams.
     *
     * @return the filtersParams
     */
    public Map<String, Map<String, String>> getFiltersParams() {
        return filtersParams;
    }


    /**
     * Getter for procedureInitiator.
     *
     * @return the procedureInitiator
     */
    public String getProcedureInitiator() {
        return procedureInitiator;
    }

    /**
     * Getter for nextStep.
     *
     * @return the nextStep
     */
    public String getNextStep() {
        return nextStep;
    }

    /**
     * Setter for nextStep.
     *
     * @param nextStep the nextStep to set
     */
    public void setNextStep(String nextStep) {
        this.nextStep = nextStep;
    }

    /**
     * Getter for actors.
     * 
     * @return the actors
     */
    public Set<String> getActors() {
        return actors;
    }

    /**
     * Getter for additionalAuthorizations.
     * 
     * @return the additionalAuthorizations
     */
    public Set<String> getAdditionalAuthorizations() {
        return additionalAuthorizations;
    }

}
