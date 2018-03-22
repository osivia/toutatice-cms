package fr.toutatice.portail.cms.nuxeo.taglib.toutatice;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.taglib.common.ToutaticeSimpleTag;

/**
 * Tranform HTML content tag.
 *
 * @author Cédric Krommenhoek
 * @see ToutaticeSimpleTag
 */
public class TransformTag extends ToutaticeSimpleTag {

    /** Property name. */
    private String property;


    /**
     * Constructor.
     */
    public TransformTag() {
        super();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void doTag(NuxeoController nuxeoController, DocumentDTO document) throws JspException, IOException {
        // Nuxeo document
        Document nuxeoDocument = document.getDocument();

        // HTML content
        String htmlContent = StringUtils.trimToEmpty(nuxeoDocument.getString(this.property));

        // Transformation
        htmlContent = nuxeoController.transformHTMLContent(htmlContent);

        this.getJspContext().getOut().write(htmlContent);
    }


    /**
     * Setter for property.
     *
     * @param property the property to set
     */
    public void setProperty(String property) {
        this.property = property;
    }

}
