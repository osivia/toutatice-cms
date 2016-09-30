package fr.toutatice.portail.cms.nuxeo.portlets.service;

import java.util.UUID;

import org.apache.commons.lang.StringUtils;
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
    private final Boolean notifiable;
    /** UUID. */
    private final UUID uuid;


    /**
     * Constructor.
     *
     * @param user user identifier
     * @param notifiable notifiable task indicator
     */
    public GetTasksCommand(String user, boolean notifiable) {
        this(user, notifiable, null);
    }

    /**
     * Constructor.
     * 
     * @param user user identifier
     * @param uuid UUID
     */
    public GetTasksCommand(String user, UUID uuid) {
        this(user, null, uuid);
    }

    /**
     * Constructor.
     * 
     * @param user user identifier
     * @param notifiable notifiable task indicator
     * @param uuid UUID
     */
    private GetTasksCommand(String user, Boolean notifiable, UUID uuid) {
        super();
        this.user = user;
        this.notifiable = notifiable;
        this.uuid = uuid;
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
        query.append("AND ecm:currentLifeCycleState = 'opened' ");
        if (StringUtils.isNotEmpty(this.user)) {
            query.append("AND nt:actors = '").append(this.user).append("' ");
        }
        if (this.notifiable != null) {
            query.append("AND nt:task_variables.notifiable = '").append(this.notifiable).append("' ");
        }
        if (this.uuid != null) {
            query.append("AND nt:pi.pi:globalVariablesValues.uuid = '").append(this.uuid).append("' ");
        }

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
        builder.append("/");
        builder.append(this.uuid);
        return builder.toString();
    }

}
