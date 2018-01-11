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
public class RollbackTransactionCommand implements INuxeoCommand {

    @Override
    public Object execute(Session nuxeoSession) throws Exception {
        TransactionService transactionService = nuxeoSession.getAdapter(TransactionService.class);
        transactionService.rollbackTransaction();

        // void operation
        return null;
    }

    @Override
    public String getId() {
        return RollbackTransactionCommand.class.getName();
    }

}
