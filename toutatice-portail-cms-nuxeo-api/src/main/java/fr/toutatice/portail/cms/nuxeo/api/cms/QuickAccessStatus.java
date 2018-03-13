package fr.toutatice.portail.cms.nuxeo.api.cms;

public enum QuickAccessStatus {
	/** Default state : can add to quick access set */
	CAN_ADD_TO_QUICKACCESS,
	/** Can remove from quick access if a quick access is already set */
	CAN_REMOVE_FROM_QUICKACCESS,
	/**
	 * Cases : Workspace parent hasn't Set facet, or the document is in a
	 * Publication spaces or personal spaces and quick access is not allowed
	 */
	CANNOT_ADD_TO_QUICKACCESS;
}
