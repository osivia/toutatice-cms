/**
 * 
 */
package fr.toutatice.portail.cms.nuxeo.portlets.service;

import net.sf.json.JSONObject;

import org.osivia.portal.core.cms.CMSExtendedDocumentInfos;


/**
 * Allows setting of internal Json flux.
 * 
 * @author david
 *
 */
public class InternalCMSExtendedDocumentInfos extends CMSExtendedDocumentInfos {

    /**
     * Constructor.
     */
    public InternalCMSExtendedDocumentInfos() {
        super();
    }
    
    /**
     * Setter for internal flux.
     */
    public void setFlux(JSONObject flux) {
        super.flux = flux;
    }
    
}
