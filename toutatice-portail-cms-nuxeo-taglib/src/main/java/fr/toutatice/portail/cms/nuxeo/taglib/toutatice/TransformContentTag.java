package fr.toutatice.portail.cms.nuxeo.taglib.toutatice;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.taglib.common.ToutaticeSimpleTag;

/**
 * Tranform HTML content tag.
 *
 * @author Cédric Krommenhoek
 * @see ToutaticeSimpleTag
 */
public class TransformContentTag extends ToutaticeSimpleTag {

    /** Content. */
    private String content;


    /**
     * Constructor.
     */
    public TransformContentTag() {
        super();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void doTag(NuxeoController nuxeoController, DocumentDTO document) throws JspException, IOException {
        // Transformation
        String transformedContent = nuxeoController.transformHTMLContent(this.content);

        this.getJspContext().getOut().write(transformedContent);
    }


    /**
     * Setter for content.
     *
     * @param content the content to set
     */
    public void setContent(String content) {
        this.content = content;
    }

}
