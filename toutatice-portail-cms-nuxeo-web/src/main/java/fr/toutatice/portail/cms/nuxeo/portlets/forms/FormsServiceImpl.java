package fr.toutatice.portail.cms.nuxeo.portlets.forms;

import java.util.ArrayList;
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
import javax.portlet.PortletContext;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.cache.services.CacheInfo;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.directory.v2.DirServiceFactory;
import org.osivia.portal.api.directory.v2.model.Person;
import org.osivia.portal.api.directory.v2.service.PersonService;
import org.osivia.portal.api.html.DOM4JUtils;
import org.osivia.portal.api.internationalization.Bundle;
import org.osivia.portal.api.internationalization.IBundleFactory;
import org.osivia.portal.api.internationalization.IInternationalizationService;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.tasks.ITasksService;
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
import fr.toutatice.portail.cms.nuxeo.api.forms.FormActors;
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
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

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
    /** Log. */
    private final Log log;
    /** CMS service locator. */
    private final ICMSServiceLocator cmsServiceLocator;
    /** Tasks service. */
    private final ITasksService tasksService;
    /** Internationalization bundle factory. */
    private final IBundleFactory bundleFactory;
    /** Person service. */
    private final PersonService personService;


    /**
     * Constructor.
     *
     * @param cmsCustomizer CMS customizer
     */
    public FormsServiceImpl(DefaultCMSCustomizer cmsCustomizer) {
        super();
        this.cmsCustomizer = cmsCustomizer;
        this.log = LogFactory.getLog(this.getClass());

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
        String procedureInitiator = portalControllerContext.getHttpServletRequest().getRemoteUser();

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


        if (variables == null) {
            variables = new HashMap<String, String>();
        }

        // UUID
        String uuid = UUID.randomUUID().toString();
        variables.put("uuid", uuid);

        // Construction du contexte et appel des filtres
        FormFilterContext filterContext = this.callFilters(modelWebId, uuid, actionId, variables, actionProperties, actors, null, portalControllerContext,
                procedureInitiator, null, nextStep);

        if (!StringUtils.equals(ENDSTEP, filterContext.getNextStep())) {
            // Properties
            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put("pi:currentStep", actionProperties.getString("stepReference"));
            properties.put("pi:procedureModelWebId", modelWebId);
            properties.put("pi:globalVariablesValues", this.generateVariablesJSON(variables));

            // Nuxeo command
            INuxeoCommand command = new StartProcedureCommand(title, filterContext.getActors().getGroups(), filterContext.getActors().getUsers(), properties);
            try {
                this.cmsCustomizer.executeNuxeoCommand(cmsContext, command);
            } catch (CMSException e) {
                throw new PortalException(e);
            }
        }


        // End step indicator
        boolean endStep = ENDSTEP.equals(filterContext.getNextStep());


        // Email notification
        if (!endStep) {
            try {
                this.sendEmailNotification(portalControllerContext, uuid, procedureInitiator);
            } catch (CMSException e) {
                throw new PortalException(e);
            }
        }


        return filterContext.getVariables();
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

        // Procedure instance UUID
        String procedureInstanceUuid = globalVariableValues.get("uuid");

        // Construction du contexte et appel des filtres
        FormFilterContext filterContext = this.callFilters(modelWebId, procedureInstanceUuid, actionId, variables, actionProperties, actors,
                globalVariableValues, portalControllerContext, procedureInitiator, previousTaskInitiator, nextStep);

        // Properties
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("pi:currentStep", actionProperties.getString("stepReference"));
        properties.put("pi:procedureModelWebId", modelWebId);
        properties.put("pi:globalVariablesValues", this.generateVariablesJSON(globalVariableValues));

        // Nuxeo command
        INuxeoCommand command = new UpdateProcedureCommand(instancePath, title, filterContext.getActors().getGroups(),
                filterContext.getActors().getUsers(), properties);
        try {
            this.cmsCustomizer.executeNuxeoCommand(cmsContext, command);
        } catch (CMSException e) {
            throw new PortalException(e);
        }


        // End step indicator
        boolean endStep = ENDSTEP.equals(filterContext.getNextStep());


        // Email notification
        if (!endStep) {
            String uuid = globalVariableValues.get("uuid");
            String initiator = portalControllerContext.getHttpServletRequest().getRemoteUser();
            try {
                this.sendEmailNotification(portalControllerContext, uuid, initiator);
            } catch (CMSException e) {
                throw new PortalException(e);
            }
        }


        // Updated variables
        Map<String, String> updatedVariables = filterContext.getVariables();

        // Check if workflow must be deleted
        boolean deleteOnEnding = BooleanUtils.toBoolean(updatedVariables.get(DELETE_ON_ENDING_PARAMETER));
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
     * Appel des filtres.
     *
     * @param modelWebId model webId
     * @param procedureInstanceUuid procedure instance UUID
     * @param actionId
     * @param variables
     * @param actionProperties
     * @param actors
     * @return
     */
    private FormFilterContext callFilters(String modelWebId, String procedureInstanceUuid, String actionId, Map<String, String> variables,
            PropertyMap actionProperties, FormActors actors, Map<String, String> globalVariableValues, PortalControllerContext portalControllerContext,
            String procedureInitiator, String taskInitiator, String nextStep) throws FormFilterException {
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
        filterContext.setModelWebId(modelWebId);
        filterContext.setProcedureInstanceUuid(procedureInstanceUuid);
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
        PortletContext portletContext = this.cmsCustomizer.getPortletCtx();
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(portletContext);
        nuxeoController.setAuthType(NuxeoCommandContext.AUTH_TYPE_SUPERUSER);
        nuxeoController.setCacheType(CacheInfo.CACHE_SCOPE_NONE);

        // Internationalization bundle
        Locale locale = portalControllerContext.getHttpServletRequest().getLocale();
        Bundle bundle = this.bundleFactory.getBundle(locale);

        if (StringUtils.isNotEmpty(procedureInstanceId)) {
            // UUID
            UUID uuid = UUID.fromString(procedureInstanceId);

            // Nuxeo command
            INuxeoCommand command = new GetTasksCommand(null, uuid);
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
                    // Email recipients
                    Set<String> emailRecipients = new HashSet<String>(actors.size());
                    for (int i = 0; i < actors.size(); i++) {
                        Person person = this.personService.getPerson(actors.getString(i));
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
                                    String url = this.tasksService.getCommandUrl(portalControllerContext, uuid, acceptActionId, null);
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

                            // SMTP transport
                            SMTPTransport transport = (SMTPTransport) mailSession.getTransport();
                            transport.connect();
                            transport.sendMessage(message, message.getAllRecipients());
                            transport.close();
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
    private String transform(PortalControllerContext portalControllerContext, String expression, Document task, boolean disabledLinks) throws PortalException {
        // Procedure instance properties
        PropertyMap instanceProperties = task.getProperties().getMap("nt:pi");

        // Global variables
        PropertyMap globalVariables = instanceProperties.getMap("pi:globalVariablesValues");

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
