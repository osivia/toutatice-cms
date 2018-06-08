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

/**
 * Nuxeo connection properties.
 */
public class NuxeoConnectionProperties {



	private static NuxeoSatelliteConnectionProperties defaultProperties=null;

    /**
     * Getter for Nuxeo context.
     * 
     * @return Nuxeo context
     */
    public static final String getNuxeoContext()	{
        return NuxeoSatelliteConnectionProperties.getNuxeoContext();
    }

    
    private static NuxeoSatelliteConnectionProperties getDefaultProperties() {
    	if( defaultProperties == null)
    		 defaultProperties = NuxeoSatelliteConnectionProperties.getConnectionProperties(null);
    	return defaultProperties;

    }

    /**
     * Getter for Nuxeo public domain URI (without context).
     * 
     * @return Nuxeo public domain URI
     */
    public static final URI getPublicDomainUri() {
    	return getDefaultProperties().getPublicDomainUri();

    }


    /**
     * Getter for Nuxeo public base URI (with context).
     * 
     * @return Nuxeo public base URI
     */
    public static final URI getPublicBaseUri() {
    	return getDefaultProperties().getPublicBaseUri();

    }


    /**
     * Getter for Nuxeo private base URI (with context).
     * 
     * @return Nuxeo private base URI
     */
    public static final URI getPrivateBaseUri() {
    	return getDefaultProperties().getPrivateBaseUri();
    }

}
