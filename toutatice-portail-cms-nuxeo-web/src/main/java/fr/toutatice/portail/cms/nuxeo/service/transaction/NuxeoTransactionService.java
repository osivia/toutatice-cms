/**
 * 
 */
package fr.toutatice.portail.cms.nuxeo.service.transaction;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.transaction.INuxeoTransactionService;


/**
 * @author david
 */
public class NuxeoTransactionService implements INuxeoTransactionService {

    /**
     * {@inheritDoc}
     */
    @Override
    public String startTransaction(NuxeoController nuxeoController) throws Exception {
        return (String) nuxeoController.executeNuxeoCommand(new StartTransactionCommand());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void markTransactionAsRollback(NuxeoController nuxeoController) throws Exception {
        nuxeoController.executeNuxeoCommand(new MarkTransactionAsRollbackCommand());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commitOrRollbackTransaction(NuxeoController nuxeoController) throws Exception {
        nuxeoController.executeNuxeoCommand(new CommitOrRollbackTransactionCommand());
    }

}
