package fr.toutatice.portail.cms.nuxeo.portlets.move;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.internationalization.Bundle;
import org.osivia.portal.api.notifications.NotificationsType;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;
import org.osivia.portal.core.cms.CMSObjectPath;

import fr.toutatice.portail.cms.nuxeo.api.CMSPortlet;
import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.api.services.dao.DocumentDAO;
import fr.toutatice.portail.cms.nuxeo.portlets.files.MoveDocumentCommand;

/**
 * Move document portlet.
 * 
 * @author Cédric Krommenhoek
 * @see CMSPortlet
 */
public class MoveDocumentPortlet extends CMSPortlet {

    /** Document path window property name. */
    public static final String DOCUMENT_PATH_WINDOW_PROPERTY = "osivia.move.documentPath";
    /** CMS base path window property name. */
    public static final String CMS_BASE_PATH_WINDOW_PROPERTY = "osivia.move.cmsBasePath";
    /** Accepted type window property name. */
    public static final String ACCEPTED_TYPE_WINDOW_PROPERTY = "osivia.move.acceptedType";

    /** Space path request parameter name. */
    private static final String SPACE_PATH_REQUEST_PARAMETER = "osivia.move.spacePath";
    /** Mode request parameter name. */
    private static final String MODE_REQUEST_PARAMETER = "osivia.move.mode";

    /** View path. */
    private static final String VIEW_PATH = "/WEB-INF/jsp/move/view.jsp";
    /** Change space path. */
    private static final String CHANGE_SPACE_PATH = "/WEB-INF/jsp/move/change-space.jsp";


    /** Document DAO. */
    private DocumentDAO documentDAO;


    /**
     * Constructor.
     */
    public MoveDocumentPortlet() {
        super();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void init(PortletConfig config) throws PortletException {
        super.init(config);

        // DAO
        this.documentDAO = DocumentDAO.getInstance();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(request, response, this.getPortletContext());

        // Current window
        PortalWindow window = WindowFactory.getWindow(request);

        // Document path
        String documentPath = window.getProperty(DOCUMENT_PATH_WINDOW_PROPERTY);
        request.setAttribute("documentPath", documentPath);

        // CMS base path
        String cmsBasePath = request.getParameter(SPACE_PATH_REQUEST_PARAMETER);
        if (cmsBasePath == null) {
            cmsBasePath = window.getProperty(CMS_BASE_PATH_WINDOW_PROPERTY);
        }
        if (cmsBasePath != null) {
            // Computed path
            cmsBasePath = nuxeoController.getComputedPath(cmsBasePath);
            request.setAttribute("cmsBasePath", cmsBasePath);

            // Fetch space root document
            Document spaceRootDocument = nuxeoController.fetchDocument(cmsBasePath);
            DocumentDTO spaceRootDocumentDTO = this.documentDAO.toDTO(spaceRootDocument);
            request.setAttribute("spaceDocument", spaceRootDocumentDTO);
        }

        // Accepted type
        String acceptedType = window.getProperty(ACCEPTED_TYPE_WINDOW_PROPERTY);
        request.setAttribute("acceptedType", acceptedType);
        
        
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
        // Portal controller context
        PortalControllerContext portalControllerContext = nuxeoController.getPortalCtx();

        // Current window
        PortalWindow window = WindowFactory.getWindow(request);

        // Bundle
        Bundle bundle = this.getBundleFactory().getBundle(request.getLocale());

        // Action name
        String action = request.getParameter(ActionRequest.ACTION_NAME);

        if ("move".equals(action)) {
            // Document path
            String path = window.getProperty(DOCUMENT_PATH_WINDOW_PROPERTY);
            // Target document path
            String targetPath = request.getParameter("targetPath");

            if (path != null) {
                if (StringUtils.isNotBlank(targetPath)) {
                    // Fetch document
                    Document document = nuxeoController.fetchDocument(path);

                    // Fetch target document
                    Document targetDocument = nuxeoController.fetchDocument(targetPath);

                    // Parent path
                    CMSObjectPath parentPath = CMSObjectPath.parse(document.getPath()).getParent();

                    // Nuxeo command
                    INuxeoCommand command = new MoveDocumentCommand(document.getId(), targetDocument.getId());
                    nuxeoController.executeNuxeoCommand(command);

                    // Redirection URL
                    String redirectionURL = this.getPortalUrlFactory().getCMSUrl(portalControllerContext, null, parentPath.toString(), null, null, null, null,
                            null, null, null);
                    redirectionURL = this.getPortalUrlFactory().adaptPortalUrlToPopup(portalControllerContext, redirectionURL,
                            IPortalUrlFactory.POPUP_URL_ADAPTER_CLOSE);
                    request.setAttribute(Constants.PORTLET_ATTR_REDIRECTION_URL, redirectionURL);

                    // Notification
                    this.getNotificationsService().addSimpleNotification(portalControllerContext, bundle.getString("DOCUMENT_MOVE_SUCCESS_MESSAGE"),
                            NotificationsType.SUCCESS);
                } else {
                    // Redirection URL
                    String redirectionURL = this.getPortalUrlFactory().getCMSUrl(portalControllerContext, null, path, null, null, null, null, null, null, null);
                    redirectionURL = this.getPortalUrlFactory().adaptPortalUrlToPopup(portalControllerContext, redirectionURL,
                            IPortalUrlFactory.POPUP_URL_ADAPTER_CLOSE);
                    request.setAttribute(Constants.PORTLET_ATTR_REDIRECTION_URL, redirectionURL);

                    // Notification
                    this.getNotificationsService().addSimpleNotification(portalControllerContext, bundle.getString("DOCUMENT_MOVE_WARNING_MESSAGE"),
                            NotificationsType.WARNING);
                }
            }
        } else if ("changeSpace".equals(action)) {
            // Space path
            String spacePath = request.getParameter("spacePath");
            response.setRenderParameter(SPACE_PATH_REQUEST_PARAMETER, spacePath);
        }
    }

}
