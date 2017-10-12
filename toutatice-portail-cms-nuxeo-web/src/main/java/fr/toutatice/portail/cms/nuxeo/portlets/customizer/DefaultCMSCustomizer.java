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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.ResourceURL;
import javax.portlet.WindowState;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.servlet.http.HttpSessionEvent;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.portal.common.invocation.Scope;
import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.PortalObject;
import org.jboss.portal.core.model.portal.PortalObjectContainer;
import org.jboss.portal.core.model.portal.PortalObjectId;
import org.jboss.portal.core.model.portal.PortalObjectPath;
import org.jboss.portal.core.model.portal.Window;
import org.jboss.portal.server.ServerInvocation;
import org.jboss.portal.server.ServerInvocationContext;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.cms.DocumentType;
import org.osivia.portal.api.cms.FileDocumentType;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.ecm.EcmCommand;
import org.osivia.portal.api.ecm.EcmCommonCommands;
import org.osivia.portal.api.internationalization.Bundle;
import org.osivia.portal.api.internationalization.IBundleFactory;
import org.osivia.portal.api.internationalization.IInternationalizationService;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.notifications.INotificationsService;
import org.osivia.portal.api.panels.PanelPlayer;
import org.osivia.portal.api.player.IPlayerModule;
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
import org.osivia.portal.core.cms.CMSPage;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.constants.InternalConstants;
import org.osivia.portal.core.context.ControllerContextAdapter;
import org.osivia.portal.core.customization.ICustomizationService;
import org.osivia.portal.core.page.PageProperties;
import org.osivia.portal.core.web.IWebIdService;
import org.osivia.portal.core.web.IWebUrlService;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import fr.toutatice.portail.cms.nuxeo.api.ContextualizationHelper;
import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.cms.NuxeoDocumentContext;
import fr.toutatice.portail.cms.nuxeo.api.domain.CommentDTO;
import fr.toutatice.portail.cms.nuxeo.api.domain.CustomizedJsp;
import fr.toutatice.portail.cms.nuxeo.api.domain.EditableWindow;
import fr.toutatice.portail.cms.nuxeo.api.domain.FragmentType;
import fr.toutatice.portail.cms.nuxeo.api.domain.ListTemplate;
import fr.toutatice.portail.cms.nuxeo.api.forms.FormFilter;
import fr.toutatice.portail.cms.nuxeo.api.player.INuxeoPlayerModule;
import fr.toutatice.portail.cms.nuxeo.api.portlet.ViewList;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCommentsService;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCustomizer;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoConnectionProperties;
import fr.toutatice.portail.cms.nuxeo.portlets.cms.ExtendedDocumentInfos;
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
import fr.toutatice.portail.cms.nuxeo.portlets.document.helpers.DocumentHelper;
import fr.toutatice.portail.cms.nuxeo.portlets.forms.ProcedureTemplateModule;
import fr.toutatice.portail.cms.nuxeo.portlets.fragment.AttachmentsFragmentModule;
import fr.toutatice.portail.cms.nuxeo.portlets.fragment.DocumentPictureFragmentModule;
import fr.toutatice.portail.cms.nuxeo.portlets.fragment.LinkFragmentModule;
import fr.toutatice.portail.cms.nuxeo.portlets.fragment.NavigationPictureFragmentModule;
import fr.toutatice.portail.cms.nuxeo.portlets.fragment.PropertyFragmentModule;
import fr.toutatice.portail.cms.nuxeo.portlets.fragment.SitePictureFragmentModule;
import fr.toutatice.portail.cms.nuxeo.portlets.fragment.SpaceMenubarFragmentModule;
import fr.toutatice.portail.cms.nuxeo.portlets.service.CMSService;
import fr.toutatice.portail.cms.nuxeo.service.commands.DeleteDocumentCommand;
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

    /** Default schemas. */
    public static final String DEFAULT_SCHEMAS = "dublincore, common, toutatice, file";
    /** PROCEDURE_SCHEMAS */
    public static final String PROCEDURE_SCHEMAS = "dublincore, common, toutatice, procedureInstance, record";
    /** Template "download". */
    public static final String TEMPLATE_DOWNLOAD = "download";

    /** Servlet URL for avatars */
    private static final String AVATAR_SERVLET = "/toutatice-portail-cms-nuxeo/avatar?username=";
    /** Binary servlet URL. */
    private static final String BINARY_SERVLET = "/toutatice-portail-cms-nuxeo/binary";

    /** Query filter pattern. */
    private static final Pattern QUERY_FILTER_PATTERN = Pattern.compile("(.*)ORDER([ ]*)BY(.*)");


    /** Log. */
    private final Log log;
    /** Portlet context. */
    private final PortletContext portletContext;
    /** CMS service. */
    private final CMSService cmsService;

    /** Binary delegations. */
    private final Map<String, Map<String, BinaryDelegation>> delegations;
    /** Players. */
    private final Map<String, INuxeoPlayerModule> players;
    /** Navigation panel players. */
    private final Map<String, PanelPlayer> navigationPanelPlayers;
    /** Avatar map. */
    private final Map<String, String> avatarMap;
    /** Binary timestamps. */
    private final Map<String, BinaryTimestamp> binaryTimestamps;

    /** Plugin manager. */
    private final CustomizationPluginMgr pluginManager;
    /** Nuxeo connection properties. */
    private final NuxeoConnectionProperties nuxeoConnection;
    /** User pages loader. */
    private final UserPagesLoader userPagesLoader;
    /** Menubar formatter. */
    private final MenuBarFormater menubarFormater;
    /** Browser adapter. */
    private final BrowserAdapter browserAdapter;
    /** Navigation item adapter. */
    @Deprecated
    private final NavigationItemAdapter navigationItemAdapter;

    /** Class loader. */
    private final ClassLoader classLoader;
    /** XML parser. */
    private final XMLReader parser;

    /** Portal URL factory. */
    private final IPortalUrlFactory portalUrlFactory;
    /** WebId service. */
    private final IWebIdService webIdService;
    /** Web URL service. */
    private final IWebUrlService webUrlService;
    /** Taskbar service. */
    private final ITaskbarService taskbarService;
    /** Customization service. */
    private final ICustomizationService customizationService;
    /** Nuxeo comments service. */
    private final INuxeoCommentsService nuxeoCommentsService;
    /** Internationalization service */
    private final IInternationalizationService internationalizationService;
    /** Internationalization bundle factory. */
    private final IBundleFactory bundleFactory;
    /** Notification service */
    private final INotificationsService notificationsService;


    /**
     * Constructor.
     *
     * @param portletContext portlet context
     * @param cmsService CMS service
     */
    public DefaultCMSCustomizer(PortletContext portletContext, CMSService cmsService) {
        super();
        this.log = LogFactory.getLog(this.getClass());
        this.portletContext = portletContext;
        this.cmsService = cmsService;

        // Binary delegations
        this.delegations = new ConcurrentHashMap<String, Map<String, BinaryDelegation>>();
        // Players
        this.players = new ConcurrentHashMap<String, INuxeoPlayerModule>();
        this.players.put("defaultPlayer", new DefaultPlayer());
        // Navigation panel players
        this.navigationPanelPlayers = new ConcurrentHashMap<String, PanelPlayer>();
        this.navigationPanelPlayers.put("toutatice-portail-cms-nuxeo-fileBrowserPortletInstance", this.getFileBrowserPanelPlayer());
        // Avatar map
        this.avatarMap = new ConcurrentHashMap<String, String>();
        // Binary timestamps
        this.binaryTimestamps = new ConcurrentHashMap<String, BinaryTimestamp>();

        // Plugin manager
        this.pluginManager = new CustomizationPluginMgr(this);
        // Nuxeo connection properties
        this.nuxeoConnection = new NuxeoConnectionProperties();
        // User pages loader
        this.userPagesLoader = new UserPagesLoader();
        // Menubar formatter
        this.menubarFormater = new MenuBarFormater(this);
        // Browser adapter
        this.browserAdapter = BrowserAdapter.getInstance(cmsService);
        // Navigation item adapter
        this.navigationItemAdapter = new NavigationItemAdapter(this);

        try {
            // Initialisé ici pour résoudre problème de classloader
            this.classLoader = Thread.currentThread().getContextClassLoader();

            this.parser = WysiwygParser.getInstance().getParser();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Portal URL factory
        this.portalUrlFactory = Locator.findMBean(IPortalUrlFactory.class, IPortalUrlFactory.MBEAN_NAME);
        // WebId service
        this.webIdService = Locator.findMBean(IWebIdService.class, IWebIdService.MBEAN_NAME);
        // Web URL service
        this.webUrlService = Locator.findMBean(IWebUrlService.class, IWebUrlService.MBEAN_NAME);
        // Taskbar service
        this.taskbarService = Locator.findMBean(ITaskbarService.class, ITaskbarService.MBEAN_NAME);
        // Customization Service
        this.customizationService = Locator.findMBean(ICustomizationService.class, ICustomizationService.MBEAN_NAME);
        // Nuxeo comments service
        this.nuxeoCommentsService = new NuxeoCommentsServiceImpl(this.cmsService);
        // Internationalization service
        this.internationalizationService = Locator.findMBean(IInternationalizationService.class, IInternationalizationService.MBEAN_NAME);
        // Internationalization bundle factory
        this.bundleFactory = this.internationalizationService.getBundleFactory(this.getClass().getClassLoader());
        // Notifications service
        this.notificationsService = Locator.findMBean(INotificationsService.class, INotificationsService.MBEAN_NAME);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public final Map<String, EditableWindow> getEditableWindows(Locale locale) {
        return this.pluginManager.customizeEditableWindows(locale);
    }


    /**
     * Init editable windows.
     *
     * @param locale current user locale
     * @return editable windows
     */
    public Map<String, EditableWindow> initEditableWindows(Locale locale) {
        Map<String, EditableWindow> map = new HashMap<String, EditableWindow>();
        map.put("fgt.html", new HTMLEditableWindow("toutatice-portail-cms-nuxeo-viewFragmentPortletInstance", "html_Frag_"));
        map.put("fgt.list", new ListEditableWindow("toutatice-portail-cms-nuxeo-viewListPortletInstance", "liste_Frag_"));
        map.put("fgt.picture", new PictureEditableWindow("toutatice-portail-cms-nuxeo-viewFragmentPortletInstance", "picture_Frag_"));
        map.put("fgt.portlet", new PortletEditableWindow("", "portlet_Frag_"));
        map.put("ew.fragment", new FragmentEditableWindow("toutatice-portail-cms-nuxeo-viewFragmentPortletInstance", "ew_frag_"));

        return map;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public final List<ListTemplate> getListTemplates(Locale locale) {
        return this.pluginManager.customizeListTemplates(locale);
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

        // Procedure
        ListTemplate procedureTemplate = new ListTemplate(ViewList.LIST_TEMPLATE_PROCEDURE, bundle.getString("LIST_TEMPLATE_PROCEDURE"), PROCEDURE_SCHEMAS);
        procedureTemplate.setModule(new ProcedureTemplateModule(portletContext));
        templates.add(procedureTemplate);

        return templates;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public final Map<String, FragmentType> getFragmentTypes(Locale locale) {
        return this.pluginManager.getFragments(locale);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, FormFilter> getFormsFilters() {
        return this.pluginManager.getFormFilters();
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
        fragmentTypes.add(new FragmentType(PropertyFragmentModule.TEXT_ID, bundle.getString("FRAGMENT_TYPE_TEXT"),
                new PropertyFragmentModule(this.portletContext, false)));
        // HTML fragment
        fragmentTypes.add(new FragmentType(PropertyFragmentModule.HTML_ID, bundle.getString("FRAGMENT_TYPE_HTML"),
                new PropertyFragmentModule(this.portletContext, true)));

        // Navigation picture fragment
        fragmentTypes.add(new FragmentType(NavigationPictureFragmentModule.ID, bundle.getString("FRAGMENT_TYPE_NAVIGATION_PICTURE"),
                new NavigationPictureFragmentModule(this.portletContext)));
        // Document attachment picture fragment
        fragmentTypes.add(new FragmentType(DocumentPictureFragmentModule.ID, bundle.getString("FRAGMENT_TYPE_DOCUMENT_PICTURE"),
                new DocumentPictureFragmentModule(this.portletContext)));
        // Link fragment
        fragmentTypes.add(new FragmentType(LinkFragmentModule.ID, bundle.getString("FRAGMENT_TYPE_LINK"), new LinkFragmentModule(this.portletContext)));
        // Space menubar fragment
        fragmentTypes.add(new FragmentType(SpaceMenubarFragmentModule.ID, bundle.getString("FRAGMENT_TYPE_MENUBAR"),
                new SpaceMenubarFragmentModule(this.portletContext)));
        // Site picture fragment
        fragmentTypes.add(new FragmentType(SitePictureFragmentModule.ID, bundle.getString("FRAGMENT_TYPE_SITE_PICTURE"),
                new SitePictureFragmentModule(this.portletContext)));
        // Attachments fragment
        fragmentTypes.add(new FragmentType(AttachmentsFragmentModule.ID, bundle.getString("FRAGMENT_TYPE_ATTACHMENTS"),
                new AttachmentsFragmentModule(this.portletContext)));

        return fragmentTypes;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public SortedMap<String, String> getMenuTemplates(Locale locale) {
        return this.pluginManager.customizeMenuTemplates(locale);
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
     * @param cmsContext CMS context
     * @return CMS default player
     * @throws PortletException
     * @throws Exception
     */
    public Player getCMSDefaultPlayer(CMSServiceCtx cmsContext) throws PortletException {
        Document document = (Document) cmsContext.getDoc();

        // Document context
        NuxeoDocumentContext documentContext;
        try {
            documentContext = cmsService.getDocumentContext(cmsContext, document.getPath());
        } catch (CMSException e) {
            throw new PortletException(e);
        }

        return this.getCMSDefaultPlayer(documentContext);
    }


    /**
     * Get CMS default player.
     *
     * @param docCtx document context
     * @return CMS default player
     * @throws Exception
     */
    public Player getCMSDefaultPlayer(NuxeoDocumentContext docCtx) {
        INuxeoPlayerModule module = this.players.get("defaultPlayer");
        return module.getCMSPlayer(docCtx);
    }


    /**
     * Get CMS ordered folder player.
     *
     * @param documentContext document context
     * @return CMS ordered folder player
     * @throws CMSException
     */
    public Player getCMSOrderedFolderPlayer(NuxeoDocumentContext documentContext) throws CMSException {
        Document document = documentContext.getDocument();

        Map<String, String> windowProperties = new HashMap<String, String>();
        windowProperties.put("osivia.nuxeoRequest", NuxeoController.createFolderRequest(documentContext, true));
        windowProperties.put("osivia.cms.style", ViewList.LIST_TEMPLATE_EDITORIAL);
        windowProperties.put("osivia.hideDecorators", "1");
        windowProperties.put("theme.dyna.partial_refresh_enabled", "false");
        windowProperties.put(Constants.WINDOW_PROP_SCOPE, documentContext.getScope());
        windowProperties.put(Constants.WINDOW_PROP_VERSION, documentContext.getDocumentState().toString());
        windowProperties.put("osivia.document.metadata", String.valueOf(false));
        windowProperties.put("osivia.title", "Dossier " + document.getTitle());
        windowProperties.put("osivia.cms.pageSizeMax", "10");

        Player linkProps = new Player();
        linkProps.setWindowProperties(windowProperties);
        linkProps.setPortletInstance("toutatice-portail-cms-nuxeo-viewListPortletInstance");

        return linkProps;
    }


    /**
     * Get file browser player properties.
     *
     * @param documentContext document context
     * @return player properties
     */
    @Override
    public Player getCMSFileBrowser(NuxeoDocumentContext documentContext) {
        Document document = documentContext.getDocument();

        Map<String, String> windowProperties = new HashMap<String, String>();
        windowProperties.put(Constants.WINDOW_PROP_VERSION, documentContext.getDocumentState().toString());
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
     * @param documentContext document context
     * @return CMS folder player
     * @throws CMSException
     */
    public Player getCMSFolderPlayer(NuxeoDocumentContext documentContext) throws CMSException {
        Document document = documentContext.getDocument();

        Map<String, String> windowProperties = new HashMap<String, String>();
        windowProperties.put("osivia.nuxeoRequest", NuxeoController.createFolderRequest(documentContext, false));
        windowProperties.put("osivia.cms.style", ViewList.LIST_TEMPLATE_EDITORIAL);
        windowProperties.put("osivia.hideDecorators", "1");
        windowProperties.put("theme.dyna.partial_refresh_enabled", "false");
        windowProperties.put(Constants.WINDOW_PROP_SCOPE, documentContext.getScope());
        windowProperties.put(Constants.WINDOW_PROP_VERSION, documentContext.getDocumentState().toString());
        windowProperties.put("osivia.title", document.getTitle());
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

        List<IPlayerModule> modules = this.pluginManager.customizeModules();
        NuxeoDocumentContext docCtx = this.cmsService.getDocumentContext(cmsContext, document.getPath());

        for (IPlayerModule module : modules) {
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

                // Player
                Player player = this.getCMSFileBrowser(docCtx);
                // Window properties
                Map<String, String> properties = player.getWindowProperties();
                properties.put(InternalConstants.PROP_WINDOW_TITLE, document.getTitle());
                properties.put(InternalConstants.PROP_WINDOW_TITLE_METADATA, String.valueOf(true));

                return player;
            } else if ("Folder".equals(document.getType())) {
                return this.getCMSFolderPlayer(docCtx);
            } else {
                return this.getCMSOrderedFolderPlayer(docCtx);
            }
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
                                windowProperties.put(map.get(WebConfigurationHelper.OPTION_KEY).toString(),
                                        map.get(WebConfigurationHelper.OPTION_VALUE).toString());
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
            if (("File".equals(document.getType()) || "Audio".equals(document.getType()) || "Video".equals(document.getType()))) {
                // Check context
                if ("download".equals(displayContext) || "downloadVersion".equals(displayContext)) {
                    url = createFileDownloadLink(cmsContext, document, displayContext);
                    downloadable = url != null;
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
     * Creates download URL of principal blob of document.
     * 
     * @param cmsContext
     * @param document
     * @param displayContext
     * @return download URL
     */
    public String createFileDownloadLink(CMSServiceCtx cmsContext, Document document, String displayContext) {
        String downloadUrl = null;

        PropertyMap attachedFileProperties = document.getProperties().getMap("file:content");
        if ((attachedFileProperties != null) && !attachedFileProperties.isEmpty()) {

            // Nuxeo controller
            NuxeoController nuxeoCtl = new NuxeoController(cmsContext.getRequest(), cmsContext.getResponse(), cmsContext.getPortletCtx());
            nuxeoCtl.setCurrentDoc(document);

            // Download URL according to context
            if ("download".equals(displayContext)) {
                downloadUrl = nuxeoCtl.createFileLink(document, "file:content");
            } else if ("downloadVersion".equals(displayContext)) {
                downloadUrl = nuxeoCtl.createFileLinkOfVersion(document, "file:content");
            }

        }

        return downloadUrl;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void formatContentMenuBar(CMSServiceCtx ctx) throws Exception {
        CMSPublicationInfos publicationInfos = null;
        ExtendedDocumentInfos extendedDocumentInfos = null;
        if (ctx.getDoc() != null) {
            Document doc = (Document) ctx.getDoc();
            publicationInfos = this.cmsService.getPublicationInfos(ctx, doc.getPath());
            if (ContextualizationHelper.isCurrentDocContextualized(ctx) || WindowState.MAXIMIZED.equals(ctx.getRequest().getWindowState())) {
                extendedDocumentInfos = this.cmsService.getExtendedDocumentInfos(ctx, doc.getPath());
            }
        }

        this.menubarFormater.formatContentMenuBar(ctx, publicationInfos, extendedDocumentInfos);
    }


    /**
     * Compute preloading pages when user log in.
     *
     * @param cmsCtx CMS context
     * @return preloaded pages
     * @throws Exception
     */
    public List<CMSPage> computeUserPreloadedPages(CMSServiceCtx cmsCtx) throws Exception {
        return this.userPagesLoader.computeUserPreloadedPages(cmsCtx);
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

        } else if ("2".equals(ctx.getDisplayLiveVersion())) {
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

            // Pour prévenir des window en Timeout.
            if (portalObjectContainer != null) {
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
                    // User domains
                    List<?> domains = (List<?>) invocation.getAttribute(Scope.SESSION_SCOPE, InternalConstants.USER_DOMAINS_ATTRIBUTE);

                    // CMS paths
                    List<String> paths;
                    if (CollectionUtils.isEmpty(domains)) {
                        if (po == null) {
                            paths = null;
                        } else {
                            Collection<PortalObject> children = po.getChildren(PortalObject.PAGE_MASK);
                            paths = new ArrayList<>(children.size());
                            for (PortalObject child : children) {
                                String path = child.getDeclaredProperty("osivia.cms.basePath");
                                if (StringUtils.isNotBlank(path)) {
                                    paths.add(path);
                                }
                            }
                        }
                    } else {
                        paths = new ArrayList<>(domains.size());
                        for (Object domain : domains) {
                            if (domain instanceof String) {
                                String path = (String) domain;
                                paths.add(path);
                            }
                        }
                    }

                    if (CollectionUtils.isNotEmpty(paths)) {
                        // Path filter builder
                        StringBuilder builder = new StringBuilder();
                        builder.append(" AND (");
                        
                        boolean first = true;
                        for (String path : paths) {
                            if (first) {
                                first = false;
                            } else {
                                builder.append(" OR ");
                            }

                            builder.append("ecm:path STARTSWITH '").append(path).append("'");
                        }

                        builder.append(") ");

                        requestFilter += builder.toString();
                    }
                }

                String extraFilter = this.getExtraRequestFilter(ctx, requestFilteringPolicy);
                if (extraFilter != null) {
                    requestFilter += " OR (" + extraFilter + ")";
                }
            }
        }

        // Insertion du filtre avant le order
        String beforeOrderBy = "";
        String orderBy = "";

        String editedNuxeoRequest = nuxeoRequest;
        try {
            Pattern ressourceExp = QUERY_FILTER_PATTERN;

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
        StringBuilder filter = new StringBuilder();
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
        Thread.currentThread().setContextClassLoader(this.classLoader);

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

        String nxPublicDomainUri = NuxeoConnectionProperties.getPublicDomainUri().toString();

        // #1421 - If not specified, use current request url insteaod of nuxeo.url
        if (StringUtils.isBlank(nxPublicDomainUri) && ctx != null && ctx.getRequest() != null) {
            nxPublicDomainUri = ctx.getRequest().getScheme() + "://" + ctx.getRequest().getServerName();
        }

        return xslFunctions.link(nxPublicDomainUri + link);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Link getLinkFromNuxeoURL(CMSServiceCtx cmsContext, String url) {
        return getLinkFromNuxeoURL(cmsContext, url, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Link getLinkFromNuxeoURL(CMSServiceCtx cmsContext, String url, String displayContext) {
        // Link
        Link link;

        // Portal controller context
        PortalControllerContext portalControllerContext = new PortalControllerContext(cmsContext.getControllerContext());

        // Current page path
        String currentPagePath = null;
        if (cmsContext.getRequest() != null) {
            Window window = (Window) cmsContext.getRequest().getAttribute("osivia.window");
            if (window != null) {
                Page page = window.getPage();
                if (page != null) {
                    currentPagePath = page.getId().toString(PortalObjectPath.CANONICAL_FORMAT);
                }
            }
        }

        if (StringUtils.startsWith(url, "/nuxeo/")) {
            // Nuxeo URL
            String nuxeoURL = StringUtils.substringBefore(url, "#");
            // Anchor
            String anchor = StringUtils.substringAfter(url, "#");

            // CMS path
            String cmsPath = this.transformNuxeoURL(cmsContext, nuxeoURL);

            // Portal URL
            String portalURL = this.portalUrlFactory.getCMSUrl(portalControllerContext, currentPagePath, cmsPath, null, null, displayContext, null, null, null,
                    null);
            if (StringUtils.isNotBlank(anchor)) {
                portalURL += "#" + anchor;
            }

            link = new Link(portalURL, false);
        } else if (StringUtils.startsWith(url, "/")) {
            // Relative URL
            String relativeUrl = StringUtils.substringBefore(url, "#");
            // Anchor
            String anchor = StringUtils.substringAfter(url, "#");

            // Controller context
            ControllerContext controllerContext = ControllerContextAdapter.getControllerContext(portalControllerContext);
            // Server context
            ServerInvocationContext serverContext = controllerContext.getServerInvocation().getServerContext();
            // Context path
            String contextPath = serverContext.getPortalContextPath();

            if (StringUtils.startsWith(relativeUrl, StringUtils.removeEnd(contextPath, "/auth") + "/")) {
                // Portal relative URL

                // Portal URL
                String portalUrl = this.portalUrlFactory.adaptPortalUrlToNavigation(portalControllerContext, relativeUrl);
                if (StringUtils.isNotBlank(anchor)) {
                    portalUrl += "#" + anchor;
                }

                link = new Link(portalUrl, false);
            } else {
                // Other relative URL
                link = new Link(url, true);
            }
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

            cmsPath = this.webIdService.webIdToCmsPath(webId);
        } else if (StringUtils.startsWith(url, "/nuxeo/nxpath/")) {
            // Nuxeo CMS path
            cmsPath = StringUtils.removeStart(url, "/nuxeo/nxpath/");
            // Remove repository
            cmsPath = "/" + StringUtils.substringAfter(cmsPath, "/");
        }

        if (cmsPath == null) {
            // Invalid Nuxeo URL
            this.log.warn("Invalid Nuxeo URL: '" + url + "'.");
        }

        return cmsPath;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, DocumentType> getCMSItemTypes() {
        return this.getDocumentTypes();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, DocumentType> getDocumentTypes() {
        return this.pluginManager.customizeCMSItemTypes();
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
        DocumentType workspace = DocumentType.createRoot("Workspace");
        workspace.addSubtypes("Folder", "Note", "Room");
        workspace.setIcon("glyphicons glyphicons-wallet");
        workspace.setPreventedCreation(true);
        workspace.setTemplate("/default/templates/workspace");
        defaultTypes.add(workspace);

        // Portal site
        DocumentType portalSite = DocumentType.createRoot("PortalSite");
        portalSite.addSubtypes("File", "PortalPage", "ContextualLink", "Note");
        portalSite.setIcon("glyphicons glyphicons-global");
        portalSite.setEditable(true);
        defaultTypes.add(portalSite);

        // Portal page
        DocumentType portalPage = DocumentType.createNode("PortalPage");
        portalPage.addSubtypes("File", "PortalPage", "ContextualLink", "Note");
        portalPage.setIcon("glyphicons glyphicons-more-items");
        portalPage.setEditable(true);
        portalPage.setMovable(true);
        portalPage.setOrdered(true);
        portalPage.setForceContextualization(true);
        defaultTypes.add(portalPage);

        // Folder
        DocumentType folder = DocumentType.createNode("Folder");
        folder.addSubtypes("File", "Folder", "Note");
        folder.setIcon("glyphicons glyphicons-folder-closed");
        folder.setEditable(true);
        folder.setMovable(true);
        defaultTypes.add(folder);

        // Ordered folder
        DocumentType orderedFolder = DocumentType.createNode("OrderedFolder");
        orderedFolder.addSubtypes("File", "Folder", "Note");
        orderedFolder.setIcon("glyphicons glyphicons-folder-closed");
        orderedFolder.setEditable(true);
        orderedFolder.setMovable(true);
        orderedFolder.setOrdered(true);
        defaultTypes.add(orderedFolder);

        // File
        DocumentType file = DocumentType.createFile("File");
        file.setIcon("glyphicons glyphicons-file");
        file.setEditable(true);
        file.setMovable(true);
        defaultTypes.add(file);

        // Note
        DocumentType note = DocumentType.createLeaf("Note");
        note.setIcon("glyphicons glyphicons-note");
        note.setEditable(true);
        note.setMovable(true);
        defaultTypes.add(note);

        // Contextual link
        DocumentType contextualLink = DocumentType.createLeaf("ContextualLink") ;
        contextualLink.setIcon("glyphicons glyphicons-link");
        contextualLink.setEditable(true);
        contextualLink.setMovable(true);
        defaultTypes.add(contextualLink);

        // Room
        DocumentType room = DocumentType.createNode("Room");
        room.addSubtypes("Folder", "Note", "Room");
        room.setIcon("glyphicons glyphicons-cube-black");
        room.setOrdered(true);
        room.setPreventedCreation(true);
        room.setTemplate("/default/templates/room");
        defaultTypes.add(room);

        // Staple
        DocumentType staple = DocumentType.createLeaf("Staple");
        staple.setIcon("glyphicons glyphicons-nails");
        staple.setNavigable(true);
        defaultTypes.add(staple);

        return defaultTypes;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<FileDocumentType> getFileDocumentTypes() {
        List<FileDocumentType> types = new ArrayList<>();

        // Archive
        FileDocumentType archive = new FileDocumentType("archive", "application", "zip", "gzip");
        archive.setIcon("flaticon flaticon-archive");
        types.add(archive);

        // Audio
        FileDocumentType audio = new FileDocumentType("audio", "audio");
        audio.setIcon("glyphicons glyphicons-music");
        types.add(audio);

        // Excel
        FileDocumentType excel = new FileDocumentType("excel", "application", "vnd.ms-excel", "vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "vnd.ms-excel.sheet.macroenabled.12");
        excel.setIcon("flaticon flaticon-excel");
        types.add(excel);

        // Image
        FileDocumentType image = new FileDocumentType("image", "image");
        image.setIcon("glyphicons glyphicons-picture");
        types.add(image);

        // OpenDocument - Presentation
        FileDocumentType odp = new FileDocumentType("odp", "application", "vnd.oasis.opendocument.presentation");
        odp.setIcon("flaticon flaticon-odp");
        types.add(odp);

        // OpenDocument - Spread sheet
        FileDocumentType ods = new FileDocumentType("ods", "application", "vnd.oasis.opendocument.spreadsheet");
        ods.setIcon("flaticon flaticon-ods");
        types.add(ods);

        // OpenDocument - Text
        FileDocumentType odt = new FileDocumentType("odt", "application", "vnd.oasis.opendocument.text");
        odt.setIcon("flaticon flaticon-odt");
        types.add(odt);

        // PDF
        FileDocumentType pdf = new FileDocumentType("pdf", "application", "pdf");
        pdf.setIcon("flaticon flaticon-pdf");
        types.add(pdf);

        // Powerpoint
        FileDocumentType powerpoint = new FileDocumentType("powerpoint", "application", "vnd.ms-powerpoint",
                "vnd.openxmlformats-officedocument.presentationml.presentation");
        powerpoint.setIcon("flaticon flaticon-powerpoint");
        types.add(powerpoint);

        // Text
        FileDocumentType text = new FileDocumentType("text", "text");
        text.setIcon("flaticon flaticon-text");
        types.add(text);

        // Video
        FileDocumentType video = new FileDocumentType("video", "video");
        video.setIcon("glyphicons glyphicons-film");
        types.add(video);

        // Word
        FileDocumentType word = new FileDocumentType("word", "application", "msword", "vnd.openxmlformats-officedocument.wordprocessingml.document");
        word.setIcon("flaticon flaticon-word");
        types.add(word);

        // XML
        FileDocumentType xml = new FileDocumentType("xml", "text", "html", "xml");
        xml.setIcon("flaticon flaticon-xml");
        types.add(xml);

        return types;
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
     * {@inheritDoc}
     */
    @Override
    public String getContentWebIdPath(CMSServiceCtx cmsCtx) {
        return getContentWebIdPath(cmsCtx, null, null);
    }

    /**
     * Gets webId according to context.
     * 
     * @param cmsCtx
     * @param pubInfos
     * @return webId
     */
    public String getContentWebIdPath(CMSServiceCtx cmsCtx, CMSPublicationInfos pubInfos, ExtendedDocumentInfos extendedInfos) {
        Document doc = (Document) cmsCtx.getDoc();

        // CMS path
        String permLinkPath = ((Document) (cmsCtx.getDoc())).getPath();

        String webId = DocumentHelper.getWebId(doc);

        // Switch on CMS path indicator
        boolean switchPath = false;
        if (StringUtils.isNotBlank(webId)) {

            // Not List case where we always use CMS path
            if (pubInfos != null) {
                // Case of permlink of remote proxy
                if (DocumentHelper.isRemoteProxy(cmsCtx, pubInfos) && StringUtils.equals("permLinkCtx", cmsCtx.getDisplayContext())) {
                    // Document must be contextualized
                    if (extendedInfos != null) {
                        webId = webId.concat(IWebIdService.RPXY_WID_MARKER).concat(extendedInfos.getParentWebId());
                    } else {
                        // Switch on CMS path
                        switchPath = true;
                    }
                }

                if (!switchPath) {
                    permLinkPath = getWebIdService().webIdToCmsPath(webId);
                }
            }
        }

        return permLinkPath;
    }


    /**
     * {@inheritDoc}
     */
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
            this.log.error(e);
        }

        return new Link(src, false);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Link getUserAvatar(CMSServiceCtx cmsCtx, String username) throws CMSException {
        return this.getUserAvatar(username);

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String refreshUserAvatar(String username) {

        // renew the timestamp and map it to the user
        String avatarTime = Long.toString(new Date().getTime());

        this.avatarMap.put(username, avatarTime);

        return avatarTime;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String refreshUserAvatar(CMSServiceCtx cmsCtx, String username) {
        return this.refreshUserAvatar(username);
    }


    /**
     * Checks if current doc is in edition state.
     *
     * @return true, if is in page edition state
     * @throws CMSException the CMS exception
     */
    public boolean isPathInLiveState(CMSServiceCtx cmsCtx, Document doc) {
        if ("1".equals(cmsCtx.getDisplayLiveVersion())) {
            return true;
        }

        return doc.getPath().equals(cmsCtx.getForcedLivePath());
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
        Map<String, BinaryDelegation> delegationMap = this.delegations.get(id);
        if (delegationMap != null) {
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
        // Nuxeo document
        Document document;
        if (binary.getDocument() == null) {
            document = null;
        } else {
            document = (Document) binary.getDocument();
        }

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


            if (document != null) {
                path = document.getPath();

                // Case of version: path is not enough, must set uuid
                if (BinaryDescription.Type.FILE_OF_VERSION.equals(binary.getType())) {
                    path = document.getId();
                }

                liveState = this.isPathInLiveState(cmsCtx, document);
                delegation.setGrantedAccess(true);
            } else {
                path = binary.getPath();
            }

            Subject subject = (Subject) PolicyContext.getContext("javax.security.auth.Subject.container");
            delegation.setSubject(subject);

            delegation.setAdmin(isAdmin);

            if (StringUtils.endsWith(path, ".proxy") && !StringUtils.endsWith(path, ".remote.proxy")) {
                path = StringUtils.removeEnd(path, ".proxy");
            }

            delegation.setUserName(cmsCtx.getServletRequest().getRemoteUser());

            Map<String, BinaryDelegation> delegationMap = this.getUserDelegation(cmsCtx);
            delegationMap.put(path, delegation);


            // File name
            String fileName = binary.getFileName();
            if (fileName == null) {
                if ((document != null) && (binary.getIndex() == null)) {
                    PropertyMap fileMap = document.getProperties().getMap("file:content");
                    if (fileMap != null) {
                        fileName = fileMap.getString("name");
                    }
                } else if ((binary.getIndex() != null) && (cmsCtx.getDoc() != null)) {
                    int index = NumberUtils.toInt(binary.getIndex());
                    Document contextDocument = (Document) cmsCtx.getDoc();
                    PropertyList files = contextDocument.getProperties().getList("files:files");
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
                sb.append("/").append(URLEncoder.encode(fileName, "UTF-8"));
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
            if (cmsCtx.getScope() != null) {
                sb.append("&scope=").append(cmsCtx.getScope());
            }
            if (cmsCtx.getForcePublicationInfosScope() != null) {
                sb.append("&fscope=").append(cmsCtx.getForcePublicationInfosScope());
            }


            // Portal timestamp
            long timestamp = System.currentTimeMillis();
            // Document modified timestamp
            Long modified;
            if (document == null) {
                modified = null;
            } else {
                Date date = document.getDate("dc:modified");
                if (date == null) {
                    modified = null;
                } else {
                    modified = date.getTime();
                }
            }
            // Refreshing page indicator
            boolean refreshingPage = PageProperties.getProperties().isRefreshingPage();
            // Reloading required indicator
            boolean reload;
            // Binary timestamp
            BinaryTimestamp binaryTimestamp = this.binaryTimestamps.get(path);
            if (binaryTimestamp == null) {
                binaryTimestamp = new BinaryTimestamp();
                binaryTimestamp.setTimestamp(timestamp);
                binaryTimestamp.setModified(modified);
                binaryTimestamp.setReloadingRequired(true);
                this.binaryTimestamps.put(path, binaryTimestamp);
            }


            if ("pdf:content".equals(binary.getFieldName()) && refreshingPage) {
                // Force reloading for PDF preview
                reload = true;
            } else if (binaryTimestamp.isReloadingRequired()) {
                // Reload required
                reload = true;

                if (timestamp - binaryTimestamp.getTimestamp() > TimeUnit.SECONDS.toMillis(10)) {
                    // Don't reload the next time
                    binaryTimestamp.setReloadingRequired(false);
                }
            } else if (modified == null) {
                // Considered up-to-date
                reload = false;

                if (refreshingPage && BinaryDescription.Type.PICTURE.equals(binary.getType())) {
                    // In case of refresh, invalidate navigator cache
                    binaryTimestamp.setTimestamp(timestamp);
                }
            } else if (binaryTimestamp.getModified() == null) {
                // Unknown "dc:modified"
                binaryTimestamp.setTimestamp(timestamp);
                binaryTimestamp.setModified(modified);
                binaryTimestamp.setReloadingRequired(true);
                reload = true;
            } else if (Math.abs(binaryTimestamp.getModified() - modified) > TimeUnit.SECONDS.toMillis(1)) {
                // Updated "dc:modified"
                binaryTimestamp.setTimestamp(timestamp);
                binaryTimestamp.setModified(modified);
                binaryTimestamp.setReloadingRequired(true);
                reload = true;
            } else {
                // Up-to-date
                reload = false;
            }


            if (reload) {
                sb.append("&t=");
                sb.append(timestamp);
                sb.append("&reload=true");
            } else {
                sb.append("&t=");
                sb.append(TimeUnit.MILLISECONDS.toSeconds(binaryTimestamp.getTimestamp()));
            }

            src = sb.toString();
        } catch (Exception e) {
            throw new CMSException(e);
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
        String id = cmsCtx.getServletRequest().getSession(true).getId();
        Map<String, BinaryDelegation> delegationMap = this.delegations.get(id);
        if (delegationMap == null) {
            delegationMap = new ConcurrentHashMap<String, BinaryDelegation>();
            this.delegations.put(id, delegationMap);
        }
        return delegationMap;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent sessionEvent) {
        this.delegations.remove(sessionEvent.getSession().getId());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void sessionCreated(HttpSessionEvent se) {

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

        commands.put(EcmCommonCommands.lock.name(), new LockCommand(this.notificationsService, this.internationalizationService));
        commands.put(EcmCommonCommands.unlock.name(), new UnlockCommand(this.notificationsService, this.internationalizationService));

        commands.put(EcmCommonCommands.subscribe.name(), new SubscribeCommand(this.notificationsService, this.internationalizationService));
        commands.put(EcmCommonCommands.unsubscribe.name(), new UnsubscribeCommand(this.notificationsService, this.internationalizationService));

        commands.put(EcmCommonCommands.synchronizeFolder.name(), new SynchronizeCommand(this.notificationsService, this.internationalizationService));
        commands.put(EcmCommonCommands.unsynchronizeFolder.name(), new UnsynchronizeCommand(this.notificationsService, this.internationalizationService));

        commands.put(EcmCommonCommands.eraseModifications.name(), new EraseModificationsCommand(this.notificationsService, this.internationalizationService));

        commands.put(EcmCommonCommands.deleteDocument.name(), new DeleteDocumentCommand(this.notificationsService, this.internationalizationService));

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
        TaskbarItem search = factory.createStapledTaskbarItem(ITaskbarService.SEARCH_TASK_ID, "SEARCH_TASK", "glyphicons glyphicons-search",
                "/default/templates/workspace/search");
        factory.hide(search, true);
        taskbarItems.add(search);

        // Documents
        TaskbarItem documents = factory.createCmsTaskbarItem("DOCUMENTS", "DOCUMENTS_TASK", "glyphicons glyphicons-folder-closed", "Folder");
        factory.preset(documents, true, 1);
        taskbarItems.add(documents);

        return taskbarItems;
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
            CustomizedJsp customizedPage = this.pluginManager.customizeJSP(name, portletContext, request);

            String customizedName;
            if (customizedPage != null) {
                customizedName = customizedPage.getName();
            } else {
                customizedName = null;
            }
            return customizedName;
        } catch (IOException e) {
            throw new CMSException(e);
        }
    }


    /**
     * Getter for portletContext.
     * 
     * @return the portletContext
     */
    public PortletContext getPortletContext() {
        return portletContext;
    }

    /**
     * Getter for cmsService.
     * 
     * @return the cmsService
     */
    public CMSService getCmsService() {
        return cmsService;
    }

    /**
     * Getter for delegations.
     * 
     * @return the delegations
     */
    public Map<String, Map<String, BinaryDelegation>> getDelegations() {
        return delegations;
    }

    /**
     * Getter for players.
     * 
     * @return the players
     */
    public Map<String, INuxeoPlayerModule> getPlayers() {
        return players;
    }

    /**
     * Getter for navigationPanelPlayers.
     * 
     * @return the navigationPanelPlayers
     */
    public Map<String, PanelPlayer> getNavigationPanelPlayers() {
        return navigationPanelPlayers;
    }

    /**
     * Getter for avatarMap.
     * 
     * @return the avatarMap
     */
    public Map<String, String> getAvatarMap() {
        return avatarMap;
    }

    /**
     * Getter for pluginManager.
     * 
     * @return the pluginManager
     */
    public CustomizationPluginMgr getPluginManager() {
        return pluginManager;
    }

    /**
     * Getter for nuxeoConnection.
     * 
     * @return the nuxeoConnection
     */
    public NuxeoConnectionProperties getNuxeoConnection() {
        return nuxeoConnection;
    }

    /**
     * Getter for userPagesLoader.
     * 
     * @return the userPagesLoader
     */
    public UserPagesLoader getUserPagesLoader() {
        return userPagesLoader;
    }

    /**
     * Getter for menubarFormater.
     * 
     * @return the menubarFormater
     */
    public MenuBarFormater getMenubarFormater() {
        return menubarFormater;
    }

    /**
     * Getter for browserAdapter.
     * 
     * @return the browserAdapter
     */
    public BrowserAdapter getBrowserAdapter() {
        return browserAdapter;
    }

    /**
     * Getter for navigationItemAdapter.
     * 
     * @return the navigationItemAdapter
     */
    public NavigationItemAdapter getNavigationItemAdapter() {
        return navigationItemAdapter;
    }

    /**
     * Getter for classLoader.
     * 
     * @return the classLoader
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Getter for parser.
     * 
     * @return the parser
     */
    public XMLReader getParser() {
        return parser;
    }

    /**
     * Getter for portalUrlFactory.
     * 
     * @return the portalUrlFactory
     */
    public IPortalUrlFactory getPortalUrlFactory() {
        return portalUrlFactory;
    }

    /**
     * Getter for webIdService.
     * 
     * @return the webIdService
     */
    public IWebIdService getWebIdService() {
        return webIdService;
    }

    /**
     * Getter for webUrlService.
     * 
     * @return the webUrlService
     */
    public IWebUrlService getWebUrlService() {
        return webUrlService;
    }

    /**
     * Getter for taskbarService.
     * 
     * @return the taskbarService
     */
    public ITaskbarService getTaskbarService() {
        return taskbarService;
    }

    /**
     * Getter for customizationService.
     * 
     * @return the customizationService
     */
    public ICustomizationService getCustomizationService() {
        return customizationService;
    }

    /**
     * Getter for nuxeoCommentsService.
     * 
     * @return the nuxeoCommentsService
     */
    public INuxeoCommentsService getNuxeoCommentsService() {
        return nuxeoCommentsService;
    }

    /**
     * Getter for internationalizationService.
     * 
     * @return the internationalizationService
     */
    public IInternationalizationService getInternationalizationService() {
        return internationalizationService;
    }

    /**
     * Getter for bundleFactory.
     * 
     * @return the bundleFactory
     */
    public IBundleFactory getBundleFactory() {
        return bundleFactory;
    }

    /**
     * Getter for notificationsService.
     * 
     * @return the notificationsService
     */
    public INotificationsService getNotificationsService() {
        return notificationsService;
    }

}
