package fr.toutatice.portail.cms.nuxeo.portlets.files;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.MimeResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletResponse;
import javax.portlet.PortletURL;
import javax.portlet.RenderMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.WindowState;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.portlet.PortletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jboss.portal.common.invocation.Scope;

import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.cms.DocumentType;
import org.osivia.portal.api.cms.EcmDocument;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.internationalization.Bundle;
import org.osivia.portal.api.internationalization.IBundleFactory;
import org.osivia.portal.api.internationalization.IInternationalizationService;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.menubar.IMenubarService;
import org.osivia.portal.api.menubar.MenubarDropdown;
import org.osivia.portal.api.menubar.MenubarGroup;
import org.osivia.portal.api.menubar.MenubarItem;
import org.osivia.portal.api.notifications.INotificationsService;
import org.osivia.portal.api.notifications.Notifications;
import org.osivia.portal.api.notifications.NotificationsType;
import org.osivia.portal.api.panels.IPanelsService;
import org.osivia.portal.api.panels.Panel;
import org.osivia.portal.api.portalobject.bridge.PortalObjectUtils;
import org.osivia.portal.api.portlet.IPortletStatusService;
import org.osivia.portal.api.taskbar.ITaskbarService;
import org.osivia.portal.api.urls.PortalUrlType;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;
import org.osivia.portal.core.cms.CMSBinaryContent;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.ICMSService;
import org.osivia.portal.core.constants.InternalConstants;

import fr.toutatice.portail.cms.nuxeo.api.CMSPortlet;
import fr.toutatice.portail.cms.nuxeo.api.FileBrowserView;
import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.PortletErrorHandler;
import fr.toutatice.portail.cms.nuxeo.api.ResourceUtil;
import fr.toutatice.portail.cms.nuxeo.api.cms.NuxeoDocumentContext;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.api.liveedit.OnlyofficeLiveEditHelper;
import fr.toutatice.portail.cms.nuxeo.api.services.dao.DocumentDAO;
import fr.toutatice.portail.cms.nuxeo.portlets.document.helpers.DocumentHelper;
import fr.toutatice.portail.cms.nuxeo.portlets.move.MoveDocumentPortlet;
import net.sf.json.JSONObject;

/**
 * File browser portlet.
 *
 * @see CMSPortlet
 */
public class FileBrowserPortlet extends CMSPortlet {

    /** default maximum size of uploaded file in Mb */
    private static final long MAX_FILE_SIZE_DEFAULT = 500;

    /** maximum size of uploaded file in Mb */
    private static final String MAX_FILE_SIZE = System.getProperty("osivia.filebrowser.max.upload.size", String.valueOf(MAX_FILE_SIZE_DEFAULT));

    /** Synchronized ES indexation flag. */
    public static final String ES_SYNC_FLAG = "nx_es_sync";

    /** File upload notifications duration. */
    private static final int FILE_UPLOAD_NOTIFICATIONS_DURATION = 1000;

    /** Nuxeo path window property name. */
    private static final String NUXEO_PATH_WINDOW_PROPERTY = "osivia.nuxeoPath";

    /** View request parameter name. */
    private static final String VIEW_REQUEST_PARAMETER = "view";
    /** Sort criteria request parameter name. */
    private static final String SORT_CRITERIA_REQUEST_PARAMETER = "sort";
    /** Alternative sort request parameter name. */
    private static final String ALTERNATIVE_SORT_REQUEST_PARAMETER = "alt";
    /** Sort criteria principal scope attribute name. */
    private static final String SORT_CRITERIA_PRINCIPAL_ATTRIBUTE = "osivia.fileBrowser.sortCriteria";

    /** Admin JSP path. */
    private static final String PATH_ADMIN = "/WEB-INF/jsp/files/admin.jsp";
    /** View JSP path. */
    private static final String PATH_VIEW = "/WEB-INF/jsp/files/view.jsp";
    /** Error JSP path. */
    private static final String PATH_ERROR = "/WEB-INF/jsp/files/error.jsp";

    /** Host joker. */
    private static final String HOST_JOKER = "__HOST__";


    /** Bundle factory. */
    private IBundleFactory bundleFactory;
    /** Notifications service. */
    private INotificationsService notificationsService;
    /** Portlet status service. */
    private IPortletStatusService portletStatusService;
    /** Panel service. */
    private IPanelsService panelsService;
    /** Taskbar service. */
    private ITaskbarService taskbarService;
    /** Menubar service. */
    private IMenubarService menubarService;
    /** Document DAO. */
    private DocumentDAO documentDao;


    /**
     * Constructor.
     */
    public FileBrowserPortlet() {
        super();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void init(PortletConfig config) throws PortletException {
        super.init(config);

        // Portlet context
        PortletContext portletContext = this.getPortletContext();

        // Bundle factory
        IInternationalizationService internationalizationService = (IInternationalizationService) portletContext
                .getAttribute(Constants.INTERNATIONALIZATION_SERVICE_NAME);
        this.bundleFactory = internationalizationService.getBundleFactory(this.getClass().getClassLoader());
        // Notification service
        this.notificationsService = (INotificationsService) portletContext.getAttribute(Constants.NOTIFICATIONS_SERVICE_NAME);
        // Portlet status service
        this.portletStatusService = Locator.findMBean(IPortletStatusService.class, IPortletStatusService.MBEAN_NAME);
        // Panels service
        this.panelsService = Locator.findMBean(IPanelsService.class, IPanelsService.MBEAN_NAME);
        // Taskbar service
        this.taskbarService = Locator.findMBean(ITaskbarService.class, ITaskbarService.MBEAN_NAME);
        // Menubar service
        this.menubarService = Locator.findMBean(IMenubarService.class, IMenubarService.MBEAN_NAME);
        // Document DAO
        this.documentDao = DocumentDAO.getInstance();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void processAction(ActionRequest request, ActionResponse response) throws PortletException, IOException {
        // Action name
        String action = request.getParameter(ActionRequest.ACTION_NAME);

        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(request, response, this.getPortletContext());
        // CMS service
        ICMSService cmsService = NuxeoController.getCMSService();
        // CMS context
        CMSServiceCtx cmsContext = nuxeoController.getCMSCtx();
        // Portal controller context
        PortalControllerContext portalControllerContext = nuxeoController.getPortalCtx();
        // Bundle
        Bundle bundle = this.bundleFactory.getBundle(request.getLocale());

        if (PortletMode.VIEW.equals(request.getPortletMode())) {
            // View

            if ("changeView".equals(action)) {
                // Change view

                FileBrowserView view = FileBrowserView.fromName(request.getParameter(VIEW_REQUEST_PARAMETER));

                // Path
                String path = this.getPath(nuxeoController);
                // Document context
                NuxeoDocumentContext documentContext = nuxeoController.getDocumentContext(path);

                // Document
                Document document = documentContext.getDocument();
                // Type
                String type = document.getType();

                // Active task identifier
                String taskId;
                try {
                    taskId = this.taskbarService.getActiveId(portalControllerContext);
                } catch (PortalException e) {
                    throw new PortletException(e);
                }

                // Portlet status
                FileBrowserStatus status = this.portletStatusService.getStatus(portalControllerContext, this.getPortletName(), FileBrowserStatus.class);
                if (status == null) {
                    status = new FileBrowserStatus(taskId);
                }
                status.getViews().put(type, view);
                this.portletStatusService.setStatus(portalControllerContext, this.getPortletName(), status);

            } else if ("copy".equals(action)) {
                // Copy action

                String sourcePath = request.getParameter("sourcePath");
                String targetPath = getPath(nuxeoController);

                INuxeoCommand command = new CopyDocumentCommand(sourcePath, targetPath);
                nuxeoController.executeNuxeoCommand(command);


                // Refresh navigation
                request.setAttribute(Constants.PORTLET_ATTR_UPDATE_CONTENTS, Constants.PORTLET_VALUE_ACTIVATE);

                // Update public render parameter for associated portlets refresh
                response.setRenderParameter("dnd-update", String.valueOf(System.currentTimeMillis()));

            } else if ("delete".equals(action)) {
                // Delete action

                String[] identifiers = StringUtils.split(request.getParameter("identifiers"), ",");
                if (ArrayUtils.isNotEmpty(identifiers)) {
                    try {
                        for (String id : identifiers) {
                            cmsService.putDocumentInTrash(cmsContext, id);
                        }


                        // Notification
                        String message = bundle.getString("SUCCESS_MESSAGE_DELETE");
                        this.notificationsService.addSimpleNotification(portalControllerContext, message, NotificationsType.SUCCESS);

                        // Refresh navigation
                        request.setAttribute(Constants.PORTLET_ATTR_UPDATE_CONTENTS, Constants.PORTLET_VALUE_ACTIVATE);

                    } catch (CMSException e) {
                        // Notification
                        String message = bundle.getString("ERROR_MESSAGE_ERROR_HAS_OCCURED");
                        this.notificationsService.addSimpleNotification(portalControllerContext, message, NotificationsType.ERROR);
                    }
                }

            } else if ("drop".equals(action)) {
                // Drop action

                // Source identifiers
                List<String> sourceIds = Arrays.asList(StringUtils.split(request.getParameter("sourceIds"), ","));
                // Target identifier
                String targetId = request.getParameter("targetId");

                // Move document command
                INuxeoCommand command = new MoveDocumentCommand(sourceIds, targetId);
                try {
                    nuxeoController.executeNuxeoCommand(command);

                    // Refresh navigation
                    request.setAttribute(Constants.PORTLET_ATTR_UPDATE_CONTENTS, Constants.PORTLET_VALUE_ACTIVATE);

                    // Update public render parameter for associated portlets refresh
                    response.setRenderParameter("dnd-update", String.valueOf(System.currentTimeMillis()));


                    // Notification
                    String message;
                    if (sourceIds.size() == 1) {
                        message = bundle.getString("DOCUMENT_MOVE_SUCCESS_MESSAGE");
                    } else {
                        message = bundle.getString("DOCUMENTS_MOVE_SUCCESS_MESSAGE", sourceIds.size());
                    }
                    this.notificationsService.addSimpleNotification(portalControllerContext, message, NotificationsType.SUCCESS);
                } catch (NuxeoException e) {
                    // Notification
                    String message;
                    if (sourceIds.size() == 1) {
                        message = bundle.getString("DOCUMENT_MOVE_WARNING_MESSAGE");
                    } else {
                        message = bundle.getString("DOCUMENTS_MOVE_WARNING_MESSAGE", sourceIds.size());
                    }
                    this.notificationsService.addSimpleNotification(portalControllerContext, message, NotificationsType.WARNING);
                }

            } else if ("sort".equals(action)) {
                // Sort action

                String sourceId = request.getParameter("sourceId");
                String targetId = request.getParameter("targetId");

                INuxeoCommand command = new SortDocumentCommand(sourceId, targetId);
                nuxeoController.executeNuxeoCommand(command);

                // Refresh navigation
                request.setAttribute(Constants.PORTLET_ATTR_UPDATE_CONTENTS, Constants.PORTLET_VALUE_ACTIVATE);

                // Update public render parameter for associated portlets refresh
                response.setRenderParameter("dnd-update", String.valueOf(System.currentTimeMillis()));

            } else if ("fileUpload".equals(action)) {
                // File upload

                String parentId = request.getParameter("parentId");


                // Notification
                Notifications notifications;

                try {
                    DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
                    PortletFileUpload fileUpload = new PortletFileUpload(fileItemFactory);
                    List<FileItem> fileItems = fileUpload.parseRequest(request);

                    // file size check
                    if (fileItems != null) {
                        for (FileItem fileItem : fileItems) {
                            long maximumFileSizeInMb = NumberUtils.toLong(MAX_FILE_SIZE, MAX_FILE_SIZE_DEFAULT);
                            if (fileItem.getSize() > maximumFileSizeInMb * FileUtils.ONE_MB) {
                                throw new FileSizeLimitExceededException(null, fileItem.getSize(), maximumFileSizeInMb * FileUtils.ONE_MB);
                            }
                        }
                    }

                    // Nuxeo command
                    INuxeoCommand command = new UploadFilesCommand(parentId, fileItems, true);
                    nuxeoController.executeNuxeoCommand(command);

                    // Refresh navigation
                    request.setAttribute(Constants.PORTLET_ATTR_UPDATE_CONTENTS, Constants.PORTLET_VALUE_ACTIVATE);


                    // Notification
                    notifications = new Notifications(NotificationsType.SUCCESS, FILE_UPLOAD_NOTIFICATIONS_DURATION);
                    notifications.addMessage(bundle.getString("MESSAGE_FILE_UPLOAD_SUCCESS"));
                } catch (FileSizeLimitExceededException e) {
                    // Notification
                    notifications = new Notifications(NotificationsType.ERROR, FILE_UPLOAD_NOTIFICATIONS_DURATION);
                    notifications.addMessage(bundle.getString("MESSAGE_FILE_UPLOAD_FILE_SIZE_TOO_LARGE", MAX_FILE_SIZE));
                } catch (FileUploadException e) {
                    // Notification
                    notifications = new Notifications(NotificationsType.ERROR, FILE_UPLOAD_NOTIFICATIONS_DURATION);
                    notifications.addMessage(bundle.getString("MESSAGE_FILE_UPLOAD_ERROR"));
                }

                this.notificationsService.addNotifications(portalControllerContext, notifications);
            }

        } else if ("admin".equals(request.getPortletMode().toString())) {
            // Admin

            if ("save".equals(action)) {
                // Save

                // Current window
                PortalWindow window = WindowFactory.getWindow(request);

                // Nuxeo path
                String path = request.getParameter("path");
                window.setProperty(NUXEO_PATH_WINDOW_PROPERTY, path);
            }

            response.setPortletMode(PortletMode.VIEW);
            response.setWindowState(WindowState.NORMAL);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
        if ("infos".equals(request.getResourceID())) {
            JSONObject data = new JSONObject();

            // Document path
            String path = request.getParameter("path");
            // File document indicator
            String isFile = request.getParameter("file");

            if (path != null) {
                NuxeoController nuxeoController = new NuxeoController(request, response, getPortletContext());
                ICMSService cmsService = this.getCMSService();
                CMSServiceCtx cmsContext = nuxeoController.getCMSCtx();

                try {
                    CMSPublicationInfos publicationInfos = cmsService.getPublicationInfos(cmsContext, path);

                    data.put("writable", publicationInfos.isEditableByUser());
                    data.put("copiable", publicationInfos.isCopiable());

                    if (BooleanUtils.toBoolean(isFile)) {
                        String driveEditUrl = publicationInfos.getDriveEditURL();

                        // No host in nxdrive URL (get the current portal request host), refs #1421
                        if (StringUtils.contains(driveEditUrl, HOST_JOKER)) {
                            StringBuilder builder = new StringBuilder();
                            builder.append(request.getScheme());
                            builder.append("/");
                            builder.append(request.getServerName());
                            builder.append("/nuxeo");
                            driveEditUrl = StringUtils.replace(driveEditUrl, HOST_JOKER, builder.toString());
                        }

                        data.put("driveEditUrl", driveEditUrl);
                    }
                } catch (CMSException e) {
                    // Do nothing
                }
            }

            // Content type
            response.setContentType("application/json");

            // Content
            PrintWriter printWriter = new PrintWriter(response.getPortletOutputStream());
            printWriter.write(data.toString());
            printWriter.close();
        } else if("zipDownload".equals(request.getResourceID())) {
            // bulk download

            // selected download paths
            String[] paths = StringUtils.split(request.getParameter("paths"), ",");

            NuxeoController nuxeoController = new NuxeoController(request, response, getPortletContext());
            CMSBinaryContent content = (CMSBinaryContent) nuxeoController.executeNuxeoCommand(new BulkFilesCommand(nuxeoController, paths));

            response.setContentType(content.getMimeType());
            response.setProperty("Content-disposition", "inline; filename=\"" + content.getName() + "\"");

            ResourceUtil.copy(new FileInputStream(content.getFile()), response.getPortletOutputStream(), 4096);
        } else {
            super.serveResource(request, response);
        }
    }


    /**
     * Admin view display.
     *
     * @param request request
     * @param response response
     * @throws PortletException
     * @throws IOException
     */
    @RenderMode(name = "admin")
    public void doAdmin(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        // Current window
        PortalWindow window = WindowFactory.getWindow(request);

        // Nuxeo path
        String path = window.getProperty(NUXEO_PATH_WINDOW_PROPERTY);
        request.setAttribute("path", path);

        response.setContentType("text/html");
        this.getPortletContext().getRequestDispatcher(PATH_ADMIN).include(request, response);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(request, response, this.getPortletContext());

        // Path
        String path = this.getPath(nuxeoController);

        PortletRequestDispatcher dispatcher;
        if (StringUtils.isNotEmpty(path)) {
            try {
                // Portal controller context
                PortalControllerContext portalControllerContext = new PortalControllerContext(this.getPortletContext(), request, response);

                // CMS service
                ICMSService cmsService = this.getCMSService();
                // CMS context
                CMSServiceCtx cmsContext = nuxeoController.getCMSCtx();

				// Computed path
                path = nuxeoController.getComputedPath(path);

                // Publication informations
                CMSPublicationInfos publicationInfos = cmsService.getPublicationInfos(cmsContext, path);
                boolean editable = publicationInfos.isEditableByUser();
                request.setAttribute("editable", editable);
                request.setAttribute("canUpload", !publicationInfos.getSubTypes().isEmpty());
                request.setAttribute("driveEnabled", publicationInfos.isDriveEnabled());

                // Fetch current Nuxeo document
                NuxeoDocumentContext documentContext = nuxeoController.getDocumentContext(path);
                Document currentDocument = documentContext.getDocument();
                nuxeoController.setCurrentDoc(currentDocument);
                FileBrowserItem fileBrowser = new FileBrowserItem(this.documentDao.toDTO(portalControllerContext, currentDocument));
                request.setAttribute("document", fileBrowser);

                // Fetch Nuxeo children documents
                INuxeoCommand command = new GetFolderFilesCommand(publicationInfos.getLiveId());
                Documents documents = (Documents) nuxeoController.executeNuxeoCommand(command);


                // Subscriptions
                List<EcmDocument> subscriptionDocuments = cmsService.getUserSubscriptions(cmsContext);
                Set<String> subscriptions = new HashSet<>(subscriptionDocuments.size());
                for (EcmDocument subscriptionDocument : subscriptionDocuments) {
                    if (subscriptionDocument instanceof Document) {
                        // Nuxeo document
                        Document subscriptionNuxeoDocument = (Document) subscriptionDocument;
                        // Nuxeo document identifier
                        String id = subscriptionNuxeoDocument.getId();

                        subscriptions.add(id);
                    }
                }

                Bundle bundle = bundleFactory.getBundle(request.getLocale());

                // Documents DTO
                int index = 1;
                List<FileBrowserItem> fileBrowserItems = new ArrayList<>(documents.size());
                for (Document document : documents) {
                    DocumentDTO documentDto = this.documentDao.toDTO(portalControllerContext, document);
                    documentDto = setDraftInfos(document, documentDto);
                    
                    if(request.getUserPrincipal() != null) {
                    	documentDto = setLiveEditUrl(documentDto, nuxeoController, bundle);
                    }
                    
                    FileBrowserItem fileBrowserItem = new FileBrowserItem(documentDto);
                    fileBrowserItem.setIndex(index++);

                    // Subscription indicator
                    boolean subscription = subscriptions.contains(document.getId());
                    fileBrowserItem.setSubscription(subscription);

                    // Sharing indicator
                    boolean sharing = (document.getFacets() != null) && document.getFacets().list().contains("Sharing");
                    fileBrowserItem.setSharing(sharing);

                    fileBrowserItems.add(fileBrowserItem);
                }


                // Ordered indicator
                DocumentType cmsItemType = nuxeoController.getCMSItemTypes().get(currentDocument.getType());
                boolean ordered = cmsItemType != null && cmsItemType.isOrdered();
                request.setAttribute("ordered", ordered);


                // Current view
                FileBrowserView currentView = this.getCurrentView(portalControllerContext, currentDocument.getType());
                request.setAttribute("view", currentView.getName());


                // Sort criteria
                FileBrowserSortCriteria criteria = this.getSortCriteria(portalControllerContext, ordered, currentView);
                request.setAttribute("criteria", criteria);


                // Sort documents
                Comparator<FileBrowserItem> comparator = new FileBrowserComparator(criteria);
                Collections.sort(fileBrowserItems, comparator);
                request.setAttribute("documents", fileBrowserItems);

                // max file upload size
                long maximumFileSizeInMb = NumberUtils.toLong(MAX_FILE_SIZE, MAX_FILE_SIZE_DEFAULT);
                request.setAttribute("maximumFileSize", maximumFileSizeInMb * FileUtils.ONE_MB);
                request.setAttribute("maximumFileSizeInMb", maximumFileSizeInMb);


                // Toolbar attributes
                this.addToolbarAttributes(portalControllerContext, nuxeoController, currentDocument);

                // Title
                response.setTitle(currentDocument.getTitle());

                // Insert standard menu bar for content item
                if (WindowState.MAXIMIZED.equals(request.getWindowState())) {
                    // Add menubar items
                    this.addMenubarItems(portalControllerContext, currentView);

                    nuxeoController.insertContentMenuBarItems();
                }
            } catch (NuxeoException e) {
                PortletErrorHandler.handleGenericErrors(response, e);
            } catch (Exception e) {
                throw new PortletException(e);
            }

            dispatcher = this.getPortletContext().getRequestDispatcher(PATH_VIEW);
        } else {
            // Error
            dispatcher = this.getPortletContext().getRequestDispatcher(PATH_ERROR);
        }

        response.setContentType("text/html");
        dispatcher.include(request, response);
    }


    /**
     * Get path.
     *
     * @param nuxeoController Nuxeo controller
     * @return path
     */
    private String getPath(NuxeoController nuxeoController) {
        // Portlet request
        PortletRequest request = nuxeoController.getRequest();
        // Current window
        PortalWindow window = WindowFactory.getWindow(request);

        String path = window.getProperty(Constants.WINDOW_PROP_URI);
        if (path == null) {
            path = window.getProperty(NUXEO_PATH_WINDOW_PROPERTY);
        }

        if (path != null) {
            path = nuxeoController.getComputedPath(path);
        }

        return path;
    }


    private DocumentDTO setLiveEditUrl(DocumentDTO documentDTO, NuxeoController nuxeoController, Bundle bundle) throws PortalException {

        // onlyoffice
        if (documentDTO.isLiveEditable() && nuxeoController.getNuxeoCMSService().getCMSCustomizer().getCustomizationService()
                .isPluginRegistered(OnlyofficeLiveEditHelper.ONLYOFFICE_PLUGIN_NAME)) {

            String onlyofficeEditLockUrl = OnlyofficeLiveEditHelper.getStartOnlyofficePortlerUrl(bundle, documentDTO.getPath(), nuxeoController, Boolean.TRUE);
            documentDTO.getProperties().put("onlyofficeEditLockUrl", onlyofficeEditLockUrl);
            
            String onlyofficeEditCollabUrl = OnlyofficeLiveEditHelper.getStartOnlyofficePortlerUrl(bundle, documentDTO.getPath(), nuxeoController, Boolean.FALSE);
            documentDTO.getProperties().put("onlyofficeEditCollabUrl", onlyofficeEditCollabUrl);            
        }
        return documentDTO;
    }

    /**
     * Set draft informations if document has draft.
     *
     * @param publicationInfos
     * @param document
     * @return document with modified properies
     */
    private DocumentDTO setDraftInfos(Document document, DocumentDTO documentDTO) {
        if (DocumentHelper.hasDraft(document)) {
            String draftPath = DocumentHelper.getDraftPath(document);
            documentDTO.getProperties().put("draftPath", draftPath);
        }
        return documentDTO;
    }


    /**
     * Get current file browser view.
     *
     * @param portalControllerContext portal controller context
     * @param type type name
     * @return view
     * @throws PortletException
     */
    private FileBrowserView getCurrentView(PortalControllerContext portalControllerContext, String type) throws PortletException {
        // Portlet request
        PortletRequest request = portalControllerContext.getRequest();
        // Current window
        PortalWindow window = WindowFactory.getWindow(request);
        // Portlet status
        FileBrowserStatus status = this.portletStatusService.getStatus(portalControllerContext, this.getPortletName(), FileBrowserStatus.class);

        // Active task identifier
        String taskId;
        try {
            taskId = this.taskbarService.getActiveId(portalControllerContext);
        } catch (PortalException e) {
            throw new PortletException(e);
        }

        // Current view
        FileBrowserView currentView = null;

        if (status != null) {
            if (StringUtils.equals(taskId, status.getTaskId())) {
                currentView = status.getViews().get(type);
            } else {
                // Status reinitialization
                this.portletStatusService.setStatus(portalControllerContext, this.getPortletName(), null);
            }
        }

        if (currentView == null) {
            currentView = FileBrowserView.fromName(window.getProperty(InternalConstants.DEFAULT_VIEW_WINDOW_PROPERTY));
        }


        // Toggle panel
        try {
            if (currentView.isClosedNavigation()) {
                this.panelsService.hidePanel(portalControllerContext, Panel.NAVIGATION_PANEL);
            } else {
                this.panelsService.showPanel(portalControllerContext, Panel.NAVIGATION_PANEL);
            }
        } catch (PortalException e) {
            throw new PortletException(e);
        }

        return currentView;
    }


    /**
     * Get sort criteria.
     *
     * @param portalControllerContext portal controller context
     * @param ordered ordered indicator
     * @param currentView current file browser view
     * @return sort criteria
     */
    private FileBrowserSortCriteria getSortCriteria(PortalControllerContext portalControllerContext, boolean ordered, FileBrowserView currentView) {

        // Request
        PortletRequest request = portalControllerContext.getRequest();


        FileBrowserSortCriteria criteria = null;

        if (currentView.isOrderable()) {
            String sort = request.getParameter(SORT_CRITERIA_REQUEST_PARAMETER);
            if (StringUtils.isEmpty(sort)) {
                Object attribute = PortalObjectUtils.getPortalSessionAttribute(portalControllerContext,  SORT_CRITERIA_PRINCIPAL_ATTRIBUTE);
                if (attribute != null && attribute instanceof FileBrowserSortCriteria) {
                    criteria = (FileBrowserSortCriteria) attribute;
                    if (!ordered && FileBrowserSortCriteria.SORT_BY_INDEX.equals(criteria.getSort())) {
                        criteria.setSort(FileBrowserSortCriteria.SORT_BY_NAME);
                    }
                }
            } else {
                boolean alternative = BooleanUtils.toBoolean(request.getParameter(ALTERNATIVE_SORT_REQUEST_PARAMETER));

                criteria = new FileBrowserSortCriteria();
                criteria.setSort(sort);
                criteria.setAlternative(alternative);

                PortalObjectUtils.setPortalSessionAttribute(portalControllerContext,  SORT_CRITERIA_PRINCIPAL_ATTRIBUTE, criteria);
            }
        }

        if (criteria == null) {
            // Default sort criteria
            criteria = new FileBrowserSortCriteria();
            if (ordered) {
                criteria.setSort(FileBrowserSortCriteria.SORT_BY_INDEX);
            } else {
                criteria.setSort(FileBrowserSortCriteria.SORT_BY_NAME);
            }
        }

        return criteria;
    }


    /**
     * Add menubar items.
     *
     * @param portalControllerContext portal controller context
     * @param currentView current file browser view
     */
    @SuppressWarnings("unchecked")
    private void addMenubarItems(PortalControllerContext portalControllerContext, FileBrowserView currentView) {
        // Request
        PortletRequest request = portalControllerContext.getRequest();

        if (WindowState.MAXIMIZED.equals(request.getWindowState())) {
            // Response
            PortletResponse response = portalControllerContext.getResponse();
            // Bundle
            Bundle bundle = this.bundleFactory.getBundle(request.getLocale());
            // Menubar
            List<MenubarItem> menubar = (List<MenubarItem>) request.getAttribute(Constants.PORTLET_ATTR_MENU_BAR);


            if (response instanceof MimeResponse) {
                MimeResponse mimeResponse = (MimeResponse) response;

                // Change view
                int order = 0;
                for (FileBrowserView view : FileBrowserView.values()) {
                    if ((view != currentView) && view.isMenubarItem() && !currentView.getLinkedViewNames().contains(view.getName())) {
                        // Identifier
                        StringBuilder builder = new StringBuilder();
                        builder.append("FILE_BROWSER_SHOW_");
                        builder.append(StringUtils.upperCase(view.getName()));
                        String id = builder.toString();

                        // URL
                        PortletURL actionURL = mimeResponse.createActionURL();
                        actionURL.setParameter(ActionRequest.ACTION_NAME, "changeView");
                        actionURL.setParameter(VIEW_REQUEST_PARAMETER, view.getName());

                        MenubarItem menubarItem = new MenubarItem(id, bundle.getString(id), view.getIcon(), MenubarGroup.SPECIFIC, order, actionURL.toString(),
                                null, null, null);
                        menubar.add(menubarItem);

                        order++;
                    }
                }
                
                // Toggle reorganization
                if (FileBrowserView.THUMBNAILS.equals(currentView) || FileBrowserView.THUMBNAILS_REORGANIZATION.equals(currentView)) {
                    // Identifier
                    String id = "FILE_BROWSER_TOGGLE_REORGANIZATION";
                    // Icon
                    String icon;
                    // Menubar item parent
                    MenubarDropdown parent = this.menubarService.getDropdown(portalControllerContext, MenubarDropdown.CMS_EDITION_DROPDOWN_MENU_ID);
                    if (parent == null) {
                        parent = new MenubarDropdown(MenubarDropdown.CMS_EDITION_DROPDOWN_MENU_ID, MenubarGroup.CMS);
                    }
                    // URL
                    PortletURL actionURL = mimeResponse.createActionURL();
                    actionURL.setParameter(ActionRequest.ACTION_NAME, "changeView");
                    
                    if (FileBrowserView.THUMBNAILS.equals(currentView)) {
                        icon = "halflings halflings-unchecked";
                        actionURL.setParameter(VIEW_REQUEST_PARAMETER, FileBrowserView.THUMBNAILS_REORGANIZATION.getName());
                    } else {
                        icon = "halflings halflings-check";
                        actionURL.setParameter(VIEW_REQUEST_PARAMETER, FileBrowserView.THUMBNAILS.getName());
                    }
                    
                    // Menubar item
                    MenubarItem menubarItem = new MenubarItem(id, bundle.getString(id), icon, parent, 0, actionURL.toString(), null, null, null);
                    menubar.add(menubarItem);
                }
            }
        }
    }


    /**
     * Add toolbar request attributes.
     *
     * @param portalControllerContext portal controller context
     * @param nuxeoController Nuxeo controller
     * @param currentDocument current Nuxeo document
     * @throws CMSException
     * @throws PortalException
     */
    private void addToolbarAttributes(PortalControllerContext portalControllerContext, NuxeoController nuxeoController, Document currentDocument)
            throws CMSException, PortalException {
        // Request
        PortletRequest request = portalControllerContext.getRequest();
        // CMS context
        CMSServiceCtx cmsContext = nuxeoController.getCMSCtx();

    

        // Move URL
        Map<String, String> moveProperties = new HashMap<>();
        moveProperties.put(MoveDocumentPortlet.DOCUMENT_PATH_WINDOW_PROPERTY, currentDocument.getPath());
        moveProperties.put(MoveDocumentPortlet.DOCUMENTS_IDENTIFIERS_WINDOW_PROPERTY, "_IDS_");
        moveProperties.put(MoveDocumentPortlet.IGNORED_PATHS_WINDOW_PROPERTY, "_PATHS_");
        moveProperties.put(MoveDocumentPortlet.CMS_BASE_PATH_WINDOW_PROPERTY, nuxeoController.getBasePath());
        moveProperties.put(MoveDocumentPortlet.ACCEPTED_TYPES_WINDOW_PROPERTY, "_TYPES_");
        String moveUrl = this.getPortalUrlFactory().getStartPortletUrl(portalControllerContext, "toutatice-portail-cms-nuxeo-move-portlet-instance",
                moveProperties, PortalUrlType.POPUP);
        request.setAttribute("moveUrl", moveUrl);
    }

}

