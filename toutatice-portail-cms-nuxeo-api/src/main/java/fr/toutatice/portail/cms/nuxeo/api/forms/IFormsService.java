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

    /** Nuxeo task actor user prefix. */
    String ACTOR_USER_PREFIX = "user:";
    /** Nuxeo task actor group prefix. */
    String ACTOR_GROUP_PREFIX = "group:";


    /**
     * Start with default action.
     *
     * @param portalControllerContext portal controller context
     * @param modelId model identifier
     * @param variables variables
     * @return updated variables
     * @throws PortalException
     * @throws FormFilterException
     */
    Map<String, String> start(PortalControllerContext portalControllerContext, String modelId, Map<String, String> variables)
            throws PortalException, FormFilterException;


    /**
     * Start.
     *
     * @param portalControllerContext portal controller context
     * @param modelId model identifier
     * @param actionId action identifier
     * @param variables variables
     * @return updated variables
     * @throws PortalException
     * @throws FormFilterException
     */
    Map<String, String> start(PortalControllerContext portalControllerContext, String modelId, String actionId, Map<String, String> variables)
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
