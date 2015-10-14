package fr.toutatice.portail.cms.test.common;

import java.io.IOException;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.ICMSService;

import fr.toutatice.portail.cms.nuxeo.api.CMSPortlet;
import fr.toutatice.portail.cms.nuxeo.api.ContextualizationHelper;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.cms.NuxeoDocumentContext;
import fr.toutatice.portail.cms.nuxeo.api.domain.CommentDTO;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCommentsService;
import fr.toutatice.portail.cms.nuxeo.api.services.dao.DocumentDAO;
import fr.toutatice.portail.cms.test.common.model.Configuration;
import fr.toutatice.portail.cms.test.common.model.Tab;
import fr.toutatice.portail.cms.test.common.service.ITestRepository;
import fr.toutatice.portail.cms.test.common.service.TestRepositoryImpl;

/**
 * Test portlet.
 *
 * @author Cédric Krommenhoek
 * @see CMSPortlet
 */
public class TestPortlet extends CMSPortlet {

    /** Path prefix. */
    private static final String PATH_PREFIX = "/WEB-INF/jsp/view-";
    /** Path suffix. */
    private static final String PATH_SUFFIX = ".jsp";

    /** Test repository. */
    private final ITestRepository repository;
    /** Document DAO. */
    private final DocumentDAO documentDao;


    /**
     * Constructor.
     */
    public TestPortlet() {
        super();
        this.repository = TestRepositoryImpl.getInstance();
        this.documentDao = DocumentDAO.getInstance();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        // Portal controller context
        PortalControllerContext portalControllerContext = new PortalControllerContext(this.getPortletContext(), request, response);
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(request, response, this.getPortletContext());

        // CMS service
        ICMSService cmsService = NuxeoController.getCMSService();
        // CMS context
        CMSServiceCtx cmsContext = nuxeoController.getCMSCtx();

        // Configuration
        Configuration configuration = this.repository.getConfiguration(portalControllerContext);
        request.setAttribute("configuration", configuration);

        // Document
        if (StringUtils.isNotBlank(configuration.getPath())) {
            // Computed path
            String path = nuxeoController.getComputedPath(configuration.getPath());
            // Document context
            NuxeoDocumentContext documentContext = NuxeoController.getDocumentContext(request, response, this.getPortletContext(), path);
            // Nuxeo document
            Document nuxeoDocument = documentContext.getDoc();
            // Document DTO
            DocumentDTO document = this.documentDao.toDTO(nuxeoDocument);
            request.setAttribute("document", document);

            // Comments
            try {
                CMSPublicationInfos publicationInfos = cmsService.getPublicationInfos(cmsContext, path);
                if (ContextualizationHelper.isCurrentDocContextualized(cmsContext) && publicationInfos.isCommentableByUser()) {
                    INuxeoCommentsService commentsService = nuxeoController.getNuxeoCommentsService();
                    List<CommentDTO> comments = commentsService.getDocumentComments(cmsContext, nuxeoDocument);

                    document.setCommentable(true);
                    document.getComments().addAll(comments);
                }
            } catch (CMSException e) {
                throw new PortletException(e);
            }


        }

        // Tabs
        Tab[] tabs = Tab.values();
        request.setAttribute("tabs", tabs);

        // Current tab
        Tab currentTab = Tab.fromId(request.getParameter("tab"));
        PortletRequestDispatcher dispatcher = this.getPortletContext().getRequestDispatcher(PATH_PREFIX + currentTab.getId() + PATH_SUFFIX);

        // Response
        response.setContentType("text/html");
        dispatcher.include(request, response);
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
        // Portal controller context
        PortalControllerContext portalControllerContext = new PortalControllerContext(this.getPortletContext(), request, response);

        // Configuration
        Configuration configuration = this.repository.getConfiguration(portalControllerContext);
        request.setAttribute("configuration", configuration);

        response.setContentType("text/html");
        this.getPortletContext().getRequestDispatcher("/WEB-INF/jsp/admin.jsp").include(request, response);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void processAction(ActionRequest request, ActionResponse response) throws PortletException, IOException {
        // Portal controller context
        PortalControllerContext portalControllerContext = new PortalControllerContext(this.getPortletContext(), request, response);
        // Action name
        String action = request.getParameter(ActionRequest.ACTION_NAME);

        if ("admin".equals(request.getPortletMode().toString())) {
            // Admin

            if ("save".equals(action)) {
                // Save configuration
                Configuration configuration = new Configuration();
                configuration.setPath(request.getParameter("path"));
                configuration.setUser(request.getParameter("user"));

                this.repository.setConfiguration(portalControllerContext, configuration);
            }

            response.setPortletMode(PortletMode.VIEW);
            response.setWindowState(WindowState.NORMAL);

        } else if (PortletMode.VIEW.equals(request.getPortletMode())) {
            // View

            // Comment action
            this.processCommentAction(request, response);

            // Current tab
            response.setRenderParameter("tab", StringUtils.trimToEmpty(request.getParameter("tab")));
        }
    }

}
