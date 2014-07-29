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

import org.jboss.portal.server.ServerInvocation;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.tracker.RequestContextUtil;

import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoService;


// TODO: Auto-generated Javadoc
/**
 * The Class NuxeoQueryFilter.
 * 
 * Adds filters to the original nuxeo request
 * 
 * @author Jean-Sébastien Steux
 */

public class NuxeoQueryFilter {
    

	
	/**
	 * Gets the CMS ctx.
	 *
	 * @return the CMS ctx
	 */
	public static CMSServiceCtx getCMSCtx()	{

		CMSServiceCtx cmsCtx = new  CMSServiceCtx();
		
		ServerInvocation invocation = RequestContextUtil.getServerInvocation();
		
		cmsCtx.setServerInvocation(invocation);
	
		return cmsCtx;
	}
	
	/**
	 * Adds the publication filter.
	 *
	 * @param queryCtx the query ctx
	 * @param nuxeoRequest the nuxeo request
	 * @return the string
	 */
	public static String addPublicationFilter(NuxeoQueryFilterContext queryCtx,String nuxeoRequest) {

		// adapt thanks to CMSCustomizer
		
		INuxeoService nuxeoService = Locator.findMBean(INuxeoService.class, "osivia:service=NuxeoService");
		
		CMSServiceCtx ctx = getCMSCtx();
		if( queryCtx.getState() == queryCtx.STATE_LIVE)
			ctx.setDisplayLiveVersion("1");
		try {
			return nuxeoService.getCMSCustomizer().addPublicationFilter(ctx, nuxeoRequest, queryCtx.getPolicy());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

}
