package fr.toutatice.portail.cms.nuxeo.portlets.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletContext;

import org.nuxeo.ecm.automation.client.jaxrs.model.Document;

import fr.toutatice.portail.api.cache.services.CacheInfo;
import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommandService;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.core.DocumentFetchCommand;
import fr.toutatice.portail.cms.nuxeo.core.NuxeoCommandServiceFactory;
import fr.toutatice.portail.cms.nuxeo.jbossportal.NuxeoCommandContext;
import fr.toutatice.portail.core.cms.CMSException;
import fr.toutatice.portail.core.cms.CMSHandlerProperties;
import fr.toutatice.portail.core.cms.CMSItem;
import fr.toutatice.portail.core.cms.CMSServiceCtx;
import fr.toutatice.portail.core.cms.ICMSService;
import fr.toutatice.portail.core.cms.NavigationItem;
import fr.toutatice.portail.core.nuxeo.INuxeoService;
import fr.toutatice.portail.core.profils.IProfilManager;

public class CMSService implements ICMSService {

	private PortletContext portletCtx;
	INuxeoCommandService nuxeoCommandService;
	INuxeoService nuxeoService;
	IProfilManager profilManager;

	public CMSService(PortletContext portletCtx) {
		super();
		this.portletCtx = portletCtx;
	}

	private CMSItem createItem(String path, String displayName, Document doc) {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("displayName", displayName);
		properties.put("type", doc.getType());
		String pageTemplate =  (String) doc.getProperties().get("ttc:pageTemplate");
		if( pageTemplate != null && pageTemplate.length() > 0)
			properties.put("pageTemplate", pageTemplate);
		String pageScope =  (String) doc.getProperties().get("ttc:pageScope");
		if( pageScope != null && pageScope.length() > 0)
			properties.put("pageScope", pageScope);

		return new CMSItem(path, properties, doc);
	}

	public List<CMSItem> getChildren(CMSServiceCtx ctx, String path) throws CMSException {

		return new ArrayList<CMSItem>();
	}

	public IProfilManager getProfilManager() throws Exception {
		if (profilManager == null)
			profilManager = (IProfilManager) portletCtx.getAttribute("ProfilService");

		return profilManager;
	}

	public INuxeoService getNuxeoService() throws Exception {

		if (nuxeoService == null)
			nuxeoService = (INuxeoService) portletCtx.getAttribute("NuxeoService");

		return nuxeoService;

	}

	public INuxeoCommandService getNuxeoCommandService() throws Exception {
		if (nuxeoCommandService == null)
			nuxeoCommandService = (INuxeoCommandService) NuxeoCommandServiceFactory.getNuxeoCommandService(portletCtx);
		return nuxeoCommandService;
	}

	private Object executeNuxeoCommand(CMSServiceCtx cmsCtx, INuxeoCommand command) throws Exception {

		NuxeoCommandContext commandCtx = new NuxeoCommandContext(portletCtx, cmsCtx.getCtx());

		// pour debug
		// commandCtx.setCacheTimeOut(0);

		/*
		 * ctx.setAuthType(getAuthType()); ctx.setAuthProfil(getScopeProfil());
		 * ctx.setCacheTimeOut(cacheTimeOut); ctx.setCacheType(cacheType);
		 * ctx.setAsynchronousUpdates(asynchronousUpdates);
		 */

		String scope = cmsCtx.getScope();

		// Par défaut
		commandCtx.setAuthType(NuxeoCommandContext.AUTH_TYPE_USER);
		commandCtx.setCacheType(CacheInfo.CACHE_SCOPE_PORTLET_SESSION);

		if (scope != null) {
			if (!"__nocache".equals(scope)) {

				commandCtx.setAsynchronousUpdates(true);

				if ("anonymous".equals(scope)) {
					commandCtx.setAuthType(NuxeoCommandContext.AUTH_TYPE_ANONYMOUS);
					commandCtx.setCacheType(CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);
				} else {
					commandCtx.setAuthType(NuxeoCommandContext.AUTH_TYPE_PROFIL);
					commandCtx.setAuthProfil(getProfilManager().getProfil(scope));
					commandCtx.setCacheType(CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);
				}
			}
		}

		return getNuxeoCommandService().executeCommand(commandCtx, command);
	}

	public CMSItem getContent(CMSServiceCtx cmsCtx, String path) throws CMSException {
		try {

			Document doc = (Document) executeNuxeoCommand(cmsCtx, (new DocumentFetchCommand(path)));
			return createItem(path, doc.getTitle(), doc);
		} catch (NuxeoException e) {
			e.rethrowCMSException();
		} catch (Exception e) {
			throw new CMSException(e);
		}

		// Not possible
		return null;
	}

	public CMSHandlerProperties getItemHandler(CMSServiceCtx ctx) throws CMSException {
		// Document doc = ctx.g
		try {
			return getNuxeoService().getLinkHandler().getLink(ctx);
		} catch (NuxeoException e) {
			e.rethrowCMSException();
		} catch (Exception e) {
			throw new CMSException(e);
		}

		// Not possible
		return null;
	}

	public CMSItem getPortalNavigationItem(CMSServiceCtx cmsCtx, String publishSpacePath, String path)
			throws CMSException {
		try {

			Map<String, NavigationItem> navItems = (Map<String, NavigationItem>) executeNuxeoCommand(cmsCtx,
					(new DocumentPublishSpaceNavigationCommand(publishSpacePath)));

			if (navItems != null) {
				NavigationItem navItem = navItems.get(path);
				if (navItem != null)
					return createItem(path, navItem.getMainDoc().getTitle(), navItem.getMainDoc());
			}
		} catch (NuxeoException e) {
			e.rethrowCMSException();
		} catch (Exception e) {
			throw new CMSException(e);
		}

		// Not possible
		return null;
	}

	public List<CMSItem> getPortalNavigationSubitems(CMSServiceCtx cmsCtx, String publishSpacePath, String path)
			throws CMSException {
		try {

			Map<String, NavigationItem> navItems = (Map<String, NavigationItem>) executeNuxeoCommand(cmsCtx,
					(new DocumentPublishSpaceNavigationCommand(publishSpacePath)));

			if (navItems != null) {
				NavigationItem navItem = navItems.get(path);
				if (navItem != null) {
					List<CMSItem> childrens = new ArrayList<CMSItem>();
					for (Document child : navItem.getChildren()) {
						childrens.add(createItem(child.getPath(), child.getTitle(), child));
					}
					return childrens;
				}
			}
		} catch (NuxeoException e) {
			e.rethrowCMSException();
		} catch (Exception e) {
			throw new CMSException(e);
		}

		// Not possible
		return null;
	}

	public CMSItem getPortalPublishSpace(CMSServiceCtx cmsCtx, String path) throws CMSException {
		try {

			Document publishSpace =  (Document) executeNuxeoCommand(cmsCtx,
					(new DocumentResolvePublishSpaceCommand(path)));

			if (publishSpace != null) {
				return createItem(publishSpace.getPath(), publishSpace.getTitle(), publishSpace);

			}
		} catch (NuxeoException e) {
			e.rethrowCMSException();
		} catch (Exception e) {
			throw new CMSException(e);
		}

		// Not possible
		return null;
	}

}
