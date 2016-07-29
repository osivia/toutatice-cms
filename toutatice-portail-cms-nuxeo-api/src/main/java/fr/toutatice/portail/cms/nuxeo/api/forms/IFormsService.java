package fr.toutatice.portail.cms.nuxeo.api.forms;

import java.util.Map;

import org.nuxeo.ecm.automation.client.model.Document;
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


    /**
     * Start with default action.
     * 
     * @param portalControllerContext portal controller context
     * @param modelId model identifier
     * @param variables variables
     * @throws PortalException
     */
    void start(PortalControllerContext portalControllerContext, String modelId, Map<String, String> variables) throws PortalException;


    /**
     * Start.
     * 
     * @param portalControllerContext portal controller context
     * @param modelId model identifier
     * @param actionId action identifier
     * @param variables variables
     * @throws PortalException
     */
    void start(PortalControllerContext portalControllerContext, String modelId, String actionId, Map<String, String> variables) throws PortalException;


    /**
     * Proceed with default action.
     * 
     * @param portalControllerContext portal controller context
     * @param task task document
     * @param variables variables
     * @throws PortalException
     */
    void proceed(PortalControllerContext portalControllerContext, Document task, Map<String, String> variables) throws PortalException;


    /**
     * Proceed.
     * 
     * @param portalControllerContext portal controller context
     * @param task task document
     * @param actionId action identifier
     * @param variables variables
     * @throws PortalException
     */
    void proceed(PortalControllerContext portalControllerContext, Document task, String actionId, Map<String, String> variables) throws PortalException;


    /**
     * Tranform expression with Expression-Language resolver.
     * 
     * @param portalControllerContext portal controller context
     * @param expression expression
     * @param variables task variables
     * @return tranformed expression
     * @throws PortalException
     */
    String transform(PortalControllerContext portalControllerContext, String expression, Map<String, String> variables) throws PortalException;

}
