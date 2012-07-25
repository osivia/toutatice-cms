package fr.toutatice.portail.cms.nuxeo.portlets.customizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.ResourceURL;

import org.nuxeo.ecm.automation.client.jaxrs.model.Document;

import fr.toutatice.portail.api.contexte.PortalControllerContext;
import fr.toutatice.portail.api.menubar.MenubarItem;
import fr.toutatice.portail.api.urls.IPortalUrlFactory;
import fr.toutatice.portail.api.urls.Link;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.core.DocumentFetchLiveCommand;
import fr.toutatice.portail.core.cms.CMSException;
import fr.toutatice.portail.core.cms.CMSHandlerProperties;
import fr.toutatice.portail.core.cms.CMSItem;
import fr.toutatice.portail.core.cms.CMSServiceCtx;
import fr.toutatice.portail.core.cms.ICMSService;
import fr.toutatice.portail.core.nuxeo.INuxeoCustomizer;
import fr.toutatice.portail.core.nuxeo.INuxeoService;
import fr.toutatice.portail.core.nuxeo.NuxeoConnectionProperties;



public class DefaultCMSCustomizer implements INuxeoCustomizer {
	
	PortletContext portletCtx;
	protected IPortalUrlFactory portalUrlFactory;
	
	NuxeoConnectionProperties nuxeoConnection;
	
	
	ICMSService CMSService;
	
	ICMSService getCMSService()	{
		if( CMSService ==null)	{
			CMSService = (INuxeoService) portletCtx.getAttribute("NuxeoService");
		}
		return CMSService;
	}

	
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

	
	/**
	 * Gére les folders 'hiddenInNavigation' 
	 *  
	 * Les fils d'un folder 'hiddenInNavigation' sont directement rattachés au parent
	 * 
	 * @param ctx
	 * @return
	 * @throws CMSException
	 */
	
	protected String createFolderRequest(CMSServiceCtx ctx) throws CMSException	{
	
		String nuxeoRequest = null;

		Document doc = (Document) ctx.getDoc();

		if (ctx.getContextualizationBasePath() != null) {
			// Publication dans un environnement contextualisé
			// On se sert du menu de navigation et on décompose chaque niveau

			nuxeoRequest = " (ecm:parentId = '" + doc.getId() + "' AND ecm:mixinType != 'Folderish' )";

			List<CMSItem> navItems = getCMSService().getPortalNavigationSubitems(ctx, ctx.getContextualizationBasePath(),
					doc.getPath());

			for (CMSItem curItem : navItems) {
				String hiddenItem = curItem.getProperties().get("hiddenInNavigation");
				if ("true".equals(hiddenItem)) {
					nuxeoRequest = nuxeoRequest + " OR (ecm:path STARTSWITH '" + curItem.getPath()
							+ "' AND ecm:mixinType != 'Folderish' )";
				}
			}

		} else {
			nuxeoRequest = "ecm:path STARTSWITH '" + doc.getPath() + "' AND ecm:mixinType != 'Folderish' ";
		}
		return nuxeoRequest;
	
	}
	
	
	
	
	public CMSHandlerProperties createAnnonceFolderLink(CMSServiceCtx ctx) throws Exception {

		
		Document doc = (Document) ctx.getDoc();
		
		Map<String, String> windowProperties = new HashMap<String, String>();
		windowProperties.put("pia.nuxeoRequest", createFolderRequest( ctx)); 
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
		windowProperties.put("pia.nuxeoRequest", createFolderRequest( ctx)); 
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
	
	
	
	
	
	public CMSHandlerProperties createFileFolderLink(CMSServiceCtx ctx) throws CMSException {
		
		Document doc = (Document) ctx.getDoc();
		

		
		
		Map<String, String> windowProperties = new HashMap<String, String>();
		windowProperties.put("pia.nuxeoRequest", createFolderRequest( ctx)); 
		windowProperties.put("pia.cms.style", CMSCustomizer.STYLE_EDITORIAL);
		windowProperties.put("pia.hideDecorators", "1");
		windowProperties.put("theme.dyna.partial_refresh_enabled", "false");		
		windowProperties.put("pia.cms.scope",  ctx.getScope());
		windowProperties.put("pia.cms.displayLiveVersion", ctx.getDisplayLiveVersion());
		windowProperties.put("pia.cms.hideMetaDatas", "1");
		windowProperties.put("pia.title", "Dossier " + doc.getTitle());	
		windowProperties.put("pia.cms.pageSizeMax", "10");
	    Map<String, String> params = new HashMap<String, String>();
		
		CMSHandlerProperties linkProps = new CMSHandlerProperties();
		linkProps.setWindowProperties(windowProperties);
		linkProps.setPortletInstance("toutatice-portail-cms-nuxeo-viewListPortletInstance");
		
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

		String url = portalUrlFactory.getStartProcUrl(new PortalControllerContext(ctx.getCtx()), ctx.getPageId(), portletInstance, "virtual", "cms",
				windowProperties, params);
		
		CMSHandlerProperties linkProps = new CMSHandlerProperties();
		linkProps.setWindowProperties(windowProperties);
		linkProps.setPortletInstance(portletInstance);

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
		} else if ("Note".equals(doc.getType()) || ("Annonce".equals(doc.getType()))|| ("PortalPage".equals(doc.getType())) || ("PortalSite".equals(doc.getType()))) {
			// types supportés par le CMS du portail
			return getCMSLink(ctx);
		} else {
			
			String externalUrl = getExternalViewer(ctx);
			if( externalUrl == null)
				externalUrl = getNuxeoConnectionProps().getPublicBaseUri().toString() + "/nxdoc/default/" + doc.getId() + "/view_documents";
			
			// Par défaut, lien direct sur Nuxeo
			
			CMSHandlerProperties linkProps = new CMSHandlerProperties();
			linkProps.setExternalUrl(externalUrl);
			return linkProps;
		}		
	}

	
	public String getExternalViewer(CMSServiceCtx ctx)	{
		
		String url = null;
		
		Document doc = (Document) ctx.getDoc();
		
		if ("Forum".equals(doc.getType())) {
			url = getNuxeoConnectionProps().getPublicBaseUri().toString() + "/nxdoc/default/" + doc.getId() + "/view_documents";
		}  
		
		return url;
		
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
		
		if( url == null)	{
			url = getExternalViewer(ctx);
			if( url != null)
				externalLink = true;
		}
		
		
		if( url != null)	{
			Link link = new Link( url, externalLink);
			return link;
			
		}
		return null;
	}
	
	
	
	
	/* 
	 * Barre de menu des portlets d'affichage de contenu 
	 */

	
	public void adaptContentMenuBar(List<MenubarItem> menuBar) throws Exception {
	}

	
}
