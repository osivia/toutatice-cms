package fr.toutatice.portail.cms.nuxeo.taglib.toutatice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.JspException;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.dom4j.io.HTMLWriter;
import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.directory.v2.DirServiceFactory;
import org.osivia.portal.api.directory.v2.model.Person;
import org.osivia.portal.api.directory.v2.service.PersonService;
import org.osivia.portal.api.html.DOM4JUtils;
import org.osivia.portal.api.html.HTMLConstants;
import org.osivia.portal.api.urls.Link;
import org.osivia.portal.core.cms.CMSException;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.discussions.DiscussionHelper;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.taglib.common.ToutaticeSimpleTag;

/**
 * Discussion tag.
 *
 * @author Jean-Sébastien steux
 * 
 * @see ToutaticeSimpleTag
 */
public class DiscussionTag extends ToutaticeSimpleTag {


    /**
     * Discussion type
     * 
     *
     */
    private String participant;


    /**
     * Constructor.
     */
    public DiscussionTag() {
        super();
        this.participant = null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void doTag(NuxeoController nuxeoController, DocumentDTO document) throws JspException, IOException {
        if (StringUtils.isNotBlank(this.participant)) {
           // URL
            String url = null;

            if (StringUtils.isNotEmpty(participant)) {
                 url = DiscussionHelper.getDiscussionUrlByParticipant(nuxeoController.getPortalCtx(), participant);
            }


            // HTML writer
            if (url != null) {
                HTMLWriter htmlWriter = new HTMLWriter(this.getJspContext().getOut());
                htmlWriter.setEscapeText(false);
                htmlWriter.write(url);
            }
        }

    }


    
    /**
     * Setter for participant.
     * @param participant the participant to set
     */
    public void setParticipant(String participant) {
        this.participant = participant;
    }



}
