package fr.toutatice.portail.cms.nuxeo.jbossportal;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osivia.portal.api.cache.services.CacheInfo;
import org.osivia.portal.api.cache.services.IServiceInvoker;
import org.osivia.portal.core.error.Debug;


/**
 * Execution asynchrone des commandes Nuxeo
 * 
 * @author jeanseb
 * 
 */

public class AsyncCommandThread implements Runnable {

	private static Log logger = LogFactory.getLog(AsyncCommandThread.class);

	private NuxeoCommandService commandService;

	public AsyncCommandThread(NuxeoCommandService commandService) {
		super();
		this.commandService = commandService;
	}

	public void run() {

		while (true) {

			try {

				Thread.sleep(1 * 12000);

				// logger.debug("execution asynchrone thread " +
				// Thread.currentThread().getId() + " nb taches:" +
				// commandService.getAsyncronousCommand().size());

				// Nouvelle itération sur les commandes

				if (!"0".equals(System.getProperty("nuxeo.asyncSupport"))) {

					List<AsyncCommandBean> commands = commandService.getAsyncronousCommands();

					for (AsyncCommandBean command : commands) {

						try {

							// On attend que le service Nuxeo soit disponible

							while (!commandService.checkStatus(command.getCtx())) {
								Thread.sleep(1 * 10000);
							}

							// Appel commande
							IServiceInvoker cacheInvoker = new NuxeoCommandCacheInvoker(command.getCtx(),
									command.getCommand());

							int scopeCache = CacheInfo.CACHE_SCOPE_NONE;
							String cacheId = command.getCommand().getId();

							if (command.getCtx().getCacheType() == CacheInfo.CACHE_SCOPE_GLOBAL) {
								scopeCache = CacheInfo.CACHE_SCOPE_GLOBAL;
							}

							if (command.getCtx().getCacheType() == CacheInfo.CACHE_SCOPE_PORTLET_SESSION) {
								scopeCache = CacheInfo.CACHE_SCOPE_PORTLET_SESSION;
							}

							if (command.getCtx().getCacheType() == CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT) {
								scopeCache = CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT;
							}

							if (command.getCtx().getAuthType() == NuxeoCommandContext.AUTH_TYPE_PROFIL) {
								cacheId = command.getCtx().getAuthProfil().getName() + "/"
										+ command.getCommand().getId();
							}

							CacheInfo cacheInfos = new CacheInfo(cacheId, scopeCache, cacheInvoker, null, command
									.getCtx().getPortletContext());

							// Forçage de la mise à jour du cache
							cacheInfos.setForceReload(true);

							commandService.getServiceCache(command.getCtx()).getCache(cacheInfos);
						}

						catch (Exception e) {

							// Par défaut, en cas d'erreur la commande
							// asynchrone est supprimée
							commandService.removeAsyncronousCommand(command);

							commandService.handleError(command.getCtx(), e);

							throw e;

						}

					}
				}

			} catch (Exception e) {
				logger.error(Debug.throwableToString(e));
			}

		}

	}

}
