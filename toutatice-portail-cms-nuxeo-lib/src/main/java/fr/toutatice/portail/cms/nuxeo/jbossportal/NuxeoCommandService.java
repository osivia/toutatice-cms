package fr.toutatice.portail.cms.nuxeo.jbossportal;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.conn.HttpHostConnectException;
import org.nuxeo.ecm.automation.client.jaxrs.RemoteException;


import fr.toutatice.portail.api.cache.services.CacheInfo;
import fr.toutatice.portail.api.cache.services.ICacheService;
import fr.toutatice.portail.api.cache.services.IServiceInvoker;
import fr.toutatice.portail.api.locator.Locator;
import fr.toutatice.portail.api.statut.IStatutService;
import fr.toutatice.portail.api.statut.ServeurIndisponible;
import fr.toutatice.portail.api.urls.IPortalUrlFactory;
import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommandService;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.core.nuxeo.INuxeoService;
import fr.toutatice.portail.core.nuxeo.NuxeoConnectionProperties;
import fr.toutatice.portail.core.profils.IProfilManager;

/**
 * Gestionnaire de commandes Nuxeo 
 * 
 * Singleton stocké dans le contexte de portlet (doit être libéré lors du undeploy du portlet par appel
 * 
 * de la méthode onDestroy()
 * 
 * @author jeanseb
 *
 */
public class NuxeoCommandService implements INuxeoCommandService {

	private static Log log = LogFactory.getLog(NuxeoCommandService.class);

	// Thread
	ExecutorService executor;

	private Set<AsyncCommandBean> asyncCommands = Collections.synchronizedSet(new HashSet<AsyncCommandBean>());
	

	// Only invoked by factory
	public NuxeoCommandService() throws Exception {
		log.debug("creating NuxeoCommandService");
		
		AsyncCommandThread asyncThread = new AsyncCommandThread(this);
		executor = Executors.newSingleThreadExecutor();
		executor.submit(asyncThread);
	}
	

	public IPortalUrlFactory getPortalUrlFactory(NuxeoCommandContext ctx ) throws Exception {
		IPortalUrlFactory portalUrlFactory = (IPortalUrlFactory) ctx.getPortletContext().getAttribute("UrlService");
		return portalUrlFactory;
	}


	public IProfilManager getProfilManager( NuxeoCommandContext ctx ) throws Exception {

		IProfilManager profilManager = (IProfilManager) ctx.getPortletContext().getAttribute("ProfilService");
		return profilManager;
	}

	public IStatutService getServiceStatut(NuxeoCommandContext ctx ) throws Exception {
		IStatutService serviceStatut = (IStatutService) ctx.getPortletContext().getAttribute("StatutService");
		return serviceStatut;
	}

	public ICacheService getServiceCache(NuxeoCommandContext ctx ) throws Exception {

		ICacheService serviceCache = (ICacheService) ctx.getPortletContext().getAttribute("CacheService");
		return serviceCache;
	}

	protected synchronized void addAsyncronousCommand(NuxeoCommandContext ctx, INuxeoCommand command) {

		if (ctx.getAuthType() == NuxeoCommandContext.AUTH_TYPE_USER) {
			// Pas de maj asychrone en mode USER
			log.warn("asynchronous mode not supported for scope USER");
			return;
		}
		
		// Mémorisation de la commande
		asyncCommands.add(new AsyncCommandBean(ctx, command));
	}
	
	protected synchronized  void removeAsyncronousCommand(AsyncCommandBean command) {
		asyncCommands.remove(command);
	}
	
	
	protected synchronized List<AsyncCommandBean> getAsyncronousCommands() {
		
		// Generate a list to avoid concurrency issues
		List<AsyncCommandBean> commands = new ArrayList<AsyncCommandBean>();
		
		for( AsyncCommandBean command : asyncCommands)	{
			commands.add(command);
		}

		return commands;
	}

	private boolean checkScope(NuxeoCommandContext ctx) throws Exception {
		if (ctx.getAuthType() == NuxeoCommandContext.AUTH_TYPE_USER)
			return true;
		if (ctx.getAuthType() == NuxeoCommandContext.AUTH_TYPE_SUPERUSER)
			return true;
		if (ctx.getAuthType() == NuxeoCommandContext.AUTH_TYPE_ANONYMOUS)
			return true;
		if (ctx.getAuthType() == NuxeoCommandContext.AUTH_TYPE_PROFIL)	{
			if ("true".equals(ctx.getRequest().getAttribute("pia.isAdministrator")))
				return true;
			else
				return getProfilManager( ctx).verifierProfilUtilisateur(ctx.getAuthProfil().getName());
		}
		return false;
	}
	
	
	private String getCacheId(NuxeoCommandContext ctx, INuxeoCommand command)	{
		String cacheId = command.getId();
		
		
		if( ctx.getAuthType() == NuxeoCommandContext.AUTH_TYPE_PROFIL)	{
				cacheId =  ctx.getAuthProfil().getName() + "/"+ command.getId();
		}
	
		return cacheId;
		
	}
	

	private Object invokeViaCache(NuxeoCommandContext ctx, INuxeoCommand command) throws Exception {

		IServiceInvoker nuxeoInvoker = new NuxeoCommandCacheInvoker(ctx, command);

		// Cache user non géré -> Appel direct
		if (ctx.getCacheType() == CacheInfo.CACHE_SCOPE_NONE)
			return nuxeoInvoker.invoke();

		// Cache invalidé -> Appel direct
		if (ctx.getCacheTimeOut() == 0)
			return nuxeoInvoker.invoke();
		

		String cacheId = getCacheId(ctx, command);
	

		CacheInfo cacheInfos = new CacheInfo(cacheId,
				ctx.getCacheType(),
				nuxeoInvoker, ctx.getRequest(), ctx.getPortletContext());

		if (ctx.getCacheTimeOut() == -1) {
			// Traitement par défaut de cache (valeur dans variable système
			// nuxeo.cacheTimeOut

			if (System.getProperty("nuxeo.cacheTimeout") != null)
				cacheInfos.setDelaiExpiration(Long.parseLong(System.getProperty("nuxeo.cacheTimeout")) * 1000);
			else
				cacheInfos.setDelaiExpiration(0L);
		} else
			cacheInfos.setDelaiExpiration(ctx.getCacheTimeOut());

		return getServiceCache(ctx).getCache(cacheInfos);
	}
	
	
	private Object getCachedValue(NuxeoCommandContext ctx, INuxeoCommand command) throws Exception {

		IServiceInvoker nuxeoInvoker = new NuxeoCommandCacheInvoker(ctx, command);

		// Cache user non géré -> Appel direct
		if (ctx.getCacheType() == CacheInfo.CACHE_SCOPE_NONE)
			return null;

		String cacheId = getCacheId(ctx, command);

		CacheInfo cacheInfos = new CacheInfo(cacheId,
				ctx.getCacheType(),
				nuxeoInvoker, ctx.getRequest(), ctx.getPortletContext());
		
		// Pas de controle
		cacheInfos.setForceNOTReload(true);

		return getServiceCache(ctx).getCache(cacheInfos);
	}

	protected boolean checkStatus(NuxeoCommandContext ctx) throws Exception {

		if (getServiceStatut(ctx).isReady(NuxeoConnectionProperties.getPrivateBaseUri().toString()))
			return true;
		else
			return false;

	}



	
	protected void handleError(NuxeoCommandContext ctx, Exception e) throws NuxeoException {
		
		try	{

		if (e instanceof RemoteException) {

			RemoteException re = (RemoteException) e;

			if (re.getStatus() == 404) {
				throw new NuxeoException(NuxeoException.ERROR_NOTFOUND);
			}
			if (re.getStatus() == 401) {
				throw new NuxeoException(NuxeoException.ERROR_FORBIDDEN);
			} else if (re.getStatus() == 500) {
				// On ne notifie pas le statut sur les erreurs 500

			} else {
				getServiceStatut(ctx).notifyError(NuxeoConnectionProperties.getPrivateBaseUri().toString(),
						new ServeurIndisponible(e.getMessage()));
				
			}
		} else if (e instanceof RuntimeException) {
			// Socket Exception non traitée
			
			Throwable cause = e.getCause();
			
			if( cause instanceof HttpHostConnectException || cause instanceof SocketTimeoutException)
				getServiceStatut(ctx).notifyError(NuxeoConnectionProperties.getPrivateBaseUri().toString(),
						new ServeurIndisponible(e.getMessage()));
			
		} 
		// Par défaut les exceptions sont propagées
		throw e;
		}
		catch( Exception e2)	{

			// On retourne toujours une NuxeoException
			if (!(e2 instanceof NuxeoException))
				throw new NuxeoException(e2);
			else
				throw (NuxeoException) e2;
		}


	}

	public Object executeCommand(NuxeoCommandContext ctx, INuxeoCommand command) throws Exception {
		try {
			Object resp = null;
			
			if (!checkScope(ctx)) {
				throw new NuxeoException(NuxeoException.ERROR_FORBIDDEN);
			}

			if (!checkStatus(ctx)) {
				// SI nuxeo est indisponible, on sert ce qu'il y a dans le cache
				// Meme si le cache est expiré
				
				Object cachedValue = getCachedValue(ctx, command);

				if( cachedValue != null)
					resp = cachedValue;
				else
					throw new NuxeoException(NuxeoException.ERROR_UNAVAILAIBLE);
			}

			// Appel avec un décorateur cache
			if( resp == null)
				resp = invokeViaCache(ctx, command);

			return resp;

		} catch (Exception e) {

			handleError(ctx, e);

		} finally {

			// Mises à jour asynchrones
			if (ctx.isAsynchronousUpdates())
				addAsyncronousCommand(ctx, command);

		}
		// On ne passe pas ici
		return null;
	}

	public void destroy() throws Exception {

			// Arret du thread
			if (executor != null) {
				executor.shutdown();
			}

	}

}
