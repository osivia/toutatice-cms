package fr.toutatice.portail.cms.nuxeo.service.editablewindow;

import java.util.List;

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
public class DocumentUpdatePropertiesCommand implements INuxeoCommand {

    protected static final Log logger = LogFactory.getLog(DocumentUpdatePropertiesCommand.class);

    /** Document courant */
    private Document inputDoc;

    /** liste de propriétés */
    private List<String> propertiesToUpdate;

    public DocumentUpdatePropertiesCommand(Document inputDoc, List<String> propertiesToUpdate) {
        this.inputDoc = inputDoc;
        this.propertiesToUpdate = propertiesToUpdate;
    }

    public Object execute(Session nuxeoSession) throws Exception {

        Object execute = null;

        OperationRequest request = nuxeoSession.newRequest("Document.Update").setInput(inputDoc);

        StringBuilder sb = new StringBuilder();
        for (String property : propertiesToUpdate) {
            sb.append(property);
            sb.append("\n");
        }

        request.set("properties", sb.toString());

        if (logger.isDebugEnabled()) {
            logger.debug(sb.toString());
        }

        execute = request.execute();

        return execute;
    }

    public String getId() {
        // TODO Auto-generated method stub
        return null;
    }


}
