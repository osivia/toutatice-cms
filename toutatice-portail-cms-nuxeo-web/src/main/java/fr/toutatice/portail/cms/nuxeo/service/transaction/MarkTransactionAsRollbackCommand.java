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
public class MarkTransactionAsRollbackCommand implements INuxeoCommand {

    @Override
    public Object execute(Session nuxeoSession) throws Exception {
        TransactionService transactionService = nuxeoSession.getAdapter(TransactionService.class);
        transactionService.markTransactionAsRollback();

        // void operation
        return null;
    }

    @Override
    public String getId() {
        return MarkTransactionAsRollbackCommand.class.getName();
    }

}
