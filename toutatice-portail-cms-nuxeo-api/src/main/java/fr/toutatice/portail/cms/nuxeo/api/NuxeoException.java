/*
 * (C) Copyright 2014 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
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
 *
 *
 *    
 */
package fr.toutatice.portail.cms.nuxeo.api;

import org.osivia.portal.core.cms.CMSException;

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
