package fr.toutatice.portail.cms.nuxeo.api.cms;

public enum PinStatus {
	/** Default state : can pin */
	CAN_PIN,
	/** Can unpin if a pin is already set */
	CAN_UNPIN,
	/**
	 * Cases : Workspace parent hasn't Set facet, or the document is in a
	 * Publication spaces or personal spaces and pin is not allowed
	 */
	CANNOT_PIN;
}
