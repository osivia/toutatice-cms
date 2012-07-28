package fr.toutatice.portail.cms.nuxeo.portlets.service;

import fr.toutatice.portail.api.cache.services.IServiceInvoker;

/**
 * Just to mark that publish site is not fetchable for this publication
 * 
 * @author jeanseb
 *
 */
public class UnresolvablePublishSpaceInvoker implements IServiceInvoker {

	public Object invoke() throws Exception {
		return new Boolean(true);
	}

}
