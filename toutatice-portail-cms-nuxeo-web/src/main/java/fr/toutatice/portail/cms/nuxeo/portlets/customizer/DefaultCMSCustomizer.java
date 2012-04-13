package fr.toutatice.portail.cms.nuxeo.portlets.customizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletContext;
import javax.portlet.ResourceURL;

import org.nuxeo.ecm.automation.client.jaxrs.model.Document;

import fr.toutatice.portail.api.contexte.PortalControllerContext;
import fr.toutatice.portail.api.urls.IPortalUrlFactory;
import fr.toutatice.portail.api.urls.Link;
import fr.toutatice.portail.core.cms.CMSHandlerProperties;
import fr.toutatice.portail.core.cms.CMSServiceCtx;
import fr.toutatice.portail.core.nuxeo.INuxeoLinkHandler;
import fr.toutatice.portail.core.nuxeo.NuxeoConnectionProperties;



public class DefaultCMSCustomizer implements INuxeoLinkHandler {
	
	PortletContext portletCtx;
	protected IPortalUrlFactory portalUrlFactory;
	
	NuxeoConnectionProperties nuxeoConnection;
	
	public NuxeoConnectionProperties getNuxeoConnectionProps() {
		if (nuxeoConnection == null)
			nuxeoConnection = new NuxeoConnectionProperties();
		return nuxeoConnection;
	}

	public DefaultCMSCustomizer(PortletContext ctx) {
		super();
		this.portletCtx = ctx;
		this.portalUrlFactory = (IPortalUrlFactory) portletCtx.getAttribute("UrlService");
	}	
	
	public static final String STYLE_MINI = "mini";
	public static final String STYLE_NORMAL = "normal";
	public static final String STYLE_DETAILED = "detailed";
	public static final String STYLE_EDITORIAL = "editorial";
	
	public static final String DEFAULT_SCHEMAS =  "dublincore,common, toutatice";	

	
	public static List<ListTemplate> getListTemplates()	{
		
		List<ListTemplate> templates = new ArrayList<ListTemplate>();
		templates.add( new ListTemplate(STYLE_MINI, "Minimal [titre]", DEFAULT_SCHEMAS));
		templates.add( new ListTemplate(STYLE_NORMAL, "Normal [titre, date]", DEFAULT_SCHEMAS));
		templates.add( new ListTemplate(STYLE_DETAILED, "Détaillé [titre, description, date, ...]", DEFAULT_SCHEMAS));
		templates.add( new ListTemplate(STYLE_EDITORIAL, "Editorial [vignette, titre, description]", DEFAULT_SCHEMAS));
		
		return templates;
	}
	
	
	public static String getSearchSchema()	{
		
		return "dublincore,common";
		
	}
	
	

	public CMSHandlerProperties getCMSLink(CMSServiceCtx ctx) throws Exception {

		Document doc = (Document) ctx.getDoc();
		
		Map<String, String> windowProperties = new HashMap<String, String>();

		windowProperties.put("pia.cms.scope", ctx.getScope());

		windowProperties.put("pia.cms.displayLiveVersion", ctx.getDisplayLiveVersion());
		windowProperties.put("pia.cms.hideMetaDatas", ctx.getHideMetaDatas());
		windowProperties.put("pia.cms.uri",doc.getPath());
		windowProperties.put("pia.cms.publishPathAlreadyConverted", "1");
		windowProperties.put("pia.hideDecorators", "1");
		windowProperties.put("theme.dyna.partial_refresh_enabled", "false");

	
		CMSHandlerProperties linkProps = new CMSHandlerProperties();
		linkProps.setWindowProperties(windowProperties);
		linkProps.setPortletInstance("toutatice-portail-cms-nuxeo-viewDocumentPortletInstance");
		
		return linkProps;
	}

	
	public CMSHandlerProperties createAnnonceFolderLink(CMSServiceCtx ctx) throws Exception {

		
		Document doc = (Document) ctx.getDoc();
		
		Map<String, String> windowProperties = new HashMap<String, String>();
		windowProperties.put("pia.nuxeoRequest", "ecm:path STARTSWITH '" + doc.getPath() + "' ");
		windowProperties.put("pia.cms.style", CMSCustomizer.STYLE_EDITORIAL);
		windowProperties.put("pia.hideDecorators", "1");
		windowProperties.put("theme.dyna.partial_refresh_enabled", "false");
		windowProperties.put("pia.cms.scope", ctx.getScope());
		windowProperties.put("pia.cms.displayLiveVersion", ctx.getDisplayLiveVersion());
		windowProperties.put("pia.cms.hideMetaDatas", "1");
		windowProperties.put("pia.title", "Annonces " + doc.getTitle());
		
		CMSHandlerProperties linkProps = new CMSHandlerProperties();
		linkProps.setWindowProperties(windowProperties);
		linkProps.setPortletInstance("toutatice-portail-cms-nuxeo-viewListPortletInstance");
		
		return linkProps;
		
	
	}
	
	public CMSHandlerProperties createDocumentUrlContainerLink(CMSServiceCtx ctx) throws Exception {

		
		Document doc = (Document) ctx.getDoc();
		
		Map<String, String> windowProperties = new HashMap<String, String>();
		windowProperties.put("pia.nuxeoRequest", "ecm:path STARTSWITH '" + doc.getPath() + "' ");
		windowProperties.put("pia.cms.style", CMSCustomizer.STYLE_EDITORIAL);
		windowProperties.put("pia.hideDecorators", "1");
		windowProperties.put("theme.dyna.partial_refresh_enabled", "false");
		windowProperties.put("pia.cms.scope", ctx.getScope());
		windowProperties.put("pia.cms.displayLiveVersion", ctx.getDisplayLiveVersion());
		windowProperties.put("pia.cms.hideMetaDatas", ctx.getHideMetaDatas());
		windowProperties.put("pia.title", "Liste de liens");

		CMSHandlerProperties linkProps = new CMSHandlerProperties();
		linkProps.setWindowProperties(windowProperties);
		linkProps.setPortletInstance("toutatice-portail-cms-nuxeo-viewListPortletInstance");
		
		return linkProps;
	}
	
	
	
	public CMSHandlerProperties createFileFolderLink(CMSServiceCtx ctx) {
		
		Document doc = (Document) ctx.getDoc();

		Map<String, String> windowProperties = new HashMap<String, String>();
		windowProperties.put("pia.cms.uri", doc.getPath());
		windowProperties.put("pia.cms.publishPathAlreadyConverted", "1");
		windowProperties.put("pia.hideDecorators", "1");
		windowProperties.put("theme.dyna.partial_refresh_enabled", "false");
		windowProperties.put("pia.cms.scope", ctx.getScope());
		windowProperties.put("pia.cms.displayLiveVersion", ctx.getDisplayLiveVersion());
		windowProperties.put("pia.cms.hideMetaDatas", ctx.getHideMetaDatas());
		windowProperties.put("pia.title", "Dossier " + doc.getTitle());


		CMSHandlerProperties linkProps = new CMSHandlerProperties();
		linkProps.setWindowProperties(windowProperties);
		linkProps.setPortletInstance("toutatice-portail-cms-nuxeo-fileBrowserPortletInstance");
		
		return linkProps;


	}
	
	public CMSHandlerProperties createPortletLink(CMSServiceCtx ctx, String portletInstance, String uid) throws Exception {

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
		
		CMSHandlerProperties linkProps = new CMSHandlerProperties();
		linkProps.setUrl(url);

		return linkProps;
	}
	
	public CMSHandlerProperties getLink(CMSServiceCtx ctx) throws Exception {
		
		Document doc = (Document) ctx.getDoc();

		
		if ("Folder".equals(doc.getType()) || "OrderedFolder".equals(doc.getType())) {
			return createFileFolderLink(ctx);
		} else if ("AnnonceFolder".equals(doc.getType())) {
			return createAnnonceFolderLink(ctx);
		} else if ("DocumentUrlContainer".equals(doc.getType())) {
			return createDocumentUrlContainerLink(ctx);
		} else if ("Forum".equals(doc.getType())) {
			CMSHandlerProperties linkProps = new CMSHandlerProperties();
			linkProps.setExternalUrl(getNuxeoConnectionProps().getPublicBaseUri().toString() + "/nxdoc/default/" + doc.getId() + "/view_documents");
			return linkProps;
		} else if ("PictureBook".equals(doc.getType())) {
			CMSHandlerProperties linkProps = new CMSHandlerProperties();
			linkProps.setExternalUrl(getNuxeoConnectionProps().getPublicBaseUri().toString() + "/nxdoc/default/" + doc.getId()
					+ "/view_documents?tabId=tab_slideshow");
			return linkProps;
		} else if ("Note".equals(doc.getType()) || ("Annonce".equals(doc.getType()))) {
			// types supportés par le CMS du portail
			return getCMSLink(ctx);
		} else {
			CMSHandlerProperties linkProps = new CMSHandlerProperties();
			linkProps.setExternalUrl(getNuxeoConnectionProps().getPublicBaseUri().toString() + "/nxdoc/default/" + doc.getId() + "/view_documents");
			return linkProps;
			// Par défaut, lien direct sur Nuxeo

		}		
		
		
	}


	public String createPortletDelegatedExternalLink(CMSServiceCtx ctx) {

		
		Document doc = (Document) ctx.getDoc();
		
		ResourceURL resourceURL = ctx.getResponse().createResourceURL();
		resourceURL.setResourceID(doc.getId());
		resourceURL.setParameter("type", "link");
		// ne marche pas : bug JBP
		// resourceURL.setCacheability(ResourceURL.PORTLET);

		return resourceURL.toString();
	}
	

	public String createPortletDelegatedFileContentLink(CMSServiceCtx ctx) {

		Document doc = (Document) ctx.getDoc();
		
		ResourceURL resourceURL = ctx.getResponse().createResourceURL();
		resourceURL.setResourceID(doc.getId() + "/" + "file:content");
		resourceURL.setParameter("type", "file");
		resourceURL.setParameter("docPath", doc.getPath());
		resourceURL.setParameter("fieldName", "file:content");
		// ne marche pas : bug JBP
		// resourceURL.setCacheability(ResourceURL.PORTLET);
		resourceURL.setCacheability(ResourceURL.PAGE);

		return resourceURL.toString();
	}
	
	
	/* 
	 * Cas ou le portlet prend directement en charge le rendu
	 */
	public Link getPortletDelegatedLink(CMSServiceCtx ctx) throws Exception {
		
		Document doc = (Document) ctx.getDoc();

		String url = null;
		boolean externalLink = false;

		if ("ContextualLink".equals(doc.getType())) {
			url = createPortletDelegatedExternalLink(ctx);
			externalLink = true;
		} else if ("File".equals(doc.getType())) {
			url = createPortletDelegatedFileContentLink(ctx);
		}
		
		if( url != null)	{
			Link link = new Link( url, externalLink);
			return link;
			
		}
		return null;
	}
	
	
	
}
