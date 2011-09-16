/**
 * 
 */
package fr.toutatice.portail.cms.nuxeo.services;

import org.jboss.system.ServiceMBean;

import fr.toutatice.portail.core.nuxeo.INuxeoService;




/**
 * @author jss
 *
 */
public interface NuxeoServiceMBean extends ServiceMBean,INuxeoService {

	public void startService()throws Exception;
	
	public void stopService()throws Exception;
}
