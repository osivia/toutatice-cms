package fr.toutatice.portail.cms.nuxeo.api.forms;

import java.util.Map;

import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.context.PortalControllerContext;

/**
 * Forms service interface.
 *
 * @author Cédric Krommenhoek
 */
public interface IFormsService {

    /** Forms webId prefix. */
    String FORMS_WEB_ID_PREFIX = "procedure_";
    /** End step name. */
    String ENDSTEP = "endStep";
    /** Delete on workflow ending parameter. */
    String DELETE_ON_ENDING_PARAMETER = "deleteOnEnding";
    /** parameter holding cms path to redirect to */
    String REDIRECT_CMS_PATH_PARAMETER = "_redirectCmsPath";
    /** parameter holding displayContext to redirect with */
    String REDIRECT_DISPLAYCONTEXT_PARAMETER = "_displayContext";
    /** parameter holding notification message after redirection */
    String REDIRECT_MESSAGE_PARAMETER = "_notificationMessage";
    /** Nuxeo task actor user prefix. */
    String ACTOR_USER_PREFIX = "user:";
    /** Nuxeo task actor group prefix. */
    String ACTOR_GROUP_PREFIX = "group:";
    /** step reference holding default form */
    String FORM_STEP_REFERENCE = "formulaire";


    /**
     * Start with default action.
     *
     * @param portalControllerContext portal controller context
     * @param modelWebId model identifier
     * @param variables variables
     * @return updated variables
     * @throws PortalException
     * @throws FormFilterException
     */
    Map<String, String> start(PortalControllerContext portalControllerContext, String modelWebId, Map<String, String> variables)
            throws PortalException, FormFilterException;


    /**
     * Start.
     *
     * @param portalControllerContext portal controller context
     * @param modelWebId model identifier
     * @param actionId action identifier
     * @param variables variables
     * @return updated variables
     * @throws PortalException
     * @throws FormFilterException
     */
    Map<String, String> start(PortalControllerContext portalControllerContext, String modelWebId, String actionId, Map<String, String> variables)
            throws PortalException, FormFilterException;


    /**
     * Proceed with default action.
     *
     * @param portalControllerContext portal controller context
     * @param task task document
     * @param variables variables
     * @return updated variables
     * @throws PortalException
     * @throws FormFilterException
     */
    Map<String, String> proceed(PortalControllerContext portalControllerContext, Document task, Map<String, String> variables)
            throws PortalException, FormFilterException;


    /**
     * Proceed.
     *
     * @param portalControllerContext portal controller context
     * @param task task document
     * @param actionId action identifier
     * @param variables variables
     * @return updated variables
     * @throws PortalException
     * @throws FormFilterException
     */
    Map<String, String> proceed(PortalControllerContext portalControllerContext, Document task, String actionId, Map<String, String> variables)
            throws PortalException, FormFilterException;


    /**
     * Proceed with default action.
     * 
     * @param portalControllerContext portal controller context
     * @param taskProperties task properties
     * @param variables variables
     * @return updated variables
     * @throws PortalException
     * @throws FormFilterException
     */
    Map<String, String> proceed(PortalControllerContext portalControllerContext, PropertyMap taskProperties, Map<String, String> variables)
            throws PortalException, FormFilterException;


    /**
     * Proceed.
     * 
     * @param portalControllerContext portal controller context
     * @param taskProperties task properties
     * @param actionId action identifier
     * @param variables variables
     * @return updated variables
     * @throws PortalException
     * @throws FormFilterException
     */
    Map<String, String> proceed(PortalControllerContext portalControllerContext, PropertyMap taskProperties, String actionId, Map<String, String> variables)
            throws PortalException, FormFilterException;


    /**
     * Tranform expression with Expression-Language resolver.
     *
     * @param portalControllerContext portal controller context
     * @param expression expression
     * @param task task document
     * @return transformed expression
     * @throws PortalException
     */
    String transform(PortalControllerContext portalControllerContext, String expression, Document task) throws PortalException;


    /**
     * Tranform expression with Expression-Language resolver.
     *
     * @param portalControllerContext portal controller context
     * @param expression expression
     * @param variables task variables
     * @return transformed expression
     * @throws PortalException
     */
    String transform(PortalControllerContext portalControllerContext, String expression, Map<String, String> variables) throws PortalException;

    /**
     * Execute initialisation of a form
     * retrieves properties from the parent model when necessary
     *
     * @param portalControllerContext
     * @param document
     * @param variables
     * @return variables updated by init filters
     * @throws PortalException
     * @throws FormFilterException
     */
    Map<String, String> init(PortalControllerContext portalControllerContext, Document document, Map<String, String> variables) throws PortalException,
    FormFilterException;
}