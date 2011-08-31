package fr.toutatice.portail.cms.nuxeo.portlets.bridge;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceURL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.PortalObjectPath;
import org.jboss.portal.core.model.portal.Window;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;

import fr.toutatice.portail.api.contexte.PortalControllerContext;
import fr.toutatice.portail.api.urls.IPortalUrlFactory;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.portlets.list.ViewListPortlet;

public class TransformationContext extends NuxeoController {

	private static Log log = LogFactory.getLog(TransformationContext.class);

	PortletRequest request;
	RenderResponse response;
	PortletContext portletCtx;
	IPortalUrlFactory urlFactory;
	String pageId;



	public TransformationContext(PortletRequest request, RenderResponse response, PortletContext portletCtx) {
		super( request, response, portletCtx);
		this.request = request;
		this.response = response;
		this.portletCtx = portletCtx;
	}
	
	public String getPageId() {
		if (pageId == null) {
			Window window = (Window) request.getAttribute("pia.window");
			Page page = (Page) window.getParent();
			try {
				pageId = URLEncoder.encode(page.getId().toString(PortalObjectPath.SAFEST_FORMAT), "UTF-8");
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		}
		return pageId;
	}
	

	public String createCMSLink(String uid) {
		
		Map<String, String> windowProperties = new HashMap<String, String>();
		windowProperties.put("pia.cms.scope", getScope());
		windowProperties.put("pia.cms.uri", uid);
		windowProperties.put("pia.hideDecorators", "1");	
		windowProperties.put("theme.dyna.partial_refresh_enabled", "false");		
		
		Map<String, String> params = new HashMap<String, String>();

		String url = getPortalUrlFactory().getStartProcUrl(new PortalControllerContext(portletCtx, request,
				response), getPageId(), "toutatice-portail-cms-nuxeo-viewDocumentPortletInstance", "virtual", "cms", windowProperties, params);

		return url;
	}
	
	public String createFileFolderLink(Document doc) {
		
		
		Map<String, String> windowProperties = new HashMap<String, String>();
		windowProperties.put("pia.cms.uri", doc.getPath());
		windowProperties.put("pia.hideDecorators", "1");
		windowProperties.put("theme.dyna.partial_refresh_enabled", "false");			
		windowProperties.put("pia.cms.scope", getScope());
		windowProperties.put("pia.title", "Dossier " + doc.getTitle());

		
		Map<String, String> params = new HashMap<String, String>();
						
		String url = getPortalUrlFactory().getStartProcUrl(new PortalControllerContext(portletCtx, request,
			response), getPageId(), "toutatice-portail-cms-nuxeo-fileBrowserPortletInstance", "virtual", "portalServiceWindow", windowProperties, params);

		return url;
	}
	
	
	public String createAnnonceFolderLink(Document doc) {
	
	Map<String, String> windowProperties = new HashMap<String, String>();
	windowProperties.put("pia.nuxeoRequest", "ecm:path STARTSWITH '"+doc.getPath()+"' ");
	windowProperties.put("pia.cms.style", ViewListPortlet.STYLE_EDITORIAL);
	windowProperties.put("pia.hideDecorators", "1");
	windowProperties.put("theme.dyna.partial_refresh_enabled", "false");		
	windowProperties.put("pia.cms.scope", getScope());
	windowProperties.put("pia.title", "Annonces " + doc.getTitle());	

	
	Map<String, String> params = new HashMap<String, String>();
	
	String url = getPortalUrlFactory().getStartProcUrl(new PortalControllerContext(portletCtx, request,
		response), getPageId(), "toutatice-portail-cms-nuxeo-viewListePortletInstance", "virtual", "portalServiceWindow", windowProperties, params);

	return url;
	}
	
	

	
	
	public String createDocumentUrlContainerLink(Document doc) {
		
		Map<String, String> windowProperties = new HashMap<String, String>();
		windowProperties.put("pia.nuxeoRequest", "ecm:path STARTSWITH '"+doc.getPath()+"' ");
		windowProperties.put("pia.cms.style", ViewListPortlet.STYLE_EDITORIAL);
		windowProperties.put("pia.hideDecorators", "1");
		windowProperties.put("theme.dyna.partial_refresh_enabled", "false");		
		windowProperties.put("pia.cms.scope", getScope());
		windowProperties.put("pia.title", "Liste de liens");	

		
		Map<String, String> params = new HashMap<String, String>();
		
		String url = getPortalUrlFactory().getStartProcUrl(new PortalControllerContext(portletCtx, request,
			response), getPageId(), "toutatice-portail-cms-nuxeo-viewListePortletInstance", "virtual", "portalServiceWindow", windowProperties, params);

		return url;
		}


	public String createExternalLink(Document doc) {

		ResourceURL resourceURL = response.createResourceURL();
		resourceURL.setResourceID(doc.getId());
		resourceURL.setParameter("type", "link");
		// ne marche pas : bug JBP
		// resourceURL.setCacheability(ResourceURL.PORTLET);

		return resourceURL.toString();
	}


	
	public String createPortletLink(String portletInstance, String uid) {
		
		Map<String, String> windowProperties = new HashMap<String, String>();
		windowProperties.put("pia.cms.scope", getScope());
		windowProperties.put("pia.cms.uri", uid);
		windowProperties.put("pia.hideDecorators", "1");
		windowProperties.put("theme.dyna.partial_refresh_enabled", "false");			
		
		Map<String, String> params = new HashMap<String, String>();

		String url = getPortalUrlFactory().getStartProcUrl(new PortalControllerContext(portletCtx, request,
				response), getPageId(), portletInstance, "virtual", "cms", windowProperties, params);


		return url;
	}
	
	public String createWindowLink( String uid) {
	 return	createPortletLink("toutatice-portail-cms-nuxeo-viewDocumentPortletInstance", uid);
	
	}

	
	
	
	public ViewContentLink createLink(Document doc) {

		String url = null;
		boolean externalLink = false;

		if ("ContextualLink".equals(doc.getType())) {
			url = createExternalLink(doc);
			externalLink = true;
		} else if ("File".equals(doc.getType()))  {
			url = createFileLink(doc, "file:content");
		} else if ("Folder".equals(doc.getType()) || "OrderedFolder".equals(doc.getType())){
			url = createFileFolderLink(doc);
		} else if ("AnnonceFolder".equals(doc.getType())){
			url = createAnnonceFolderLink(doc);
		} else if ("FaqFolder".equals(doc.getType())){
			// TODO : factory
			url = createPortletLink("toutatice-faq-portletInstance", doc.getPath());
			externalLink = false;
		} else if ("DocumentUrlContainer".equals(doc.getType())){
			url = createDocumentUrlContainerLink(doc);
		} else if ("Forum".equals(doc.getType())){
			url = getNuxeoBaseUri().toString()+"/nxdoc/default/"+doc.getId()+"/view_documents";
			externalLink = true;
		} else if ("PictureBook".equals(doc.getType())){
			url = getNuxeoBaseUri().toString()+"/nxdoc/default/"+doc.getId()+"/view_documents?tabId=tab_slideshow";
			externalLink = true;
		} else if ("Question".equals(doc.getType())){
			// TODO : factory
			url = createPortletLink("toutatice-faq-portletInstance", doc.getPath());
			externalLink = false;
		} else if ("Note".equals(doc.getType()) || ("Annonce".equals(doc.getType()))){
			// types supportés par le CMS du portail
			url = createCMSLink(doc.getPath());
		}else {
			// Par défaut, lien direct sur Nuxeo
			url = getNuxeoBaseUri().toString()+"/nxdoc/default/"+doc.getId()+"/view_documents";
			externalLink = true;
		}

		return new ViewContentLink(url, externalLink);
	}

}
