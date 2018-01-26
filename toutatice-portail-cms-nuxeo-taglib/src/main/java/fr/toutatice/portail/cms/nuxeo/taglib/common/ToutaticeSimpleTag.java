package fr.toutatice.portail.cms.nuxeo.taglib.common;

import java.io.IOException;

import javax.portlet.PortletRequest;
import javax.servlet.jsp.JspException;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.taglib.common.PortalSimpleTag;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.cms.NuxeoDocumentContext;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoServiceFactory;
import fr.toutatice.portail.cms.nuxeo.api.services.dao.DocumentDAO;
import fr.toutatice.portail.cms.nuxeo.api.services.tag.INuxeoTagService;

/**
 * Toutatice simple tag.
 *
 * @author Cédric Krommenhoek
 * @see PortalSimpleTag
 */
public abstract class ToutaticeSimpleTag extends PortalSimpleTag {

    /** Nuxeo controller request attribute. */
    private static final String NUXEO_CONTROLLER_REQUEST_ATTRIBUTE = NuxeoController.class.getName();


    /** Document DTO. */
    private DocumentDTO document;
    /** Path. */
    private String path;


    /** Nuxeo tag service. */
    private final INuxeoTagService tagService;
    /** Document DAO. */
    private final DocumentDAO dao;


    /**
     * Constructor.
     */
    public ToutaticeSimpleTag() {
        super();

        // Tag service
        this.tagService = NuxeoServiceFactory.getTagService();
        // Document DAO
        this.dao = DocumentDAO.getInstance();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void doTag() throws JspException, IOException {
        // Portal controller context
        PortalControllerContext portalControllerContext = this.getPortalControllerContext();
        // Nuxeo controller
        NuxeoController nuxeoController = this.getNuxeoController(portalControllerContext);

        if ((this.document == null) && StringUtils.isNotBlank(this.path)) {
            NuxeoDocumentContext documentContext = nuxeoController.getDocumentContext(this.path);
            Document document = documentContext.getDocument();
            nuxeoController.setCurrentDoc(document);

            this.document = this.dao.toDTO(portalControllerContext, document);
        }

        this.doTag(nuxeoController, this.document);
    }


    /**
     * Get Nuxeo controller.
     * 
     * @param portalControllerContext portal controller context
     * @return Nuxeo controller
     * @throws JspException
     */
    protected NuxeoController getNuxeoController(PortalControllerContext portalControllerContext) throws JspException {
        // Portlet request
        PortletRequest request = this.getPortletRequest();
        
        // Get Nuxeo controller in portlet request
        NuxeoController nuxeoController;
        if (request == null) {
            nuxeoController = null;
        } else {
            nuxeoController = (NuxeoController) request.getAttribute(NUXEO_CONTROLLER_REQUEST_ATTRIBUTE);
        }

        if (nuxeoController == null) {
            // Instanciated new Nuxeo controller
            nuxeoController = new NuxeoController(portalControllerContext);

            if (request != null) {
                request.setAttribute(NUXEO_CONTROLLER_REQUEST_ATTRIBUTE, nuxeoController);
            }
        }

        // Current Nuxeo document
        if (this.document != null) {
            Document nuxeoDocument = this.document.getDocument();
            nuxeoController.setCurrentDoc(nuxeoDocument);
        } else {
            nuxeoController.setCurrentDoc(null);
        }

        return nuxeoController;
    }


    /**
     * Do tag.
     *
     * @param nuxeoController Nuxeo controller
     * @param document document DTO
     * @throws JspException
     * @throws IOException
     */
    protected abstract void doTag(NuxeoController nuxeoController, DocumentDTO document) throws JspException, IOException;


    /**
     * Setter for document.
     *
     * @param document the document to set
     */
    public void setDocument(DocumentDTO document) {
        this.document = document;
    }

    /**
     * Setter for path.
     * 
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Getter for tagService.
     *
     * @return the tagService
     */
    public INuxeoTagService getTagService() {
        return this.tagService;
    }

}
