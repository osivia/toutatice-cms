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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.portlet.RenderResponse;

import org.jboss.portal.common.invocation.Scope;
import org.jboss.portal.core.model.portal.Portal;
import org.jboss.portal.core.model.portal.PortalObject;
import org.jboss.portal.core.model.portal.PortalObjectContainer;
import org.jboss.portal.core.model.portal.PortalObjectId;
import org.jboss.portal.core.model.portal.PortalObjectPath;
import org.jboss.portal.server.ServerInvocation;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.menubar.MenubarItem;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.page.PageProperties;
import org.osivia.portal.core.tracker.ITracker;
import org.osivia.portal.core.tracker.RequestContextUtil;

import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoService;

public class NuxeoQueryFilter {

	
	public static String addPublicationFilter(String nuxeoRequest, boolean displayLiveVersion)	{
		return addPublicationFilter(nuxeoRequest, displayLiveVersion, null);
	}
	
	
	public static CMSServiceCtx getCMSCtx()	{

		CMSServiceCtx cmsCtx = new  CMSServiceCtx();
		
		ServerInvocation invocation = RequestContextUtil.getServerInvocation();
		
		cmsCtx.setServerInvocation(invocation);
	
		return cmsCtx;
	}
	
	public static String addPublicationFilter(String nuxeoRequest, boolean displayLiveVersion,  String requestFilteringPolicy) {

		// Adaptation via le CMSCustomizer
		
		INuxeoService nuxeoService = Locator.findMBean(INuxeoService.class, "osivia:service=NuxeoService");
		
		CMSServiceCtx ctx = getCMSCtx();
		if( displayLiveVersion)
			ctx.setDisplayLiveVersion("1");
		try {
			return nuxeoService.getCMSCustomizer().addPublicationFilter(ctx, nuxeoRequest, requestFilteringPolicy);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

}
