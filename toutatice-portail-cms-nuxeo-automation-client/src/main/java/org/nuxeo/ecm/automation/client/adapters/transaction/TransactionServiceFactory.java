/**
 * 
 */
package org.nuxeo.ecm.automation.client.adapters.transaction;

import org.nuxeo.ecm.automation.client.AdapterFactory;
import org.nuxeo.ecm.automation.client.Session;


/**
 * @author david
 */
public class TransactionServiceFactory implements AdapterFactory<TransactionService> {

    @Override
    public Class<?> getAcceptType() {
        return Session.class;
    }

    @Override
    public Class<TransactionService> getAdapterType() {
        return TransactionService.class;
    }

    @Override
    public TransactionService getAdapter(Object toAdapt) {
        return new TransactionService((Session) toAdapt);
    }

}
