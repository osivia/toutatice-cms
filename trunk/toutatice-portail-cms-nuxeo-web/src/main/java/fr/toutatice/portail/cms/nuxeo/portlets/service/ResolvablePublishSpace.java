package fr.toutatice.portail.cms.nuxeo.portlets.service;

import fr.toutatice.portail.api.cache.services.IServiceInvoker;

/**
 * Just to mark that publish site is not fetchable for this publication
 * 
 * @author jeanseb
 *
 */
public class ResolvablePublishSpace implements IServiceInvoker {

	private static final long serialVersionUID = -4271471756834717062L;
	private boolean resolvable;

	
	public ResolvablePublishSpace(boolean resolvable) {
		super();
		this.resolvable = resolvable;

	}



	public Object invoke() throws Exception {
		return new Boolean(resolvable);
	}

}
