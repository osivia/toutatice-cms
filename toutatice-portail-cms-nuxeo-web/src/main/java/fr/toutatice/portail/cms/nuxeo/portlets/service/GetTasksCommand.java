package fr.toutatice.portail.cms.nuxeo.portlets.service;

import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

/**
 * Get tasks Nuxeo command.
 * 
 * @author Cédric Krommenhoek
 * @see INuxeoCommand
 */
public class GetTasksCommand implements INuxeoCommand {

    /** User UID. */
    private final String user;
    /** Notifiable task indicator. */
    private final boolean notifiable;


    /**
     * Constructor.
     * 
     * @param user user UID
     * @param notifiable notifiable task indicator
     */
    public GetTasksCommand(String user, boolean notifiable) {
        super();
        this.user = user;
        this.notifiable = notifiable;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Session nuxeoSession) throws Exception {
        // Query
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM Document ");
        query.append("WHERE ecm:primaryType = 'TaskDoc' ");
        query.append("AND ecm:currentLifeCycleState <> 'ended' ");
        if (notifiable) {
            query.append("AND nt:task_variables.notifiable = 'true' ");
        }
        query.append("AND nt:actors = '").append(this.user).append("' ");

        // Operation request
        OperationRequest request = nuxeoSession.newRequest("Document.QueryES");
        request.set(Constants.HEADER_NX_SCHEMAS, "dublincore, task");
        request.set("query", query.toString());

        return request.execute();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getName());
        builder.append("/");
        builder.append(this.user);
        builder.append("/");
        builder.append(this.notifiable);
        return builder.toString();
    }

}
