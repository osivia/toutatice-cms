/**
 * 
 */
package fr.toutatice.portail.cms.nuxeo.service.editablewindow;

import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;


/**
 * @author David Chevrier
 */
public class ValidationPublishCommand implements INuxeoCommand {
    
    /** Current document */
    private Document inputDoc;
    /** Decision */
    private boolean accept;

    public ValidationPublishCommand(Document inputDoc, boolean accept) {
        this.inputDoc = inputDoc;
        this.accept = accept;
    }

    /* (non-Javadoc)
     * @see fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand#execute(org.nuxeo.ecm.automation.client.Session)
     */
    @Override
    public Object execute(Session nuxeoSession) throws Exception {
        String chainId = "portal_onlineWorkflow_validate";
        if(!accept){
            chainId = "portal_onlineWorkflow_rejected";
        }
        OperationRequest request = nuxeoSession.newRequest(chainId).setInput(inputDoc);
        return request.execute();
    }

    /* (non-Javadoc)
     * @see fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand#getId()
     */
    @Override
    public String getId() {
        return this.getClass().getSimpleName().concat(" : ").concat(inputDoc.getPath()).concat(" : ").concat(String.valueOf(accept));
    }

}
