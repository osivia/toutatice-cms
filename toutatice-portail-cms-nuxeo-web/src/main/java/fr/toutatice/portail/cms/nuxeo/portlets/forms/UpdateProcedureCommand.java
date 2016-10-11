package fr.toutatice.portail.cms.nuxeo.portlets.forms;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.DocRef;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

/**
 * Update procedure Nuxeo command.
 *
 * @author Cédric Krommenhoek
 * @see INuxeoCommand
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UpdateProcedureCommand implements INuxeoCommand {

    /** Procedure instance path. */
    private final String path;
    /** Task title */
    private final String taskTitle;
    /** Groups. */
    private final String groups;
    /** Users. */
    private final String users;
    /** Properties */
    private final PropertyMap properties;


    /**
     * Constructor.
     *
     * @param path procedure instance path
     * @param title task title
     * @param groups groups
     * @param users users
     * @param properties properties
     */
    public UpdateProcedureCommand(String path, String title, List<String> groups, List<String> users, Map<String, Object> properties) {
        super();
        this.path = path;
        this.taskTitle = title;
        this.groups = StringUtils.join(groups, ",");
        this.users = StringUtils.join(users, ",");
        this.properties = new PropertyMap(properties);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Session nuxeoSession) throws Exception {
        // Operation request
        OperationRequest request = nuxeoSession.newRequest("Services.UpdateProcedure");
        request.setInput(new DocRef(this.path));
        request.set("taskTitle", this.taskTitle);
        if (StringUtils.isNotEmpty(this.groups)) {
            request.set("groups", this.groups);
        }

        if (StringUtils.isNotEmpty(this.users)) {
            request.set("users", this.users);
        }
        request.set("properties", this.properties);
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
