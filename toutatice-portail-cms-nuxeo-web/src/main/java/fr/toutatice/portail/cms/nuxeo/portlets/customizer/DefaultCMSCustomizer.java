package fr.toutatice.portail.cms.nuxeo.portlets.customizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletContext;
import javax.portlet.ResourceURL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;

import fr.toutatice.portail.api.contexte.PortalControllerContext;
import fr.toutatice.portail.api.menubar.MenubarItem;
import fr.toutatice.portail.api.urls.IPortalUrlFactory;
import fr.toutatice.portail.api.urls.Link;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.MenuBarFormater;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.NavigationItemAdaptor;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.UserPagesLoader;
import fr.toutatice.portail.cms.nuxeo.portlets.service.CMSService;
import fr.toutatice.portail.core.cms.CMSException;
import fr.toutatice.portail.core.cms.CMSHandlerProperties;
import fr.toutatice.portail.core.cms.CMSItem;
import fr.toutatice.portail.core.cms.CMSPage;
import fr.toutatice.portail.core.cms.CMSServiceCtx;
import fr.toutatice.portail.core.nuxeo.INuxeoCustomizer;
import fr.toutatice.portail.core.nuxeo.NuxeoConnectionProperties;

public class DefaultCMSCustomizer implements INuxeoCustomizer {

	PortletContext portletCtx;
	protected IPortalUrlFactory portalUrlFactory;
	UserPagesLoader userPagesLoader;
	MenuBarFormater menuBarFormater;
	NavigationItemAdaptor navigationItemAdaptor;
	NuxeoConnectionProperties nuxeoConnection;

	protected static final Log logger = LogFactory.getLog(DefaultCMSCustomizer.class);
	
	
	/* Default style for lists */


	public static final String STYLE_MINI = "mini";
	public static final String STYLE_NORMAL = "normal";
	public static final String STYLE_DETAILED = "detailed";
	public static final String STYLE_EDITORIAL = "editorial";

	public static final String DEFAULT_SCHEMAS = "dublincore,common, toutatice";
	
	public static String TEMPLATE_DOWNLOAD = "download";	
	
	
	CMSService CMSService;

	CMSService getCMSService() {
		return CMSService;
	}
	
	public void  setCMSService(CMSService CMSService) {
		this.CMSService = CMSService;
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
	
	public UserPagesLoader getUserPagesLoader()	{
		if( userPagesLoader == null){
			userPagesLoader = new UserPagesLoader(portletCtx, this, getCMSService());
		}
		
		return userPagesLoader;
	}
	
	public MenuBarFormater getMenuBarFormater()	{
		if( menuBarFormater == null){
			menuBarFormater = new MenuBarFormater(portletCtx, this, getCMSService());
		}
		
		return menuBarFormater;
	}
	
	public NavigationItemAdaptor getNavigationItemAdaptor()	{
		if( navigationItemAdaptor == null){
			navigationItemAdaptor = new NavigationItemAdaptor(portletCtx, this, getCMSService());
		}
		
		return navigationItemAdaptor;
	}


	public static List<ListTemplate> getListTemplates() {

		List<ListTemplate> templates = new ArrayList<ListTemplate>();
		templates.add(new ListTemplate(STYLE_MINI, "Minimal [titre]", DEFAULT_SCHEMAS));
		templates.add(new ListTemplate(STYLE_NORMAL, "Normal [titre, date]", DEFAULT_SCHEMAS));
		templates.add(new ListTemplate(STYLE_DETAILED, "Détaillé [titre, description, date, ...]", DEFAULT_SCHEMAS));
		templates.add(new ListTemplate(STYLE_EDITORIAL, "Editorial [vignette, titre, description]", DEFAULT_SCHEMAS));

		return templates;
	}

	public static String getSearchSchema() {

		return "dublincore,common";

	}

	public CMSHandlerProperties getCMSDefaultPlayer(CMSServiceCtx ctx) throws Exception {

		Document doc = (Document) ctx.getDoc();

		Map<String, String> windowProperties = new HashMap<String, String>();

		windowProperties.put("pia.cms.scope", ctx.getScope());

		windowProperties.put("pia.cms.displayLiveVersion", ctx.getDisplayLiveVersion());
		windowProperties.put("pia.cms.hideMetaDatas", ctx.getHideMetaDatas());
		windowProperties.put("pia.cms.uri", doc.getPath());
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
	 * Les fils d'un folder 'hiddenInNavigation' sont directement rattachés au
	 * parent
	 * 
	 * @param ctx
	 * @return
	 * @throws CMSException
	 */

	protected String createFolderRequest(CMSServiceCtx ctx, boolean ordered) throws CMSException {

		String nuxeoRequest = null;

		Document doc = (Document) ctx.getDoc();

		if (ctx.getContextualizationBasePath() != null) {
			// Publication dans un environnement contextualisé
			// On se sert du menu de navigation et on décompose chaque niveau

			nuxeoRequest = " (ecm:parentId = '" + doc.getId() + "' AND ecm:mixinType != 'Folderish' )";

			List<CMSItem> navItems = getCMSService().getPortalNavigationSubitems(ctx,
					ctx.getContextualizationBasePath(), doc.getPath());

			for (CMSItem curItem : navItems) {
				String hiddenItem = curItem.getProperties().get("hiddenInNavigation");
				if ("1".equals(hiddenItem)) {
					nuxeoRequest = nuxeoRequest + " OR (ecm:path STARTSWITH '" + curItem.getPath()
							+ "' AND ecm:mixinType != 'Folderish' )";
				}
			}

		} else {
			nuxeoRequest = "ecm:path STARTSWITH '" + doc.getPath() + "' AND ecm:mixinType != 'Folderish' ";
			
			if( ordered)
				nuxeoRequest += " order by ecm:pos";
		}
		return nuxeoRequest;

	}

	public CMSHandlerProperties getCMSAnnonceFolderPlayer(CMSServiceCtx ctx) throws Exception {

		Document doc = (Document) ctx.getDoc();

		Map<String, String> windowProperties = new HashMap<String, String>();
		windowProperties.put("pia.nuxeoRequest", createFolderRequest(ctx, true));
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
	
	public CMSHandlerProperties getCMSOrderedFolderPlayer(CMSServiceCtx ctx) throws Exception {

		Document doc = (Document) ctx.getDoc();

		Map<String, String> windowProperties = new HashMap<String, String>();
		windowProperties.put("pia.nuxeoRequest", createFolderRequest(ctx, true));
		windowProperties.put("pia.cms.style", CMSCustomizer.STYLE_EDITORIAL);
		windowProperties.put("pia.hideDecorators", "1");
		windowProperties.put("theme.dyna.partial_refresh_enabled", "false");
		windowProperties.put("pia.cms.scope", ctx.getScope());
		windowProperties.put("pia.cms.displayLiveVersion", ctx.getDisplayLiveVersion());
		windowProperties.put("pia.cms.hideMetaDatas", "1");
		windowProperties.put("pia.title", "Dossier " + doc.getTitle());
		windowProperties.put("pia.cms.pageSizeMax", "10");

		CMSHandlerProperties linkProps = new CMSHandlerProperties();
		linkProps.setWindowProperties(windowProperties);
		linkProps.setPortletInstance("toutatice-portail-cms-nuxeo-viewListPortletInstance");

		return linkProps;

	}
	
	
	
	
	

	public CMSHandlerProperties getCMSUrlContainerPlayer(CMSServiceCtx ctx) throws Exception {

		Document doc = (Document) ctx.getDoc();

		Map<String, String> windowProperties = new HashMap<String, String>();
		windowProperties.put("pia.nuxeoRequest", createFolderRequest(ctx, true));
		windowProperties.put("pia.cms.style", CMSCustomizer.STYLE_EDITORIAL);
		windowProperties.put("pia.hideDecorators", "1");
		windowProperties.put("theme.dyna.partial_refresh_enabled", "false");
		windowProperties.put("pia.cms.scope", ctx.getScope());
		windowProperties.put("pia.cms.displayLiveVersion", ctx.getDisplayLiveVersion());
		windowProperties.put("pia.cms.hideMetaDatas", ctx.getHideMetaDatas());
		windowProperties.put("pia.cms.pageSizeMax", "10");
		windowProperties.put("pia.title", "Liste de liens");

		CMSHandlerProperties linkProps = new CMSHandlerProperties();
		linkProps.setWindowProperties(windowProperties);
		linkProps.setPortletInstance("toutatice-portail-cms-nuxeo-viewListPortletInstance");

		return linkProps;
	}

	public CMSHandlerProperties getCMSFolderPlayer(CMSServiceCtx ctx) throws CMSException {

		Document doc = (Document) ctx.getDoc();

		Map<String, String> windowProperties = new HashMap<String, String>();
		windowProperties.put("pia.nuxeoRequest", createFolderRequest(ctx, true));
		windowProperties.put("pia.cms.style", CMSCustomizer.STYLE_EDITORIAL);
		windowProperties.put("pia.hideDecorators", "1");
		windowProperties.put("theme.dyna.partial_refresh_enabled", "false");
		windowProperties.put("pia.cms.scope", ctx.getScope());
		windowProperties.put("pia.cms.displayLiveVersion", ctx.getDisplayLiveVersion());
		windowProperties.put("pia.cms.hideMetaDatas", ctx.getHideMetaDatas());
		windowProperties.put("pia.title", "Dossier " + doc.getTitle());
		windowProperties.put("pia.cms.pageSizeMax", "10");
		Map<String, String> params = new HashMap<String, String>();

		CMSHandlerProperties linkProps = new CMSHandlerProperties();
		linkProps.setWindowProperties(windowProperties);
		linkProps.setPortletInstance("toutatice-portail-cms-nuxeo-viewListPortletInstance");

		return linkProps;

	}
	
	
	public CMSHandlerProperties getCMSSectionPlayer(CMSServiceCtx ctx) throws Exception {
		Document doc = (Document) ctx.getDoc();

		Map<String, String> windowProperties = new HashMap<String, String>();
		
		windowProperties.put("pia.nuxeoRequest", "ecm:path STARTSWITH '"+ doc.getPath()+"' AND ecm:mixinType != 'Folderish'   ORDER BY dc:modified DESC");
		windowProperties.put("pia.cms.style", CMSCustomizer.STYLE_EDITORIAL);
		windowProperties.put("pia.hideDecorators", "1");
		windowProperties.put("theme.dyna.partial_refresh_enabled", "false");
		windowProperties.put("pia.cms.scope", ctx.getScope());
		windowProperties.put("pia.cms.displayLiveVersion", ctx.getDisplayLiveVersion());
		windowProperties.put("pia.cms.hideMetaDatas", ctx.getHideMetaDatas());
		windowProperties.put("pia.title", "Dossier " + doc.getTitle());
		windowProperties.put("pia.cms.pageSizeMax", "10");

		CMSHandlerProperties linkProps = new CMSHandlerProperties();
		linkProps.setWindowProperties(windowProperties);
		linkProps.setPortletInstance("toutatice-portail-cms-nuxeo-viewListPortletInstance");

		return linkProps;


	}
	

	public CMSHandlerProperties createPortletLink(CMSServiceCtx ctx, String portletInstance, String uid)
			throws Exception {

		Map<String, String> windowProperties = new HashMap<String, String>();
		windowProperties.put("pia.cms.scope", ctx.getScope());
		windowProperties.put("pia.cms.displayLiveVersion", ctx.getDisplayLiveVersion());
		windowProperties.put("pia.cms.hideMetaDatas", ctx.getHideMetaDatas());
		windowProperties.put("pia.cms.uri", uid);
		windowProperties.put("pia.cms.publishPathAlreadyConverted", "1");
		windowProperties.put("pia.hideDecorators", "1");
		windowProperties.put("theme.dyna.partial_refresh_enabled", "false");

		Map<String, String> params = new HashMap<String, String>();

		String url = portalUrlFactory.getStartProcUrl(new PortalControllerContext(ctx.getControllerContext()),
				ctx.getPageId(), portletInstance, "virtual", "cms", windowProperties, params);

		CMSHandlerProperties linkProps = new CMSHandlerProperties();
		linkProps.setWindowProperties(windowProperties);
		linkProps.setPortletInstance(portletInstance);

		return linkProps;
	}

	
	
	
	/*
	 * On détermine le player associé à chaque item
	 */


	/*
	 * On détermine le player associé à chaque item
	 */

	public CMSHandlerProperties getCMSPlayer(CMSServiceCtx ctx) throws Exception {

		Document doc = (Document) ctx.getDoc();

		if ("UserWorkspace".equals(doc.getType())) {
			// Pas de filtre sur les versions publiées
			ctx.setDisplayLiveVersion("1");
			return createPortletLink(ctx, "toutatice-portail-cms-nuxeo-fileBrowserPortletInstance", doc.getPath());
		}
		
		if ( ("DocumentUrlContainer".equals(doc.getType()))  ) 
			return getCMSUrlContainerPlayer(ctx);		
		
		if ("AnnonceFolder".equals(doc.getType()) ) {
			return getCMSAnnonceFolderPlayer(ctx);
		} 		

		if (("Folder".equals(doc.getType()) || "OrderedFolder".equals(doc.getType())) ) {
			return getCMSFolderPlayer(ctx);
		} 
		
		return getCMSDefaultPlayer(ctx);

	}
	

	public String getDefaultExternalViewer(CMSServiceCtx ctx) {

		Document doc = (Document) ctx.getDoc();

		String externalUrl =  getNuxeoConnectionProps().getPublicBaseUri().toString() + "/nxdoc/default/"
				+ doc.getId() + "/view_documents";

		// Par défaut, lien direct sur Nuxeo

		return externalUrl;

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

	
	public String getNuxeoNativeViewerUrl (CMSServiceCtx ctx)	{
		return null;

	}
	


	/*
	 * 
	 * Ici, on intercepte le traitement CMS lors de la génération du lien
	 * 
	 * C'est le cas pour :
	 *    - des traitements directements pris en charge par le portlet (ex : download d'un document)
	 *    - des liens s'ouvrant dans des fenetres externes
	 *    
	 * Si le lien n'est pas pris en charge ici, il sera intégré au traitement CMS
	 * standard, cad
	 *    - lien de type /cms/
	 *    - passage par la couche player au moment du click sur le lien
	 * 
	 * 
	 * displayContext : menu, download, fileExplorer, permlink ...
	 */
	
	
	public Link createCustomLink(CMSServiceCtx ctx) throws Exception {

		Document doc = (Document) ctx.getDoc();

		String url = null;
		boolean externalLink = false;
	

		if ("File".equals(doc.getType()) && (TEMPLATE_DOWNLOAD.equals(ctx.getDisplayContext()))) {
			url = createPortletDelegatedFileContentLink(ctx);
			
			// v1.0.27 : plantage sur liens documents
			//TODO : A INTEGRER ???
			/*
			if(  ctx.getResponse() != null)  
		 		url = createFileContentLink(ctx);
			else
				url = createCMSLink(ctx);
				*/
		}
	
		
		if ("ContextualLink".equals(doc.getType())) {
			url = createPortletDelegatedExternalLink(ctx);
			externalLink = true;
		}
		
			
		// Gestion des vues externes
		// Nécessaire pour poser une ancre au moment de la génération du lien
		
		if (url == null) {
		
			url = getNuxeoNativeViewerUrl(ctx);
			externalLink = true;

		}
		

		if (url != null) {
			Link link = new Link(url, externalLink);
			return link;

		} 
		return null;
	}

	
	
	

	/*
	 * Barre de menu des portlets d'affichage de contenu
	 */

	public void formatContentMenuBar(CMSServiceCtx ctx) throws Exception {

		getMenuBarFormater().formatContentMenuBar(ctx);

		List<MenubarItem> menuBar = (List<MenubarItem>) ctx.getRequest().getAttribute("pia.menuBar");

		adaptContentMenuBar(ctx, menuBar);

	}

	/*
	 * Personnalisation des items de la menubar
	 */

	protected void adaptContentMenuBar(CMSServiceCtx ctx, List<MenubarItem> menuBar) throws Exception {
	}

	/*
	 * Préchargement des pages au login
	 */

	

	public List<CMSPage> computeUserPreloadedPages(CMSServiceCtx cmsCtx)  throws Exception	{
		return getUserPagesLoader().computeUserPreloadedPages(cmsCtx);
	}
	


	
}
