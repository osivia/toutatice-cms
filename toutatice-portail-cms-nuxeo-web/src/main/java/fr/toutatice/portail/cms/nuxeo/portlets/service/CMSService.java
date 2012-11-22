package fr.toutatice.portail.cms.nuxeo.portlets.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletContext;

import org.jboss.portal.common.invocation.Scope;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;

import fr.toutatice.portail.api.cache.services.CacheInfo;
import fr.toutatice.portail.api.cache.services.ICacheService;
import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommandService;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.core.DocumentFetchPublishedCommand;
import fr.toutatice.portail.cms.nuxeo.core.NuxeoCommandServiceFactory;
import fr.toutatice.portail.cms.nuxeo.jbossportal.NuxeoCommandContext;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.DefaultCMSCustomizer;
import fr.toutatice.portail.cms.nuxeo.portlets.document.DocumentFetchLiveCommand;
import fr.toutatice.portail.core.cms.CMSException;
import fr.toutatice.portail.core.cms.CMSHandlerProperties;
import fr.toutatice.portail.core.cms.CMSItem;
import fr.toutatice.portail.core.cms.CMSPublicationInfos;
import fr.toutatice.portail.core.cms.CMSServiceCtx;
import fr.toutatice.portail.core.cms.ICMSService;
import fr.toutatice.portail.core.cms.NavigationItem;
import fr.toutatice.portail.core.nuxeo.INuxeoService;
import fr.toutatice.portail.core.profils.IProfilManager;

public class CMSService implements ICMSService {

	private final PortletContext portletCtx;
	INuxeoCommandService nuxeoCommandService;
	INuxeoService nuxeoService;
	IProfilManager profilManager;
	ICacheService serviceCache;
	DefaultCMSCustomizer customizer;

	public CMSService(PortletContext portletCtx) {
		super();
		this.portletCtx = portletCtx;
	}

	public DefaultCMSCustomizer getCustomizer() {
		return customizer;
	}

	public void setCustomizer(DefaultCMSCustomizer customizer) {
		this.customizer = customizer;
	}

	public CMSItem createItem(CMSServiceCtx cmsCtx, String path, String displayName, Document doc) throws CMSException {

		Map<String, String> properties = new HashMap<String, String>();

		properties.put("displayName", displayName);
		properties.put("type", doc.getType());

		CMSItem cmsItem = new CMSItem(path, properties, doc);

		return cmsItem;
	}

	public CMSItem createNavigationItem(CMSServiceCtx cmsCtx, String path, String displayName, Document doc,
			String publishSpacePath) throws CMSException {

		CMSItem cmsItem = createItem(cmsCtx, path, displayName, doc);

		CMSItem publishSpaceItem = null;

		if (publishSpacePath != null && !path.equals(publishSpacePath))
			publishSpaceItem = getPortalNavigationItem(cmsCtx, publishSpacePath, publishSpacePath);
		else
			publishSpaceItem = cmsItem;

		getCustomizer().getNavigationItemAdaptor().adaptPublishSpaceNavigationItem(cmsItem, publishSpaceItem);

		return cmsItem;
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

	public ICacheService getCacheService() throws Exception {

		if (serviceCache == null)

			serviceCache = (ICacheService) portletCtx.getAttribute("CacheService");

		return serviceCache;
	}

	public INuxeoCommandService getNuxeoCommandService() throws Exception {
		if (nuxeoCommandService == null)
			nuxeoCommandService = NuxeoCommandServiceFactory.getNuxeoCommandService(portletCtx);
		return nuxeoCommandService;
	}

	public Object executeNuxeoCommand(CMSServiceCtx cmsCtx, INuxeoCommand command) throws Exception {

		NuxeoCommandContext commandCtx = new NuxeoCommandContext(portletCtx, cmsCtx.getServerInvocation());

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

				// commandCtx.setAsynchronousUpdates(true);

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

	private CMSItem fetchContent(CMSServiceCtx cmsCtx, String path) throws Exception {

		CMSPublicationInfos pubInfos = getPublicationInfos(cmsCtx, path);

		if ("1".equals(cmsCtx.getDisplayLiveVersion()) || !pubInfos.isPublished()) {

			Document doc = (Document) executeNuxeoCommand(cmsCtx, (new DocumentFetchLiveCommand(path, "Read")));
			return createItem(cmsCtx, doc.getPath(), doc.getTitle(), doc);
		} else {

			Document doc = (Document) executeNuxeoCommand(cmsCtx, (new DocumentFetchPublishedCommand(path)));
			return createItem(cmsCtx, doc.getPath(), doc.getTitle(), doc);
		}

	}

	public CMSItem getContent(CMSServiceCtx cmsCtx, String path) throws CMSException {

		try {
			CMSItem cmsItem = (CMSItem) cmsCtx.getControllerContext().getAttribute(Scope.REQUEST_SCOPE,
					"pia.content." + path);

			if (cmsItem == null) {

				cmsItem = fetchContent(cmsCtx, path);

				cmsCtx.getControllerContext().setAttribute(Scope.REQUEST_SCOPE, "pia.content." + path, cmsItem);
			}

			return cmsItem;

		} catch (NuxeoException e) {
			e.rethrowCMSException();
		} catch (Exception e) {
			if (!(e instanceof CMSException))
				throw new CMSException(e);
			else
				throw (CMSException) e;
		}

		return null;

	}

	public boolean checkContentAnonymousAccess(CMSServiceCtx cmsCtx, String path) throws CMSException {

		try {
			CMSPublicationInfos pubInfos = getPublicationInfos(cmsCtx, path);

			return pubInfos.isAnonymouslyReadable();
		} catch (NuxeoException e) {
			e.rethrowCMSException();
		} catch (Exception e) {
			if (!(e instanceof CMSException))
				throw new CMSException(e);
			else
				throw (CMSException) e;
		}

		// Ne passe jamamis
		return false;
	}

	public CMSHandlerProperties getItemHandler(CMSServiceCtx ctx) throws CMSException {
		// Document doc = ctx.g
		try {
			return getNuxeoService().getCMSCustomizer().getCMSPlayer(ctx);
		} catch (NuxeoException e) {
			e.rethrowCMSException();
		} catch (Exception e) {
			if (!(e instanceof CMSException))
				throw new CMSException(e);
			else
				throw (CMSException) e;
		}

		// Not possible
		return null;
	}

	public CMSItem getPortalNavigationItem(CMSServiceCtx cmsCtx, String publishSpacePath, String path)
			throws CMSException {
		try {

			CMSPublicationInfos pubInfos = getPublicationInfos(cmsCtx, path);

			boolean live = false;
			if (pubInfos.getPublishSpacePath() == null)
				live = true;

			Map<String, NavigationItem> navItems = (Map<String, NavigationItem>) executeNuxeoCommand(cmsCtx,
					(new DocumentPublishSpaceNavigationCommand(publishSpacePath, live)));

			if (navItems != null) {
				NavigationItem navItem = navItems.get(path);
				if (navItem != null) {

					CMSItem item = navItem.getAdaptedCMSItem();
					if (item == null) {
						if (navItem.getMainDoc() != null)
							navItem.setAdaptedCMSItem(createNavigationItem(cmsCtx, path, navItem.getMainDoc()
									.getTitle(), navItem.getMainDoc(), publishSpacePath));
						else
							return null;
					}

					return navItem.getAdaptedCMSItem();
				}

			}

		} catch (NuxeoException e) {
			e.rethrowCMSException();
		} catch (Exception e) {
			if (!(e instanceof CMSException))
				throw new CMSException(e);
			else
				throw (CMSException) e;
		}

		// Not possible
		return null;
	}

	public List<CMSItem> getPortalNavigationSubitems(CMSServiceCtx cmsCtx, String publishSpacePath, String path)
			throws CMSException {
		try {

			CMSPublicationInfos pubInfos = getPublicationInfos(cmsCtx, path);

			boolean live = false;
			if (pubInfos.getPublishSpacePath() == null)
				live = true;

			Map<String, NavigationItem> navItems = (Map<String, NavigationItem>) executeNuxeoCommand(cmsCtx,
					(new DocumentPublishSpaceNavigationCommand(publishSpacePath, live)));

			if (navItems != null) {
				NavigationItem navItem = navItems.get(path);
				if (navItem != null) {
					List<CMSItem> childrens = new ArrayList<CMSItem>();
					for (Document child : navItem.getChildren()) {

						childrens.add(getPortalNavigationItem(cmsCtx, publishSpacePath, child.getPath()));

					}
					return childrens;
				}
			}

		} catch (NuxeoException e) {
			e.rethrowCMSException();
		} catch (Exception e) {
			if (!(e instanceof CMSException))
				throw new CMSException(e);
			else
				throw (CMSException) e;
		}

		// Not possible
		return null;
	}

	public CMSItem getPortalPublishSpace(CMSServiceCtx cmsCtx, String path) throws CMSException {

		CMSPublicationInfos pubInfos = getPublicationInfos(cmsCtx, path);

		if (pubInfos.getPublishSpacePath() != null) {
			CMSItem portalSpace = getPortalNavigationItem(cmsCtx, pubInfos.getPublishSpacePath(),
					pubInfos.getPublishSpacePath());
			return portalSpace;
		}

		if (pubInfos.getWorkspacePath() != null) {
			CMSItem portalSpace = getPortalNavigationItem(cmsCtx, pubInfos.getWorkspacePath(),
					pubInfos.getWorkspacePath());
			return portalSpace;
		}

		throw new CMSException(CMSException.ERROR_NOTFOUND);

	}

	public CMSPublicationInfos getPublicationInfos(CMSServiceCtx ctx, String path) throws CMSException {
		/* Instanciation pour que la méthode soit techniquement "null safe" */
		CMSPublicationInfos pubInfos = new CMSPublicationInfos();

		try {
			pubInfos = (CMSPublicationInfos) ctx.getControllerContext().getAttribute(Scope.REQUEST_SCOPE,
					"pia.publicationInfos." + path);

			if (pubInfos == null) {

				String savedScope = ctx.getScope();

				try {
					ctx.setScope(null);

					pubInfos = (CMSPublicationInfos) executeNuxeoCommand(ctx, (new PublishInfosCommand(path)));

					if (pubInfos != null) {
						List<Integer> errors = pubInfos.getErrorCodes();
						if (errors != null) {
							if (errors.contains(CMSPublicationInfos.ERROR_CONTENT_FORBIDDEN)) {
								throw new CMSException(CMSException.ERROR_FORBIDDEN);
							}
							if (errors.contains(CMSPublicationInfos.ERROR_CONTENT_NOT_FOUND)) {
								throw new CMSException(CMSException.ERROR_NOTFOUND);
							}
						}

						ctx.getControllerContext().setAttribute(Scope.REQUEST_SCOPE, "pia.publicationInfos." + path,
								pubInfos);
					}
				} finally {
					ctx.setScope(savedScope);
				}

			}

		} catch (Exception e) {
			if (!(e instanceof CMSException))
				throw new CMSException(e);
			else
				throw (CMSException) e;
		}

		return pubInfos;

	}

}
