package fr.toutatice.portail.cms.nuxeo.portlets.service;

import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.batch.AbstractBatch;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.core.cms.ICMSService;
import org.osivia.portal.core.cms.ICMSServiceLocator;

import java.util.Map;

/**
 * Remove old temporary files every 10 minutes (or setted by nuxeo.tempfiles.cron).
 *
 * @author Loïc Billon
 */
public class CleanTempFilesBatch  extends AbstractBatch {

    @Override
    public String getJobScheduling() {

        return System.getProperty("nuxeo.tempfiles.cron", "0 0/10 * * * ?");

    }

    @Override
    public void execute(Map<String, Object> parameters) throws PortalException {

        ICMSServiceLocator mBean = Locator.findMBean(ICMSServiceLocator.class, ICMSServiceLocator.MBEAN_NAME);
        ICMSService cmsService = mBean.getCMSService();
        cmsService.cleanTempFiles();

    }

    @Override
    public boolean isRunningOnMasterOnly() {
        return false;
    }


}
