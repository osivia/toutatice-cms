package fr.toutatice.portail.cms.nuxeo.api.forms;

import java.util.ArrayList;
import java.util.List;

/**
 * Form actors java-bean.
 */
public class FormActors {

    /** Actors groups. */
    private final List<String> groups;

    /** Actors users. */
    private final List<String> users;


    /**
     * Constructor.
     */
    public FormActors() {
        super();
        this.groups = new ArrayList<String>();
        this.users = new ArrayList<String>();
    }


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
