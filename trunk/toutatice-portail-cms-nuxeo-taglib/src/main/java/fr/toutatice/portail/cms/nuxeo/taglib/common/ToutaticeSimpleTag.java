package fr.toutatice.portail.cms.nuxeo.taglib.common;

import java.io.IOException;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.servlet.jsp.JspException;

import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.taglib.common.PortalSimpleTag;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoServiceFactory;
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

    /** Nuxeo tag service. */
    private final INuxeoTagService tagService;


    /**
     * Constructor.
     */
    public ToutaticeSimpleTag() {
        super();

        // Tag service
        this.tagService = NuxeoServiceFactory.getTagService();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void doTag() throws JspException, IOException {
        // Nuxeo controller
        NuxeoController nuxeoController = this.getNuxeoController();

        this.doTag(nuxeoController, this.document);
    }


    /**
     * Get Nuxeo controller.
     * 
     * @return Nuxeo controller
     * @throws JspException
     */
    protected NuxeoController getNuxeoController() throws JspException {
        // Portlet request
        PortletRequest request = this.getPortletRequest();
        
        // Get Nuxeo controller in portlet request
        NuxeoController nuxeoController = (NuxeoController) request.getAttribute(NUXEO_CONTROLLER_REQUEST_ATTRIBUTE);

        if (nuxeoController == null) {
            // Portlet response
            PortletResponse response = this.getPortletResponse();
            // Portlet context
            PortletContext portletContext = this.getPortletContext();

            // Instanciated new Nuxeo controller
            nuxeoController = new NuxeoController(request, response, portletContext);
            request.setAttribute(NUXEO_CONTROLLER_REQUEST_ATTRIBUTE, nuxeoController);
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
     * Getter for tagService.
     *
     * @return the tagService
     */
    public INuxeoTagService getTagService() {
        return this.tagService;
    }

}
