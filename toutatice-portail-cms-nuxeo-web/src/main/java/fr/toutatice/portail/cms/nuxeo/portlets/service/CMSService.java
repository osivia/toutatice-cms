package fr.toutatice.portail.cms.nuxeo.portlets.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletContext;

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
			nuxeoCommandService = (INuxeoCommandService) NuxeoCommandServiceFactory.getNuxeoCommandService(portletCtx);
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

	private CMSItem fetchContent(CMSServiceCtx cmsCtx, String path) throws Exception {

		if ("1".equals(cmsCtx.getDisplayLiveVersion())) {

			Document doc = (Document) executeNuxeoCommand(cmsCtx, (new DocumentFetchCommand(path)));
			return createItem(cmsCtx, doc.getPath(), doc.getTitle(), doc);
		} else {
			Document doc = (Document) executeNuxeoCommand(cmsCtx, (new DocumentFetchPublishedCommand(path)));
			return createItem(cmsCtx,  doc.getPath(), doc.getTitle(), doc);
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

		String cacheId = "anonymous_content/" + path;

		CacheInfo cacheInfos = new CacheInfo(cacheId, CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT, null, cmsCtx.getRequest(),
				portletCtx);

		try {

			Object anonymousCheck = getCacheService().getCache(cacheInfos);

			if (anonymousCheck != null) {
				return ((Boolean) anonymousCheck);
			}

			else {

				/* Perform check in anonymous mode */

				CMSServiceCtx checkAnonymousAccess = new CMSServiceCtx();
				checkAnonymousAccess.setControllerContext(cmsCtx.getControllerContext());
				checkAnonymousAccess.setScope("anonymous");

				cacheInfos.setForceReload(true);

				try {

					getContent(checkAnonymousAccess, path);
					cacheInfos.setInvoker(new AnonymousAccesInvoker(true));

				} catch (CMSException e) {
					if (e.getErrorCode() == CMSException.ERROR_FORBIDDEN)

						cacheInfos.setInvoker(new AnonymousAccesInvoker(false));
					else
						throw e;
				}

				return ((Boolean) getCacheService().getCache(cacheInfos));

			}
		} catch (Exception e) {
			if (!(e instanceof CMSException))
				throw new CMSException(e);
			else
				throw (CMSException) e;
		}

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

			Map<String, NavigationItem> navItems = (Map<String, NavigationItem>) executeNuxeoCommand(cmsCtx,
					(new DocumentPublishSpaceNavigationCommand(publishSpacePath)));

			if (navItems != null) {
				NavigationItem navItem = navItems.get(path);
				if (navItem != null) {

					CMSItem item = navItem.getAdaptedCMSItem();
					if (item == null) {
						if( navItem.getMainDoc() != null)
							navItem.setAdaptedCMSItem(createNavigationItem(cmsCtx, path, navItem.getMainDoc().getTitle(),
								navItem.getMainDoc(), publishSpacePath));
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

			Map<String, NavigationItem> navItems = (Map<String, NavigationItem>) executeNuxeoCommand(cmsCtx,
					(new DocumentPublishSpaceNavigationCommand(publishSpacePath)));

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

		try {

			/*
			 * le cache UnresolvablePublishSpaceInvoker permet de ne pas
			 * multiplier les appels pour un meme contenu tout en conservant un
			 * scope user
			 * 
			 * les memes objets inresolvables sont donc mutualisés entre les
			 * scopes et les users
			 */

			String cacheId = "resolvable/" + path;

			CacheInfo cacheInfos = new CacheInfo(cacheId, CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT, null,
					cmsCtx.getRequest(), portletCtx);

			Object resolvable = getCacheService().getCache(cacheInfos);
			if (resolvable != null) {
				// Déja non resolu
				if (((Boolean) resolvable) == false)
					throw new CMSException(CMSException.ERROR_NOTFOUND);
			}

			try {

				NavigationItem publishSpace = (NavigationItem) executeNuxeoCommand(cmsCtx,
						(new DocumentResolvePublishSpaceCommand(path)));

				if (publishSpace != null) {

					CMSItem item = publishSpace.getAdaptedCMSItem();

					if (item == null) {
						publishSpace.setAdaptedCMSItem(createNavigationItem(cmsCtx,
								publishSpace.getMainDoc().getPath(), publishSpace.getMainDoc().getTitle(),
								publishSpace.getMainDoc(), publishSpace.getMainDoc().getPath()));
					}

					// On met à jour le cache pour cet objet resovable

					cacheInfos.setForceReload(true);
					cacheInfos.setInvoker(new ResolvablePublishSpace(true));
					getCacheService().getCache(cacheInfos);

					return publishSpace.getAdaptedCMSItem();
				}
			} catch (NuxeoException e) {

				if (e.getErrorCode() == NuxeoException.ERROR_NOTFOUND) {

					// On met à jour le cache pour cet objet non resovable

					cacheInfos.setForceReload(true);
					cacheInfos.setInvoker(new ResolvablePublishSpace(false));
					getCacheService().getCache(cacheInfos);

				}

				e.rethrowCMSException();
			}
		} catch (Exception e) {
			if (!(e instanceof CMSException))
				throw new CMSException(e);
			else
				throw (CMSException) e;
		}

		// Not possible
		return null;
	}

}
