package fr.toutatice.portail.cms.nuxeo.portlets.customizer;

import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceURL;

import org.nuxeo.ecm.automation.client.jaxrs.model.Document;

import fr.toutatice.portail.api.contexte.PortalControllerContext;
import fr.toutatice.portail.api.urls.IPortalUrlFactory;
import fr.toutatice.portail.api.urls.Link;
import fr.toutatice.portail.core.nuxeo.INuxeoLinkHandler;
import fr.toutatice.portail.core.nuxeo.LinkHandlerCtx;



public class DefaultLinkHandler implements INuxeoLinkHandler {
	
	PortletContext portletCtx;
	protected IPortalUrlFactory portalUrlFactory;	

	public DefaultLinkHandler(PortletContext ctx) {
		super();
		this.portletCtx = ctx;
		this.portalUrlFactory = (IPortalUrlFactory) portletCtx.getAttribute("UrlService");		
	}
	
	public String createExternalLink(LinkHandlerCtx ctx) {

		ResourceURL resourceURL = ctx.getResponse().createResourceURL();
		resourceURL.setResourceID(ctx.getDoc().getId());
		resourceURL.setParameter("type", "link");
		// ne marche pas : bug JBP
		// resourceURL.setCacheability(ResourceURL.PORTLET);

		return resourceURL.toString();
	}
	
	
	public String createCMSLink(LinkHandlerCtx ctx)throws Exception {
		
		Map<String, String> windowProperties = new HashMap<String, String>();
		windowProperties.put("pia.cms.scope", ctx.getScope());
		windowProperties.put("pia.cms.displayLiveVersion", ctx.getDisplayLiveVersion());
		windowProperties.put("pia.cms.uri", ctx.getDoc().getPath());
		windowProperties.put("pia.cms.publishPathAlreadyConverted", "1");		
		windowProperties.put("pia.hideDecorators", "1");	
		windowProperties.put("theme.dyna.partial_refresh_enabled", "false");		
		
		Map<String, String> params = new HashMap<String, String>();

		String url = portalUrlFactory.getStartProcUrl(new PortalControllerContext(ctx.getPortletCtx(), ctx.getRequest(),
				ctx.getResponse()), ctx.getPageId(), "toutatice-portail-cms-nuxeo-viewDocumentPortletInstance", "virtual", "cms", windowProperties, params);

		return url;
	}

	public String createFileFolderLink(LinkHandlerCtx ctx) {
		
		Map<String, String> windowProperties = new HashMap<String, String>();
		windowProperties.put("pia.cms.uri", ctx.getDoc().getPath());
		windowProperties.put("pia.cms.publishPathAlreadyConverted", "1");		
		windowProperties.put("pia.hideDecorators", "1");
		windowProperties.put("theme.dyna.partial_refresh_enabled", "false");			
		windowProperties.put("pia.cms.scope", ctx.getScope());
		windowProperties.put("pia.cms.displayLiveVersion", ctx.getDisplayLiveVersion());		
		windowProperties.put("pia.title", "Dossier " + ctx.getDoc().getTitle());

		
		Map<String, String> params = new HashMap<String, String>();
						
		String url = portalUrlFactory.getStartProcUrl(new PortalControllerContext(ctx.getPortletCtx(), ctx.getRequest(),
				ctx.getResponse()), ctx.getPageId(), "toutatice-portail-cms-nuxeo-fileBrowserPortletInstance", "virtual", "portalServiceWindow", windowProperties, params);
		
		return url;
	}
	
	
	public String createAnnonceFolderLink(LinkHandlerCtx ctx)throws Exception{
		
		Map<String, String> windowProperties = new HashMap<String, String>();
		windowProperties.put("pia.nuxeoRequest", "ecm:path STARTSWITH '"+ ctx.getDoc().getPath()+"' ");
		windowProperties.put("pia.cms.style", DefaultListTemplatesHandler.STYLE_EDITORIAL);
		windowProperties.put("pia.hideDecorators", "1");
		windowProperties.put("theme.dyna.partial_refresh_enabled", "false");		
		windowProperties.put("pia.cms.scope",  ctx.getScope());
		windowProperties.put("pia.cms.displayLiveVersion", ctx.getDisplayLiveVersion());		
		windowProperties.put("pia.title", "Annonces " + ctx.getDoc().getTitle());	

		
		Map<String, String> params = new HashMap<String, String>();
		
		String url = portalUrlFactory.getStartProcUrl(new PortalControllerContext(ctx.getPortletCtx(), ctx.getRequest(),
				ctx.getResponse()), ctx.getPageId(), "toutatice-portail-cms-nuxeo-viewListPortletInstance", "virtual", "portalServiceWindow", windowProperties, params);

		return url;
		}
	
	public String createFileContentLink(LinkHandlerCtx ctx) {

		ResourceURL resourceURL = ctx.getResponse().createResourceURL();
		resourceURL.setResourceID(ctx.getDoc().getId() + "/" + "file:content");
		resourceURL.setParameter("type", "file");
		resourceURL.setParameter("docPath", ctx.getDoc().getPath());
		resourceURL.setParameter("fieldName", "file:content");
		// ne marche pas : bug JBP
		// resourceURL.setCacheability(ResourceURL.PORTLET);
		resourceURL.setCacheability(ResourceURL.PAGE);

		return resourceURL.toString();
	}	
	
	
	public String createDocumentUrlContainerLink(LinkHandlerCtx ctx) throws Exception {
		
		Map<String, String> windowProperties = new HashMap<String, String>();
		windowProperties.put("pia.nuxeoRequest", "ecm:path STARTSWITH '"+ctx.getDoc().getPath()+"' ");
		windowProperties.put("pia.cms.style", DefaultListTemplatesHandler.STYLE_EDITORIAL);
		windowProperties.put("pia.hideDecorators", "1");
		windowProperties.put("theme.dyna.partial_refresh_enabled", "false");		
		windowProperties.put("pia.cms.scope", ctx.getScope());
		windowProperties.put("pia.cms.displayLiveVersion", ctx.getDisplayLiveVersion());		
		windowProperties.put("pia.title", "Liste de liens");	

		
		Map<String, String> params = new HashMap<String, String>();
		
		String url = portalUrlFactory.getStartProcUrl(new PortalControllerContext(ctx.getPortletCtx(), ctx.getRequest(),
				ctx.getResponse()), ctx.getPageId(), "toutatice-portail-cms-nuxeo-viewListPortletInstance", "virtual", "portalServiceWindow", windowProperties, params);

		return url;
		}

	
	public String createPortletLink(LinkHandlerCtx ctx, String portletInstance, String uid) throws Exception {
		
		Map<String, String> windowProperties = new HashMap<String, String>();
		windowProperties.put("pia.cms.scope", ctx.getScope());
		windowProperties.put("pia.cms.displayLiveVersion", ctx.getDisplayLiveVersion());		
		windowProperties.put("pia.cms.uri", uid);
		windowProperties.put("pia.cms.publishPathAlreadyConverted", "1");		
		windowProperties.put("pia.hideDecorators", "1");
		windowProperties.put("theme.dyna.partial_refresh_enabled", "false");			
		
		Map<String, String> params = new HashMap<String, String>();

		String url = portalUrlFactory.getStartProcUrl(new PortalControllerContext(ctx.getPortletCtx(), ctx.getRequest(),
				ctx.getResponse()), ctx.getPageId(), portletInstance, "virtual", "cms", windowProperties, params);


		return url;
	}
	
	
	public Link getLink(LinkHandlerCtx ctx)	throws Exception  {
			

		Document doc = ctx.getDoc();

		String url = null;
		boolean externalLink = false;
		

	if ("ContextualLink".equals(doc.getType())) {
		url = createExternalLink(ctx);
		externalLink = true;
	} else if ("File".equals(doc.getType()))  {
		url = createFileContentLink(ctx);
	} else if ("Folder".equals(doc.getType()) || "OrderedFolder".equals(doc.getType())){
		url = createFileFolderLink(ctx);
	} else if ("AnnonceFolder".equals(doc.getType())){
		url = createAnnonceFolderLink(ctx);
	} else if ("DocumentUrlContainer".equals(doc.getType())){
		url = createDocumentUrlContainerLink(ctx);
	} else if ("Forum".equals(doc.getType())){
		url = ctx.getNuxeoBaseUri().toString()+"/nxdoc/default/"+doc.getId()+"/view_documents";
		externalLink = true;
	} else if ("PictureBook".equals(doc.getType())){
		url = ctx.getNuxeoBaseUri().toString()+"/nxdoc/default/"+doc.getId()+"/view_documents?tabId=tab_slideshow";
		externalLink = true;
	} else if ("Note".equals(doc.getType()) || ("Annonce".equals(doc.getType()))){
		// types supportés par le CMS du portail
		url = createCMSLink(ctx);
	} else {
		// Par défaut, lien direct sur Nuxeo
		url = ctx.getNuxeoBaseUri().toString()+"/nxdoc/default/"+doc.getId()+"/view_documents";
		externalLink = true;
	}

	return new Link (url, externalLink);
	}

	
	
	
	
}
