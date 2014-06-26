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
public class AskSetOnLineCommand implements INuxeoCommand {
    
    /** Current document */
    private Document inputDoc;

    public AskSetOnLineCommand(Document inputDoc) {
        this.inputDoc = inputDoc;
    }

    /* (non-Javadoc)
     * @see fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand#execute(org.nuxeo.ecm.automation.client.Session)
     */
    @Override
    public Object execute(Session nuxeoSession) throws Exception {
        OperationRequest request = nuxeoSession.newRequest("onlineWorkflow_start").setInput(inputDoc);
        return request.execute();
    }

    /* (non-Javadoc)
     * @see fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand#getId()
     */
    @Override
    public String getId() {
        return this.getClass().getSimpleName().concat(" : ").concat(inputDoc.getPath());
    }

}
