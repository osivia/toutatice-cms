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
package fr.toutatice.portail.cms.nuxeo.api.services;

import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.ICMSService;
import org.osivia.portal.core.cms.ICMSServiceLocator;
import org.osivia.portal.core.cms.Satellite;
import org.springframework.util.StringUtils;

/**
 * Nuxeo connection properties.
 */
public class NuxeoSatelliteConnectionProperties {

    /** Nuxeo context. */
    public static final String NUXEO_CONTEXT = "/nuxeo";

    /** Connections. */
    private static Map<String, NuxeoSatelliteConnectionProperties> connections = new ConcurrentHashMap<>();


    /** Satellite. */
    private final Satellite satellite;


    /**
     * Constructor.
     * 
     * @param satelliteName satellite name
     */
	public NuxeoSatelliteConnectionProperties(String satelliteName) {
		super();

        if (StringUtils.isEmpty(satelliteName)) {
            this.satellite = Satellite.MAIN;
            this.satellite.setPublicHost("nuxeo.publicHost");
            this.satellite.setPublicPort(System.getProperty("nuxeo.publicPort"));
            this.satellite.setPrivateHost(System.getProperty("nuxeo.privateHost"));
            this.satellite.setPrivatePort(System.getProperty("nuxeo.privatePort"));
        } else {
            // CMS service
            ICMSServiceLocator cmsServiceLocator = Locator.findMBean(ICMSServiceLocator.class, ICMSServiceLocator.MBEAN_NAME);
            ICMSService cmsService = cmsServiceLocator.getCMSService();

            Set<Satellite> satellites;
            try {
                satellites = cmsService.getSatellites();
            } catch (CMSException e) {
                satellites = null;
            }

            Satellite findedSatellite = null;
            if (CollectionUtils.isNotEmpty(satellites)) {
                Iterator<Satellite> iterator = satellites.iterator();
                while ((findedSatellite == null) && iterator.hasNext()) {
                    Satellite satellite = iterator.next();
                    if (StringUtils.pathEquals(satelliteName, satellite.getId())) {
                        findedSatellite = satellite;
                    }
                }
            }
            this.satellite = findedSatellite;
        }
	}


    /**
     * Get connection properties.
     * 
     * @param satelliteName satellite name
     * @return connection properties
     */
    public static NuxeoSatelliteConnectionProperties getConnectionProperties(String satelliteName) {
        String searchName = satelliteName;
        if (StringUtils.isEmpty(searchName)) {
            searchName = "MAIN";
        }
        NuxeoSatelliteConnectionProperties conn = connections.get(searchName);
        if (conn == null) {
            conn = new NuxeoSatelliteConnectionProperties(satelliteName);
            connections.put(searchName, conn);
        }
        return conn;
    }


	/**
	 * Getter for Nuxeo public domain URI (without context).
	 * 
	 * @return Nuxeo public domain URI
	 */
	public final URI getPublicDomainUri() {
        String publicHost = this.getPublicHost();
        String publicPort = this.getPublicPort();
	    
	    
		String scheme = "http";
		if ("443".equals(publicPort)) {
			scheme = "https";
		}

		try {
			URI uri;

			// #1421 If not specified, use current fqdn
            if (publicHost == null && publicPort == null) {
				uri = new URI("");
			}

            else if ("80".equals(publicPort) || "443".equals(publicPort)) {
                uri = new URI(scheme + "://" + publicHost);
			} else {
                uri = new URI(scheme + "://" + publicHost + ":" + publicPort);
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
	public final URI getPublicBaseUri() {
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
	public final URI getPrivateBaseUri() {
        String privateHost = this.getPrivateHost();
        String privatePort = this.getPrivatePort();

		try {
            return new URI("http://" + privateHost + ":" + privatePort + NUXEO_CONTEXT);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


    /**
     * Get public host.
     * 
     * @return host
     */
    private String getPublicHost() {
	    String publicHost;
	    if (this.satellite == null) {
	        publicHost = null;
	    } else {
	        publicHost = this.satellite.getPublicHost();
	    }
	    return publicHost;
	}


    /**
     * Get public port.
     * 
     * @return port
     */
    private String getPublicPort() {
        String publicPort;
        if (this.satellite == null) {
            publicPort = null;
        } else {
            publicPort = this.satellite.getPublicPort();
        }
        return publicPort;
    }


    /**
     * Get private host.
     * 
     * @return host
     */
    private String getPrivateHost() {
        String privateHost;
        if (this.satellite == null) {
            privateHost = null;
        } else {
            privateHost = this.satellite.getPrivateHost();
        }
        return privateHost;
    }


    /**
     * Get private port.
     * 
     * @return port
     */
    private String getPrivatePort() {
        String privateHost;
        if (this.satellite == null) {
            privateHost = null;
        } else {
            privateHost = this.satellite.getPrivatePort();
        }
        return privateHost;
    }

}
