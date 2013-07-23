package fr.toutatice.portail.cms.nuxeo.services;

import java.io.Serializable;
import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletContext;
import javax.servlet.http.HttpSessionEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.system.ServiceMBeanSupport;
import org.nuxeo.ecm.automation.client.jaxrs.Session;
import org.nuxeo.ecm.automation.client.jaxrs.impl.HttpAutomationClient;
import org.nuxeo.ecm.automation.client.jaxrs.spi.auth.PortalSSOAuthInterceptor;
import org.osivia.portal.api.profiler.IProfilerService;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.core.cms.CMSBinaryContent;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSHandlerProperties;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSPage;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.ICMSService;
import org.osivia.portal.core.dynamic.DynamicPageBean;
import org.osivia.portal.core.profils.IProfilManager;


import fr.toutatice.portail.core.nuxeo.INuxeoCommandService;
import fr.toutatice.portail.core.nuxeo.INuxeoCustomizer;
import fr.toutatice.portail.core.nuxeo.INuxeoServiceCommand;
import fr.toutatice.portail.core.nuxeo.NuxeoCommandContext;
import fr.toutatice.portail.core.nuxeo.NuxeoCommandServiceFactory;
import fr.toutatice.portail.core.nuxeo.NuxeoConnectionProperties;

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
			session = client.getSession();

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
