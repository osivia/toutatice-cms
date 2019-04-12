package fr.toutatice.portail.cms.nuxeo.taglib.toutatice;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import org.dom4j.Element;
import org.osivia.portal.api.html.DOM4JUtils;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.taglib.common.ToutaticeSimpleTag;

/**
 * Document icon tag.
 * 
 * @author Cédric Krommenhoek
 * @see ToutaticeSimpleTag
 */
public class IconTag extends ToutaticeSimpleTag {

    /** Icon style. */
    private String style;


    /**
     * Constructor.
     */
    public IconTag() {
        super();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void doTag(NuxeoController nuxeoController, DocumentDTO document) throws JspException, IOException {
        // Icon DOM element
        Element icon = this.getTagService().getDocumentIcon(nuxeoController, document, this.style);

        this.getJspContext().getOut().write(DOM4JUtils.writeCompact(icon));
    }


    /**
     * Setter for style.
     * 
     * @param style the style to set
     */
    public void setStyle(String style) {
        this.style = style;
    }

}
