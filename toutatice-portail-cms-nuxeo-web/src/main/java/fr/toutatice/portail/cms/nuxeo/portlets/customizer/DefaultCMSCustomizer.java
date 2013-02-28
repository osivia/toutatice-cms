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
import org.osivia.portal.api.contexte.PortalControllerContext;
import org.osivia.portal.api.menubar.MenubarItem;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.api.urls.Link;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSHandlerProperties;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSPage;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.cms.CMSServiceCtx;

import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.CMSItemAdapter;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.MenuBarFormater;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.NavigationItemAdapter;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.NavigationPictureFragmentModule;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.DocumentPictureFragmentModule;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.PropertyFragmentModule;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.UserPagesLoader;
import fr.toutatice.portail.cms.nuxeo.portlets.service.CMSService;
import fr.toutatice.portail.cms.nuxeo.portlets.service.DocumentPublishSpaceNavigationCommand;
import fr.toutatice.portail.core.nuxeo.INuxeoCustomizer;
import fr.toutatice.portail.core.nuxeo.NuxeoConnectionProperties;

public class DefaultCMSCustomizer implements INuxeoCustomizer {

	PortletContext portletCtx;
	protected IPortalUrlFactory portalUrlFactory;
	UserPagesLoader userPagesLoader;
	MenuBarFormater menuBarFormater;
	NavigationItemAdapter navigationItemAdapter;
	CMSItemAdapter cmsItemAdapter;
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
	
	public NavigationItemAdapter getNavigationItemAdapter()	{
		if( navigationItemAdapter == null){
			navigationItemAdapter = new NavigationItemAdapter(portletCtx, this, getCMSService());
		}
		
		return navigationItemAdapter;
	}
	
	public CMSItemAdapter getCMSItemAdaptor()	{
		if( cmsItemAdapter == null){
			cmsItemAdapter = new CMSItemAdapter(portletCtx, this, getCMSService());
		}
		
		return cmsItemAdapter;
	}


	public static List<ListTemplate> getListTemplates() {

		List<ListTemplate> templates = new ArrayList<ListTemplate>();
		templates.add(new ListTemplate(STYLE_MINI, "Minimal [titre]", DEFAULT_SCHEMAS));
		templates.add(new ListTemplate(STYLE_NORMAL, "Normal [titre, date]", DEFAULT_SCHEMAS));
		templates.add(new ListTemplate(STYLE_DETAILED, "Détaillé [titre, description, date, ...]", DEFAULT_SCHEMAS));
		templates.add(new ListTemplate(STYLE_EDITORIAL, "Editorial [vignette, titre, description]", DEFAULT_SCHEMAS));

		return templates;
	}
	
	
	
	public static List<FragmentType> getFragmentTypes() {
		

		List<FragmentType> fragmentTypes = new ArrayList<FragmentType>();
		
		fragmentTypes.add(new FragmentType("text_property", "Propriété texte", new PropertyFragmentModule(), "property-text", "property"));
		fragmentTypes.add(new FragmentType("html_property", "Propriété html", new PropertyFragmentModule(), "property-html", "property"));
		fragmentTypes.add(new FragmentType("navigation_picture", "Visuel navigation", new NavigationPictureFragmentModule(), "navigation-picture", "navigation"));
		fragmentTypes.add(new FragmentType("document_picture", "Image jointe", new DocumentPictureFragmentModule(), "document-picture", "property"));
		return fragmentTypes;
	}

	public static String getSearchSchema() {

		return "dublincore,common";

	}

	

	public CMSHandlerProperties getCMSDefaultPlayer(CMSServiceCtx ctx) throws Exception {

		Document doc = (Document) ctx.getDoc();

		Map<String, String> windowProperties = new HashMap<String, String>();

		/*windowProperties.put("osivia.cms.scope", ctx.getScope());*/
		windowProperties.put("osivia.cms.displayLiveVersion", ctx.getDisplayLiveVersion());
		windowProperties.put("osivia.cms.hideMetaDatas", ctx.getHideMetaDatas());
		windowProperties.put("osivia.cms.uri", doc.getPath());
		windowProperties.put("osivia.cms.publishPathAlreadyConverted", "1");
		windowProperties.put("osivia.hideDecorators", "1");
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
		
		CMSPublicationInfos pubInfos = getCMSService().getPublicationInfos(ctx, doc.getPath());
		
		
	
		
		List<CMSItem> navItems = null;
		
		
		
		if (ctx.getContextualizationBasePath() != null) {
			// Publication dans un environnement contextualisé
			// On se sert du menu de navigation et on décompose chaque niveau
			

			 navItems = getCMSService().getPortalNavigationSubitems(ctx,
					ctx.getContextualizationBasePath(), DocumentPublishSpaceNavigationCommand.computeNavPath(doc.getPath()));
		}
		
		if( navItems != null)	{

			nuxeoRequest = "( (ecm:parentId = '" + pubInfos.getLiveId() + "' )";
			
			String excludedFoldersRequest = "";

			for (CMSItem curItem : navItems) {
				String hiddenItem = curItem.getProperties().get("hideInNavigation");
				if (!"1".equals(hiddenItem)) {
					if( excludedFoldersRequest.length() > 0)
						excludedFoldersRequest += " AND ";
					excludedFoldersRequest = excludedFoldersRequest + " ( NOT ecm:path STARTSWITH '" + curItem.getPath()	+ "' )";
				}
			}
			
			nuxeoRequest += " OR ( ecm:path STARTSWITH '" + DocumentPublishSpaceNavigationCommand.computeNavPath(doc.getPath()) + "' ) )";
			
			if( excludedFoldersRequest.length() > 0)
				nuxeoRequest += " AND " + excludedFoldersRequest;
			
			nuxeoRequest +=  "AND ecm:mixinType != 'Folderish'";
			
			

		} else {
			nuxeoRequest = "ecm:path STARTSWITH '" + DocumentPublishSpaceNavigationCommand.computeNavPath(doc.getPath()) + "' AND ecm:mixinType != 'Folderish' ";
			
			if( ordered)
				nuxeoRequest += " order by ecm:pos";
		}
	
		
		return nuxeoRequest;

	}

	public CMSHandlerProperties getCMSAnnonceFolderPlayer(CMSServiceCtx ctx) throws Exception {

		Document doc = (Document) ctx.getDoc();

		Map<String, String> windowProperties = new HashMap<String, String>();
		windowProperties.put("osivia.nuxeoRequest", createFolderRequest(ctx, true));
		windowProperties.put("osivia.cms.style", CMSCustomizer.STYLE_EDITORIAL);
		windowProperties.put("osivia.hideDecorators", "1");
		windowProperties.put("theme.dyna.partial_refresh_enabled", "false");
		windowProperties.put("osivia.cms.scope", ctx.getScope());
		windowProperties.put("osivia.cms.displayLiveVersion",ctx.getDisplayLiveVersion());
		windowProperties.put("osivia.cms.hideMetaDatas", "1");
		windowProperties.put("osivia.title", "Annonces " + doc.getTitle());

		CMSHandlerProperties linkProps = new CMSHandlerProperties();
		linkProps.setWindowProperties(windowProperties);
		linkProps.setPortletInstance("toutatice-portail-cms-nuxeo-viewListPortletInstance");

		return linkProps;

	}
	
	public CMSHandlerProperties getCMSOrderedFolderPlayer(CMSServiceCtx ctx) throws Exception {

		Document doc = (Document) ctx.getDoc();

		Map<String, String> windowProperties = new HashMap<String, String>();
		windowProperties.put("osivia.nuxeoRequest", createFolderRequest(ctx, true));
		windowProperties.put("osivia.cms.style", CMSCustomizer.STYLE_EDITORIAL);
		windowProperties.put("osivia.hideDecorators", "1");
		windowProperties.put("theme.dyna.partial_refresh_enabled", "false");
		windowProperties.put("osivia.cms.scope", ctx.getScope());
		windowProperties.put("osivia.cms.displayLiveVersion", ctx.getDisplayLiveVersion());
		windowProperties.put("osivia.cms.hideMetaDatas", "1");
		windowProperties.put("osivia.title", "Dossier " + doc.getTitle());
		windowProperties.put("osivia.cms.pageSizeMax", "10");

		CMSHandlerProperties linkProps = new CMSHandlerProperties();
		linkProps.setWindowProperties(windowProperties);
		linkProps.setPortletInstance("toutatice-portail-cms-nuxeo-viewListPortletInstance");

		return linkProps;

	}
	
	
	
	
	

	public CMSHandlerProperties getCMSUrlContainerPlayer(CMSServiceCtx ctx) throws Exception {

		Document doc = (Document) ctx.getDoc();

		Map<String, String> windowProperties = new HashMap<String, String>();
		windowProperties.put("osivia.nuxeoRequest", createFolderRequest(ctx, true));
		windowProperties.put("osivia.cms.style", CMSCustomizer.STYLE_EDITORIAL);
		windowProperties.put("osivia.hideDecorators", "1");
		windowProperties.put("theme.dyna.partial_refresh_enabled", "false");
		windowProperties.put("osivia.cms.scope", ctx.getScope());
		windowProperties.put("osivia.cms.displayLiveVersion", ctx.getDisplayLiveVersion());
		windowProperties.put("osivia.cms.hideMetaDatas", ctx.getHideMetaDatas());
		windowProperties.put("osivia.cms.pageSizeMax", "10");
		windowProperties.put("osivia.title", "Liste de liens");

		CMSHandlerProperties linkProps = new CMSHandlerProperties();
		linkProps.setWindowProperties(windowProperties);
		linkProps.setPortletInstance("toutatice-portail-cms-nuxeo-viewListPortletInstance");

		return linkProps;
	}

	public CMSHandlerProperties getCMSFolderPlayer(CMSServiceCtx ctx) throws CMSException {

		Document doc = (Document) ctx.getDoc();

		Map<String, String> windowProperties = new HashMap<String, String>();
		windowProperties.put("osivia.nuxeoRequest", createFolderRequest(ctx, true));
		windowProperties.put("osivia.cms.style", CMSCustomizer.STYLE_EDITORIAL);
		windowProperties.put("osivia.hideDecorators", "1");
		windowProperties.put("theme.dyna.partial_refresh_enabled", "false");
		windowProperties.put("osivia.cms.scope", ctx.getScope());
		windowProperties.put("osivia.cms.displayLiveVersion", ctx.getDisplayLiveVersion());
		windowProperties.put("osivia.cms.hideMetaDatas", ctx.getHideMetaDatas());
		windowProperties.put("osivia.title", "Dossier " + doc.getTitle());
		windowProperties.put("osivia.cms.pageSizeMax", "10");
		Map<String, String> params = new HashMap<String, String>();

		CMSHandlerProperties linkProps = new CMSHandlerProperties();
		linkProps.setWindowProperties(windowProperties);
		linkProps.setPortletInstance("toutatice-portail-cms-nuxeo-viewListPortletInstance");

		return linkProps;

	}
	
	
	public CMSHandlerProperties getCMSSectionPlayer(CMSServiceCtx ctx) throws Exception {
		Document doc = (Document) ctx.getDoc();

		Map<String, String> windowProperties = new HashMap<String, String>();
		
		windowProperties.put("osivia.nuxeoRequest", "ecm:path STARTSWITH '"+ doc.getPath()+"' AND ecm:mixinType != 'Folderish'   ORDER BY dc:modified DESC");
		windowProperties.put("osivia.cms.style", CMSCustomizer.STYLE_EDITORIAL);
		windowProperties.put("osivia.hideDecorators", "1");
		windowProperties.put("theme.dyna.partial_refresh_enabled", "false");
		windowProperties.put("osivia.cms.scope", ctx.getScope());
		windowProperties.put("osivia.cms.displayLiveVersion", ctx.getDisplayLiveVersion());
		windowProperties.put("osivia.cms.hideMetaDatas", ctx.getHideMetaDatas());
		windowProperties.put("osivia.title", "Dossier " + doc.getTitle());
		windowProperties.put("osivia.cms.pageSizeMax", "10");

		CMSHandlerProperties linkProps = new CMSHandlerProperties();
		linkProps.setWindowProperties(windowProperties);
		linkProps.setPortletInstance("toutatice-portail-cms-nuxeo-viewListPortletInstance");

		return linkProps;


	}

	
	public CMSHandlerProperties getCMSVirtualPagePlayer(CMSServiceCtx ctx) throws CMSException {

		Document doc = (Document) ctx.getDoc();

		Map<String, String> windowProperties = new HashMap<String, String>();
		windowProperties.put("osivia.nuxeoRequest", doc.getString("ttc:queryPart"));
		windowProperties.put("osivia.cms.style", CMSCustomizer.STYLE_EDITORIAL);
		windowProperties.put("osivia.hideDecorators", "1");
		windowProperties.put("theme.dyna.partial_refresh_enabled", "false");
		windowProperties.put("osivia.cms.scope", ctx.getScope());
		//windowProperties.put("osivia.cms.displayLiveVersion", ctx.getDisplayLiveVersion());
		windowProperties.put("osivia.cms.hideMetaDatas", ctx.getHideMetaDatas());
		windowProperties.put("osivia.title", "Dossier " + doc.getTitle());
		windowProperties.put("osivia.cms.pageSizeMax", "10");
		Map<String, String> params = new HashMap<String, String>();

		CMSHandlerProperties linkProps = new CMSHandlerProperties();
		linkProps.setWindowProperties(windowProperties);
		linkProps.setPortletInstance("toutatice-portail-cms-nuxeo-viewListPortletInstance");

		return linkProps;

	}
	
	

	public CMSHandlerProperties createPortletLink(CMSServiceCtx ctx, String portletInstance, String uid)
			throws Exception {

		Map<String, String> windowProperties = new HashMap<String, String>();
		windowProperties.put("osivia.cms.scope", ctx.getScope());
		windowProperties.put("osivia.cms.displayLiveVersion", ctx.getDisplayLiveVersion());
		windowProperties.put("osivia.cms.hideMetaDatas", ctx.getHideMetaDatas());
		windowProperties.put("osivia.cms.uri", uid);
		windowProperties.put("osivia.cms.publishPathAlreadyConverted", "1");
		windowProperties.put("osivia.hideDecorators", "1");
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
		
		if ("PortalVirtualPage".equals(doc.getType())) {
			return getCMSVirtualPagePlayer(ctx);
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
		boolean downloadable = false;

		if (!"detailedView".equals(ctx.getDisplayContext())) {
			if ("File".equals(doc.getType())) {
				url = createPortletDelegatedFileContentLink(ctx);
				downloadable = true;
			}

			if ("ContextualLink".equals(doc.getType())) {
				url = createPortletDelegatedExternalLink(ctx);
				externalLink = true;
			}

			// Gestion des vues externes
			// Nécessaire pour poser une ancre au moment de la génération du
			// lien

			if (url == null) {

				url = getNuxeoNativeViewerUrl(ctx);
				externalLink = true;

			}
		}

		if (url != null) {
			Link link = new Link(url, externalLink);
			link.setDownloadable(downloadable);
			return link;

		}
		return null;
	}

	
	
	

	/*
	 * Barre de menu des portlets d'affichage de contenu
	 */

	public void formatContentMenuBar(CMSServiceCtx ctx) throws Exception {

		getMenuBarFormater().formatContentMenuBar(ctx);

		List<MenubarItem> menuBar = (List<MenubarItem>) ctx.getRequest().getAttribute("osivia.menuBar");

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

	/*
	 * Parsing CMS spécifiques
	 */
	
	public Map<String, String> parseCMSURL(CMSServiceCtx cmsCtx, String requestPath, Map<String, String> requestParameters)
			throws Exception {
		
		return null;
	}

	public Map<String, String> getDocumentConfiguration(CMSServiceCtx ctx, Document doc) throws Exception {
		return getCMSItemAdaptor().adaptDocument( ctx,  doc);
	}



	

	
}
