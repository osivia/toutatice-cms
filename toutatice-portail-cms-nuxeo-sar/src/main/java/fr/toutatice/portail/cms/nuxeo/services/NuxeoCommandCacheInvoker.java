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

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.portlet.PortletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.server.ServerInvocation;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.FileBlob;
import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.cache.services.IServiceInvoker;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.log.LoggerMessage;
import org.osivia.portal.api.profiler.IProfilerService;
import org.osivia.portal.api.status.IStatusService;
import org.osivia.portal.api.status.UnavailableServer;
import org.osivia.portal.api.transaction.ITransactionService;
import org.osivia.portal.core.cms.IContentStreamingSupport;
import org.osivia.portal.core.cms.Satellite;
import org.osivia.portal.core.error.IPortalLogger;

import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoService;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoServiceCommand;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandContext;

public class NuxeoCommandCacheInvoker implements IServiceInvoker {
	

    private static final long serialVersionUID = 1L;

    private static Log logger = LogFactory.getLog(NuxeoCommandCacheInvoker.class);

    NuxeoCommandContext ctx;
    INuxeoServiceCommand command;
    
    /** Maximum time when conneixons are considered inactive */
    private long maxIdleTime = 600000;
    
	private static Map<Integer, Long> sessionsIdle = new HashMap<Integer, Long>();

    public NuxeoCommandCacheInvoker(NuxeoCommandContext ctx, INuxeoServiceCommand command) {
        super();
        this.command = command;
        this.ctx = ctx;
        
        if(System.getProperty("nuxeo.connection.maxIdleTime") != null) {
        	maxIdleTime = Long.parseLong(System.getProperty("nuxeo.connection.maxIdleTime"));
        }
        
    }

    private static Map<String, Object> sessionCreationSynchronizers = new HashMap<String, Object>();
    
    
    public static  Map<String, Session> getUserSessions( HttpSession session){
    	@SuppressWarnings("unchecked")
		Map<String, Session> sessions =  (Map<String, Session>) session.getAttribute("nuxeo.sessions");
    	if (sessions == null)	{
    		sessions =  new ConcurrentHashMap<String, Session>();
    		session.setAttribute("nuxeo.sessions", sessions);
    	}
    	return sessions;
    	
    }
    
    private static String getSessionPostName(Satellite satellite) {
        if (satellite == null) {
            satellite = Satellite.MAIN;
        }

        return "." + satellite.getId();
    }
    
    private String getSessionKey() {
        return ctx.getSatellite().getId();
    }
    
    private  String getSessionPrefix()	{
    	return getSessionKey()+".";
    }
    

    private static synchronized Object getSessionCreationSynchronizer(PortletContext ctx, String virtualUser, Satellite satellite) {

        String key = "SYNC_" +  ctx.hashCode();

        if (virtualUser != null)
            key += "_" + virtualUser;
        
        key += getSessionPostName( satellite) ;

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
            	IProfilerService profiler = Locator.findMBean(IProfilerService.class, "osivia:service=ProfilerService");
            	

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
                                
                                if( userName == null)
                                    userName = userRequest.getRemoteUser();
                            }
                        }


                        userSession = userRequest.getSession();



                        profilerUser = userName;
                        if (profilerUser == null)
                            profilerUser = "unlogged-user";

                        // On regarde s'il existe déjà une session pour cet utilisateur
                        try {
                        	
                        	nuxeoSession = getUserSessions( userSession ).get(getSessionKey());
                        	

                        } catch (ClassCastException e) {
                            // Peut arriver si rechargement des classes de Nuxeo
                        }
                        
                        if(nuxeoSession != null) {
                        	Long long1 = sessionsIdle.get(nuxeoSession.hashCode());
                        	if(long1 != null && System.currentTimeMillis() - long1 >= maxIdleTime) {
    	                		String name = "shutdown";

                                String nuxeoSrc = "NUXEO/" + ctx.getSatellite().getId();
    	                        profiler.logEvent(nuxeoSrc, name, System.currentTimeMillis() - long1, error);

                        		nuxeoSession.getClient().shutdown();
                        		sessionsIdle.remove(nuxeoSession.hashCode());
                        		nuxeoSession = null;
                        		
                            	getUserSessions( userSession ).remove(getSessionKey());

                        	}
                        }
                        

                        String sessionUserName = (String) userSession.getAttribute("osivia.nuxeoSessionUser"+getSessionPrefix());

                        String sessionCreationSynchronizer = (String) userSession.getAttribute("osivia.sessionCreationSynchronizer");

                        if (sessionCreationSynchronizer == null) {
                            sessionCreationSynchronizer = "sessionCreationSynchronizer"+getSessionPrefix();
                            userSession.setAttribute("osivia.sessionCreationSynchronizer"+getSessionPrefix(), sessionCreationSynchronizer);
                        }
                        
                        
                        if (nuxeoSession == null || (nuxeoSession != null && userName != null && sessionUserName == null)) {
                            // Création d'une nouvelle session

                            synchronized (sessionCreationSynchronizer) {

                                // On refait les controles pour la synchronisation

                                nuxeoSession = getUserSessions( userSession ).get(getSessionKey());

                                sessionUserName = (String) userSession.getAttribute("osivia.nuxeoSessionUser"+getSessionPrefix());
                                
                                

                                if (nuxeoSession == null || (nuxeoSession != null && userName != null && sessionUserName == null)) {

                                    INuxeoService nuxeoService = Locator.findMBean(INuxeoService.class, "osivia:service=NuxeoService");

                                    nuxeoSession = nuxeoService.createUserSession(ctx.getSatellite(), userName);
                                    
                                    long start = System.currentTimeMillis();
                                    sessionsIdle.put(nuxeoSession.hashCode(), start);

                                    getUserSessions( userSession ).put(getSessionKey(), nuxeoSession);
                                     userSession.setAttribute("osivia.nuxeoProfilerUserSessionTs"+getSessionPrefix(), System.currentTimeMillis());

                                    if (userName != null)
                                        userSession.setAttribute("osivia.nuxeoSessionUser"+getSessionPrefix(), userName);
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
                    String sessionKey = "osivia.nuxeoSession_virtualuser."+getSessionPrefix();
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
                    synchronized (getSessionCreationSynchronizer(portletCtx, sessionKey, ctx.getSatellite())) {

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
                  
                    if(nuxeoSession != null) {
	                    Long long1 = sessionsIdle.get(nuxeoSession.hashCode());
	                	if(long1 != null && System.currentTimeMillis() - long1 >= maxIdleTime) {
	                		
	                		String name = "shutdown";

                            String nuxeoSrc = "NUXEO/" + ctx.getSatellite().getId();
	                		
	                        profiler.logEvent(nuxeoSrc, name, System.currentTimeMillis() - long1, error);
	                		
	                		nuxeoSession.getClient().shutdown();
	                		sessionsIdle.remove(nuxeoSession.hashCode());
	                		nuxeoSession = null;
	                	}
                    }
                    

                    if (nuxeoSession == null) {
                        // logger.info("Creating nuxeo session for virtual user" + virtualUser);

                        INuxeoService nuxeoService = Locator.findMBean(INuxeoService.class, "osivia:service=NuxeoService");
                        nuxeoSession = nuxeoService.createUserSession(ctx.getSatellite(), virtualUser);
                        
                        long start = System.currentTimeMillis();
                        sessionsIdle.put(nuxeoSession.hashCode(), start);                        
                    }
                }

                logger.debug("Execution commande " + this.command.getId());
                

                
            

                long begin = 0;

                try {
                    synchronized (nuxeoSession) {
                        // v1.0.16 : déplacement création de session

                        begin = System.currentTimeMillis();
                        
                        ITransactionService transactionService = Locator.findMBean(ITransactionService.class, ITransactionService.MBEAN_NAME);
                        
                        if (transactionService.isStarted() && (transactionService.getResource("NUXEO") == null)) {

                            // Start Tx
                            Object object = nuxeoSession.newRequest("Repository.StartTransaction").execute();
                            if (object instanceof FileBlob) {
                                FileBlob txIdAsBlob = (FileBlob) object;
                                String txId = IOUtils.toString(txIdAsBlob.getStream(), "UTF-8");
                                
                                // and register
                                 transactionService.register("NUXEO", new TransactionResource(this.ctx, txId));
                             }
                        }
                        

                        
                        res = this.command.execute(nuxeoSession);
                        
                        
                        
                        
                    }
                } catch (Exception e) {

                    if (e.getCause() instanceof IllegalStateException  || (e.getCause() instanceof SocketTimeoutException))
                        recyclableSession = false;

                    error = true;
                    throw e;
                } finally {

                    long end = System.currentTimeMillis();
                    
                    
                    sessionsIdle.put(nuxeoSession.hashCode(), end);
                    
                    long elapsedTime = end - begin;

                    // LBI si la commande n'a pas d'ID, on lui propose l'instance java
                    String cmdId = this.command.getId();
                    if (this.command.getId() == null) {
                        cmdId = this.command.toString();
                    }

                    // log into profiler

                    String name = "id='" + cmdId + "',user='" + profilerUser + "'";

                    name += ", nuxeoSession=" + nuxeoSession.hashCode();

                    


                    // Moyenne flottante sur les publishInfosCommands
					if (ctx.getSatellite() == null) {

						String maxAverageDelay = System.getProperty("nuxeo.maxAverageDelayMs");

						if (maxAverageDelay != null) {
							long maxDelay = Long.parseLong(maxAverageDelay);

							if (!error && StringUtils.startsWith(this.command.getId(), "PublishInfosCommand")) {
								synchronized (AVERAGE_LIST) {

									while (AVERAGE_LIST.size() >= AVERAGE_SIZE) {
										AVERAGE_LIST.remove(0);
									}

									// On ignore les timeout genre 60000 car il n'illustre pas un comportement
									// progressif
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
											String statusMsg = "Floating average time : " + moyenne + "ms";

											if (this.getServiceStatut(this.ctx).isReady("NX-OVERLOAD")) {
												this.getServiceStatut(this.ctx).notifyError("NX-OVERLOAD",
														new UnavailableServer("[DOWN]" + statusMsg));
											}

											AVERAGE_LIST.clear();

										} else {
											if (!this.getServiceStatut(this.ctx).isReady("NX-OVERLOAD")) {
												String statusMsg = "Floating average time : " + moyenne + "ms";
												this.getServiceStatut(this.ctx).notifyError("NX-OVERLOAD",
														new UnavailableServer("[UP] " + statusMsg));
											}
										}
									}
								}
							}
						}
					}


                    String nuxeoSrc = "NUXEO/" + ctx.getSatellite().getId();
                    profiler.logEvent(nuxeoSrc, name, elapsedTime, error);
                      
                    
                    if( IPortalLogger.logger.isDebugEnabled()){
                        String commandName = "";
                        if(this.command.getId() != null)
                            commandName = this.command.getId().replaceAll("\"", "'");
                         

                        if( error == false)
                            IPortalLogger.logger.debug(new LoggerMessage("call to "+ nuxeoSrc +" \""+commandName +"\" " + elapsedTime));
                        else
                            IPortalLogger.logger.debug(new LoggerMessage("call to "+ nuxeoSrc +" \""+commandName +"\" " + elapsedTime + " \"an error as occured\""));
                           
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

                if (userSession != null && recyclableSession == false)	{
                	getUserSessions( userSession ).remove(getSessionKey());
                }


            }

            return res;
        } catch (Exception e) {
            throw PortalException.wrap(e);
        }
    }

}
