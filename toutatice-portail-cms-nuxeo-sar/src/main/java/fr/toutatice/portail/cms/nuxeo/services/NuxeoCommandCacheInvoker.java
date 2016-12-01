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
 */
package fr.toutatice.portail.cms.nuxeo.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.server.ServerInvocation;
import org.nuxeo.ecm.automation.client.Session;
import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.cache.services.IServiceInvoker;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.log.LoggerMessage;
import org.osivia.portal.api.profiler.IProfilerService;
import org.osivia.portal.api.status.IStatusService;
import org.osivia.portal.api.status.UnavailableServer;
import org.osivia.portal.core.cms.IContentStreamingSupport;
import org.osivia.portal.core.error.IPortalLogger;

import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoService;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoServiceCommand;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandContext;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoConnectionProperties;

public class NuxeoCommandCacheInvoker implements IServiceInvoker {

    private static final long serialVersionUID = 1L;

    private static Log logger = LogFactory.getLog(NuxeoCommandCacheInvoker.class);

    NuxeoCommandContext ctx;
    INuxeoServiceCommand command;

    public NuxeoCommandCacheInvoker(NuxeoCommandContext ctx, INuxeoServiceCommand command) {
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


    private static List<Integer> AVERAGE_LIST = new ArrayList<Integer>();
    private static final int AVERAGE_SIZE = 20;


    public IStatusService getServiceStatut(NuxeoCommandContext ctx) throws Exception {
        IStatusService serviceStatut = (IStatusService) ctx.getPortletContext().getAttribute("StatusService");
        return serviceStatut;
    }


    @Override
    public Object invoke() throws PortalException {

        try {

            Object res = null;

            boolean error = false;

            String profilerUser = null;

            List<Session> sessionsProfils = null;

            HttpSession userSession = null;

            Session nuxeoSession = null;
            boolean recyclableSession = true;

            try {

                if (this.ctx.getAuthType() == NuxeoCommandContext.AUTH_TYPE_USER) {

                    {

                        HttpServletRequest userRequest = null;
                        String userName = null;                        


                        {

                            ServerInvocation invocation = this.ctx.getServerInvocation();


                            if (invocation == null) {
                                ControllerContext controllerCtx = this.ctx.getControlerContext();
                                if( controllerCtx != null)
                                    invocation = controllerCtx.getServerInvocation();
                            }


                            if (invocation != null) {
                                userRequest = invocation.getServerContext().getClientRequest();
                                userName = userRequest.getRemoteUser();
                            }
                            else    {
                                userRequest = (HttpServletRequest) ctx.getRequest();
                                userName = (String) userRequest.getAttribute("osivia.delegation.userName");
                            }
                        }


                        userSession = userRequest.getSession();



                        profilerUser = userName;
                        if (profilerUser == null)
                            profilerUser = "unlogged-user";

                        // On regarde s'il existe déjà une session pour cet utilisateur
                        try {
                            nuxeoSession = (Session) userSession.getAttribute("osivia.nuxeoSession");
                        } catch (ClassCastException e) {
                            // Peut arriver si rechargement des classes de Nuxeo
                        }

                        String sessionUserName = (String) userSession.getAttribute("osivia.nuxeoSessionUser");

                        String sessionCreationSynchronizer = (String) userSession.getAttribute("osivia.sessionCreationSynchronizer");

                        if (sessionCreationSynchronizer == null) {
                            sessionCreationSynchronizer = "sessionCreationSynchronizer";
                            userSession.setAttribute("osivia.sessionCreationSynchronizer", sessionCreationSynchronizer);
                        }

                        if (nuxeoSession == null || (nuxeoSession != null && userName != null && sessionUserName == null)) {
                            // Création d'une nouvelle session

                            synchronized (sessionCreationSynchronizer) {

                                // On refait les controles pour la synchronisation

                                nuxeoSession = (Session) userSession.getAttribute("osivia.nuxeoSession");

                                sessionUserName = (String) userSession.getAttribute("osivia.nuxeoSessionUser");

                                if (nuxeoSession == null || (nuxeoSession != null && userName != null && sessionUserName == null)) {

                                    INuxeoService nuxeoService = Locator.findMBean(INuxeoService.class, "osivia:service=NuxeoService");

                                    nuxeoSession = nuxeoService.createUserSession(userName);

                                    userSession.setAttribute("osivia.nuxeoSession", nuxeoSession);

                                    userSession.setAttribute("osivia.nuxeoProfilerUserSessionTs", System.currentTimeMillis());

                                    if (userName != null)
                                        userSession.setAttribute("osivia.nuxeoSessionUser", userName);
                                }


                            }
                        }
                    }
                }


                else {
                    /* Gestion des sessions atypiques (ANONYMOUS, PROFIL SUPERUSER) */

                    // Il a une session nuxeo par contexte de portlet et virtual
                    // user

                    PortletContext portletCtx = this.ctx.getPortletContext();

                    // Valeurs par défaut
                    String sessionKey = "osivia.nuxeoSession_virtualuser.";
                    String virtualUser = null;

                    if (this.ctx.getAuthType() == NuxeoCommandContext.AUTH_TYPE_ANONYMOUS) {
                        sessionKey += "__ANONYMOUS__";
                    } else if (this.ctx.getAuthType() == NuxeoCommandContext.AUTH_TYPE_PROFIL) {
                        sessionKey += "__PROFIL_" + this.ctx.getAuthProfil().getNuxeoVirtualUser() + "__";
                        virtualUser = this.ctx.getAuthProfil().getNuxeoVirtualUser();
                    } else if (this.ctx.getAuthType() == NuxeoCommandContext.AUTH_TYPE_SUPERUSER) {
                        sessionKey += "__SUPERUSER__";
                        virtualUser = System.getProperty("nuxeo.superUserId");
                    }

                    profilerUser = virtualUser;
                    if (profilerUser == null)
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
                        // logger.info("Creating nuxeo session for virtual user" + virtualUser);

                        INuxeoService nuxeoService = Locator.findMBean(INuxeoService.class, "osivia:service=NuxeoService");
                        nuxeoSession = nuxeoService.createUserSession(virtualUser);
                    }
                }

                logger.debug("Execution commande " + this.command.getId());
                
                if( IPortalLogger.logger.isDebugEnabled()){
                    String commandName = "";
                    if(this.command.getId() != null)
                        commandName = this.command.getId().replaceAll("\"", "'");
                    IPortalLogger.logger.debug(new LoggerMessage("call to nuxeo request  \""+commandName +"\""));
                }
                
            

                long begin = 0;

                try {
                    synchronized (nuxeoSession) {
                        // v1.0.16 : déplacement création de session

                        begin = System.currentTimeMillis();

                        res = this.command.execute(nuxeoSession);
                    }
                } catch (Exception e) {

                    if (e.getCause() instanceof IllegalStateException)
                        recyclableSession = false;

                    error = true;
                    throw e;
                } finally {

                    long end = System.currentTimeMillis();
                    long elapsedTime = end - begin;

                    // LBI si la commande n'a pas d'ID, on lui propose l'instance java
                    String cmdId = this.command.getId();
                    if (this.command.getId() == null) {
                        cmdId = this.command.toString();
                    }

                    // log into profiler

                    String name = "id='" + cmdId + "',user='" + profilerUser + "'";

                    name += ", nuxeoSession=" + nuxeoSession.hashCode();

                    IProfilerService profiler = Locator.findMBean(IProfilerService.class, "osivia:service=ProfilerService");


                    // Moyenne flottante sur les publishInfosCommands

                    String statusErrorMsg = null;


                    String maxAverageDelay = System.getProperty("nuxeo.maxAverageDelayMs");

                    if (maxAverageDelay != null) {
                        long maxDelay = Long.parseLong(maxAverageDelay);

                        if (!error && this.command.getId().startsWith("PublishInfosCommand")) {
                            synchronized (AVERAGE_LIST) {

                                while (AVERAGE_LIST.size() >= AVERAGE_SIZE) {
                                    AVERAGE_LIST.remove(0);
                                }

                                // On ignore les timeout genre 60000 car il n'illustre pas un comportement progressif
                                // Le but est de determiner des mini-pics

                                if (elapsedTime < 1000)
                                    AVERAGE_LIST.add((int) elapsedTime);

                                if (AVERAGE_LIST.size() == AVERAGE_SIZE) {

                                    long total = 0l;
                                    for (int i = 0; i < AVERAGE_LIST.size(); i++) {
                                        total += AVERAGE_LIST.get(i);
                                    }

                                    long moyenne = total / AVERAGE_LIST.size();

                                    if (moyenne > maxDelay) {
                                        statusErrorMsg = "Moyenne flottante : " + moyenne + "ms";

                                        AVERAGE_LIST.clear();
                                    }
                                }
                            }
                        }
                    }

                    if (statusErrorMsg != null) {
                        // On force le DOWN pour laisser Nuxeo souffler
                        this.getServiceStatut(this.ctx).notifyError(NuxeoConnectionProperties.getPrivateBaseUri().toString(),
                                new UnavailableServer("[DOWN]" + statusErrorMsg));
                    }


                    profiler.logEvent("NUXEO", name, elapsedTime, error);
                    
                    
                    
                    
                    if( IPortalLogger.logger.isDebugEnabled()){
                        String nuxeoHost = NuxeoConnectionProperties.getPrivateBaseUri().getHost();
                        if( error == false)
                            IPortalLogger.logger.debug(new LoggerMessage("call to nuxeo result "+ nuxeoHost+ " " + elapsedTime));
                        else
                            IPortalLogger.logger.debug(new LoggerMessage("call to nuxeo result "+ nuxeoHost+ " " + elapsedTime + " \"an error as occured\""));
                           
                    }

                }

            } finally {

                if (res instanceof IContentStreamingSupport) {
                    IContentStreamingSupport contentStreamingSupport = (IContentStreamingSupport) res;
                    recyclableSession = (contentStreamingSupport.getStream() == null);
                }


                // recycle the session
                if (sessionsProfils != null && recyclableSession == true)
                    sessionsProfils.add(nuxeoSession);

                if (userSession != null && recyclableSession == false)
                    userSession.removeAttribute("osivia.nuxeoSession");


            }

            return res;
        } catch (Exception e) {
            throw PortalException.wrap(e);
        }
    }

}
