/**
 * 
 */
package fr.toutatice.portail.cms.nuxeo.portlets.files;

import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.adapters.DocumentService;
import org.nuxeo.ecm.automation.client.model.DocRef;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.IdRef;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;


/**
 * Copy document command.
 *
 * @see INuxeoCommand
 */
public class CopyDocumentCommand implements INuxeoCommand {
    
    /** Default prefix title. */
    public final static String DEFAULT_COPY_TITLE_PREFIX = "Copie de ";
    
    /** Document's id to copy. */
    private String sourceId;
    /** Folderish target's id. */
    private String targetId;
    
    /**
     * Constructor with default title.
     * 
     * @param docToCopyId String
     * @param targetId String
     */
    public CopyDocumentCommand(String sourceId, String targetId) {
        super();
        this.sourceId = sourceId;
        this.targetId = targetId;
    }

    /**
     * {@inheritDoc}
     * @throws Exception 
     */
    @Override
    public Document execute(Session nuxeoSession) throws Exception {
        DocumentService docService = nuxeoSession.getAdapter(DocumentService.class);
        
        DocRef sourceRef = new IdRef(this.sourceId); 
        DocRef targetRef = new IdRef(this.targetId);
        
        Document copiedDocument = docService.copy(sourceRef, targetRef);
        
        String title = DEFAULT_COPY_TITLE_PREFIX.concat(copiedDocument.getTitle());
        docService.setProperty(copiedDocument, "dc:title", title);
        return copiedDocument;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return this.getClass().getName().concat(": ").concat(this.sourceId).concat(" | ").concat(this.targetId);
    }

}
