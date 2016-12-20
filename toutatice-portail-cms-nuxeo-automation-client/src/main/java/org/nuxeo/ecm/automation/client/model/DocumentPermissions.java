/**
 * 
 */
package org.nuxeo.ecm.automation.client.model;

import java.util.Map;


/**
 * A Permission is a Nuxeo granted ACE.
 * 
 * @author david
 *
 */
public class DocumentPermissions extends PropertyMap {

    private static final long serialVersionUID = -668727788004070858L;

    /**
     * 
     */
    public DocumentPermissions() {
        super();
    }

    /**
     * @param props
     */
    public DocumentPermissions(PropertyMap props) {
        super(props);
    }

    /**
     * @param map
     */
    public DocumentPermissions(Map<String, Object> map) {
        super(map);
    }

    /**
     * @param size
     */
    public DocumentPermissions(int size) {
        super(size);
    }

}
