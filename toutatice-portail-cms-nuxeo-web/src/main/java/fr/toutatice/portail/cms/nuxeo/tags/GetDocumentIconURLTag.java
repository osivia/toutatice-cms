package fr.toutatice.portail.cms.nuxeo.tags;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.nuxeo.ecm.automation.client.model.Document;

import fr.toutatice.portail.cms.nuxeo.portlets.bridge.Formater;

/**
 * Get Nuxeo document type icon URL tag.
 *
 * @author Cédric Krommenhoek
 * @see SimpleTagSupport
 */
public class GetDocumentIconURLTag extends SimpleTagSupport {

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
        // Context
        PageContext pageContext = (PageContext) this.getJspContext();
        // Request
        ServletRequest request = pageContext.getRequest();
        // Nuxeo document
        Document document = (Document) request.getAttribute("nuxeoDocument");

        if (document != null) {
            // Context path
            String contextPath = pageContext.getServletConfig().getServletContext().getContextPath();
            // Nuxeo
            String icon = Formater.formatNuxeoIcon(document);

            JspWriter out = pageContext.getOut();
            out.write(contextPath + icon);
        }
    }

}
