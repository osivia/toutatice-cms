package fr.toutatice.portail.cms.nuxeo.api;

public class NuxeoException extends RuntimeException {
	

	private static final long serialVersionUID = 1L;
	
	private int errorCode;
	
	public int getErrorCode() {
		return errorCode;
	}

	public static int ERROR_FORBIDDEN = 1;
	public static int ERROR_UNAVAILAIBLE = 2;
	public static int ERROR_NOTFOUND = 3;
	

	public NuxeoException(Throwable cause) {
	        super(cause);
	    }
	
	public NuxeoException(int errorCode) {
        this.errorCode = errorCode;
    }	

}
