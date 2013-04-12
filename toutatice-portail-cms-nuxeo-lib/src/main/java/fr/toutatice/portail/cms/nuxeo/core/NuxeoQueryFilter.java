package fr.toutatice.portail.cms.nuxeo.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.portal.common.invocation.Scope;
import org.jboss.portal.core.model.portal.Portal;
import org.jboss.portal.core.model.portal.PortalObject;
import org.jboss.portal.core.model.portal.PortalObjectContainer;
import org.jboss.portal.core.model.portal.PortalObjectId;
import org.jboss.portal.core.model.portal.PortalObjectPath;
import org.jboss.portal.server.ServerInvocation;
import org.osivia.portal.core.page.PageProperties;
import org.osivia.portal.core.tracker.ITracker;
import org.osivia.portal.core.tracker.RequestContextUtil;

public class NuxeoQueryFilter {

	
	public static String addPublicationFilter(String nuxeoRequest, boolean displayLiveVersion)	{
		return addPublicationFilter(nuxeoRequest, displayLiveVersion, null);
	}
	
	public static String addPublicationFilter(String nuxeoRequest, boolean displayLiveVersion,  String requestFilteringPolicy) {

		/* Filtre pour sélectionner uniquement les version publiées */

		
		
		String requestFilter = "";

		if (displayLiveVersion) {
			// selection des versions lives : il faut exclure les proxys
			requestFilter = "ecm:mixinType != 'HiddenInNavigation' AND ecm:isProxy = 0  AND ecm:currentLifeCycleState <> 'deleted'  AND ecm:isCheckedInVersion = 0 ";
		} else {
			// sélection des folders et des documents publiés

			//requestFilter = "ecm:mixinType != 'HiddenInNavigation' AND ecm:isProxy = 1  AND ecm:currentLifeCycleState <> 'deleted' ";
			requestFilter = "ecm:isProxy = 1 AND ecm:mixinType != 'HiddenInNavigation'  AND ecm:currentLifeCycleState <> 'deleted' ";
		}
		
		String policyFilter = null;
		
		ServerInvocation invocation = RequestContextUtil.getServerInvocation();
		String portalName = PageProperties.getProperties().getPagePropertiesMap().get("portalName");
		PortalObjectContainer portalObjectContainer = (PortalObjectContainer) invocation.getAttribute( Scope.REQUEST_SCOPE,  "osivia.portalObjectContainer");
		PortalObject po = portalObjectContainer.getObject(PortalObjectId.parse("", "/" + portalName, PortalObjectPath.CANONICAL_FORMAT));

		
		if( requestFilteringPolicy != null)
			policyFilter = requestFilteringPolicy;
		else	{
			// Get portal policy filter
			policyFilter = po.getProperty("osivia.portal.requestFilteringPolicy");
		}

		
		if( "local".equals(policyFilter)){
			// Parcours des pages pour appliquer le filtre sur les  paths

			String pathFilter = "";

			for (PortalObject child : ((Portal) po).getChildren(PortalObject.PAGE_MASK)) {
				String cmsPath = child.getDeclaredProperty("osivia.cms.basePath");
				if (cmsPath != null && cmsPath.length() > 0) {
					if (pathFilter.length() > 0)
						pathFilter += " OR ";
					pathFilter += "ecm:path STARTSWITH '" + cmsPath + "'";
				}
			}

			if (pathFilter.length() > 0) {
				requestFilter = requestFilter + " AND " + "(" + pathFilter + ")";
			}
		}

		// Insertion du filtre avant le order

		String beforeOrderBy = "";
		String orderBy = "";

		try {
			Pattern ressourceExp = Pattern.compile("(.*)ORDER([ ]*)BY(.*)");

			Matcher m = ressourceExp.matcher(nuxeoRequest.toUpperCase());
			m.matches();

			if (m.groupCount() == 3) {
				beforeOrderBy = nuxeoRequest.substring(0, m.group(1).length());
				orderBy = nuxeoRequest.substring(m.group(1).length());
			}
		} catch (IllegalStateException e) {
			beforeOrderBy = nuxeoRequest;
		}

		String finalRequest = beforeOrderBy;

		if (finalRequest.length() > 0)
			finalRequest += " AND ";
		finalRequest += requestFilter;

		finalRequest += " " + orderBy;
		nuxeoRequest = finalRequest;

		return nuxeoRequest;

	}

}
