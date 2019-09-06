package fr.toutatice.portail.cms.nuxeo.portlets.forms;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.DocRef;
import org.osivia.portal.api.portlet.model.UploadedFile;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Update procedure Nuxeo command.
 *
 * @author Cédric Krommenhoek
 * @see AbstractProcedureCommand
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UpdateProcedureCommand extends AbstractProcedureCommand {

    /** Request operation identifier. */
    private static final String OPERATION_ID = "Services.UpdateProcedure";


    /** Procedure instance path. */
    private final String path;

    
    /** Force file upload */
    private DocRef uploadDocument = null;

    /**
     * Constructor.
     *
     * @param path procedure instance path
     * @param title task title
     * @param actors task actors
     * @param additionalAuthorizations task additional authorizations
     * @param properties task properties
     * @param uploadedFiles task uploaded files
     */
    public UpdateProcedureCommand(String path, String title, Set<String> actors, Set<String> additionalAuthorizations, Map<String, Object> properties) {
        super(title, actors, additionalAuthorizations, properties);
        this.path = path;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Session nuxeoSession) throws Exception {
        // Operation request
        OperationRequest operationRequest = this.createOperationRequest(nuxeoSession);
        operationRequest.setInput(new DocRef(this.path));

        // Result
        DocRef result = (DocRef) operationRequest.execute();


        return result;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected String getOperationId() {
        return OPERATION_ID;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {

        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getSimpleName());
        builder.append("/");
        builder.append(new Date().getTime());
        builder.append("/");
        builder.append(properties.getString("pi:procedureModelWebId"));
        builder.append("/");
        builder.append(properties.getString("pi:currentStep"));
            	
        return builder.toString();    
   }

}
