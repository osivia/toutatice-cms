package fr.toutatice.portail.cms.nuxeo.api.services;

import java.net.URI;

/**
 * Nuxeo connection properties.
 */
public class NuxeoConnectionProperties {

    /** Nuxeo public host. */
    private static final String NUXEO_PUBLIC_HOST = System.getProperty("nuxeo.publicHost");
    /** Nuxeo public port. */
    private static final String NUXEO_PUBLIC_PORT = System.getProperty("nuxeo.publicPort");
    /** Nuxeo private host. */
    private static final String NUXEO_PRIVATE_HOST = System.getProperty("nuxeo.privateHost");
    /** Nuxeo private port. */
    private static final String NUXEO_PRIVATE_PORT = System.getProperty("nuxeo.privatePort");
    /** Nuxeo context. */
    private static final String NUXEO_CONTEXT = "/nuxeo";


    /**
     * Getter for Nuxeo context.
     * 
     * @return Nuxeo context
     */
    public static final String getNuxeoContext()	{
        return NUXEO_CONTEXT;
    }


    /**
     * Getter for Nuxeo public domain URI (without context).
     * 
     * @return Nuxeo public domain URI
     */
    public static final URI getPublicDomainUri() {
        String scheme = "http";
        if ("443".equals(NUXEO_PUBLIC_PORT)) {
            scheme = "https";
        }

        try {
            URI uri;
            if ("80".equals(NUXEO_PUBLIC_PORT) || "443".equals(NUXEO_PUBLIC_PORT)) {
                uri = new URI(scheme + "://" + NUXEO_PUBLIC_HOST);
            } else {
                uri = new URI(scheme + "://" + NUXEO_PUBLIC_HOST + ":" + NUXEO_PUBLIC_PORT);
            }
            return uri;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Getter for Nuxeo public base URI (with context).
     * 
     * @return Nuxeo public base URI
     */
    public static final URI getPublicBaseUri() {
        try {
            String domain = getPublicDomainUri().toString();
            return new URI(domain + NUXEO_CONTEXT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Getter for Nuxeo private base URI (with context).
     * 
     * @return Nuxeo private base URI
     */
    public static final URI getPrivateBaseUri() {
        try {
            return new URI("http://" + NUXEO_PRIVATE_HOST + ":" + NUXEO_PRIVATE_PORT + NUXEO_CONTEXT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
