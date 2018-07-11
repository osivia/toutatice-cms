package fr.toutatice.portail.cms.nuxeo.portlets.move;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.cms.DocumentState;
import org.osivia.portal.api.cms.DocumentType;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.internationalization.Bundle;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.notifications.NotificationsType;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSObjectPath;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.ICMSService;

import fr.toutatice.portail.cms.nuxeo.api.CMSPortlet;
import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.cms.NuxeoDocumentContext;
import fr.toutatice.portail.cms.nuxeo.api.cms.NuxeoPublicationInfos;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoService;
import fr.toutatice.portail.cms.nuxeo.api.services.dao.DocumentDAO;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.DefaultCMSCustomizer;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.BrowserAdapter;
import fr.toutatice.portail.cms.nuxeo.portlets.files.MoveDocumentCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.publish.RequestPublishStatus;

/**
 * Move document portlet.
 *
 * @author Cédric Krommenhoek
 * @see CMSPortlet
 */
public class MoveDocumentPortlet extends CMSPortlet {

    /** Document path window property name. */
    public static final String DOCUMENT_PATH_WINDOW_PROPERTY = "osivia.move.documentPath";
    /** Documents identifiers window property name. */
    public static final String DOCUMENTS_IDENTIFIERS_WINDOW_PROPERTY = "osivia.move.documentsIdentifiers";
    /** Ignored paths window property name. */
    public static final String IGNORED_PATHS_WINDOW_PROPERTY = "osivia.move.ignoredPaths";
    /** CMS base path window property name. */
    public static final String CMS_BASE_PATH_WINDOW_PROPERTY = "osivia.move.cmsBasePath";
    /** Accepted types window property name. */
    public static final String ACCEPTED_TYPES_WINDOW_PROPERTY = "osivia.move.acceptedTypes";

    /** Space path request parameter name. */
    private static final String SPACE_PATH_REQUEST_PARAMETER = "osivia.move.spacePath";
    /** Mode request parameter name. */
    private static final String MODE_REQUEST_PARAMETER = "osivia.move.mode";

    /** View path. */
    private static final String VIEW_PATH = "/WEB-INF/jsp/move/view.jsp";
    /** Change space path. */
    private static final String CHANGE_SPACE_PATH = "/WEB-INF/jsp/move/change-space.jsp";

    /** Nuxeo service. */
    private final INuxeoService nuxeoService;
    /** Document DAO. */
    private final DocumentDAO documentDAO;


    /**
     * Constructor.
     */
    public MoveDocumentPortlet() {
        super();

        // Nuxeo service
        this.nuxeoService = Locator.findMBean(INuxeoService.class, INuxeoService.MBEAN_NAME);
        // DAO
        this.documentDAO = DocumentDAO.getInstance();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        // Portal controller context
        PortalControllerContext portalControllerContext = new PortalControllerContext(this.getPortletContext(), request, response);
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(portalControllerContext);
        nuxeoController.setDisplayLiveVersion(RequestPublishStatus.live.getStatus());

        // CMS customizer
        DefaultCMSCustomizer cmsCustomizer = (DefaultCMSCustomizer) this.nuxeoService.getCMSCustomizer();
        // Browser adapter
        BrowserAdapter browserAdapter = cmsCustomizer.getBrowserAdapter();

        // CMS service
        ICMSService cmsService = NuxeoController.getCMSService();
        // CMS context
        CMSServiceCtx cmsContext = nuxeoController.getCMSCtx();

        // Current window
        PortalWindow window = WindowFactory.getWindow(request);

        // CMS base path
        String basePath = request.getParameter(SPACE_PATH_REQUEST_PARAMETER);
        if (basePath == null) {
            basePath = window.getProperty(CMS_BASE_PATH_WINDOW_PROPERTY);
        }
        if (basePath != null) {
            // Computed path
            basePath = nuxeoController.getComputedPath(basePath);

            // Root document
            Document space = this.getRootDocument(cmsContext, basePath);
            basePath = space.getPath();

            // Root document DTO
            DocumentDTO spaceDto = this.documentDAO.toDTO(space);
            request.setAttribute("spaceDocument", spaceDto);

            request.setAttribute("cmsBasePath", basePath);
        }


        // Document path
        String documentPath = window.getProperty(DOCUMENT_PATH_WINDOW_PROPERTY);

        // Navigation item
        CMSItem navigationItem;
        if (documentPath == null) {
            navigationItem = null;
        } else {
            try {
                navigationItem = cmsService.getPortalNavigationItem(cmsContext, basePath, documentPath);
                if (navigationItem == null) {
                    CMSObjectPath objectPath = CMSObjectPath.parse(documentPath);
                    CMSObjectPath parentObjectPath = objectPath.getParent();
                    navigationItem = cmsService.getPortalNavigationItem(cmsContext, basePath, parentObjectPath.toString());
                }
            } catch (CMSException e) {
                throw new PortletException(e);
            }

        }

        // Navigation path
        if (navigationItem != null) {
            String navigationPath = navigationItem.getNavigationPath();
            request.setAttribute("cmsNavigationPath", navigationPath);
        }

        // CMS item
        CMSItem cmsItem;
        if ((navigationItem != null) && StringUtils.equals(documentPath, navigationItem.getNavigationPath())) {
            cmsItem = navigationItem;
        } else if (StringUtils.isNotEmpty(documentPath)) {
            try {
                cmsItem = cmsService.getContent(cmsContext, documentPath);
            } catch (CMSException e) {
                throw new PortletException(e);
            }
        } else {
            cmsItem = null;
        }

        // Document type
        if (cmsItem != null) {
            DocumentType documentType = cmsItem.getType();
            boolean enableSpaceChange = (documentType != null) && !documentType.isFolderish();
            request.setAttribute("enableSpaceChange", enableSpaceChange);
        }

        // Ignored paths
        String ignoredPaths = window.getProperty(IGNORED_PATHS_WINDOW_PROPERTY);
        if (ignoredPaths == null) {
            ignoredPaths = documentPath;
        }
        request.setAttribute("ignoredPaths", ignoredPaths);

        // Accepted types
        String acceptedTypes = window.getProperty(ACCEPTED_TYPES_WINDOW_PROPERTY);
        request.setAttribute("acceptedTypes", acceptedTypes);

        // Excluded types
        String excludedTypes;
        if (StringUtils.startsWith(basePath, browserAdapter.getUserWorkspacesPath())) {
            excludedTypes = browserAdapter.getUserWorkspacesType();
        } else {
            excludedTypes = null;
        }
        request.setAttribute("excludedTypes", excludedTypes);

        // Error
        String error = request.getParameter("error");
        request.setAttribute("error", error);


        // Dispatcher path
        String dispatcherPath;
        if ("space".equals(request.getParameter(MODE_REQUEST_PARAMETER))) {
            dispatcherPath = CHANGE_SPACE_PATH;
        } else {
            dispatcherPath = VIEW_PATH;
        }

        response.setContentType("text/html");
        this.getPortletContext().getRequestDispatcher(dispatcherPath).include(request, response);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void processAction(ActionRequest request, ActionResponse response) throws PortletException, IOException {
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(request, response, this.getPortletContext());
        nuxeoController.setDisplayLiveVersion(DocumentState.LIVE.toString());
        // Portal controller context
        PortalControllerContext portalControllerContext = nuxeoController.getPortalCtx();

        // Current window
        PortalWindow window = WindowFactory.getWindow(request);

        // Bundle
        Bundle bundle = this.getBundleFactory().getBundle(request.getLocale());

        // Action name
        String action = request.getParameter(ActionRequest.ACTION_NAME);

        if ("move".equals(action)) {
            // Target document path
            String targetPath = request.getParameter("targetPath");

            if (StringUtils.isBlank(targetPath)) {
                // Empty target path
                response.setRenderParameter("error", "emptyTargetPath");
            } else {
                // Document path
                String path = window.getProperty(DOCUMENT_PATH_WINDOW_PROPERTY);
                // Base path
                String basePath = window.getProperty(CMS_BASE_PATH_WINDOW_PROPERTY);

                // CMS context
                CMSServiceCtx cmsContext = nuxeoController.getCMSCtx();

                // Nuxeo document context
                NuxeoDocumentContext documentContext = nuxeoController.getDocumentContext(path);
                // Nuxeo document
                Document document = documentContext.getDocument();

                // Documents identifiers
                String[] identifiersProperty = StringUtils.split(window.getProperty(DOCUMENTS_IDENTIFIERS_WINDOW_PROPERTY), ",");

                // Source identifiers
                List<String> sourceIds;
                if (identifiersProperty == null) {
                    sourceIds = new ArrayList<String>(1);
                    sourceIds.add(document.getId());
                } else {
                    sourceIds = Arrays.asList(identifiersProperty);
                }


                // Target Nuxeo document context
                NuxeoDocumentContext targetDocumentContext = nuxeoController.getDocumentContext(targetPath);
                // Target publication infos
                NuxeoPublicationInfos targetPublicationInfos = targetDocumentContext.getPublicationInfos();
                // Target Nuxeo document identifier
                String targetId = targetPublicationInfos.getLiveId();


                // Redirection path
                String redirectionPath;
                if (identifiersProperty == null) {
                    CMSObjectPath parentPath = CMSObjectPath.parse(document.getPath()).getParent();
                    redirectionPath = parentPath.toString();
                } else {
                    redirectionPath = document.getPath();
                }


                // Nuxeo command
                INuxeoCommand command = new MoveDocumentCommand(sourceIds, targetId);

                try {
                    nuxeoController.executeNuxeoCommand(command);

                    // Source root document
                    Document sourceRoot = this.getRootDocument(cmsContext, basePath);
                    // Target root document
                    Document targetRoot = this.getRootDocument(cmsContext, targetPath);

                    if (!StringUtils.equals(sourceRoot.getPath(), targetRoot.getPath())) {
                        // Update ACLs
                        command = new RemoveAllPermissionsCommand(sourceIds);
                        nuxeoController.executeNuxeoCommand(command);
                    }

                    // Redirection URL
                    String redirectionURL = this.getPortalUrlFactory().getCMSUrl(portalControllerContext, null, redirectionPath, null, null,
                            IPortalUrlFactory.DISPLAYCTX_REFRESH, null, null, null, null);
                    redirectionURL = this.getPortalUrlFactory().adaptPortalUrlToPopup(portalControllerContext, redirectionURL,
                            IPortalUrlFactory.POPUP_URL_ADAPTER_CLOSE);
                    request.setAttribute(Constants.PORTLET_ATTR_REDIRECTION_URL, redirectionURL);

                    // Notification
                    String message;
                    if (sourceIds.size() == 1) {
                        message = bundle.getString("DOCUMENT_MOVE_SUCCESS_MESSAGE");
                    } else {
                        message = bundle.getString("DOCUMENTS_MOVE_SUCCESS_MESSAGE", sourceIds.size());
                    }
                    this.getNotificationsService().addSimpleNotification(portalControllerContext, message, NotificationsType.SUCCESS);
                } catch (NuxeoException e) {
                    if (NuxeoException.ERROR_FORBIDDEN == e.getErrorCode()) {
                        response.setRenderParameter("error", "403");
                    } else {
                        // Notification
                        String message = bundle.getString("DOCUMENT_MOVE_ERROR_MESSAGE");
                        this.getNotificationsService().addSimpleNotification(portalControllerContext, message, NotificationsType.ERROR);
                    }
                }
            }

        } else if ("changeSpace".equals(action)) {
            // Space path
            String spacePath = request.getParameter("spacePath");
            response.setRenderParameter(SPACE_PATH_REQUEST_PARAMETER, spacePath);
        }
    }


    /**
     * Get root document.
     * 
     * @param cmsContext CMS context
     * @param basePath base path
     * @return document
     * @throws PortletException
     */
    private Document getRootDocument(CMSServiceCtx cmsContext, String basePath) throws PortletException {
        // CMS service
        ICMSService cmsService = NuxeoController.getCMSService();

        // Current space document
        Document currentSpace = null;
        // Root document
        Document root = null;

        String path = basePath;
        while ((root == null) && StringUtils.isNotEmpty(path)) {
            // Space config
            CMSItem spaceConfig;
            try {
                spaceConfig = cmsService.getSpaceConfig(cmsContext, path);
            } catch (CMSException e) {
                throw new PortletException(e);
            }

            if (currentSpace == null) {
                currentSpace = (Document) spaceConfig.getNativeItem();
            }

            // Document type
            DocumentType spaceType = spaceConfig.getType();

            if ((spaceType != null) && spaceType.isRoot()) {
                root = (Document) spaceConfig.getNativeItem();
            }

            // Loop on parent path
            CMSObjectPath objectPath = CMSObjectPath.parse(path);
            CMSObjectPath parentObjectPath = objectPath.getParent();
            path = parentObjectPath.toString();
        }

        if (root == null) {
            root = currentSpace;
        }

        return root;
    }

}
