/*
 * (C) Copyright 2014 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 *
 *
 */
package fr.toutatice.portail.cms.nuxeo.services;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.conn.HttpHostConnectException;
import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.server.ServerInvocation;
import org.nuxeo.ecm.automation.client.RemoteException;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.cache.services.CacheInfo;
import org.osivia.portal.api.cache.services.ICacheService;
import org.osivia.portal.api.cache.services.IServiceInvoker;
import org.osivia.portal.api.status.IStatusService;
import org.osivia.portal.api.status.UnavailableServer;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.cms.NavigationItem;
import org.osivia.portal.core.cms.Satellite;
import org.osivia.portal.core.page.PageProperties;
import org.osivia.portal.core.profils.IProfilManager;

import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCommandService;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoServiceCommand;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandContext;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoConnectionProperties;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoSatelliteConnectionProperties;

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

	private final Set<AsyncCommandBean> asyncCommands = Collections.synchronizedSet(new HashSet<AsyncCommandBean>());


	// Only invoked by factory
	public NuxeoCommandService() throws Exception {
		log.debug("creating NuxeoCommandService");

		AsyncCommandThread asyncThread = new AsyncCommandThread(this);
		this.executor = Executors.newSingleThreadExecutor();
		this.executor.submit(asyncThread);
	}


	@Override
    public IPortalUrlFactory getPortalUrlFactory(NuxeoCommandContext ctx ) throws Exception {
		IPortalUrlFactory portalUrlFactory = (IPortalUrlFactory) ctx.getPortletContext().getAttribute("UrlService");
		return portalUrlFactory;
	}


	@Override
    public IProfilManager getProfilManager( NuxeoCommandContext ctx ) throws Exception {

        IProfilManager profilManager = (IProfilManager) ctx.getPortletContext().getAttribute(Constants.PROFILE_SERVICE_NAME);
		return profilManager;
	}

	public IStatusService getServiceStatut(NuxeoCommandContext ctx ) throws Exception {
		IStatusService serviceStatut = (IStatusService) ctx.getPortletContext().getAttribute("StatusService");
		return serviceStatut;
	}

	public ICacheService getServiceCache(NuxeoCommandContext ctx ) throws Exception {

		ICacheService serviceCache = (ICacheService) ctx.getPortletContext().getAttribute("CacheService");
		return serviceCache;
	}

	protected synchronized void addAsyncronousCommand(NuxeoCommandContext ctx, INuxeoServiceCommand command) {

		if (ctx.getAuthType() == NuxeoCommandContext.AUTH_TYPE_USER) {
			// Pas de maj asychrone en mode USER
			//log.warn("asynchronous mode not supported for scope USER");
			return;
		}

		// Mémorisation de la commande

		this.asyncCommands.add(new AsyncCommandBean(ctx, command));
	}

	protected synchronized  void removeAsyncronousCommand(AsyncCommandBean command) {
		this.asyncCommands.remove(command);
	}


	protected synchronized List<AsyncCommandBean> getAsyncronousCommands() {

		// Generate a list to avoid concurrency issues
		List<AsyncCommandBean> commands = new ArrayList<AsyncCommandBean>();

		for( AsyncCommandBean command : this.asyncCommands)	{
			commands.add(command);
		}

		return commands;
	}

	private boolean checkScope(NuxeoCommandContext ctx) throws Exception {
		if (ctx.getAuthType() == NuxeoCommandContext.AUTH_TYPE_USER) {
            return true;
        }
		if (ctx.getAuthType() == NuxeoCommandContext.AUTH_TYPE_SUPERUSER) {
            return true;
        }
		if (ctx.getAuthType() == NuxeoCommandContext.AUTH_TYPE_ANONYMOUS) {
            return true;
        }
		if (ctx.getAuthType() == NuxeoCommandContext.AUTH_TYPE_PROFIL)	{
			if( ctx.isAdministrator()) {
                return true;
            } else {
                return this.getProfilManager( ctx).verifierProfilUtilisateur(ctx.getAuthProfil().getName());
            }
		}
		return false;
	}


	private String getCacheId(NuxeoCommandContext ctx, INuxeoServiceCommand command)	{
		String cacheId = command.getId();

        // Les données en anonymous doivent être marquées tels quelles
        // Sinon elles sont confondues avec les SUPERUSER
        if (ctx.getAuthType() == NuxeoCommandContext.AUTH_TYPE_ANONYMOUS) {
            cacheId = "anonymous" + "/" + command.getId();
        }


		if( ctx.getAuthType() == NuxeoCommandContext.AUTH_TYPE_PROFIL)	{
				cacheId =  ctx.getAuthProfil().getName() + "/"+ command.getId();
		}
		
		cacheId = Satellite.getAsKey(ctx.getSatellite()) +"/" + cacheId;
		
		return cacheId;

	}





	private Object invokeViaCache(NuxeoCommandContext ctx, INuxeoServiceCommand command) throws Exception {

		IServiceInvoker nuxeoInvoker = new NuxeoCommandCacheInvoker(ctx, command);

		/* On regarde si la commande a déjà été traitée dans la requete http */

		ServerInvocation  serverInvoc = ctx.getServerInvocation();
		if( serverInvoc == null)	{
			ControllerContext controllerCtx = ctx.getControlerContext();
			if( controllerCtx != null) {
                // Ne marche pas pour les ressources
				serverInvoc = controllerCtx.getServerInvocation();
            }
		}

		HttpServletRequest portalRequest = null;



		// v2.0.8 : ajout d'une request key pour distinguer les types
		// d'autorisation
		// (il ne faut pas mélanger les résultats pour des authentifications
		// différentes)

		String requestKey = "" + ctx.getAuthType();
		if (ctx.getAuthType() == NuxeoCommandContext.AUTH_TYPE_PROFIL) {
            requestKey += ctx.getAuthProfil().getName();
        }
		requestKey +=  Satellite.getAsKey(ctx.getSatellite()) + "/" + command.getId();

		if (serverInvoc != null) {
			portalRequest = serverInvoc.getServerContext()
					.getClientRequest();
		}

		// LOIC BILLON : cas de la modification/ suppression de fragment
		// On force l'invocation pour recharger le fragment dans tous les cas
		// Meme si présent dans la requete
		if (!ctx.isForceReload())	{

			if (portalRequest != null) {
				Object value = portalRequest.getAttribute(requestKey);
				if (value != null)  {
				    // Has been reloaded since PageResfresh
		            if(  PageProperties.getProperties().isRefreshingPage())  {
		                if( portalRequest.getAttribute(requestKey + ".resfreshed") == null) {
		                    portalRequest.setAttribute(requestKey + ".resfreshed", "1");
                            value = null;
                        }
		            }
		            if( value != null) {
                        return value;
                    }
				}
			}

			// Cache user non géré -> Appel direct
			if (ctx.getCacheType() == CacheInfo.CACHE_SCOPE_NONE) {
                return nuxeoInvoker.invoke();
            }

			// Cache invalidé -> Appel direct
			if (ctx.getCacheTimeOut() == 0) {
                return nuxeoInvoker.invoke();
            }

		}

		String cacheId = this.getCacheId(ctx, command);


		CacheInfo cacheInfos = new CacheInfo(cacheId,
				ctx.getCacheType(),
				nuxeoInvoker, ctx.getRequest(), ctx.getPortletContext(),
				ctx.isAsyncCacheRefreshing());

		// LOIC BILLON : cas de la modification/ suppression de fragment
		if (!ctx.isForceReload())	{

			if (ctx.getCacheType() == CacheInfo.CACHE_SCOPE_PORTLET_SESSION) {
				// 2 minutes de cache de session
				// (PublishInfos & Navigation)
				cacheInfos.setExpirationDelay(120000);
			} else {

				if (ctx.getCacheTimeOut() == -1) {
					// Traitement par défaut de cache (valeur dans variable
					// système
					// nuxeo.cacheTimeOut

					if (System.getProperty("nuxeo.cacheTimeout") != null) {
                        cacheInfos.setExpirationDelay(Long.parseLong(System
								.getProperty("nuxeo.cacheTimeout")) * 1000);
                    } else {
                        cacheInfos.setExpirationDelay(0L);
                    }
				} else {
                    cacheInfos.setExpirationDelay(ctx.getCacheTimeOut());
                }
			}
		} 	else {
			cacheInfos.setForceReload(true);
		}


		Object response =  this.getServiceCache(ctx).getCache(cacheInfos);


		if(portalRequest != null) {

            // prise en compte des éléments de navigation
            boolean navigationItems = false;

            if (response instanceof HashMap) {
                    navigationItems = true;
                    for (Object item : ((Map) response).values()) {
                        if (!(item instanceof NavigationItem)) {
                            navigationItems = false;
                        }

                    }
            }

            //  dans une requete, on ne stocke que les éléments Document et CMSPublicationInfos
            // pour éviter les classcast exception  entre 2 webapps
            if( (response instanceof Document) || (response instanceof CMSPublicationInfos) || navigationItems) {
                portalRequest.setAttribute(requestKey, response);
            }
        }
        return response;

	}


	private Object getCachedValue(NuxeoCommandContext ctx, INuxeoServiceCommand command) throws Exception {

		IServiceInvoker nuxeoInvoker = new NuxeoCommandCacheInvoker(ctx, command);

		// Cache user non géré -> Appel direct
		if (ctx.getCacheType() == CacheInfo.CACHE_SCOPE_NONE) {
            return null;
        }

		String cacheId = this.getCacheId(ctx, command);

		CacheInfo cacheInfos = new CacheInfo(cacheId,
				ctx.getCacheType(),
				nuxeoInvoker, ctx.getRequest(), ctx.getPortletContext(),
				ctx.isAsyncCacheRefreshing());

		// Pas de controle
		cacheInfos.setForceNOTReload(true);

		return this.getServiceCache(ctx).getCache(cacheInfos);
	}

	protected boolean checkStatus(NuxeoCommandContext ctx) throws Exception {

		if (this.getServiceStatut(ctx).isReady(NuxeoSatelliteConnectionProperties.getConnectionProperties(ctx.getSatellite()).getPrivateBaseUri().toString())) {
            return true;
        } else {
            return false;
        }

	}




	protected void handleError(NuxeoCommandContext ctx, String commandId, Exception e) throws CMSException {

		try	{

		if (e instanceof RemoteException) {

			RemoteException re = (RemoteException) e;

			if (re.getStatus() == 404) {
				throw new CMSException(CMSException.ERROR_NOTFOUND);
			}
                // DCH : Bug CAS ??? (nouveau code erreur 403 à gérer)

                if ((re.getStatus() == 401) || (re.getStatus() == 403)) {
				throw new CMSException(CMSException.ERROR_FORBIDDEN);
			} else if (re.getStatus() == 500) {
				// On ne notifie pas le statut sur les erreurs 500

			} else {
				this.getServiceStatut(ctx).notifyError(NuxeoSatelliteConnectionProperties.getConnectionProperties(ctx.getSatellite()).getPrivateBaseUri().toString(),
						new UnavailableServer(e.getMessage()));

			}
		} else if (e instanceof RuntimeException) {
			// Socket Exception non traitée

			Throwable cause = e.getCause();

			if( (cause instanceof HttpHostConnectException) || (cause instanceof SocketTimeoutException)) {
                this.getServiceStatut(ctx).notifyError(NuxeoSatelliteConnectionProperties.getConnectionProperties(ctx.getSatellite()).getPrivateBaseUri().toString(),
						new UnavailableServer(e.getMessage()));
            }

		}
		// Par défaut les exceptions sont propagées
		throw e;
		}
		catch( Exception e2)	{
			
			CMSException exc = null;
			
			// On retourne toujours une NuxeoException
			if (!(e2 instanceof CMSException)) {
				exc =  new CMSException(e2);
            } else {
            	exc =  (CMSException) e2;
            }
			
			exc.setSatellite(ctx.getSatellite());
			exc.setCommandId(commandId);
			
			throw exc;
		}


	}

	@Override
    public Object executeCommand(NuxeoCommandContext ctx, INuxeoServiceCommand command) throws Exception {
		try {
            if (ctx.isAsynchronousCommand()) {
                this.addAsyncronousCommand(ctx, command);
            } else {
                Object resp = null;

                if (!this.checkScope(ctx)) {
                    throw new CMSException(CMSException.ERROR_FORBIDDEN);
                }

                if (!this.checkStatus(ctx)) {
                    // SI nuxeo est indisponible, on sert ce qu'il y a dans le cache
                    // Meme si le cache est expiré

                    Object cachedValue = this.getCachedValue(ctx, command);

                    if (cachedValue != null) {
                        resp = cachedValue;
                    } else {
                        throw new CMSException(CMSException.ERROR_UNAVAILAIBLE);
                    }
                }

                // Appel avec un décorateur cache
                if (resp == null) {
                    resp = this.invokeViaCache(ctx, command);
                }

                return resp;
            }

		} catch (Exception e) {

			this.handleError(ctx, command.getId(), e);

		} finally {

			// Mises à jour asynchrones
			if (ctx.isAsynchronousUpdates()) {
                this.addAsyncronousCommand(ctx, command);
            }

		}
		// On ne passe pas ici
		return null;
	}

	@Override
    public void destroy() throws Exception {

			// Arret du thread
			if (this.executor != null) {
				this.executor.shutdown();
			}

	}

}
