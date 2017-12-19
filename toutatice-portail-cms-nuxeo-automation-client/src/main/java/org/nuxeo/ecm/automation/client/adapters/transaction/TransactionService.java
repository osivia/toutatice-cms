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
    private static final String MARK_TRANSAC_ROLLBACK_OP_ID = "Repository.MarkTransactionAsRollback";
    private static final String COMMIT_OR_ROLLBACK_TRANSAC_OP_ID = "Repository.CommitOrRollbackTransaction";

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

    public String startTransaction() throws Exception {
        return (String) this.session.newRequest(START_TRANSAC_OP_ID).execute();
    }

    public void markTransactionAsRollback() throws Exception {
        this.session.newRequest(MARK_TRANSAC_ROLLBACK_OP_ID).execute();
    }

    public void commitOrRollbackTransaction() throws Exception {
        this.session.newRequest(COMMIT_OR_ROLLBACK_TRANSAC_OP_ID).execute();
    }
}
