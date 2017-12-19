package fr.toutatice.portail.cms.nuxeo.api.transaction;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;

/**
 * 
 */

/**
 * @author david
 */
public interface INuxeoTransactionService {

    /**
     * Starts a transaction with Nx.
     * 
     * @param nuxeoController
     * @return transation identifiant
     * @throws Exception
     */
    String startTransaction(NuxeoController nuxeoController) throws Exception;

    void markTransactionAsRollback(NuxeoController nuxeoController) throws Exception;

    void commitOrRollbackTransaction(NuxeoController nuxeoController) throws Exception;

}
