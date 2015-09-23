package fr.toutatice.portail.cms.nuxeo.taglib.common;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.osivia.portal.api.locator.Locator;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoService;
import fr.toutatice.portail.cms.nuxeo.api.services.tag.INuxeoTagService;

/**
 * Toutatice simple tag.
 *
 * @author Cédric Krommenhoek
 * @see SimpleTagSupport
 */
public abstract class ToutaticeSimpleTag extends SimpleTagSupport {

    /** Document DTO. */
    private DocumentDTO document;

    /** Nuxeo service. */
    private final INuxeoService nuxeoService;
    /** Nuxeo tag service. */
    private final INuxeoTagService tagService;


    /**
     * Constructor.
     */
    public ToutaticeSimpleTag() {
        super();

        // Nuxeo service
        this.nuxeoService = Locator.findMBean(INuxeoService.class, INuxeoService.MBEAN_NAME);

        // Tag service
        this.tagService = this.nuxeoService.getTagService();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void doTag() throws JspException, IOException {
        // Page context
        PageContext pageContext = (PageContext) this.getJspContext();
        // Request
        ServletRequest request = pageContext.getRequest();

        // Nuxeo controller
        NuxeoController nuxeoController = (NuxeoController) request.getAttribute(NuxeoController.REQUEST_ATTRIBUTE);
        if (nuxeoController != null) {
            this.doTag(nuxeoController, this.document);
        } else {
            throw new JspException("NuxeoController must not be null.");
        }
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
     * Getter for nuxeoService.
     *
     * @return the nuxeoService
     */
    public INuxeoService getNuxeoService() {
        return this.nuxeoService;
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
