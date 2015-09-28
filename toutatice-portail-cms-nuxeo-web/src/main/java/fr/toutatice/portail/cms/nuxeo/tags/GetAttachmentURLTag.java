package fr.toutatice.portail.cms.nuxeo.tags;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;

/**
 * Get Nuxeo document attachment URL tag.
 *
 * @author Cédric Krommenhoek
 * @see SimpleTagSupport
 */
public class GetAttachmentURLTag extends SimpleTagSupport {

    /** Document DTO. */
    private DocumentDTO document;
    /** Attachement index. */
    private int index;


    /**
     * Constructor.
     */
    public GetAttachmentURLTag() {
        super();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void doTag() throws JspException, IOException {
        // Context
        PageContext pageContext = (PageContext) this.getJspContext();
        // Request
        ServletRequest request = pageContext.getRequest();
        // Nuxeo controller
        NuxeoController nuxeoController = (NuxeoController) request.getAttribute(NuxeoController.REQUEST_ATTRIBUTE);

        if (nuxeoController != null) {
            // URL
            String url = nuxeoController.createAttachedFileLink(this.document.getPath(), String.valueOf(this.index));

            JspWriter out = pageContext.getOut();
            out.write(url);
        }
    }


    /**
     * Setter for document.
     *
     * @param document the document to set
     */
    public void setDocument(DocumentDTO document) {
        this.document = document;
    }

    /**
     * Setter for index.
     *
     * @param index the index to set
     */
    public void setIndex(int index) {
        this.index = index;
    }

}
