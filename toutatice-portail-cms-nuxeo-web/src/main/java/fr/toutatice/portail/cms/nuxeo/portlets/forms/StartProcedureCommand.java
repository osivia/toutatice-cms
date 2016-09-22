package fr.toutatice.portail.cms.nuxeo.portlets.forms;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Blobs;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

/**
 * Start procedure Nuxeo command.
 *
 * @author Cédric Krommenhoek
 * @see INuxeoCommand
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class StartProcedureCommand implements INuxeoCommand {

    /** Task title */
    private final String taskTitle;
    /** Groups. */
    private final String groups;
    /** Users. */
    private final String users;
    /** Properties */
    private final PropertyMap properties;
    /** blobs */
    private final Blobs blobs;


    /**
     * Constructor.
     *
     * @param title task title
     * @param groups groups
     * @param users users
     * @param properties properties
     */
    public StartProcedureCommand(String title, List<String> groups, List<String> users, Map<String, Object> properties) {
        this(title, groups, users, properties, null);
    }

    /**
     * Constructor.
     *
     * @param title task title
     * @param groups groups
     * @param users users
     * @param properties properties
     * @param blobs blobs
     */
    public StartProcedureCommand(String title, List<String> groups, List<String> users, Map<String, Object> properties, Blobs blobs) {
        super();
        this.taskTitle = title;
        this.groups = StringUtils.join(groups, ",");
        this.users = StringUtils.join(users, ",");
        this.properties = new PropertyMap(properties);
        this.blobs = blobs;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Session nuxeoSession) throws Exception {
        // Operation request
        OperationRequest request = nuxeoSession.newRequest("Services.StartProcedure");

        request.set("taskTitle", this.taskTitle);

        if (StringUtils.isNotEmpty(this.groups)) {
            request.set("groups", this.groups);
        }

        if (StringUtils.isNotEmpty(this.users)) {
            request.set("users", this.users);
        }

        request.set("properties", this.properties);

        if (this.blobs != null) {
            request.setInput(this.blobs);
        }

        return request.execute();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return null;
    }

}