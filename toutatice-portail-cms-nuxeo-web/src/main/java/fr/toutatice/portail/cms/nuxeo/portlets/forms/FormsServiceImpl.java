package fr.toutatice.portail.cms.nuxeo.portlets.forms;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.naming.Name;
import javax.portlet.PortletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.dom4j.Element;
import org.nuxeo.ecm.automation.client.model.DocRef;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.PortalApplicationException;
import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.cache.services.CacheInfo;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.directory.v2.DirServiceFactory;
import org.osivia.portal.api.directory.v2.model.Group;
import org.osivia.portal.api.directory.v2.model.Person;
import org.osivia.portal.api.directory.v2.service.GroupService;
import org.osivia.portal.api.directory.v2.service.PersonService;
import org.osivia.portal.api.html.DOM4JUtils;
import org.osivia.portal.api.internationalization.Bundle;
import org.osivia.portal.api.internationalization.IBundleFactory;
import org.osivia.portal.api.internationalization.IInternationalizationService;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.portlet.model.UploadedFile;
import org.osivia.portal.api.tasks.ITasksService;
import org.osivia.portal.api.transaction.IPostcommitResource;
import org.osivia.portal.api.transaction.ITransactionService;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.ICMSService;
import org.osivia.portal.core.cms.ICMSServiceLocator;

import com.sun.mail.smtp.SMTPTransport;

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
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandContext;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.CustomizationPluginMgr;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.DefaultCMSCustomizer;
import fr.toutatice.portail.cms.nuxeo.portlets.service.GetTasksCommand;
import net.sf.json.JSONObject;

/**
 * Forms service implementation.
 *
 * @author Cédric Krommenhoek
 * @see IFormsService
 */
public class FormsServiceImpl implements IFormsService {

	
	private final static Log procLogger = LogFactory.getLog("procedures");
	
    /** Thread local. */
    private static ThreadLocal<ThreadLocalContainer> threadLocal = new ThreadLocal<ThreadLocalContainer>();

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    /** CMS customizer. */
    private final DefaultCMSCustomizer cmsCustomizer;
    /** Log. */
    private final Log log;

    /** JSON object mapper. */
    private final ObjectMapper mapper;

    /** CMS service locator. */
    private final ICMSServiceLocator cmsServiceLocator;
    /** Tasks service. */
    private final ITasksService tasksService;
    /** Internationalization bundle factory. */
    private final IBundleFactory bundleFactory;
    /** Person service. */
    private final PersonService personService;
    /** Group service. */
    private final GroupService groupService;
    /** Transaction service. */
    private final ITransactionService transactionService;
    /**
     * Constructor.
     *
     * @param cmsCustomizer CMS customizer
     */
    public FormsServiceImpl(DefaultCMSCustomizer cmsCustomizer) {
        super();
        this.cmsCustomizer = cmsCustomizer;
        this.log = LogFactory.getLog(this.getClass());

        // JSON object mapper
        this.mapper = new ObjectMapper();
        this.mapper.getSerializationConfig().setSerializationInclusion(Inclusion.NON_DEFAULT);

        // CMS service locator
        this.cmsServiceLocator = Locator.findMBean(ICMSServiceLocator.class, ICMSServiceLocator.MBEAN_NAME);
        // Tasks service
        this.tasksService = Locator.findMBean(ITasksService.class, ITasksService.MBEAN_NAME);
        // Internationalization bundle factory
        IInternationalizationService internationalizationService = Locator.findMBean(IInternationalizationService.class,
                IInternationalizationService.MBEAN_NAME);
        this.bundleFactory = internationalizationService.getBundleFactory(this.getClass().getClassLoader());
        // Person service
        this.personService = DirServiceFactory.getService(PersonService.class);
        // Group service
        this.groupService = DirServiceFactory.getService(GroupService.class);
        this.transactionService = Locator.findMBean(ITransactionService.class, ITransactionService.MBEAN_NAME);
    }


    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Map<String, String> start(PortalControllerContext portalControllerContext, String modelWebId, Map<String, String> variables)
            throws PortalException, FormFilterException {
        return this.start(portalControllerContext, modelWebId, null, variables);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> start(PortalControllerContext portalControllerContext, String modelWebId, String actionId, Map<String, String> variables)
            throws PortalException, FormFilterException {
        return this.start(portalControllerContext, modelWebId, actionId, variables, null);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> start(PortalControllerContext portalControllerContext, String modelWebId, String actionId, Map<String, String> variables,
            Map<String, UploadedFile> uploadedFiles) throws PortalException, FormFilterException {
        // CMS context
        CMSServiceCtx cmsContext = new CMSServiceCtx();
        cmsContext.setPortalControllerContext(portalControllerContext);
        
        
        Locale locale = null;
        
        if(portalControllerContext.getHttpServletRequest() != null && portalControllerContext.getHttpServletRequest().getLocale() != null) {
            locale = portalControllerContext.getHttpServletRequest().getLocale();
        }
        Bundle bundle = this.bundleFactory.getBundle(locale);
        


        if (variables == null) {
            variables = new HashMap<String, String>();
        }

        // Model
        Document model = this.getModel(portalControllerContext, modelWebId);

        // Starting step
        String startingStep = StringUtils.defaultIfBlank(variables.get("pcd:startingStep"), model.getString("pcd:startingStep"));

        if (StringUtils.isBlank(startingStep)) {
            String errorMessage = bundle.getString("FORMS_NO_STARTING_STEP");
            throw new PortalException(errorMessage);
        }

        // Starting step properties
        PropertyMap formStepProperties;
        PropertyMap actionStepProperties;

        // recordFolders have parentModel for the action properties but regular model holds the form
        if (StringUtils.equals(model.getType(), "RecordFolder")) {
            formStepProperties = this.getStepProperties(model, IFormsService.FORM_STEP_REFERENCE);
            String parentModelWebId = model.getString("pcd:webIdParent");
            model = this.getModel(portalControllerContext, parentModelWebId);
            actionStepProperties = this.getStepProperties(model, startingStep);
        } else {
            formStepProperties = this.getStepProperties(model, startingStep);
            actionStepProperties = formStepProperties;
        }

        if (actionStepProperties == null) {
            String errorMessage = bundle.getString("FORMS_BAD_STARTING_STEP", startingStep);
            throw new PortalException(errorMessage);
        }

        // Action properties
        PropertyMap actionProperties = this.getActionProperties(actionStepProperties, actionId, startingStep, bundle);

        // Uploaded files
        if (uploadedFiles == null) {
            uploadedFiles = new HashMap<>(0);
        }
        variables.putAll(this.getUploadedFilesVariables(portalControllerContext, uploadedFiles));

        // Procedure initiator
        String procedureInitiator = "";
        HttpServletRequest httpServletRequest = portalControllerContext.getHttpServletRequest();
        if(httpServletRequest != null) {
            procedureInitiator = httpServletRequest.getRemoteUser();
            // Required fields validation
            this.requiredFieldsValidation(portalControllerContext, formStepProperties, variables, uploadedFiles);
        }
        else {
            // #1569 - Specific parameters for procedures in batch mode
            procedureInitiator = "admin";
            cmsContext.setScope("superuser_no_cache");
        }

        // Next step
        String nextStep = actionProperties.getString("stepReference");

        if (StringUtils.isBlank(nextStep)) {
            String errorMessage = bundle.getString("FORMS_NO_NEXT_STEP", actionId, nextStep);
            throw new PortalException(errorMessage);
        }

        // Task title
        String title;
        // Actors
        List<String> actors;
        if (StringUtils.equals(ENDSTEP, nextStep)) {
            title = StringUtils.EMPTY;
            actors = new ArrayList<>(0);
        } else {
            // Next step properties
            PropertyMap nextStepProperties = this.getStepProperties(model, nextStep);

            if (nextStepProperties == null) {
                String errorMessage = bundle.getString("FORMS_BAD_NEXT_STEP", nextStep, actionId, startingStep);
                throw new PortalException(errorMessage);
            }

            title = nextStepProperties.getString("name");
            actors = getActors(model, nextStep, title, nextStepProperties);
        }

        // UUID
        String uuid = UUID.randomUUID().toString();
        variables.put("uuid", uuid);

        // Start date
        String startDate = DATE_FORMAT.format(new Date());
        
    	procLogger.info("Start procedure "+uuid+ " ("+modelWebId+") by "+procedureInitiator);
    	procLogger.info("  variables "+Collections.singletonList(variables));

        // Construction du contexte et appel des filtres
        FormFilterContext filterContext = this.callFilters(modelWebId, uuid, actionId, variables, actionProperties, actors, null, uploadedFiles,
                portalControllerContext, procedureInitiator, startDate, startDate, procedureInitiator, nextStep, startingStep, bundle);


        // End step indicator
        boolean endStep = ENDSTEP.equals(filterContext.getNextStep());

        if (!endStep) {
            // Properties
            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put("pi:currentStep", actionProperties.getString("stepReference"));
            properties.put("pi:procedureModelWebId", modelWebId);
            properties.put("pi:globalVariablesValues", this.convertVariablesToJson(portalControllerContext, variables));
            
            // Nuxeo command
            INuxeoCommand command = new StartProcedureCommand(title, filterContext.getActors(), filterContext.getAdditionalAuthorizations(), properties);
            
            
            // Save current scope
            String savedScope = cmsContext.getScope();

            
            try {
             	DocRef docRef = (DocRef) this.cmsCustomizer.executeNuxeoCommand(cmsContext, command);
            	cmsContext.setScope("superuser_no_cache");
                INuxeoCommand blobCmd = new BlobsProcedureCommand(uploadedFiles, docRef);
                this.cmsCustomizer.executeNuxeoCommand(cmsContext, blobCmd);
            } catch (CMSException e) {
                throw new PortalException(e);
            }
            finally {
                cmsContext.setScope(savedScope);
            }
            
            IPostcommitResource sendModule = new ProcedureSendMailModule(portalControllerContext, this.cmsCustomizer.getPortletContext(), this, uuid, procedureInitiator);
            transactionService.registerPostcommit(sendModule);

        }
    	procLogger.info(" Procedure started "+uuid);

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
        return this.proceed(portalControllerContext, taskProperties, actionId, variables, null);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> proceed(PortalControllerContext portalControllerContext, PropertyMap taskProperties, String actionId,
            Map<String, String> variables, Map<String, UploadedFile> uploadedFiles) throws PortalException, FormFilterException {
        // CMS service
        ICMSService cmsService = cmsServiceLocator.getCMSService();
        // CMS context
        CMSServiceCtx cmsContext = new CMSServiceCtx();
        cmsContext.setPortalControllerContext(portalControllerContext);

        Locale locale = null;
        
        if(portalControllerContext.getHttpServletRequest() != null && portalControllerContext.getHttpServletRequest().getLocale() != null) {
        	locale = portalControllerContext.getHttpServletRequest().getLocale();
        }
        Bundle bundle = this.bundleFactory.getBundle(locale);

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

        // procedure startDate
        Date date = instanceProperties.getDate("dc:created");
        String startDate = DATE_FORMAT.format(date);

        // procedure lastModified
        date = instanceProperties.getDate("dc:modified");
        String lastModified = DATE_FORMAT.format(date);

        // Previous step
        String currentStep = instanceProperties.getString("pi:currentStep");
        // Previous step properties
        PropertyMap previousStepProperties = this.getStepProperties(model, currentStep);

        // Action properties
        PropertyMap actionProperties = this.getActionProperties(previousStepProperties, actionId, currentStep, bundle);

        // Next step
        String nextStep = actionProperties.getString("stepReference");

        if (StringUtils.isBlank(nextStep)) {
            String errorMessage = bundle.getString("FORMS_NO_NEXT_STEP", actionId, nextStep);
            throw new PortalException(errorMessage);
        }

        // Task title
        String title;
        // Actors
        List<String> actors;
        if (StringUtils.equals(ENDSTEP, nextStep)) {
            title = StringUtils.EMPTY;
            actors = new ArrayList<>(0);
        } else {
            // Next step properties
            PropertyMap nextStepProperties = this.getStepProperties(model, nextStep);

            if (nextStepProperties == null) {
                String errorMessage = bundle.getString("FORMS_BAD_NEXT_STEP", nextStep, actionId, currentStep);
                throw new PortalException(errorMessage);
            }

            title = nextStepProperties.getString("name");
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

        // Uploaded files
        if (uploadedFiles == null) {
            uploadedFiles = new HashMap<>(0);
        }
        variables.putAll(this.getUploadedFilesVariables(portalControllerContext, uploadedFiles));

        // Procedure instance UUID
        String procedureInstanceUuid = globalVariableValues.get("uuid");

        // Required fields validation
        this.requiredFieldsValidation(portalControllerContext, previousStepProperties, variables, uploadedFiles);


    	procLogger.info("Proceed "+procedureInstanceUuid+ " ("+modelWebId+") to step "+actionProperties.getString("stepReference")+", actors "+actors);
    	procLogger.info("  global variables "+Collections.singletonList(variables));
        
        
        // Construction du contexte et appel des filtres
        FormFilterContext filterContext = this.callFilters(modelWebId, procedureInstanceUuid, actionId, variables, actionProperties, actors,
                globalVariableValues, uploadedFiles, portalControllerContext, procedureInitiator, startDate, lastModified, previousTaskInitiator, nextStep,
                currentStep, bundle);

        // Properties
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("pi:currentStep", actionProperties.getString("stepReference"));
        properties.put("pi:procedureModelWebId", modelWebId);
        properties.put("pi:globalVariablesValues", this.convertVariablesToJson(portalControllerContext, globalVariableValues));

        
        // Nuxeo command
        INuxeoCommand command = new UpdateProcedureCommand(instancePath, title, filterContext.getActors(), filterContext.getAdditionalAuthorizations(),
                properties);
        
         
        // Save current scope
        String savedContextScope = cmsContext.getScope();
       
        
        
        
        try {
        	if(portalControllerContext.getHttpServletRequest() == null) {
            	cmsContext.setScope("superuser_no_cache");
        	}
        	
        	DocRef docRef = (DocRef) this.cmsCustomizer.executeNuxeoCommand(cmsContext, command);
        	
        	cmsContext.setScope("superuser_no_cache");
            INuxeoCommand blobCmd = new BlobsProcedureCommand(uploadedFiles, docRef);
            this.cmsCustomizer.executeNuxeoCommand(cmsContext, blobCmd);
        } catch (CMSException e) {
            throw new PortalException(e);
        }
        
        finally {
            cmsContext.setScope(savedContextScope);
        }

        
        

        // End step indicator
        boolean endStep = ENDSTEP.equals(filterContext.getNextStep());


        // Email notification
        if (!endStep) {
            String uuid = globalVariableValues.get("uuid");
            String initiator = "";
            if(portalControllerContext.getHttpServletRequest() != null) {
            	initiator = portalControllerContext.getHttpServletRequest().getRemoteUser();
            }
            else {
            	// TODO get default administrator
            	initiator = "admin";
            }
            
            
            ProcedureSendMailModule sendModule = new ProcedureSendMailModule(portalControllerContext, this.cmsCustomizer.getPortletContext(), this, uuid, initiator);
            if (transactionService.isStarted()) {
                transactionService.registerPostcommit(sendModule);
            }   else    {
                sendModule.run();
            }
        }


        // Updated variables
        Map<String, String> updatedVariables = filterContext.getVariables();

        // Check if workflow must be deleted
        boolean deleteOnEnding = BooleanUtils.toBoolean(updatedVariables.get(DELETE_ON_ENDING_PARAMETER));
        if (deleteOnEnding && endStep) {
            
            ProcedureRemoveInstanceModule removeInstanceModule = new ProcedureRemoveInstanceModule(cmsContext, cmsService, instancePath);
            if (transactionService.isStarted()) {
                transactionService.registerPostcommit(removeInstanceModule);
            }   else    {
                removeInstanceModule.run();
            }
          }
        
    	procLogger.info(" Procedure proceeded "+globalVariableValues.get("uuid"));


        return updatedVariables;
    }


    /**
     * Get file digest.
     * 
     * @param file file
     * @return digest
     * @throws IOException
     */
    private String getDigest(File file) throws IOException {
        String digest;

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            digest = DigestUtils.md5Hex(inputStream);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        return digest;
    }


    /**
     * Process required fields validation.
     * 
     * @param portalControllerContext portal controller context
     * @param step current step properties
     * @param variables current variables
     * @param uploadedFiles uploaded files
     * @throws FormFilterException
     */
    private void requiredFieldsValidation(PortalControllerContext portalControllerContext, PropertyMap step, Map<String, String> variables,
            Map<String, UploadedFile> uploadedFiles)
            throws FormFilterException {
        // Internationalization bundle
        Locale locale = null;
        
        if(portalControllerContext.getHttpServletRequest() != null && portalControllerContext.getHttpServletRequest().getLocale() != null) {
        	locale = portalControllerContext.getHttpServletRequest().getLocale();
        }
        
        Bundle bundle = this.bundleFactory.getBundle(locale);

        // Step fields
        PropertyList fields = step.getList("globalVariablesReferences");

        for (int i = 0; i < fields.size(); i++) {
            PropertyMap field = fields.getMap(i);

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
     * Appel des filtres.
     *
     * @param modelWebId model webId
     * @param procedureInstanceUuid procedure instance UUID
     * @param actionId
     * @param variables
     * @param actionProperties
     * @param actors
     * @param bundle
     * @return
     * @throws FormFilterException
     */
    private FormFilterContext callFilters(String modelWebId, String procedureInstanceUuid, String actionId, Map<String, String> variables,
            PropertyMap actionProperties, List<String> actors, Map<String, String> globalVariableValues, Map<String, UploadedFile> uploadedFiles,
            PortalControllerContext portalControllerContext, String procedureInitiator, String startDate, String lastModified, String taskInitiator,
            String nextStep, String currentStep, Bundle bundle) throws FormFilterException, PortalException {
        // on retrouve les filtres installés
        CustomizationPluginMgr pluginManager = this.cmsCustomizer.getPluginManager();
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
        FormFilterContext filterContext = new FormFilterContext(filtersParams, procedureInitiator, taskInitiator, startDate, lastModified, nextStep,
                currentStep);
        filterContext.setPortalControllerContext(portalControllerContext);
        filterContext.setModelWebId(modelWebId);
        filterContext.setProcedureInstanceUuid(procedureInstanceUuid);
        filterContext.setActionId(actionId);
        filterContext.setUploadedFiles(uploadedFiles);
        if (globalVariableValues != null) {
            // Copy submitted variables into Global Variables Values
            globalVariableValues.putAll(variables);
            filterContext.setVariables(globalVariableValues);
        } else {
            filterContext.setVariables(variables);
        }
        if (CollectionUtils.isNotEmpty(actors)) {
            filterContext.getActors().addAll(actors);
        }

        // on construit l'executor parent
        FormFilterExecutor parentExecutor = new FormFilterExecutor(filtersByParentPathMap, StringUtils.EMPTY, StringUtils.EMPTY, bundle);
        // on execute les filtres de premier niveau
        try {
            parentExecutor.executeChildren(filterContext);
        } catch (FormFilterException | PortalApplicationException e) {
            throw e;
        } catch (PortalException e) {
            if ((e.getCause() != null) && (e.getMessage() != null)) {
                String message = bundle.getString("FORMS_FILTER_ERROR_FILTER", e.getMessage());
                log.error(message, e);
                throw new PortalException(message, e);
            } else if (e.getMessage() != null) {
                log.error(e.getMessage(), e);
                throw e;
            } else {
                log.error(e);
                throw e;
            }
        } catch (Exception e) {
            String message = bundle.getString("FORMS_FILTER_ERROR");
            log.error(message, e);
            throw new PortalException(message, e);
        }
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
        if (!StringUtils.equals(step, ENDSTEP)) {
            for (int i = 0; i < steps.size(); i++) {
                PropertyMap map = steps.getMap(i);
                if (StringUtils.equals(step, map.getString("reference"))) {
                    properties = map;
                    break;
                }
            }
        }
        return properties;
    }


    /**
     * Get action properties.
     *
     * @param stepProperties step properties
     * @param actionId action identifier
     * @param bundle internationalization bundle
     * @param step step
     * @return properties
     * @throws PortalException
     */
    private PropertyMap getActionProperties(PropertyMap stepProperties, String actionId, String step, Bundle bundle) throws PortalException {
        if (actionId == null) {
            // Default action identifier
            actionId = String.valueOf(stepProperties.get("actionIdDefault"));
        }

        if (StringUtils.isBlank(actionId)) {
            String errorMessage = bundle.getString("FORMS_NO_ACTION");
            throw new PortalException(errorMessage);
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

        if (properties == null) {
            String errorMessage = bundle.getString("FORMS_BAD_ACTION", actionId, step);
            throw new PortalException(errorMessage);
        }

        return properties;
    }


    /**
     * Send email notification.
     * 
     * @param portalControllerContext portal controller context
     * @param procedureInstanceId procedure instance identifier
     * @param initiator task initiator
     * @throws CMSException
     * @throws PortalException
     */
    private void sendEmailNotification(PortalControllerContext portalControllerContext, String procedureInstanceId, String initiator)
            throws CMSException, PortalException {
        // Portlet context
        PortletContext portletContext = this.cmsCustomizer.getPortletContext();
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(portletContext);
        nuxeoController.setAuthType(NuxeoCommandContext.AUTH_TYPE_SUPERUSER);
        nuxeoController.setCacheType(CacheInfo.CACHE_SCOPE_NONE);

        // Internationalization bundle
        Locale locale = null;
        if(portalControllerContext.getHttpServletRequest() != null) {

        	locale = portalControllerContext.getHttpServletRequest().getLocale();
        }
        
        Bundle bundle = this.bundleFactory.getBundle(locale);

        if (StringUtils.isNotEmpty(procedureInstanceId)) {
            // UUID
            UUID uuid = UUID.fromString(procedureInstanceId);

            // Nuxeo command
            INuxeoCommand command = new GetTasksCommand(null, null, uuid);
            Documents documents = (Documents) nuxeoController.executeNuxeoCommand(command);

            // Task document
            Document task;
            if (documents.size() == 1) {
                task = documents.get(0);
            } else {
                throw new CMSException(CMSException.ERROR_NOTFOUND);
            }

            // Task variables
            PropertyMap variables = task.getProperties().getMap("nt:task_variables");

            if (BooleanUtils.isTrue(variables.getBoolean("notifEmail"))) {
                // Actors
                PropertyList actors = task.getProperties().getList("nt:actors");

                if (!actors.isEmpty()) {
                    // User names
                    Set<Name> names = new HashSet<>(actors.size());
                    for (int i = 0; i < actors.size(); i++) {
                        String actor = actors.getString(i);

                        // Group
                        Group group;
                        if (StringUtils.startsWith(actor, ACTOR_USER_PREFIX)) {
                            group = null;
                        } else if (StringUtils.startsWith(actor, ACTOR_GROUP_PREFIX)) {
                            group = this.groupService.get(StringUtils.removeStart(actor, ACTOR_GROUP_PREFIX));
                        } else {
                            group = this.groupService.get(actor);
                        }

                        if (group == null) {
                            String user = StringUtils.removeStart(actor, ACTOR_USER_PREFIX);
                            names.add(this.personService.getEmptyPerson().buildDn(user));
                        } else {
                            for (Name member : group.getMembers()) {
                                names.add(member);
                            }
                        }
                    }

                    // Email recipients
                    Set<String> emailRecipients = new HashSet<String>(names.size());
                    for (Name name : names) {
                        Person person = this.personService.getPerson(name);
                        if (person != null) {
                            String email = person.getMail();
                            if (StringUtils.isNotBlank(email)) {
                                emailRecipients.add(email);
                            }
                        }
                    }

                    if (!emailRecipients.isEmpty()) {
                        // Sender email
                        Person sender = this.personService.getPerson(initiator);
                        String emailSender = StringUtils.defaultIfBlank(sender.getMail(), initiator);

                        // Expression
                        String expression = variables.getString("stringMsg");

                        try {
                            // Mail session
                            Session mailSession = Session.getInstance(System.getProperties(), null);

                            // Message
                            MimeMessage message = new MimeMessage(mailSession);
                            message.setSentDate(new Date());

                            // From
                            InternetAddress from = new InternetAddress(emailSender);
                            message.setFrom(from);

                            // To
                            InternetAddress[] to = InternetAddress.parse(StringUtils.join(emailRecipients, ","));
                            message.setRecipients(Message.RecipientType.TO, to);

                            // Reply to
                            InternetAddress[] replyTo = new InternetAddress[]{from};
                            message.setReplyTo(replyTo);

                            // Subject
                            String subject = StringUtils.substringBefore(this.transform(portalControllerContext, expression, task, true),
                                    System.lineSeparator());
                            message.setSubject(subject, CharEncoding.UTF_8);

                            // Body
                            String inlineBody = this.transform(portalControllerContext, expression, task, false);
                            StringBuilder body = new StringBuilder();
                            for (String line : StringUtils.split(inlineBody, System.lineSeparator())) {
                                body.append("<p>");
                                body.append(line);
                                body.append("</p>");
                            }
                            // Body actions
                            if (BooleanUtils.isTrue(variables.getBoolean("acquitable"))) {
                                // Accept
                                String acceptActionId = variables.getString("actionIdYes");
                                if (StringUtils.isNotBlank(acceptActionId)) {
                                    String url = this.tasksService.getCommandUrl(portalControllerContext, uuid, acceptActionId);
                                    String title = bundle.getString("ACCEPT");
                                    Element link = DOM4JUtils.generateLinkElement(url, null, null, null, title);
                                    
                                    body.append("<p>");
                                    body.append(DOM4JUtils.writeCompact(link));
                                    body.append("</p>");
                                }
                            }

                            // Multipart
                            Multipart multipart = new MimeMultipart();
                            MimeBodyPart htmlPart = new MimeBodyPart();
                            htmlPart.setContent(body.toString(), "text/html; charset=UTF-8");
                            multipart.addBodyPart(htmlPart);
                            message.setContent(multipart);

                        	procLogger.info("  About to send mail on "+uuid+ " from "+emailSender+ " to "+StringUtils.join(emailRecipients, ",")+ " subject "+subject);

                            
                            // SMTP transport
                            SMTPTransport transport = (SMTPTransport) mailSession.getTransport();
                            transport.connect();
                            transport.sendMessage(message, message.getAllRecipients());
                            transport.close();
                            
                        	procLogger.info("  Mail sentl on "+uuid);

                        } catch (MessagingException e) {
                            this.log.warn("Email sending error", e.getCause());
                        }
                    }
                }
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String transform(PortalControllerContext portalControllerContext, String expression, Document task) throws PortalException {
        return this.transform(portalControllerContext, expression, task, false);
    }


    /**
     * Tranform expression with Expression-Language resolver.
     *
     * @param portalControllerContext portal controller context
     * @param expression expression
     * @param task task document
     * @param disabledLinks disabled links indicator
     * @return transformed expression
     * @throws PortalException
     */
    protected String transform(PortalControllerContext portalControllerContext, String expression, Document task, boolean disabledLinks) throws PortalException {
        // Procedure instance properties
        PropertyMap instanceProperties = task.getProperties().getMap("nt:pi");
        if (instanceProperties == null) {
            instanceProperties = new PropertyMap(0);
        }

        // Global variables
        PropertyMap globalVariables = instanceProperties.getMap("pi:globalVariablesValues");
        if (globalVariables == null) {
            globalVariables = new PropertyMap(0);
        }

        // Task variables
        PropertyMap taskVariables = task.getProperties().getMap("nt:task_variables");


        // Variables
        Map<String, String> variables = new HashMap<String, String>(globalVariables.size() + taskVariables.size());
        for (Entry<String, Object> entry : globalVariables.getMap().entrySet()) {
            variables.put(entry.getKey(), String.valueOf(entry.getValue()));
        }
        for (Entry<String, Object> entry : taskVariables.getMap().entrySet()) {
            variables.put(entry.getKey(), String.valueOf(entry.getValue()));
        }
        variables.put("procedureInitiator", instanceProperties.getString("pi:procedureInitiator"));
        variables.put("taskInitiator", task.getString("nt:initiator"));
        variables.put("taskName", task.getString("nt:name"));
        variables.put("taskUuid", task.getId());
        variables.put("taskPath", task.getPath());

        return this.transform(portalControllerContext, expression, variables, disabledLinks);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String transform(PortalControllerContext portalControllerContext, String expression, Map<String, String> variables) throws PortalException {
        return this.transform(portalControllerContext, expression, variables, false);
    }


    /**
     * Tranform expression with Expression-Language resolver.
     *
     * @param portalControllerContext portal controller context
     * @param expression expression
     * @param variables task variables
     * @param disabledLinks disabled links indicator
     * @return transformed expression
     * @throws PortalException
     */
    private String transform(PortalControllerContext portalControllerContext, String expression, Map<String, String> variables, boolean disabledLinks)
            throws PortalException {
        // UUID
        UUID uuid = null;
        if (variables != null) {
            String value = variables.get("uuid");
            if (StringUtils.isNotBlank(value)) {
                uuid = UUID.fromString(value);
            }
        }

        // Thread local container
        ThreadLocalContainer container = new ThreadLocalContainer(portalControllerContext, uuid, disabledLinks);


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
            context.setFunction("user", "email", TransformationFunctions.getUserEmailMethod());
            context.setFunction("group", "emails", TransformationFunctions.getGroupEmailsMethod());
            context.setFunction("document", "title", TransformationFunctions.getDocumentTitleMethod());
            context.setFunction("command", "link", TransformationFunctions.getCommandLinkMethod());
            context.setFunction("task", "link", TransformationFunctions.getViewTaskLinkMethod());            
            context.setFunction("document", "linkWithText", TransformationFunctions.getDocumentLinkWithTextMethod());
            context.setFunction("portal", "link", TransformationFunctions.getPortalLinkMethod());
            context.setFunction("portal", "translate", TransformationFunctions.getPortalTranslateMethod());            
            context.setFunction("portal", "prop", TransformationFunctions.getPortalPropertyMethod());            

            if (disabledLinks) {
                context.setFunction("user", "link", TransformationFunctions.getUserDisplayNameMethod());
                context.setFunction("document", "link", TransformationFunctions.getDocumentTitleMethod());
            } else {
                context.setFunction("user", "link", TransformationFunctions.getUserLinkMethod());
                context.setFunction("document", "link", TransformationFunctions.getDocumentLinkMethod());
            }
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
    
    @Override
    public Map<String, String> init(PortalControllerContext portalControllerContext, Document document, Map<String, String> variables) throws PortalException,
    FormFilterException {
        if (document != null) {
            if (variables == null) {
                variables = new HashMap<String, String>();
            }
            Map<String, UploadedFile> uploadedFiles = new HashMap<>(0);
            Principal userPrincipal = portalControllerContext.getHttpServletRequest().getUserPrincipal();
            
            // #1964 - User initiator may be anonymous
            String procedureInitiator = null;
            if(userPrincipal != null) {
            	procedureInitiator = userPrincipal.getName();
            }
            else {
            	// TODO null value for anonymous users
            	procedureInitiator = "anonymous";
            }
            
            Locale locale = portalControllerContext.getHttpServletRequest().getLocale();
            Bundle bundle = this.bundleFactory.getBundle(locale);
            PropertyMap initActionProperties = null;
            PropertyMap currentStepProperties = null;
            String startDate = null;
            String lastModified = null;
            String previousTaskInitiator = null;
            String modelWebId = null;
            String procedureInstanceUuid = null;
            // retrieve correct properties according to type of provided document
            if (StringUtils.equals(document.getType(), "ProcedureModel")) {
                String startingStep = document.getString("pcd:startingStep");
                if (StringUtils.isNotBlank(startingStep)) {
                    modelWebId = document.getString("ttc:webid");
                    PropertyMap startingStepProperties = this.getStepProperties(document, startingStep);
                    if (startingStepProperties != null) {
                        currentStepProperties = startingStepProperties;
                    } else {
                        String errorMessage = bundle.getString("FORMS_BAD_STARTING_STEP", startingStep);
                        throw new PortalException(errorMessage);
                    }
                } else {
                    String errorMessage = bundle.getString("FORMS_NO_STARTING_STEP");
                    throw new PortalException(errorMessage);
                }
            } else if (StringUtils.equals(document.getType(), "RecordFolder")) {
                String startingStep = variables.get("pcd:startingStep");
                if (StringUtils.isNotBlank(startingStep)) {
                    modelWebId = document.getString("pcd:webIdParent");
                    document = getModel(portalControllerContext, modelWebId);
                    PropertyMap startingStepProperties = this.getStepProperties(document, startingStep);
                    if (startingStepProperties != null) {
                        currentStepProperties = startingStepProperties;
                    } else {
                        String errorMessage = bundle.getString("FORMS_BAD_STARTING_STEP", startingStep);
                        throw new PortalException(errorMessage);
                    }
                } else {
                    String errorMessage = bundle.getString("FORMS_NO_STARTING_STEP");
                    throw new PortalException(errorMessage);
                }
            } else {
                PropertyMap instanceProperties = null;
                if (StringUtils.equals(document.getType(), "ProcedureInstance")) {
                    instanceProperties = document.getProperties();
                } else if (StringUtils.equals(document.getType(), "TaskDoc")) {
                    PropertyMap taskProperties = document.getProperties();
                    instanceProperties = taskProperties.getMap("nt:pi");

                    // Task initiator
                    previousTaskInitiator = taskProperties.getString("nt:initiator");
                }
                modelWebId = instanceProperties.getString("pi:procedureModelWebId");
                Document model = this.getModel(portalControllerContext, modelWebId);
                if (StringUtils.equals(model.getType(), "RecordFolder")) {
                    modelWebId = model.getString("pcd:webIdParent");
                    model = this.getModel(portalControllerContext, modelWebId);
                }
                String currentStep = instanceProperties.getString("pi:currentStep");
                currentStepProperties = this.getStepProperties(model, currentStep);

                // Global Variables Values
                Map<String, Object> globalVariableValuesMap = instanceProperties.getMap("pi:globalVariablesValues").map();
                Map<String, String> globalVariableValues = new HashMap<String, String>(globalVariableValuesMap.size());
                for (Entry<String, Object> gvvEntry : globalVariableValuesMap.entrySet()) {
                    globalVariableValues.put(gvvEntry.getKey(), String.valueOf(gvvEntry.getValue()));
                }
                // Procedure instance UUID
                procedureInstanceUuid = globalVariableValues.get("uuid");

                // procedure startDate
                Date date = instanceProperties.getDate("dc:created");
                startDate = DATE_FORMAT.format(date);

                // procedure lastModified
                date = instanceProperties.getDate("dc:modified");
                lastModified = DATE_FORMAT.format(date);

            }
            if (currentStepProperties != null) {
                initActionProperties = currentStepProperties.getMap("initAction");
                if (initActionProperties != null) {
                    variables = callFilters(modelWebId, procedureInstanceUuid, null, variables, initActionProperties, null, null, uploadedFiles,
                            portalControllerContext, procedureInitiator, startDate, lastModified, previousTaskInitiator, null, null, bundle).getVariables();
                }
            }
        }
        return variables;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getUploadedFilesVariables(PortalControllerContext portalControllerContext, Map<String, UploadedFile> uploadedFiles)
            throws PortalException {
        Map<String, String> variables;

        if (MapUtils.isEmpty(uploadedFiles)) {
            variables = new HashMap<>(0);
        } else {
            variables = new HashMap<>(uploadedFiles.size());

            for (Entry<String, UploadedFile> entry : uploadedFiles.entrySet()) {
                String variableName = entry.getKey();
                UploadedFile uploadedFile = entry.getValue();

                // Temporary file
                File temporaryFile = uploadedFile.getTemporaryFile();

                if (uploadedFile.isDeleted()) {
                    variables.put(variableName, null);
                } else if (temporaryFile != null) {
                    // Digest
                    String digest;
                    try {
                        digest = this.getDigest(temporaryFile);
                    } catch (IOException e) {
                        throw new PortalException(e);
                    }

                    JSONObject object = new JSONObject();
                    object.put("digest", digest);
                    object.put("fileName", uploadedFile.getTemporaryMetadata().getFileName());

                    variables.put(variableName, object.toString());
                }
            }
        }

        return variables;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String convertToJson(PortalControllerContext portalControllerContext, Object object) throws PortalException {
        // JSON object writer
        ObjectWriter writer = mapper.writer();

        try {
            return writer.writeValueAsString(object);
        } catch (IOException e) {
            throw new PortalException(e);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String convertVariablesToJson(PortalControllerContext portalControllerContext, Map<String, String> variables) throws PortalException {
        // Objects
        Set<Map<String, String>> objects = new HashSet<>();
        for (Entry<String, String> entry : variables.entrySet()) {
            Map<String, String> object = new HashMap<>(2);
            object.put("name", entry.getKey());
            object.put("value", StringUtils.trimToEmpty(entry.getValue()));

            objects.add(object);
        }

        return this.convertToJson(portalControllerContext, objects);
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
     * Check if links are disabled.
     * 
     * @return true if links are disabled
     */
    public static boolean areLinksDisabled() {
        // Thread local container
        ThreadLocalContainer container = threadLocal.get();

        // Disabled links indicator
        boolean disabledLinks;
        if (container == null) {
            disabledLinks = false;
        } else {
            disabledLinks = container.disabledLinks;
        }

        return disabledLinks;
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
        /** Disabled links indicator. */
        private final boolean disabledLinks;


        /**
         * Constructor.
         *
         * @param portalControllerContext portal controller context
         * @param uuid UUID
         * @param disabledLinks disabled links indicator
         */
        public ThreadLocalContainer(PortalControllerContext portalControllerContext, UUID uuid, boolean disabledLinks) {
            super();
            this.portalControllerContext = portalControllerContext;
            this.uuid = uuid;
            this.disabledLinks = disabledLinks;
        }

    }

}
