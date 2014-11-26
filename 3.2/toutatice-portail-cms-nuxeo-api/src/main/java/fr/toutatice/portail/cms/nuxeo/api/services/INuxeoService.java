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

import javax.portlet.PortletContext;

import org.nuxeo.ecm.automation.client.Session;
import org.osivia.portal.core.cms.spi.ICMSIntegration;



/**
 * The Interface INuxeoService.
 */
public interface INuxeoService extends ICMSIntegration {
	
	/**
	 * Creates the user session.
	 *
	 * @param userId the user id
	 * @return the session
	 * @throws Exception the exception
	 */
	public Session createUserSession(String userId) throws Exception ;
	
	/**
	 * Register cms customizer.
	 *
	 * @param linkManager the link manager
	 */
	public void registerCMSCustomizer( INuxeoCustomizer linkManager);

	/**
	 * Gets the CMS customizer.
	 *
	 * @return the CMS customizer
	 */
	public INuxeoCustomizer getCMSCustomizer();
	
	/**
	 * Start nuxeo command service.
	 *
	 * @param portletCtx the portlet ctx
	 * @return the i nuxeo command service
	 * @throws Exception the exception
	 */
	public INuxeoCommandService startNuxeoCommandService(PortletContext portletCtx)  throws Exception ;

}
