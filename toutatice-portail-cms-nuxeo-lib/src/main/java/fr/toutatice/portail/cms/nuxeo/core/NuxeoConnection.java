package fr.toutatice.portail.cms.nuxeo.core;

import java.net.URI;

import org.nuxeo.ecm.automation.client.jaxrs.Session;
import org.nuxeo.ecm.automation.client.jaxrs.impl.HttpAutomationClient;
import org.nuxeo.ecm.automation.client.jaxrs.spi.auth.PortalSSOAuthInterceptor;

public class NuxeoConnection {
	
	private String nuxeoHost;
	private String nuxeoPort;
	private String secretKey;
	private static String nuxeoCtx = "/nuxeo";
	private boolean debugMode = false;
	
	public NuxeoConnection()	{
	
	
	nuxeoHost = System.getProperty("nuxeo.host");
	nuxeoPort = System.getProperty("nuxeo.port");
	secretKey = System.getProperty("nuxeo.secretKey");
	if( "1".equals(System.getProperty("nuxeo.debugMode")))
		debugMode = true;
	}


	public String getNuxeoContext()	{
		return nuxeoCtx;
	}
	
	public URI getBaseUri() {
		URI uri = null;

		try {
			uri = new URI("http://" + nuxeoHost + ":" + nuxeoPort + nuxeoCtx);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return uri;

	}
	
	public PortalSSOAuthInterceptor createAuthInterceptor( String userId) {
		return new PortalSSOAuthInterceptor(secretKey, userId);
	}

}
