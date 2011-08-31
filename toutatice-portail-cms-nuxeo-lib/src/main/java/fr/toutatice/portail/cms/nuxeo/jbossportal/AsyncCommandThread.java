package fr.toutatice.portail.cms.nuxeo.jbossportal;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.toutatice.portail.api.cache.services.CacheInfo;
import fr.toutatice.portail.api.cache.services.IServiceInvoker;

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
				
				logger.debug("execution asynchrone thread " + Thread.currentThread().getId() + " nb taches:" + commandService.getAsyncronousCommand().size());

				// Nouvelle itération sur les commandes

				Iterator<AsyncCommandBean> iCommands = commandService.getAsyncronousCommand().iterator();

				while (iCommands.hasNext()) {

					AsyncCommandBean command = iCommands.next();



					try {

						// On attend que le service Nuxeo soit disponible

						while (!commandService.checkStatus(command.getCtx())) {
							Thread.sleep(1 * 10000);
						}

						// Appel commande
						IServiceInvoker cacheInvoker = new NuxeoCommandCacheInvoker(command.getCtx(),
								command.getCommand());

						CacheInfo cacheInfos = new CacheInfo(command.getCommand().getId(), command.getCtx()
								.getScopeType() == NuxeoCommandContext.SCOPE_TYPE_ANONYMOUS ? "anonymous" : command
								.getCtx().getScopeProfil().getNuxeoVirtualUser(), cacheInvoker, null, command.getCtx()
								.getPortletContext());

						// Forçage de la mise à jour du cache
						cacheInfos.setForceReload(true);

						commandService.getServiceCache().getCache(cacheInfos);
					}

					catch (Exception e) {

						// Par défaut, en cas d'erreur la commande
						// asynchrone est supprimée
						commandService.removeAsyncronousCommand(command);

						commandService.handleError(command.getCtx(), e);
						
						throw e;

					}

				}

			} catch (Exception e) {
				logger.error(e);
			}

		}

	}

}
