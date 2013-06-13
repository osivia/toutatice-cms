package fr.toutatice.portail.cms.nuxeo.service.editablewindow;

import java.util.List;

import org.nuxeo.ecm.automation.client.jaxrs.OperationRequest;
import org.nuxeo.ecm.automation.client.jaxrs.Session;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

/**
 * Requête Nuxeo pour supprimer plusieurs propriétés.
 * 
 */
public class DocumentRemovePropertyCommand implements INuxeoCommand {

    /** Document courant */
    private Document inputDoc;

    /** liste de propriétés */
    private List<String> propertiesToRemove;

    public DocumentRemovePropertyCommand(Document inputDoc, List<String> propertiesToRemove) {
        this.inputDoc = inputDoc;
        this.propertiesToRemove = propertiesToRemove;
    }

    public Object execute(Session nuxeoSession) throws Exception {

        Object execute = null;
        for (String property : propertiesToRemove) {
            OperationRequest request = nuxeoSession.newRequest("Document.RemoveProperty").setInput(inputDoc);

            request.set("xpath", property);

            execute = request.execute();
        }
        return execute;
    }

    public String getId() {
        // TODO Auto-generated method stub
        return null;
    }


}
