/**
 * 
 */
package fr.toutatice.portail.cms.nuxeo.portlets.service;

import net.sf.json.JSONObject;

import org.osivia.portal.core.cms.CMSPublicationInfos;


/**
 * Allows setting of internal Json flux.
 * 
 * @author david
 *
 */
public class InternalCMSPublicationInfos extends CMSPublicationInfos {

    /**
     * Constructor.
     */
    public InternalCMSPublicationInfos() {
        super();
    }

    
    /**
     * Setter for internal flux.
     */
    public void setFlux(JSONObject flux) {
        super.flux = flux;
    }
    
}
