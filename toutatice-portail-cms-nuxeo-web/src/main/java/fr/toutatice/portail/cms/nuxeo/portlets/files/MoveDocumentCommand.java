package fr.toutatice.portail.cms.nuxeo.portlets.files;

import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.adapters.DocumentService;
import org.nuxeo.ecm.automation.client.model.DocRef;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.IdRef;
import org.osivia.portal.core.page.PageProperties;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

/**
 * Move document command.
 *
 * @author Cédric Krommenhoek
 * @see INuxeoCommand
 */
public class MoveDocumentCommand implements INuxeoCommand {

    /** Source document identifier. */
    private final String sourceId;
    /** Target parent document identifier. */
    private final String targetId;


    /**
     * Constructor.
     *
     * @param sourceId source document identifier
     * @param targetId target parent document identifier
     */
    public MoveDocumentCommand(String sourceId, String targetId) {
        super();
        this.sourceId = sourceId;
        this.targetId = targetId;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Session nuxeoSession) throws Exception {
        // Source document reference
        DocRef source = new IdRef(this.sourceId);
        // Target parent document reference
        DocRef target = new IdRef(this.targetId);

        // Document service
        DocumentService documentService = nuxeoSession.getAdapter(DocumentService.class);
        Document result = documentService.move(source, target);

        // Reload navigation tree
        PageProperties.getProperties().setRefreshingPage(true);

        return result;
    }


    /**
     * {@inheritDoc}
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
