/**
 * 
 */
package org.nuxeo.ecm.automation.client.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


/**
 * A Permission is a Nuxeo granted ACE.
 * 
 * @author david
 *
 */
public class DocumentPermissions {

    /** Specific map. */
    private Map<String, List<String>> aces;

    /**
     * 
     */
    public DocumentPermissions() {
        this.aces = new LinkedHashMap<String, List<String>>();
    }

    /**
     * @param aces
     */
    public DocumentPermissions(Map<String, List<String>> aces) {
        this.aces = aces;
    }

    /**
     * @param size
     */
    public DocumentPermissions(int size) {
        this.aces = new LinkedHashMap<String, List<String>>(size);
    }

    public void setPermission(String userName, String permission) {
        if (this.aces.containsKey(userName)) {
            List<String> permissions = this.aces.get(userName);
            if (permissions != null) {
                permissions.add(permission);
            }
        } else {
            ArrayList<String> permissions = new ArrayList<String>();
            permissions.add(permission);
            this.aces.put(userName, permissions);
        }
    }

    /**
     * @param userName
     * @param permissions
     */
    public void setPermissions(String userName, List<String> permissions) {
        this.aces.put(userName, permissions);
    }

    /**
     * Getter for permissions.
     * 
     * @param userName
     * @return permissions
     */
    public List<String> getPermissions(String userName) {
        return this.aces.get(userName);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();

        for (final Entry<String, List<String>> ace : this.aces.entrySet()) {
            builder.append(ace.getKey()).append("=");

            final List<String> permissions = ace.getValue();
            if (permissions != null) {
                PropertyList propL = new PropertyList();
                propL.getList().addAll(permissions);
                builder.append(DocumentPermissions.toString(propL));
            }

            builder.append("\n ");
        }

        return builder.toString();
    }
    
    // FIXME: to remove later: cf NuxeoJsonMapper#toString(final PropertyList list)
    public static String toString(final PropertyList list) {
        final StringBuilder builder = new StringBuilder("[");

        final Iterator<Object> iter = list.list().iterator();
        while (iter.hasNext()) {
            builder.append((String) iter.next());
            if (iter.hasNext()) {
                builder.append(",");
            }
        }

        builder.append("]");
        return builder.toString();
    }

}
