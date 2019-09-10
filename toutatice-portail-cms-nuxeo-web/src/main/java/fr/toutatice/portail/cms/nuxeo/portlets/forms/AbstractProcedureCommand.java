package fr.toutatice.portail.cms.nuxeo.portlets.forms;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.adapters.DocumentService;
import org.nuxeo.ecm.automation.client.model.Blob;
import org.nuxeo.ecm.automation.client.model.Blobs;
import org.nuxeo.ecm.automation.client.model.DocRef;
import org.nuxeo.ecm.automation.client.model.FileBlob;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.portlet.model.UploadedFile;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

/**
 * Procedure Nuxeo command abstract super-class.
 * 
 * @author Cédric Krommenhoek
 * @see INuxeoCommand
 */
public abstract class AbstractProcedureCommand implements INuxeoCommand {

    /** Files Nuxeo document property. */
    private static final String FILES_PROPERTY = "files:files";


    /** Task title */
    private final String title;
    /** Task actors. */
    private final String actors;
    /** Task additional authorizations. */
    private final String additionalAuthorizations;
    /** Task properties */
    protected final PropertyMap properties;



    /**
     * Constructor.
     * 
     * @param taskTitle task title
     * @param actors task actors
     * @param additionalAuthorizations task additional authorizations.
     * @param properties task properties
     * @param uploadedFiles task uploaded files
     */
    public AbstractProcedureCommand(String title, Set<String> actors, Set<String> additionalAuthorizations, Map<String, Object> properties) {
        super();
        this.title = title;
        this.actors = StringUtils.trimToNull(StringUtils.join(actors, ","));
        this.additionalAuthorizations = StringUtils.trimToNull(StringUtils.join(additionalAuthorizations, ","));
        this.properties = new PropertyMap(properties);

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return null;
    }


    /**
     * Create operation request.
     * 
     * @param nuxeoSession Nuxeo session
     * @return operation request
     * @throws Exception
     */
    protected OperationRequest createOperationRequest(Session nuxeoSession) throws Exception {
        OperationRequest operationRequest = nuxeoSession.newRequest(this.getOperationId());
        operationRequest.set("taskTitle", this.title);
        operationRequest.set("actors", this.actors);
        operationRequest.set("additionalAuthorizations", this.additionalAuthorizations);
        operationRequest.set("properties", this.properties);
        return operationRequest;
    }


    /**
     * Get request operation identifier.
     * 
     * @return request operation identifier
     */
    protected abstract String getOperationId();


  
}
