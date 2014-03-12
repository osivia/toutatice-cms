package fr.toutatice.portail.cms.nuxeo.portlets.comments;

import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

/**
 * Delete comment command.
 * 
 * @author David Chevrier
 * @author Cédric Krommenhoek
 * @see INuxeoCommand
 */
public class DeleteCommentCommand implements INuxeoCommand {

    /** Nuxeo document. */
    private final Document document;
    /** Comment identifier. */
    private final String id;


    /**
     * Constructor.
     *
     * @param document Nuxeo document
     * @param id comment identifier
     */
    public DeleteCommentCommand(Document document, String id) {
        super();
        this.document = document;
        this.id = id;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Session nuxeoSession) throws Exception {
        OperationRequest request = nuxeoSession.newRequest("Document.DeleteComment");
        request.set("commentableDoc", this.document.getId());
        request.set("comment", this.id);
        return request.execute();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return "Document.DeleteComment: " + this.document.getTitle();
    }

}
