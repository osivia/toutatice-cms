package fr.toutatice.portail.cms.nuxeo.core;

import java.net.URI;

import org.nuxeo.ecm.automation.client.jaxrs.Session;
import org.nuxeo.ecm.automation.client.jaxrs.impl.HttpAutomationClient;
import org.nuxeo.ecm.automation.client.jaxrs.spi.auth.PortalSSOAuthInterceptor;

public class NuxeoProperties {
	
	private String nuxeoHost;
	private String nuxeoPort;
	private static String nuxeoCtx = "/nuxeo";
	
	public NuxeoProperties()	{
		
		nuxeoHost = System.getProperty("nuxeo.host");
		nuxeoPort = System.getProperty("nuxeo.port");
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
	
	}
