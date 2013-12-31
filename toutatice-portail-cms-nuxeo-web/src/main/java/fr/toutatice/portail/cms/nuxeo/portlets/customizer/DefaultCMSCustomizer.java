package fr.toutatice.portail.cms.nuxeo.portlets.customizer;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.portlet.PortletContext;
import javax.portlet.ResourceURL;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.portal.common.invocation.Scope;
import org.jboss.portal.core.model.portal.Portal;
import org.jboss.portal.core.model.portal.PortalObject;
import org.jboss.portal.core.model.portal.PortalObjectContainer;
import org.jboss.portal.core.model.portal.PortalObjectId;
import org.jboss.portal.core.model.portal.PortalObjectPath;
import org.jboss.portal.server.ServerInvocation;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.menubar.MenubarItem;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.api.urls.Link;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSHandlerProperties;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSPage;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.constants.InternalConstants;
import org.osivia.portal.core.page.PageProperties;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import fr.toutatice.portail.cms.nuxeo.api.services.DocTypeDefinition;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCustomizer;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoConnectionProperties;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.CMSItemAdapter;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.CMSToWebPathAdapter;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.DefaultPlayer;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.DocumentPictureFragmentModule;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.EditableWindowAdapter;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.IPlayer;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.LinkFragmentModule;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.MenuBarFormater;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.NavigationItemAdapter;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.NavigationPictureFragmentModule;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.PropertyFragmentModule;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.SpaceMenuBarFragmentModule;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.UserPagesLoader;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.WebConfiguratinQueryCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.WebConfiguratinQueryCommand.WebConfigurationType;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.WebConfigurationHelper;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.WysiwygParser;
import fr.toutatice.portail.cms.nuxeo.portlets.service.CMSService;
import fr.toutatice.portail.cms.nuxeo.portlets.service.DocumentPublishSpaceNavigationCommand;

public class DefaultCMSCustomizer implements INuxeoCustomizer{


	PortletContext portletCtx;
	protected IPortalUrlFactory portalUrlFactory;
	UserPagesLoader userPagesLoader;
	MenuBarFormater menuBarFormater;
	NavigationItemAdapter navigationItemAdapter;
	CMSItemAdapter cmsItemAdapter;
	NuxeoConnectionProperties nuxeoConnection;
	XMLReader parser;
    EditableWindowAdapter editableWindowAdapter;
    CMSToWebPathAdapter cmsToWebAdapter;

	protected static final Log logger = LogFactory.getLog(DefaultCMSCustomizer.class);

    /** CMS Players. */
    protected Map<String, IPlayer> players = new HashMap<String, IPlayer>();

	/* Default style for lists */


	public static final String STYLE_MINI = "mini";
	public static final String STYLE_NORMAL = "normal";
	public static final String STYLE_DETAILED = "detailed";
	public static final String STYLE_EDITORIAL = "editorial";

	public static final String DEFAULT_SCHEMAS = "dublincore,common, toutatice, file";

	public static String TEMPLATE_DOWNLOAD = "download";


	CMSService CMSService;

	CMSService getCMSService() {
		return this.CMSService;
	}

	public void  setCMSService(CMSService CMSService) {
		this.CMSService = CMSService;
	}

	public NuxeoConnectionProperties getNuxeoConnectionProps() {
		if (this.nuxeoConnection == null) {
            this.nuxeoConnection = new NuxeoConnectionProperties();
        }
		return this.nuxeoConnection;
	}

	public DefaultCMSCustomizer(PortletContext ctx) {
		super();
		this.portletCtx = ctx;
		this.portalUrlFactory = (IPortalUrlFactory) this.portletCtx.getAttribute("UrlService");

        // initialise le player view document par défaut
        this.players.put("defaultPlayer", new DefaultPlayer());

		try   {
		 // Initialisé ici pour résoudre problème de classloader

         this.parser = WysiwygParser.getInstance().getParser();
		} catch(Exception e){
		    throw new RuntimeException(e);
		}

	}




	public UserPagesLoader getUserPagesLoader()	{
		if( this.userPagesLoader == null){
			this.userPagesLoader = new UserPagesLoader(this.portletCtx, this, this.getCMSService());
		}

		return this.userPagesLoader;
	}

	public MenuBarFormater getMenuBarFormater()	{
		if( this.menuBarFormater == null){
			this.menuBarFormater = new MenuBarFormater(this.portletCtx, this, this.getCMSService());
		}

		return this.menuBarFormater;
	}

	public NavigationItemAdapter getNavigationItemAdapter()	{
		if( this.navigationItemAdapter == null){
			this.navigationItemAdapter = new NavigationItemAdapter(this.portletCtx, this, this.getCMSService());
		}

		return this.navigationItemAdapter;
	}

	public CMSItemAdapter getCMSItemAdapter()	{
		if( this.cmsItemAdapter == null){
			this.cmsItemAdapter = new CMSItemAdapter(this.portletCtx, this, this.getCMSService());
		}

		return this.cmsItemAdapter;
	}

    /**
     * EditableWindowAdapter permet de gérer les types de EditableWindow affichables
     *
     * @return Instance du EditableWindowAdapter
     */
    public EditableWindowAdapter getEditableWindowAdapter() {
        if (this.editableWindowAdapter == null) {
            this.editableWindowAdapter = new EditableWindowAdapter();
        }

        return this.editableWindowAdapter;
    }

    public CMSToWebPathAdapter getCMSToWebPathAdapter() {
        if (this.cmsToWebAdapter == null) {
            this.cmsToWebAdapter = new CMSToWebPathAdapter(this.CMSService);
        }

        return this.cmsToWebAdapter;
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
		fragmentTypes.add(new FragmentType("doc_link", "Lien portail ou Nuxeo", new LinkFragmentModule(), "link", "link"));
        fragmentTypes.add(new FragmentType("space_menubar", "MenuBar d'un Espace", new SpaceMenuBarFragmentModule(), "spaceMenubar", "spaceMenubar"));
		
		return fragmentTypes;
	}

	public static String getSearchSchema() {

		return "dublincore,common,file";

	}



    public CMSHandlerProperties getCMSDefaultPlayer(CMSServiceCtx ctx) throws Exception {
        Document doc = (Document) ctx.getDoc();
        return this.players.get("defaultPlayer").play(ctx, doc);
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

		CMSPublicationInfos pubInfos = this.getCMSService().getPublicationInfos(ctx, doc.getPath());




		List<CMSItem> navItems = null;



		if (ctx.getContextualizationBasePath() != null) {
			// Publication dans un environnement contextualisé
			// On se sert du menu de navigation et on décompose chaque niveau


			 navItems = this.getCMSService().getPortalNavigationSubitems(ctx,
					ctx.getContextualizationBasePath(), DocumentPublishSpaceNavigationCommand.computeNavPath(doc.getPath()));
		}

		if( navItems != null)	{

		    // On exclut les folderish, car ils sont présentés dans le menu en mode contextualisé


			nuxeoRequest = "ecm:parentId = '" + pubInfos.getLiveId() + "' AND ecm:mixinType != 'Folderish'";
			if( ordered) {
                nuxeoRequest += " order by ecm:pos";
            } else {
                nuxeoRequest += " order by dc:modified desc";
            }



		} else {
			nuxeoRequest = "ecm:path STARTSWITH '" + DocumentPublishSpaceNavigationCommand.computeNavPath(doc.getPath()) + "' AND ecm:mixinType != 'Folderish' ";

			if( ordered) {
                nuxeoRequest += " order by ecm:pos";
            } else {
                nuxeoRequest += " order by dc:modified desc";
            }
		}


		return nuxeoRequest;

	}

	public CMSHandlerProperties getCMSAnnonceFolderPlayer(CMSServiceCtx ctx) throws Exception {

		Document doc = (Document) ctx.getDoc();

		Map<String, String> windowProperties = new HashMap<String, String>();
        windowProperties.put("osivia.nuxeoRequest", createFolderRequest(ctx, false));		windowProperties.put("osivia.cms.style", CMSCustomizer.STYLE_EDITORIAL);
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
		windowProperties.put("osivia.nuxeoRequest", this.createFolderRequest(ctx, true));
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
		windowProperties.put("osivia.nuxeoRequest", this.createFolderRequest(ctx, true));
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
		windowProperties.put("osivia.nuxeoRequest", this.createFolderRequest(ctx, false));
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

		String url = this.portalUrlFactory.getStartPortletInRegionUrl(new PortalControllerContext(ctx.getControllerContext()),
				ctx.getPageId(), portletInstance, "virtual", "cms", windowProperties, params);

		CMSHandlerProperties linkProps = new CMSHandlerProperties();
		linkProps.setWindowProperties(windowProperties);
		linkProps.setPortletInstance(portletInstance);

		return linkProps;
	}





    /**
     * On détermine le player associé à chaque item.
     *
     * @param ctx cms context
     * @return portlet & properties
     */
	public CMSHandlerProperties getCMSPlayer(CMSServiceCtx ctx) throws Exception {

		Document doc = (Document) ctx.getDoc();

		if ("UserWorkspace".equals(doc.getType())) {
			// Pas de filtre sur les versions publiées
			ctx.setDisplayLiveVersion("1");
			return this.createPortletLink(ctx, "toutatice-portail-cms-nuxeo-fileBrowserPortletInstance", doc.getPath());
		}

		if ( ("DocumentUrlContainer".equals(doc.getType()))  ) {
            return this.getCMSUrlContainerPlayer(ctx);
        }

		if ("AnnonceFolder".equals(doc.getType()) ) {
			return this.getCMSAnnonceFolderPlayer(ctx);
		}

		if (("Folder".equals(doc.getType()) || "OrderedFolder".equals(doc.getType())) || ("Section".equals(doc.getType()))) {
            if (ctx.getContextualizationBasePath() != null) {

                CMSItem spaceConfig = this.CMSService.getSpaceConfig(ctx, ctx.getContextualizationBasePath());

                // v2.0-SP1 : Folders contextualisés dans les workspaces à afficher avec le filebrowser a la place du portlet liste
                // v2.0.5 : file explorer également sur userworkspace
                String spaceType = ((Document) spaceConfig.getNativeItem()).getType();
                if ("Workspace".equals(spaceType) || "UserWorkspace".equals(spaceType)) {

                    // if( "Workspace".equals(((Document) spaceConfig.getNativeItem()).getType())) {
                    // Pas de filtre sur les versions publiées
                    ctx.setDisplayLiveVersion("1");
                    CMSHandlerProperties props = this.createPortletLink(ctx, "toutatice-portail-cms-nuxeo-fileBrowserPortletInstance", doc.getPath());
                    props.getWindowProperties().put("osivia.title", doc.getTitle());
                    return props;
                }
            }
            //  ordre par date de modif par défaut
            if ("Folder".equals(doc.getType())) {
                return this.getCMSFolderPlayer(ctx);
            } else {
                return this.getCMSOrderedFolderPlayer(ctx);
            }

		}

		if ("PortalVirtualPage".equals(doc.getType())) {
			return this.getCMSVirtualPagePlayer(ctx);
		}


        // ========== Try to get external config for players
        // compute domain path
        String domainPath = WebConfigurationHelper.getDomainPath(ctx);

        if (domainPath != null) {
            // get configs installed in nuxeo
            WebConfiguratinQueryCommand command = new WebConfiguratinQueryCommand(domainPath, WebConfigurationType.CMSPlayer);
            Documents configs = null;
            try {
                configs = (Documents) this.CMSService.executeNuxeoCommand(ctx, command);
            } catch (Exception e) {
                // Can't get confs
            }

            if ((configs != null) && (configs.size() > 0)) {
                int i = 0;
                for (Document config : configs) {
                    String documentType = config.getProperties().getString(WebConfigurationHelper.CODE);
                    String playerInstance = config.getProperties().getString(WebConfigurationHelper.CODECOMP);

                    if (doc.getType().equals(documentType) && this.players.containsKey(playerInstance)) {

                        Map<String, String> windowProperties = new HashMap<String, String>();
                        PropertyList list = config.getProperties().getList(WebConfigurationHelper.OPTIONS);

                        // Inject defined values in conf in the player properties map
                        for (Object o : list.list()) {
                            if (o instanceof PropertyMap) {
                                PropertyMap map = (PropertyMap) o;
                                windowProperties.put(map.get(WebConfigurationHelper.OPTION_KEY).toString(), map.get(WebConfigurationHelper.OPTION_VALUE)
                                        .toString());

                            }
                        }

                        return this.players.get(playerInstance).play(ctx, doc, windowProperties);
                    }
                }
            }

        }
        return this.players.get("defaultPlayer").play(ctx, doc);

	}


	public String getDefaultExternalViewer(CMSServiceCtx ctx) {

		Document doc = (Document) ctx.getDoc();

		this.getNuxeoConnectionProps();
        String externalUrl =  NuxeoConnectionProperties.getPublicBaseUri().toString() + "/nxdoc/default/"
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
		if(("file-browser-menu-workspace".equals(ctx.getDisplayContext()))) {
            return this.getDefaultExternalViewer(ctx);
        }

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

		if (  (!"detailedView".equals(ctx.getDisplayContext()) ))
			{
			// Le download sur les fichiers doit être explicite (plus dans l'esprit GED)
			if ("File".equals(doc.getType()) && ("download".equals(ctx.getDisplayContext()))) {
				PropertyMap attachedFileProperties = doc.getProperties().getMap("file:content");
				if((attachedFileProperties != null) && !attachedFileProperties.isEmpty()){
					url = this.createPortletDelegatedFileContentLink(ctx);

                    // Gestion du back sur lien téléchargeable
                    url = this.portalUrlFactory.adaptPortalUrlToNavigation(new PortalControllerContext(ctx.getControllerContext()), url);

					downloadable = true;
				}
			}

			if ("ContextualLink".equals(doc.getType())) {
				url = this.createPortletDelegatedExternalLink(ctx);
				externalLink = true;
			}

			// Gestion des vues externes
			// Nécessaire pour poser une ancre au moment de la génération du
			// lien

			if (url == null) {

				url = this.getNuxeoNativeViewerUrl(ctx);
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

		this.getMenuBarFormater().formatContentMenuBar(ctx);

		List<MenubarItem> menuBar = (List<MenubarItem>) ctx.getRequest().getAttribute("osivia.menuBar");

		this.adaptContentMenuBar(ctx, menuBar);

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
		return this.getUserPagesLoader().computeUserPreloadedPages(cmsCtx);
	}

	/*
	 * Parsing CMS spécifiques
	 */

	public Map<String, String> parseCMSURL(CMSServiceCtx cmsCtx, String requestPath, Map<String, String> requestParameters)
			throws Exception {

		return null;
	}

	public String adaptCMSPathToWeb(CMSServiceCtx cmsCtx, String basePath, String requestPath, boolean webPath) throws CMSException {
	    return this.getCMSToWebPathAdapter().adaptCMSPathToWeb(cmsCtx, basePath, requestPath, webPath);
	}


	public Map<String, String> getDocumentConfiguration(CMSServiceCtx ctx, Document doc) throws Exception {
		return this.getCMSItemAdapter().adaptDocument( ctx,  doc);
	}

	public String addPublicationFilter(CMSServiceCtx ctx, String nuxeoRequest, String requestFilteringPolicy) throws Exception {
	/* Filtre pour sélectionner uniquement les version publiées */



		String requestFilter = "";

		if ("1".equals(ctx.getDisplayLiveVersion())) {
			// selection des versions lives : il faut exclure les proxys
			requestFilter = "ecm:mixinType != 'HiddenInNavigation' AND ecm:isProxy = 0  AND ecm:currentLifeCycleState <> 'deleted'  AND ecm:isCheckedInVersion = 0 ";
		} else {
			// sélection des folders et des documents publiés

			//requestFilter = "ecm:mixinType != 'HiddenInNavigation' AND ecm:isProxy = 1  AND ecm:currentLifeCycleState <> 'deleted' ";
			requestFilter = "ecm:isProxy = 1 AND ecm:mixinType != 'HiddenInNavigation'  AND ecm:currentLifeCycleState <> 'deleted' ";
		}

		String policyFilter = null;

		ServerInvocation invocation = ctx.getServerInvocation();
        String portalName = PageProperties.getProperties().getPagePropertiesMap().get(Constants.PORTAL_NAME);

		// Dans certaines cas, le nom du portail n'est pas connu
		// cas des stacks server (par exemple, le pre-cahrgement des pages)
		if( portalName != null) {
			PortalObjectContainer portalObjectContainer = (PortalObjectContainer) invocation.getAttribute(Scope.REQUEST_SCOPE, "osivia.portalObjectContainer");
			PortalObject po = portalObjectContainer.getObject(PortalObjectId.parse("", "/" + portalName, PortalObjectPath.CANONICAL_FORMAT));

			if (requestFilteringPolicy != null) {
                policyFilter = requestFilteringPolicy;
            } else {
				// Get portal policy filter
				String sitePolicy = po.getProperty(InternalConstants.PORTAL_PROP_NAME_CMS_REQUEST_FILTERING_POLICY);
				if( sitePolicy != null) {
				    if (InternalConstants.PORTAL_CMS_REQUEST_FILTERING_POLICY_LOCAL.equals(sitePolicy)) {
                        policyFilter = InternalConstants.PORTAL_CMS_REQUEST_FILTERING_POLICY_LOCAL;
                    }
				}   else    {
				    String portalType =  po.getProperty(InternalConstants.PORTAL_PROP_NAME_PORTAL_TYPE);
				    if( InternalConstants.PORTAL_TYPE_SPACE.equals(portalType)) {
				        policyFilter = InternalConstants.PORTAL_CMS_REQUEST_FILTERING_POLICY_LOCAL;
				    }
				}
			}

			if (InternalConstants.PORTAL_CMS_REQUEST_FILTERING_POLICY_LOCAL.equals(policyFilter)) {
				// Parcours des pages pour appliquer le filtre sur les paths

				String pathFilter = "";

				for (PortalObject child : ((Portal) po).getChildren(PortalObject.PAGE_MASK)) {
					String cmsPath = child.getDeclaredProperty("osivia.cms.basePath");
					if ((cmsPath != null) && (cmsPath.length() > 0)) {
						if (pathFilter.length() > 0) {
                            pathFilter += " OR ";
                        }
						pathFilter += "ecm:path STARTSWITH '" + cmsPath + "'";
					}
				}

				if (pathFilter.length() > 0) {
					requestFilter = requestFilter + " AND " + "(" + pathFilter + ")";
				}
			}

		}


		String extraFilter =  this.getExtraRequestFilter( ctx,  requestFilteringPolicy);
		if( extraFilter != null){
			requestFilter = requestFilter + " OR " + "(" + extraFilter + ")";
		}


		// Insertion du filtre avant le order

		String beforeOrderBy = "";
		String orderBy = "";

		try {
			Pattern ressourceExp = Pattern.compile("(.*)ORDER([ ]*)BY(.*)");

			Matcher m = ressourceExp.matcher(nuxeoRequest.toUpperCase());
			m.matches();

			if (m.groupCount() == 3) {
				beforeOrderBy = nuxeoRequest.substring(0, m.group(1).length());
				orderBy = nuxeoRequest.substring(m.group(1).length());
			}
		} catch (IllegalStateException e) {
			beforeOrderBy = nuxeoRequest;
		}

		String finalRequest = beforeOrderBy;

		if (finalRequest.length() > 0) {
            finalRequest += " AND ";
        }
		finalRequest += "(" + requestFilter + ") ";

		finalRequest += " " + orderBy;
		nuxeoRequest = finalRequest;

		return nuxeoRequest;

	}


    /**
     * Etendre la recherche à d'autres path.
     *
     * @param ctx cms context
     * @param requestFilteringPolicy policy de filtrage
     * @return code NXQL
     * @throws Exception
     */
	public String getExtraRequestFilter(CMSServiceCtx ctx, String requestFilteringPolicy) throws Exception {
        String extraRequetFilter = null;

        // compute domain path
        String domainPath = WebConfigurationHelper.getDomainPath(ctx);

        if (domainPath != null) {
            // get configs installed in nuxeo
            WebConfiguratinQueryCommand command = new WebConfiguratinQueryCommand(domainPath, WebConfigurationType.extraRequestFilter);
            Documents configs = (Documents) this.CMSService.executeNuxeoCommand(ctx, command);


            if (configs.size() > 0) {
                extraRequetFilter = "";
                int i = 0;
                for (Document config : configs) {
                    String nxqlCode = config.getProperties().getString(WebConfigurationHelper.CODE);
                    if (nxqlCode != null) {

                        // build the request
                        if (i > 0) {
                            extraRequetFilter = extraRequetFilter.concat("OR");
                        }
                        extraRequetFilter = extraRequetFilter.concat(" ").concat(nxqlCode);
                        i++;
                    }
                }
            }
        }
        // return the request
        return extraRequetFilter;

	}

protected Map<String, DocTypeDefinition> docTypes = null;
    
    protected DocTypeDefinition createDocType( String docTypeName, String displayName, boolean supportsPortalForm, List<String> portalFormSubTypes){
        
        DocTypeDefinition portalDocType = new DocTypeDefinition();
        portalDocType.setName(docTypeName);
        portalDocType.setDisplayName(displayName);
        portalDocType.setSupportingPortalForm(supportsPortalForm);
        portalDocType.setPortalFormSubTypes(portalFormSubTypes);
            
        return portalDocType;
    }
    

    
    public Map<String, DocTypeDefinition> getDocTypeDefinitions(CMSServiceCtx ctx) throws Exception {
 
        if (docTypes == null) {

            docTypes = new LinkedHashMap<String, DocTypeDefinition>();
            docTypes.put("Workspace", createDocType("Workspace", "Workspace", false, Arrays.asList("File", "Folder", "Note")));
            docTypes.put("Folder", createDocType("Folder", "Dossier", true, Arrays.asList("File", "Folder", "Note")));
            docTypes.put("File", createDocType("File", "Fichier", true, new ArrayList<String>()));
            docTypes.put("Note", createDocType("Note", "Note", true,  new ArrayList<String>()));
            docTypes.put("Annonce", createDocType("Annonce", "Annonce", true,  new ArrayList<String>()));
            docTypes.put("AnnonceFolder", createDocType("AnnonceFolder", "AnnonceFolder", false, Arrays.asList("Annonce")));
            docTypes.put("ContextualLink", createDocType("ContextualLink", "Lien", true,  new ArrayList<String>()));
            docTypes.put("DocumentUrlContainer", createDocType("DocumentUrlContainer", "Container de liens", false,  Arrays.asList("ContextualLink")));

        }
        return docTypes;

    }

    public synchronized String transformHTMLContent(CMSServiceCtx ctx, String htmlContent) throws Exception {
        Transformer transformer = WysiwygParser.getInstance().getTemplate().newTransformer();

        transformer.setParameter("bridge", new fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.XSLFunctions(this, ctx));
        OutputStream output = new ByteArrayOutputStream();
        //XMLReader parser = WysiwygParser.getInstance().getParser();
        transformer.transform(new SAXSource(this.parser, new InputSource(new StringReader(htmlContent))), new StreamResult(
                output));

        return output.toString();

    }

}
