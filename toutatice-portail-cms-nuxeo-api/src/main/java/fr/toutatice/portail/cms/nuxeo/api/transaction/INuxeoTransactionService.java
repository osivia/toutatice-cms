package fr.toutatice.portail.cms.nuxeo.api.transaction;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;

/**
 * 
 */

/**
 * @author david
 */
public interface INuxeoTransactionService {

    void startTransaction(NuxeoController nuxeoController) throws Exception;

    void rollbackTransaction(NuxeoController nuxeoController) throws Exception;

    void commitTransaction(NuxeoController nuxeoController) throws Exception;

}
