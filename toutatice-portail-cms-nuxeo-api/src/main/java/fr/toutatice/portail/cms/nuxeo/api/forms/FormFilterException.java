package fr.toutatice.portail.cms.nuxeo.api.forms;


public class FormFilterException extends Exception {

    /** serialVersionUID */
    private static final long serialVersionUID = 1L;

    public FormFilterException(String message) {
        super(message);
    }

    public FormFilterException(String message, Throwable cause) {
        super(message, cause);
    }


    public FormFilterException(Throwable cause) {
        super(cause);
    }
}
