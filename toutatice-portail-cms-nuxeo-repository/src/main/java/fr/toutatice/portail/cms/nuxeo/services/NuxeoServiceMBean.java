/**
 *
 */
package fr.toutatice.portail.cms.nuxeo.services;


import org.osivia.portal.api.profiler.IProfilerService;
import org.osivia.portal.spi.cms.ICMSIntegration;

import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoService;


/**
 * Nuxeo service MBean interface.
 * 
 * @see ServiceMBean
 * @see INuxeoService
 * @see ICMSIntegration
 */
public interface NuxeoServiceMBean extends  INuxeoService, ICMSIntegration {

    /**
     * Start service.
     *
     * @throws Exception
     */
    void startService() throws Exception;


    /**
     * Stop service.
     *
     * @throws Exception
     */
    void stopService() throws Exception;


    /**
     * Get profiler.
     *
     * @return profiler
     */
    IProfilerService getProfiler();


    /**
     * Set profiler.
     *
     * @param profiler profiler
     */
    void setProfiler(IProfilerService profiler);

}
