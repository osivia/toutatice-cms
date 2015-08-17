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
 */
package fr.toutatice.portail.cms.nuxeo.portlets.customizer;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.portlet.PortletContext;
import javax.portlet.ResourceURL;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.servlet.http.HttpSessionEvent;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.portal.common.invocation.Scope;
import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.Portal;
import org.jboss.portal.core.model.portal.PortalObject;
import org.jboss.portal.core.model.portal.PortalObjectContainer;
import org.jboss.portal.core.model.portal.PortalObjectId;
import org.jboss.portal.core.model.portal.PortalObjectPath;
import org.jboss.portal.core.model.portal.Window;
import org.jboss.portal.server.ServerInvocation;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.directory.IDirectoryService;
import org.osivia.portal.api.ecm.EcmCommand;
import org.osivia.portal.api.ecm.EcmCommonCommands;
import org.osivia.portal.api.internationalization.Bundle;
import org.osivia.portal.api.internationalization.IBundleFactory;
import org.osivia.portal.api.internationalization.IInternationalizationService;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.menubar.MenubarItem;
import org.osivia.portal.api.notifications.INotificationsService;
import org.osivia.portal.api.taskbar.ITaskbarService;
import org.osivia.portal.api.taskbar.TaskbarPlayer;
import org.osivia.portal.api.taskbar.TaskbarTask;
import org.osivia.portal.api.urls.ExtendedParameters;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.api.urls.Link;
import org.osivia.portal.core.cms.BinaryDelegation;
import org.osivia.portal.core.cms.BinaryDescription;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSHandlerProperties;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSItemType;
import org.osivia.portal.core.cms.CMSPage;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.constants.InternalConstants;
import org.osivia.portal.core.customization.ICustomizationService;
import org.osivia.portal.core.page.PageProperties;
import org.osivia.portal.core.web.IWebIdService;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import fr.toutatice.portail.cms.nuxeo.api.FileBrowserView;
import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.CommentDTO;
import fr.toutatice.portail.cms.nuxeo.api.domain.EditableWindow;
import fr.toutatice.portail.cms.nuxeo.api.domain.FragmentType;
import fr.toutatice.portail.cms.nuxeo.api.domain.IPlayerModule;
import fr.toutatice.portail.cms.nuxeo.api.domain.ListTemplate;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCommentsService;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCustomizer;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoConnectionProperties;
import fr.toutatice.portail.cms.nuxeo.portlets.comments.CommentsFormatter;
import fr.toutatice.portail.cms.nuxeo.portlets.comments.NuxeoCommentsServiceImpl;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.BrowserAdapter;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.CMSItemAdapter;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.DefaultPlayer;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.IPlayer;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.MenuBarFormater;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.NavigationItemAdapter;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.UserPagesLoader;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.WebConfigurationHelper;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.WebConfigurationQueryCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.WebConfigurationQueryCommand.WebConfigurationType;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.WysiwygParser;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.XSLFunctions;
import fr.toutatice.portail.cms.nuxeo.portlets.document.helpers.DocumentHelper;
import fr.toutatice.portail.cms.nuxeo.portlets.fragment.DocumentPictureFragmentModule;
import fr.toutatice.portail.cms.nuxeo.portlets.fragment.LinkFragmentModule;
import fr.toutatice.portail.cms.nuxeo.portlets.fragment.LinksFragmentModule;
import fr.toutatice.portail.cms.nuxeo.portlets.fragment.NavigationPictureFragmentModule;
import fr.toutatice.portail.cms.nuxeo.portlets.fragment.PropertyFragmentModule;
import fr.toutatice.portail.cms.nuxeo.portlets.fragment.SitePictureFragmentModule;
import fr.toutatice.portail.cms.nuxeo.portlets.fragment.SpaceMenubarFragmentModule;
import fr.toutatice.portail.cms.nuxeo.portlets.fragment.SummaryFragmentModule;
import fr.toutatice.portail.cms.nuxeo.portlets.fragment.ZoomFragmentModule;
import fr.toutatice.portail.cms.nuxeo.portlets.service.CMSService;
import fr.toutatice.portail.cms.nuxeo.portlets.service.DocumentPublishSpaceNavigationCommand;
import fr.toutatice.portail.cms.nuxeo.service.commands.LockCommand;
import fr.toutatice.portail.cms.nuxeo.service.commands.SubscribeCommand;
import fr.toutatice.portail.cms.nuxeo.service.commands.SynchronizeCommand;
import fr.toutatice.portail.cms.nuxeo.service.commands.UnlockCommand;
import fr.toutatice.portail.cms.nuxeo.service.commands.UnsubscribeCommand;
import fr.toutatice.portail.cms.nuxeo.service.commands.UnsynchronizeCommand;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.HTMLEditableWindow;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.ListEditableWindow;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.PortletEditableWindow;

/**
 * Default CMS customizer.
 *
 * @see INuxeoCustomizer
 */
public class DefaultCMSCustomizer implements INuxeoCustomizer {

    /** Logger. */
    protected static final Log LOGGER = LogFactory.getLog(DefaultCMSCustomizer.class);

    /* Default style for lists */
    /** List template minimal. */
    public static final String LIST_TEMPLATE_MINI = "mini";
    /** List template normal. */
    public static final String LIST_TEMPLATE_NORMAL = "normal";
    /** List template detailed. */
    public static final String LIST_TEMPLATE_DETAILED = "detailed";
    /** List template editorial. */
    public static final String LIST_TEMPLATE_EDITORIAL = "editorial";
    /** List template contextual links. */
    public static final String LIST_TEMPLATE_CONTEXTUAL_LINKS = "contextual-links";

    /** Default schemas. */
    public static final String DEFAULT_SCHEMAS = "dublincore, common, toutatice, file";
    /** Template "download". */
    public static final String TEMPLATE_DOWNLOAD = "download";

    /** Servlet URL for avatars */
    private static final String AVATAR_SERVLET = "/toutatice-portail-cms-nuxeo/avatar?username=";
    /** Binary servlet URL. */
    private static final String BINARY_SERVLET = "/toutatice-portail-cms-nuxeo/binary";

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

    /** Nuxeo comments service. */
    private INuxeoCommentsService commentsService;
    /** Class loader. */
    private ClassLoader cl;

    /** CMS service. */
    private CMSService cmsService;
    /** WEBID service. */
    private IWebIdService webIdService;
    /** Directory service. */
    private IDirectoryService directoryService;
    /** Notification service */
    private INotificationsService notificationsService;
    /** Internationalization service */
    private IInternationalizationService internationalizationService;
    
    /** The plugin mgr. */
    private CustomizationPluginMgr pluginMgr;

    


    /** Avatar map. */
    private Map<String, String> avatarMap = new ConcurrentHashMap<String, String>();
    /** binary map. */
    private Map<String, String> binaryMap = new ConcurrentHashMap<String, String>();
    /** binary delegation */
    public static Map<String, Map<String,BinaryDelegation>> delegations = new ConcurrentHashMap<String, Map<String,BinaryDelegation>>() ;

    private Map<String, IPlayer> players = new ConcurrentHashMap<String, IPlayer>();
    
    
    
    /** Taskbar tasks. */
    private List<TaskbarTask> tasks;
    
    /** Navigation taskbar players. */
    private Map<String, TaskbarPlayer> navigationTaskbarPlayers;

    /** Portal URL factory. */
    protected final ICustomizationService customizationService;

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

        // Customization Service
        this.customizationService = Locator.findMBean(ICustomizationService.class,
                ICustomizationService.MBEAN_NAME);

        // initialise le player view document par défaut
        this.players = new Hashtable<String, IPlayer>();
        this.players.put("defaultPlayer", new DefaultPlayer(this));
        
        // Plugin
        this.pluginMgr = new CustomizationPluginMgr(this);

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


//    /**
//     * EditableWindowAdapter permet de gérer les types de EditableWindow affichables.
//     *
//     * @return Instance du EditableWindowAdapter
//     */
//    public EditableWindowAdapter getEditableWindowAdapter() {
//        if (this.editableWindowAdapter == null) {
//            this.editableWindowAdapter = new EditableWindowAdapter();
//        }
//        return this.editableWindowAdapter;
//    }
    


    /**
     * {@inheritDoc}
     */
    @Override
    public final Map<String,EditableWindow> getEditableWindows(Locale locale) {
        return pluginMgr.customizeEditableWindows(locale);
    }


    public Map<String,EditableWindow> initListEditableWindows(Locale locale) {

    	Map<String,EditableWindow> map = new HashMap<String, EditableWindow>();
    	map.put("fgt.html", new HTMLEditableWindow("toutatice-portail-cms-nuxeo-viewFragmentPortletInstance", "html_Frag_"));
    	map.put("fgt.list", new ListEditableWindow("toutatice-portail-cms-nuxeo-viewListPortletInstance", "liste_Frag_"));
    	map.put("fgt.portlet", new PortletEditableWindow("", "portlet_Frag_"));
    	
    	return map;
    }



    /**
     * Get browser adapter.
     *
     * @return browser adapter
     */
    public BrowserAdapter getBrowserAdapter() {
        return BrowserAdapter.getInstance(this.cmsService);
    }

    
    
    /**
     * Gets the plugin mgr.
     *
     * @return the plugin mgr
     */
    public CustomizationPluginMgr getPluginMgr() {
        return pluginMgr;
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
     * {@inheritDoc}
     */
    @Override
    public final List<ListTemplate> getListTemplates(Locale locale) {

        return pluginMgr.customizeListTemplates(locale);
    }


    public List<ListTemplate> initListTemplates(Locale locale) {

        List<ListTemplate> templates = new ArrayList<ListTemplate>();

        // Bundle
        Bundle bundle = this.bundleFactory.getBundle(locale);

        // Minimal
        templates.add(new ListTemplate(LIST_TEMPLATE_MINI, bundle.getString("LIST_TEMPLATE_MINI"), DEFAULT_SCHEMAS));
        // Normal
        templates.add(new ListTemplate(LIST_TEMPLATE_NORMAL, bundle.getString("LIST_TEMPLATE_NORMAL"), DEFAULT_SCHEMAS));
        // Detailed
        templates.add(new ListTemplate(LIST_TEMPLATE_DETAILED, bundle.getString("LIST_TEMPLATE_DETAILED"), DEFAULT_SCHEMAS));
        // Editorial
        templates.add(new ListTemplate(LIST_TEMPLATE_EDITORIAL, bundle.getString("LIST_TEMPLATE_EDITORIAL"), DEFAULT_SCHEMAS));
        // Contextual links
        templates.add(new ListTemplate(LIST_TEMPLATE_CONTEXTUAL_LINKS, bundle.getString("LIST_TEMPLATE_CONTEXTUAL_LINKS"), DEFAULT_SCHEMAS));


        return templates;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Map<String, FragmentType> getFragmentTypes(Locale locale) {

        return pluginMgr.getFragments(locale);


    }





    /**
     * Inits the list fragments.
     *
     * @param locale the locale
     * @return the list
     */
    public List<FragmentType> initListFragments(Locale locale) {

        List<FragmentType> fragmentTypes = new ArrayList<FragmentType>();

        // Bundle
        Bundle bundle = this.bundleFactory.getBundle(locale);

        // Text fragment
        fragmentTypes.add(new FragmentType(PropertyFragmentModule.TEXT_ID, bundle.getString("FRAGMENT_TYPE_TEXT"), PropertyFragmentModule.getInstance(false)));
        // HTML fragment
        fragmentTypes.add(new FragmentType(PropertyFragmentModule.HTML_ID, bundle.getString("FRAGMENT_TYPE_HTML"), PropertyFragmentModule.getInstance(true)));
        // Zoom fragment
        fragmentTypes.add(new FragmentType(ZoomFragmentModule.ID, bundle.getString("FRAGMENT_TYPE_ZOOM"), ZoomFragmentModule.getInstance()));
        // Links fragment
        fragmentTypes.add(new FragmentType(LinksFragmentModule.ID, bundle.getString("FRAGMENT_TYPE_LINKS"), LinksFragmentModule.getInstance()));
        // Navigation picture fragment
        fragmentTypes.add(new FragmentType(NavigationPictureFragmentModule.ID, bundle.getString("FRAGMENT_TYPE_NAVIGATION_PICTURE"),
                NavigationPictureFragmentModule.getInstance()));
        // Document attachment picture fragment
        fragmentTypes.add(new FragmentType(DocumentPictureFragmentModule.ID, bundle.getString("FRAGMENT_TYPE_DOCUMENT_PICTURE"), DocumentPictureFragmentModule
                .getInstance()));
        // Link fragment
        fragmentTypes.add(new FragmentType(LinkFragmentModule.ID, bundle.getString("FRAGMENT_TYPE_LINK"), LinkFragmentModule.getInstance()));
        // Space menubar fragment
        fragmentTypes.add(new FragmentType(SpaceMenubarFragmentModule.ID, bundle.getString("FRAGMENT_TYPE_MENUBAR"), SpaceMenubarFragmentModule.getInstance()));
        // Site picture fragment
        fragmentTypes.add(new FragmentType(SitePictureFragmentModule.ID, bundle.getString("FRAGMENT_TYPE_SITE_PICTURE"), SitePictureFragmentModule
                .getInstance()));
        // Summary fragment
        fragmentTypes.add(new FragmentType(SummaryFragmentModule.ID, bundle.getString("FRAGMENT_TYPE_SUMMARY"), SummaryFragmentModule.getInstance()));

        return fragmentTypes;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedMap<String, String> getMenuTemplates(Locale locale) {
        SortedMap<String, String> templates = new TreeMap<String, String>();

        // Bundle
        Bundle bundle = this.bundleFactory.getBundle(locale);

        // Default
        templates.put(StringUtils.EMPTY, bundle.getString("MENU_TEMPLATE_DEFAULT"));
        // Horizontal
        templates.put("horizontal", bundle.getString("MENU_TEMPLATE_HORIZONTAL"));
        // Footer
        templates.put("footer", bundle.getString("MENU_TEMPLATE_FOOTER"));
        // Fancytree with lazy loading
        templates.put("fancytree-lazy", bundle.getString("MENU_TEMPLATE_FANCYTREE_LAZY"));
        // Fancytree with filter
        templates.put("fancytree-filter", bundle.getString("MENU_TEMPLATE_FANCYTREE_FILTER"));

        return templates;
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
    @Override
    public String createFolderRequest(CMSServiceCtx ctx, boolean ordered) throws CMSException {
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
        windowProperties.put("osivia.cms.style", CMSCustomizer.LIST_TEMPLATE_EDITORIAL);
        windowProperties.put("osivia.hideDecorators", "1");
        windowProperties.put("theme.dyna.partial_refresh_enabled", "false");
        windowProperties.put(Constants.WINDOW_PROP_SCOPE, ctx.getScope());
        windowProperties.put(Constants.WINDOW_PROP_VERSION, ctx.getDisplayLiveVersion());
        windowProperties.put("osivia.document.metadata", String.valueOf(false));
        windowProperties.put("osivia.title", doc.getTitle());

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
        windowProperties.put("osivia.cms.style", CMSCustomizer.LIST_TEMPLATE_EDITORIAL);
        windowProperties.put("osivia.hideDecorators", "1");
        windowProperties.put("theme.dyna.partial_refresh_enabled", "false");
        windowProperties.put(Constants.WINDOW_PROP_SCOPE, ctx.getScope());
        windowProperties.put(Constants.WINDOW_PROP_VERSION, ctx.getDisplayLiveVersion());
        windowProperties.put("osivia.document.metadata", String.valueOf(false));
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
        windowProperties.put("osivia.cms.style", CMSCustomizer.LIST_TEMPLATE_CONTEXTUAL_LINKS);
        windowProperties.put("osivia.hideDecorators", "1");
        windowProperties.put("theme.dyna.partial_refresh_enabled", "false");
        windowProperties.put(Constants.WINDOW_PROP_SCOPE, ctx.getScope());
        windowProperties.put(Constants.WINDOW_PROP_VERSION, ctx.getDisplayLiveVersion());
        windowProperties.put(InternalConstants.METADATA_WINDOW_PROPERTY, ctx.getHideMetaDatas());
        windowProperties.put("osivia.cms.pageSizeMax", "10");
        // JSS V3.1 : incompatible avec refresh CMS de type portlets
        // windowProperties.put("osivia.title", "Liste de liens");

        CMSHandlerProperties linkProps = new CMSHandlerProperties();
        linkProps.setWindowProperties(windowProperties);
        linkProps.setPortletInstance("toutatice-portail-cms-nuxeo-viewListPortletInstance");

        return linkProps;
    }





    /**
     * Get file browser player properties.
     *
     * @param cmsContext CMS context
     * @return player properties
     */
    @Override
    public CMSHandlerProperties getCMSFileBrowser(CMSServiceCtx cmsContext) {
        Document document = (Document) cmsContext.getDoc();

        Map<String, String> windowProperties = new HashMap<String, String>();
        windowProperties.put(Constants.WINDOW_PROP_SCOPE, cmsContext.getScope());
        windowProperties.put(Constants.WINDOW_PROP_VERSION, cmsContext.getDisplayLiveVersion());
        windowProperties.put(InternalConstants.METADATA_WINDOW_PROPERTY, cmsContext.getHideMetaDatas());
        windowProperties.put(Constants.WINDOW_PROP_URI, document.getPath());
        windowProperties.put("osivia.cms.publishPathAlreadyConverted", "1");
        windowProperties.put("osivia.hideDecorators", "1");
        windowProperties.put("osivia.ajaxLink", "1");

        CMSHandlerProperties linkProps = new CMSHandlerProperties();
        linkProps.setWindowProperties(windowProperties);
        linkProps.setPortletInstance("toutatice-portail-cms-nuxeo-fileBrowserPortletInstance");

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
        windowProperties.put("osivia.cms.style", CMSCustomizer.LIST_TEMPLATE_EDITORIAL);
        windowProperties.put("osivia.hideDecorators", "1");
        windowProperties.put("theme.dyna.partial_refresh_enabled", "false");
        windowProperties.put(Constants.WINDOW_PROP_SCOPE, ctx.getScope());
        windowProperties.put(Constants.WINDOW_PROP_VERSION, ctx.getDisplayLiveVersion());
        windowProperties.put(InternalConstants.METADATA_WINDOW_PROPERTY, ctx.getHideMetaDatas());
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
        windowProperties.put("osivia.cms.style", CMSCustomizer.LIST_TEMPLATE_EDITORIAL);
        windowProperties.put("osivia.hideDecorators", "1");
        windowProperties.put("theme.dyna.partial_refresh_enabled", "false");
        windowProperties.put(Constants.WINDOW_PROP_SCOPE, ctx.getScope());
        windowProperties.put(Constants.WINDOW_PROP_VERSION, ctx.getDisplayLiveVersion());
        windowProperties.put(InternalConstants.METADATA_WINDOW_PROPERTY, ctx.getHideMetaDatas());
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
        windowProperties.put("osivia.cms.style", CMSCustomizer.LIST_TEMPLATE_EDITORIAL);
        windowProperties.put("osivia.hideDecorators", "1");
        windowProperties.put("theme.dyna.partial_refresh_enabled", "false");
        windowProperties.put(Constants.WINDOW_PROP_SCOPE, ctx.getScope());
        // windowProperties.put(Constants.WINDOW_PROP_VERSION, ctx.getDisplayLiveVersion());
        windowProperties.put(InternalConstants.METADATA_WINDOW_PROPERTY, ctx.getHideMetaDatas());
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
        windowProperties.put(InternalConstants.METADATA_WINDOW_PROPERTY, ctx.getHideMetaDatas());
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
    public CMSHandlerProperties getCMSPlayer(CMSServiceCtx cmsContext) throws Exception {
        Document document = (Document) cmsContext.getDoc();
        CMSPublicationInfos pubInfos = this.cmsService.getPublicationInfos(cmsContext, document.getPath());

        // Workspace indicator
        boolean workspace = (cmsContext.getContextualizationBasePath() != null) && (pubInfos.isLiveSpace());




        List<IPlayerModule> modules = pluginMgr.customizeModules(cmsContext);

        for (IPlayerModule icmsCustomizerModule : modules) {
            CMSHandlerProperties properties = icmsCustomizerModule.getCMSPlayer(cmsContext, this.getCmsService());
            if (properties != null) {
                return properties;
            }
        }




        if ("UserWorkspace".equals(document.getType())) {
            // Pas de filtre sur les versions publiées
            cmsContext.setDisplayLiveVersion("1");
            return this.getCMSFileBrowser(cmsContext);
        }

        if (("DocumentUrlContainer".equals(document.getType()))) {
            if (workspace) {
                // File browser
                cmsContext.setDisplayLiveVersion("1");
                CMSHandlerProperties properties = this.getCMSFileBrowser(cmsContext);
                Map<String, String> windowProperties = properties.getWindowProperties();
                windowProperties.put(InternalConstants.PROP_WINDOW_TITLE, document.getTitle());
                windowProperties.put(InternalConstants.DEFAULT_VIEW_WINDOW_PROPERTY, FileBrowserView.THUMBNAILS.getName());
                return properties;
            } else {
                return this.getCMSUrlContainerPlayer(cmsContext);
            }
        }



        if ("AnnonceFolder".equals(document.getType())) {
            return this.getCMSAnnonceFolderPlayer(cmsContext);
        }

        if (("Folder".equals(document.getType()) || "OrderedFolder".equals(document.getType())) || ("Section".equals(document.getType()))) {
            if (workspace) {
                // File browser
                cmsContext.setDisplayLiveVersion("1");
                CMSHandlerProperties props = this.getCMSFileBrowser(cmsContext);
                props.getWindowProperties().put("osivia.title", document.getTitle());
                return props;
            } else if ("Folder".equals(document.getType())) {
                return this.getCMSFolderPlayer(cmsContext);
            } else {
                return this.getCMSOrderedFolderPlayer(cmsContext);
            }
        }

        if ("PortalVirtualPage".equals(document.getType())) {
            return this.getCMSVirtualPagePlayer(cmsContext);
        }


        // ========== Try to get external config for players
        // compute domain path
        String domainPath = WebConfigurationHelper.getDomainPath(cmsContext);

        if (domainPath != null) {
            // get configs installed in nuxeo
            WebConfigurationQueryCommand command = new WebConfigurationQueryCommand(domainPath, WebConfigurationType.CMS_PLAYER);
            Documents configs = null;
            try {

                configs = WebConfigurationHelper.executeWebConfigCmd(cmsContext, this.cmsService, command);

            } catch (Exception e) {
                // Can't get confs
            }

            if ((configs != null) && (configs.size() > 0)) {
                for (Document config : configs) {
                    String documentType = config.getProperties().getString(WebConfigurationHelper.CODE);
                    String playerInstance = config.getProperties().getString(WebConfigurationHelper.ADDITIONAL_CODE);

                    if (document.getType().equals(documentType) && this.players.containsKey(playerInstance)) {

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

                        return this.players.get(playerInstance).play(cmsContext, document, windowProperties);
                    }
                }
            }

        }
        return this.players.get("defaultPlayer").play(cmsContext, document);
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
        String externalUrl = NuxeoConnectionProperties.getPublicBaseUri().toString() + "/nxdoc/default/" + doc.getId() + "/view_documents";

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
    public Link createCustomLink(CMSServiceCtx cmsContext) throws Exception {
        Document document = (Document) cmsContext.getDoc();
        String displayContext = cmsContext.getDisplayContext();

        String url = null;
        boolean externalLink = false;
        boolean downloadable = false;


        if (!"detailedView".equals(displayContext)) {
            // Le download sur les fichiers doit être explicite (plus dans l'esprit GED)
            if (("File".equals(document.getType()) || "Audio".equals(document.getType()) || "Video".equals(document.getType()))
                    && ("download".equals(displayContext))) {
                PropertyMap attachedFileProperties = document.getProperties().getMap("file:content");
                if ((attachedFileProperties != null) && !attachedFileProperties.isEmpty()) {

                    // Nuxeo controller
                    NuxeoController nuxeoCtl =  new NuxeoController(cmsContext.getRequest(), cmsContext.getResponse(), cmsContext.getPortletCtx());
                    nuxeoCtl.setCurrentDoc(document);

                    url = nuxeoCtl.createFileLink(document, "file:content");
                    downloadable = true;
                }
            }

            if ("ContextualLink".equals(document.getType()) && !("document".equals(displayContext) || "fileExplorer".equals(displayContext))) {
                url = this.createPortletDelegatedExternalLink(cmsContext);
                externalLink = true;
            }

            // Gestion des vues externes
            // Nécessaire pour poser une ancre au moment de la génération du lien
            if (url == null) {
                url = this.getNuxeoNativeViewerUrl(cmsContext);
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
        String requestFilter = StringUtils.EMPTY;

        if ("1".equals(ctx.getDisplayLiveVersion())) {
            // selection des versions lives : il faut exclure les proxys
            requestFilter = "ecm:mixinType != 'HiddenInNavigation' AND ecm:isProxy = 0  AND ecm:currentLifeCycleState <> 'deleted'  AND ecm:isCheckedInVersion = 0 ";
        } else {
            // sélection des folders et des documents publiés

            // requestFilter = "ecm:mixinType != 'HiddenInNavigation' AND ecm:isProxy = 1  AND ecm:currentLifeCycleState <> 'deleted' ";
            requestFilter = "ecm:isProxy = 1 AND ecm:mixinType != 'HiddenInNavigation'  AND ecm:currentLifeCycleState <> 'deleted' ";
        }

        return this.addExtraNxQueryFilters(ctx, nuxeoRequest, requestFilteringPolicy, requestFilter);

    }

    /**
     * @param ctx
     * @param nuxeoRequest
     * @param requestFilteringPolicy
     * @param requestFilter
     * @return request with extra filters according to policy and others.
     * @throws Exception
     */
    protected String addExtraNxQueryFilters(CMSServiceCtx ctx, String nuxeoRequest, String requestFilteringPolicy, String requestFilter) throws Exception {
        String policyFilter = null;

        ServerInvocation invocation = ctx.getServerInvocation();

        String portalName = null;


        // Cas des chargement asynchrones : pas de contexte
        if (invocation != null) {
            portalName = PageProperties.getProperties().getPagePropertiesMap().get(Constants.PORTAL_NAME);
        }

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
     * {@inheritDoc}
     */
    @Override
    public String addSearchFilter(CMSServiceCtx ctx, String nuxeoRequest, String requestFilteringPolicy) throws Exception {

        StringBuffer filter = new StringBuffer(2);
        filter.append(" ecm:mixinType <> 'HiddenInNavigation' AND ecm:currentLifeCycleState <> 'deleted'  AND ecm:isCheckedInVersion = 0");
        filter.append(" AND ecm:mixinType <> 'isLocalPublishLive'");

        return this.addExtraNxQueryFilters(ctx, nuxeoRequest, requestFilteringPolicy, filter.toString());
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
            WebConfigurationQueryCommand command = new WebConfigurationQueryCommand(domainPath, WebConfigurationType.EXTRA_REQUEST_FILTER);
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

            transformer.setParameter("bridge", new XSLFunctions(this, ctx));
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
    public String transformLink(CMSServiceCtx ctx, String link) {
        XSLFunctions xslFunctions = new XSLFunctions(this, ctx);
        return xslFunctions.link(NuxeoConnectionProperties.getPublicDomainUri().toString() + link);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Link getLinkFromNuxeoURL(CMSServiceCtx cmsContext, String url) {
        // Link
        Link link;

        // Portal controller context
        PortalControllerContext portalControllerContext = new PortalControllerContext(cmsContext.getControllerContext());

        if (StringUtils.startsWith(url, "/nuxeo/")) {
            // Nuxeo URL
            String nuxeoURL = StringUtils.substringBefore(url, "#");
            // Anchor
            String anchor = StringUtils.substringAfter(url, "#");

            // CMS path
            String cmsPath = this.transformNuxeoURL(cmsContext, nuxeoURL);

            Map<String, String> parameters = new HashMap<String, String>(0);

            // CMS path can have parameters
            ExtendedParameters extendedParameters = null;
            Map<String, String> nxPathParameters = this.cmsService.getNxPathParameters(cmsPath);
            if(MapUtils.isNotEmpty(nxPathParameters)){
                cmsPath = StringUtils.substringBefore(cmsPath, "?");
                extendedParameters = new ExtendedParameters();
                extendedParameters.setAllParameters(nxPathParameters);
            }

            String currentPagePath = null;
            if( cmsContext.getRequest() != null)    {
                Window window = (Window) cmsContext.getRequest().getAttribute("osivia.window");
                if (window != null) {
                    Page page = window.getPage();
                    if (page != null) {
                        currentPagePath = page.getId().toString(PortalObjectPath.CANONICAL_FORMAT);
                    }
                }
            }


            // Portal URL
            String portalURL = StringUtils.EMPTY;
            if(extendedParameters != null){
                portalURL = this.portalUrlFactory.getCMSUrl(portalControllerContext, currentPagePath, cmsPath, parameters, null, null, null, null, null, null, extendedParameters);
            } else {
                portalURL = this.portalUrlFactory.getCMSUrl(portalControllerContext, currentPagePath, cmsPath, parameters, null, null, null, null, null, null);
            }

            if (StringUtils.isNotBlank(anchor)) {
                portalURL += "#" + anchor;
            }

            link = new Link(portalURL, false);
        } else if (StringUtils.isBlank(url)) {
            // Empty URL
            link = new Link("#", false);
        } else {
            // Absolute URL
            String absoluteURL;
            boolean external;

            StringBuilder builder = new StringBuilder();
            if (!(StringUtils.startsWithIgnoreCase(url, "http://") || StringUtils.startsWithIgnoreCase(url, "https://"))) {
                builder.append("http://");
            }
            builder.append(url);
            absoluteURL = builder.toString();

            try {
                URL urlObject = new URL(absoluteURL);
                String serverName = cmsContext.getRequest().getServerName();
                external = !StringUtils.equals(urlObject.getHost(), serverName);
            } catch (Exception e) {
                external = false;
            }
            link = new Link(absoluteURL, external);
        }

        return link;
    }

    /**
     * Transform Nuxeo URL into CMS path.
     *
     * @param cmsContext CMS
     * @param url Nuxeo URL
     * @return CMS path
     */
    private String transformNuxeoURL(CMSServiceCtx cmsContext, String url) {
        // Result CMS path
        String cmsPath = null;

        if (StringUtils.startsWith(url, "/nuxeo/web/")) {
            // Nuxeo web path
            String webPath = StringUtils.removeStart(url, "/nuxeo/web/");

            // Webpath can have path parameter containing "/"
            String pathParameters = StringUtils.EMPTY;
            if(StringUtils.contains(webPath, "?")){

                String parameters = StringUtils.substringAfter(webPath, "?");

                if(StringUtils.contains(parameters, "/")){
                    pathParameters = parameters;
                    webPath = StringUtils.substringBefore(webPath, "?");
                }
            }

            String[] splittedWebPath = StringUtils.split(webPath, "/");
            if (splittedWebPath.length > 0) {

                // Web ID
                String webId = splittedWebPath[splittedWebPath.length - 1];
                if(StringUtils.isNotBlank(pathParameters)){
                    webId = webId.concat("?").concat(pathParameters);
                }

                // Service web ID
                String serviceWebId = this.getWebIdService().webIdToFetchInfoService(webId);

                // Document path
                String path = null;
                try {

                    CMSPublicationInfos pubInfos = this.getCmsService().getPublicationInfos(cmsContext, serviceWebId);
                    path = pubInfos.getDocumentPath();
                } catch (CMSException e) {
                    //path = StringUtils.EMPTY;
                    // keep null path
                }

                // CMS item
                CMSItem cmsItem = new CMSItem(path, null, webId, null, null);

                cmsPath = this.getWebIdService().itemToPageUrl(cmsContext, cmsItem);
            }
        } else if (StringUtils.startsWith(url, "/nuxeo/nxpath/")) {
            // Nuxeo CMS path
            cmsPath = StringUtils.removeStart(url, "/nuxeo/nxpath/");
            // Remove repository
            cmsPath = "/" + StringUtils.substringAfter(cmsPath, "/");
        }

        if (cmsPath == null) {
            // Invalid Nuxeo URL
            LOGGER.warn("Invalid Nuxeo URL: '" + url + "'.");
        }

        return cmsPath;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, CMSItemType> getCMSItemTypes() {

        return pluginMgr.customizeCMSItemTypes();

    }

    protected List<CMSItemType> getCustomizedCMSItemTypes() {
        return new ArrayList<CMSItemType>();
    }


    /**
     * Get default CMS item types.
     *
     * @return default CMS item types
     */
    public List<CMSItemType> getDefaultCMSItemTypes() {
        List<CMSItemType> defaultTypes = new ArrayList<CMSItemType>();

        // Workspace
        defaultTypes.add(new CMSItemType("Workspace", true, false, false, true, false, false, Arrays.asList("Folder", "DocumentUrlContainer",
                "AnnonceFolder", "PictureBook"), "/default/templates/workspace", "glyphicons glyphicons-wallet", true));
        // Portal site
        defaultTypes.add(new CMSItemType("PortalSite", true, false, false, true, true, true, Arrays.asList("File", "Audio", "Video", "Annonce", "PortalPage",
                "Picture", "ContextualLink"), null, "glyphicons glyphicons-global", true));
        // Portal page
        defaultTypes.add(new CMSItemType("PortalPage", true, true, true, true, true, true, Arrays.asList("File", "Audio", "Video", "Annonce", "PortalPage",
                "Picture", "ContextualLink"), null, "glyphicons glyphicons-more-items"));
        // Folder
        defaultTypes.add(new CMSItemType("Folder", true, true, true, false, false, true, Arrays.asList("File", "Audio", "Video", "Folder", "Note", "Picture"),
                null, "glyphicons glyphicons-folder-closed"));
        // Ordered folder
        defaultTypes.add(new CMSItemType("OrderedFolder", true, true, true, true, false, true, Arrays.asList("File", "Audio", "Video", "Folder", "Note",
                "Picture"), null, "glyphicons glyphicons-folder-closed"));
        // File
        defaultTypes.add(new CMSItemType("File", false, false, false, false, false, true, new ArrayList<String>(0), null, "glyphicons glyphicons-file"));
        // Audio file
        defaultTypes.add(new CMSItemType("Audio", false, false, false, false, false, true, new ArrayList<String>(0), null, "glyphicons glyphicons-headphones"));
        // Video file
        defaultTypes.add(new CMSItemType("Video", false, false, false, false, false, true, new ArrayList<String>(0), null, "glyphicons glyphicons-film"));
        // Note
        defaultTypes.add(new CMSItemType("Note", false, false, false, false, false, true, new ArrayList<String>(0), null, "glyphicons glyphicons-notes-2"));
        // Annonce
        defaultTypes
                .add(new CMSItemType("Annonce", false, false, false, false, false, true, new ArrayList<String>(0), null, "glyphicons glyphicons-newspaper"));
        // Annonce folder
        defaultTypes.add(new CMSItemType("AnnonceFolder", true, true, false, false, false, true, Arrays.asList("Annonce"), null,
                "glyphicons glyphicons-newspaper"));
        // Document URL container
        defaultTypes.add(new CMSItemType("DocumentUrlContainer", true, true, true, true, false, true, Arrays.asList("DocumentUrlContainer", "ContextualLink"),
                null, "glyphicons glyphicons-bookmark"));
        // Contextual link
        defaultTypes.add(new CMSItemType("ContextualLink", false, false, false, false, false, true, new ArrayList<String>(0), null,
                "glyphicons glyphicons-link"));

        return defaultTypes;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommentsHTMLContent(CMSServiceCtx cmsContext, Document document) throws CMSException {
        List<CommentDTO> comments = this.getNuxeoCommentsService().getDocumentComments(cmsContext, document);
        CommentsFormatter formatter = new CommentsFormatter(comments);
        Locale locale = cmsContext.getRequest().getLocale();
        return formatter.generateHTMLContent(locale);
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
	 * @return the notificationsService
	 */
	public INotificationsService getNotificationsService() {
		return this.notificationsService;
	}

	/**
	 * @param notificationsService the notificationsService to set
	 */
	public void setNotificationsService(INotificationsService notificationsService) {
		this.notificationsService = notificationsService;
	}

	/**
	 * @return the internationalizationService
	 */
	public IInternationalizationService getInternationalizationService() {
		return this.internationalizationService;
	}

	/**
	 * @param internationalizationService the internationalizationService to set
	 */
	public void setInternationalizationService(
			IInternationalizationService internationalizationService) {
		this.internationalizationService = internationalizationService;
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


    /**
     * {@inheritDoc}
     */
    @Override
    public String getContentWebIdPath(CMSServiceCtx cmsCtx) {
        Document doc = (Document) cmsCtx.getDoc();

        String webId = doc.getString("ttc:webid");

        String permLinkPath = ((Document) (cmsCtx.getDoc())).getPath();

        if (StringUtils.isNotEmpty(webId)) {
            String explicitUrl = doc.getString("ttc:explicitUrl");
            String extension = doc.getString("ttc:extensionUrl");


            Map<String, String> properties = new HashMap<String, String>();
            if (explicitUrl != null) {
                properties.put(IWebIdService.EXPLICIT_URL, explicitUrl);
            }
            if (extension != null) {
                properties.put(IWebIdService.EXTENSION_URL, extension);
            }


            // Case of remote proxy
            try {
                if (!StringUtils.contains(webId, "?")) {

                    if (DocumentHelper.isRemoteProxy(doc)) {
                        String parentId = this.getWebIdService().getParentId(cmsCtx, doc.getPath());
                        if (StringUtils.isNotBlank(parentId)) {
                            webId = webId.concat("?").concat(IWebIdService.PARENT_ID).concat("=").concat(parentId);
                        } else {
                            String parentPath = this.getCmsService().getParentPath(doc.getPath());
                            if (StringUtils.isNotBlank(parentPath)) {
                                webId = webId.concat("?").concat(IWebIdService.PARENT_PATH).concat("=").concat(parentPath);
                            }
                        }
                    }
                }
            } catch (CMSException e) {
                // Noting
            }

            CMSItem cmsItem = new CMSItem(doc.getPath(), null, webId, properties, doc);

            CMSItemType cmsItemType = this.getCMSItemTypes().get(doc.getType());
            cmsItem.setType(cmsItemType);

            permLinkPath = this.getWebIdService().itemToPageUrl(cmsCtx, cmsItem);
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
     * Checks if current doc is in edition state.
     *
     * @param path the path
     * @return true, if is in page edition state
     * @throws CMSException the CMS exception
     */



    public boolean isPathInLiveState(CMSServiceCtx cmsCtx, Document doc) {

        if ("1".equals(cmsCtx.getDisplayLiveVersion())) {
            return true;
        }

        return doc.getPath().equals( cmsCtx.getForcedLivePath());
    }


    /**
     * Validate binary delegation.
     *
     * @param cmsCtx the cms ctx
     * @param path the path
     * @return the binary delegation
     */
    public BinaryDelegation validateBinaryDelegation(CMSServiceCtx cmsCtx, String path) {

        String id = cmsCtx.getServletRequest().getSession().getId();
        Map<String, BinaryDelegation> delegationMap = delegations.get(id);
        if( delegationMap != null){
            return delegationMap.get(path);
        }
        return null;
    }


    /**
     * Gets the binary resource URL.
     *
     * @param cmsCtx CMS context
     * @param binary binary description
     * @return the binary resource URL
     */
    public Link getBinaryResourceURL(CMSServiceCtx cmsCtx, BinaryDescription binary) throws CMSException {
        String src = StringUtils.EMPTY;
        String path = StringUtils.EMPTY;
        boolean liveState = false;

        BinaryDelegation delegation = new BinaryDelegation();

        try {
            String portalName = null;
            ServerInvocation invocation = cmsCtx.getServerInvocation();
            Boolean isAdmin = false;

            if (invocation != null) {
                portalName = PageProperties.getProperties().getPagePropertiesMap().get(Constants.PORTAL_NAME);
                isAdmin = (Boolean) invocation.getAttribute(Scope.PRINCIPAL_SCOPE, "osivia.isAdmin");
            }


            if (binary.getDocument() != null) {
                Document doc = (Document) binary.getDocument();

                path = doc.getPath();
                liveState = this.isPathInLiveState(cmsCtx, doc);
                delegation.setGrantedAccess(true);
            } else {
                path = binary.getPath();
            }

            Subject subject = (Subject) PolicyContext.getContext("javax.security.auth.Subject.container");
            delegation.setSubject(subject);

            delegation.setAdmin(isAdmin);

            if(  StringUtils.endsWith(path, ".proxy") && !StringUtils.endsWith(path, ".remote.proxy")) {
                path = StringUtils.removeEnd(path, ".proxy");
            }

            delegation.setUserName(cmsCtx.getServletRequest().getRemoteUser());

            Map<String, BinaryDelegation> delegationMap = this.getUserDelegation(cmsCtx);
            delegationMap.put(path, delegation);

            boolean refresh = PageProperties.getProperties().isRefreshingPage();

            StringBuilder sb = new StringBuilder();

            sb.append(BINARY_SERVLET).append("?type=").append(binary.getType().name()).append("&path=").append(URLEncoder.encode(path, "UTF-8"));

            if (portalName != null) {
                sb.append("&portalName=").append(portalName);
            }

            if (binary.getIndex() != null) {
                sb.append("&index=").append(binary.getIndex());
            }
            if (liveState) {
                sb.append("&liveState=").append(liveState);
            }
            if (binary.getContent() != null) {
                sb.append("&content=").append(binary.getContent());
            }
            if (binary.getFieldName() != null) {
                sb.append("&fieldName=").append(binary.getFieldName());
            }
            if (binary.getFileName() != null) {
                sb.append("&fileName=").append(URLEncoder.encode(binary.getFileName(), CharEncoding.UTF_8));
            }
            if (refresh) {
                sb.append("&refresh=").append(refresh);
            }
            if (cmsCtx.getScope() != null) {
                sb.append("&scope=").append(cmsCtx.getScope());
            }
            if (cmsCtx.getForcePublicationInfosScope() != null) {
                sb.append("&fscope=").append(cmsCtx.getForcePublicationInfosScope());
            }

            String binaryTimeStamp = this.binaryMap.get(path);
            if (binaryTimeStamp == null) {
                // if not defined, set ie
                binaryTimeStamp = this.refreshBinaryResource(cmsCtx, path);
            }

            // timestamp is concated in the url to control the client cache
            sb.append("&t=");
            sb.append(binaryTimeStamp.toString());

            src = sb.toString();


        } catch (Exception e) {
            new CMSException(e);
        }


        return new Link(src, false);
    }


    /**
     * Gets the user delegation.
     *
     * @param cmsCtx the cms ctx
     * @return the user delegation
     */

    private Map<String, BinaryDelegation> getUserDelegation(CMSServiceCtx cmsCtx) {
        String id = cmsCtx.getServletRequest().getSession().getId();
        Map<String, BinaryDelegation> delegationMap = delegations.get(id);
        if( delegationMap == null)  {
            delegationMap = new ConcurrentHashMap<String, BinaryDelegation>();
            delegations.put(id, delegationMap);
        }
        return delegationMap;
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent sessionEvent) {
        delegations.remove(sessionEvent.getSession().getId());
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {

    }



    public String refreshBinaryResource(CMSServiceCtx cmsCtx, String path) {

        // renew the timestamp and map it to the user
        String pathTime = Long.toString(new Date().getTime());

        this.binaryMap.put(path, pathTime);

        return pathTime;
    }


    /**
     * Getter for bundleFactory.
     *
     * @return the bundleFactory
     */
    public IBundleFactory getBundleFactory() {
        return this.bundleFactory;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object executeNuxeoCommand(CMSServiceCtx cmsContext, INuxeoCommand command) throws CMSException {
        try {
            return this.cmsService.executeNuxeoCommand(cmsContext, command);
        } catch (CMSException e) {
            throw e;
        } catch (Exception e) {
            throw new CMSException(e);
        }
    }


    /**
     * {@inheritDoc}
     */
	@Override
	public Map<String, EcmCommand> getEcmCommands() {

		Map<String, EcmCommand> commands = new ConcurrentHashMap<String, EcmCommand>();

		commands.put(EcmCommonCommands.lock.name(), new LockCommand(this.getNotificationsService(), this.getInternationalizationService()));
		commands.put(EcmCommonCommands.unlock.name(), new UnlockCommand(this.getNotificationsService(), this.getInternationalizationService()));

		commands.put(EcmCommonCommands.subscribe.name(), new SubscribeCommand(this.getNotificationsService(), this.getInternationalizationService()));
		commands.put(EcmCommonCommands.unsubscribe.name(), new UnsubscribeCommand(this.getNotificationsService(), this.getInternationalizationService()));

		commands.put(EcmCommonCommands.synchronizeFolder.name(), new SynchronizeCommand(this.getNotificationsService(), this.getInternationalizationService()));
		commands.put(EcmCommonCommands.unsynchronizeFolder.name(), new UnsynchronizeCommand(this.getNotificationsService(), this.getInternationalizationService()));


		return commands;
	}


    /**
     * Get navigation taskbar players.
     *
     * @return taskbar players
     */
    public Map<String, TaskbarPlayer> getNavigationTaskbarPlayers() {
        if (this.navigationTaskbarPlayers == null) {
            this.navigationTaskbarPlayers = new ConcurrentHashMap<String, TaskbarPlayer>();

            // Folder
            TaskbarPlayer folderPlayer = this.getFolderTaskbarPlayer();
            this.navigationTaskbarPlayers.put("Folder", folderPlayer);
            this.navigationTaskbarPlayers.put("OrderedFolder", folderPlayer);
        }
        return this.navigationTaskbarPlayers;
    }


    /**
     * Get folder taskbar player.
     *
     * @return taskbar player
     */
    protected TaskbarPlayer getFolderTaskbarPlayer() {
        TaskbarPlayer player = new TaskbarPlayer();
        player.setInstance("toutatice-portail-cms-nuxeo-publishMenuPortletInstance");
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("osivia.cms.template", "fancytree-lazy");
        properties.put("osivia.cms.startLevel", String.valueOf(2));
        player.setProperties(properties);
        return player;
    }


    /**
     * Get taskbar tasks.
     *
     * @return tasks
     */
    public List<TaskbarTask> getTaskbarTasks(CMSServiceCtx cmsContext) {
        if (this.tasks == null) {
            this.tasks = new ArrayList<TaskbarTask>();

            // Home
            this.tasks.add(this.getHomeTask(cmsContext));

            // Search
            this.tasks.add(this.getSearchTask(cmsContext));
        }
        return this.tasks;
    }


    /**
     * Get home task.
     *
     * @param cmsContext CMS context
     * @return task
     */
    protected TaskbarTask getHomeTask(CMSServiceCtx cmsContext) {
        // Task
        TaskbarTask task = new TaskbarTask();

        // Identifier
        task.setId(ITaskbarService.HOME_TASK_ID);
        // Internationalized name
        task.setKey("HOME");
        // Icon
        task.setIcon("glyphicons glyphicons-home");

        return task;
    }


    /**
     * Get search task.
     *
     * @param cmsContext CMS context
     * @return task
     */
    protected TaskbarTask getSearchTask(CMSServiceCtx cmsContext) {
        // Task
        TaskbarTask task = new TaskbarTask();

        // Identifier
        task.setId("SEARCH");
        // Internationalized name
        task.setKey("SEARCH");
        // Icon
        task.setIcon("glyphicons glyphicons-search");
        // Taskbar player
        TaskbarPlayer player = new TaskbarPlayer();
        player.setInstance("toutatice-portail-cms-nuxeo-searchPortletInstance");
        Map<String, String> properties = new HashMap<String, String>(1);
        properties.put(Constants.WINDOW_PROP_URI, "${basePath}");
        player.setProperties(properties);
        task.setTaskbarPlayer(player);

        return task;
    }

}
