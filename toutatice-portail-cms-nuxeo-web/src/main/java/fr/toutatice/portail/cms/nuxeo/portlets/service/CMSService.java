package fr.toutatice.portail.cms.nuxeo.portlets.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
import org.nuxeo.ecm.automation.client.jaxrs.model.PropertyList;
import org.osivia.portal.api.cache.services.CacheInfo;
import org.osivia.portal.api.cache.services.ICacheService;
import org.osivia.portal.core.cms.CMSBinaryContent;
import org.osivia.portal.core.cms.CMSEditableWindow;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSHandlerProperties;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSObjectPath;
import org.osivia.portal.core.cms.CMSPage;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.EcmCommand;
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
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.DocumentRemovePropertyCommand;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.DocumentUpdatePropertiesCommand;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.EditableWindowService;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.EditableWindowTypeEnum;
import fr.toutatice.portail.core.nuxeo.INuxeoService;

public class CMSService implements ICMSService {

	protected static final Log logger = LogFactory.getLog(CMSService.class);
	
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

		getCustomizer().getNavigationItemAdapter().adaptPublishSpaceNavigationItem(cmsItem, publishSpaceItem);

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
		/* Transmission du mode asynchrone ou non de la mise en cache 
		 * du résultat de la commande.
		 */
		commandCtx.setAsyncCacheRefreshing(cmsCtx.isAsyncCacheRefreshing());
		
		if( cmsCtx.isForceReload())	{
			commandCtx.setForceReload(true);
		}

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
			boolean saveAsync = cmsCtx.isAsyncCacheRefreshing();
			
			cmsCtx.setAsyncCacheRefreshing(false);
			CMSPublicationInfos pubInfos = getPublicationInfos(cmsCtx, path);
			cmsCtx.setAsyncCacheRefreshing(saveAsync);
			
			boolean haveToGetLive = "1".equals(cmsCtx.getDisplayLiveVersion());
			
			// Document non publié et rattaché à un workspace
			if( (!pubInfos.isPublished() && StringUtils.isNotEmpty(pubInfos.getPublishSpacePath()) && pubInfos
							.isLiveSpace()))
					haveToGetLive = true;
			
			//Ajout JSS 20130122 
			//Document non publié et non rattaché à un espace : usage collaboratif
			if( !pubInfos.isPublished() && pubInfos.getPublishSpacePath() == null)
					haveToGetLive = true;
			


			cmsCtx.setScope("superuser_context");

			if (haveToGetLive) {

				Document doc = (Document) executeNuxeoCommand(cmsCtx, (new DocumentFetchLiveCommand(path, "Read")));
				return createItem(cmsCtx, path, doc.getTitle(), doc);

			} else {

				Document doc = (Document) executeNuxeoCommand(cmsCtx, (new DocumentFetchPublishedCommand(path)));
				

				
				return createItem(cmsCtx, path, doc.getTitle(), doc);

			}
		} finally {
			cmsCtx.setScope(savedScope);
		}

	}
	
	

	public CMSItem getContent(CMSServiceCtx cmsCtx, String path) throws CMSException {
		
		
		CMSItem content = null;
		try {		

			content =  fetchContent(cmsCtx, path);		
			
			getCustomizer().getCMSItemAdapter().adaptItem(cmsCtx, content);

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
			/*
			 * On tente de récupérer l'image "Nuxeo" en mode anonyme;
			 * getAnonymousContent retourne null sil'image n'est pas accessible
			 * en mode anonyme, i.e. si elle n'est pas publique.
			 */
			CMSItem picture = getAnonymousContent(cmsCtx, docPath);

			if (picture == null) {
				/* On tente alors de récupérer l'image avec les droits de l'utilisateur. */
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
			/* Si un scope a été posé dans la portlet appelant la resource,
			 * on applique celui-ci.
			 */
			if(StringUtils.isNotEmpty(savedScope)){
				cmsCtx.setForcePublicationInfosScope(savedScope);
			}
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
		
		/* Informations du cache pour l'accès en mode anonyme" */
		CacheInfo cacheInfos = new CacheInfo(cacheId, CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT, null, cmsCtx.getRequest(),
				portletCtx, cmsCtx.isAsyncCacheRefreshing());
		CMSServiceCtx checkAnonymousAccess = new CMSServiceCtx();
		checkAnonymousAccess.setControllerContext(cmsCtx.getControllerContext());
		checkAnonymousAccess.setForcePublicationInfosScope("anonymous");
		
		/* Affectation du délai d'expiration des caches. */
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
			} else {/* Mise en cache. */

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
			cmsCtx.setForcePublicationInfosScope(null);
		}

		return anonymousContent;

	}

	public CMSHandlerProperties getItemHandler(CMSServiceCtx ctx) throws CMSException {
		// Document doc = ctx.g
		try {
			if( !"detailedView".equals(ctx.getDisplayContext()))
				return getNuxeoService().getCMSCustomizer().getCMSPlayer(ctx);
			else
				return ((DefaultCMSCustomizer) getNuxeoService().getCMSCustomizer()).getCMSDefaultPlayer(ctx);
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

	
	
	
	
	
	public Map<String, NavigationItem> loadPartialNavigationTree(CMSServiceCtx cmsCtx, CMSItem publishSpaceConfig, String path, boolean fetchSubItems) throws CMSException{
		
		String savedScope = cmsCtx.getScope();
		
		try {

			
			Map<String, NavigationItem> navItems = null;
			
			List<String> idsToFetch = new ArrayList<String>();
			boolean fetchRoot = false;
			
			/* On récupère le dernier arbre de publication partiel */


			String cacheId = "partial_navigation_tree/" + publishSpaceConfig.getPath();
			Object request = cmsCtx.getServerInvocation().getServerContext().getClientRequest();
			CacheInfo cacheInfos = new CacheInfo(cacheId, CacheInfo.CACHE_SCOPE_PORTLET_SESSION, null, request, portletCtx,
					false);
			// délai d'une session
			cacheInfos.setDelaiExpiration(200000);


			navItems = (Map<String, NavigationItem>) getCacheService().getCache(cacheInfos);



			if (navItems == null) {

				navItems = new HashMap<String, NavigationItem>();
				fetchRoot = true;
			}

			/* Boucle sur l'arbo pour recuperer les ids à fetcher
			 * (doc absents de l'arbre)
			 * */

			String pathToCheck = path;
			
			CMSServiceCtx superUserCtx = new CMSServiceCtx();
			superUserCtx.setControllerContext(cmsCtx.getControllerContext());
			cmsCtx.setScope("superuser_context");
			
			do {
				NavigationItem navItem = navItems.get(pathToCheck);
				if (navItem == null || (fetchSubItems && navItem.isUnfetchedChildren() )) {
					Document doc = (Document) executeNuxeoCommand(cmsCtx, (new DocumentFetchLiveCommand(pathToCheck, "Read")));

					idsToFetch.add(doc.getId());
				}

				CMSObjectPath parentPath = CMSObjectPath.parse(pathToCheck).getParent();
				pathToCheck = parentPath.toString();

			} while (pathToCheck.contains(publishSpaceConfig.getPath()));



			
			if( idsToFetch.size() > 0 || fetchRoot)

			{
				cmsCtx.setScope("__nocache");

				/* appel de la commande */

				navItems = (Map<String, NavigationItem>) executeNuxeoCommand(cmsCtx, (new PartialNavigationCommand(publishSpaceConfig,
						navItems, idsToFetch, fetchRoot, path)));

				/* Stockage de l'arbre partiel */

				cacheInfos.setForceReload(true);
				cacheInfos.setForceNOTReload(false);
				cacheInfos.setInvoker(new PartialNavigationInvoker(navItems));
				getCacheService().getCache(cacheInfos);

			}

			return navItems;
		} catch (Exception e) {
			if (!(e instanceof CMSException))
				throw new CMSException(e);
			else
				throw (CMSException) e;
		}		finally	{
			cmsCtx.setScope(savedScope);
		}
	}
	
	
	public CMSItem getPortalNavigationItem(CMSServiceCtx cmsCtx, String publishSpacePath, String path)
			throws CMSException {
		
		String savedScope = cmsCtx.getScope();
		
		if( cmsCtx.getScope() == null || "__nocache".equals(cmsCtx.getScope()) )	{
			cmsCtx.setScope("user_session");
		}
		
		try {

			String livePath = DocumentPublishSpaceNavigationCommand.computeNavPath(path);
			
			CMSItem publishSpaceConfig = getSpaceConfig(cmsCtx, publishSpacePath);
			
			if( publishSpaceConfig == null)
				throw new CMSException(CMSException.ERROR_NOTFOUND);
			
			
			
			Map<String, NavigationItem> navItems = null;
			
			if( "1".equals(publishSpaceConfig.getProperties().get("partialLoading")))	{
				navItems = loadPartialNavigationTree(cmsCtx, publishSpaceConfig, path, false);
			}	else   {
			    boolean forceLiveVersion = false;
                if ("1".equals(cmsCtx.getDisplayLiveVersion()))
			            forceLiveVersion = true;  
				navItems = (Map<String, NavigationItem>) executeNuxeoCommand(cmsCtx,
					(new DocumentPublishSpaceNavigationCommand(publishSpaceConfig, forceLiveVersion)));
			}

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
			
			CMSItem publishSpaceConfig = getSpaceConfig(cmsCtx, publishSpacePath);
			
			if( publishSpaceConfig == null)
				throw new CMSException(CMSException.ERROR_NOTFOUND);
			

			Map<String, NavigationItem> navItems = null;
			
			if( "1".equals(publishSpaceConfig.getProperties().get("partialLoading")))	
				navItems = loadPartialNavigationTree(cmsCtx, publishSpaceConfig, path, true);
            else {
                boolean forceLiveVersion = false;
                if ("1".equals(cmsCtx.getDisplayLiveVersion()))
                    forceLiveVersion = true;

				navItems = (Map<String, NavigationItem>) executeNuxeoCommand(cmsCtx,
 (new DocumentPublishSpaceNavigationCommand(publishSpaceConfig,
                        forceLiveVersion)));
            }

			if (navItems != null) {
				NavigationItem navItem = navItems.get(path);
				if (navItem != null) {
					List<CMSItem> childrens = new ArrayList<CMSItem>();
				
					
					for (Object child : navItem.getChildren()) {
						
						Document docChild = (Document) child;
						
						String childNavPath = DocumentPublishSpaceNavigationCommand.computeNavPath(docChild.getPath());
						
						NavigationItem navChild =  navItems.get( childNavPath);
						
						CMSItem item = navChild.getAdaptedCMSItem();
						if (item == null) {
							if (navChild.getMainDoc() != null)
								navChild.setAdaptedCMSItem(createNavigationItem(cmsCtx, childNavPath, ((Document) navChild.getMainDoc())
										.getTitle(), (Document) navChild.getMainDoc(), publishSpacePath));
						}
						
						childrens.add(navChild.getAdaptedCMSItem());

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

	
	/* (non-Javadoc)
	 * @see org.osivia.portal.core.cms.ICMSService#getPublicationInfos(org.osivia.portal.core.cms.CMSServiceCtx, java.lang.String)
	 */
	public CMSPublicationInfos getPublicationInfos(CMSServiceCtx ctx, String path) throws CMSException {
		/* Instanciation pour que la méthode soit techniquement "null safe" */
		CMSPublicationInfos pubInfos = new CMSPublicationInfos();

		try {

			String savedScope = ctx.getScope();

			try {/*
				 * getPublicationInfos est toujours utilisé avec les droits de
				 * l'utilisateur (il remplit en ce sens un testeur de droits car
				 * les informations retournées sont faites selon ces derniers).
				 * Cependant, il est possible de forcer son exécution avec
				 * un autre modepar l'intermédiaire d'une vairiable du CMS Service
				 * Context (cas des méthodes getAnonymousContent(), getAttachedPicture()).
				 */
				if (StringUtils.isNotEmpty(ctx.getForcePublicationInfosScope())) {
					ctx.setScope(ctx.getForcePublicationInfosScope());
				}else{
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
			} finally {
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

	public CMSItem getSpaceConfig(CMSServiceCtx cmsCtx, String publishSpacePath) throws CMSException {
		CMSItem configItem = null;
		try {
			String savedScope = cmsCtx.getScope();
			String savedPubInfosScope = cmsCtx.getForcePublicationInfosScope();
			try {
				/* La mise en cache du résultat de cette méthode
				 * s'effectue de manière asynchrone.
				 */
				cmsCtx.setAsyncCacheRefreshing(true);
				cmsCtx.setForcePublicationInfosScope("superuser_context");

				configItem = fetchContent(cmsCtx, publishSpacePath);
				
				getCustomizer().getNavigationItemAdapter().adaptPublishSpaceNavigationItem(configItem, configItem);
			}
			finally {
				cmsCtx.setScope(savedScope);
				cmsCtx.setAsyncCacheRefreshing(false);
				cmsCtx.setForcePublicationInfosScope(savedPubInfosScope);
			}

		} catch (Exception e) {
			if (!(e instanceof CMSException))	{
				if( e instanceof NuxeoException && ( ( (NuxeoException) e).getErrorCode() == NuxeoException.ERROR_NOTFOUND))
					return null;
				else
					throw new CMSException(e);
			}
			else	{
				
					throw (CMSException) e;
			}
		}
		return configItem;
	}

	public Map<String, String> parseCMSURL(CMSServiceCtx cmsCtx, String requestPath, Map<String, String> requestParameters)
			throws CMSException  {
		try {
		
			return customizer.parseCMSURL(cmsCtx, requestPath, requestParameters);
		}
			catch (Exception e) {
				if (!(e instanceof CMSException))	{
					if( e instanceof NuxeoException && ( ( (NuxeoException) e).getErrorCode() == NuxeoException.ERROR_NOTFOUND))
						return null;
					else
						throw new CMSException(e);
				}
				else	{
					
						throw (CMSException) e;
				}
			}			
	}

	public List<CMSPage> computeUserPreloadedPages(CMSServiceCtx cmsCtx) throws CMSException {
		try {		

		return customizer.computeUserPreloadedPages(cmsCtx);
		}
		catch (Exception e) {
			if (!(e instanceof CMSException))	{
				if( e instanceof NuxeoException && ( ( (NuxeoException) e).getErrorCode() == NuxeoException.ERROR_NOTFOUND))
					return null;
				else
					throw new CMSException(e);
			}
			else	{
				
					throw (CMSException) e;
			}
		}
	}
	
	/**
	 * Création des fragments dans la page
	 */
	public List<CMSEditableWindow> getEditableWindows(CMSServiceCtx cmsCtx, String pagePath) throws CMSException {
		try {

			CMSItem pageItem = fetchContent(cmsCtx, pagePath);

			List<CMSEditableWindow> windows = new ArrayList<CMSEditableWindow>();
			
			boolean editionMode = false;
			if( "1".equals(cmsCtx.getDisplayLiveVersion()))
				editionMode = true;

			Document doc = (Document) pageItem.getNativeItem();

			// Propriétés générales des fragments
			PropertyList pmFragmentsValues = doc.getProperties().getList(
					EditableWindowService.SCHEMA);

			if (pmFragmentsValues != null && !pmFragmentsValues.isEmpty()) {

				// Pour chaque fragment
				for (int fragmentIndex = 0; fragmentIndex < pmFragmentsValues.size(); fragmentIndex++) {

					// Test de la catégorie
					String fragmentCategory = (String) pmFragmentsValues.getMap(fragmentIndex).get("fragmentCategory");
					String uri = (String) pmFragmentsValues.getMap(fragmentIndex).get("uri");

					EditableWindowTypeEnum type = EditableWindowTypeEnum.findByName(fragmentCategory);

					if (type != null) {
						
						// Récupération d'une classe utilitaire se chargeant des traitements spécifiques à chaque fgt
						EditableWindowService ewService = type.getService();
						
						// Valorisation des propriétés 
						Map<String, String> props = ewService.fillProps(doc, pmFragmentsValues.getMap(fragmentIndex), editionMode);

						// Construction de la window
						windows.add(ewService.createNewEditabletWindow(fragmentIndex, props));
						
					}
					// Si type de portlet non trouvé, erreur.
					else {
						logger.warn("Type de fragment "+fragmentCategory+" non géré");
					}
				}
			}
			

			return windows;
		} catch (Exception e) {
			if (!(e instanceof CMSException)) {
				throw new CMSException(e);
			} else {

				throw (CMSException) e;
			}
		}
		
	}
	
	
	public void deleteFragment(CMSServiceCtx cmsCtx, String pagePath, String refURI)  throws CMSException {

		cmsCtx.setDisplayLiveVersion("1");
		
		CMSItem cmsItem = getContent(cmsCtx, pagePath);
		Document doc = (Document) cmsItem.getNativeItem();
		
		// Propriétés générales des fragments
		PropertyList fragments = doc.getProperties().getList(
				EditableWindowService.SCHEMA);

		List<String> propertiesToRemove = null;
		if (fragments != null && !fragments.isEmpty()) {

			// Recherche du fragment
			for (int fragmentIndex = 0; fragmentIndex < fragments.size(); fragmentIndex++) {
				if(refURI.equals(fragments.getMap(fragmentIndex).get(EditableWindowService.FGT_URI))) {
					
					String typeStr = (String) fragments.getMap(fragmentIndex).get(EditableWindowService.FGT_TYPE);

					EditableWindowTypeEnum type = EditableWindowTypeEnum.findByName(typeStr);
					
					if(type != null) {
						EditableWindowService service = type.getService();
						propertiesToRemove = service.prepareDelete(doc, refURI);
					}
				}
			}
		}

		
        try {
			if(propertiesToRemove != null) {
				
				Document docSaved = (Document) executeNuxeoCommand(cmsCtx, (new DocumentRemovePropertyCommand(doc, propertiesToRemove)));
				
				// On force le rechargement du cache
				cmsCtx.setForceReload(true);
				getContent(cmsCtx, pagePath);
				cmsCtx.setForceReload(false);
			}
        } catch (Exception e) {
            throw new CMSException(e);
        }


	}


	
	public String getEcmUrl(CMSServiceCtx cmsCtx, EcmCommand command, String path,
			Map<String, String> requestParameters) throws CMSException {
		
		String nuxeoPublicHost = System.getProperty("nuxeo.publicHost");
		String nuxeoPublicPort = System.getProperty("nuxeo.publicPort");
		String nuxeoCtx = "/nuxeo";

		URI uri = null;

		try {
			uri = new URI("http://" + nuxeoPublicHost + ":" + nuxeoPublicPort + nuxeoCtx);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}


		String url = "";
		
        if (command == EcmCommand.createPage) {
            url = uri.toString() + "/nxpath/default" + path + "@osivia_create_document?";
            requestParameters.put("type", "SimplePage");
        } else if (command == EcmCommand.editPage) {
            url = uri.toString() + "/nxpath/default" + path + "@osivia_edit_document?";
        } else if (command == EcmCommand.createFgtInRegion) {
			url = uri.toString() + "/nxpath/default" + path + "@fragment_create?";
		}
		else if(command == EcmCommand.createFgtBelowWindow) {
			url = uri.toString() + "/nxpath/default" + path + "@fragment_create?";
		}
		else if(command == EcmCommand.editFgt) {
			url = uri.toString() + "/nxpath/default" + path + "@fragment_edit?";
        } else if (command == EcmCommand.viewSummary) {
            url = uri.toString() + "/nxpath/default" + path + "@view_documents?";
        }

		for(Map.Entry<String, String> param : requestParameters.entrySet()) {
			url = url.concat(param.getKey()).concat("=").concat(param.getValue()).concat("&");
		}
		
		return url;
	}

    public void moveFragment(CMSServiceCtx cmsCtx, String pagePath, String fromRegion, Integer fromPos, String toRegion, Integer toPos, String refUri)
            throws CMSException {

        // On force le rechargement du cache
        cmsCtx.setForceReload(true);
        cmsCtx.setDisplayLiveVersion("1");

        CMSItem cmsItem = getContent(cmsCtx, pagePath);
        Document doc = (Document) cmsItem.getNativeItem();

        try {

            List<String> propertiesToUpdate = EditableWindowService.checkBeforeMove(doc, fromRegion, fromPos, refUri);
            if (propertiesToUpdate.size() > 0) {
                executeNuxeoCommand(cmsCtx, (new DocumentUpdatePropertiesCommand(doc, propertiesToUpdate)));

                CMSItem content = getContent(cmsCtx, pagePath);
                doc = (Document) content.getNativeItem();

            }


            propertiesToUpdate = EditableWindowService.prepareMove(doc, fromRegion, fromPos, toRegion, toPos, refUri);

            if (propertiesToUpdate.size() > 0) {

                executeNuxeoCommand(cmsCtx, (new DocumentUpdatePropertiesCommand(doc, propertiesToUpdate)));

                CMSItem content = getContent(cmsCtx, pagePath);
                Document docReloaded = (Document) content.getNativeItem();


            }
        } catch (Exception e) {
            throw new CMSException(e);
        } finally {
            cmsCtx.setForceReload(false);
        }
    }

}
