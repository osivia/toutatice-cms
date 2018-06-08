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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.StringUtils;

/**
 * Nuxeo connection properties.
 */
public class NuxeoSatelliteConnectionProperties {

	/** Nuxeo public host. */
	private final String NUXEO_PUBLIC_HOST;
	/** Nuxeo public port. */
	private final String NUXEO_PUBLIC_PORT;
	/** Nuxeo private host. */
	private final String NUXEO_PRIVATE_HOST;
	/** Nuxeo private port. */
	private final String NUXEO_PRIVATE_PORT;
	/** Nuxeo context. */
	private static String NUXEO_CONTEXT = "/nuxeo";

	private static Map<String, NuxeoSatelliteConnectionProperties> connections = new ConcurrentHashMap<String, NuxeoSatelliteConnectionProperties>();

	public NuxeoSatelliteConnectionProperties(String satelliteName) {
		super();
		String propertyPrefix = "";
		if (!StringUtils.isEmpty(satelliteName))
			propertyPrefix = ".satellite." + satelliteName ;

		NUXEO_PUBLIC_HOST = System.getProperty("nuxeo" + propertyPrefix + ".publicHost");

		NUXEO_PUBLIC_PORT = System.getProperty("nuxeo" + propertyPrefix + ".publicPort");

		NUXEO_PRIVATE_HOST = System.getProperty("nuxeo" + propertyPrefix + ".privateHost");

		NUXEO_PRIVATE_PORT = System.getProperty("nuxeo" + propertyPrefix + ".privatePort");

	}

	/**
	 * Getter for Nuxeo context.
	 * 
	 * @return Nuxeo context
	 */
	public static final String getNuxeoContext() {
		return NUXEO_CONTEXT;
	}

	/**
	 * Getter for Nuxeo public domain URI (without context).
	 * 
	 * @return Nuxeo public domain URI
	 */
	public final URI getPublicDomainUri() {
		String scheme = "http";
		if ("443".equals(NUXEO_PUBLIC_PORT)) {
			scheme = "https";
		}

		try {
			URI uri;

			// #1421 If not specified, use current fqdn
			if (NUXEO_PUBLIC_HOST == null && NUXEO_PUBLIC_PORT == null) {
				uri = new URI("");
			}

			else if ("80".equals(NUXEO_PUBLIC_PORT) || "443".equals(NUXEO_PUBLIC_PORT)) {
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
		try {
			return new URI("http://" + NUXEO_PRIVATE_HOST + ":" + NUXEO_PRIVATE_PORT + NUXEO_CONTEXT);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

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

}
