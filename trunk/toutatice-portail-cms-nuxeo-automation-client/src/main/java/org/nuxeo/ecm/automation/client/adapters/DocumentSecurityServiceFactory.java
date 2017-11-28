/**
 * 
 */
package org.nuxeo.ecm.automation.client.adapters;

import org.nuxeo.ecm.automation.client.AdapterFactory;
import org.nuxeo.ecm.automation.client.Session;


/**
 * 
 * @author david
 *
 */
public class DocumentSecurityServiceFactory implements AdapterFactory<DocumentSecurityService> {

    @Override
    public Class<?> getAcceptType() {
        return Session.class;
    }

    @Override
    public Class<DocumentSecurityService> getAdapterType() {
        return DocumentSecurityService.class;
    }

    @Override
    public DocumentSecurityService getAdapter(Object toAdapt) {
        return new DocumentSecurityService((Session) toAdapt);
    }

}
