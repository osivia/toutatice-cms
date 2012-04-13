package fr.toutatice.portail.cms.nuxeo.services;

import java.io.Serializable;
import java.net.URI;
import java.util.List;

import javax.servlet.http.HttpSessionEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.system.ServiceMBeanSupport;
import org.nuxeo.ecm.automation.client.jaxrs.Session;
import org.nuxeo.ecm.automation.client.jaxrs.impl.HttpAutomationClient;
import org.nuxeo.ecm.automation.client.jaxrs.spi.auth.PortalSSOAuthInterceptor;

import fr.toutatice.portail.api.profiler.IProfilerService;
import fr.toutatice.portail.core.cms.CMSException;
import fr.toutatice.portail.core.cms.CMSHandlerProperties;
import fr.toutatice.portail.core.cms.CMSItem;
import fr.toutatice.portail.core.cms.CMSServiceCtx;
import fr.toutatice.portail.core.cms.ICMSService;

import fr.toutatice.portail.core.nuxeo.INuxeoLinkHandler;
import fr.toutatice.portail.core.nuxeo.NuxeoConnectionProperties;

public class NuxeoService extends ServiceMBeanSupport implements NuxeoServiceMBean, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	private static Log logger = LogFactory.getLog(NuxeoService.class);

	INuxeoLinkHandler linkHandler;


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
				nuxeoUserId = "anonymous";

			String name = "createAutomationSession,user='" + nuxeoUserId + "'";

			profiler.logEvent("NUXEO", name, elapsedTime, error);

		}

		return session;

	}

	public void registerLinkHandler(INuxeoLinkHandler linkHandler) {
		this.linkHandler = linkHandler;

	}

	public INuxeoLinkHandler getLinkHandler() {
		return linkHandler;

	}


	public void sessionDestroyed(HttpSessionEvent sessionEvent) {

		Session session = (Session) sessionEvent.getSession().getAttribute("portal.session" + "pia.nuxeoSession");

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

	public void registerCMSService(ICMSService cmsService) {
		this.cmsService = cmsService;
		
	}

	public List<CMSItem> getChildren(CMSServiceCtx ctx, String path) throws CMSException{
		return cmsService.getChildren(ctx, path);
	}

	public CMSItem getContent(CMSServiceCtx ctx, String path) throws CMSException{

		return cmsService.getContent(ctx, path);
	}

	public CMSHandlerProperties getItemHandler(CMSServiceCtx ctx) throws CMSException {
		return  cmsService.getItemHandler(ctx);
	}




}
