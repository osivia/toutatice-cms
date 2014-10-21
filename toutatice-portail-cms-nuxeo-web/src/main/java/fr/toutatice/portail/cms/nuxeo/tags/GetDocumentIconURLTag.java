package fr.toutatice.portail.cms.nuxeo.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.nuxeo.ecm.automation.client.model.Document;

import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.portlets.bridge.Formater;

/**
 * Get Nuxeo document type icon URL tag.
 *
 * @author Cédric Krommenhoek
 * @see SimpleTagSupport
 */
public class GetDocumentIconURLTag extends SimpleTagSupport {

    /** Document DTO. */
    private DocumentDTO document;


    /**
     * Default constructor.
     */
    public GetDocumentIconURLTag() {
        super();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void doTag() throws JspException, IOException {
        if (this.document != null) {
            // Original Nuxeo document
            Document nuxeoDocument = this.document.getDocument();
            // JSP context
            PageContext pageContext = (PageContext) this.getJspContext();
            // Context path
            String contextPath = pageContext.getServletConfig().getServletContext().getContextPath();
            // Nuxeo
            String icon = Formater.formatNuxeoIcon(nuxeoDocument);

            JspWriter out = pageContext.getOut();
            out.write(contextPath + icon);
        }
    }


    /**
     * Getter for document.
     *
     * @return the document
     */
    public DocumentDTO getDocument() {
        return this.document;
    }

    /**
     * Setter for document.
     *
     * @param document the document to set
     */
    public void setDocument(DocumentDTO document) {
        this.document = document;
    }

}
