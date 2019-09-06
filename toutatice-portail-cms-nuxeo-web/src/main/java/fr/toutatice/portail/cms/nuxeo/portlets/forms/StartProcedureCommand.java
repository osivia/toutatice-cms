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
 * Start procedure Nuxeo command.
 *
 * @author Cédric Krommenhoek
 * @see AbstractProcedureCommand
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class StartProcedureCommand extends AbstractProcedureCommand {

    /** Request operation identifier. */
    private static final String OPERATION_ID = "Services.StartProcedure";


    /**
     * Constructor.
     *
     * @param title task title
     * @param actors task actors
     * @param additionalAuthorizations task additional authorizations
     * @param properties task properties
     * @param uploadedFiles task uploaded files
     */
    public StartProcedureCommand(String title, Set<String> actors, Set<String> additionalAuthorizations, Map<String, Object> properties) {
        super(title, actors, additionalAuthorizations, properties);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Session nuxeoSession) throws Exception {
        // Operation request
        OperationRequest operationRequest = this.createOperationRequest(nuxeoSession);

        // Result
        DocRef result = (DocRef) operationRequest.execute();


        return result;
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
            	
        return builder.toString();
    }

}
