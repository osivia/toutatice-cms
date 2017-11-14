package fr.toutatice.portail.cms.nuxeo.taglib.toutatice;

import java.io.IOException;

import javax.portlet.PortletRequest;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.lang.StringUtils;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.CustomizedJsp;
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
        // Servlet request
        ServletRequest servletRequest = pageContext.getRequest();
        // Portlet request
        PortletRequest portletRequest = nuxeoController.getRequest();

        // Path
        String path;

        if (!this.page.startsWith("/")) {
            String servletPath = (String) servletRequest.getAttribute("javax.servlet.include.servlet_path");
            String parentPath = StringUtils.substringBeforeLast(servletPath, "/");
            path = parentPath + "/" + this.page;
        } else {
            path = this.page;
        }


        try {
            // Customized JavaServer page
            CustomizedJsp customizedPage = this.getTagService().getCustomizedJsp(nuxeoController, path);

            // Customized class loader
            ClassLoader customizedClassLoader = customizedPage.getClassLoader();
            if (customizedClassLoader != null) {
                portletRequest.setAttribute("osivia.customizer.cms.jsp.classloader", customizedClassLoader);
            }

            // Include customized page
            pageContext.include(customizedPage.getName());
        } catch (ServletException e) {
            throw new JspException(e);
        } finally {
            portletRequest.removeAttribute("osivia.customizer.cms.jsp.classloader");
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
