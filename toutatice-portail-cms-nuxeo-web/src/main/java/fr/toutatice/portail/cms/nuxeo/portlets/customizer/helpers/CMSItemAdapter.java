package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletContext;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSServiceCtx;

import fr.toutatice.portail.cms.nuxeo.portlets.customizer.DefaultCMSCustomizer;
import fr.toutatice.portail.cms.nuxeo.portlets.service.CMSService;

public class CMSItemAdapter {
	
	protected CMSService CMSService;
	protected DefaultCMSCustomizer customizer;
	protected PortletContext portletCtx;
	
	public CMSItemAdapter(PortletContext portletCtx, DefaultCMSCustomizer customizer, CMSService cmsService) {
		super();
		CMSService = cmsService;
		this.portletCtx = portletCtx;
		this.customizer = customizer;

	};
	
	// TODO : remonter dans ICMSService
	public static String  computeNavPath(String path){
		String result = path;
		if( path.endsWith(".proxy"))
			result = result.substring(0, result.length() - 6);
		return result;
	}
	
	
	/*
	 * Personnalisation des propriétés des éléments d'un CMSItem
	 * 

	 */

	public void adaptItem(CMSServiceCtx ctx, CMSItem item) throws Exception{

		Document doc = (Document) item.getNativeItem();
		
		Map<String, String> properties = item.getProperties();

		adaptDoc(ctx, doc, properties);
	}
	
	public  Map<String, String> adaptDocument(CMSServiceCtx ctx, Document doc) throws Exception {

		Map<String, String> properties = new HashMap<String, String>();
		
		adaptDoc(ctx, doc, properties);
		
		return properties;
	}
	
	
	
	
	public boolean supportsOnlyPortalContextualization(CMSServiceCtx ctx, Document doc)	{
		if (doc.getType().equals("PortalPage")   || ((doc.getType().equals("PortalSite"))) || ((doc.getType().equals("PortalVirtualPage")))  )
			return true;
		
		return false;

	}
	
	
	
	public void adaptNavigationProperties(CMSServiceCtx ctx, Document doc, Map<String, String> properties)	throws Exception {
		return;

	}
	
	public void adaptDoc(CMSServiceCtx ctx, Document doc, Map<String, String> properties) throws Exception {

		
		if( supportsOnlyPortalContextualization(ctx, doc))
			properties.put( "supportsOnlyPortalContextualization", "1");
		
		
		adaptNavigationProperties(ctx, doc, properties);

		

	}


}
