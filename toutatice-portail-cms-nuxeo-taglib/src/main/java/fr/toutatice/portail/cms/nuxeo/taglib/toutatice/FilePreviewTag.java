/**
 * 
 */
package fr.toutatice.portail.cms.nuxeo.taglib.toutatice;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import org.dom4j.Element;
import org.osivia.portal.api.html.DOM4JUtils;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.taglib.common.ToutaticeSimpleTag;


/**
 * Preview file tag.
 *
 * @author dchevrier
 * @see ToutaticeSimpleTag
 */
public class FilePreviewTag extends ToutaticeSimpleTag {

    /**
     * Constructor.
     */
    public FilePreviewTag() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doTag(NuxeoController nuxeoController, DocumentDTO document) throws JspException, IOException {
        // Context
        PageContext pageContext = (PageContext) this.getJspContext();
        JspWriter out = pageContext.getOut();
        
        String htmlPreview = String.format("/nuxeo/restAPI/preview/default/%s/file:content/", document.getId());
        out.write(htmlPreview);
    }

}
