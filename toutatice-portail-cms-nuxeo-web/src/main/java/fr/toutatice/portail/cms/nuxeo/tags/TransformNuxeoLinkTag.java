package fr.toutatice.portail.cms.nuxeo.tags;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

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
    /** Portal link params. */
    private Map<String, String> params;
    
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
            
            //Params
            if(MapUtils.isNotEmpty(this.params)){
                for(Entry<String, String> param : this.params.entrySet()){
                    portalLink = portalLink.concat("&").concat(param.getKey()).concat("=").concat(param.getValue());
                }
            }

            JspWriter out = pageContext.getOut();
            out.write(portalLink);
        }
    }

    /**
     * @return the link
     */
    public String getLink() {
        return this.link;
    }

    /**
     * @param link the link to set
     */
    public void setLink(String link) {
        this.link = link;
    }

    /**
     * @return the params
     */
    public Map<String, String> getParams() {
        return params;
    }

    /**
     * @param params the params to set
     */
    public void setParams(Map<String, String> params) {
        this.params = params;
    }
    
}
