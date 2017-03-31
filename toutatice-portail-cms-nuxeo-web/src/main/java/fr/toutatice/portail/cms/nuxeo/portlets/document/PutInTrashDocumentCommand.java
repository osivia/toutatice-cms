/**
 * 
 */
package fr.toutatice.portail.cms.nuxeo.portlets.document;

import org.nuxeo.ecm.automation.client.Session;

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
        
        return nuxeoSession.newRequest("Document.PutDocumentInTrash").set("document", docId).setHeader("nx_es_sync", "true").execute();

    }

    /* (non-Javadoc)
     * @see fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand#getId()
     */
    public String getId() {
        return "Document.PutInTrash: " + docId;
    }

}
