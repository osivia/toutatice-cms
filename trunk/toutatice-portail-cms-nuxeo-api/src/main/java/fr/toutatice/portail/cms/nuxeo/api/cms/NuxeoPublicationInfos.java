package fr.toutatice.portail.cms.nuxeo.api.cms;

import java.util.Date;
import java.util.Set;

import org.osivia.portal.api.cms.PublicationInfos;

/**
 * Nuxeo document publication informations.
 * 
 * @author Cédric Krommenhoek
 * @see PublicationInfos
 */
public interface NuxeoPublicationInfos extends PublicationInfos {

    /**
     * Check if the document is publishable remotely.
     * 
     * @return true if the document is publishable remotely
     */
    boolean isRemotePublishable();


    /**
     * Check if the document is published remotely.
     * 
     * @return true if the document is published remotely
     */
    boolean isRemotePublished();


    /**
     * Get subtype names of the document.
     * 
     * @return names
     */
    Set<String> getSubtypes();


    /**
     * Get space document identifier.
     * 
     * @return document identifier
     */
    String getSpaceId();


    /**
     * Get parent space document identifier.
     * 
     * @return document identifier
     */
    String getParentSpaceId();


    /**
     * Get Nuxeo Drive edition URL.
     * 
     * @return URL
     */
    String getDriveEditionUrl();


    /**
     * Get task name.
     * 
     * @return task name
     */
    String getTaskName();


    /**
     * Check if they are online task pending.
     * 
     * @return true if they are online task pending
     */
    boolean isOnlineTaskPending();


    /**
     * Check if the current user is the online task initiator.
     * 
     * @return true if the current user is the online task initiator
     */
    boolean isOnlineTaskInitiator();


    /**
     * Check if the document has a running validation workflow.
     * 
     * @return true if the document has a running validation workflow
     */
    boolean isValidationWorkflowRunning();


    /**
     * Get draft count.
     * 
     * @return count
     */
    int getDraftCount();


    /**
     * Get subscription status.
     * 
     * @return subscription status
     */
    SubscriptionStatus getSubscriptionStatus();


    /**
     * Get lock status.
     * 
     * @return lock status
     */
    LockStatus getLockStatus();


    /**
     * Get lock owner.
     * 
     * @return owner
     */
    String getLockOwner();


    /**
     * Get lock date.
     * 
     * @return date
     */
    Date getLockDate();


    /**
     * Get synchronisation root path.
     * 
     * @return path
     */
    String getSynchronizationRootPath();


    /**
     * Check if the document is PDF convertible.
     * @return true if the document is PDF convertible
     */
    boolean isPdfConvertible();


    /**
     * Check if an error occurs on PDF conversion.
     * 
     * @return true if an error occurs on PDF conversion
     */
    boolean isErrorOnPdfConversion();

}
