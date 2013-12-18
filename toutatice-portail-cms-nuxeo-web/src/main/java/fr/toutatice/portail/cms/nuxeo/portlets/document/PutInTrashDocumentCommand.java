/**
 * 
 */
package fr.toutatice.portail.cms.nuxeo.portlets.document;

import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.adapters.DocumentService;
import org.nuxeo.ecm.automation.client.model.DocRef;
import org.nuxeo.ecm.automation.client.model.Document;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;


/**
 * @author david
 */
public class PutInTrashDocumentCommand implements INuxeoCommand {

    private String docId;

    public PutInTrashDocumentCommand(String docId) {
        this.docId = docId;
    }

    /* (non-Javadoc)
     * @see fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand#execute(org.nuxeo.ecm.automation.client.Session)
     */
    public Object execute(Session nuxeoSession) throws Exception {
        DocumentService documentService = nuxeoSession.getAdapter(DocumentService.class);
        DocRef docRef = Document.newRef(docId);
        return documentService.setState(docRef, "delete");
    }

    /* (non-Javadoc)
     * @see fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand#getId()
     */
    public String getId() {
        return "Document.PutInTrash: " + docId;
    }

}
