package fr.toutatice.portail.cms.nuxeo.portlets.files;

import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

/**
 * Sort document command.
 *
 * @author Cédric Krommenhoek
 * @see INuxeoCommand
 */
public class SortDocumentCommand implements INuxeoCommand {

    /** Source identifier. */
    private final String sourceId;
    /** Target identifier. */
    private final String targetId;


    /**
     * Constructor.
     *
     * @param sourceId source identifier
     * @param targetId target identifier, may be null
     */
    public SortDocumentCommand(String sourceId, String targetId) {
        super();
        this.sourceId = sourceId;
        this.targetId = targetId;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Session nuxeoSession) throws Exception {
        OperationRequest request = nuxeoSession.newRequest("Document.OrderDocument");
        request.set("sourceId", this.sourceId);
        if (this.targetId != null) {
            request.set("targetId", this.targetId);
        }
        return request.execute();
    }


    /**
     *
     */
    @Override
    public String getId() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getSimpleName());
        builder.append(" : ");
        builder.append(this.sourceId);
        builder.append(" ; ");
        builder.append(this.targetId);
        return builder.toString();
    }

}
