package fr.toutatice.portail.cms.nuxeo.taglib.toutatice;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.lang.StringUtils;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.taglib.common.ToutaticeSimpleTag;

/**
 * Include JSP tag.
 *
 * @author Cédric Krommenhoek
 * @see ToutaticeSimpleTag
 */
public class IncludeTag extends ToutaticeSimpleTag {

    /** Page name. */
    private String page;


    /**
     * Constructor.
     */
    public IncludeTag() {
        super();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void doTag(NuxeoController nuxeoController, DocumentDTO document) throws JspException, IOException {
        // Page context
        PageContext pageContext = (PageContext) this.getJspContext();
        // Request
        ServletRequest request = pageContext.getRequest();

        // Path
        String path;

        if (!this.page.startsWith("/")) {
            String servletPath = (String) request.getAttribute("javax.servlet.include.servlet_path");

            // FIXME bidouille pour que les JSP include fonctionnent
            servletPath = servletPath.replaceAll("/WEB-INF/jsp//WEB-INF/jsp", "/WEB-INF/jsp");

            String parentPath = servletPath.substring(0, StringUtils.lastIndexOf(servletPath, '/') + 1);
            path = parentPath + this.page;
        } else {
            path = this.page;
        }

        // JSP name
        String name = this.getTagService().getIncludedJspName(nuxeoController, path);

        try {
            pageContext.include(name);
        } catch (ServletException e) {
            throw new JspException(e);
        }
    }


    /**
     * Setter for page.
     *
     * @param page the page to set
     */
    public void setPage(String page) {
        this.page = page;
    }

}
