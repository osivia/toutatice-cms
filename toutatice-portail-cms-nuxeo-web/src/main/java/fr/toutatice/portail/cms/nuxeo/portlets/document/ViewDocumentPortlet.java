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
package fr.toutatice.portail.cms.nuxeo.portlets.document;

import fr.toutatice.portail.cms.nuxeo.api.*;
import fr.toutatice.portail.cms.nuxeo.api.cms.NuxeoDocumentContext;
import fr.toutatice.portail.cms.nuxeo.api.comments.GetCommentsCommand;
import fr.toutatice.portail.cms.nuxeo.api.domain.CommentDTO;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.api.domain.RemotePublishedDocumentDTO;
import fr.toutatice.portail.cms.nuxeo.api.liveedit.OnlyofficeLiveEditHelper;
import fr.toutatice.portail.cms.nuxeo.api.portlet.IPortletModule;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCustomizer;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoService;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoServiceInvocationHandler;
import fr.toutatice.portail.cms.nuxeo.api.services.dao.CommentDAO;
import fr.toutatice.portail.cms.nuxeo.api.services.dao.DocumentDAO;
import fr.toutatice.portail.cms.nuxeo.api.services.dao.RemotePublishedDocumentDAO;
import fr.toutatice.portail.cms.nuxeo.api.services.tag.INuxeoTagService;
import fr.toutatice.portail.cms.nuxeo.portlets.avatar.AvatarServlet;
import fr.toutatice.portail.cms.nuxeo.portlets.binaries.BinaryServlet;
import fr.toutatice.portail.cms.nuxeo.portlets.cms.ExtendedDocumentInfos;
import fr.toutatice.portail.cms.nuxeo.portlets.commands.CommandConstants;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.CMSCustomizer;
import fr.toutatice.portail.cms.nuxeo.portlets.document.helpers.DocumentConstants;
import fr.toutatice.portail.cms.nuxeo.portlets.forms.FormsServiceImpl;
import fr.toutatice.portail.cms.nuxeo.portlets.service.CMSService;
import fr.toutatice.portail.cms.nuxeo.portlets.site.SitePictureServlet;
import fr.toutatice.portail.cms.nuxeo.portlets.thumbnail.ThumbnailServlet;
import fr.toutatice.portail.cms.nuxeo.service.tag.NuxeoTagService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.portal.core.model.portal.Portal;
import org.jboss.portal.theme.ThemeConstants;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.cache.services.CacheInfo;
import org.osivia.portal.api.cms.DocumentType;
import org.osivia.portal.api.cms.UpdateScope;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.directory.v2.DirServiceFactory;
import org.osivia.portal.api.directory.v2.model.Person;
import org.osivia.portal.api.directory.v2.service.PersonService;
import org.osivia.portal.api.internationalization.Bundle;
import org.osivia.portal.api.internationalization.IBundleFactory;
import org.osivia.portal.api.internationalization.IInternationalizationService;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.notifications.INotificationsService;
import org.osivia.portal.api.notifications.NotificationsType;
import org.osivia.portal.api.portalobject.bridge.PortalObjectUtils;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;
import org.osivia.portal.core.cms.*;
import org.osivia.portal.core.constants.InternalConstants;
import org.osivia.portal.core.utils.URLUtils;

import javax.portlet.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.security.Principal;
import java.util.*;


/**
 * View Nuxeo document portlet.
 *
 * @see CMSPortlet
 */
public class ViewDocumentPortlet extends CMSPortlet {

    /**
     * Path window property name.
     */
    public static final String PATH_WINDOW_PROPERTY = Constants.WINDOW_PROP_URI;
    /**
     * Display only description indicator window property name.
     */
    public static final String ONLY_DESCRIPTION_WINDOW_PROPERTY = "osivia.document.onlyDescription";
    /**
     * Display only remote sections indicator window property name.
     */
    public static final String ONLY_REMOTE_SECTIONS_WINDOW_PROPERTY = "osivia.document.onlyRemoteSections";
    /**
     * Indicates if portlet is in remote sections list page.
     */
    public static final String REMOTE_SECTIONS_PAGE_WINDOW_PROPERTY = "osivia.document.remoteSectionsPage";
    /**
     * Hide metadata indicator window property name.
     */
    public static final String HIDE_METADATA_WINDOW_PROPERTY = InternalConstants.METADATA_WINDOW_PROPERTY;
    /**
     * Hide attachment indicator window property name.
     */
    public static final String HIDE_ATTACHMENTS_WINDOW_PROPERTY = "osivia.document.hideAttachments";
    /**
     * Host joker.
     */
    private static final String HOST_JOKER = "__HOST__";
    /**
     * Admin path.
     */
    private static final String PATH_ADMIN = "/WEB-INF/jsp/document/admin.jsp";
    /**
     * View path.
     */
    private static final String PATH_VIEW = "/WEB-INF/jsp/document/view.jsp";
    /**
     * Internationalization bundle factory.
     */
    private final IBundleFactory bundleFactory;
    /**
     * Notifications service.
     */
    private final INotificationsService notificationsService;
    /**
     * Document DAO.
     */
    private final DocumentDAO documentDao;
    /**
     * Document comment DAO.
     */
    private  CommentDAO commentDao;
    /**
     * Document published documents DAO.
     */
    private final RemotePublishedDocumentDAO publishedDocumentsDao;
    /**
     * CMS service.
     */
    private CMSService cmsService;
    /**
     * Nuxeo service.
     */
    private INuxeoService nuxeoService;



    
    /**
     * Constructor.
     */
    public ViewDocumentPortlet() {
        super();

        // Internationalization bundle factory
        IInternationalizationService internationalizationService = Locator.getService(IInternationalizationService.class);
        this.bundleFactory = internationalizationService.getBundleFactory(this.getClass().getClassLoader());
        // Notifications service
        this.notificationsService = Locator.getService(INotificationsService.class);

        // DAO
        this.documentDao = DocumentDAO.getInstance();
        
        // TODO refonte
        //this.commentDao = CommentDAO.getInstance();
        this.publishedDocumentsDao = RemotePublishedDocumentDAO.getInstance();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void init(PortletConfig config) throws PortletException {
        super.init(config);

        // Portlet context
        PortletContext portletContext = this.getPortletContext();

        try {
          
            // Nuxeo service
            this.nuxeoService = (INuxeoService) portletContext.getAttribute("NuxeoService");
            if (this.nuxeoService == null) {
                
                this.nuxeoService = Locator.findMBean(INuxeoService.class, "osivia:service=NuxeoService");
                if (this.nuxeoService == null) {
                
                throw new PortletException("Cannot start ViewDocumentPortlet portlet due to service unavailability");
                }
            }

            // CMS service
            this.cmsService = new CMSService(portletContext);
            ICMSServiceLocator cmsLocator = Locator.findMBean(ICMSServiceLocator.class, "osivia:service=CmsServiceLocator");
            cmsLocator.register(this.cmsService);

            // CMS customizer
            CMSCustomizer customizer = new CMSCustomizer(portletContext, this.cmsService);
            this.cmsService.setCustomizer(customizer);
            this.nuxeoService.registerCMSCustomizer(customizer);

            // Nuxeo tag service

            INuxeoTagService tagService = new NuxeoTagService();
            this.registerService(this.nuxeoService.getTagService(), tagService);

            // Forms service
            FormsServiceImpl formsService = new FormsServiceImpl(customizer);
            this.registerService(this.nuxeoService.getFormsService(), formsService);


            // v1.0.16
            ThumbnailServlet.setPortletContext(portletContext);
            SitePictureServlet.setPortletContext(portletContext);
            BinaryServlet.setPortletContext(portletContext);
        }
         catch (Exception e) {
            throw new PortletException(e);
        }
    }


    /**
     * Register service.
     *
     * @param proxy    proxy
     * @param instance instance
     */
    private void registerService(Object proxy, Object instance) {
        NuxeoServiceInvocationHandler invocationHandler = (NuxeoServiceInvocationHandler) Proxy.getInvocationHandler(proxy);
        invocationHandler.setInstance(instance);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void processAction(ActionRequest request, ActionResponse response) throws IOException, PortletException {
        
        /*
        if(true)
            throw new NullPointerException();
            */
        
        // Portal controller context
        PortalControllerContext portalControllerContext = new PortalControllerContext(this.getPortletContext(), request, response);
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(portalControllerContext);

        // Current window
        PortalWindow window = WindowFactory.getWindow(request);
        // Action name
        String action = request.getParameter(ActionRequest.ACTION_NAME);

        if ("admin".equals(request.getPortletMode().toString())) {
            // Admin

            if ("save".equals(action)) {
                // Save action

                // Path
                String path = request.getParameter("path");
                window.setProperty(PATH_WINDOW_PROPERTY, path);

                // Display only description indicator
                String onlyDescription = request.getParameter("onlyDescription");
                window.setProperty(ONLY_DESCRIPTION_WINDOW_PROPERTY, onlyDescription);

                // Hide metadata indicator
                boolean displayMetadata = BooleanUtils.toBoolean(request.getParameter("metadata"));
                window.setProperty(HIDE_METADATA_WINDOW_PROPERTY, BooleanUtils.toString(displayMetadata, null, "1"));

                // Hide attachments indicator
                boolean displayAttachments = BooleanUtils.toBoolean(request.getParameter("attachments"));
                window.setProperty(HIDE_ATTACHMENTS_WINDOW_PROPERTY, BooleanUtils.toString(displayAttachments, null, "1"));
            }

            response.setPortletMode(PortletMode.VIEW);
            response.setWindowState(WindowState.NORMAL);
        } else if (PortletMode.VIEW.equals(request.getPortletMode())) {
            // View

            // Comment action
            this.processCommentAction(request, response);

            // Inline edition action
            this.processInlineEditionAction(request, response);

            // Cancel inline edition action
            this.processCancelInlineEditionAction(request, response);

            // Cancel inline edition action
            this.processLinkActivationAction(request, response);
        }


        // Document modules
        List<IPortletModule> modules = this.getModules(nuxeoController);
        if (CollectionUtils.isNotEmpty(modules)) {
            for (IPortletModule module : modules) {
                module.processAction(portalControllerContext);
            }
        }
    }

    /**
     * Process link activation action.
     *
     * @param request  action request
     * @param response action response
     * @throws PortletException
     */

    private void processLinkActivationAction(ActionRequest request, ActionResponse response) {
        // Action name
        String action = request.getParameter(ActionRequest.ACTION_NAME);
        if ("link-activation".equals(action)) {
            boolean activation = BooleanUtils.toBoolean(request.getParameter("activate"));

            // Portal controller context
            PortalControllerContext portalControllerContext = new PortalControllerContext(this.getPortletContext(), request, response);
            // Nuxeo controller
            NuxeoController nuxeoController = new NuxeoController(portalControllerContext);

            // Internationalization bundle
            Bundle bundle = this.bundleFactory.getBundle(request.getLocale());

            // Current window
            PortalWindow window = WindowFactory.getWindow(request);
            // Current document path
            String path = nuxeoController.getComputedPath(window.getProperty(PATH_WINDOW_PROPERTY));

            // Nuxeo document context
            NuxeoDocumentContext documentContext = nuxeoController.getDocumentContext(path);
            // Nuxeo document
            Document document = documentContext.getDocument();


            // Nuxeo command
            INuxeoCommand command = new ShareLinkActivationCommand(document, activation);
            nuxeoController.executeNuxeoCommand(command);

            // Reload document
            documentContext.reload();


            // Notification
            StringBuilder message = new StringBuilder();
            if (activation)
                message.append(bundle.getString("DOCUMENT_SHARE_ACTIVATION_SUCCESS"));
            else
                message.append(bundle.getString("DOCUMENT_SHARE_DEACTIVATION_SUCCESS"));


            NotificationsType notificationType = NotificationsType.SUCCESS;


            this.notificationsService.addSimpleNotification(portalControllerContext, message.toString(), notificationType);


        }

    }


    /**
     * Process inline edition action.
     *
     * @param request  action request
     * @param response action response
     * @throws PortletException
     */
    private void processInlineEditionAction(ActionRequest request, ActionResponse response) throws PortletException {
        // Action name
        String action = request.getParameter(ActionRequest.ACTION_NAME);

        if ("inline-edition".equals(action)) {
            // Inline edition property
            String property = request.getParameter("property");

            if (StringUtils.isNotEmpty(property)) {
                // Portal controller context
                PortalControllerContext portalControllerContext = new PortalControllerContext(this.getPortletContext(), request, response);
                // Nuxeo controller
                NuxeoController nuxeoController = new NuxeoController(portalControllerContext);

                // Internationalization bundle
                Bundle bundle = this.bundleFactory.getBundle(request.getLocale());

                // Current window
                PortalWindow window = WindowFactory.getWindow(request);
                // Current document path
                String path = nuxeoController.getComputedPath(window.getProperty(PATH_WINDOW_PROPERTY));

                // Nuxeo document context
                NuxeoDocumentContext documentContext = nuxeoController.getDocumentContext(path);
                // Nuxeo document
                Document document = documentContext.getDocument();

                // Inline values
                String[] values = request.getParameterValues("inline-values");

                // Cancel URL
                String cancelUrl = request.getParameter("cancel-url");

                // Custom message
                String warnMessage = request.getParameter("warn-message");

                // Save previous state in portlet session
                if (StringUtils.isNotEmpty(cancelUrl)) {
                    PortletSession session = request.getPortletSession();
                    session.setAttribute("inline-edition.property", property);
                    session.setAttribute("inline-edition.previous-values", document.getProperties().get(property));
                }

                // Nuxeo command
                INuxeoCommand command = new InlineEditionCommand(path, property, values);
                nuxeoController.executeNuxeoCommand(command);

                String spacePath = nuxeoController.getSpacePath();
                nuxeoController.notifyUpdate(path, spacePath, UpdateScope.SCOPE_CONTENT,false);

                // Prevent Ajax refresh
                request.setAttribute("osivia.ajax.preventRefresh", true);

                // Notification
                StringBuilder message = new StringBuilder();
                message.append(bundle.getString("DOCUMENT_INLINE_EDITION_SUCCESS"));
                if (StringUtils.isNotEmpty(warnMessage)) {
                    message.append("<br>");
                    message.append(warnMessage);
                }


                if (StringUtils.isNotEmpty(cancelUrl)) {
                    message.append("<br>");
                    message.append(bundle.getString("DOCUMENT_INLINE_EDITION_CANCEL", cancelUrl));
                }

                NotificationsType notificationType;
                if (StringUtils.isNotEmpty(warnMessage))
                    notificationType = NotificationsType.WARNING;
                else
                    notificationType = NotificationsType.SUCCESS;


                this.notificationsService.addSimpleNotification(portalControllerContext, message.toString(), notificationType);
            }
        }
    }


    /**
     * Process cancel inline edition action.
     *
     * @param request  action request
     * @param response action response
     * @throws PortletException
     */
    private void processCancelInlineEditionAction(ActionRequest request, ActionResponse response) throws PortletException {
        // Action name
        String action = request.getParameter(ActionRequest.ACTION_NAME);

        if ("cancel-inline-edition".equals(action)) {
            // Portal controller context
            PortalControllerContext portalControllerContext = new PortalControllerContext(this.getPortletContext(), request, response);
            // Portlet session
            PortletSession session = request.getPortletSession();

            // Internationalization bundle
            Bundle bundle = this.bundleFactory.getBundle(request.getLocale());

            // Inline edition property
            String property = (String) session.getAttribute("inline-edition.property");

            if (StringUtils.isEmpty(property)) {
                // Notification
                String message = bundle.getString("DOCUMENT_INLINE_EDITION_CANCEL_ERROR");
                this.notificationsService.addSimpleNotification(portalControllerContext, message, NotificationsType.ERROR);
            } else {
                // Nuxeo controller
                NuxeoController nuxeoController = new NuxeoController(portalControllerContext);

                // Current window
                PortalWindow window = WindowFactory.getWindow(request);
                // Current document path
                String path = nuxeoController.getComputedPath(window.getProperty(PATH_WINDOW_PROPERTY));

                // Inline edition values
                String[] values;
                Object object = session.getAttribute("inline-edition.previous-values");
                if (object == null) {
                    values = null;
                } else if (object instanceof PropertyList) {
                    PropertyList propertyList = (PropertyList) object;
                    values = new String[propertyList.size()];
                    for (int i = 0; i < propertyList.size(); i++) {
                        values[i] = propertyList.getString(i);
                    }
                } else {
                    String value = String.valueOf(object);
                    values = new String[]{value};
                }

                // Nuxeo command
                INuxeoCommand command = new InlineEditionCommand(path, property, values);
                nuxeoController.executeNuxeoCommand(command);

                String spacePath = nuxeoController.getSpacePath();
                nuxeoController.notifyUpdate(path, spacePath, UpdateScope.SCOPE_CONTENT, false);


                // Notification
                String message = bundle.getString("DOCUMENT_INLINE_EDITION_CANCEL_SUCCESS");
                this.notificationsService.addSimpleNotification(portalControllerContext, message, NotificationsType.INFO);
            }
        }
    }


    /**
     * Admin view display.
     *
     * @param request  request
     * @param response response
     * @throws PortletException
     * @throws IOException
     */
    @RenderMode(name = "admin")
    public void doAdmin(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        // Portal controller context
        PortalControllerContext portalControllerContext = new PortalControllerContext(this.getPortletContext(), request, response);
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(portalControllerContext);

        // Current window
        PortalWindow window = WindowFactory.getWindow(request);

        // Path
        String path = window.getProperty(PATH_WINDOW_PROPERTY);
        request.setAttribute("path", path);

        // Display only description indicator
        boolean onlyDescription = BooleanUtils.toBoolean(window.getProperty(ONLY_DESCRIPTION_WINDOW_PROPERTY));
        request.setAttribute("onlyDescription", onlyDescription);

        // Display metadata indicator
        boolean metadata = BooleanUtils.toBoolean(window.getProperty(HIDE_METADATA_WINDOW_PROPERTY), null, "1");
        request.setAttribute("metadata", metadata);

        // Display attachments indicator
        boolean attachments = BooleanUtils.toBoolean(window.getProperty(HIDE_ATTACHMENTS_WINDOW_PROPERTY), null, "1");
        request.setAttribute("attachments", attachments);


        // Document modules
        List<IPortletModule> modules = this.getModules(nuxeoController);
        if (CollectionUtils.isNotEmpty(modules)) {
            for (IPortletModule module : modules) {
                module.doAdmin(portalControllerContext);
            }
        }


        response.setContentType("text/html");
        this.getPortletContext().getRequestDispatcher(PATH_ADMIN).include(request, response);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        try {
            // Portal controller context
            PortalControllerContext portalControllerContext = new PortalControllerContext(this.getPortletContext(), request, response);
            
            
            // Nuxeo controller
            NuxeoController nuxeoController = new NuxeoController(portalControllerContext);
            // CMS context
            CMSServiceCtx cmsContext = nuxeoController.getCMSCtx();

            // Current window
            PortalWindow window = WindowFactory.getWindow(request);

            // Theme
            String theme = window.getPageProperty(ThemeConstants.PORTAL_PROP_THEME);
            request.setAttribute("theme", theme);

            // Path
            String path = window.getProperty(PATH_WINDOW_PROPERTY);
            // Computed path
            path = nuxeoController.getComputedPath(path);
            // Publication informations
            CMSPublicationInfos publicationInfos = this.cmsService.getPublicationInfos(cmsContext, path);


            if (StringUtils.isNotBlank(path)) {
                // Document context
                NuxeoDocumentContext documentContext = nuxeoController.getDocumentContext(path);

                // Maximized indicator
                boolean maximized = WindowState.MAXIMIZED.equals(request.getWindowState());

                // Display only remote sections indicator
                boolean onlyRemoteSections = BooleanUtils.toBoolean(window.getProperty(ONLY_REMOTE_SECTIONS_WINDOW_PROPERTY));
                request.setAttribute("onlyRemoteSections", onlyRemoteSections);

                // Remote sections page indicator
                boolean remoteSectionsPage = BooleanUtils.toBoolean(window.getProperty(REMOTE_SECTIONS_PAGE_WINDOW_PROPERTY));
                request.setAttribute("remoteSectionsPage", remoteSectionsPage);

                // Display only description indicator
                boolean onlyDescription = BooleanUtils.toBoolean(window.getProperty(ONLY_DESCRIPTION_WINDOW_PROPERTY));
                if (!maximized) {
                    request.setAttribute("onlyDescription", onlyDescription);
                }

                // Display metadata indicator
                boolean metadata = BooleanUtils.toBoolean(window.getProperty(HIDE_METADATA_WINDOW_PROPERTY), null, "1");
                request.setAttribute("metadata", metadata);

                // Display attachments indicator
                boolean attachments = BooleanUtils.toBoolean(window.getProperty(HIDE_ATTACHMENTS_WINDOW_PROPERTY), null, "1");
                request.setAttribute("attachments", attachments);

                // Document
                Document document = documentContext.getDocument();
                nuxeoController.setCurrentDoc(document);

                // View dispatched JSP
                String dispatchJsp = window.getProperty("osivia.document.dispatch.jsp");
                if (StringUtils.isEmpty(dispatchJsp)) {
                    dispatchJsp = this.getDispatchJspName(nuxeoController, document, null);
                }
                request.setAttribute("dispatchJsp", dispatchJsp);

                // View dispatched extra JSP
                String dispatchExtraJsp = this.getDispatchJspName(nuxeoController, document, "extra");
                request.setAttribute("dispatchExtraJsp", dispatchExtraJsp);

                // View dispatched layout JSP
                String dispatchLayoutJsp = this.getDispatchJspName(nuxeoController, document, "layout");
                request.setAttribute("dispatchLayoutJsp", dispatchLayoutJsp);

                // DTO
                DocumentDTO documentDto = this.documentDao.toDTO(portalControllerContext, document);
                request.setAttribute("document", documentDto);

                // Title
                String title = document.getTitle();
                if (StringUtils.isNotBlank(title)) {
                    response.setTitle(documentDto.getDisplayTitle());
                }

                // Extended document informations
                ExtendedDocumentInfos extendedDocumentInfos = this.cmsService.getExtendedDocumentInfos(cmsContext, document.getPath());

                if (extendedDocumentInfos.isCurrentlyEdited() || extendedDocumentInfos.isRecentlyEdited()) {
                    this.addCurrentlyEditedNotification(nuxeoController, request.getUserPrincipal(), extendedDocumentInfos);
                }

                // handle live edition through onlyofice link
                if (request.getUserPrincipal() != null) {
                    this.handleLiveEdit(request, document.getPath(), documentDto, nuxeoController);
                }

                if (onlyRemoteSections && maximized) {
                    // Remote Published documents
                    this.generatePublishedDocumentsInfos(nuxeoController, documentContext, documentDto, true);
                } else if (!onlyDescription || maximized) {
                    // Insert content menubar items
                    nuxeoController.insertContentMenuBarItems();

                    // Remote Published documents
                    this.generatePublishedDocumentsInfos(nuxeoController, documentContext, documentDto, false);

                    if (ContextualizationHelper.isCurrentDocContextualized(cmsContext)) {
                        if (publicationInfos.isLiveSpace()) {
                            if (ContextualizationHelper.isCurrentDocContextualized(cmsContext)) {
                                extendedDocumentInfos = this.cmsService.getExtendedDocumentInfos(cmsContext, document.getPath());
                            } else {
                                extendedDocumentInfos = null;
                            }


                            // Validation state
                            this.addValidationState(document, documentDto, extendedDocumentInfos);
                        }

                        // Comments
                        boolean commentsEnabled = this.areCommentsEnabled(this.cmsService, publicationInfos, cmsContext);
                        if (commentsEnabled && publicationInfos.isCommentableByUser()) {
                            documentDto.setCommentable(true);

                            // Comments
                            this.generateComments(nuxeoController, document, documentDto);
                        }

                        // Nuxeo Drive
                        this.handleDriveEdition(nuxeoController.getPortalCtx(), document, documentDto, publicationInfos);
                    }
                }


                // Document modules
                List<IPortletModule> modules = this.getModules(document.getType());
                if (CollectionUtils.isNotEmpty(modules)) {
                    for (IPortletModule module : modules) {
                        module.doView(portalControllerContext);
                    }
                }


                request.setAttribute("isEditableByUser", publicationInfos.isEditableByUser());
            }

            response.setContentType("text/html");
            this.getPortletContext().getRequestDispatcher(PATH_VIEW).include(request, response);
        } catch (NuxeoException e) {
            PortletErrorHandler.handleGenericErrors(response, e);
        } catch (PortletException e) {
            throw e;
        } catch (Exception e) {
            throw new PortletException(e);
        }
    }


    private void handleLiveEdit(RenderRequest request, String path, DocumentDTO documentDto, NuxeoController nuxeoController) throws PortalException {

        boolean isOnlyofficeRegistered = this.cmsService.getCustomizer().getPluginManager().isPluginRegistered(OnlyofficeLiveEditHelper.ONLYOFFICE_PLUGIN_NAME);

        if (isOnlyofficeRegistered && documentDto.isLiveEditable()) {

            Bundle bundle = this.bundleFactory.getBundle(request.getLocale());

            String onlyofficeEditLockUrl = OnlyofficeLiveEditHelper.getStartOnlyofficePortlerUrl(bundle, path, nuxeoController, Boolean.TRUE);
            String onlyofficeEditCollabUrl = OnlyofficeLiveEditHelper.getStartOnlyofficePortlerUrl(bundle, path, nuxeoController, Boolean.FALSE);

            request.setAttribute("onlyofficeEditLockUrl", onlyofficeEditLockUrl);
            request.setAttribute("onlyofficeEditCollabUrl", onlyofficeEditCollabUrl);
        }
    }


    /**
     * Adds a notification if the document is currently being edited e.g. onlyoffice
     *
     * @param nuxeoController
     * @param principal
     * @param extendedDocumentInfos
     */
    private void addCurrentlyEditedNotification(NuxeoController nuxeoController, Principal principal, ExtendedDocumentInfos extendedDocumentInfos) {
        PersonService personService = DirServiceFactory.getService(PersonService.class);

        Set<String> editingNames = new HashSet<>();
        Set<String> recentlyEditedNames = new HashSet<>();

        boolean editedByMe = false;

        JSONObject currentlyEditedEntry = extendedDocumentInfos.getCurrentlyEditedEntry();
        this.addDisplayNameToSet(principal, personService, editingNames, currentlyEditedEntry);

        JSONObject recentlyEditedEntry = extendedDocumentInfos.getRecentlyEditedEntry();
        this.addDisplayNameToSet(principal, personService, recentlyEditedNames, recentlyEditedEntry);

        if (this.isEditedByMe(principal, currentlyEditedEntry)) {
            editedByMe = true;
        }

        if (editedByMe) {
            if (editingNames.size() > 0) {
                this.addNotification(nuxeoController.getPortalCtx(), "CURRENTLY_EDITED_BY_OTHERS_AND_I", NotificationsType.WARNING,
                        StringUtils.join(editingNames, ", "));
            } else {
                this.addNotification(nuxeoController.getPortalCtx(), "CURRENTLY_EDITED_BY_ME", NotificationsType.WARNING);
            }
        } else {
            if (editingNames.size() > 0) {
                this.addNotification(nuxeoController.getPortalCtx(), "CURRENTLY_EDITED_BY", NotificationsType.WARNING, StringUtils.join(editingNames, ", "));
            } else if (recentlyEditedNames.size() > 0) {
                this.addNotification(nuxeoController.getPortalCtx(), "RECENTLY_EDITED_BY", NotificationsType.WARNING,
                        StringUtils.join(recentlyEditedNames, ", "));
            }
        }


    }

    private void addDisplayNameToSet(Principal principal, PersonService personService, Set<String> displayNames, JSONObject currentlyEditedEntry) {
        if (currentlyEditedEntry != null) {
            JSONArray usernamesArray = currentlyEditedEntry.getJSONArray("username");
            if (usernamesArray != null) {
                ListIterator userNamesI = usernamesArray.listIterator();
                while (userNamesI.hasNext()) {
                    String userName = (String) userNamesI.next();
                    if ((principal == null) || !(StringUtils.equals(principal.getName(), userName))) {
                        Person person = personService.getPerson(userName);
                        if (person != null) {
                            displayNames.add(person.getDisplayName());
                        }
                    }

                }
            }
        }
    }

    private boolean isEditedByMe(Principal principal, JSONObject currentlyEditedEntry) {

        if (currentlyEditedEntry != null) {
            JSONArray usernamesArray = currentlyEditedEntry.getJSONArray("username");
            if (usernamesArray != null) {
                ListIterator userNamesI = usernamesArray.listIterator();
                while (userNamesI.hasNext()) {
                    String userName = (String) userNamesI.next();
                    if ((principal != null) && (StringUtils.equals(principal.getName(), userName))) {
                        return true;
                    }

                }
            }
        }
        return false;
    }

    /**
     * Get dispatch JSP name.
     *
     * @param nuxeoController Nuxeo controller
     * @param document        Nuxeo document
     * @param extra           dispatch extra JSP indicator
     * @return JSP name
     * @throws CMSException
     */
    private String getDispatchJspName(NuxeoController nuxeoController, Document document, String suffix) throws CMSException {
        // CMS customizer
        INuxeoCustomizer customizer = nuxeoController.getNuxeoCMSService().getCMSCustomizer();
        // Portlet request
        PortletRequest request = nuxeoController.getRequest();

        // Document type
        String type = StringUtils.lowerCase(document.getType());

        // JSP path
        StringBuilder path = new StringBuilder();
        path.append("/WEB-INF/jsp/document/view-");
        path.append(type);
        if (StringUtils.isNotEmpty(suffix)) {
            path.append("-");
            path.append(suffix);
        }
        path.append(".jsp");
        // JSP name
        String name = customizer.getJSPName(path.toString(), this.getPortletContext(), request);
        // JSP real path
        String realPath = this.getPortletContext().getRealPath(name);
        // JSP file
        File file = new File(realPath);

        // Dispatch JSP name
        String dispatchJspName;

        if (file.exists()) {
            dispatchJspName = type;
        } else {
            dispatchJspName = "default";
        }

        return dispatchJspName;
    }


    /**
     * Add document validation state.
     *
     * @param document              Nuxeo document
     * @param documentDTO           document DTO
     * @param extendedDocumentInfos extended document informations, may be null
     */
    private void addValidationState(Document document, DocumentDTO documentDTO, ExtendedDocumentInfos extendedDocumentInfos) {
        // Validation state internationalization key
        String key;
        // Validation state icon
        String icon;
        // Validation state color
        String color;

        if ((extendedDocumentInfos != null) && extendedDocumentInfos.isValidationWorkflowRunning()) {
            // Validation in progress
            key = "DOCUMENT_STATE_VALIDATION_IN_PROGRESS";
            icon = "glyphicons glyphicons-hourglass";
            color = "info";
        } else if (DocumentConstants.APPROVED_DOC_STATE.equals(document.getState())) {
            // Valid
            key = "DOCUMENT_STATE_VALID";
            icon = "glyphicons glyphicons-ok";
            color = "success";
        } else {
            key = null;
            icon = null;
            color = null;
        }

        if (key != null) {
            // Validation state map
            Map<String, String> validationState = new HashMap<>();
            validationState.put("key", key);
            validationState.put("icon", icon);
            validationState.put("color", color);

            documentDTO.getProperties().put("validationState", validationState);
        }
    }


    /**
     * Generate document comments.
     *
     * @param nuxeoController Nuxeo controller
     * @param document        Nuxeo document
     * @param documentDTO     document DTO
     */
    private void generateComments(NuxeoController nuxeoController, Document document, DocumentDTO documentDTO) {
        INuxeoCommand getCommentsCommand = new GetCommentsCommand(document);
        JSONArray jsonComments = (JSONArray) nuxeoController.executeNuxeoCommand(getCommentsCommand);

        for (int i = 0; i < jsonComments.size(); i++) {
            JSONObject jsonComment = jsonComments.getJSONObject(i);
            CommentDTO commentDTO = this.commentDao.toDTO(jsonComment);
            documentDTO.getComments().add(commentDTO);
        }
    }


    /**
     * @param cmsService
     * @param publicationInfos
     * @param cmsContext
     * @return true if publish space of document
     * allows comments
     * @throws CMSException
     */
    protected boolean areCommentsEnabled(ICMSService cmsService, CMSPublicationInfos publicationInfos, CMSServiceCtx cmsContext) throws CMSException {
        boolean enable = true;

        String publishSpacePath = publicationInfos.getPublishSpacePath();
        if (StringUtils.isBlank(publishSpacePath)) {
            /* Case where currentDoc is PublishSpace */
            publishSpacePath = publicationInfos.getDocumentPath();
        }

        if (StringUtils.isNotBlank(publishSpacePath)) {
            CMSItem spaceConfig = cmsService.getSpaceConfig(cmsContext, publishSpacePath);

            Document space = (Document) spaceConfig.getNativeItem();
            boolean isPublishSpace = (space.getFacets() != null) && space.getFacets().list().contains(CommandConstants.PUBLISH_SPACE_CHARACTERISTIC);

            if (isPublishSpace) {
                enable = BooleanUtils.toBoolean(spaceConfig.getProperties().get(CommandConstants.COMMENTS_ENABLED_INDICATOR));
            }
        }

        return enable;
    }


    /**
     * Get remote published documents.
     *
     * @param readFilter      filter published documents on user read permission
     * @param nuxeoController
     * @param document
     * @param documentDTO
     */
    protected void generatePublishedDocumentsInfos(NuxeoController nuxeoController, NuxeoDocumentContext docCtx, DocumentDTO documentDTO, Boolean readFilter) {
        // We show remote sections only in Live Spaces (Wokspaces, ...)
        if (NuxeoCompatibility.isVersionGreaterOrEqualsThan(NuxeoCompatibility.VERSION_61) && docCtx.getPublicationInfos().isLiveSpace()) {

            int cacheType = nuxeoController.getCacheType();
            int authType = nuxeoController.getAuthType();

            try {

                nuxeoController.setCacheType(CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);

                GetPublishedDocumentsInfosCommand getPublishedCommand = new GetPublishedDocumentsInfosCommand(docCtx.getDocument(), readFilter);
                JSONArray jsonPublishedDocumentsInfos = (JSONArray) nuxeoController.executeNuxeoCommand(getPublishedCommand);

                for (int index = 0; index < jsonPublishedDocumentsInfos.size(); index++) {
                    JSONObject publishedDocumentInfos = jsonPublishedDocumentsInfos.getJSONObject(index);
                    RemotePublishedDocumentDTO publishedDocumentDTO = this.publishedDocumentsDao.toDTO(publishedDocumentInfos);
                    documentDTO.getPublishedDocuments().add(publishedDocumentDTO);
                }
            } finally {
                nuxeoController.setCacheType(cacheType);
                nuxeoController.setAuthType(authType);
            }
        }
    }


    /**
     * Handle current document Nuxeo Drive edition.
     *
     * @param portalControllerContext portal controller context
     * @param document                Nuxeo document
     * @param documentDto             document DTO
     * @param publicationInfos        publication informations
     */
    private void handleDriveEdition(PortalControllerContext portalControllerContext, Document document, DocumentDTO documentDto,
                                    CMSPublicationInfos publicationInfos) {
        // Portlet request
        PortletRequest request = portalControllerContext.getRequest();

        if (publicationInfos.isEditableByUser()) {
            DocumentType documentType = documentDto.getType();
            if ((documentType != null) && documentType.isFile()) {
                // Drive edit URL
                String driveEditUrl = publicationInfos.getDriveEditURL();

                // No host in nxdrive URL (get the current portal request host), refs #1421
                if (StringUtils.contains(driveEditUrl, HOST_JOKER)) {

                    // Try to get official host (in header)
                    String vhost = portalControllerContext.getHttpServletRequest().getHeader(URLUtils.VIRTUAL_HOST_REQUEST_HEADER);

                    if (StringUtils.isBlank(vhost)) {
                        // if blank, try to get the host by the request
                        vhost = request.getScheme() + "/" + request.getServerName();
                    } else {
                        vhost = vhost.replace("://", "/"); // Ndrive protocol
                    }

                    StringBuilder builder = new StringBuilder();
                    builder.append(vhost);
                    builder.append("/nuxeo");
                    driveEditUrl = StringUtils.replace(driveEditUrl, HOST_JOKER, builder.toString());
                }

                // Drive enabled indicator
                boolean driveEnabled = BooleanUtils.isTrue(publicationInfos.isDriveEnabled());

                if ((driveEditUrl != null) || driveEnabled) {
                    request.setAttribute("driveEditUrl", driveEditUrl);
                    request.setAttribute("driveEnabled", driveEnabled);
                }
            }
        }
    }


    @Override
    public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
        // Portal controller context
        PortalControllerContext portalControllerContext = new PortalControllerContext(this.getPortletContext(), request, response);
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(portalControllerContext);


        super.serveResource(request, response);


        // Document modules
        List<IPortletModule> modules = this.getModules(nuxeoController);
        if (CollectionUtils.isNotEmpty(modules)) {
            for (IPortletModule module : modules) {
                module.serveResource(portalControllerContext);
            }
        }
    }


    /**
     * Get document modules.
     *
     * @param nuxeoController Nuxeo controller
     * @return document modules
     */
    private List<IPortletModule> getModules(NuxeoController nuxeoController) {
        // Current window
        PortalWindow window = WindowFactory.getWindow(nuxeoController.getRequest());

        // Path
        String path = window.getProperty(PATH_WINDOW_PROPERTY);
        // Computed path
        path = nuxeoController.getComputedPath(path);

        // Document type
        String type;

        if (StringUtils.isBlank(path)) {
            type = null;
        } else {
            // Document context
            NuxeoDocumentContext documentContext = nuxeoController.getDocumentContext(path);
            // Document
            Document document;
            try {
                document = documentContext.getDocument();
            } catch (NuxeoException e) {
                document = null;
            }

            if (document == null) {
                type = null;
            } else {
                type = document.getType();
            }
        }

        return this.getModules(type);
    }


    /**
     * Get document modules.
     *
     * @param type document type
     * @return document modules
     */
    private List<IPortletModule> getModules(String type) {
        // Customizer
        INuxeoCustomizer customizer = this.nuxeoService.getCMSCustomizer();

        // Modules
        List<IPortletModule> modules;

        if (type == null) {
            modules = null;
        } else {
            modules = customizer.getDocumentModules(type);
        }

        return modules;
    }

}
