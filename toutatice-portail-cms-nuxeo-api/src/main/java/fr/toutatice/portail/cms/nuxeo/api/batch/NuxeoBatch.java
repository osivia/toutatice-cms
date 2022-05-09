/**
 * 
 */
package fr.toutatice.portail.cms.nuxeo.api.batch;

import java.util.Map;
import java.util.UUID;

import javax.portlet.PortletContext;

import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.batch.AbstractBatch;
import org.osivia.portal.api.cache.services.CacheInfo;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.ICMSService;
import org.osivia.portal.core.cms.ICMSServiceLocator;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.forms.FormFilterException;
import fr.toutatice.portail.cms.nuxeo.api.forms.IFormsService;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandContext;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoServiceFactory;

/**
 * Utility class used to provide helpful methods in nuxeo and procedures batch processing
 * @author Loïc Billon
 *
 */
public abstract class NuxeoBatch extends AbstractBatch {

	private NuxeoController nuxeoController;
	
	private ICMSServiceLocator locator = Locator.findMBean(ICMSServiceLocator.class, ICMSServiceLocator.MBEAN_NAME);

	public NuxeoBatch() {
		super();
	}
	
	public NuxeoBatch(String batchId) {
		super(batchId);
	}


	protected NuxeoController getNuxeoController() {
		
		if(nuxeoController == null) {

			nuxeoController = new NuxeoController(getPortletContext());
	
			nuxeoController.setAuthType(NuxeoCommandContext.AUTH_TYPE_SUPERUSER);
			nuxeoController.setCacheType(CacheInfo.CACHE_SCOPE_NONE);
			nuxeoController.setForcePublicationInfosScope("superuser_context");
			
		}
		return nuxeoController;
	}
	

	/**
	 * @param string
	 * @param variables
	 * @throws FormFilterException 
	 * @throws PortalException 
	 */
	protected Map<String, String> startProcedure(String modelId, Map<String, String> variables) throws PortalException, FormFilterException {
		IFormsService formsService = NuxeoServiceFactory.getFormsService();
		PortalControllerContext portalControllerContext = new PortalControllerContext(getPortletContext(), null, null);
		return formsService.start(portalControllerContext, modelId, variables);
		
	}
	
	protected Map<String, String> proceed(String uuid, String action, Map<String, String> variables) throws PortalException, FormFilterException, CMSException {
		IFormsService formsService = NuxeoServiceFactory.getFormsService();
		PortalControllerContext portalControllerContext = new PortalControllerContext(getPortletContext(), null, null);
		
		Document task = getTask(uuid);
		
		if(task == null) {
			throw new CMSException(CMSException.ERROR_NOTFOUND);
		}
		return formsService.proceed(portalControllerContext, task, action, variables);
		
	}
	
	protected Document getTask(String uuid) throws CMSException {
		ICMSService cmsService = locator.getCMSService();
		CMSServiceCtx cmsContext = new CMSServiceCtx();
		cmsContext.setPortletCtx(getPortletContext());
        cmsContext.setScope("superuser_no_cache");
		
		return (Document) cmsService.getTask(cmsContext, null, null, UUID.fromString(uuid));

	}

	protected abstract PortletContext getPortletContext();
	
}
