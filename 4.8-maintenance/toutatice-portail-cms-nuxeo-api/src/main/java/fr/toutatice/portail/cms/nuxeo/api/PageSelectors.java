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
package fr.toutatice.portail.cms.nuxeo.api;


import java.util.List;
import java.util.Map;

import org.osivia.portal.api.page.PageParametersEncoder;



/**
 * The Class PageSelectors.
 * 
 * Provides facilites to manipulates public selector parameters (advanced search)
 */
public class PageSelectors {
	
	/**
	 * Encode properties.
	 *
	 * @param props the props
	 * @return the string
	 */
	public static String encodeProperties( Map <String, List<String>> props)	{
		
		return PageParametersEncoder.encodeProperties(props);
	}
	
	/**
	 * Decode properties.
	 *
	 * @param urlParams the url params
	 * @return the map
	 */
	public static Map<String,List<String>> decodeProperties( String urlParams)	{
		return PageParametersEncoder.decodeProperties(urlParams);
		

	}

	
}
