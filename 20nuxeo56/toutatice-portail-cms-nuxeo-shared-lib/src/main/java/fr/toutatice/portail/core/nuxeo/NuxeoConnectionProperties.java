package fr.toutatice.portail.core.nuxeo;

import java.net.URI;

public class NuxeoConnectionProperties {
	
	private static String nuxeoPublicHost = System.getProperty("nuxeo.publicHost");
	private static String nuxeoPublicPort = System.getProperty("nuxeo.publicPort");
	private static String nuxeoPrivateHost = System.getProperty("nuxeo.privateHost");
	private static String nuxeoPrivatePort = System.getProperty("nuxeo.privatePort");
	
	private static String nuxeoCtx = "/nuxeo";
	

	public static String getNuxeoContext()	{
		return nuxeoCtx;
	}
	
	public static URI getPublicBaseUri() {
		URI uri = null;

		try {
			uri = new URI("http://" + nuxeoPublicHost + ":" + nuxeoPublicPort + nuxeoCtx);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return uri;
	}
	
	public static URI getPrivateBaseUri() {
		URI uri = null;

		try {
			uri = new URI("http://" + nuxeoPrivateHost + ":" + nuxeoPrivatePort + nuxeoCtx);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return uri;
	}	
	
}
