package fr.toutatice.portail.cms.nuxeo.services;

import java.io.Serializable;
import java.net.URI;

import javax.portlet.PortletContext;
import javax.servlet.http.HttpSessionEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.system.ServiceMBeanSupport;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.jaxrs.impl.HttpAutomationClient;
import org.nuxeo.ecm.automation.client.jaxrs.spi.auth.PortalSSOAuthInterceptor;
import org.osivia.portal.api.profiler.IProfilerService;
import org.osivia.portal.core.cms.ICMSService;

import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCommandService;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCustomizer;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoConnectionProperties;

public class NuxeoService extends ServiceMBeanSupport implements NuxeoServiceMBean, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	private static Log logger = LogFactory.getLog(NuxeoService.class);

	INuxeoCustomizer nuxeoCustomizer;


	ICMSService cmsService;

	private transient IProfilerService profiler;

	public IProfilerService getProfiler() {
		return profiler;
	}

	public void setProfiler(IProfilerService profiler) {
		this.profiler = profiler;
	}

	public void stopService() throws Exception {
		logger.info("Gestionnaire nuxeo arrete");

	}

	public void startService() throws Exception {
		logger.info("Gestionnaire nuxeo demarre");

	}

	public Session createUserSession(String userId) throws Exception {

		long begin = System.currentTimeMillis();
		boolean error = false;
		
		Session session = null;

		try {

			String secretKey = System.getProperty("nuxeo.secretKey");

			URI uri = NuxeoConnectionProperties.getPrivateBaseUri();

			HttpAutomationClient client = new HttpAutomationClient(uri.toString() + "/site/automation");

			if (userId != null)
				client.setRequestInterceptor(new PortalSSOAuthInterceptor(secretKey, userId));
			session = (Session) client.getSession();

		} catch (Exception e) {
			error = true;
			throw e;
		} finally {

			// log into profiler
			long end = System.currentTimeMillis();
			long elapsedTime = end - begin;
			
			String nuxeoUserId = userId;
			if( nuxeoUserId == null)
				nuxeoUserId = "null";

			String name = "createAutomationSession,user='" + nuxeoUserId + "'";

			profiler.logEvent("NUXEO", name, elapsedTime, error);

		}

		return session;

	}

	public void registerCMSCustomizer(INuxeoCustomizer CMSCustomizer) {
		this.nuxeoCustomizer = CMSCustomizer;

	}

	public INuxeoCustomizer getCMSCustomizer() {
		return nuxeoCustomizer;

	}


	public void sessionDestroyed(HttpSessionEvent sessionEvent) {

		Session session = (Session) sessionEvent.getSession().getAttribute("portal.session" + "osivia.nuxeoSession");

		if (session != null) {
			
			long begin = System.currentTimeMillis();
			boolean error = false;
			
			try {
				session.getClient().shutdown();
			} finally {

				// log into profiler
				long end = System.currentTimeMillis();
				long elapsedTime = end - begin;

				String name = "shutdown";

				profiler.logEvent("NUXEO", name, elapsedTime, error);

			}			

		}

	}

    public INuxeoCommandService startNuxeoCommandService(PortletContext portletCtx) throws Exception {
        return new NuxeoCommandService();
    }




}
