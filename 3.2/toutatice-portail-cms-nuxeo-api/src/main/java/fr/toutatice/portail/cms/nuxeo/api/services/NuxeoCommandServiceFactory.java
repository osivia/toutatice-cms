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



/**
 * Nuxeo portlet engine factory
 * 
 * Must be called during initialization dans destroy.
 *
 * @author jeanseb
 */
public class NuxeoCommandServiceFactory {
	
	/**
	 * Start nuxeo command service.
	 *
	 * @param ctx the ctx
	 * @throws Exception the exception
	 */
	public static void startNuxeoCommandService(PortletContext ctx) throws Exception	{
    	
    	INuxeoService nuxeoService = (INuxeoService)ctx.getAttribute("NuxeoService");
    	INuxeoCommandService nuxeoCommandService =  nuxeoService.startNuxeoCommandService(ctx);
     	ctx.setAttribute("nuxeoCommandService", nuxeoCommandService);
	}
	
	
	/**
	 * Gets the nuxeo command service.
	 *
	 * @param ctx the ctx
	 * @return the nuxeo command service
	 * @throws Exception the exception
	 */
	public static INuxeoCommandService getNuxeoCommandService(PortletContext ctx) throws Exception	{
		INuxeoCommandService nuxeoService =  (INuxeoCommandService) ctx.getAttribute("nuxeoCommandService");
		return nuxeoService;
	}
	
	/**
	 * Stop nuxeo command service.
	 *
	 * @param ctx the ctx
	 * @throws Exception the exception
	 */
	public static void stopNuxeoCommandService(PortletContext ctx) throws Exception	{
		INuxeoCommandService nuxeoService =  (INuxeoCommandService) ctx.getAttribute("nuxeoCommandService");
		
		if( nuxeoService != null)
			nuxeoService.destroy();

	}
	

}
