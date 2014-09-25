package fr.toutatice.portail.cms.nuxeo.tags;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.urls.Link;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;

/**
 * Get Nuxeo document URL tag.
 *
 * @author Cédric Krommenhoek
 * @see SimpleTagSupport
 */
public class GetDocumentURLTag extends SimpleTagSupport {

    /** Document display context. */
    private String displayContext;
    /** Picture document indicator. */
    private Boolean picture;


    /**
     * Default constructor.
     */
    public GetDocumentURLTag() {
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
            // URL
            String url;
            if (BooleanUtils.isTrue(this.picture)) {
                String path = document.getPath();
                url = nuxeoController.createPictureLink(path, StringUtils.defaultIfEmpty(this.displayContext, "Original"));
            } else {
                Link link = nuxeoController.getLink(document, StringUtils.trimToNull(this.displayContext));
                url = link.getUrl();
            }

            JspWriter out = pageContext.getOut();
            out.write(url);
        }
    }


    /**
     * Getter for displayContext.
     *
     * @return the displayContext
     */
    public String getDisplayContext() {
        return this.displayContext;
    }

    /**
     * Setter for displayContext.
     *
     * @param displayContext the displayContext to set
     */
    public void setDisplayContext(String displayContext) {
        this.displayContext = displayContext;
    }

    /**
     * Getter for picture.
     *
     * @return the picture
     */
    public Boolean getPicture() {
        return this.picture;
    }

    /**
     * Setter for picture.
     *
     * @param picture the picture to set
     */
    public void setPicture(Boolean picture) {
        this.picture = picture;
    }

}
