/**
 * 
 */
package fr.toutatice.portail.cms.nuxeo.portlets.document;

import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;


/**
 * @author david
 *
 */
public class FetchDocumentByUUIDCommand implements INuxeoCommand {
    
    /** Id. */
    private String id;

    /**
     * 
     */
    public FetchDocumentByUUIDCommand(String id) {
        this.id = id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Session nuxeoSession) throws Exception {
        OperationRequest request = nuxeoSession.newRequest("Fetch.Document").setHeader(Constants.HEADER_NX_SCHEMAS, "*").set("id", this.id);
        return request.execute();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return this.getClass().getName().concat(" [ID]: ").concat(this.id);
    }

}
