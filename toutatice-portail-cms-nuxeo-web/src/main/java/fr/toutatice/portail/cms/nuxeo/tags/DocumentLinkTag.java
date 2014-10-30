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
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;

/**
 * Nuxeo document link tag.
 *
 * @author Cédric Krommenhoek
 * @see SimpleTagSupport
 */
public class DocumentLinkTag extends SimpleTagSupport {

    /** Document DTO. */
    private DocumentDTO document;
    /** Nuxeo document link property name. */
    private String property;
    /** Document display context. */
    private String displayContext;
    /** Picture document indicator. */
    private Boolean picture;
    /** Request variable name. */
    private String var;


    /**
     * Default constructor.
     */
    public DocumentLinkTag() {
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
            // Link
            Link link;

            if (StringUtils.isEmpty(this.property)) {
                // Original Nuxeo document
                Document nuxeoDocument = this.document.getDocument();

                if (BooleanUtils.isTrue(this.picture)) {
                    String path = nuxeoDocument.getPath();
                    String url = nuxeoController.createPictureLink(path, StringUtils.defaultIfEmpty(this.displayContext, "Original"));
                    link = new Link(url, false);
                } else {
                    link = nuxeoController.getLink(nuxeoDocument, StringUtils.trimToNull(this.displayContext));
                }
            } else {
                // Property value
                String value = String.valueOf(this.document.getProperties().get(this.property));
                if (StringUtils.startsWith(value, "/nuxeo/")) {
                    // Web URL
                    String url = nuxeoController.transformNuxeoLink(value);
                    link = new Link(url, false);
                } else if (StringUtils.startsWith(value, "/")) {
                    // CMS
                    link = nuxeoController.getCMSLinkByPath(value, null);
                } else {
                    // Absolute URL
                    String serverName = nuxeoController.getRequest().getServerName();
                    String urlServerName = StringUtils.substringBefore(StringUtils.substringAfter(value, "://"), "/");
                    boolean external = StringUtils.isNotBlank(value) && !StringUtils.equals(serverName, urlServerName);
                    link = new Link(value, external);
                }
            }


            if (StringUtils.isEmpty(this.var)) {
                JspWriter out = pageContext.getOut();
                out.write(link.getUrl());
            } else {
                request.setAttribute(this.var, link);
            }
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

    /**
     * Getter for var.
     *
     * @return the var
     */
    public String getVar() {
        return this.var;
    }

    /**
     * Setter for var.
     *
     * @param var the var to set
     */
    public void setVar(String var) {
        this.var = var;
    }

}
