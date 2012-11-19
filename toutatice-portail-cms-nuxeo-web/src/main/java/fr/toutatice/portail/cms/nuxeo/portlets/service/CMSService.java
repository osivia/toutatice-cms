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
import fr.toutatice.portail.cms.nuxeo.core.DocumentFetchCommand;
import fr.toutatice.portail.cms.nuxeo.core.DocumentFetchPublishedCommand;
import fr.toutatice.portail.cms.nuxeo.core.NuxeoCommandServiceFactory;
import fr.toutatice.portail.cms.nuxeo.jbossportal.NuxeoCommandContext;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.DefaultCMSCustomizer;
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

			Document doc = (Document) executeNuxeoCommand(cmsCtx, (new DocumentFetchCommand(path)));
			return createItem(cmsCtx, doc.getPath(), doc.getTitle(), doc);
		} else {

			Document doc = (Document) executeNuxeoCommand(cmsCtx, (new DocumentFetchPublishedCommand(path)));
			return createItem(cmsCtx, doc.getPath(), doc.getTitle(), doc);
		}

	}

	public CMSItem getContent(CMSServiceCtx cmsCtx, String path) throws CMSException {

		try {

			/*
			 * le cache FetchableContentInvoker permet de ne pas multiplier les
			 * appels sur un scope
			 * 
			 * 
			 * Si un appel sur un scope échoue, on passe en scope user
			 */

			CMSItem cmsItem;

			if (cmsCtx.getScope() != null && !"__nocache".equals(cmsCtx.getScope())) {

				String cacheId = "fetchableContentByScope/" + cmsCtx.getScope() + "/" + path;

				CacheInfo cacheInfos = new CacheInfo(cacheId, CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT, null,
						cmsCtx.getRequest(), portletCtx);

				Object fetchableWithScope = getCacheService().getCache(cacheInfos);
				if (fetchableWithScope == null || (((Boolean) fetchableWithScope) == true)) {

					try {
						// Lecture avec le scope
						cmsItem = fetchContent(cmsCtx, path);

						cacheInfos.setInvoker(new FetchableContentInvoker(true));
						cacheInfos.setForceReload(true);
						getCacheService().getCache(cacheInfos);

						return cmsItem;

					} catch (Exception e) {
						cacheInfos.setInvoker(new FetchableContentInvoker(false));
						cacheInfos.setForceReload(true);
						getCacheService().getCache(cacheInfos);
					}
				}
			}

			/*
			 * le chargement par scope n'a pas marché
			 * 
			 * on modifie le scope en scope user
			 */

			cmsCtx.setScope("__nocache");

			return fetchContent(cmsCtx, path);

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
			if (("1".equals(cmsCtx.getDisplayLiveVersion())) || !pubInfos.isPublished())
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
			if (("1".equals(cmsCtx.getDisplayLiveVersion())) || !pubInfos.isPublished())
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
			// TODO : prendre le scope de navigation
			CMSItem portalSpace = getPortalNavigationItem(cmsCtx, pubInfos.getPublishSpacePath(),
					pubInfos.getPublishSpacePath());
			return portalSpace;
		}

		if (pubInfos.getWorkspacePath() != null) {
			// TODO : prendre le scope de navigation
			CMSItem portalSpace = getPortalNavigationItem(cmsCtx, pubInfos.getWorkspacePath(),
					pubInfos.getWorkspacePath());
			return portalSpace;
		}

		throw new CMSException(CMSException.ERROR_NOTFOUND);

	}

	public CMSPublicationInfos getPublicationInfosTest(CMSServiceCtx ctx, String path) throws CMSException {
		try {
			CMSPublicationInfos pubInfos = (CMSPublicationInfos) ctx.getControllerContext().getAttribute(
					Scope.REQUEST_SCOPE, "pia.publicationInfos." + path);

			if (pubInfos == null) {

				pubInfos = new CMSPublicationInfos();

				pubInfos.setDocumentPath(path);

				if (path.startsWith("/default-domain/workspaces/seria")) {
					pubInfos.setWorkspaceDisplayName("SERIA");
					pubInfos.setWorkspacePath("/default-domain/workspaces/seria");
					pubInfos.setPublished(false);
					pubInfos.getErrorCodes().add(CMSPublicationInfos.ERROR_PUBLISH_SPACE_NOT_FOUND);
				} else {

					// A remplacer par l'opération
					CMSItem publishSpace = null;
					try {
						publishSpace = getPortalPublishSpace(ctx, path);
					} catch (CMSException e) {

					}

					if (publishSpace != null) {
						pubInfos.setPublishSpaceDisplayName(publishSpace.getProperties().get("displayName"));
						pubInfos.setPublishSpacePath(publishSpace.getPath());
						pubInfos.setPublished(true);
					}
				}

				ctx.getControllerContext().setAttribute(Scope.REQUEST_SCOPE, "pia.publicationInfos." + path, pubInfos);
			}

			return pubInfos;

		} catch (Exception e) {
			if (!(e instanceof CMSException))
				throw new CMSException(e);
			else
				throw (CMSException) e;
		}
	}

	public CMSPublicationInfos getPublicationInfos(CMSServiceCtx ctx, String docIdent) throws CMSException {
		CMSPublicationInfos pubInfos = null;

		try {
			pubInfos = (CMSPublicationInfos) ctx.getControllerContext().getAttribute(Scope.REQUEST_SCOPE,
					"pia.publicationInfos." + docIdent);

			if (pubInfos == null) {
				pubInfos = new CMSPublicationInfos();

				Map<String, Object> cmsInfos = (Map<String, Object>) executeNuxeoCommand(ctx, (new PublishInfosCommand(
						docIdent)));

				List<Integer> errors = (List<Integer>) cmsInfos.get("errors");
				if (errors != null) {
					if (errors.contains(CMSPublicationInfos.ERROR_CONTENT_FORBIDDEN)) {
						throw new CMSException(CMSException.ERROR_FORBIDDEN);
					}
					if (errors.contains(CMSPublicationInfos.ERROR_CONTENT_NOT_FOUND)) {
						throw new CMSException(CMSException.ERROR_NOTFOUND);
					}
				}
				pubInfos.setErrorCodes(errors);
				pubInfos.setDocumentPath((String) cmsInfos.get("documentPath"));
				pubInfos.setLiveId((String) cmsInfos.get("liveId"));
				pubInfos.setPublishSpaceDisplayName((String) cmsInfos.get("publishSpaceDisplayName"));
				pubInfos.setPublishSpaceInContextualization(((Boolean) cmsInfos.get("publishSpaceInContextualization"))
						.booleanValue());
				pubInfos.setPublishSpacePath((String) cmsInfos.get("publishSpacePath"));
				pubInfos.setWorkspaceDisplayName((String) cmsInfos.get("workspaceDisplayName"));
				pubInfos.setWorkspaceInContextualization(((Boolean) cmsInfos.get("workspaceInContextualization"))
						.booleanValue());
				pubInfos.setWorkspacePath((String) cmsInfos.get("workspacePath"));
				pubInfos.setEditableByUser((Boolean) cmsInfos.get("editableByUser"));
				pubInfos.setPublished(((Boolean) cmsInfos.get("published")).booleanValue());
				pubInfos.setAnonymouslyReadable(((Boolean) cmsInfos.get("anonymouslyReadable")).booleanValue());

				ctx.getControllerContext().setAttribute(Scope.REQUEST_SCOPE, "pia.publicationInfos." + docIdent,
						pubInfos);

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
