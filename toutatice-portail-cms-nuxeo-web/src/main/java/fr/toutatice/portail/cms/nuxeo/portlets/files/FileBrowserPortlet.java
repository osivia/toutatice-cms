package fr.toutatice.portail.cms.nuxeo.portlets.files;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.osivia.portal.api.Constants;
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

    /** Nuxeo path window property name. */
    private static final String NUXEO_PATH_WINDOW_PROPERTY = "osivia.nuxeoPath";

    /** Admin JSP path. */
    private static final String PATH_ADMIN = "/WEB-INF/jsp/files/admin.jsp";
    /** View JSP path. */
    private static final String PATH_VIEW = "/WEB-INF/jsp/files/view.jsp";
    /** Error JSP path. */
    private static final String PATH_ERROR = "/WEB-INF/jsp/files/error.jsp";


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

        if ("admin".equals(request.getPortletMode().toString())) {
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
                Document document = nuxeoController.fetchDocument(path);
                nuxeoController.setCurrentDoc(document);
                request.setAttribute("document", this.documentDAO.toDTO(document));

                // Publication informations
                CMSPublicationInfos publicationInfos = cmsService.getPublicationInfos(cmsContext, path);

                // Fetch Nuxeo children documents
                INuxeoCommand command = new GetFolderFilesCommand(publicationInfos.getLiveId());
                Documents documents = (Documents) nuxeoController.executeNuxeoCommand(command);

                // Sorted documents
                List<Document> sortedDocuments = documents.list();
                CMSItemType cmsItemType = nuxeoController.getCMSItemTypes().get(document.getType());
                if ((cmsItemType == null) || !cmsItemType.isOrdered()) {
                    Comparator<Document> comparator = new FileBrowserComparator(nuxeoController);
                    Collections.sort(sortedDocuments, comparator);
                }

                // Documents DTO
                List<DocumentDTO> documentsDTO = new ArrayList<DocumentDTO>(sortedDocuments.size());
                for (Document sortedDocument : sortedDocuments) {
                    DocumentDTO documentDTO = this.documentDAO.toDTO(sortedDocument);
                    documentsDTO.add(documentDTO);
                }
                request.setAttribute("documents", documentsDTO);

                // Insert standard menu bar for content item
                nuxeoController.insertContentMenuBarItems();
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

}
