package fr.toutatice.portail.cms.nuxeo.portlets.forms;

import java.util.Date;
import java.util.Map;
import java.util.Set;

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
    /** Task actors. */
    private final String actors;
    /** Task additional authorizations. */
    private final String additionalAuthorizations;
    /** Task properties */
    private final PropertyMap properties;
    /** Task associated BLOBs */
    private final Blobs blobs;


    /**
     * Constructor.
     *
     * @param title task title
     * @param actors task actors
     * @param additionalAuthorizations task additional authorizations
     * @param properties task properties
     */
    public StartProcedureCommand(String title, Set<String> actors, Set<String> additionalAuthorizations, Map<String, Object> properties) {
        this(title, actors, additionalAuthorizations, properties, null);
    }

    /**
     * Constructor.
     *
     * @param title task title
     * @param actors task actors
     * @param additionalAuthorizations task additional authorizations
     * @param properties task properties
     * @param blobs task BLOBs
     */
    public StartProcedureCommand(String title, Set<String> actors, Set<String> additionalAuthorizations, Map<String, Object> properties,
            Blobs blobs) {
        super();
        this.taskTitle = title;
        this.actors = StringUtils.trimToNull(StringUtils.join(actors, ","));
        this.additionalAuthorizations = StringUtils.trimToNull(StringUtils.join(additionalAuthorizations, ","));
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
        request.set("actors", this.actors);
        request.set("additionalAuthorizations", this.additionalAuthorizations);
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
    	
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getSimpleName());
        builder.append("/");
        builder.append(new Date().getTime());
        builder.append("/");
        builder.append(properties.getString("pi:procedureModelWebId"));
            	
        return builder.toString();
    }

}
