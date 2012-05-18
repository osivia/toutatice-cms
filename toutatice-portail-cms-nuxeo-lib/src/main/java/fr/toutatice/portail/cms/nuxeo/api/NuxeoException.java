package fr.toutatice.portail.cms.nuxeo.api;

import fr.toutatice.portail.core.cms.CMSException;

public class NuxeoException extends RuntimeException {
	

	private static final long serialVersionUID = 1L;
	
	private int errorCode = -1;
	
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
	
	public void rethrowCMSException () throws CMSException {
		
		if( getCause() != null)
			throw new CMSException(getCause());
		
		if( errorCode != -1)	{
			if( errorCode == ERROR_FORBIDDEN)
				throw new CMSException(CMSException.ERROR_FORBIDDEN);
			
			if( errorCode == ERROR_UNAVAILAIBLE)
				throw new CMSException(CMSException.ERROR_UNAVAILAIBLE);

			if( errorCode == ERROR_NOTFOUND)
				throw new CMSException(CMSException.ERROR_NOTFOUND);
		}
		
		throw new RuntimeException( this);
		
	}

}
