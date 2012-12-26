package fr.toutatice.portail.cms.nuxeo.portlets.service;

import org.osivia.portal.api.cache.services.IServiceInvoker;

/**
 * Just to mark that publish site is not fetchable for this publication
 * 
 * @author jeanseb
 *
 */
public class FetchableContentInvoker implements IServiceInvoker {

	private static final long serialVersionUID = -4271471756834717062L;
	private boolean resolvable = false;

	
	public FetchableContentInvoker(boolean resolvable) {

		super();

		this.resolvable = true;
	}



	public Object invoke() throws Exception {
		return new Boolean(resolvable);
	}

}
