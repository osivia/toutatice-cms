package fr.toutatice.portail.cms.nuxeo.api.cms;

import org.osivia.portal.api.cms.Permissions;

/**
 * Nuxeo document permissions.
 * 
 * @author Cédric Krommenhoek
 * @see Permissions
 */
public interface NuxeoPermissions extends Permissions {

    /**
     * Check if the document can be validated by the current user.
     * 
     * @return true if the document can be validated by the current user
     */
    boolean canBeValidated();


    /**
     * Check if the document can be copied by the current user.
     * 
     * @return true if the document can be copied by the current user
     */
    boolean canBeCopied();


    /**
     * Check if the current user can validate online task.
     * 
     * @return true if the current user can validate online task
     */
    boolean canValidateOnlineTask();


    /**
     * Check if the current user can synchronize the document.
     * 
     * @return true if the current user can synchronize the document
     */
    boolean canSynchronize();


    /**
     * Check if the current user can unsynchronize the document.
     * 
     * @return true if the current user can unsynchronize the document
     */
    boolean canUnsynchronize();

}
