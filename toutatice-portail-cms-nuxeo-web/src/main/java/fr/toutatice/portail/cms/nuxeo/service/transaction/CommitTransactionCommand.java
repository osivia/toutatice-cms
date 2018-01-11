/**
 * 
 */
package fr.toutatice.portail.cms.nuxeo.service.transaction;

import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.adapters.transaction.TransactionService;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;


/**
 * @author david
 */
public class CommitTransactionCommand implements INuxeoCommand {

    /**
     * Constructor (limited to package).
     */
    protected CommitTransactionCommand() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Session nuxeoSession) throws Exception {
        TransactionService transactionService = nuxeoSession.getAdapter(TransactionService.class);
        transactionService.startTransaction();

        // void operation
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return CommitTransactionCommand.class.getName();
    }

}
