package fr.toutatice.portail.cms.nuxeo.portlets.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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

    /** Task actors. */
    private final String actors;
    /** Notifiable task indicator. */
    private final Boolean notifiable;
    /** Task directives. */
    private final String directives;
    /** Task path. */
    private final String path;
    /** Task UUID. */
    private final UUID uuid;


    /**
     * Constructor.
     *
     * @param actor task actor
     */
    public GetTasksCommand(String actor) {
        super();
        Set<String> actors = new HashSet<String>(1);
        actors.add(actor);
        this.actors = this.getStringValues(actors);
        this.notifiable = null;
        this.directives = null;
        this.path = null;
        this.uuid = null;
    }

    /**
     * @param actor task actor
     * @param path task path
     */
    public GetTasksCommand(String actor, String path) {
        super();
        Set<String> actors = new HashSet<String>(1);
        actors.add(actor);
        this.actors = this.getStringValues(actors);
        this.notifiable = null;
        this.directives = null;
        this.path = path;
        this.uuid = null;
    }

    /**
     * Constructor.
     *
     * @param actors task actors
     * @param notifiable notifiable task indicator
     */
    public GetTasksCommand(Set<String> actors) {
        this(actors, true, null, null, null);
    }

    /**
     * Constructor.
     *
     * @param actors task actors
     * @param notifiable notifiable task indicator
     */
    public GetTasksCommand(Set<String> actors, String path) {
        this(actors, true, null, path, null);
    }

    /**
     * Constructor.
     *
     * @param actors task actors
     * @param notifiable notifiable task indicator
     */
    public GetTasksCommand(Set<String> actors, boolean notifiable) {
        this(actors, notifiable, null, null, null);
    }

    /**
     * Constructor.
     *
     * @param actors task actors
     * @param notifiable notifiable task indicator
     * @param directives task directives
     */
    public GetTasksCommand(Set<String> actors, boolean notifiable, Set<String> directives) {
        this(actors, notifiable, directives, null, null);
    }

    /**
     * Constructor.
     *
     * @param actors task actors
     * @param path task path
     * @param uuid task UUID
     */
    public GetTasksCommand(Set<String> actors, String path, UUID uuid) {
        this(actors, null, null, path, uuid);
    }

    /**
     * Constructor.
     *
     * @param actors task actors
     * @param notifiable notifiable task indicator
     * @param path task path
     * @param uuid task UUID
     */
    private GetTasksCommand(Set<String> actors, Boolean notifiable, Set<String> directives, String path, UUID uuid) {
        super();
        this.actors = this.getStringValues(actors);
        this.notifiable = notifiable;
        this.directives = this.getStringValues(directives);
        this.path = path;
        this.uuid = uuid;
    }


    /**
     * Get string values.
     *
     * @param values values
     * @return string
     */
    private String getStringValues(Collection<String> values) {
        String result;

        if (values == null) {
            result = null;
        } else {
            StringBuilder builder = new StringBuilder();

            boolean first = true;
            for (String value : values) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }

                builder.append("'");
                builder.append(value);
                builder.append("'");
            }

            result = builder.toString();
        }

        return result;
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
        if (this.actors != null) {
            query.append("AND nt:actors/* IN (").append(this.actors).append(") ");
        }
        if ((this.notifiable != null) || (this.directives != null)) {
            query.append("AND (");
            if (this.notifiable != null) {
                query.append("nt:task_variables.notifiable = '").append(this.notifiable).append("'");

                if (this.directives != null) {
                    query.append(" OR ");
                }
            }
            if (this.directives != null) {
                query.append("nt:directive IN (").append(this.directives).append(")");
            }
            query.append(") ");
        }
        if (this.path != null) {
            query.append("AND ecm:path = '").append(this.path).append("' ");
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
        builder.append(this.actors);
        builder.append("/");
        builder.append(this.notifiable);
        builder.append("/");
        builder.append(this.directives);
        builder.append("/");
        builder.append(this.path);
        builder.append("/");
        builder.append(this.uuid);
        return builder.toString();
    }

}