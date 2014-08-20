/*
 * (C) Copyright 2014 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 *
 *
 */
package fr.toutatice.portail.cms.nuxeo.portlets.customizer;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.portlet.PortletContext;
import javax.portlet.ResourceURL;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
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
import org.osivia.portal.api.directory.IDirectoryService;
import org.osivia.portal.api.internationalization.Bundle;
import org.osivia.portal.api.internationalization.IBundleFactory;
import org.osivia.portal.api.internationalization.IInternationalizationService;
import org.osivia.portal.api.menubar.MenubarItem;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.api.urls.Link;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSHandlerProperties;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSItemType;
import org.osivia.portal.core.cms.CMSPage;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.constants.InternalConstants;
import org.osivia.portal.core.page.PageProperties;
import org.osivia.portal.core.web.IWebIdService;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import fr.toutatice.portail.cms.nuxeo.api.domain.Comment;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCommentsService;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCustomizer;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoConnectionProperties;
import fr.toutatice.portail.cms.nuxeo.portlets.comments.CommentsFormatter;
import fr.toutatice.portail.cms.nuxeo.portlets.comments.NuxeoCommentsServiceImpl;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.CMSItemAdapter;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.DefaultPlayer;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.DocumentPictureFragmentModule;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.EditableWindowAdapter;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.IPlayer;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.LinkFragmentModule;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.MenuBarFormater;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.NavigationItemAdapter;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.NavigationPictureFragmentModule;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.PropertyFragmentModule;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.SitePictueFragmentModule;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.SpaceMenuBarFragmentModule;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.UserPagesLoader;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.WebConfiguratinQueryCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.WebConfiguratinQueryCommand.WebConfigurationType;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.WebConfigurationHelper;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.WysiwygParser;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.ZoomFragmentModule;
import fr.toutatice.portail.cms.nuxeo.portlets.service.CMSService;
import fr.toutatice.portail.cms.nuxeo.portlets.service.DocumentPublishSpaceNavigationCommand;

/**
 * Default CMS customizer.
 *
 * @see INuxeoCustomizer
 */
public class DefaultCMSCustomizer implements INuxeoCustomizer {

    /** Logger. */
    protected static final Log logger = LogFactory.getLog(DefaultCMSCustomizer.class);

    /* Default style for lists */
    /** Style "mini". */
    public static final String STYLE_MINI = "mini";
    /** Style "normal". */
    public static final String STYLE_NORMAL = "normal";
    /** Style "detailed". */
    public static final String STYLE_DETAILED = "detailed";
    /** Style "editorial". */
    public static final String STYLE_EDITORIAL = "editorial";

    /** Default schemas. */
    public static final String DEFAULT_SCHEMAS = "dublincore,common, toutatice, file";
    /** Template "download". */
    public static final String TEMPLATE_DOWNLOAD = "download";

    /** servlet url for avatars */
    private static final String AVATAR_SERVLET = "/toutatice-portail-cms-nuxeo/avatar?username=";


    /** Portlet context. */
    private final PortletContext portletCtx;
    /** Portal URL factory. */
    private final IPortalUrlFactory portalUrlFactory;
    /** Bundle factory. */
    private final IBundleFactory bundleFactory;

    /** User pages loader. */
    private UserPagesLoader userPagesLoader;
    /** Menu bar formatter. */
    private MenuBarFormater menuBarFormater;
    /** Navigation item adapter. */
    private NavigationItemAdapter navigationItemAdapter;
    /** CMS item adapter. */
    private CMSItemAdapter cmsItemAdapter;
    /** Nuxeo connection properties. */
    private NuxeoConnectionProperties nuxeoConnection;
    /** XML parser. */
    private XMLReader parser;
    /** Editable window adapter. */
    private EditableWindowAdapter editableWindowAdapter;

    /** Nuxeo comments service. */
    private INuxeoCommentsService commentsService;
    /** Class loader. */
    private ClassLoader cl;

    /** CMS item types. */
    private Map<String, CMSItemType> cmsItemTypes;

    /** CMS Players. */
    private final Map<String, IPlayer> players;

    /** CMS service. */
    private CMSService cmsService;

    /** WEBID service. */
    private IWebIdService webIdService;

    /** Directory service */
    private IDirectoryService directoryService;

    private Map<String, String> avatarMap = new HashMap<String, String>();


    /**
     * Constructor.
     *
     * @param ctx portlet context
     */
    public DefaultCMSCustomizer(PortletContext ctx) {
        super();
        // Portlet context
        this.portletCtx = ctx;

        // Portal URL factory
        this.portalUrlFactory = (IPortalUrlFactory) this.portletCtx.getAttribute(Constants.URL_SERVICE_NAME);

        // Bundle factory
        IInternationalizationService internationalizationService = (IInternationalizationService) this.portletCtx
                .getAttribute(Constants.INTERNATIONALIZATION_SERVICE_NAME);
        this.bundleFactory = internationalizationService.getBundleFactory(this.getClass().getClassLoader());

        // initialise le player view document par défaut
        this.players = new HashMap<String, IPlayer>();
        this.players.put("defaultPlayer", new DefaultPlayer());

        try {
            // Initialisé ici pour résoudre problème de classloader
            this.cl = Thread.currentThread().getContextClassLoader();

            this.parser = WysiwygParser.getInstance().getParser();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public IWebIdService getWebIdService() {
        if (this.webIdService == null) {
            this.webIdService = (IWebIdService) this.portletCtx.getAttribute("webIdService");
        }

        return this.webIdService;
    }

    /**
     * Get Nuxeo connection properties.
     *
     * @return Nuxeo connection properties
     */
    public NuxeoConnectionProperties getNuxeoConnectionProps() {
        if (this.nuxeoConnection == null) {
            this.nuxeoConnection = new NuxeoConnectionProperties();
        }
        return this.nuxeoConnection;
    }


    /**
     * Get user pages loader.
     *
     * @return user pages loader
     */
    public UserPagesLoader getUserPagesLoader()	{
        if (this.userPagesLoader == null) {
            this.userPagesLoader = new UserPagesLoader(this.portletCtx, this, this.cmsService);
        }
        return this.userPagesLoader;
    }


    /**
     * Get menu bar formatter.
     *
     * @return menu bar formatter
     */
    public MenuBarFormater getMenuBarFormater() {
        if (this.menuBarFormater == null) {
            this.menuBarFormater = new MenuBarFormater(this.portletCtx, this, this.cmsService);
        }
        return this.menuBarFormater;
    }


    /**
     * Get navigation item adapter.
     *
     * @return navigation item adapter
     */
    public NavigationItemAdapter getNavigationItemAdapter() {
        if (this.navigationItemAdapter == null) {
            this.navigationItemAdapter = new NavigationItemAdapter(this.portletCtx, this, this.cmsService);
        }
        return this.navigationItemAdapter;
    }


    /**
     * Get CMS item adapter.
     *
     * @return CMS item adapter
     */
    public CMSItemAdapter getCMSItemAdapter() {
        if (this.cmsItemAdapter == null) {
            this.cmsItemAdapter = new CMSItemAdapter(this.portletCtx, this, this.cmsService);
        }
        return this.cmsItemAdapter;
    }


    /**
     * EditableWindowAdapter permet de gérer les types de EditableWindow affichables.
     *
     * @return Instance du EditableWindowAdapter
     */
    public EditableWindowAdapter getEditableWindowAdapter() {
        if (this.editableWindowAdapter == null) {
            this.editableWindowAdapter = new EditableWindowAdapter();
        }
        return this.editableWindowAdapter;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public INuxeoCommentsService getNuxeoCommentsService() {
        if (this.commentsService == null) {
            this.commentsService = new NuxeoCommentsServiceImpl(this.cmsService, this.directoryService);
        }
        return this.commentsService;
    }


    /**
     * Get templates list.
     *
     * @return template list
     */
    public static List<ListTemplate> getListTemplates() {
        List<ListTemplate> templates = new ArrayList<ListTemplate>();
        templates.add(new ListTemplate(STYLE_MINI, "Minimal [titre]", DEFAULT_SCHEMAS));
        templates.add(new ListTemplate(STYLE_NORMAL, "Normal [titre, date]", DEFAULT_SCHEMAS));
        templates.add(new ListTemplate(STYLE_DETAILED, "Détaillé [titre, description, date, ...]", DEFAULT_SCHEMAS));
        templates.add(new ListTemplate(STYLE_EDITORIAL, "Editorial [vignette, titre, description]", DEFAULT_SCHEMAS));
        return templates;
    }


    /**
     * Get fragments list.
     *
     * @return fragments list
     */
    public static List<FragmentType> getFragmentTypes() {
        List<FragmentType> fragmentTypes = new ArrayList<FragmentType>();
        fragmentTypes.add(new FragmentType("text_property", "Propriété texte", new PropertyFragmentModule(), "property-text", "property"));
        fragmentTypes.add(new FragmentType("html_property", "Propriété html", new PropertyFragmentModule(), "property-html", "property"));
        fragmentTypes.add(new FragmentType(ZoomFragmentModule.ID, ZoomFragmentModule.DESC, new ZoomFragmentModule(), ZoomFragmentModule.JSP,
                ZoomFragmentModule.ADMIN_JSP));
        fragmentTypes.add(new FragmentType("navigation_picture", "Visuel navigation", new NavigationPictureFragmentModule(), "navigation-picture", "navigation"));
        fragmentTypes.add(new FragmentType("document_picture", "Image jointe", new DocumentPictureFragmentModule(), "document-picture", "document-picture"));
        fragmentTypes.add(new FragmentType("doc_link", "Lien portail ou Nuxeo", new LinkFragmentModule(), "link", "link"));
        fragmentTypes.add(new FragmentType("space_menubar", "MenuBar d'un Espace", new SpaceMenuBarFragmentModule(), "space-menubar", "empty"));
        fragmentTypes.add(new FragmentType("site_picture", "Visuel site", new SitePictueFragmentModule(), "site-picture", "site-picture"));
        return fragmentTypes;
    }


    /**
     * Get search schema.
     *
     * @return search schema
     */
    public static String getSearchSchema() {
        return "dublincore,common,file,toutatice";
    }


    /**
     * Get CMS default player.
     *
     * @param ctx CMS context
     * @return CMS default player
     * @throws Exception
     */
    public CMSHandlerProperties getCMSDefaultPlayer(CMSServiceCtx ctx) throws Exception {
        Document doc = (Document) ctx.getDoc();
        return this.players.get("defaultPlayer").play(ctx, doc);
    }


    /**
     * Gére les folders 'hiddenInNavigation'.
     * Les fils d'un folder 'hiddenInNavigation' sont directement rattachés au parent.
     *
     * @param ctx CMS context
     * @param ordered ordered indicator
     * @return Nuxeo request
     * @throws CMSException
     */
    protected String createFolderRequest(CMSServiceCtx ctx, boolean ordered) throws CMSException {
        String nuxeoRequest = null;

        Document doc = (Document) ctx.getDoc();

        CMSPublicationInfos pubInfos = this.cmsService.getPublicationInfos(ctx, doc.getPath());

        List<CMSItem> navItems = null;

        if (ctx.getContextualizationBasePath() != null) {
            // Publication dans un environnement contextualisé
            // On se sert du menu de navigation et on décompose chaque niveau
            navItems = this.cmsService.getPortalNavigationSubitems(ctx, ctx.getContextualizationBasePath(),
                    DocumentPublishSpaceNavigationCommand.computeNavPath(doc.getPath()));
        }

        if (navItems != null) {
            // On exclut les folderish, car ils sont présentés dans le menu en mode contextualisé
            nuxeoRequest = "ecm:parentId = '" + pubInfos.getLiveId() + "' AND ecm:mixinType != 'Folderish'";
            if (ordered) {
                nuxeoRequest += " order by ecm:pos";
            } else {
                nuxeoRequest += " order by dc:modified desc";
            }
        } else {
            nuxeoRequest = "ecm:path STARTSWITH '" + DocumentPublishSpaceNavigationCommand.computeNavPath(doc.getPath())
                    + "' AND ecm:mixinType != 'Folderish' ";

            if (ordered) {
                nuxeoRequest += " order by ecm:pos";
            } else {
                nuxeoRequest += " order by dc:modified desc";
            }
        }

        return nuxeoRequest;
    }


    /**
     * Get CMS annonce folder player.
     *
     * @param ctx CMS context
     * @return CMS annonce folder player
     * @throws CMSException
     */
    public CMSHandlerProperties getCMSAnnonceFolderPlayer(CMSServiceCtx ctx) throws CMSException {
        Document doc = (Document) ctx.getDoc();

        Map<String, String> windowProperties = new HashMap<String, String>();
        windowProperties.put("osivia.nuxeoRequest", this.createFolderRequest(ctx, false));
        windowProperties.put("osivia.cms.style", CMSCustomizer.STYLE_EDITORIAL);
        windowProperties.put("osivia.hideDecorators", "1");
        windowProperties.put("theme.dyna.partial_refresh_enabled", "false");
        windowProperties.put(Constants.WINDOW_PROP_SCOPE, ctx.getScope());
        windowProperties.put(Constants.WINDOW_PROP_VERSION, ctx.getDisplayLiveVersion());
        windowProperties.put("osivia.cms.hideMetaDatas", "1");
        windowProperties.put("osivia.title", "Annonces " + doc.getTitle());

        CMSHandlerProperties linkProps = new CMSHandlerProperties();
        linkProps.setWindowProperties(windowProperties);
        linkProps.setPortletInstance("toutatice-portail-cms-nuxeo-viewListPortletInstance");

        return linkProps;
    }


    /**
     * Get CMS ordered folder player.
     *
     * @param ctx CMS context
     * @return CMS ordered folder player
     * @throws CMSException
     */
    public CMSHandlerProperties getCMSOrderedFolderPlayer(CMSServiceCtx ctx) throws CMSException {
        Document doc = (Document) ctx.getDoc();

        Map<String, String> windowProperties = new HashMap<String, String>();
        windowProperties.put("osivia.nuxeoRequest", this.createFolderRequest(ctx, true));
        windowProperties.put("osivia.cms.style", CMSCustomizer.STYLE_EDITORIAL);
        windowProperties.put("osivia.hideDecorators", "1");
        windowProperties.put("theme.dyna.partial_refresh_enabled", "false");
        windowProperties.put(Constants.WINDOW_PROP_SCOPE, ctx.getScope());
        windowProperties.put(Constants.WINDOW_PROP_VERSION, ctx.getDisplayLiveVersion());
        windowProperties.put("osivia.cms.hideMetaDatas", "1");
        windowProperties.put("osivia.title", "Dossier " + doc.getTitle());
        windowProperties.put("osivia.cms.pageSizeMax", "10");

        CMSHandlerProperties linkProps = new CMSHandlerProperties();
        linkProps.setWindowProperties(windowProperties);
        linkProps.setPortletInstance("toutatice-portail-cms-nuxeo-viewListPortletInstance");

        return linkProps;
    }


    /**
     * Get CMS URL container player.
     *
     * @param ctx CMS context
     * @return CMS URL container player
     * @throws CMSException
     */
    public CMSHandlerProperties getCMSUrlContainerPlayer(CMSServiceCtx ctx) throws CMSException {
        Map<String, String> windowProperties = new HashMap<String, String>();
        windowProperties.put("osivia.nuxeoRequest", this.createFolderRequest(ctx, true));
        windowProperties.put("osivia.cms.style", CMSCustomizer.STYLE_EDITORIAL);
        windowProperties.put("osivia.hideDecorators", "1");
        windowProperties.put("theme.dyna.partial_refresh_enabled", "false");
        windowProperties.put(Constants.WINDOW_PROP_SCOPE, ctx.getScope());
        windowProperties.put(Constants.WINDOW_PROP_VERSION, ctx.getDisplayLiveVersion());
        windowProperties.put("osivia.cms.hideMetaDatas", ctx.getHideMetaDatas());
        windowProperties.put("osivia.cms.pageSizeMax", "10");
        // JSS V3.1 : incompatible avec refresh CMS de type portlets
        // windowProperties.put("osivia.title", "Liste de liens");

        CMSHandlerProperties linkProps = new CMSHandlerProperties();
        linkProps.setWindowProperties(windowProperties);
        linkProps.setPortletInstance("toutatice-portail-cms-nuxeo-viewListPortletInstance");

        return linkProps;
    }


    /**
     * Get CMS folder player.
     *
     * @param ctx CMS context
     * @return CMS folder player
     * @throws CMSException
     */
    public CMSHandlerProperties getCMSFolderPlayer(CMSServiceCtx ctx) throws CMSException {
        Document doc = (Document) ctx.getDoc();

        Map<String, String> windowProperties = new HashMap<String, String>();
        windowProperties.put("osivia.nuxeoRequest", this.createFolderRequest(ctx, false));
        windowProperties.put("osivia.cms.style", CMSCustomizer.STYLE_EDITORIAL);
        windowProperties.put("osivia.hideDecorators", "1");
        windowProperties.put("theme.dyna.partial_refresh_enabled", "false");
        windowProperties.put(Constants.WINDOW_PROP_SCOPE, ctx.getScope());
        windowProperties.put(Constants.WINDOW_PROP_VERSION, ctx.getDisplayLiveVersion());
        windowProperties.put("osivia.cms.hideMetaDatas", ctx.getHideMetaDatas());
        windowProperties.put("osivia.title", "Dossier " + doc.getTitle());
        windowProperties.put("osivia.cms.pageSizeMax", "10");

        CMSHandlerProperties linkProps = new CMSHandlerProperties();
        linkProps.setWindowProperties(windowProperties);
        linkProps.setPortletInstance("toutatice-portail-cms-nuxeo-viewListPortletInstance");

        return linkProps;
    }


    /**
     * Get CMS section player.
     *
     * @param ctx CMS context
     * @return CMS section player
     */
    public CMSHandlerProperties getCMSSectionPlayer(CMSServiceCtx ctx) {
        Document doc = (Document) ctx.getDoc();

        Map<String, String> windowProperties = new HashMap<String, String>();

        windowProperties.put("osivia.nuxeoRequest", "ecm:path STARTSWITH '" + doc.getPath() + "' AND ecm:mixinType != 'Folderish' ORDER BY dc:modified DESC");
        windowProperties.put("osivia.cms.style", CMSCustomizer.STYLE_EDITORIAL);
        windowProperties.put("osivia.hideDecorators", "1");
        windowProperties.put("theme.dyna.partial_refresh_enabled", "false");
        windowProperties.put(Constants.WINDOW_PROP_SCOPE, ctx.getScope());
        windowProperties.put(Constants.WINDOW_PROP_VERSION, ctx.getDisplayLiveVersion());
        windowProperties.put("osivia.cms.hideMetaDatas", ctx.getHideMetaDatas());
        windowProperties.put("osivia.title", "Dossier " + doc.getTitle());
        windowProperties.put("osivia.cms.pageSizeMax", "10");

        CMSHandlerProperties linkProps = new CMSHandlerProperties();
        linkProps.setWindowProperties(windowProperties);
        linkProps.setPortletInstance("toutatice-portail-cms-nuxeo-viewListPortletInstance");

        return linkProps;
    }


    /**
     * Get CMS virtual page player.
     *
     * @param ctx CMS context
     * @return CMS virtual page player
     */
    public CMSHandlerProperties getCMSVirtualPagePlayer(CMSServiceCtx ctx) {
        Document doc = (Document) ctx.getDoc();

        Map<String, String> windowProperties = new HashMap<String, String>();
        windowProperties.put("osivia.nuxeoRequest", doc.getString("ttc:queryPart"));
        windowProperties.put("osivia.cms.style", CMSCustomizer.STYLE_EDITORIAL);
        windowProperties.put("osivia.hideDecorators", "1");
        windowProperties.put("theme.dyna.partial_refresh_enabled", "false");
        windowProperties.put(Constants.WINDOW_PROP_SCOPE, ctx.getScope());
        //windowProperties.put(Constants.WINDOW_PROP_VERSION, ctx.getDisplayLiveVersion());
        windowProperties.put("osivia.cms.hideMetaDatas", ctx.getHideMetaDatas());
        windowProperties.put("osivia.title", "Dossier " + doc.getTitle());
        windowProperties.put("osivia.cms.pageSizeMax", "10");

        CMSHandlerProperties linkProps = new CMSHandlerProperties();
        linkProps.setWindowProperties(windowProperties);
        linkProps.setPortletInstance("toutatice-portail-cms-nuxeo-viewListPortletInstance");

        return linkProps;
    }


    /**
     * Create portlet link.
     *
     * @param ctx CMS context
     * @param portletInstance portlet instance
     * @param uid UID
     * @return portlet link
     */
    public CMSHandlerProperties createPortletLink(CMSServiceCtx ctx, String portletInstance, String uid) {
        Map<String, String> windowProperties = new HashMap<String, String>();
        windowProperties.put(Constants.WINDOW_PROP_SCOPE, ctx.getScope());
        windowProperties.put(Constants.WINDOW_PROP_VERSION, ctx.getDisplayLiveVersion());
        windowProperties.put("osivia.cms.hideMetaDatas", ctx.getHideMetaDatas());
        windowProperties.put(Constants.WINDOW_PROP_URI, uid);
        windowProperties.put("osivia.cms.publishPathAlreadyConverted", "1");
        windowProperties.put("osivia.hideDecorators", "1");
        windowProperties.put("theme.dyna.partial_refresh_enabled", "false");

        CMSHandlerProperties linkProps = new CMSHandlerProperties();
        linkProps.setWindowProperties(windowProperties);
        linkProps.setPortletInstance(portletInstance);

        return linkProps;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public CMSHandlerProperties getCMSPlayer(CMSServiceCtx ctx) throws Exception {
        Document doc = (Document) ctx.getDoc();

        if ("UserWorkspace".equals(doc.getType())) {
            // Pas de filtre sur les versions publiées
            ctx.setDisplayLiveVersion("1");
            return this.createPortletLink(ctx, "toutatice-portail-cms-nuxeo-fileBrowserPortletInstance", doc.getPath());
        }

        if (("DocumentUrlContainer".equals(doc.getType()))) {
            return this.getCMSUrlContainerPlayer(ctx);
        }

        if ("AnnonceFolder".equals(doc.getType())) {
            return this.getCMSAnnonceFolderPlayer(ctx);
        }

        if (("Folder".equals(doc.getType()) || "OrderedFolder".equals(doc.getType())) || ("Section".equals(doc.getType()))) {
            // if (ctx.getContextualizationBasePath() != null)

            // Test JSS (tant que pas d'objet affichable en liste dans les workspace open-toutatice)
            if ((ctx.getContextualizationBasePath() != null) && !doc.getTitle().startsWith("test-list")) {

                CMSItem spaceConfig = this.cmsService.getSpaceConfig(ctx, ctx.getContextualizationBasePath());

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
            // ordre par date de modif par défaut
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

               configs = WebConfigurationHelper.executeWebConfigCmd(ctx, this.cmsService, command);

            } catch (Exception e) {
                // Can't get confs
            }

            if ((configs != null) && (configs.size() > 0)) {
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


    /**
     * Get default external viewer.
     *
     * @param ctx CMS context
     * @return default external viewer
     */
    public String getDefaultExternalViewer(CMSServiceCtx ctx) {
        Document doc = (Document) ctx.getDoc();

        this.getNuxeoConnectionProps();
        String externalUrl =  NuxeoConnectionProperties.getPublicBaseUri().toString() + "/nxdoc/default/"
                + doc.getId() + "/view_documents";

        // Par défaut, lien direct sur Nuxeo
        return externalUrl;
    }


    /**
     * Create portlet delegated external link.
     *
     * @param ctx CMS context
     * @return portlet delegated external link
     */
    public String createPortletDelegatedExternalLink(CMSServiceCtx ctx) {
        Document doc = (Document) ctx.getDoc();

        ResourceURL resourceURL = ctx.getResponse().createResourceURL();
        resourceURL.setResourceID(doc.getId());
        resourceURL.setParameter("type", "link");
        // ne marche pas : bug JBP
        // resourceURL.setCacheability(ResourceURL.PORTLET);

        return resourceURL.toString();
    }


    /**
     * Create portlet delegated file content link.
     *
     * @param ctx CMS context
     * @return portlet delegated file content link
     */
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


    /**
     * Get Nuxeo native viewer URL.
     *
     * @param ctx CMS context
     * @return Nuxeo native viewer URL
     */
    public String getNuxeoNativeViewerUrl(CMSServiceCtx ctx) {
        if (("file-browser-menu-workspace".equals(ctx.getDisplayContext()))) {
            return this.getDefaultExternalViewer(ctx);
        }
        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Link createCustomLink(CMSServiceCtx ctx) throws Exception {
        Document doc = (Document) ctx.getDoc();

        String url = null;
        boolean externalLink = false;
        boolean downloadable = false;

        if (!"detailedView".equals(ctx.getDisplayContext())) {
            // Le download sur les fichiers doit être explicite (plus dans l'esprit GED)
            if ("File".equals(doc.getType()) && ("download".equals(ctx.getDisplayContext()))) {
                PropertyMap attachedFileProperties = doc.getProperties().getMap("file:content");
                if ((attachedFileProperties != null) && !attachedFileProperties.isEmpty()) {
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
            // Nécessaire pour poser une ancre au moment de la génération du lien
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


    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public void formatContentMenuBar(CMSServiceCtx ctx) throws Exception {
        this.getMenuBarFormater().formatContentMenuBar(ctx);
        List<MenubarItem> menuBar = (List<MenubarItem>) ctx.getRequest().getAttribute(Constants.PORTLET_ATTR_MENU_BAR);
        this.adaptContentMenuBar(ctx, menuBar);
    }


    /**
     * Customize menu bar items.
     *
     * @param ctx CMS context
     * @param menuBar menu bar
     */
    protected void adaptContentMenuBar(CMSServiceCtx ctx, List<MenubarItem> menuBar) {
    }


    /**
     * Compute preloading pages when user log in.
     *
     * @param cmsCtx CMS context
     * @return preloaded pages
     * @throws Exception
     */
    public List<CMSPage> computeUserPreloadedPages(CMSServiceCtx cmsCtx) throws Exception {
        return this.getUserPagesLoader().computeUserPreloadedPages(cmsCtx);
    }


    /**
     * Parse specified CMS URL.
     *
     * @param cmsCtx CMS context
     * @param requestPath request path
     * @param requestParameters request parameters
     * @return CMS URL
     * @throws Exception
     */
    public Map<String, String> parseCMSURL(CMSServiceCtx cmsCtx, String requestPath, Map<String, String> requestParameters) throws Exception {
        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getDocumentConfiguration(CMSServiceCtx ctx, Document doc) throws Exception {
        return this.getCMSItemAdapter().adaptDocument(ctx, doc);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String addPublicationFilter(CMSServiceCtx ctx, String nuxeoRequest, String requestFilteringPolicy) throws Exception {
        /* Filtre pour sélectionner uniquement les version publiées */
        String requestFilter = "";

        if ("1".equals(ctx.getDisplayLiveVersion())) {
            // selection des versions lives : il faut exclure les proxys
            requestFilter = "ecm:mixinType != 'HiddenInNavigation' AND ecm:isProxy = 0  AND ecm:currentLifeCycleState <> 'deleted'  AND ecm:isCheckedInVersion = 0 ";
        } else {
            // sélection des folders et des documents publiés

            // requestFilter = "ecm:mixinType != 'HiddenInNavigation' AND ecm:isProxy = 1  AND ecm:currentLifeCycleState <> 'deleted' ";
            requestFilter = "ecm:isProxy = 1 AND ecm:mixinType != 'HiddenInNavigation'  AND ecm:currentLifeCycleState <> 'deleted' ";
        }

        String policyFilter = null;

        ServerInvocation invocation = ctx.getServerInvocation();

        String portalName = null;


        // Cas des chargement asynchrones : pas de contexte
        if(invocation != null)
            portalName = PageProperties.getProperties().getPagePropertiesMap().get(Constants.PORTAL_NAME);

        // Dans certaines cas, le nom du portail n'est pas connu
        // cas des stacks server (par exemple, le pre-cahrgement des pages)
        if (portalName != null) {
            PortalObjectContainer portalObjectContainer = (PortalObjectContainer) invocation.getAttribute(Scope.REQUEST_SCOPE, "osivia.portalObjectContainer");
            PortalObject po = portalObjectContainer.getObject(PortalObjectId.parse("", "/" + portalName, PortalObjectPath.CANONICAL_FORMAT));

            if (requestFilteringPolicy != null) {
                policyFilter = requestFilteringPolicy;
            } else {
                // Get portal policy filter
                String sitePolicy = po.getProperty(InternalConstants.PORTAL_PROP_NAME_CMS_REQUEST_FILTERING_POLICY);
                if (sitePolicy != null) {
                    if (InternalConstants.PORTAL_CMS_REQUEST_FILTERING_POLICY_LOCAL.equals(sitePolicy)) {
                        policyFilter = InternalConstants.PORTAL_CMS_REQUEST_FILTERING_POLICY_LOCAL;
                    }
                } else {
                    String portalType = po.getProperty(InternalConstants.PORTAL_PROP_NAME_PORTAL_TYPE);
                    if (InternalConstants.PORTAL_TYPE_SPACE.equals(portalType)) {
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

            String extraFilter = this.getExtraRequestFilter(ctx, requestFilteringPolicy);
            if (extraFilter != null) {
                requestFilter = requestFilter + " OR " + "(" + extraFilter + ")";
            }
        }

        // Insertion du filtre avant le order
        String beforeOrderBy = "";
        String orderBy = "";

        String editedNuxeoRequest = nuxeoRequest;
        try {
            Pattern ressourceExp = Pattern.compile("(.*)ORDER([ ]*)BY(.*)");

            Matcher m = ressourceExp.matcher(editedNuxeoRequest.toUpperCase());
            m.matches();

            if (m.groupCount() == 3) {
                beforeOrderBy = editedNuxeoRequest.substring(0, m.group(1).length());
                orderBy = editedNuxeoRequest.substring(m.group(1).length());
            }
        } catch (IllegalStateException e) {
            beforeOrderBy = editedNuxeoRequest;
        }

        String finalRequest = beforeOrderBy;

        if (finalRequest.length() > 0) {
            finalRequest += " AND ";
        }
        finalRequest += "(" + requestFilter + ") ";

        finalRequest += " " + orderBy;
        editedNuxeoRequest = finalRequest;

        return editedNuxeoRequest;
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
            Documents configs = null;

            configs = WebConfigurationHelper.executeWebConfigCmd(ctx, this.cmsService, command);


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


    /**
     * {@inheritDoc}
     */
    @Override
    public String transformHTMLContent(CMSServiceCtx ctx, String htmlContent) throws Exception {
        ClassLoader originalCL = Thread.currentThread().getContextClassLoader();

        // L'instanciation du parser Neko nécessite de passer dans le classloader du CMSCustomizer
        // (Sinon, on n'arrive pas à trouver la classe du parser)
        Thread.currentThread().setContextClassLoader(this.cl);

        try {
            Transformer transformer = WysiwygParser.getInstance().getTemplate().newTransformer();

            transformer.setParameter("bridge", new fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.XSLFunctions(this, ctx));
            OutputStream output = new ByteArrayOutputStream();
            XMLReader parser = WysiwygParser.getInstance().getParser();
            transformer.transform(new SAXSource(parser, new InputSource(new StringReader(htmlContent))), new StreamResult(output));

            return output.toString();
        } finally {
            Thread.currentThread().setContextClassLoader(originalCL);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, CMSItemType> getCMSItemTypes() {
        if (this.cmsItemTypes == null) {
            List<CMSItemType> defaultTypes = this.getDefaultCMSItemTypes();
            this.cmsItemTypes = new LinkedHashMap<String, CMSItemType>(defaultTypes.size());
            for (CMSItemType defaultType : defaultTypes) {
                this.cmsItemTypes.put(defaultType.getName(), defaultType);
            }
        }
        return this.cmsItemTypes;
    }


    /**
     * Get default CMS item types.
     *
     * @return default CMS item types
     */
    private List<CMSItemType> getDefaultCMSItemTypes() {
        List<CMSItemType> defaultTypes = new ArrayList<CMSItemType>();

        // Workspace
        defaultTypes.add(new CMSItemType("Workspace", true, false, true, false, false, Arrays.asList("File", "Folder", "Note"), "/default/templates/workspace"));
        // Portal site
        defaultTypes.add(new CMSItemType("PortalSite", true, false, true, true, true, Arrays.asList("File", "Folder", "Note"), null));
        // Portal page
        defaultTypes.add(new CMSItemType("PortalPage", true, true, true, true, true, Arrays.asList("File", "Folder", "Note"), null));
        // Folder
        defaultTypes.add(new CMSItemType("Folder", true, true, false, false, true, Arrays.asList("File", "Folder", "Note"), null));
        // File
        defaultTypes.add(new CMSItemType("File", false, false, false, false, true, new ArrayList<String>(0), null));
        // Note
        defaultTypes.add(new CMSItemType("Note", false, false, false, false, true, new ArrayList<String>(0), null));
        // Annonce
        defaultTypes.add(new CMSItemType("Annonce", false, false, false, false, true, new ArrayList<String>(0), null));
        // Annonce folder
        defaultTypes.add(new CMSItemType("AnnonceFolder", true, true, false, false, false, Arrays.asList("Annonce"), null));
        // Contextual link
        defaultTypes.add(new CMSItemType("ContextualLink", false, false, false, false, true, new ArrayList<String>(0), null));
        // Document URL container
        defaultTypes.add(new CMSItemType("DocumentUrlContainer", true, true, false, false, false, Arrays.asList("ContextualLink"), null));

        defaultTypes.add(new CMSItemType("OrderedFolder", true, true, true, false, true, Arrays.asList("File", "Folder", "Note"), null));



        return defaultTypes;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommentsHTMLContent(CMSServiceCtx cmsContext, Document document) throws CMSException {
        List<Comment> comments = this.getNuxeoCommentsService().getDocumentComments(cmsContext, document);
        CommentsFormatter formatter = new CommentsFormatter(comments);
        return formatter.generateHTMLContent();
    }


    /**
     * Getter for cmsService.
     *
     * @return the cmsService
     */
    public CMSService getCmsService() {
        return this.cmsService;
    }

    /**
     * Setter for cmsService.
     *
     * @param cmsService the cmsService to set
     */
    public void setCmsService(CMSService cmsService) {
        this.cmsService = cmsService;
    }

    /**
     * @return the directoryService
     */
    public IDirectoryService getDirectoryService() {
        return this.directoryService;
    }


    /**
     * @param directoryService the directoryService to set
     */
    public void setDirectoryService(IDirectoryService directoryService) {
        this.directoryService = directoryService;
    }

    /**
     * Getter for portletCtx.
     *
     * @return the portletCtx
     */
    public PortletContext getPortletCtx() {
        return this.portletCtx;
    }

    /**
     * Setter for navigationItemAdapter.
     *
     * @param navigationItemAdapter the navigationItemAdapter to set
     */
    public void setNavigationItemAdapter(NavigationItemAdapter navigationItemAdapter) {
        this.navigationItemAdapter = navigationItemAdapter;
    }

    /**
     * Getter for parser.
     *
     * @return the parser
     */
    public XMLReader getParser() {
        return this.parser;
    }

    /**
     * Setter for parser.
     *
     * @param parser the parser to set
     */
    public void setParser(XMLReader parser) {
        this.parser = parser;
    }


    /*
     * @see fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCustomizer#getContentWebIdAwarePath(org.osivia.portal.core.cms.CMSServiceCtx)
     */
    @Override

    public String getContentWebIdPath(CMSServiceCtx cmsCtx)  {

        Document doc = (Document) cmsCtx.getDoc();

        String webId = doc.getString("ttc:webid");

        String domainId = doc.getString("ttc:domainID");

        String permLinkPath = ((Document) (cmsCtx.getDoc())).getPath();


        // webId and domainId have no signification without each other
        if (StringUtils.isNotEmpty(webId) && StringUtils.isNotEmpty(domainId)) {

            String explicitUrl = doc.getString("ttc:explicitUrl");
            String extension = doc.getString("ttc:extensionUrl");


            Map<String, String> properties = new HashMap<String, String>();
            if (domainId != null) {
                properties.put(IWebIdService.DOMAIN_ID, domainId);
            }
            if (explicitUrl != null) {
                properties.put(IWebIdService.EXPLICIT_URL, explicitUrl);
            }
            if (extension != null) {
                properties.put(IWebIdService.EXTENSION_URL, extension);
            }
            CMSItem cmsItem = new CMSItem(doc.getPath(), webId, properties, doc);

            permLinkPath = this.getWebIdService().itemToPageUrl(cmsItem);

        }

        return permLinkPath;
    }

    @Override
    public Link getUserAvatar(CMSServiceCtx cmsCtx, String username) throws CMSException {

        String src = "";
        try {

            // get timestamp defined previously
            String avatarTime = this.avatarMap.get(username);

            if (avatarTime == null) {
                // if not defined, set ie
                avatarTime = this.refreshUserAvatar(cmsCtx, username);
            }

            // timestamp is concated in the url to control the client cache
            src = AVATAR_SERVLET.concat(URLEncoder.encode(username, "UTF-8")).concat("&t=").concat(avatarTime.toString());
        } catch (UnsupportedEncodingException e) {
            throw new CMSException(e);
        }

        return new Link(src, false);
    }

    @Override
    public String refreshUserAvatar(CMSServiceCtx cmsCtx, String username) {

        // renew the timestamp and map it to the user
        String avatarTime = Long.toString(new Date().getTime());

        this.avatarMap.put(username, avatarTime);

        return avatarTime;
    }


    /**
     * Get menu templates.
     *
     * @param cmsContext CMS context
     * @return menu templates
     */
    public SortedMap<String, String> getMenuTemplates(CMSServiceCtx cmsContext) {
        SortedMap<String, String> templates = Collections.synchronizedSortedMap(new TreeMap<String, String>());

        // Bundle
        Bundle bundle = this.bundleFactory.getBundle(cmsContext.getRequest().getLocale());

        // Default
        templates.put(StringUtils.EMPTY, bundle.getString("MENU_TEMPLATE_DEFAULT"));
        // Horizontal
        templates.put("horizontal", bundle.getString("MENU_TEMPLATE_HORIZONTAL"));
        // Footer
        templates.put("footer", bundle.getString("MENU_TEMPLATE_FOOTER"));
        // JSTree
        templates.put("jstree", bundle.getString("MENU_TEMPLATE_JSTREE"));

        return templates;
    }


    /**
     * Getter for bundleFactory.
     *
     * @return the bundleFactory
     */
    public IBundleFactory getBundleFactory() {
        return this.bundleFactory;
    }

}
