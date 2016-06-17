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
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.ResourceURL;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.servlet.http.HttpSessionEvent;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
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
import org.osivia.portal.api.cms.DocumentContext;
import org.osivia.portal.api.cms.DocumentType;
import org.osivia.portal.api.cms.impl.BasicPublicationInfos;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.directory.IDirectoryService;
import org.osivia.portal.api.ecm.EcmCommand;
import org.osivia.portal.api.ecm.EcmCommonCommands;
import org.osivia.portal.api.internationalization.Bundle;
import org.osivia.portal.api.internationalization.IBundleFactory;
import org.osivia.portal.api.internationalization.IInternationalizationService;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.notifications.INotificationsService;
import org.osivia.portal.api.panels.PanelPlayer;
import org.osivia.portal.api.player.Player;
import org.osivia.portal.api.taskbar.ITaskbarService;
import org.osivia.portal.api.taskbar.TaskbarFactory;
import org.osivia.portal.api.taskbar.TaskbarItem;
import org.osivia.portal.api.taskbar.TaskbarItems;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.api.urls.Link;
import org.osivia.portal.core.cms.BinaryDelegation;
import org.osivia.portal.core.cms.BinaryDescription;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSExtendedDocumentInfos;
import org.osivia.portal.core.cms.CMSPage;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.constants.InternalConstants;
import org.osivia.portal.core.customization.ICustomizationService;
import org.osivia.portal.core.page.PageProperties;
import org.osivia.portal.core.web.IWebIdService;
import org.osivia.portal.core.web.IWebUrlService;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.CommentDTO;
import fr.toutatice.portail.cms.nuxeo.api.domain.CustomizedJsp;
import fr.toutatice.portail.cms.nuxeo.api.domain.EditableWindow;
import fr.toutatice.portail.cms.nuxeo.api.domain.FragmentType;
import fr.toutatice.portail.cms.nuxeo.api.domain.ListTemplate;
import fr.toutatice.portail.cms.nuxeo.api.player.INuxeoPlayerModule;
import fr.toutatice.portail.cms.nuxeo.api.portlet.ViewList;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCommentsService;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCustomizer;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoConnectionProperties;
import fr.toutatice.portail.cms.nuxeo.portlets.comments.CommentsFormatter;
import fr.toutatice.portail.cms.nuxeo.portlets.comments.NuxeoCommentsServiceImpl;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.BrowserAdapter;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.DefaultPlayer;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.MenuBarFormater;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.NavigationItemAdapter;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.UserPagesLoader;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.WebConfigurationHelper;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.WebConfigurationQueryCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.WebConfigurationQueryCommand.WebConfigurationType;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.WysiwygParser;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.XSLFunctions;
import fr.toutatice.portail.cms.nuxeo.portlets.document.helpers.ContextDocumentsHelper;
import fr.toutatice.portail.cms.nuxeo.portlets.fragment.DocumentPictureFragmentModule;
import fr.toutatice.portail.cms.nuxeo.portlets.fragment.LinkFragmentModule;
import fr.toutatice.portail.cms.nuxeo.portlets.fragment.NavigationPictureFragmentModule;
import fr.toutatice.portail.cms.nuxeo.portlets.fragment.PropertyFragmentModule;
import fr.toutatice.portail.cms.nuxeo.portlets.fragment.SitePictureFragmentModule;
import fr.toutatice.portail.cms.nuxeo.portlets.fragment.SpaceMenubarFragmentModule;
import fr.toutatice.portail.cms.nuxeo.portlets.service.CMSService;
import fr.toutatice.portail.cms.nuxeo.service.commands.EraseModificationsCommand;
import fr.toutatice.portail.cms.nuxeo.service.commands.LockCommand;
import fr.toutatice.portail.cms.nuxeo.service.commands.SubscribeCommand;
import fr.toutatice.portail.cms.nuxeo.service.commands.SynchronizeCommand;
import fr.toutatice.portail.cms.nuxeo.service.commands.UnlockCommand;
import fr.toutatice.portail.cms.nuxeo.service.commands.UnsubscribeCommand;
import fr.toutatice.portail.cms.nuxeo.service.commands.UnsynchronizeCommand;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.FragmentEditableWindow;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.HTMLEditableWindow;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.ListEditableWindow;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.PictureEditableWindow;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.PortletEditableWindow;

/**
 * Default CMS customizer.
 *
 * @see INuxeoCustomizer
 */
public class DefaultCMSCustomizer implements INuxeoCustomizer {

    /** Logger. */
    protected static final Log LOGGER = LogFactory.getLog(DefaultCMSCustomizer.class);



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
    @Deprecated
    private NavigationItemAdapter navigationItemAdapter;
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
    /** WebId service. */
    private IWebIdService webIdService;
    /** Web URL service. */
    private IWebUrlService webUrlService;
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

    private Map<String, INuxeoPlayerModule> players = new ConcurrentHashMap<String, INuxeoPlayerModule>();


    /** Navigation panel players. */
    private Map<String, PanelPlayer> navigationPanelPlayers;


    /** Portal URL factory. */
    protected final ICustomizationService customizationService;
    /** Taskbar service. */
    private final ITaskbarService taskbarService;


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
        this.players = new Hashtable<String, INuxeoPlayerModule>();
        this.players.put("defaultPlayer", new DefaultPlayer());

        // Plugin
        this.pluginMgr = new CustomizationPluginMgr(this);

        try {
            // Initialisé ici pour résoudre problème de classloader
            this.cl = Thread.currentThread().getContextClassLoader();

            this.parser = WysiwygParser.getInstance().getParser();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Taskbar service
        this.taskbarService = Locator.findMBean(ITaskbarService.class, ITaskbarService.MBEAN_NAME);
    }


    /**
     * Get webId service.
     *
     * @return webId service
     */
    public IWebIdService getWebIdService() {
        if (this.webIdService == null) {
            this.webIdService = (IWebIdService) this.portletCtx.getAttribute("webIdService");
        }

        return this.webIdService;
    }


    /**
     * Get web URL service.
     *
     * @return web URL service
     */
    public IWebUrlService getWebUrlService() {
        if (this.webUrlService == null) {
            this.webUrlService = Locator.findMBean(IWebUrlService.class, IWebUrlService.MBEAN_NAME);
        }
        return this.webUrlService;
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
    @Deprecated
    public NavigationItemAdapter getNavigationItemAdapter() {
        if (this.navigationItemAdapter == null) {
            this.navigationItemAdapter = new NavigationItemAdapter(this.portletCtx, this, this.cmsService);
        }
        return this.navigationItemAdapter;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public final Map<String,EditableWindow> getEditableWindows(Locale locale) {
        return this.pluginMgr.customizeEditableWindows(locale);
    }


    /**
     * Init editable windows.
     *
     * @param locale current user locale
     * @return editable windows
     */
    public Map<String, EditableWindow> initEditableWindows(Locale locale) {
    	Map<String,EditableWindow> map = new HashMap<String, EditableWindow>();
    	map.put("fgt.html", new HTMLEditableWindow("toutatice-portail-cms-nuxeo-viewFragmentPortletInstance", "html_Frag_"));
    	map.put("fgt.list", new ListEditableWindow("toutatice-portail-cms-nuxeo-viewListPortletInstance", "liste_Frag_"));
    	map.put("fgt.picture", new PictureEditableWindow("toutatice-portail-cms-nuxeo-viewFragmentPortletInstance", "picture_Frag_"));
    	map.put("fgt.portlet", new PortletEditableWindow("", "portlet_Frag_"));
    	map.put("ew.fragment", new FragmentEditableWindow("toutatice-portail-cms-nuxeo-viewFragmentPortletInstance", "ew_frag_"));

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
        return this.pluginMgr;
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

        return this.pluginMgr.customizeListTemplates(locale);
    }


    public List<ListTemplate> initListTemplates(Locale locale) {

        List<ListTemplate> templates = new ArrayList<ListTemplate>();

        // Bundle
        Bundle bundle = this.bundleFactory.getBundle(locale);

        // Minimal
        templates.add(new ListTemplate(ViewList.LIST_TEMPLATE_MINI, bundle.getString("LIST_TEMPLATE_MINI"), DEFAULT_SCHEMAS));
        // Normal
        templates.add(new ListTemplate(ViewList.LIST_TEMPLATE_NORMAL, bundle.getString("LIST_TEMPLATE_NORMAL"), DEFAULT_SCHEMAS));
        // Detailed
        templates.add(new ListTemplate(ViewList.LIST_TEMPLATE_DETAILED, bundle.getString("LIST_TEMPLATE_DETAILED"), DEFAULT_SCHEMAS));
        // Editorial
        templates.add(new ListTemplate(ViewList.LIST_TEMPLATE_EDITORIAL, bundle.getString("LIST_TEMPLATE_EDITORIAL"), DEFAULT_SCHEMAS));
        // Contextual links
        templates.add(new ListTemplate(ViewList.LIST_TEMPLATE_CONTEXTUAL_LINKS, bundle.getString("LIST_TEMPLATE_CONTEXTUAL_LINKS"), DEFAULT_SCHEMAS));


        return templates;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Map<String, FragmentType> getFragmentTypes(Locale locale) {

        return this.pluginMgr.getFragments(locale);


    }





    /**
     * Inits the list fragments.
     *
     * @param locale the locale
     * @return the list
     */
    public List<FragmentType> initListFragments(Locale locale) {

        List<FragmentType> fragmentTypes = new ArrayList<FragmentType>();

        // Portlet context
        PortletContext portletContext = this.getPortletCtx();
        // Bundle
        Bundle bundle = this.bundleFactory.getBundle(locale);

        // Text fragment
        fragmentTypes.add(new FragmentType(PropertyFragmentModule.TEXT_ID, bundle.getString("FRAGMENT_TYPE_TEXT"), new PropertyFragmentModule(portletContext,
                false)));
        // HTML fragment
        fragmentTypes.add(new FragmentType(PropertyFragmentModule.HTML_ID, bundle.getString("FRAGMENT_TYPE_HTML"), new PropertyFragmentModule(portletContext,
                true)));

        // Navigation picture fragment
        fragmentTypes.add(new FragmentType(NavigationPictureFragmentModule.ID, bundle.getString("FRAGMENT_TYPE_NAVIGATION_PICTURE"),
                new NavigationPictureFragmentModule(portletContext)));
        // Document attachment picture fragment
        fragmentTypes.add(new FragmentType(DocumentPictureFragmentModule.ID, bundle.getString("FRAGMENT_TYPE_DOCUMENT_PICTURE"),
                new DocumentPictureFragmentModule(portletContext)));
        // Link fragment
        fragmentTypes.add(new FragmentType(LinkFragmentModule.ID, bundle.getString("FRAGMENT_TYPE_LINK"), new LinkFragmentModule(portletContext)));
        // Space menubar fragment
        fragmentTypes.add(new FragmentType(SpaceMenubarFragmentModule.ID, bundle.getString("FRAGMENT_TYPE_MENUBAR"), new SpaceMenubarFragmentModule(
                portletContext)));
        // Site picture fragment
        fragmentTypes.add(new FragmentType(SitePictureFragmentModule.ID, bundle.getString("FRAGMENT_TYPE_SITE_PICTURE"), new SitePictureFragmentModule(
                portletContext)));

        return fragmentTypes;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedMap<String, String> getMenuTemplates(Locale locale) {
        return this.pluginMgr.customizeMenuTemplates(locale);
    }


    /**
     * Menu templates initialization.
     *
     * @param locale current user locale
     * @return menu templates
     */
    public SortedMap<String, String> initMenuTemplates(Locale locale) {
        SortedMap<String, String> templates = Collections.synchronizedSortedMap(new TreeMap<String, String>());

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
     * @throws PortletException
     * @throws Exception
     */
	public Player getCMSDefaultPlayer(CMSServiceCtx ctx) throws PortletException {
        Document doc = (Document) ctx.getDoc();

        DocumentContext<Document> docCtx = NuxeoController.getDocumentContext(ctx, doc.getPath());

		return this.getCMSDefaultPlayer(docCtx);
	}


    /**
     * Get CMS default player.
     *
     * @param docCtx document context
     * @return CMS default player
     * @throws Exception
     */
    public Player getCMSDefaultPlayer(DocumentContext<Document> docCtx) {

        return this.players.get("defaultPlayer").getCMSPlayer(docCtx);
    }


    /**
     * Get CMS ordered folder player.
     *
     * @param ctx CMS context
     * @return CMS ordered folder player
     * @throws CMSException
     */
    public Player getCMSOrderedFolderPlayer(DocumentContext<Document> docCtx) throws CMSException {

        BasicPublicationInfos navigationInfos = docCtx.getPublicationInfos(BasicPublicationInfos.class);
        Document doc = docCtx.getDoc();

        Map<String, String> windowProperties = new HashMap<String, String>();
        windowProperties.put("osivia.nuxeoRequest", NuxeoController.createFolderRequest(docCtx, true));
        windowProperties.put("osivia.cms.style", ViewList.LIST_TEMPLATE_EDITORIAL);
        windowProperties.put("osivia.hideDecorators", "1");
        windowProperties.put("theme.dyna.partial_refresh_enabled", "false");
        windowProperties.put(Constants.WINDOW_PROP_SCOPE, navigationInfos.getScope());
        windowProperties.put(Constants.WINDOW_PROP_VERSION, navigationInfos.getState().toString());
        windowProperties.put("osivia.document.metadata", String.valueOf(false));
        windowProperties.put("osivia.title", "Dossier " + doc.getTitle());
        windowProperties.put("osivia.cms.pageSizeMax", "10");

        Player linkProps = new Player();
        linkProps.setWindowProperties(windowProperties);
        linkProps.setPortletInstance("toutatice-portail-cms-nuxeo-viewListPortletInstance");

        return linkProps;
    }



    /**
     * Get file browser player properties.
     *
     * @param docCtx CMS context
     * @return player properties
     */
    @Override
    public Player getCMSFileBrowser(DocumentContext<Document> docCtx) {

        Document document = docCtx.getDoc();
        BasicPublicationInfos navigationInfos = docCtx.getPublicationInfos(BasicPublicationInfos.class);

        Map<String, String> windowProperties = new HashMap<String, String>();
        //windowProperties.put(Constants.WINDOW_PROP_SCOPE, docCtx.getScope());
        windowProperties.put(Constants.WINDOW_PROP_VERSION, navigationInfos.getState().toString());
        //windowProperties.put(InternalConstants.METADATA_WINDOW_PROPERTY, docCtx.getHideMetaDatas());
        windowProperties.put(Constants.WINDOW_PROP_URI, document.getPath());
        windowProperties.put("osivia.cms.publishPathAlreadyConverted", "1");
        windowProperties.put("osivia.hideDecorators", "1");
        windowProperties.put("osivia.ajaxLink", "1");

        Player linkProps = new Player();
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
    public Player getCMSFolderPlayer(DocumentContext<Document> docCtx) throws CMSException {
        Document doc = docCtx.getDoc();
        BasicPublicationInfos navigationInfos = docCtx.getPublicationInfos(BasicPublicationInfos.class);

        Map<String, String> windowProperties = new HashMap<String, String>();
        windowProperties.put("osivia.nuxeoRequest", NuxeoController.createFolderRequest(docCtx, false));
        windowProperties.put("osivia.cms.style", ViewList.LIST_TEMPLATE_EDITORIAL);
        windowProperties.put("osivia.hideDecorators", "1");
        windowProperties.put("theme.dyna.partial_refresh_enabled", "false");
        windowProperties.put(Constants.WINDOW_PROP_SCOPE, navigationInfos.getScope());
        windowProperties.put(Constants.WINDOW_PROP_VERSION, navigationInfos.getState().toString());
        //TODO
        //windowProperties.put(InternalConstants.METADATA_WINDOW_PROPERTY, ctx.getHideMetaDatas());
        windowProperties.put("osivia.title", doc.getTitle());
        windowProperties.put("osivia.cms.pageSizeMax", "10");

        Player linkProps = new Player();
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
    public Player getCMSSectionPlayer(CMSServiceCtx ctx) {
        Document doc = (Document) ctx.getDoc();

        Map<String, String> windowProperties = new HashMap<String, String>();

        windowProperties.put("osivia.nuxeoRequest", "ecm:path STARTSWITH '" + doc.getPath() + "' AND ecm:mixinType != 'Folderish' ORDER BY dc:modified DESC");
        windowProperties.put("osivia.cms.style", ViewList.LIST_TEMPLATE_EDITORIAL);
        windowProperties.put("osivia.hideDecorators", "1");
        windowProperties.put("theme.dyna.partial_refresh_enabled", "false");
        windowProperties.put(Constants.WINDOW_PROP_SCOPE, ctx.getScope());
        windowProperties.put(Constants.WINDOW_PROP_VERSION, ctx.getDisplayLiveVersion());
        windowProperties.put(InternalConstants.METADATA_WINDOW_PROPERTY, ctx.getHideMetaDatas());
        windowProperties.put("osivia.title", "Dossier " + doc.getTitle());
        windowProperties.put("osivia.cms.pageSizeMax", "10");

        Player linkProps = new Player();
        linkProps.setWindowProperties(windowProperties);
        linkProps.setPortletInstance("toutatice-portail-cms-nuxeo-viewListPortletInstance");

        return linkProps;
    }


//    /**
//     * Get CMS virtual page player.
//     *
//     * @param ctx CMS context
//     * @return CMS virtual page player
//     */
//    public Player getCMSVirtualPagePlayer(CMSServiceCtx ctx) {
//        Document doc = (Document) ctx.getDoc();
//
//        Map<String, String> windowProperties = new HashMap<String, String>();
//        windowProperties.put("osivia.nuxeoRequest", doc.getString("ttc:queryPart"));
//        windowProperties.put("osivia.cms.style", ViewList.LIST_TEMPLATE_EDITORIAL);
//        windowProperties.put("osivia.hideDecorators", "1");
//        windowProperties.put("theme.dyna.partial_refresh_enabled", "false");
//        windowProperties.put(Constants.WINDOW_PROP_SCOPE, ctx.getScope());
//        // windowProperties.put(Constants.WINDOW_PROP_VERSION, ctx.getDisplayLiveVersion());
//        windowProperties.put(InternalConstants.METADATA_WINDOW_PROPERTY, ctx.getHideMetaDatas());
//        windowProperties.put("osivia.title", "Dossier " + doc.getTitle());
//        windowProperties.put("osivia.cms.pageSizeMax", "10");
//
//        Player linkProps = new Player();
//        linkProps.setWindowProperties(windowProperties);
//        linkProps.setPortletInstance("toutatice-portail-cms-nuxeo-viewListPortletInstance");
//
//        return linkProps;
//    }


    /**
     * Create portlet link.
     *
     * @param ctx CMS context
     * @param portletInstance portlet instance
     * @param uid UID
     * @return portlet link
     */
    public Player createPortletLink(CMSServiceCtx ctx, String portletInstance, String uid) {
        Map<String, String> windowProperties = new HashMap<String, String>();
        windowProperties.put(Constants.WINDOW_PROP_SCOPE, ctx.getScope());
        windowProperties.put(Constants.WINDOW_PROP_VERSION, ctx.getDisplayLiveVersion());
        windowProperties.put(InternalConstants.METADATA_WINDOW_PROPERTY, ctx.getHideMetaDatas());
        windowProperties.put(Constants.WINDOW_PROP_URI, uid);
        windowProperties.put("osivia.cms.publishPathAlreadyConverted", "1");
        windowProperties.put("osivia.hideDecorators", "1");
        windowProperties.put("theme.dyna.partial_refresh_enabled", "false");

        Player linkProps = new Player();
        linkProps.setWindowProperties(windowProperties);
        linkProps.setPortletInstance(portletInstance);

        return linkProps;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Player getCMSPlayer(CMSServiceCtx cmsContext) throws Exception {
        Document document = (Document) cmsContext.getDoc();
        CMSPublicationInfos pubInfos = this.cmsService.getPublicationInfos(cmsContext, document.getPath());

        // Workspace indicator
        boolean workspace = (cmsContext.getContextualizationBasePath() != null) && (pubInfos.isLiveSpace());

        List<INuxeoPlayerModule> modules = this.pluginMgr.customizeModules();
        DocumentContext<Document> docCtx = NuxeoController.getDocumentContext(cmsContext, document.getPath());

        for (INuxeoPlayerModule module : modules) {
            Player player = module.getCMSPlayer(docCtx);
            if (player != null) {
                return player;
            }
        }


        if ("UserWorkspace".equals(document.getType())) {
            // Pas de filtre sur les versions publiées
            cmsContext.setDisplayLiveVersion("1");
            return this.getCMSFileBrowser(docCtx);
        }


        if (("Folder".equals(document.getType()) || "OrderedFolder".equals(document.getType())) || ("Section".equals(document.getType()))) {
            if (workspace) {
                // File browser
                cmsContext.setDisplayLiveVersion("1");
                Player props = this.getCMSFileBrowser(docCtx);
                props.getWindowProperties().put("osivia.title", document.getTitle());
                return props;
            } else if ("Folder".equals(document.getType())) {
                return this.getCMSFolderPlayer(docCtx);
            } else {
                return this.getCMSOrderedFolderPlayer(docCtx);
            }
        }

//        if ("PortalVirtualPage".equals(document.getType())) {
//            return this.getCMSVirtualPagePlayer(cmsContext);
//        }


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

                        return this.players.get(playerInstance).getCMSPlayer(docCtx);
                    }
                }
            }

        }
        return this.players.get("defaultPlayer").getCMSPlayer(docCtx);
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
    public void formatContentMenuBar(CMSServiceCtx ctx) throws Exception {
        CMSPublicationInfos publicationInfos = null;
        CMSExtendedDocumentInfos extendedDocumentInfos = null;
        if(ctx.getDoc() != null) {
        	Document doc = (Document) ctx.getDoc();
        	publicationInfos = this.cmsService.getPublicationInfos(ctx, doc.getPath());
        	extendedDocumentInfos = this.cmsService.getExtendedDocumentInfos(ctx, doc.getPath());
        }

        this.getMenuBarFormater().formatContentMenuBar(ctx, publicationInfos, extendedDocumentInfos);
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
    public String addPublicationFilter(CMSServiceCtx ctx, String nuxeoRequest, String requestFilteringPolicy) throws Exception {
        /* Filtre pour sélectionner uniquement les version publiées */
        String requestFilter = StringUtils.EMPTY;

        if ("1".equals(ctx.getDisplayLiveVersion())) {
            // selection des versions lives : il faut exclure les proxys
            requestFilter = "ecm:mixinType != 'HiddenInNavigation' AND ecm:isProxy = 0  AND ecm:currentLifeCycleState <> 'deleted'  AND ecm:isCheckedInVersion = 0 "
                            + "AND ecm:currentLifeCycleState <> 'deleted'";

        } else if("2".equals(ctx.getDisplayLiveVersion())){
            // All except lives of publish spaces
            requestFilter = " ecm:mixinType <> 'HiddenInNavigation' AND ecm:currentLifeCycleState <> 'deleted'  AND ecm:isCheckedInVersion = 0"
                            + " AND ecm:mixinType <> 'isLocalPublishLive'";
        } else {
            // sélection des folders et des documents publiés
            requestFilter = "ecm:isProxy = 1 AND ecm:mixinType != 'HiddenInNavigation'  AND ecm:currentLifeCycleState <> 'deleted' AND ecm:isCheckedInVersion = 0";
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
            String portalURL = this.portalUrlFactory.getCMSUrl(portalControllerContext, currentPagePath, cmsPath, parameters, null, null, null, null, null,
                    null);
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

            // WebId (wich can have parameter
            String webId = StringUtils.removeStart(url, "/nuxeo/web/");
            if (webId.contains("/")) {
                webId = StringUtils.substringAfterLast(webId, "/");
            }
            if (webId.contains(".")) {
                webId = StringUtils.substringBefore(webId, ".");
            }

            cmsPath = this.getWebIdService().webIdToCmsPath(webId);
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
    public Map<String, DocumentType> getCMSItemTypes() {

        return this.pluginMgr.customizeCMSItemTypes();

    }

    protected List<DocumentType> getCustomizedCMSItemTypes() {
        return new ArrayList<DocumentType>();
    }


    /**
     * Get default CMS item types.
     *
     * @return default CMS item types
     */
    public List<DocumentType> getDefaultCMSItemTypes() {
        List<DocumentType> defaultTypes = new ArrayList<DocumentType>();

        // Workspace
        defaultTypes.add(new DocumentType("Workspace", true, false, false, true, false, false,
                Arrays.asList("Folder", "File", "Note", "ContextualLink", "Room"), "/default/templates/workspace", "glyphicons glyphicons-wallet", true));
        // Portal site
        defaultTypes.add(new DocumentType("PortalSite", true, false, false, true, true, true, Arrays.asList("File", "PortalPage", "ContextualLink"), null,
                "glyphicons glyphicons-global", true));
        // Portal page
        defaultTypes.add(new DocumentType("PortalPage", true, true, true, true, true, true, Arrays.asList("File", "PortalPage", "ContextualLink"), null,
                "glyphicons glyphicons-more-items"));
        // Folder
        defaultTypes.add(new DocumentType("Folder", true, true, true, false, false, true, Arrays.asList("File", "Folder", "Note"), null,
                "glyphicons glyphicons-folder-closed"));
        // Ordered folder
        defaultTypes.add(new DocumentType("OrderedFolder", true, true, true, true, false, true, Arrays.asList("File", "Folder", "Note"), null,
                "glyphicons glyphicons-folder-closed"));
        // File
        defaultTypes.add(new DocumentType("File", false, false, false, false, false, true, new ArrayList<String>(0), null, "glyphicons glyphicons-file", false,
                true, true));
        // Note
        defaultTypes.add(new DocumentType("Note", false, false, false, false, false, true, new ArrayList<String>(0), null, "glyphicons glyphicons-note"));
        // Contextual link
        defaultTypes.add(new DocumentType("ContextualLink", false, false, false, false, false, true, new ArrayList<String>(0), null,
                "glyphicons glyphicons-link"));
        // Room
        defaultTypes.add(new DocumentType("Room", true, false, false, true, false, false, Arrays.asList("Folder", "File", "Note", "ContextualLink", "Room"),
                "/default/templates/room", "glyphicons glyphicons-cube-black"));
        // Staple
        defaultTypes.add(new DocumentType("Staple", false, true, false, false, false, false, new ArrayList<String>(0), null, "glyphicons glyphicons-nails"));

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
    @Deprecated
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

        if (StringUtils.isNotEmpty(webId) && !ContextDocumentsHelper.isRemoteProxy(doc)) {
            String explicitUrl = doc.getString("ttc:explicitUrl");
            String extension = doc.getString("ttc:extensionUrl");


            Map<String, String> properties = new HashMap<String, String>();
            if (explicitUrl != null) {
                properties.put(IWebIdService.EXPLICIT_URL, explicitUrl);
            }
            if (extension != null) {
                properties.put(IWebIdService.EXTENSION_URL, extension);
            }

            permLinkPath = this.getWebIdService().webIdToCmsPath(webId);
        }

        return permLinkPath;
    }

    @Override
    public Link getUserAvatar(String username) {
        String src = "";
        
        // get timestamp defined previously
        String avatarTime = this.avatarMap.get(username);

        if (avatarTime == null) {
            // if not defined, set ie
            avatarTime = this.refreshUserAvatar(username);
        }

        // timestamp is concated in the url to control the client cache
        try {
			src = AVATAR_SERVLET.concat(URLEncoder.encode(username, "UTF-8")).concat("&t=").concat(avatarTime.toString());
		} catch (UnsupportedEncodingException e) {
			LOGGER.error(e);
		}

        return new Link(src, false);
    }

    @Override
    public Link getUserAvatar(CMSServiceCtx cmsCtx, String username) throws CMSException {
    	return getUserAvatar(username);

    }
    
    @Override
    public String refreshUserAvatar(String username) {

        // renew the timestamp and map it to the user
        String avatarTime = Long.toString(new Date().getTime());

        this.avatarMap.put(username, avatarTime);

        return avatarTime;
    }    

    @Override
    public String refreshUserAvatar(CMSServiceCtx cmsCtx, String username) {

        return refreshUserAvatar(username);
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


            // File name
            String fileName = binary.getFileName();
            if (fileName == null) {
                if (binary.getDocument() != null) {
                    Document document = (Document) binary.getDocument();
                    PropertyMap fileMap = document.getProperties().getMap("file:content");
                    if (fileMap != null) {
                        fileName = fileMap.getString("name");
                    }
                } else if ((binary.getIndex() != null) && (cmsCtx.getDoc() != null)) {
                    int index = NumberUtils.toInt(binary.getIndex());
                    Document document = (Document) cmsCtx.getDoc();
                    PropertyList files = document.getProperties().getList("files:files");
                    if ((files != null) && (files.size() > index)) {
                        PropertyMap fileMap = files.getMap(index);
                        if (fileMap != null) {
                            fileName = fileMap.getString("filename");
                        }
                    }
                }
            }


            // URL builder
            StringBuilder sb = new StringBuilder();
            sb.append(BINARY_SERVLET);

            if (StringUtils.isNotBlank(fileName)) {
                sb.append("/").append(fileName);
            }

            sb.append("?type=").append(binary.getType().name()).append("&path=").append(URLEncoder.encode(path, "UTF-8"));

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

        commands.put(EcmCommonCommands.eraseModifications.name(), new EraseModificationsCommand(this.getNotificationsService(), this.getInternationalizationService()));

		return commands;
	}


    /**
     * Get default taskbar items.
     *
     * @return taskbar items
     * @throws CMSException
     */
    public TaskbarItems getDefaultTaskbarItems() throws CMSException {
	    // Taskbar items
	    TaskbarItems taskbarItems;

        // Taskbar items factory
        TaskbarFactory factory = this.taskbarService.getFactory();
        taskbarItems = factory.createTaskbarItems();

        // Home
        TaskbarItem home = factory.createTransversalTaskbarItem(ITaskbarService.HOME_TASK_ID, "HOME_TASK", "glyphicons glyphicons-home", null);
        taskbarItems.add(home);

        // Search
        PanelPlayer searchPlayer = new PanelPlayer();
        searchPlayer.setInstance("toutatice-portail-cms-nuxeo-searchPortletInstance");
        Map<String, String> properties = new HashMap<String, String>(1);
        properties.put(Constants.WINDOW_PROP_URI, "${basePath}");
        searchPlayer.setProperties(properties);
        TaskbarItem search = factory.createTransversalTaskbarItem("SEARCH", "SEARCH_TASK", "glyphicons glyphicons-search", searchPlayer);
        taskbarItems.add(search);

        // Documents
        TaskbarItem documents = factory.createCmsTaskbarItem("DOCUMENTS", "DOCUMENTS_TASK", "glyphicons glyphicons-folder-closed", "Folder");
        documents.setToDefault(1);
        taskbarItems.add(documents);

        return taskbarItems;
	}


    /**
     * Get navigation panel players.
     *
     * @return panel players, grouped by maximized window instance
     */
    public Map<String, PanelPlayer> getNavigationPanelPlayers() {
        if (this.navigationPanelPlayers == null) {
            this.navigationPanelPlayers = new ConcurrentHashMap<String, PanelPlayer>();

            // File browser
            this.navigationPanelPlayers.put("toutatice-portail-cms-nuxeo-fileBrowserPortletInstance", this.getFileBrowserPanelPlayer());
        }
        return this.navigationPanelPlayers;
    }


    /**
     * Get file browser panel player.
     *
     * @return panel player
     */
    protected PanelPlayer getFileBrowserPanelPlayer() {
        PanelPlayer player = new PanelPlayer();
        player.setInstance("toutatice-portail-cms-nuxeo-publishMenuPortletInstance");
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("osivia.bootstrapPanelStyle", String.valueOf(true));
        properties.put("osivia.cms.template", "fancytree-lazy");
        properties.put("osivia.cms.startLevel", String.valueOf(2));
        properties.put("osivia.cms.forceNavigation", String.valueOf(true));

        player.setProperties(properties);
        return player;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getJSPName(String name, PortletContext portletContext, PortletRequest request) throws CMSException {
        try {
            CustomizedJsp customizedPage = this.pluginMgr.customizeJSP(name, portletContext, request);

            String customizedName;
            if (customizedPage != null) {
                customizedName = customizedPage.getName();
            } else {
                customizedName = null;
            }
            return customizedName;
        } catch( IOException e){
            throw new CMSException( e);
        }
    }

}
