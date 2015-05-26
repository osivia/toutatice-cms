package fr.toutatice.portail.cms.nuxeo.portlets.files;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.portlet.PortletFileUpload;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.portal.common.invocation.Scope;
import org.jboss.portal.core.controller.ControllerContext;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.internationalization.Bundle;
import org.osivia.portal.api.internationalization.IBundleFactory;
import org.osivia.portal.api.internationalization.IInternationalizationService;
import org.osivia.portal.api.notifications.INotificationsService;
import org.osivia.portal.api.notifications.Notifications;
import org.osivia.portal.api.notifications.NotificationsType;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;
import org.osivia.portal.core.cms.CMSItemType;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.ICMSService;

import fr.toutatice.portail.cms.nuxeo.api.CMSPortlet;
import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.PortletErrorHandler;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.api.services.dao.DocumentDAO;

/**
 * File browser portlet.
 *
 * @see CMSPortlet
 */
public class FileBrowserPortlet extends CMSPortlet {

    /** File upload notifications duration. */
    private static final int FILE_UPLOAD_NOTIFICATIONS_DURATION = 1000;

    /** Nuxeo path window property name. */
    private static final String NUXEO_PATH_WINDOW_PROPERTY = "osivia.nuxeoPath";
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


    /** Bundle factory. */
    private IBundleFactory bundleFactory;
    /** Notifications service. */
    private INotificationsService notificationsService;
    /** Document DAO. */
    private DocumentDAO documentDAO;


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
        // Document DAO
        this.documentDAO = DocumentDAO.getInstance();
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
        // Portal controller context
        PortalControllerContext portalControllerContext = nuxeoController.getPortalCtx();
        // Bundle
        Bundle bundle = this.bundleFactory.getBundle(request.getLocale());

        if (PortletMode.VIEW.equals(request.getPortletMode())) {
            // View

            if ("drop".equals(action)) {
                // Drop action

                // Source
                String sourceId = request.getParameter("sourceId");
                // Target
                String targetId = request.getParameter("targetId");

                // Move document command
                INuxeoCommand command = new MoveDocumentCommand(sourceId, targetId);
                try {
                    nuxeoController.executeNuxeoCommand(command);

                    // Refresh navigation
                    request.setAttribute(Constants.PORTLET_ATTR_UPDATE_CONTENTS, Constants.PORTLET_VALUE_ACTIVATE);

                    // Update public render parameter for associated portlets refresh
                    response.setRenderParameter("dnd-update", String.valueOf(System.currentTimeMillis()));

                    // Notification
                    String message = bundle.getString("MESSAGE_MOVE_SUCCESS");
                    this.notificationsService.addSimpleNotification(portalControllerContext, message, NotificationsType.SUCCESS);
                } catch (NuxeoException e) {
                    // Notification
                    String message = bundle.getString("MESSAGE_MOVE_ERROR");
                    this.notificationsService.addSimpleNotification(portalControllerContext, message, NotificationsType.ERROR);
                }

            } else if ("fileUpload".equals(action)) {
                // File upload

                String parentId = request.getParameter("parentId");

                // Notification
                Notifications notifications;

                try {
                    DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
                    PortletFileUpload fileUpload = new PortletFileUpload(fileItemFactory);
                    List<FileItem> fileItems = fileUpload.parseRequest(request);

                    // Nuxeo command
                    INuxeoCommand command = new UploadFilesCommand(parentId, fileItems);
                    nuxeoController.executeNuxeoCommand(command);

                    // Refresh navigation
                    request.setAttribute(Constants.PORTLET_ATTR_UPDATE_CONTENTS, Constants.PORTLET_VALUE_ACTIVATE);

                    // Notification
                    notifications = new Notifications(NotificationsType.SUCCESS, FILE_UPLOAD_NOTIFICATIONS_DURATION);
                    notifications.addMessage(bundle.getString("MESSAGE_FILE_UPLOAD_SUCCESS"));
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
        // Current window
        PortalWindow window = WindowFactory.getWindow(request);

        // Path
        String path = window.getProperty(Constants.WINDOW_PROP_URI);
        if (path == null) {
            path = window.getProperty(NUXEO_PATH_WINDOW_PROPERTY);
        }

        PortletRequestDispatcher dispatcher;
        if (StringUtils.isNotEmpty(path)) {
            try {
                // Controller context
                ControllerContext controllerContext = (ControllerContext) request.getAttribute("osivia.controller");

                // Nuxeo controller
                NuxeoController nuxeoController = new NuxeoController(request, response, this.getPortletContext());
                request.setAttribute("nuxeoController", nuxeoController);

                // CMS service
                ICMSService cmsService = NuxeoController.getCMSService();
                // CMS context
                CMSServiceCtx cmsContext = nuxeoController.getCMSCtx();

                // Computed path
                path = nuxeoController.getComputedPath(path);

                // Fetch current Nuxeo document
                Document currentDocument = nuxeoController.fetchDocument(path);
                nuxeoController.setCurrentDoc(currentDocument);
                request.setAttribute("document", this.documentDAO.toDTO(currentDocument));

                // Publication informations
                CMSPublicationInfos publicationInfos = cmsService.getPublicationInfos(cmsContext, path);
                boolean editable = publicationInfos.isEditableByUser();
                request.setAttribute("editable", editable);

                // Fetch Nuxeo children documents
                INuxeoCommand command = new GetFolderFilesCommand(publicationInfos.getLiveId());
                Documents documents = (Documents) nuxeoController.executeNuxeoCommand(command);


                // Documents DTO
                int index = 1;
                List<FileBrowserItem> fileBrowserItems = new ArrayList<FileBrowserItem>(documents.size());
                for (Document document : documents) {
                    DocumentDTO documentDTO = this.documentDAO.toDTO(document);
                    FileBrowserItem fileBrowserItem = new FileBrowserItem(documentDTO);
                    fileBrowserItem.setIndex(index++);

                    if ("File".equals(document.getType())) {
                        this.addMimeType(document, fileBrowserItem);
                    }

                    fileBrowserItems.add(fileBrowserItem);
                }


                // Ordered indicator
                CMSItemType cmsItemType = nuxeoController.getCMSItemTypes().get(currentDocument.getType());
                boolean ordered = ((cmsItemType != null) && cmsItemType.isOrdered());
                request.setAttribute("ordered", ordered);


                // Sort criteria
                FileBrowserSortCriteria criteria;
                String sort = request.getParameter(SORT_CRITERIA_REQUEST_PARAMETER);
                if (StringUtils.isEmpty(sort)) {
                    criteria = (FileBrowserSortCriteria) controllerContext.getAttribute(Scope.PRINCIPAL_SCOPE, SORT_CRITERIA_PRINCIPAL_ATTRIBUTE);
                    
                    if (criteria == null) {
                        criteria = new FileBrowserSortCriteria();
                        if (ordered) {
                            criteria.setSort("index");
                        } else {
                            criteria.setSort("name");
                        }

                        controllerContext.setAttribute(Scope.PRINCIPAL_SCOPE, SORT_CRITERIA_PRINCIPAL_ATTRIBUTE, criteria);
                    }
                } else {
                    boolean alternative = BooleanUtils.toBoolean(request.getParameter(ALTERNATIVE_SORT_REQUEST_PARAMETER));

                    criteria = new FileBrowserSortCriteria();
                    criteria.setSort(sort);
                    criteria.setAlternative(alternative);

                    controllerContext.setAttribute(Scope.PRINCIPAL_SCOPE, SORT_CRITERIA_PRINCIPAL_ATTRIBUTE, criteria);
                }
                request.setAttribute("criteria", criteria);


                // Sort documents
                Comparator<FileBrowserItem> comparator = new FileBrowserComparator(criteria);
                Collections.sort(fileBrowserItems, comparator);
                request.setAttribute("documents", fileBrowserItems);


                response.setTitle(currentDocument.getTitle());

                // Insert standard menu bar for content item
                if (WindowState.MAXIMIZED.equals(request.getWindowState())) {
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
     * Add mime-type property.
     *
     * @param document Nuxeo document
     * @param documentDTO document DTO
     */
    private void addMimeType(Document document, DocumentDTO documentDTO) {
        String icon = "file";

        PropertyMap fileContent = document.getProperties().getMap("file:content");
        if (fileContent != null) {
            try {
                MimeType mimeType = new MimeType(fileContent.getString("mime-type"));
                String primaryType = mimeType.getPrimaryType();
                String subType = mimeType.getSubType();

                if ("application".equals(primaryType)) {
                    // Application

                    if ("pdf".equals(subType)) {
                        // PDF
                        icon = "pdf";
                    } else if ("msword".equals(subType) || "vnd.openxmlformats-officedocument.wordprocessingml.document".equals(subType)) {
                        // MS Word
                        icon = "word";
                    } else if ("vnd.ms-excel".equals(subType) || "vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(subType)) {
                        // MS Excel
                        icon = "excel";
                    } else if ("vnd.ms-powerpoint".equals(subType) || "vnd.openxmlformats-officedocument.presentationml.presentation".equals(subType)) {
                        // MS Powerpoint
                        icon = "powerpoint";
                    } else if ("vnd.oasis.opendocument.text".equals(subType)) {
                        // OpenDocument - Text
                        icon = "odt";
                    } else if ("vnd.oasis.opendocument.spreadsheet".equals(subType)) {
                        // OpenDocument - Spread sheet
                        icon = "ods";
                    } else if ("vnd.oasis.opendocument.presentation".equals(subType)) {
                        // OpenDocument - Presentation
                        icon = "odp";
                    } else if ("zip".equals(subType) || "gzip".equals(subType)) {
                        // Archive
                        icon = "archive";
                    }
                } else if ("text".equals(primaryType)) {
                    // Text

                    if ("html".equals(subType) || "xml".equals(subType)) {
                        // HTML or XML
                        icon = "xml";
                    } else {
                        // Plain text
                        icon = "text";
                    }
                } else if ("image".equals(primaryType)) {
                    // Image
                    icon = "image";
                } else if ("video".equals(primaryType)) {
                    // Video
                    icon = "video";
                } else if ("audio".equals(primaryType)) {
                    // Audio
                    icon = "audio";
                }
            } catch (MimeTypeParseException e) {
                // Do nothing
            }
        }

        documentDTO.getProperties().put("mimeTypeIcon", icon);
    }

}

