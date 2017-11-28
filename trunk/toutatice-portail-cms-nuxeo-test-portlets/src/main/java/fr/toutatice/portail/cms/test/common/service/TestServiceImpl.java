package fr.toutatice.portail.cms.test.common.service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.selection.ISelectionService;
import org.osivia.portal.api.selection.SelectionItem;
import org.osivia.portal.api.sequencing.IPortletSequencingService;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.ICMSService;

import fr.toutatice.portail.cms.nuxeo.api.ContextualizationHelper;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.cms.NuxeoDocumentContext;
import fr.toutatice.portail.cms.nuxeo.api.domain.CommentDTO;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCommentsService;
import fr.toutatice.portail.cms.nuxeo.api.services.dao.DocumentDAO;
import fr.toutatice.portail.cms.test.common.model.Configuration;

/**
 * Test service implementation.
 *
 * @author Cédric Krommenhoek
 * @see ITestService
 */
public class TestServiceImpl implements ITestService {

    /** Singleton instance. */
    private static TestServiceImpl instance;


    /** Test repository. */
    private final ITestRepository repository;
    /** Document DAO. */
    private final DocumentDAO documentDao;
    /** Selection service. */
    private final ISelectionService selectionService;
    /** Portlet sequencing service. */
    private final IPortletSequencingService portletSequencingService;


    /**
     * Constructor.
     */
    protected TestServiceImpl() {
        super();
        this.repository = TestRepositoryImpl.getInstance();
        this.documentDao = DocumentDAO.getInstance();
        this.selectionService = Locator.findMBean(ISelectionService.class, ISelectionService.MBEAN_NAME);
        this.portletSequencingService = Locator.findMBean(IPortletSequencingService.class, IPortletSequencingService.MBEAN_NAME);
    }


    /**
     * Get singleton instance.
     *
     * @return singleton instance
     */
    public static ITestService getInstance() {
        if (instance == null) {
            instance = new TestServiceImpl();
        }
        return instance;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void injectTagsData(PortalControllerContext portalControllerContext, Configuration configuration) throws PortletException {
        // Request
        PortletRequest request = portalControllerContext.getRequest();
        // Response
        PortletResponse response = portalControllerContext.getResponse();
        // Portlet context
        PortletContext portletContext = portalControllerContext.getPortletCtx();

        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(request, response, portletContext);
        // CMS service
        ICMSService cmsService = NuxeoController.getCMSService();
        // CMS context
        CMSServiceCtx cmsContext = nuxeoController.getCMSCtx();


        // Document
        if (StringUtils.isNotBlank(configuration.getPath())) {
            // Computed path
            String path = nuxeoController.getComputedPath(configuration.getPath());
            // Document context
            NuxeoDocumentContext documentContext = nuxeoController.getDocumentContext(path);
            // Nuxeo document
            Document nuxeoDocument = documentContext.getDocument();
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
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void injectAttributesStorageData(PortalControllerContext portalControllerContext, Configuration configuration) throws PortletException {
        // Request
        PortletRequest request = portalControllerContext.getRequest();

        // Window
        PortalWindow window = WindowFactory.getWindow(request);


        // Sequence priority
        request.setAttribute("priority", window.getProperty("osivia.sequence.priority"));

        // Last refresh date
        request.setAttribute("lastRefresh", new Date());

        // Storage attributes
        request.setAttribute("storageAttributes", this.portletSequencingService.getAttributes(portalControllerContext));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void addToSelection(PortalControllerContext portalControllerContext, String content) throws PortletException {
        // Configuration
        Configuration configuration = this.repository.getConfiguration(portalControllerContext);

        // Selection identifier
        String selectionId = configuration.getSelectionId();
        // Selection item
        SelectionItem item = new SelectionItem(UUID.randomUUID().toString(), content, null);

        this.selectionService.addItem(portalControllerContext, selectionId, item);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void addToStorage(PortalControllerContext portalControllerContext, String name, String value) throws PortletException {
        this.portletSequencingService.setAttribute(portalControllerContext, name, value);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void removeFromStorage(PortalControllerContext portalControllerContext, String name) throws PortletException {
        this.portletSequencingService.removeAttribute(portalControllerContext, name);
    }

}
