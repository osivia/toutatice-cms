package fr.toutatice.portail.cms.nuxeo.jbossportal;

import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.portal.common.invocation.Scope;
import org.jboss.portal.core.aspects.server.UserInterceptor;
import org.jboss.portal.core.controller.ControllerCommand;
import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.identity.User;
import org.nuxeo.ecm.automation.client.jaxrs.Session;

import fr.toutatice.portail.api.cache.services.IServiceInvoker;
import fr.toutatice.portail.api.locator.Locator;
import fr.toutatice.portail.api.profiler.IProfilerService;
import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.core.nuxeo.INuxeoService;

public class NuxeoCommandCacheInvoker implements IServiceInvoker {
	

	private static final long serialVersionUID = 1L;

	private static Log logger = LogFactory.getLog(NuxeoCommandCacheInvoker.class);
	

	NuxeoCommandContext ctx;
	INuxeoCommand command;
	
	public NuxeoCommandCacheInvoker(NuxeoCommandContext ctx, INuxeoCommand command ) {
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
	

	public Object invoke() throws Exception {
		
			Session nuxeoSession = null;
			
			Object res = null;			
			
			long    begin = System.currentTimeMillis();
			boolean error = false;
			
			String profilerUser = null;

			
			try	{
			

			if (ctx.getScopeType() == NuxeoCommandContext.SCOPE_TYPE_USER) {

				ControllerContext controllerCtx = (ControllerContext) ctx.getRequest().getAttribute("pia.controller");

				User user = (User) controllerCtx.getServerInvocation().getAttribute(Scope.PRINCIPAL_SCOPE,
						UserInterceptor.USER_KEY);

				String userName = null;
				if (user != null)
					userName = user.getUserName();
				
				profilerUser = userName;

				// On regarde s'il existe déjà une session pour cet utilisateur
				try	{
					nuxeoSession = (Session) controllerCtx.getAttribute(ControllerCommand.SESSION_SCOPE, "pia.nuxeoSession");
				} catch (ClassCastException e)	{
					// Peut arriver si rechargement des classes de Nuxeo
				}

				String sessionUserName = (String) controllerCtx.getAttribute(ControllerCommand.SESSION_SCOPE,
						"pia.nuxeoSessionUser");

				if (nuxeoSession == null || (nuxeoSession != null && userName != null && sessionUserName == null)) {
					// Création d'une nouvelle session

					synchronized (ctx.getRequest().getPortletSession()) {

						// On refait les controles pour la synchronisation

						nuxeoSession = (Session) controllerCtx.getAttribute(ControllerCommand.SESSION_SCOPE,
								"pia.nuxeoSession");

						if (nuxeoSession == null || (nuxeoSession != null && userName != null && sessionUserName == null)) {

							long debut = System.currentTimeMillis();

							INuxeoService nuxeoService = Locator.findMBean(INuxeoService.class, "pia:service=NuxeoService");

							nuxeoSession = nuxeoService.createUserSession(userName);

							long fin = System.currentTimeMillis();

							logger.debug("Création de session : " + (fin - debut));

							controllerCtx.setAttribute(ControllerCommand.SESSION_SCOPE, "pia.nuxeoSession", nuxeoSession);
							if (user != null)
								controllerCtx.setAttribute(ControllerCommand.SESSION_SCOPE, "pia.nuxeoSessionUser", user
										.getUserName());
						}
					}
				}
			}

			else {
				/* Gestion des sessions atypiques (ANONYMOUS, PROFIL SUPERUSER) */
				

				// Il a une session nuxeo par contexte de portlet et virtual user

				PortletContext portletCtx = ctx.getPortletContext();

				// Valeurs par défaut (mode anonyme)
				String sessionKey = "pia.nuxeoSession_virtualuser";
				String virtualUser = null;

				if (ctx.getScopeType() == NuxeoCommandContext.SCOPE_TYPE_PROFIL )	{
						sessionKey += "_" + ctx.getScopeProfil().getNuxeoVirtualUser();
						virtualUser = ctx.getScopeProfil().getNuxeoVirtualUser();
				} else if (ctx.getScopeType() == NuxeoCommandContext.SCOPE_TYPE_SUPERUSER )	{
					sessionKey += "_superUser";
					virtualUser = System.getProperty("nuxeo.superUserId" );
				}
				
				profilerUser = virtualUser;
				
				// On regarde s'il existe déjà une session pour cd profil
				try	{
					nuxeoSession = (Session) portletCtx.getAttribute(sessionKey);
				} catch (ClassCastException e)	{
					// Peut arriver si rechargement des classes de Nuxeo
				}


				if (nuxeoSession == null) {
					
					synchronized (getSessionCreationSynchronizer(portletCtx, sessionKey)) {
						if (nuxeoSession == null) {
							INuxeoService nuxeoService = Locator.findMBean(INuxeoService.class, "pia:service=NuxeoService");
							nuxeoSession = nuxeoService.createUserSession(virtualUser);
							portletCtx.setAttribute(sessionKey, nuxeoSession);
						}
					}
				}
			}
			
		


			synchronized (nuxeoSession) {
				
				logger.debug("Execution commande " + command.getId());
				
				res = command.execute(nuxeoSession);
			}
			
			} catch (Exception e)	{

				error = true;
				throw e;
			}
			finally	{
				
				
				long end = System.currentTimeMillis();
				long elapsedTime = end - begin;
						
				String name = "id='"+command.getId()+"',user='" + profilerUser + "'";
				
				IProfilerService profiler = Locator.findMBean(IProfilerService.class, "pia:service=ProfilerService");

				profiler.logEvent("NUXEO", name, elapsedTime, error);
				
			}
			
			

			return res;		
	}

}
