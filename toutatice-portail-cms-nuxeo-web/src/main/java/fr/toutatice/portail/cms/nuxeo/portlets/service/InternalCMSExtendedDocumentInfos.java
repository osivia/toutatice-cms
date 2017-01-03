/**
 * 
 */
package fr.toutatice.portail.cms.nuxeo.portlets.service;

import org.osivia.portal.core.cms.CMSExtendedDocumentInfos;

import net.sf.json.JSONObject;


/**
 * Allows setting of internal JSON flux.
 * 
 * @author David Chevrier
 * @see CMSExtendedDocumentInfos
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
     * 
     * @param flux JSON flux
     */
    public void setFlux(JSONObject flux) {
        super.setFlux(flux);
    }
    
}
