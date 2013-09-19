package fr.toutatice.portail.cms.nuxeo.service.editablewindow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

/**
 * Requête Nuxeo pour supprimer une page.
 * 
 */
public class DocumentDeleteCommand implements INuxeoCommand {

    protected static final Log logger = LogFactory.getLog(DocumentDeleteCommand.class);

    /** Document courant */
    private Document inputDoc;

    public DocumentDeleteCommand(Document inputDoc) {
        this.inputDoc = inputDoc;
    }

    public Object execute(Session nuxeoSession) throws Exception {

        Object execute = null;

        OperationRequest request = nuxeoSession.newRequest("Document.Delete").setInput(inputDoc);

        execute = request.execute();

        return execute;
    }

    public String getId() {
        // TODO Auto-generated method stub
        return null;
    }


}
