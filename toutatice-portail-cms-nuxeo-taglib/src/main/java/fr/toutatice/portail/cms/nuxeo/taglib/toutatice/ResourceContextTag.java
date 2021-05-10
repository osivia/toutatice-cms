package fr.toutatice.portail.cms.nuxeo.taglib.toutatice;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.taglib.common.ToutaticeSimpleTag;

/**
 * Resource context  tag.
 *
 * @author Jean-Sébastien Steux
 * @see ResourceContextTag
 */
public class ResourceContextTag extends ToutaticeSimpleTag {


    /**
     * Constructor.
     */
    public ResourceContextTag() {
        super();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void doTag(NuxeoController nuxeoController, DocumentDTO document) throws JspException, IOException {
  
        this.getJspContext().getOut().write(NuxeoController.getCMSNuxeoWebContextName());
    }
 

}
