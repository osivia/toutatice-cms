package fr.toutatice.portail.cms.nuxeo.taglib.common;

import java.io.IOException;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.osivia.portal.api.urls.Link;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;


public abstract class ToutaticeLinkTag extends ToutaticeSimpleTag {

    /** Request variable name. */
    private String var;


    /**
     * Constructor.
     */
    public ToutaticeLinkTag() {
        super();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void doTag(NuxeoController nuxeoController, DocumentDTO document) throws JspException, IOException {
        // JSP context
        JspContext jspContext = this.getJspContext();

        // Link
        Link link = this.getLink(nuxeoController, document);

        if ((link != null) && (link.getUrl() != null)) {
            if (this.var == null) {
                // Out
                jspContext.getOut().write(link.getUrl());
            } else {
                // Request attribute
                PageContext pageContext = (PageContext) jspContext;
                pageContext.getRequest().setAttribute(this.var, link);

            }
        }
    }


    /**
     * Get link.
     *
     * @param nuxeoController Nuxeo controller
     * @param document document DTO
     * @return
     */
    protected abstract Link getLink(NuxeoController nuxeoController, DocumentDTO document);


    /**
     * Setter for var.
     *
     * @param var the var to set
     */
    public void setVar(String var) {
        this.var = var;
    }

}

