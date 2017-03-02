package fr.toutatice.portail.cms.nuxeo.portlets.forms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.internationalization.Bundle;
import org.osivia.portal.api.internationalization.IBundleFactory;
import org.osivia.portal.api.internationalization.IInternationalizationService;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.ICMSService;
import org.osivia.portal.core.cms.ICMSServiceLocator;

import de.odysseus.el.ExpressionFactoryImpl;
import de.odysseus.el.util.SimpleContext;
import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.forms.FormFilter;
import fr.toutatice.portail.cms.nuxeo.api.forms.FormFilterContext;
import fr.toutatice.portail.cms.nuxeo.api.forms.FormFilterException;
import fr.toutatice.portail.cms.nuxeo.api.forms.FormFilterExecutor;
import fr.toutatice.portail.cms.nuxeo.api.forms.FormFilterInstance;
import fr.toutatice.portail.cms.nuxeo.api.forms.IFormsService;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.CustomizationPluginMgr;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.DefaultCMSCustomizer;

/**
 * Forms service implementation.
 *
 * @author Cédric Krommenhoek
 * @see IFormsService
 */
public class FormsServiceImpl implements IFormsService {

    /** Thread local. */
    private static ThreadLocal<ThreadLocalContainer> threadLocal = new ThreadLocal<ThreadLocalContainer>();


    /** CMS customizer. */
    private final DefaultCMSCustomizer cmsCustomizer;

    /** CMS service locator. */
    private final ICMSServiceLocator cmsServiceLocator;

    /** Bundle factory. */
    private final IBundleFactory bundleFactory;

    /**
     * Constructor.
     *
     * @param cmsCustomizer CMS customizer
     */
    public FormsServiceImpl(DefaultCMSCustomizer cmsCustomizer) {
        super();
        this.cmsCustomizer = cmsCustomizer;

        // CMS service locator
        this.cmsServiceLocator = Locator.findMBean(ICMSServiceLocator.class, ICMSServiceLocator.MBEAN_NAME);

        // Bundle factory
        IInternationalizationService internationalizationService = (IInternationalizationService) cmsCustomizer.getPortletCtx()
                .getAttribute(Constants.INTERNATIONALIZATION_SERVICE_NAME);
        this.bundleFactory = internationalizationService.getBundleFactory(this.getClass().getClassLoader());
    }


    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Map<String, String> start(PortalControllerContext portalControllerContext, String modelId, Map<String, String> variables)
            throws PortalException, FormFilterException {
        return this.start(portalControllerContext, modelId, null, variables);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> start(PortalControllerContext portalControllerContext, String modelId, String actionId, Map<String, String> variables)
            throws PortalException, FormFilterException {
        // CMS context
        CMSServiceCtx cmsContext = new CMSServiceCtx();
        cmsContext.setPortalControllerContext(portalControllerContext);


        // Model
        String modelWebId = FORMS_WEB_ID_PREFIX + modelId;
        Document model = this.getModel(portalControllerContext, modelWebId);

        // Procedure initiator
        String procedureInitiator = portalControllerContext.getHttpServletRequest().getUserPrincipal().getName();

        // Starting step
        String startingStep = model.getString("pcd:startingStep");
        // Starting step properties
        PropertyMap startingStepProperties = this.getStepProperties(model, startingStep);

        // Action properties
        PropertyMap actionProperties = this.getActionProperties(startingStepProperties, actionId);

        // Next step
        String nextStep = actionProperties.getString("stepReference");

        // Task title
        String title = StringUtils.EMPTY;

        // Next step properties
        PropertyMap nextStepProperties = this.getStepProperties(model, nextStep);
        title = nextStepProperties.getString("name");

        // Actors
        List<String> actors = getActors(model, nextStep, title, nextStepProperties);


        if (variables == null) {
            variables = new HashMap<String, String>();
        }

        // UUID
        String uuid = UUID.randomUUID().toString();
        variables.put("uuid", uuid);

        // Required fields validation
        this.requiredFieldsValidation(portalControllerContext, startingStepProperties, variables);

        // Construction du contexte et appel des filtres
        FormFilterContext filterContext = this.callFilters(actionId, variables, actionProperties, actors, null, portalControllerContext, procedureInitiator,
                null, nextStep);

        if (!StringUtils.equals(ENDSTEP, filterContext.getNextStep())) {
            // Properties
            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put("pi:currentStep", actionProperties.getString("stepReference"));
            properties.put("pi:procedureModelWebId", modelWebId);
            properties.put("pi:globalVariablesValues", this.generateVariablesJSON(variables));

            // Nuxeo command
            INuxeoCommand command = new StartProcedureCommand(title, filterContext.getActors(), properties);
            try {
                this.cmsCustomizer.executeNuxeoCommand(cmsContext, command);
            } catch (CMSException e) {
                throw new PortalException(e);
            }
        }

        return filterContext.getVariables();
    }


    /**
     * @param model
     * @param nextStep
     * @param title
     * @param actors
     * @return
     */
    private List<String> getActors(Document model, String nextStep, String title, PropertyMap nextStepProperties) {
        List<String> actors = new ArrayList<String>();
        if (!StringUtils.equals(ENDSTEP, nextStep)) {
            // add authorizedGroups to actors
            final PropertyList groupsObjectsList = nextStepProperties.getList("actors");
            if (groupsObjectsList != null) {
                for (final Object groupsObject : groupsObjectsList.list()) {
                    actors.add((String) groupsObject);
                }
            }
        }
        return actors;
    }

    /**
     * Process required fields validation.
     *
     * @param portalControllerContext portal controller context
     * @param step current step properties
     * @param variables current variables
     * @throws FormFilterException
     */
    private void requiredFieldsValidation(PortalControllerContext portalControllerContext, PropertyMap step, Map<String, String> variables)
            throws FormFilterException {
        // Internationalization bundle
        Locale locale = portalControllerContext.getHttpServletRequest().getLocale();
        Bundle bundle = this.bundleFactory.getBundle(locale);

        // Step fields
        PropertyList fields = step.getList("globalVariablesReferences");


        List<PropertyMap> fieldsList = new ArrayList<PropertyMap>();
        for (Object field : fields.list()) {
            fieldsList.add((PropertyMap) field);
        }
        Collections.sort(fieldsList, new FieldComparator());

        for (PropertyMap field : fieldsList) {

            // Required field indicator
            boolean required = BooleanUtils.isTrue(field.getBoolean("required"));

            if (required) {
                // Field name
                String name = field.getString("variableName");
                // Field value
                String value = variables.get(name);

                if (StringUtils.isBlank(value)) {
                    // Field label
                    String label = StringUtils.defaultIfEmpty(field.getString("superLabel"), name);

                    // Error message
                    String message = bundle.getString("MESSAGE_MISSING_REQUIRED_FIELD_ERROR", label);

                    throw new FormFilterException(message);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Map<String, String> proceed(PortalControllerContext portalControllerContext, Document task, Map<String, String> variables)
            throws PortalException, FormFilterException {
        return this.proceed(portalControllerContext, task, null, variables);
    }


    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Map<String, String> proceed(PortalControllerContext portalControllerContext, Document task, String actionId, Map<String, String> variables)
            throws PortalException, FormFilterException {
        return this.proceed(portalControllerContext, task.getProperties(), actionId, variables);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Map<String, String> proceed(PortalControllerContext portalControllerContext, PropertyMap taskProperties, Map<String, String> variables)
            throws PortalException, FormFilterException {
        return this.proceed(portalControllerContext, taskProperties, null, variables);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Map<String, String> proceed(PortalControllerContext portalControllerContext, PropertyMap taskProperties, String actionId,
            Map<String, String> variables) throws PortalException, FormFilterException {
        // CMS service
        ICMSService cmsService = cmsServiceLocator.getCMSService();
        // CMS context
        CMSServiceCtx cmsContext = new CMSServiceCtx();
        cmsContext.setPortalControllerContext(portalControllerContext);


        // Procedure instance properties
        PropertyMap instanceProperties = taskProperties.getMap("nt:pi");
        // Procedure instance path
        String instancePath = instanceProperties.getString("ecm:path");
        // Task initiator
        String previousTaskInitiator = taskProperties.getString("nt:initiator");

        // Model document
        String modelWebId = instanceProperties.getString("pi:procedureModelWebId");
        Document model = this.getModel(portalControllerContext, modelWebId);

        // Procedure initiator
        String procedureInitiator = instanceProperties.getString("pi:procedureInitiator");


        // Previous step
        String previousStep = instanceProperties.getString("pi:currentStep");
        // Previous step properties
        PropertyMap previousStepProperties = this.getStepProperties(model, previousStep);

        // Action properties
        PropertyMap actionProperties = this.getActionProperties(previousStepProperties, actionId);

        // Next step
        String nextStep = actionProperties.getString("stepReference");

        // Task title
        String title = StringUtils.EMPTY;

        List<String> actors = ListUtils.EMPTY_LIST;
        if (!StringUtils.equals(ENDSTEP, nextStep)) {
            // Next step properties
            PropertyMap nextStepProperties = this.getStepProperties(model, nextStep);
            title = nextStepProperties.getString("name");
            // Actors
            actors = getActors(model, nextStep, title, nextStepProperties);
        }


        // Global Variables Values
        Map<String, Object> globalVariableValuesMap = instanceProperties.getMap("pi:globalVariablesValues").map();
        Map<String, String> globalVariableValues = new HashMap<String, String>(globalVariableValuesMap.size());
        for (Entry<String, Object> gvvEntry : globalVariableValuesMap.entrySet()) {
            globalVariableValues.put(gvvEntry.getKey(), String.valueOf(gvvEntry.getValue()));
        }

        if (variables == null) {
            variables = new HashMap<String, String>();
        }

        // Required fields validation
        this.requiredFieldsValidation(portalControllerContext, previousStepProperties, variables);

        // construction du contexte et appel des filtres
        FormFilterContext filterContext = this.callFilters(actionId, variables, actionProperties, actors, globalVariableValues, portalControllerContext,
                procedureInitiator, previousTaskInitiator, nextStep);

        // Properties
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("pi:currentStep", actionProperties.getString("stepReference"));
        properties.put("pi:procedureModelWebId", modelWebId);
        properties.put("pi:globalVariablesValues", this.generateVariablesJSON(globalVariableValues));

        // Nuxeo command
        INuxeoCommand command = new UpdateProcedureCommand(instancePath, title, filterContext.getActors(), properties);
        try {
            this.cmsCustomizer.executeNuxeoCommand(cmsContext, command);
        } catch (CMSException e) {
            throw new PortalException(e);
        }


        // Updated variables
        Map<String, String> updatedVariables = filterContext.getVariables();

        // Check if workflow must be deleted
        boolean deleteOnEnding = BooleanUtils.toBoolean(updatedVariables.get(DELETE_ON_ENDING_PARAMETER));
        boolean endStep = ENDSTEP.equals(filterContext.getNextStep());
        if (deleteOnEnding && endStep) {
            // Save current scope
            String savedScope = cmsContext.getScope();

            try {
                cmsContext.setScope("superuser_no_cache");

                cmsService.deleteDocument(cmsContext, instancePath);
            } catch (CMSException e) {
                throw new PortalException(e);
            } finally {
                cmsContext.setScope(savedScope);
            }
        }

        return updatedVariables;
    }


    /**
     * Appel des filtres
     *
     * @param actionId
     * @param variables
     * @param actionProperties
     * @param actors
     * @return
     */
    private FormFilterContext callFilters(String actionId, Map<String, String> variables, PropertyMap actionProperties, List<String> actors,
            Map<String, String> globalVariableValues, PortalControllerContext portalControllerContext, String procedureInitiator, String taskInitiator,
            String nextStep) throws FormFilterException {
        // on retrouve les filtres installés
        CustomizationPluginMgr pluginManager = this.cmsCustomizer.getPluginMgr();
        Map<String, FormFilter> portalFilters = pluginManager.getFormFilters();

        // on retrouve les filtres de l'actions voulu
        PropertyList actionFilters = actionProperties.getList("filtersList");
        Map<String, List<FormFilterInstance>> filtersByParentPathMap = new HashMap<String, List<FormFilterInstance>>();
        Map<String, Map<String, String>> filtersParams = new HashMap<String, Map<String, String>>();
        for (Object filterObject : actionFilters.list()) {
            PropertyMap filterMap = (PropertyMap) filterObject;
            FormFilter filter = portalFilters.get(filterMap.getString("filterId"));
            if (filter != null) {
                FormFilterInstance filterInstance = new FormFilterInstance(filter, filterMap.getString("filterPath"), filterMap.getString("filterName"),
                        filterMap.getString("filterInstanceId"));
                // on garde le path du parent du filtre pour l'ajouter à la map
                String parentPath = StringUtils.contains(filterInstance.getPath(), ',') ? StringUtils.substringBeforeLast(filterInstance.getPath(), ",")
                        : StringUtils.EMPTY;
                List<FormFilterInstance> parentFiltersList = filtersByParentPathMap.get(parentPath);
                if (parentFiltersList == null) {
                    parentFiltersList = new ArrayList<FormFilterInstance>();
                }
                parentFiltersList.add(filterInstance);
                filtersByParentPathMap.put(parentPath, parentFiltersList);

                // on ajoute les arguments du filtre à la liste des variables
                PropertyList argumentsList = filterMap.getList("argumentsList");
                if (argumentsList != null) {
                    Map<String, String> filterParams = new HashMap<String, String>(argumentsList.size());
                    for (int i = 0; i < argumentsList.size(); i++) {
                        PropertyMap argumentMap = argumentsList.getMap(i);
                        if (StringUtils.isNotBlank(argumentMap.getString("argumentName"))) {
                            filterParams.put(argumentMap.getString("argumentName"), argumentMap.getString("argumentValue"));
                        }
                    }
                    filtersParams.put(filterInstance.getId(), filterParams);
                }
            }
        }

        // init du contexte des filtres
        FormFilterContext filterContext = new FormFilterContext(filtersParams, procedureInitiator, taskInitiator, nextStep);
        filterContext.setPortalControllerContext(portalControllerContext);
        filterContext.setActors(actors);
        filterContext.setActionId(actionId);
        if (globalVariableValues != null) {
            // Copy submitted variables into Global Variables Values
            globalVariableValues.putAll(variables);
            filterContext.setVariables(globalVariableValues);
        } else {
            filterContext.setVariables(variables);
        }

        // on construit l'executor parent
        FormFilterExecutor parentExecutor = new FormFilterExecutor(filtersByParentPathMap, StringUtils.EMPTY, StringUtils.EMPTY);
        // on execute les filtres de premier niveau
        parentExecutor.executeChildren(filterContext);
        return filterContext;
    }


    /**
     * Get model document.
     *
     * @param portalControllerContext portal controller context
     * @param webId model webId
     * @return document
     * @throws PortalException
     */
    private Document getModel(PortalControllerContext portalControllerContext, String webId) throws PortalException {
        // CMS service
        ICMSService cmsService = this.cmsServiceLocator.getCMSService();
        // CMS context
        CMSServiceCtx cmsContext = new CMSServiceCtx();
        cmsContext.setPortalControllerContext(portalControllerContext);
        cmsContext.setScope("superuser_context");

        // Path
        String path = NuxeoController.webIdToFetchPath(webId);

        // CMS item
        CMSItem cmsItem;
        try {
            cmsItem = cmsService.getContent(cmsContext, path);
        } catch (CMSException e) {
            throw new PortalException(e);
        }

        return (Document) cmsItem.getNativeItem();
    }


    /**
     * Get step properties.
     *
     * @param model model document
     * @param step step name
     * @return properties
     */
    private PropertyMap getStepProperties(Document model, String step) {
        // Steps
        PropertyList steps = model.getProperties().getList("pcd:steps");

        // Properties
        PropertyMap properties = null;
        for (int i = 0; i < steps.size(); i++) {
            PropertyMap map = steps.getMap(i);
            if (StringUtils.equals(step, map.getString("reference"))) {
                properties = map;
                break;
            }
        }

        return properties;
    }


    /**
     * Get action properties.
     *
     * @param stepProperties step properties
     * @param actionId action identifier
     * @return properties
     */
    private PropertyMap getActionProperties(PropertyMap stepProperties, String actionId) {
        if (actionId == null) {
            // Default action identifier
            actionId = String.valueOf(stepProperties.get("actionIdDefault"));
        }

        // Actions
        PropertyList actions = stepProperties.getList("actions");

        // Properties
        PropertyMap properties = null;
        for (int i = 0; i < actions.size(); i++) {
            PropertyMap map = actions.getMap(i);
            if (StringUtils.equals(actionId, map.getString("actionId"))) {
                properties = map;
                break;
            }
        }

        return properties;
    }


    /**
     * Generate variables JSON content.
     *
     * @param variables variables
     * @return JSON
     */
    private String generateVariablesJSON(Map<String, String> variables) {
        JSONArray array = new JSONArray();
        for (Entry<String, String> entry : variables.entrySet()) {
            JSONObject object = new JSONObject();
            object.put("name", entry.getKey());
            object.put("value", entry.getValue());
            array.add(object);
        }
        return array.toString();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String transform(PortalControllerContext portalControllerContext, String expression, Map<String, String> variables) throws PortalException {
        // UUID
        UUID uuid = null;
        if (variables != null) {
            String value = variables.get("uuid");
            if (StringUtils.isNotBlank(value)) {
                uuid = UUID.fromString(value);
            }
        }

        // Thread local container
        ThreadLocalContainer container = new ThreadLocalContainer(portalControllerContext, uuid);


        // Expression factory
        ExpressionFactory factory = new ExpressionFactoryImpl();

        // Simple context
        SimpleContext context = new SimpleContext();

        // Variables
        if (MapUtils.isNotEmpty(variables)) {
            for (Entry<String, String> entry : variables.entrySet()) {
                context.setVariable(entry.getKey(), factory.createValueExpression(entry.getValue(), String.class));
            }
        }

        // Functions
        try {
            context.setFunction("user", "name", TransformationFunctions.getUserDisplayNameMethod());
            //context.setFunction("user", "link", TransformationFunctions.getUserLinkMethod());
            context.setFunction("user", "email", TransformationFunctions.getUserEmailMethod());
            context.setFunction("document", "title", TransformationFunctions.getDocumentTitleMethod());
            context.setFunction("document", "link", TransformationFunctions.getDocumentLinkMethod());
            //context.setFunction("command", "link", TransformationFunctions.getCommandLinkMethod());
            context.setFunction("document", "linkWithText", TransformationFunctions.getDocumentLinkWithTextMethod());
        } catch (NoSuchMethodException e) {
            throw new PortalException(e);
        } catch (SecurityException e) {
            throw new PortalException(e);
        }

        // Value expression
        ValueExpression value = factory.createValueExpression(context, StringUtils.trimToEmpty(expression), String.class);

        // Transformed expression
        String transformedExpression;
        try {
            threadLocal.set(container);

            transformedExpression = String.valueOf(value.getValue(context));
        } finally {
            threadLocal.remove();
        }

        return transformedExpression;
    }


    /**
     * Get portal controller context.
     *
     * @return portal controller context
     */
    public static PortalControllerContext getPortalControllerContext() {
        // Thread local container
        ThreadLocalContainer container = threadLocal.get();

        // Portal controller context
        PortalControllerContext portalControllerContext;
        if (container == null) {
            portalControllerContext = null;
        } else {
            portalControllerContext = container.portalControllerContext;
        }

        return portalControllerContext;
    }


    /**
     * Get UUID.
     *
     * @return UUID
     */
    public static UUID getUuid() {
        // Thread local container
        ThreadLocalContainer container = threadLocal.get();

        // UUID
        UUID uuid;
        if (container == null) {
            uuid = null;
        } else {
            uuid = container.uuid;
        }

        return uuid;
    }


    /**
     * Thread local container.
     *
     * @author Cédric Krommenhoek
     */
    private class ThreadLocalContainer {

        /** Portal controller context. */
        private final PortalControllerContext portalControllerContext;
        /** UUID. */
        private final UUID uuid;


        /**
         * Constructor.
         *
         * @param portalControllerContext portal controller context
         * @param uuid UUID
         */
        public ThreadLocalContainer(PortalControllerContext portalControllerContext, UUID uuid) {
            super();
            this.portalControllerContext = portalControllerContext;
            this.uuid = uuid;
        }

    }

}
