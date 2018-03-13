package fr.toutatice.portail.cms.nuxeo.api.cms;


public enum LockStatus {

    /** Default state : can lock */
    CAN_LOCK,
    /** Can uunlock */
    CAN_UNLOCK,
    /** a lock is set and is not removable by this user */
    LOCKED,
    /** No lock avaliable (proxies, versions, ...) */
    NO_LOCK;

}
