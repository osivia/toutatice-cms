package fr.toutatice.portail.cms.nuxeo.tags;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.lang.StringUtils;
import org.osivia.portal.api.urls.Link;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;


/**
 * Transform Nuxeo link to Portal Link.
 * 
 * @author David Chevrier.
 * @see SimpleTagSupport
 */
public class TransformNuxeoLinkTag extends SimpleTagSupport {
    
    /** Nuxeo link. */
    private String link;
    
    /**
     * Default constructor.
     */
    public TransformNuxeoLinkTag(){
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

        if ((nuxeoController != null)) {

            // Nuxeo URL
            String nxURL = StringUtils.trimToEmpty(this.link);

            // Transformation
            String portalLink = nuxeoController.transformNuxeoLink(nxURL);

            JspWriter out = pageContext.getOut();
            out.write(portalLink);
        }
    }

    /**
     * @return the link
     */
    public String getLink() {
        return link;
    }

    /**
     * @param link the link to set
     */
    public void setLink(String link) {
        this.link = link;
    }
    
}
