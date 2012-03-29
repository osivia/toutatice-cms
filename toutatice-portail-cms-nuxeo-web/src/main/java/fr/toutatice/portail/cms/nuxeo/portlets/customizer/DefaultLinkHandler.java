package fr.toutatice.portail.cms.nuxeo.portlets.customizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceURL;
import javax.servlet.http.HttpServletRequest;

import org.jboss.portal.core.model.instance.InstanceDefinition;
import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.Portal;
import org.jboss.portal.core.model.portal.PortalObject;
import org.jboss.portal.core.model.portal.PortalObjectPath;
import org.jboss.portal.core.model.portal.Window;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;

import fr.toutatice.portail.api.contexte.PortalControllerContext;
import fr.toutatice.portail.api.urls.IPortalUrlFactory;
import fr.toutatice.portail.api.urls.Link;
import fr.toutatice.portail.api.windows.PortalWindow;
import fr.toutatice.portail.api.windows.WindowFactory;
import fr.toutatice.portail.cms.nuxeo.api.PageSelectors;

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

	public String createCMSLink(LinkHandlerCtx ctx) throws Exception {



		Map<String, String> pageParams = new HashMap<String, String>();
		
		
		/*
		Map<String, List<String>> selectors = PageSelectors
		.decodeProperties(ctx.getRequest().getParameter("selectors"));
		
		
		if (selectors != null && selectors.size() > 0)
			pageParams.put("selectors", PageSelectors.encodeProperties(selectors));

*/
		String pageId = PortalObjectPath.parse(ctx.getPageId(), PortalObjectPath.SAFEST_FORMAT).toString(
				PortalObjectPath.CANONICAL_FORMAT);

		Link link = new Link(portalUrlFactory.getCMSUrl(
				new PortalControllerContext(ctx.getPortletCtx(), ctx.getRequest(), ctx.getResponse()), pageId, ctx
						.getDoc().getPath(), pageParams,null), false);

		return link.getUrl();

		/*
		 * Map<String, String> windowProperties = new HashMap<String, String>();
		 * 
		 * windowProperties.put("pia.cms.scope", ctx.getScope());
		 * 
		 * windowProperties.put("pia.cms.displayLiveVersion",
		 * ctx.getDisplayLiveVersion());
		 * windowProperties.put("pia.cms.hideMetaDatas",
		 * ctx.getHideMetaDatas()); windowProperties.put("pia.cms.uri",
		 * ctx.getDoc().getPath());
		 * windowProperties.put("pia.cms.publishPathAlreadyConverted", "1");
		 * windowProperties.put("pia.hideDecorators", "1");
		 * windowProperties.put("theme.dyna.partial_refresh_enabled", "false");
		 * 
		 * 
		 * Map<String, String> params = new HashMap<String, String>();
		 * 
		 * String url = portalUrlFactory.getStartProcUrl(new
		 * PortalControllerContext(ctx.getPortletCtx(), ctx.getRequest(),
		 * ctx.getResponse()), ctx.getPageId(),
		 * "toutatice-portail-cms-nuxeo-viewDocumentPortletInstance", "virtual",
		 * "cms", windowProperties, params);
		 * 
		 * return url;
		 */
	}

	public String createFileFolderLink(LinkHandlerCtx ctx) {

		Map<String, String> windowProperties = new HashMap<String, String>();
		windowProperties.put("pia.cms.uri", ctx.getDoc().getPath());
		windowProperties.put("pia.cms.publishPathAlreadyConverted", "1");
		windowProperties.put("pia.hideDecorators", "1");
		windowProperties.put("theme.dyna.partial_refresh_enabled", "false");
		windowProperties.put("pia.cms.scope", ctx.getScope());
		windowProperties.put("pia.cms.displayLiveVersion", ctx.getDisplayLiveVersion());
		windowProperties.put("pia.cms.hideMetaDatas", ctx.getHideMetaDatas());
		windowProperties.put("pia.title", "Dossier " + ctx.getDoc().getTitle());

		Map<String, String> params = new HashMap<String, String>();

		String url = portalUrlFactory.getStartProcUrl(new PortalControllerContext(ctx.getPortletCtx(),
				ctx.getRequest(), ctx.getResponse()), ctx.getPageId(),
				"toutatice-portail-cms-nuxeo-fileBrowserPortletInstance", "virtual", "portalServiceWindow",
				windowProperties, params);

		return url;
	}

	public String createAnnonceFolderLink(LinkHandlerCtx ctx) throws Exception {

		Map<String, String> windowProperties = new HashMap<String, String>();
		windowProperties.put("pia.nuxeoRequest", "ecm:path STARTSWITH '" + ctx.getDoc().getPath() + "' ");
		windowProperties.put("pia.cms.style", CMSCustomizer.STYLE_EDITORIAL);
		windowProperties.put("pia.hideDecorators", "1");
		windowProperties.put("theme.dyna.partial_refresh_enabled", "false");
		windowProperties.put("pia.cms.scope", ctx.getScope());
		windowProperties.put("pia.cms.displayLiveVersion", ctx.getDisplayLiveVersion());
		windowProperties.put("pia.cms.hideMetaDatas", "1");
		windowProperties.put("pia.title", "Annonces " + ctx.getDoc().getTitle());

		Map<String, String> params = new HashMap<String, String>();

		String url = portalUrlFactory.getStartProcUrl(new PortalControllerContext(ctx.getPortletCtx(),
				ctx.getRequest(), ctx.getResponse()), ctx.getPageId(),
				"toutatice-portail-cms-nuxeo-viewListPortletInstance", "virtual", "portalServiceWindow",
				windowProperties, params);

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
		windowProperties.put("pia.nuxeoRequest", "ecm:path STARTSWITH '" + ctx.getDoc().getPath() + "' ");
		windowProperties.put("pia.cms.style", CMSCustomizer.STYLE_EDITORIAL);
		windowProperties.put("pia.hideDecorators", "1");
		windowProperties.put("theme.dyna.partial_refresh_enabled", "false");
		windowProperties.put("pia.cms.scope", ctx.getScope());
		windowProperties.put("pia.cms.displayLiveVersion", ctx.getDisplayLiveVersion());
		windowProperties.put("pia.cms.hideMetaDatas", ctx.getHideMetaDatas());
		windowProperties.put("pia.title", "Liste de liens");

		Map<String, String> params = new HashMap<String, String>();

		String url = portalUrlFactory.getStartProcUrl(new PortalControllerContext(ctx.getPortletCtx(),
				ctx.getRequest(), ctx.getResponse()), ctx.getPageId(),
				"toutatice-portail-cms-nuxeo-viewListPortletInstance", "virtual", "portalServiceWindow",
				windowProperties, params);

		return url;
	}

	public String createPortletLink(LinkHandlerCtx ctx, String portletInstance, String uid) throws Exception {

		Map<String, String> windowProperties = new HashMap<String, String>();
		windowProperties.put("pia.cms.scope", ctx.getScope());
		windowProperties.put("pia.cms.displayLiveVersion", ctx.getDisplayLiveVersion());
		windowProperties.put("pia.cms.hideMetaDatas", ctx.getHideMetaDatas());
		windowProperties.put("pia.cms.uri", uid);
		windowProperties.put("pia.cms.publishPathAlreadyConverted", "1");
		windowProperties.put("pia.hideDecorators", "1");
		windowProperties.put("theme.dyna.partial_refresh_enabled", "false");

		Map<String, String> params = new HashMap<String, String>();

		String url = portalUrlFactory.getStartProcUrl(new PortalControllerContext(ctx.getPortletCtx(),
				ctx.getRequest(), ctx.getResponse()), ctx.getPageId(), portletInstance, "virtual", "cms",
				windowProperties, params);

		return url;
	}

	public Link getServiceLink(LinkHandlerCtx ctx) throws Exception {

		Document doc = ctx.getDoc();

		String url = null;
		boolean externalLink = false;

		if ("ContextualLink".equals(doc.getType())) {
			url = createExternalLink(ctx);
			externalLink = true;
		} else if ("File".equals(doc.getType())) {
			url = createFileContentLink(ctx);
		} else if ("Folder".equals(doc.getType()) || "OrderedFolder".equals(doc.getType())) {
			url = createFileFolderLink(ctx);
		} else if ("AnnonceFolder".equals(doc.getType())) {
			url = createAnnonceFolderLink(ctx);
		} else if ("DocumentUrlContainer".equals(doc.getType())) {
			url = createDocumentUrlContainerLink(ctx);
		} else if ("Forum".equals(doc.getType())) {
			url = ctx.getNuxeoBaseUri().toString() + "/nxdoc/default/" + doc.getId() + "/view_documents";
			externalLink = true;
		} else if ("PictureBook".equals(doc.getType())) {
			url = ctx.getNuxeoBaseUri().toString() + "/nxdoc/default/" + doc.getId()
					+ "/view_documents?tabId=tab_slideshow";
			externalLink = true;
		} else if ("Note".equals(doc.getType()) || ("Annonce".equals(doc.getType()))) {
			// types supportés par le CMS du portail
			url = createCMSLink(ctx);
		} else {
			// Par défaut, lien direct sur Nuxeo
			url = ctx.getNuxeoBaseUri().toString() + "/nxdoc/default/" + doc.getId() + "/view_documents";
			externalLink = true;
		}

		return new Link(url, externalLink);
	}

	public Link getLink(LinkHandlerCtx ctx) throws Exception {

		Map<String, String> windowProperties = new HashMap<String, String>();

		windowProperties.put("pia.cms.scope", ctx.getScope());

		windowProperties.put("pia.cms.displayLiveVersion", ctx.getDisplayLiveVersion());
		windowProperties.put("pia.cms.hideMetaDatas", ctx.getHideMetaDatas());
		windowProperties.put("pia.cms.uri", ctx.getDoc().getPath());
		windowProperties.put("pia.cms.publishPathAlreadyConverted", "1");
		windowProperties.put("pia.hideDecorators", "1");
		windowProperties.put("theme.dyna.partial_refresh_enabled", "false");

		Map<String, String> params = new HashMap<String, String>();

		String url = portalUrlFactory.getStartProcUrl(new PortalControllerContext(ctx.getPortletCtx(),
				ctx.getRequest(), ctx.getResponse()), ctx.getPageId(),
				"toutatice-portail-cms-nuxeo-viewDocumentPortletInstance", "virtual", "cms", windowProperties, params);

		return new Link(url, false);
	}
	
	



	/*
	 * renvoie un lien contextuel vers le contenu si la page courante n'est pas
	 * la page de publication
	 * 
	 * Sinon, renvoie null
	 */

	public Link getContextualLink(LinkHandlerCtx ctx) throws Exception {
		
		Map<String, String> pageParams = new HashMap<String, String>();

		String pageId = PortalObjectPath.parse(ctx.getPageId(), PortalObjectPath.SAFEST_FORMAT).toString(
		PortalObjectPath.CANONICAL_FORMAT);

		Link link = new Link(portalUrlFactory.getCMSUrl(
		new PortalControllerContext(ctx.getPortletCtx(), ctx.getRequest(), ctx.getResponse()), pageId, ctx
				.getDoc().getPath(), pageParams,"1"), false);

		return link;
		
		


	}

}
