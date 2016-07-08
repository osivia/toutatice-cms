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
import fr.toutatice.portail.cms.nuxeo.api.forms.IFormsService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Forms service implementation.
 * 
 * @author Cédric Krommenhoek
 * @see IFormsService
 */
public class FormsServiceImpl implements IFormsService {

    /**
     * Constructor.
     */
    public FormsServiceImpl() {
        super();
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
        // Next step properties
        PropertyMap nextStepProperties = this.getStepProperties(model, nextStep);
        
        
        // Task title
        String title = nextStepProperties.getString("name");
        
        // User & group names
        // TODO
        List<String> groups = new ArrayList<String>();
        List<String> users = new ArrayList<String>();

        // Properties
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("pi:currentStep", actionProperties.getString("stepReference"));
        properties.put("pi:procedureModelPath", model.getPath());
        properties.put("pi:globalVariablesValues", this.generateVariablesJSON(variables));

        // Nuxeo command
        INuxeoCommand command = new StartProcedureCommand(title, groups, users, properties);
        nuxeoController.executeNuxeoCommand(command);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void proceed(PortalControllerContext portalControllerContext, Document task, String actionId, Map<String, String> variables) throws PortalException {
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
        // Next step properties
        PropertyMap nextStepProperties = this.getStepProperties(model, nextStep);


        // Task title
        String title = nextStepProperties.getString("name");

        // User & group names
        // TODO
        List<String> groups = new ArrayList<String>();
        List<String> users = new ArrayList<String>();

        // Properties
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("pi:currentStep", actionProperties.getString("stepReference"));
        properties.put("pi:procedureModelPath", model.getPath());
        properties.put("pi:globalVariablesValues", this.generateVariablesJSON(variables));

        // Nuxeo command
        INuxeoCommand command = new UpdateProcedureCommand(instance, title, groups, users, properties);
        nuxeoController.executeNuxeoCommand(command);
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
