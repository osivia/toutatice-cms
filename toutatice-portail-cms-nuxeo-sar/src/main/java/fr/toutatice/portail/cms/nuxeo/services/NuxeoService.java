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

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.Map;

import javax.portlet.PortletContext;
import javax.servlet.http.HttpSessionEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.system.ServiceMBeanSupport;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.jaxrs.impl.HttpAutomationClient;
import org.nuxeo.ecm.automation.client.jaxrs.spi.auth.PortalSSOAuthInterceptor;
import org.osivia.portal.api.profiler.IProfilerService;

import fr.toutatice.portail.cms.nuxeo.api.forms.IFormsService;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCommandService;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCustomizer;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoConnectionProperties;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoSatelliteConnectionProperties;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoServiceInvocationHandler;
import fr.toutatice.portail.cms.nuxeo.api.services.tag.INuxeoTagService;

/**
 * Nuxeo service implementation.
 *
 * @see ServiceMBeanSupport
 * @see NuxeoServiceMBean
 * @see Serializable
 */
public class NuxeoService extends ServiceMBeanSupport implements NuxeoServiceMBean, Serializable {

    /** Default serial version ID. */
    private static final long serialVersionUID = 1L;

    /** Logger. */
    private static Log logger = LogFactory.getLog(NuxeoService.class);

    /** CMS customizer. */
    private INuxeoCustomizer cmsCustomizer;

    /** Profiler. */
    private transient IProfilerService profiler;

    /** Tag service. */
    private final INuxeoTagService tagService;
    /** Forms service. */
    private final IFormsService formsService;


    /**
     * Constructor.
     */
    public NuxeoService() {
        super();

        // Tag service
        this.tagService = createProxy(INuxeoTagService.class);
        // Forms service proxy
        this.formsService = createProxy(IFormsService.class);
    }


    /**
     * Create proxy.
     * 
     * @param clazz proxy class
     * @return proxy
     */
    @SuppressWarnings("unchecked")
    private <T> T createProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new NuxeoServiceInvocationHandler());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public IProfilerService getProfiler() {
        return this.profiler;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setProfiler(IProfilerService profiler) {
        this.profiler = profiler;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void stopService() throws Exception {
        logger.info("Gestionnaire nuxeo arrete");
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void startService() throws Exception {
        logger.info("Gestionnaire nuxeo demarre");
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Session createUserSession(String satelliteName, String userId) throws Exception {
        long begin = System.currentTimeMillis();
        boolean error = false;

        Session session = null;

        try {
            String secretKey = System.getProperty("nuxeo.secretKey");

            URI uri = NuxeoSatelliteConnectionProperties.getConnectionProperties(satelliteName).getPrivateBaseUri();

            HttpAutomationClient client = new HttpAutomationClient(uri.toString() + "/site/automation");

            if (userId != null) {
                client.setRequestInterceptor(new PortalSSOAuthInterceptor(secretKey, userId));
            }
            session = client.getSession();
        } catch (Exception e) {
            error = true;
            throw e;
        } finally {
            // log into profiler
            long end = System.currentTimeMillis();
            long elapsedTime = end - begin;

            String nuxeoUserId = userId;
            if (nuxeoUserId == null) {
                nuxeoUserId = "null";
            }

            String name = "createAutomationSession,user='" + nuxeoUserId;
            
            if(session != null) {
            	name += ", nuxeoSession=" + session.hashCode();
            }

            this.profiler.logEvent("NUXEO", name, elapsedTime, error);
        }

        return session;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void registerCMSCustomizer(INuxeoCustomizer CMSCustomizer) {
        this.cmsCustomizer = CMSCustomizer;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public INuxeoCustomizer getCMSCustomizer() {
        return this.cmsCustomizer;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public INuxeoTagService getTagService() {
        return this.tagService;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public IFormsService getFormsService() {
        return this.formsService;
    }


    /**
     * {@inheritDoc}
     */
    @Override
	public void sessionDestroyed(HttpSessionEvent sessionEvent) {
		Map<String, Session> sessions = NuxeoCommandCacheInvoker.getUserSessions(sessionEvent.getSession());
		if (sessions != null) {
			for (Session session : sessions.values()) {
				long begin = System.currentTimeMillis();
				boolean error = false;

				try {
					session.getClient().shutdown();
				} finally {
					// log into profiler
					long end = System.currentTimeMillis();
					long elapsedTime = end - begin;

					String name = "shutdown";

					this.profiler.logEvent("NUXEO", name, elapsedTime, error);
				}
			}
		}

		this.getCMSCustomizer().sessionDestroyed(sessionEvent);
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public INuxeoCommandService startNuxeoCommandService(PortletContext portletCtx) throws Exception {
        return new NuxeoCommandService();
    }

}
