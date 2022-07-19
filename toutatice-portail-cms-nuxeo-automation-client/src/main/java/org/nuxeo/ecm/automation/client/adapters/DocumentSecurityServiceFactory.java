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
    public DocumentSecurityService getAdapter(Session session, Class<DocumentSecurityService> clazz) {
        return new DocumentSecurityService(session);
    }

}
