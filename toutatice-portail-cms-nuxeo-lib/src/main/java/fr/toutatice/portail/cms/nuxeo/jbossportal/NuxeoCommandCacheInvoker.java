package fr.toutatice.portail.cms.nuxeo.jbossportal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.portal.common.invocation.Scope;
import org.jboss.portal.core.aspects.server.UserInterceptor;
import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.identity.User;
import org.jboss.portal.server.ServerInvocation;
import org.nuxeo.ecm.automation.client.Session;
import org.osivia.portal.api.cache.services.IServiceInvoker;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.profiler.IProfilerService;
import org.osivia.portal.api.statut.IStatutService;
import org.osivia.portal.api.statut.ServeurIndisponible;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.core.nuxeo.INuxeoService;
import fr.toutatice.portail.core.nuxeo.NuxeoConnectionProperties;

public class NuxeoCommandCacheInvoker implements IServiceInvoker {

	private static final long serialVersionUID = 1L;

	private static Log logger = LogFactory.getLog(NuxeoCommandCacheInvoker.class);

	NuxeoCommandContext ctx;
	INuxeoCommand command;

	public NuxeoCommandCacheInvoker(NuxeoCommandContext ctx, INuxeoCommand command) {
		super();
		this.command = command;
		this.ctx = ctx;
	}

	private static Map<String, Object> sessionCreationSynchronizers = new HashMap<String, Object>();

	private static synchronized Object getSessionCreationSynchronizer(PortletContext ctx, String virtualUser) {

		String key = "SYNC_" + ctx.hashCode();

		if (virtualUser != null)
			key += "_" + virtualUser;

		if (sessionCreationSynchronizers.get(key) == null)
			sessionCreationSynchronizers.put(key, key);

		return sessionCreationSynchronizers.get(key);
	}

	
	private static List<Integer> AVERAGE_LIST  = new ArrayList<Integer>();
	private static final int AVERAGE_SIZE = 20;
	
	
	public IStatutService getServiceStatut(NuxeoCommandContext ctx ) throws Exception {
		IStatutService serviceStatut = (IStatutService) ctx.getPortletContext().getAttribute("StatutService");
		return serviceStatut;
	}
	
	
	
	public Object invoke() throws Exception {

		Object res = null;

		boolean error = false;

		String profilerUser = null;

		List<Session> sessionsProfils = null;
		ServerInvocation userSessionInvocation = null;

		Session nuxeoSession = null;
		
		boolean recyclableSession = true;


		try {

			if (ctx.getAuthType() == NuxeoCommandContext.AUTH_TYPE_USER) {
				
				ServerInvocation invocation = ctx.getServerInvocation();
				
				if( invocation == null)	{
					ControllerContext controllerCtx = ctx.getControlerContext();
					invocation = controllerCtx.getServerInvocation();
				}


				User user = (User) invocation.getAttribute(Scope.PRINCIPAL_SCOPE,
						UserInterceptor.USER_KEY);

				String userName = null;
				if (user != null)
					userName = user.getUserName();

				profilerUser = userName;
				if( profilerUser == null)
					profilerUser = "unlogged-user";

				// On regarde s'il existe déjà une session pour cet utilisateur
				try {
					nuxeoSession = (Session) invocation.getAttribute(Scope.SESSION_SCOPE,
							"osivia.nuxeoSession");
				} catch (ClassCastException e) {
					// Peut arriver si rechargement des classes de Nuxeo
				}

				String sessionUserName = (String) invocation.getAttribute(Scope.SESSION_SCOPE,
						"osivia.nuxeoSessionUser");

				String sessionCreationSynchronizer = (String) invocation.getAttribute(
						Scope.SESSION_SCOPE, "osivia.sessionCreationSynchronizer");

				if (sessionCreationSynchronizer == null) {
					sessionCreationSynchronizer = "sessionCreationSynchronizer";
					invocation.setAttribute(Scope.SESSION_SCOPE, "osivia.sessionCreationSynchronizer",
							sessionCreationSynchronizer);
				}

				if (nuxeoSession == null || (nuxeoSession != null && userName != null && sessionUserName == null)) {
					// Création d'une nouvelle session

					synchronized (sessionCreationSynchronizer) {

						// On refait les controles pour la synchronisation

						nuxeoSession = (Session) invocation.getAttribute(Scope.SESSION_SCOPE,
								"osivia.nuxeoSession");

						sessionUserName = (String) invocation.getAttribute(Scope.SESSION_SCOPE,
								"osivia.nuxeoSessionUser");

						if (nuxeoSession == null
								|| (nuxeoSession != null && userName != null && sessionUserName == null)) {

							INuxeoService nuxeoService = Locator.findMBean(INuxeoService.class,
									"osivia:service=NuxeoService");

							nuxeoSession = nuxeoService.createUserSession(userName);

							invocation.setAttribute(Scope.SESSION_SCOPE, "osivia.nuxeoSession",
									nuxeoSession);
							
							invocation.setAttribute(Scope.SESSION_SCOPE, "osivia.nuxeoProfilerUserSessionTs",
									System.currentTimeMillis());

							if (user != null)
								invocation.setAttribute(Scope.SESSION_SCOPE, "osivia.nuxeoSessionUser",
										user.getUserName());
						}
						
						userSessionInvocation = invocation;
					}
				}				

	
			}

			else {
				/* Gestion des sessions atypiques (ANONYMOUS, PROFIL SUPERUSER) */

				// Il a une session nuxeo par contexte de portlet et virtual
				// user

				PortletContext portletCtx = ctx.getPortletContext();

				// Valeurs par défaut
				String sessionKey = "osivia.nuxeoSession_virtualuser.";
				String virtualUser = null;

				if (ctx.getAuthType() == NuxeoCommandContext.AUTH_TYPE_ANONYMOUS) {
					sessionKey += "__ANONYMOUS__";
				} else if (ctx.getAuthType() == NuxeoCommandContext.AUTH_TYPE_PROFIL) {
					sessionKey += "__PROFIL_" + ctx.getAuthProfil().getNuxeoVirtualUser() + "__";
					virtualUser = ctx.getAuthProfil().getNuxeoVirtualUser();
				} else if (ctx.getAuthType() == NuxeoCommandContext.AUTH_TYPE_SUPERUSER) {
					sessionKey += "__SUPERUSER__";
					virtualUser = System.getProperty("nuxeo.superUserId");
				}

				profilerUser = virtualUser;
				if( profilerUser == null)
					profilerUser = "vu-anonymous";


				// Profils session list creation
				synchronized (getSessionCreationSynchronizer(portletCtx, sessionKey)) {

					sessionsProfils = (List<Session>) portletCtx.getAttribute(sessionKey);

					if (sessionsProfils == null) {
						sessionsProfils = new ArrayList<Session>();
						portletCtx.setAttribute(sessionKey, sessionsProfils);
					}

					if (sessionsProfils.size() > 0) {
						nuxeoSession = sessionsProfils.get(0);
						sessionsProfils.remove(0);
					}
				}

				if (nuxeoSession == null) {
					// v2.0.22 : code inutile
					//logger.info("Creating nuxeo session for virtual user" + virtualUser);

					INuxeoService nuxeoService = Locator.findMBean(INuxeoService.class, "osivia:service=NuxeoService");
					nuxeoSession = nuxeoService.createUserSession(virtualUser);
				}
			}

			logger.debug("Execution commande " + command.getId());

			long begin = 0;

			try {
				synchronized (nuxeoSession) {
					// v1.0.16 : déplacement création de session
					
					begin = System.currentTimeMillis();
					
					res = command.execute(nuxeoSession);
				}
			} 
			
			catch (Exception e) {
				
				if( e.getCause() instanceof IllegalStateException)
					recyclableSession = false;
				error = true;
				throw e;
			} finally {
				
				long end = System.currentTimeMillis();
				long elapsedTime = end - begin;
				
				// log into profiler

				String name = "id='" + command.getId() + "',user='" + profilerUser + "'";
				
				name +=", nuxeoSession=" +nuxeoSession.hashCode();

				IProfilerService profiler = Locator.findMBean(IProfilerService.class, "osivia:service=ProfilerService");
				
				
				
					// Moyenne flottante sur les publishInfosCommands
				
					String statusErrorMsg = null;

					if (!error && command.getId().startsWith("PublishInfosCommand")) {
						synchronized (AVERAGE_LIST) {

							while (AVERAGE_LIST.size() >= AVERAGE_SIZE) {
								AVERAGE_LIST.remove(0);
							}

							AVERAGE_LIST.add((int) elapsedTime);

							if (AVERAGE_LIST.size() == AVERAGE_SIZE) {

								long total = 0l;
								for (int i = 0; i < AVERAGE_LIST.size(); i++) {
									total += AVERAGE_LIST.get(i);
								}

								long moyenne = total / AVERAGE_LIST.size();

								if (moyenne > 150) {
									statusErrorMsg = "Moyenne flottante : " + moyenne + "ms";

									AVERAGE_LIST.clear();
								}
							}
						}
					}
					
					if( statusErrorMsg != null)	{
						// On force le DOWN pour laisser Nuxeo souffler
						getServiceStatut(ctx).notifyError(NuxeoConnectionProperties.getPrivateBaseUri().toString(), new ServeurIndisponible("[DOWN]" + statusErrorMsg));
					}




				profiler.logEvent("NUXEO", name, elapsedTime, error);				
			}

		} finally {

			// recycle the session
			// v2.0.21 : ajout test session recyclable
			
			if (sessionsProfils != null && recyclableSession == true)
				sessionsProfils.add(nuxeoSession);

			if( userSessionInvocation != null && recyclableSession == false)
				userSessionInvocation.removeAttribute(Scope.SESSION_SCOPE,	"osivia.nuxeoSession");
		}

		return res;
	}

}
