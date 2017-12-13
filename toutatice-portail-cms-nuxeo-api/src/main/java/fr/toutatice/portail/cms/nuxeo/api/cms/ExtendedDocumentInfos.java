/*
 * (C) Copyright 2014 OSIVIA (http://www.osivia.com)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package fr.toutatice.portail.cms.nuxeo.api.cms;

import java.util.Calendar;

import org.apache.commons.lang.StringUtils;

import net.sf.json.JSONObject;


/**
 * Object containing extended informations about a document (compare to CMSPublicationInfos).
 *
 * @author David Chevrier.
 */
public class ExtendedDocumentInfos {

    /** Task name from wich we get informations. */
    private String taskName;
    /** Indicates if a task of name taskName is pending on document */
    private boolean isOnlineTaskPending;
    /** Indicates if current user can manage pending task on document. */
    private boolean canUserValidateOnlineTask;
    /** Indicates if current user is the task's initiator. */
    private boolean isUserOnlineTaskInitiator;
    /** Indicates if a validation workflow is running on a given document. */
    private boolean isValidationWorkflowRunning;
    /** Draft count. */
    private int draftCount;
    /** Subscription status. */
    private SubscriptionStatus subscriptionStatus;
    /** Lock status */
    private LockStatus lockStatus;
    /** Owner of the lock */
    private String lockOwner;
    /** The time when document has been locked */
    private Calendar lockDate;
    /** Drive, folder can be synchronized */
    private boolean canSynchronize;
    /** Drive, folder can be unsynchronized */
    private boolean canUnsynchronize;
    /** Drive, Root of the synchro */
    private String synchronizationRootPath;

    /** Conversion of PDF. */
    private boolean pdfConvertible;
    /** Error on PDF conversion. */
    private boolean errorOnPdfConversion;

    private boolean isCurrentlyEdited;

    private JSONObject currentlyEditedEntry;

    private boolean isRecentlyEdited;

    private JSONObject recentlyEditedEntry;

    /**
     * Constructor.
     */
    public ExtendedDocumentInfos() {
        super();
        taskName = StringUtils.EMPTY;
    }


    /**
     * Subscription status enumeration.
     * A document has a state depending of the user who is browsing it.
     */
    public enum SubscriptionStatus {

        /** Default state : can subscribe */
        can_subscribe,
        /** Can unsubscribe if a subscription is already set */
        can_unsubscribe,
        /** If a subscription is defined by other document upper in the hierarchy, or if a group has subscribed before to them */
        has_inherited_subscriptions,
        /** Special cases : Domains, WorkspacesRoot, ... are not allowing subscription */
        no_subscriptions;

    }


    /**
     * Lock status enumeration.
     */
    public enum LockStatus {

        /** Default state : can lock */
        can_lock,
        /** Can uunlock */
        can_unlock,
        /** a lock is set and is not removable by this user */
        locked,
        /** No lock avaliable (proxies, versions, ...) */
        no_lock;

    }

    /**
     * Getter for taskName.
     *
     * @return the taskName
     */
    public String getTaskName() {
        return taskName;
    }

    /**
     * Setter for taskName.
     *
     * @param taskName the taskName to set
     */
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    /**
     * Getter for isOnlineTaskPending.
     *
     * @return the isOnlineTaskPending
     */
    public boolean isOnlineTaskPending() {
        return isOnlineTaskPending;
    }

    /**
     * Setter for isOnlineTaskPending.
     *
     * @param isOnlineTaskPending the isOnlineTaskPending to set
     */
    public void setOnlineTaskPending(boolean isOnlineTaskPending) {
        this.isOnlineTaskPending = isOnlineTaskPending;
    }

    /**
     * Getter for canUserValidateOnlineTask.
     *
     * @return the canUserValidateOnlineTask
     */
    public boolean isCanUserValidateOnlineTask() {
        return canUserValidateOnlineTask;
    }

    /**
     * Setter for canUserValidateOnlineTask.
     *
     * @param canUserValidateOnlineTask the canUserValidateOnlineTask to set
     */
    public void setCanUserValidateOnlineTask(boolean canUserValidateOnlineTask) {
        this.canUserValidateOnlineTask = canUserValidateOnlineTask;
    }

    /**
     * Getter for isUserOnlineTaskInitiator.
     *
     * @return the isUserOnlineTaskInitiator
     */
    public boolean isUserOnlineTaskInitiator() {
        return isUserOnlineTaskInitiator;
    }

    /**
     * Setter for isUserOnlineTaskInitiator.
     *
     * @param isUserOnlineTaskInitiator the isUserOnlineTaskInitiator to set
     */
    public void setUserOnlineTaskInitiator(boolean isUserOnlineTaskInitiator) {
        this.isUserOnlineTaskInitiator = isUserOnlineTaskInitiator;
    }

    /**
     * Getter for isValidationWorkflowRunning.
     *
     * @return the isValidationWorkflowRunning
     */
    public boolean isValidationWorkflowRunning() {
        return isValidationWorkflowRunning;
    }

    /**
     * Setter for isValidationWorkflowRunning.
     *
     * @param isValidationWorkflowRunning the isValidationWorkflowRunning to set
     */
    public void setValidationWorkflowRunning(boolean isValidationWorkflowRunning) {
        this.isValidationWorkflowRunning = isValidationWorkflowRunning;
    }

    /**
     * Getter for draftCount.
     *
     * @return the draftCount
     */
    public int getDraftCount() {
        return draftCount;
    }

    /**
     * Setter for draftCount.
     *
     * @param draftCount the draftCount to set
     */
    public void setDraftCount(int draftCount) {
        this.draftCount = draftCount;
    }

    /**
     * Getter for subscriptionStatus.
     *
     * @return the subscriptionStatus
     */
    public SubscriptionStatus getSubscriptionStatus() {
        return subscriptionStatus;
    }

    /**
     * Setter for subscriptionStatus.
     *
     * @param subscriptionStatus the subscriptionStatus to set
     */
    public void setSubscriptionStatus(SubscriptionStatus subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    /**
     * Getter for lockStatus.
     *
     * @return the lockStatus
     */
    public LockStatus getLockStatus() {
        return lockStatus;
    }

    /**
     * Setter for lockStatus.
     *
     * @param lockStatus the lockStatus to set
     */
    public void setLockStatus(LockStatus lockStatus) {
        this.lockStatus = lockStatus;
    }

    /**
     * Getter for lockOwner.
     *
     * @return the lockOwner
     */
    public String getLockOwner() {
        return lockOwner;
    }

    /**
     * Setter for lockOwner.
     *
     * @param lockOwner the lockOwner to set
     */
    public void setLockOwner(String lockOwner) {
        this.lockOwner = lockOwner;
    }

    /**
     * Getter for lockDate.
     *
     * @return the lockDate
     */
    public Calendar getLockDate() {
        return lockDate;
    }

    /**
     * Setter for lockDate.
     *
     * @param lockDate the lockDate to set
     */
    public void setLockDate(Calendar lockDate) {
        this.lockDate = lockDate;
    }

    /**
     * Getter for canSynchronize.
     *
     * @return the canSynchronize
     */
    public boolean isCanSynchronize() {
        return canSynchronize;
    }

    /**
     * Setter for canSynchronize.
     *
     * @param canSynchronize the canSynchronize to set
     */
    public void setCanSynchronize(boolean canSynchronize) {
        this.canSynchronize = canSynchronize;
    }

    /**
     * Getter for canUnsynchronize.
     *
     * @return the canUnsynchronize
     */
    public boolean isCanUnsynchronize() {
        return canUnsynchronize;
    }

    /**
     * Setter for canUnsynchronize.
     *
     * @param canUnsynchronize the canUnsynchronize to set
     */
    public void setCanUnsynchronize(boolean canUnsynchronize) {
        this.canUnsynchronize = canUnsynchronize;
    }

    /**
     * Getter for synchronizationRootPath.
     *
     * @return the synchronizationRootPath
     */
    public String getSynchronizationRootPath() {
        return synchronizationRootPath;
    }

    /**
     * Setter for synchronizationRootPath.
     *
     * @param synchronizationRootPath the synchronizationRootPath to set
     */
    public void setSynchronizationRootPath(String synchronizationRootPath) {
        this.synchronizationRootPath = synchronizationRootPath;
    }


    /**
     * @return the pdfConvertible
     */
    public boolean isPdfConvertible() {
        return pdfConvertible;
    }


    /**
     * @param pdfConvertible the pdfConvertible to set
     */
    public void setPdfConvertible(boolean pdfConvertible) {
        this.pdfConvertible = pdfConvertible;
    }


    /**
     * @return the errorOnPdfConversion
     */
    public boolean isErrorOnPdfConversion() {
        return errorOnPdfConversion;
    }


    /**
     * @param errorOnPdfConversion the errorOnPdfConversion to set
     */
    public void setErrorOnPdfConversion(boolean errorOnPdfConversion) {
        this.errorOnPdfConversion = errorOnPdfConversion;
    }


    /**
     * @return the isCurrentlyEdited
     */
    public boolean isCurrentlyEdited() {
        return isCurrentlyEdited;
    }


    /**
     * @param isCurrentlyEdited the isCurrentlyEdited to set
     */
    public void setCurrentlyEdited(boolean isCurrentlyEdited) {
        this.isCurrentlyEdited = isCurrentlyEdited;
    }


    /**
     * @return the currentlyEditedEntry
     */
    public JSONObject getCurrentlyEditedEntry() {
        return currentlyEditedEntry;
    }


    /**
     * @param currentlyEditedEntry the currentlyEditedEntry to set
     */
    public void setCurrentlyEditedEntry(JSONObject currentlyEditedEntry) {
        this.currentlyEditedEntry = currentlyEditedEntry;
    }


    /**
     * @return the recentlyEditedEntry
     */
    public JSONObject getRecentlyEditedEntry() {
        return recentlyEditedEntry;
    }


    /**
     * @param recentlyEditedEntry the recentlyEditedEntry to set
     */
    public void setRecentlyEditedEntry(JSONObject recentlyEditedEntry) {
        this.recentlyEditedEntry = recentlyEditedEntry;
    }


    /**
     * @return the isRecentlyEdited
     */
    public boolean isRecentlyEdited() {
        return isRecentlyEdited;
    }


    /**
     * @param isRecentlyEdited the isRecentlyEdited to set
     */
    public void setRecentlyEdited(boolean isRecentlyEdited) {
        this.isRecentlyEdited = isRecentlyEdited;
    }


}
