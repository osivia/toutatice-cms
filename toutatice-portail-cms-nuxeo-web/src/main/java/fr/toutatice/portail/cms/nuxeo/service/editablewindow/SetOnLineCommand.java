package fr.toutatice.portail.cms.nuxeo.service.editablewindow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

/**
 * Requête Nuxeo pour modifier plusieurs propriétés.
 * 
 */
public class SetOnLineCommand implements INuxeoCommand {

    protected static final Log logger = LogFactory.getLog(SetOnLineCommand.class);

    /** Document courant */
    private Document inputDoc;

    public SetOnLineCommand(Document inputDoc) {
        this.inputDoc = inputDoc;
    }

    public Object execute(Session nuxeoSession) throws Exception {

        Object execute = null;

        OperationRequest request = nuxeoSession.newRequest("setOnLine").setInput(inputDoc);

        execute = request.execute();

        return execute;
    }

    public String getId() {
        return this.getClass().getSimpleName().concat(" : ").concat(inputDoc.getPath());
    }


}
