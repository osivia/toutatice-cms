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

/**
 * Transform HTML content tag.
 *
 * @author Cédric Krommenhoek
 * @see SimpleTagSupport
 */
public class TransformHTMLTag extends SimpleTagSupport {

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
        // Nuxeo document
        Document document = (Document) request.getAttribute("nuxeoDocument");

        if ((nuxeoController != null) && (document != null)) {
            // HTML content
            String htmlContent = StringUtils.trimToEmpty(document.getString(this.property));

            // Transformation
            htmlContent = nuxeoController.transformHTMLContent(htmlContent);

            JspWriter out = pageContext.getOut();
            out.write(htmlContent);
        }
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
