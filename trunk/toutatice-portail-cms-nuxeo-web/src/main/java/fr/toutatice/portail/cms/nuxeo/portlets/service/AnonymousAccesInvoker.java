package fr.toutatice.portail.cms.nuxeo.portlets.service;

import fr.toutatice.portail.api.cache.services.IServiceInvoker;

/**
 * Just to mark that publish site is not fetchable for this publication
 * 
 * @author jeanseb
 *
 */
public class AnonymousAccesInvoker implements IServiceInvoker {

	private static final long serialVersionUID = -4271471756834717062L;
	private boolean anonymousAccess;

	
	public AnonymousAccesInvoker(boolean anonymousAccess) {
		super();
		this.anonymousAccess = anonymousAccess;

	}



	public Object invoke() throws Exception {
		return new Boolean(anonymousAccess);
	}

}
