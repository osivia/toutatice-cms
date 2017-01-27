package fr.toutatice.portail.cms.nuxeo.tags;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;

/**
 * Transform HTML content tag.
 *
 * @author Cédric Krommenhoek
 * @see SimpleTagSupport
 */
public class TransformHTMLTag extends SimpleTagSupport {

    /** Document DTO. */
    private DocumentDTO document;
    /** Nuxeo document HTML content property name. */
    private String property;


    /**
     * Default constructor.
     */
    public TransformHTMLTag() {
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
        NuxeoController nuxeoController = (NuxeoController) request.getAttribute("nuxeoController");

        if ((nuxeoController != null) && (this.document != null)) {
            // Original Nuxeo document
            Document nuxeoDocument = this.document.getDocument();

            // HTML content
            String htmlContent = StringUtils.trimToEmpty(nuxeoDocument.getString(this.property));

            // Transformation
            htmlContent = nuxeoController.transformHTMLContent(htmlContent);

            JspWriter out = pageContext.getOut();
            out.write(htmlContent);
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

    /**
     * Getter for property.
     *
     * @return the property
     */
    public String getProperty() {
        return this.property;
    }

    /**
     * Setter for property.
     *
     * @param property the property to set
     */
    public void setProperty(String property) {
        this.property = property;
    }

}