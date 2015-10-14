package fr.toutatice.portail.cms.nuxeo.portlets.reorder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.ICMSService;

import fr.toutatice.portail.cms.nuxeo.api.CMSPortlet;
import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.api.services.dao.DocumentDAO;
import fr.toutatice.portail.cms.nuxeo.portlets.files.SortDocumentCommand;

/**
 * Reorder documents portlet.
 *
 * @author Cédric Krommenhoek
 * @see CMSPortlet
 */
public class ReorderDocumentsPortlet extends CMSPortlet {

    /** Path window property name. */
    public static final String PATH_WINDOW_PROPERTY = "osivia.reorder.path";


    /** View path. */
    private static final String VIEW_PATH = "/WEB-INF/jsp/reorder/view.jsp";


    /** Document DAO. */
    private DocumentDAO documentDAO;


    /**
     * {@inheritDoc}
     */
    public ReorderDocumentsPortlet() {
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
        // Portal controller context
        PortalControllerContext portalControllerContext = nuxeoController.getPortalCtx();
        // CMS service
        ICMSService cmsService = NuxeoController.getCMSService();
        // CMS context
        CMSServiceCtx cmsContext = new CMSServiceCtx();
        cmsContext.setPortalControllerContext(portalControllerContext);
        cmsContext.setDisplayLiveVersion("1");

        // Current window
        PortalWindow window = WindowFactory.getWindow(request);

        // Path
        String path = window.getProperty(PATH_WINDOW_PROPERTY);


        // Children
        List<CMSItem> children;
        try {
            children = cmsService.getPortalSubitems(cmsContext, path);
        } catch (CMSException e) {
            throw new PortletException(e);
        }


        // DTO
        List<DocumentDTO> documentsDTO = new ArrayList<DocumentDTO>(children.size());
        for (CMSItem child : children) {
            Document document = (Document) child.getNativeItem();
            DocumentDTO documentDTO = this.documentDAO.toDTO(document);
            documentsDTO.add(documentDTO);
        }
        request.setAttribute("documents", documentsDTO);


        response.setContentType("text/html");
        this.getPortletContext().getRequestDispatcher(VIEW_PATH).include(request, response);
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
        // Path
        String path = window.getProperty(PATH_WINDOW_PROPERTY);

        // Action name
        String action = request.getParameter(ActionRequest.ACTION_NAME);

        if ("reorder".equals(action)) {
            // Order parameter
            String orderParameter = request.getParameter("order");
            String[] order = StringUtils.split(orderParameter, "|");

            for (int i = (order.length - 1); i >= 0; i--) {
                // Source identifier
                String sourceId = order[i];

                // Target identifier
                String targetId;
                if (i == (order.length - 1)) {
                    targetId = null;
                } else {
                    targetId = order[i + 1];
                }

                // Nuxeo command
                INuxeoCommand command = new SortDocumentCommand(sourceId, targetId);
                nuxeoController.executeNuxeoCommand(command);
            }


            // Redirection URL
            String redirectionURL = this.getPortalUrlFactory().getCMSUrl(portalControllerContext, null, path, null, null, IPortalUrlFactory.DISPLAYCTX_REFRESH,
                    null, null, null, null);
            redirectionURL = this.getPortalUrlFactory().adaptPortalUrlToPopup(portalControllerContext, redirectionURL,
                    IPortalUrlFactory.POPUP_URL_ADAPTER_CLOSE);
            request.setAttribute(Constants.PORTLET_ATTR_REDIRECTION_URL, redirectionURL);
        }
    }

}
