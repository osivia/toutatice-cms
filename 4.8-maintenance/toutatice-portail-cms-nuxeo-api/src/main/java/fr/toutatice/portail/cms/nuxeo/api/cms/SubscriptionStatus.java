package fr.toutatice.portail.cms.nuxeo.api.cms;


public enum SubscriptionStatus {

    /** Default state : can subscribe */
    CAN_SUBSCRIBE,
    /** Can unsubscribe if a subscription is already set */
    CAN_UNSUBSCRIBE,
    /** If a subscription is defined by other document upper in the hierarchy, or if a group has subscribed before to them */
    HAS_INHERITED_SUBSCRIPTIONS,
    /** Special cases : Domains, WorkspacesRoot, ... are not allowing subscription */
    NO_SUBSCRIPTIONS;

}
