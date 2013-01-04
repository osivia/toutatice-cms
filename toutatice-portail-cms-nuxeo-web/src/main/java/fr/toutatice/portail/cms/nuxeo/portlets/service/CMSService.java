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

			cmsCtx.setScope("superuser_context");

			if ("1".equals(cmsCtx.getDisplayLiveVersion()) || !pubInfos.isPublished()) {

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
	
	public CMSBinaryContent getBinaryContent(CMSServiceCtx cmsCtx, String type, String path, String parameter) throws CMSException {
		CMSBinaryContent content = new CMSBinaryContent();
		
		if("file".equals(type)){
			content = getFileContent(cmsCtx, path, parameter);
		}else if("attachedPicture".equals(type)){
			content = getAttachedPicture(cmsCtx, path, parameter);
		}else if("picture".equals(type)){
			content = getPicture(cmsCtx, path, parameter);
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
			/*CMSPublicationInfos infos = getPublicationInfos(cmsCtx, docPath);
			boolean isAnonymous = infos.isAnonymouslyReadable();*/
			
			/*
			 * On prend toujours la version publiée de l'image si elle existe;
			 * on ne prend donc pas en compte le displayLiveVersion.
			 */
			String displayLive = cmsCtx.getDisplayLiveVersion();
			cmsCtx.setDisplayLiveVersion("0");
			
			cmsContent = fetchPicture(cmsCtx, docPath, content/* , isAnonymous */);
			
			cmsCtx.setDisplayLiveVersion(displayLive);
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
	
	
	private CMSBinaryContent fetchPicture(CMSServiceCtx cmsCtx, String docPath, String content/*, boolean isAnonymous*/) throws Exception {
		CMSBinaryContent pictureContent = null;
		String savedScope = cmsCtx.getScope();
		try {
			// if(!isAnonymous){
			// getPublicationInfos(cmsCtx, docPath);
			// }
			CMSItem image = fetchContent(cmsCtx, docPath);
			if (image != null) {

				cmsCtx.setScope("superuser_context");

				pictureContent = (CMSBinaryContent) executeNuxeoCommand(cmsCtx, (new PictureContentCommand(
						(Document) image.getNativeItem(), content)));
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
			CMSItem doc = fetchContent(cmsCtx, docPath);
			if(doc != null){
			
			cmsCtx.setScope("superuser_context");

			content = (CMSBinaryContent) executeNuxeoCommand(cmsCtx, (new FileContentCommand((Document) doc.getNativeItem(),
					fieldName)));
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
			if (pubInfos.getPublishSpacePath() == null)
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
			if (pubInfos.getPublishSpacePath() == null)
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

				String savedScope = ctx.getScope();

				try {
					if((StringUtils.isEmpty(savedScope)) || ("__nocache".equals(savedScope))){
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
}
