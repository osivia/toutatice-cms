package fr.toutatice.portail.cms.nuxeo.portlets.forms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.cache.services.CacheInfo;
import org.osivia.portal.api.context.PortalControllerContext;

import de.odysseus.el.ExpressionFactoryImpl;
import de.odysseus.el.util.SimpleContext;
import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.cms.NuxeoDocumentContext;
import fr.toutatice.portail.cms.nuxeo.api.forms.FormActors;
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

    /** End step name. */
    private static final String ENDSTEP = "endStep";


    /** Portal controller context thread local. */
    private static ThreadLocal<PortalControllerContext> portalControllerContextThreadLocal = new ThreadLocal<PortalControllerContext>();


    /** CMS customizer. */
    private DefaultCMSCustomizer cmsCustomizer;


    /**
     * Constructor.
     * 
     * @param cmsCustomizer CMS customizer
     */
    public FormsServiceImpl(DefaultCMSCustomizer cmsCustomizer) {
        super();
        this.cmsCustomizer = cmsCustomizer;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void start(PortalControllerContext portalControllerContext, String modelId, Map<String, String> variables) throws PortalException,
            FormFilterException {
        this.start(portalControllerContext, modelId, null, variables);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void start(PortalControllerContext portalControllerContext, String modelId, String actionId, Map<String, String> variables) throws PortalException,
            FormFilterException {
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(portalControllerContext);
        nuxeoController.setCacheType(CacheInfo.CACHE_SCOPE_NONE);

        // Model
        String modelPath = NuxeoController.webIdToFetchPath(FORMS_WEB_ID_PREFIX + modelId);
        Document model = this.getModel(portalControllerContext, modelPath);
        
        String initiator = portalControllerContext.getHttpServletRequest().getUserPrincipal().getName();

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
        // Actors
        FormActors actors = new FormActors();
        if (!StringUtils.equals(ENDSTEP, nextStep)) {
            // Next step properties
            PropertyMap nextStepProperties = this.getStepProperties(model, nextStep);
            title = nextStepProperties.getString("name");
            // add authorizedGroups to actors
            final PropertyList groupsObjectsList = nextStepProperties.getList("authorizedGroups");
            if (groupsObjectsList != null) {
                for (final Object groupsObject : groupsObjectsList.list()) {
                    actors.getGroups().add((String) groupsObject);
                }
            }
        }

        // construction du contexte et appel des filtres
        FormFilterContext filterContext = callFilters(actionId, variables, actionProperties, actors, null, portalControllerContext, initiator, nextStep);

        // Properties
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("pi:currentStep", actionProperties.getString("stepReference"));
        properties.put("pi:procedureModelPath", model.getPath());
        properties.put("pi:globalVariablesValues", this.generateVariablesJSON(variables));

        // Nuxeo command
        INuxeoCommand command = new StartProcedureCommand(title, filterContext.getActors().getGroups(), filterContext.getActors().getUsers(), properties);
        nuxeoController.executeNuxeoCommand(command);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void proceed(PortalControllerContext portalControllerContext, Document task, Map<String, String> variables) throws PortalException,
            FormFilterException {
        this.proceed(portalControllerContext, task, null, variables);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void proceed(PortalControllerContext portalControllerContext, Document task, String actionId, Map<String, String> variables) throws PortalException,
            FormFilterException {
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(portalControllerContext);
        nuxeoController.setCacheType(CacheInfo.CACHE_SCOPE_NONE);

        // Task properties
        PropertyMap taskProperties = task.getProperties();
        // Procedure instance properties
        PropertyMap instanceProperties = taskProperties.getMap("nt:pi");

        // Model document
        String modelPath = instanceProperties.getString("pi:procedureModelPath");
        Document model = this.getModel(portalControllerContext, modelPath);

        // Initiator
        String initiator = instanceProperties.getString("pi:procedureInitiator");

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
        // Actors
        FormActors actors = new FormActors();
        if (!StringUtils.equals(ENDSTEP, nextStep)) {
            // Next step properties
            PropertyMap nextStepProperties = this.getStepProperties(model, nextStep);
            title = nextStepProperties.getString("name");
            // Add authorizedGroups to actors
            final PropertyList groupsObjectsList = nextStepProperties.getList("authorizedGroups");
            if (groupsObjectsList != null) {
                for (final Object groupsObject : groupsObjectsList.list()) {
                    actors.getGroups().add((String) groupsObject);
                }
            }
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

        // construction du contexte et appel des filtres
        FormFilterContext filterContext = callFilters(actionId, variables, actionProperties, actors, globalVariableValues, portalControllerContext, initiator,
                nextStep);

        // Properties
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("pi:currentStep", actionProperties.getString("stepReference"));
        properties.put("pi:procedureModelPath", model.getPath());
        properties.put("pi:globalVariablesValues", this.generateVariablesJSON(globalVariableValues));

        // Nuxeo command
        INuxeoCommand command = new UpdateProcedureCommand(instanceProperties.getString("ecm:path"), title, filterContext.getActors().getGroups(),
                filterContext.getActors().getUsers(), properties);
        nuxeoController.executeNuxeoCommand(command);
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
    private FormFilterContext callFilters(String actionId, Map<String, String> variables, PropertyMap actionProperties, FormActors actors,
            Map<String, String> globalVariableValues, PortalControllerContext portalControllerContext, String initiator, String nextStep)
            throws FormFilterException {
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
                String parentPath = StringUtils.substringAfter(filterInstance.getPath(), ",");
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
        FormFilterContext filterContext = new FormFilterContext(filtersParams, initiator, nextStep);
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
     * @param path model path
     * @return document
     */
    private Document getModel(PortalControllerContext portalControllerContext, String path) {
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(portalControllerContext);

        // Document context
        NuxeoDocumentContext documentContext = nuxeoController.getDocumentContext(path);

        return documentContext.getDoc();
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
            context.setFunction("user", "name", TransformationFunctions.getUserNameMethod());
            context.setFunction("user", "link", TransformationFunctions.getUserLinkMethod());
            context.setFunction("document", "link", TransformationFunctions.getDocumentLinkMethod());
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
            portalControllerContextThreadLocal.set(portalControllerContext);
            transformedExpression = String.valueOf(value.getValue(context));
        } finally {
            portalControllerContextThreadLocal.remove();
        }

        return transformedExpression;
    }


    /**
     * Get portal controller context.
     * 
     * @return portal controller context
     */
    public static PortalControllerContext getPortalControllerContext() {
        return portalControllerContextThreadLocal.get();
    }

}
