/**
 * 
 */
package org.nuxeo.ecm.automation.client.adapters.transaction;

import org.nuxeo.ecm.automation.client.Session;

/**
 * @author david
 */
public class TransactionService {

    private static final String START_TRANSAC_OP_ID = "Repository.StartTransaction";
    private static final String ROLLBACK_TRANSAC_OP_ID = "Repository.RollbackTransaction";
    private static final String COMMIT_TRANSAC_OP_ID = "Repository.CommitTransaction";

    /**
     * Client session.
     */
    protected Session session;

    /**
     * Constructor.
     * 
     * @param session
     */
    public TransactionService(Session session) {
        this.session = session;
    }

    /**
     * Getter for session.
     * 
     * @return session
     */
    public Session getSession() {
        return this.session;
    }

    public void startTransaction() throws Exception {
        this.session.newRequest(START_TRANSAC_OP_ID).execute();
    }

    public void rollbackTransaction() throws Exception {
        this.session.newRequest(ROLLBACK_TRANSAC_OP_ID).execute();
    }

    public void commitTransaction() throws Exception {
        this.session.newRequest(COMMIT_TRANSAC_OP_ID).execute();
    }
}
