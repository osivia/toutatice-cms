package fr.toutatice.portail.cms.nuxeo.tags;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyMap;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;

/**
 * Get Nuxeo document image URL tag.
 *
 * @author Cédric Krommenhoek
 * @see SimpleTagSupport
 */
public class GetImageURLTag extends SimpleTagSupport {

    /** Nuxeo document image property name. */
    private String property;


    /**
     * Default contructor.
     */
    public GetImageURLTag() {
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
            PropertyMap map = document.getProperties().getMap(this.property);
            if ((map != null) && (map.getString("data") != null)) {
                String url = nuxeoController.createFileLink(document, this.property);

                JspWriter out = pageContext.getOut();
                out.write(url);
            }
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
