package fr.toutatice.portail.cms.nuxeo.portlets.files;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.adapters.DocumentService;
import org.nuxeo.ecm.automation.client.model.DocRef;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.nuxeo.ecm.automation.client.model.IdRef;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

/**
 * Move document command.
 *
 * @author Cédric Krommenhoek
 * @see INuxeoCommand
 */
public class MoveDocumentCommand implements INuxeoCommand {

    /** Source documents identifiers. */
    private final Set<String> sourceIds;
    /** Target parent document identifier. */
    private final String targetId;


    /**
     * Constructor.
     *
     * @param sourceIds source documents identifiers
     * @param targetId target parent document identifier
     */
    public MoveDocumentCommand(List<String> sourceIds, String targetId) {
        super();
        this.sourceIds = new HashSet<String>(sourceIds);
        this.targetId = targetId;
    }


    /**
     * Constructor.
     *
     * @param sourceId source document identifier
     * @param targetId target parent document identifier
     */
    public MoveDocumentCommand(String sourceId, String targetId) {
        super();
        this.sourceIds = new HashSet<String>(1);
        this.sourceIds.add(sourceId);
        this.targetId = targetId;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public Documents execute(Session nuxeoSession) throws Exception {
        // Target parent document reference
        DocRef target = new IdRef(this.targetId);

        // Document service
        DocumentService documentService = nuxeoSession.getAdapter(DocumentService.class);

        // Moved documents
        Documents documents = new Documents(this.sourceIds.size());

        for (String sourceId : this.sourceIds) {
            // Source document reference
            DocRef source = new IdRef(sourceId);

            Document document = documentService.move(source, target);
            documents.add(document);
        }

        return documents;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getSimpleName());
        builder.append(" : ");
        builder.append(StringUtils.join(this.sourceIds, ","));
        builder.append(" ; ");
        builder.append(this.targetId);
        return builder.toString();
    }

}
