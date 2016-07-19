package fr.toutatice.portail.cms.nuxeo.portlets.forms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.cache.services.CacheInfo;
import org.osivia.portal.api.context.PortalControllerContext;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.cms.NuxeoDocumentContext;
import fr.toutatice.portail.cms.nuxeo.api.forms.FormActors;
import fr.toutatice.portail.cms.nuxeo.api.forms.FormFilter;
import fr.toutatice.portail.cms.nuxeo.api.forms.FormFilterContext;
import fr.toutatice.portail.cms.nuxeo.api.forms.FormFilterExecutor;
import fr.toutatice.portail.cms.nuxeo.api.forms.FormFilterInstance;
import fr.toutatice.portail.cms.nuxeo.api.forms.IFormsService;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.CustomizationPluginMgr;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.DefaultCMSCustomizer;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Forms service implementation.
 *
 * @author Cédric Krommenhoek
 * @see IFormsService
 */
public class FormsServiceImpl implements IFormsService {

    /** End step name. */
    private static final String ENDSTEP = "endStep";


    /** CMS customizer. */
    private DefaultCMSCustomizer customizer;


    /** Class loader. */
    private final ClassLoader classLoader;


    /**
     * Constructor.
     */
    public FormsServiceImpl(DefaultCMSCustomizer customizer) {
        super();
        this.customizer = customizer;
        this.classLoader = Thread.currentThread().getContextClassLoader();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void start(PortalControllerContext portalControllerContext, String modelId, Map<String, String> variables) throws PortalException {
        this.start(portalControllerContext, modelId, null, variables);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void start(PortalControllerContext portalControllerContext, String modelId, String actionId, Map<String, String> variables) throws PortalException {
        // L'instanciation du parser Neko nécessite de passer dans le classloader du CMSCustomizer
        // (Sinon, on n'arrive pas à trouver la classe du parser)
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.classLoader);

        try {
            // Nuxeo controller
            NuxeoController nuxeoController = new NuxeoController(portalControllerContext);
            nuxeoController.setCacheType(CacheInfo.CACHE_SCOPE_NONE);

            // Model
            Document model = this.getModel(portalControllerContext, modelId);

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
            FormFilterContext filterContext = callFilters(actionId, variables, actionProperties, actors, null);

            // Properties
            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put("pi:currentStep", actionProperties.getString("stepReference"));
            properties.put("pi:procedureModelPath", model.getPath());
            properties.put("pi:globalVariablesValues", this.generateVariablesJSON(variables));

            // Nuxeo command
            INuxeoCommand command = new StartProcedureCommand(title, filterContext.getActors().getGroups(), filterContext.getActors().getUsers(), properties);
            nuxeoController.executeNuxeoCommand(command);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
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
            Map<String, String> globalVariableValues) {
        // on retrouve les filtres installés
        CustomizationPluginMgr pluginManager = this.customizer.getPluginMgr();
        Map<String, FormFilter> portalFilters = pluginManager.getFormFilters();

        // on retrouve les filtres de l'actions voulu
        PropertyList actionFilters = actionProperties.getList("filtersList");
        Map<String, List<FormFilterInstance>> filtersByParentPathMap = new HashMap<String, List<FormFilterInstance>>();
        Map<String, Map<String, String>> filtersParams = new HashMap<String, Map<String, String>>();
        for (Object filterObject : actionFilters.list()) {
            PropertyMap filterMap = (PropertyMap) filterObject;
            FormFilter filter = portalFilters.get(filterMap.getString("filterId"));
            if (filter != null) {
                FormFilterInstance filterInstance = new FormFilterInstance(filter, filterMap.getString("filterPath"), filterMap.getString("filterName"));
                // on garde le path du parent du filtre pour l'ajouter à la map
                String parentPath = StringUtils.split(",").length > 1 ? StringUtils.substringBeforeLast(filterInstance.getPath(), ",") : StringUtils.EMPTY;
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
                    filtersParams.put(filterInstance.getName(), filterParams);
                }
            }
        }

        // init du contexte des filtres
        FormFilterContext filterContext = new FormFilterContext(filtersParams);
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
        FormFilterExecutor parentExecutor = new FormFilterExecutor(filtersByParentPathMap, StringUtils.EMPTY);
        // on execute les filtres de premier niveau
        parentExecutor.executeChildren(filterContext);
        return filterContext;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void proceed(PortalControllerContext portalControllerContext, Document task, String actionId, Map<String, String> variables) throws PortalException {
        if (variables == null) {
            variables = new HashMap<String, String>();
        }

        // L'instanciation du parser Neko nécessite de passer dans le classloader du CMSCustomizer
        // (Sinon, on n'arrive pas à trouver la classe du parser)
        Thread.currentThread().setContextClassLoader(this.classLoader);
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();

        try {
            // Nuxeo controller
            NuxeoController nuxeoController = new NuxeoController(portalControllerContext);
            nuxeoController.setCacheType(CacheInfo.CACHE_SCOPE_NONE);

            // Instance document
            Document instance = this.getInstance(portalControllerContext, task);
            // Model document
            Document model = this.getModel(portalControllerContext, instance);

            // Previous step
            String previousStep = instance.getString("pi:currentStep");
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
                // add authorizedGroups to actors
                // add authorizedGroups to actors
                final PropertyList groupsObjectsList = nextStepProperties.getList("authorizedGroups");
                if (groupsObjectsList != null) {
                    for (final Object groupsObject : groupsObjectsList.list()) {
                        actors.getGroups().add((String) groupsObject);
                    }
                }
            }

            // Global Variables Values
            PropertyMap taskProperties = task.getProperties();
            PropertyMap procedureInstance = taskProperties.getMap("nt:pi");
            Map<String, Object> globalVariableValuesMap = procedureInstance.getMap("pi:globalVariablesValues").map();
            Map<String, String> globalVariableValues = new HashMap<String, String>(globalVariableValuesMap.size());
            for (Entry<String, Object> gvvEntry : globalVariableValuesMap.entrySet()) {
                globalVariableValues.put(gvvEntry.getKey(), String.valueOf(gvvEntry.getValue()));
            }

            // construction du contexte et appel des filtres
            FormFilterContext filterContext = callFilters(actionId, variables, actionProperties, actors, globalVariableValues);

            // Properties
            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put("pi:currentStep", actionProperties.getString("stepReference"));
            properties.put("pi:procedureModelPath", model.getPath());
            properties.put("pi:globalVariablesValues", this.generateVariablesJSON(globalVariableValues));

            // Nuxeo command
            INuxeoCommand command = new UpdateProcedureCommand(instance, title, filterContext.getActors().getGroups(), filterContext.getActors().getUsers(),
                    properties);
            nuxeoController.executeNuxeoCommand(command);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }


    /**
     * Get model document.
     *
     * @param portalControllerContext portal controller context
     * @param modelId model identifier
     * @return document
     */
    private Document getModel(PortalControllerContext portalControllerContext, String modelId) {
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(portalControllerContext);

        // Fetch path
        String fetchPath = NuxeoController.webIdToFetchPath(FORMS_WEB_ID_PREFIX + modelId);

        // Nuxeo document context
        NuxeoDocumentContext documentContext = nuxeoController.getDocumentContext(fetchPath);

        return documentContext.getDoc();
    }


    /**
     * Get model document.
     *
     * @param portalControllerContext portal controller context
     * @param instance instance document
     * @return document
     */
    private Document getModel(PortalControllerContext portalControllerContext, Document instance) {
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(portalControllerContext);

        // Path
        String path = instance.getString("pi:procedureModelPath");

        // Document context
        NuxeoDocumentContext documentContext = nuxeoController.getDocumentContext(path);

        return documentContext.getDoc();
    }


    /**
     * Get instance document.
     *
     * @param portalControllerContext portal controller context
     * @param task task document
     * @return document
     */
    private Document getInstance(PortalControllerContext portalControllerContext, Document task) {
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(portalControllerContext);

        // Instance properties
        PropertyMap properties = task.getProperties().getMap("nt:pi");

        // Path
        String path = properties.getString("ecm:path");

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

}
