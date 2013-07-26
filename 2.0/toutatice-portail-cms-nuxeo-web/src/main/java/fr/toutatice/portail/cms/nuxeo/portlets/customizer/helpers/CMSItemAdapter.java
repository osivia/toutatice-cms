package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletContext;

import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSServiceCtx;

import fr.toutatice.portail.cms.nuxeo.portlets.customizer.DefaultCMSCustomizer;
import fr.toutatice.portail.cms.nuxeo.portlets.service.CMSService;

public class CMSItemAdapter {
	
	CMSService CMSService;
	DefaultCMSCustomizer customizer;
	PortletContext portletCtx;
	
	public CMSItemAdapter(PortletContext portletCtx, DefaultCMSCustomizer customizer, CMSService cmsService) {
		super();
		CMSService = cmsService;
		this.portletCtx = portletCtx;
		this.customizer = customizer;

	};
	
	
	/*
	 * Personnalisation des propriétés des éléments d'un CMSItem
	 * 

	 */

	public void adaptItem(CMSItem item) {

		Document doc = (Document) item.getNativeItem();
		
		Map<String, String> properties = item.getProperties();

		adaptDoc(doc, properties);
	}
	
	public  Map<String, String> adaptDocument(CMSServiceCtx ctx, Document doc) {

		Map<String, String> properties = new HashMap<String, String>();
		
		adaptDoc(doc, properties);
		
		return properties;
	}
	
	
	
	
	protected boolean supportsOnlyPortalContextualization(Document doc)	{
		if (doc.getType().equals("PortalPage")   || ((doc.getType().equals("PortalSite"))) || ((doc.getType().equals("PortalVirtualPage")))  )
			return true;
		
		return false;

	}
	
	
	
	public void adaptDoc(Document doc, Map<String, String> properties) {

		
		if( supportsOnlyPortalContextualization(doc))
			properties.put( "supportsOnlyPortalContextualization", "1");

		

	}


}
