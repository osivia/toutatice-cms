package fr.toutatice.portail.cms.nuxeo.portlets.rename;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.notifications.NotificationsType;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;

import fr.toutatice.portail.cms.nuxeo.api.CMSPortlet;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.cms.NuxeoDocumentContext;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.api.services.dao.DocumentDAO;


/**
 * Rename document portlet
 *
 * @author Dorian Licois
 */
public class RenamePortlet extends CMSPortlet {

    /** View JSP path. */
    private static final String PATH_VIEW = "/WEB-INF/jsp/rename/view.jsp";

    public static final String DOCUMENT_REDIRECT_PATH_WINDOW_PROPERTY = "osivia.rename.document.redirect.path";

    public RenamePortlet() {
        super();
    }

    @Override
    public void init(PortletConfig config) throws PortletException {
        super.init(config);
    }

    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        // Current window
        PortalWindow window = WindowFactory.getWindow(request);
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(request, response, this.getPortletContext());
        // Current Document
        Document currentDocument = getCurrentDocument(window, nuxeoController);

        request.setAttribute("currentDocTitle", currentDocument.getTitle());
        DocumentDTO currentDocumentDto = DocumentDAO.getInstance().toDTO(currentDocument);
        request.setAttribute("docIcon", currentDocumentDto.getIcon());
        request.setAttribute("error", request.getParameter("error"));
        response.setContentType("text/html");

        PortletRequestDispatcher dispatcher = getPortletContext().getRequestDispatcher(PATH_VIEW);
        dispatcher.include(request, response);
    }

    @Override
    public void processAction(ActionRequest request, ActionResponse response) throws PortletException, IOException {
        // Action name
        String action = request.getParameter(ActionRequest.ACTION_NAME);

        if (PortletMode.VIEW.equals(request.getPortletMode())) {
            if ("renameDoc".equals(action)) {
                // Current window
                PortalWindow window = WindowFactory.getWindow(request);

                // Nuxeo controller
                NuxeoController nuxeoController = new NuxeoController(request, response, this.getPortletContext());

                // Current Document
                Document currentDocument = getCurrentDocument(window, nuxeoController);

                // new title
                String newDocTitle = request.getParameter("newDocTitle");

                if (StringUtils.isNotBlank(newDocTitle)) {
                    Document updatedDcoument = (Document) nuxeoController.executeNuxeoCommand(new RenameCommand(currentDocument, newDocTitle));
                    addNotification(nuxeoController.getPortalCtx(), "RENAME_DOCUMENT_SUCCESS", NotificationsType.SUCCESS);
                    // refresh portlet cache
                    nuxeoController.getDocumentContext(updatedDcoument.getPath(), true);
                    // Redirect
                    String redirectPath = window.getProperty(DOCUMENT_REDIRECT_PATH_WINDOW_PROPERTY);
                    if (StringUtils.isEmpty(redirectPath)) {
                        redirectPath = updatedDcoument.getPath();
                    }
                    String redirectionUrl = nuxeoController.getPortalUrlFactory().getCMSUrl(nuxeoController.getPortalCtx(), null, redirectPath, null, null,
                            IPortalUrlFactory.DISPLAYCTX_REFRESH, null, null, null, null);
                    response.sendRedirect(redirectionUrl);
                } else {
                    response.setRenderParameter("error", getBundleFactory().getBundle(request.getLocale()).getString("RENAME_DOCUMENT_TITLE_REQUIRED"));
                }
            }
        }
    }

    /**
     * @param window
     * @param nuxeoController
     * @return
     */
    private Document getCurrentDocument(PortalWindow window, NuxeoController nuxeoController) {
        String path = window.getProperty(Constants.WINDOW_PROP_URI);
        path = nuxeoController.getComputedPath(path);
        NuxeoDocumentContext documentContext = nuxeoController.getDocumentContext(path, true);
        Document currentDocument = documentContext.getDoc();
        return currentDocument;
    }
}
