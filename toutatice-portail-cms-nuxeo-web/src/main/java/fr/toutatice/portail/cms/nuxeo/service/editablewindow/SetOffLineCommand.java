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
public class SetOffLineCommand implements INuxeoCommand {

    protected static final Log logger = LogFactory.getLog(SetOffLineCommand.class);

    /** Document courant */
    private Document inputDoc;

    public SetOffLineCommand(Document inputDoc) {
        this.inputDoc = inputDoc;
    }

    public Object execute(Session nuxeoSession) throws Exception {

        Object execute = null;

        OperationRequest request = nuxeoSession.newRequest("setOffLine").setInput(inputDoc);

        execute = request.execute();

        return execute;
    }

    public String getId() {
        // TODO Auto-generated method stub
        return null;
    }


}
