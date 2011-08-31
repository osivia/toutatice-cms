package fr.toutatice.portail.cms.nuxeo.services;

import java.io.Serializable;
import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.portal.common.invocation.Scope;
import org.jboss.portal.core.aspects.server.UserInterceptor;
import org.jboss.portal.core.controller.ControllerCommand;
import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.identity.User;
import org.jboss.system.ServiceMBeanSupport;
import org.nuxeo.ecm.automation.client.jaxrs.Session;
import org.nuxeo.ecm.automation.client.jaxrs.impl.HttpAutomationClient;
import org.nuxeo.ecm.automation.client.jaxrs.spi.auth.PortalSSOAuthInterceptor;

import fr.toutatice.portail.api.contexte.PortalControllerContext;

public class NuxeoService extends ServiceMBeanSupport implements NuxeoServiceMBean, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static String nuxeoCtx = "/nuxeo";

	private static Log logger = LogFactory.getLog(NuxeoService.class);

	public void stopService() throws Exception {
		logger.info("Gestionnaire nuxeo arrete");

	}

	public void startService() throws Exception {
		logger.info("Gestionnaire nuxeo demarre");

	}

	public Session createUserSession(String userId) throws Exception {

		String nuxeoHost = System.getProperty("nuxeo.host");
		String nuxeoPort = System.getProperty("nuxeo.port");
		String secretKey = System.getProperty("nuxeo.secretKey");

		URI uri = new URI("http://" + nuxeoHost + ":" + nuxeoPort + nuxeoCtx);

		HttpAutomationClient client = new HttpAutomationClient(uri.toString() + "/site/automation");

		if (userId != null)
			client.setRequestInterceptor(new PortalSSOAuthInterceptor(secretKey, userId));
		Session session = client.getSession();

		return session;

	}


}
