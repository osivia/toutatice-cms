package fr.toutatice.portail.cms.nuxeo.portlets.forms;

import java.util.Date;
import java.util.Map;
import java.util.Set;

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
    /** Task actors. */
    private final String actors;
    /** Task additional authorizations. */
    private final String additionalAuthorizations;
    /** Task properties */
    private final PropertyMap properties;


    /**
     * Constructor.
     *
     * @param path procedure instance path
     * @param title task title
     * @param actors task actors
     * @param additionalAuthorizations task additional authorizations
     * @param properties task properties
     */
    public UpdateProcedureCommand(String path, String title, Set<String> actors, Set<String> additionalAuthorizations,
            Map<String, Object> properties) {
        super();
        this.path = path;
        this.taskTitle = title;
        this.actors = StringUtils.trimToNull(StringUtils.join(actors, ","));
        this.additionalAuthorizations = StringUtils.trimToNull(StringUtils.join(additionalAuthorizations, ","));
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
        request.set("actors", this.actors);
        request.set("additionalAuthorizations", this.additionalAuthorizations);
        request.set("properties", this.properties);

        return request.execute();
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
            	
        return builder.toString();    }

}
