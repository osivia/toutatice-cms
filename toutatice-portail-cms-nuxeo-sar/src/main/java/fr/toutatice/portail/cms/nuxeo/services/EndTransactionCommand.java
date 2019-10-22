package fr.toutatice.portail.cms.nuxeo.services;

import org.apache.commons.io.IOUtils;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.FileBlob;
import org.nuxeo.ecm.automation.client.model.PathRef;
import org.nuxeo.ecm.automation.client.model.PropertyMap;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

public class EndTransactionCommand implements INuxeoCommand {

    private boolean commit;
    private String transactionId;

    public EndTransactionCommand(String transactionId, boolean commit) {
        super();
        this.commit = commit;
        this.transactionId = transactionId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Session session) throws Exception {
        try {
            if (!commit)
                session.newRequest("Repository.MarkTransactionAsRollback").setHeader("Tx-conversation-id", transactionId).execute();

            session.newRequest("Repository.CommitOrRollbackTransaction").setHeader("Tx-conversation-id", transactionId).execute();

        } finally {
            if (session != null) {
                session.close();
            }
        }
        return null;
    }

    @Override
    public String getId() {
        return "transaction " + transactionId + " " + commit;
    }
}
