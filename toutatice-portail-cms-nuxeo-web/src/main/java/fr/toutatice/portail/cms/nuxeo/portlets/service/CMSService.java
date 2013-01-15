package fr.toutatice.portail.cms.nuxeo.portlets.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletContext;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
import org.osivia.portal.api.cache.services.CacheInfo;
import org.osivia.portal.api.cache.services.ICacheService;
import org.osivia.portal.core.cms.CMSBinaryContent;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSHandlerProperties;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.ICMSService;
import org.osivia.portal.core.cms.NavigationItem;
import org.osivia.portal.core.profils.IProfilManager;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommandService;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.core.DocumentFetchPublishedCommand;
import fr.toutatice.portail.cms.nuxeo.core.NuxeoCommandServiceFactory;
import fr.toutatice.portail.cms.nuxeo.jbossportal.NuxeoCommandContext;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.DefaultCMSCustomizer;
import fr.toutatice.portail.cms.nuxeo.portlets.document.DocumentFetchLiveCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.document.FileContentCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.document.InternalPictureCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.document.PictureContentCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.service.AnonymousAccesInvoker.AccesStatus;
import fr.toutatice.portail.core.nuxeo.INuxeoService;

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
		commandCtx.setCacheType(CacheInfo.CACHE_SCOPE_NONE);

		if (scope != null) {
			if (!"__nocache".equals(scope)) {

				// commandCtx.setAsynchronousUpdates(true);
				
				
				if ("user_session".equals(scope))	{
					commandCtx.setAuthType(NuxeoCommandContext.AUTH_TYPE_USER);
					commandCtx.setCacheType(CacheInfo.CACHE_SCOPE_PORTLET_SESSION);
				} else {
					if ("anonymous".equals(scope)) {
						commandCtx.setAuthType(NuxeoCommandContext.AUTH_TYPE_ANONYMOUS);
						commandCtx.setCacheType(CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);
					} else if("superuser_context".equals(scope)){
						commandCtx.setAuthType(NuxeoCommandContext.AUTH_TYPE_SUPERUSER);
						commandCtx.setCacheType(CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);
					}else{
						commandCtx.setAuthType(NuxeoCommandContext.AUTH_TYPE_PROFIL);
						commandCtx.setAuthProfil(getProfilManager().getProfil(scope));
						commandCtx.setCacheType(CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);
					}
				}
			}
		}

		return getNuxeoCommandService().executeCommand(commandCtx, command);
	}

	private CMSItem fetchContent(CMSServiceCtx cmsCtx, String path) throws Exception {

		String savedScope = cmsCtx.getScope();
		try {
			CMSPublicationInfos pubInfos = getPublicationInfos(cmsCtx, path);
			
			boolean haveToGetLive = "1".equals(cmsCtx.getDisplayLiveVersion())
					|| (!pubInfos.isPublished() && StringUtils.isNotEmpty(pubInfos.getPublishSpacePath()) && pubInfos
							.isLiveSpace());

			cmsCtx.setScope("superuser_context");

			if (haveToGetLive) {

				Document doc = (Document) executeNuxeoCommand(cmsCtx, (new DocumentFetchLiveCommand(path, "Read")));
				return createItem(cmsCtx, doc.getPath(), doc.getTitle(), doc);

			} else {

				Document doc = (Document) executeNuxeoCommand(cmsCtx, (new DocumentFetchPublishedCommand(path)));
				return createItem(cmsCtx, doc.getPath(), doc.getTitle(), doc);

			}
		} finally {
			cmsCtx.setScope(savedScope);
		}

	}

	public CMSItem getContent(CMSServiceCtx cmsCtx, String path) throws CMSException {
		CMSItem content = null;
		try {		

			content =  fetchContent(cmsCtx, path);			

		} catch (NuxeoException e) {
			e.rethrowCMSException();
		} catch (Exception e) {
			if (!(e instanceof CMSException))
				throw new CMSException(e);
			else
				throw (CMSException) e;
		}
		return content;
	}
	
	public CMSBinaryContent getBinaryContent(CMSServiceCtx cmsCtx, String type, String docPath, String parameter) throws CMSException {
		CMSBinaryContent content = new CMSBinaryContent();
		
		if("file".equals(type)){
			content = getFileContent(cmsCtx, docPath, parameter);
		}else if("attachedPicture".equals(type)){
			content = getAttachedPicture(cmsCtx, docPath, parameter);
		}else if("picture".equals(type)){
			content = getPicture(cmsCtx, docPath, parameter);
		}
		
		return content;
	}
	
	public CMSBinaryContent getAttachedPicture(CMSServiceCtx cmsCtx, String docPath, String pictureIndex) throws CMSException{
		CMSBinaryContent cmsContent = null;
		try {

			cmsContent = fetchAttachedPicture(cmsCtx, docPath, pictureIndex);
			
		} catch (NuxeoException e) {
			e.rethrowCMSException();
		} catch (Exception e) {
			if (!(e instanceof CMSException))
				throw new CMSException(e);
			else
				throw (CMSException) e;
		}
		
		return cmsContent;
	}

	private CMSBinaryContent fetchAttachedPicture(CMSServiceCtx cmsCtx, String docPath, String pictureIndex) throws Exception {
		CMSBinaryContent pictureContent = null;
		String savedScope = cmsCtx.getScope();
		try {		
			 CMSItem containerDoc = fetchContent(cmsCtx, docPath);
			
			if (containerDoc != null) {
				cmsCtx.setScope("superuser_context");

				pictureContent = (CMSBinaryContent) executeNuxeoCommand(cmsCtx, (new InternalPictureCommand(
						(Document) containerDoc.getNativeItem(), pictureIndex)));
			}

		} finally {
			cmsCtx.setScope(savedScope);
		}
		return pictureContent;
	}
	
	public CMSBinaryContent getPicture(CMSServiceCtx cmsCtx, String docPath, String content) throws CMSException{
		CMSBinaryContent cmsContent = null;

		try {		
			
			cmsContent = fetchPicture(cmsCtx, docPath, content);
			
		} catch (NuxeoException e) {
			e.rethrowCMSException();
		} catch (Exception e) {
			if (!(e instanceof CMSException))
				throw new CMSException(e);
			else
				throw (CMSException) e;
		}
		
		return cmsContent;
	}
	
	
	private CMSBinaryContent fetchPicture(CMSServiceCtx cmsCtx, String docPath, String content) throws Exception {
		CMSBinaryContent pictureContent = null;
		String savedScope = cmsCtx.getScope();
		try {		
			CMSItem picture = getAnonymousContent(cmsCtx, docPath);
			
			if(picture == null){
				picture = fetchContent(cmsCtx, docPath);
			}			
			if (picture != null) {
				cmsCtx.setScope("superuser_context");
				
				pictureContent = (CMSBinaryContent) executeNuxeoCommand(cmsCtx, (new PictureContentCommand(
						(Document) picture.getNativeItem(), content)));
			}

		} finally {
			cmsCtx.setScope(savedScope);
		}
		return pictureContent;
	}
	
	public CMSBinaryContent getFileContent(CMSServiceCtx cmsCtx, String docPath, String fieldName) throws CMSException{
		CMSBinaryContent cmsContent = null;
		try {

			cmsContent = fetchFileContent(cmsCtx, docPath, fieldName);

		} catch (NuxeoException e) {
			e.rethrowCMSException();
		} catch (Exception e) {
			if (!(e instanceof CMSException))
				throw new CMSException(e);
			else
				throw (CMSException) e;
		}
		
		return cmsContent;
	}
	
	private CMSBinaryContent fetchFileContent(CMSServiceCtx cmsCtx, String docPath, String fieldName) throws Exception {
		CMSBinaryContent content = null;
		String savedScope = cmsCtx.getScope();
		try {

			CMSItem	document = fetchContent(cmsCtx, docPath);
			
			if (document != null) {

				cmsCtx.setScope("superuser_context");

				content = (CMSBinaryContent) executeNuxeoCommand(cmsCtx,
						(new FileContentCommand((Document) document.getNativeItem(), fieldName)));
			}
		} finally {
			cmsCtx.setScope(savedScope);
		}
		return content;
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
	
	public CMSItem getAnonymousContent(CMSServiceCtx cmsCtx, String path) throws CMSException {

		CMSItem anonymousContent = null;
		String savedScope = cmsCtx.getScope();
		String cacheId = "anonymous_content/" + path;
		
		CacheInfo cacheInfos = new CacheInfo(cacheId, CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT, null, cmsCtx.getRequest(),
				portletCtx);
		CMSServiceCtx checkAnonymousAccess = new CMSServiceCtx();
		checkAnonymousAccess.setControllerContext(cmsCtx.getControllerContext());
		checkAnonymousAccess.setScope("anonymous");
		
		NuxeoCommandContext commandCtx = new NuxeoCommandContext(portletCtx, cmsCtx.getServerInvocation());
		if (commandCtx.getCacheTimeOut() == -1) {
			if (System.getProperty("nuxeo.cacheTimeout") != null)
				cacheInfos.setDelaiExpiration(Long.parseLong(System.getProperty("nuxeo.cacheTimeout")) * 1000);
			else
				cacheInfos.setDelaiExpiration(0L);
		} else
			cacheInfos.setDelaiExpiration(commandCtx.getCacheTimeOut());

		try {

			Object anonymousCheck = getCacheService().getCache(cacheInfos);

			if (anonymousCheck != null) {
				AccesStatus anonymousCheckStatus = (AccesStatus) anonymousCheck;
				if (!anonymousCheckStatus.isAccess()) {
					if (anonymousCheckStatus.getStatus() == AnonymousAccesInvoker.NOT_FOUND) {
						throw new CMSException(CMSException.ERROR_NOTFOUND);
					}
				}else{
					/* Le contenu est accessible: aucune erreur n'est possible. */
					anonymousContent = getContent(checkAnonymousAccess, path);
				}
			} else {

				try {
					cacheInfos.setForceReload(true);

					anonymousContent = getContent(checkAnonymousAccess, path);
					
					cacheInfos.setInvoker(new AnonymousAccesInvoker(true, AnonymousAccesInvoker.AUTHORIZED));
					getCacheService().getCache(cacheInfos);

				} catch (CMSException e) {
					if (e.getErrorCode() == CMSException.ERROR_FORBIDDEN) {

						cacheInfos.setInvoker(new AnonymousAccesInvoker(false, AnonymousAccesInvoker.FORBIDDEN));
						getCacheService().getCache(cacheInfos);

					} else if (e.getErrorCode() == CMSException.ERROR_NOTFOUND) {

						cacheInfos.setInvoker(new AnonymousAccesInvoker(false, AnonymousAccesInvoker.NOT_FOUND));
						getCacheService().getCache(cacheInfos);
						throw new CMSException(CMSException.ERROR_NOTFOUND);

					} else
						throw e;
				}

			}
		} catch (Exception e) {
			if (!(e instanceof CMSException))
				throw new CMSException(e);
			else
				throw (CMSException) e;
		} finally {
			cmsCtx.setScope(savedScope);
		}

		return anonymousContent;

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
		
		String savedScope = cmsCtx.getScope();
		
		if( cmsCtx.getScope() == null || "__nocache".equals(cmsCtx.getScope()) )	{
			cmsCtx.setScope("user_session");
		}
		
		try {
			// TODO : optimiser l'appel pubInfos (pas d'appel pour connaitre les proprietes du publishSpace)
			// Attention, peut être appelé à de multiples reprises pour une requete (cas du menu de publication)
			CMSPublicationInfos pubInfos = getPublicationInfos(cmsCtx, publishSpacePath);
			String livePath = DocumentPublishSpaceNavigationCommand.computeNavPath(path);
			boolean live = false;
			if ((pubInfos.getPublishSpacePath() != null) && (pubInfos.isLiveSpace()))
				live = true;
			
			

			Map<String, NavigationItem> navItems = (Map<String, NavigationItem>) executeNuxeoCommand(cmsCtx,
					(new DocumentPublishSpaceNavigationCommand(publishSpacePath, live)));

			if (navItems != null) {
				NavigationItem navItem = navItems.get(livePath);
				if (navItem != null) {

					CMSItem item = navItem.getAdaptedCMSItem();
					if (item == null) {
						if (navItem.getMainDoc() != null)
							navItem.setAdaptedCMSItem(createNavigationItem(cmsCtx, livePath, ((Document) navItem.getMainDoc())
									.getTitle(), (Document) navItem.getMainDoc(), publishSpacePath));
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
		finally	{
			cmsCtx.setScope(savedScope);
		}

		// Not possible
		return null;
	}

	public List<CMSItem> getPortalNavigationSubitems(CMSServiceCtx cmsCtx, String publishSpacePath, String path)
			throws CMSException {
		
		String savedScope = cmsCtx.getScope();
		
		if( cmsCtx.getScope() == null || "__nocache".equals(cmsCtx.getScope()) )	{
			cmsCtx.setScope("user_session");
		}
		try {
			// TODO : optimiser l'appel pubInfos (pas d'appel pour connaitre les proprietes du publishSpace)
			// Attention, peut être appelé à de multiples reprises pour une requete (cas du menu de publication)			
			CMSPublicationInfos pubInfos = getPublicationInfos(cmsCtx, publishSpacePath);
			
			boolean live = false;
			if ((pubInfos.getPublishSpacePath() != null) && (pubInfos.isLiveSpace()))
				live = true;

			Map<String, NavigationItem> navItems = (Map<String, NavigationItem>) executeNuxeoCommand(cmsCtx,
					(new DocumentPublishSpaceNavigationCommand(publishSpacePath, live)));

			if (navItems != null) {
				NavigationItem navItem = navItems.get(path);
				if (navItem != null) {
					List<CMSItem> childrens = new ArrayList<CMSItem>();
					for (Object child : navItem.getChildren()) {

						childrens.add(getPortalNavigationItem(cmsCtx, publishSpacePath, ((Document)child).getPath()));

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
		finally	{
			cmsCtx.setScope(savedScope);
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

		/* TOCHECK
		  if (pubInfos.getWorkspacePath() != null) {
			CMSItem portalSpace = getPortalNavigationItem(cmsCtx, pubInfos.getWorkspacePath(),
					pubInfos.getWorkspacePath());
			return portalSpace;
		}*/

		throw new CMSException(CMSException.ERROR_NOTFOUND);

	}

		public CMSPublicationInfos getPublicationInfos(CMSServiceCtx ctx, String path) throws CMSException {
		/* Instanciation pour que la méthode soit techniquement "null safe" */
		CMSPublicationInfos pubInfos = new CMSPublicationInfos();

		try {

				String savedScope = ctx.getScope();

				try {
					if((StringUtils.isEmpty(savedScope)) || ("__nocache".equals(savedScope)) 
							|| (!"anonymous".equals(savedScope))){
						ctx.setScope("user_session");
					}

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

					}
				}
				 finally {
					ctx.setScope(savedScope);
				}

		} catch (Exception e) {
			if (!(e instanceof CMSException))
				throw new CMSException(e);
			else
				throw (CMSException) e;
		}

		return pubInfos;

	}
		
	public CMSItem getPublicationConfig(CMSServiceCtx cmsCtx, String publishSpacePath) throws CMSException {
		CMSItem configItem = null;
		try {
			String savedScope = cmsCtx.getScope();
			try {
				cmsCtx.setScope("superuser_context");
				configItem = (CMSItem) executeNuxeoCommand(cmsCtx, (new PublishConfigCommand(this, cmsCtx, publishSpacePath)));
			}
			finally {
				cmsCtx.setScope(savedScope);
			}

		} catch (Exception e) {
			if (!(e instanceof CMSException))
				throw new CMSException(e);
			else
				throw (CMSException) e;
		}
		return configItem;
	}
}
