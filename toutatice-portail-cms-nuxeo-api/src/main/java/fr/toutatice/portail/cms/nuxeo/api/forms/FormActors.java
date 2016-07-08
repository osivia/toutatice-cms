package fr.toutatice.portail.cms.nuxeo.api.forms;

import java.util.ArrayList;
import java.util.List;



/**
 * The Class FormActors.
 */
public class FormActors {
    
    /** The groups. */
    List<String> groups = new ArrayList<String>();
    
    /** The users. */
    List<String> users = new ArrayList<String>();
    
    /**
     * Gets the users.
     *
     * @return the users
     */
    public List<String> getUsers() {
        return users;
    }
    
    /**
     * Gets the groups.
     *
     * @return the groups
     */
    public List<String> getGroups() {
        return groups;
    }
    

}
