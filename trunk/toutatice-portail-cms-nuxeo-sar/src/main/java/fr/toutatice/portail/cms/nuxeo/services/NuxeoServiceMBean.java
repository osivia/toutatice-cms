/**
 * 
 */
package fr.toutatice.portail.cms.nuxeo.services;

import org.jboss.system.ServiceMBean;

import fr.toutatice.portail.api.profiler.IProfilerService;
import fr.toutatice.portail.core.cms.spi.ICMSIntegration;
import fr.toutatice.portail.core.nuxeo.INuxeoService;




/**
 * @author jss
 *
 */
public interface NuxeoServiceMBean extends ServiceMBean,INuxeoService,ICMSIntegration {

	public void startService()throws Exception;
	
	public void stopService()throws Exception;
	
	public IProfilerService getProfiler() ;

	public void setProfiler(IProfilerService profiler) ;

}
