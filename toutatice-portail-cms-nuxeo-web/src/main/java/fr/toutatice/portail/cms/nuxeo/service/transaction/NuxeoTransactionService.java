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
    public void startTransaction(NuxeoController nuxeoController) throws Exception {
        nuxeoController.executeNuxeoCommand(new StartTransactionCommand());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rollbackTransaction(NuxeoController nuxeoController) throws Exception {
        nuxeoController.executeNuxeoCommand(new RollbackTransactionCommand());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commitTransaction(NuxeoController nuxeoController) throws Exception {
        nuxeoController.executeNuxeoCommand(new CommitTransactionCommand());
    }

}
