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


    /** The id. */
    private String id;
    


    /** The participant. */
    private String participant;
    
    /** The publication id. */
    private String publicationId;
    
    
    

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

        // URL
        String url = null;

        if (StringUtils.isNotEmpty(id)) {
            url = DiscussionHelper.getDiscussionUrlById(nuxeoController.getPortalCtx(), id);
        }

        if (StringUtils.isNotEmpty(participant)) {
            url = DiscussionHelper.getDiscussionUrlByParticipant(nuxeoController.getPortalCtx(), participant);
        }

        if (StringUtils.isNotEmpty(publicationId)) {
            url = DiscussionHelper.getDiscussionUrlByPublication(nuxeoController.getPortalCtx(), publicationId);
        }

        // HTML writer
        if (url != null) {
            HTMLWriter htmlWriter = new HTMLWriter(this.getJspContext().getOut());
            htmlWriter.setEscapeText(false);
            htmlWriter.write(url);
        }


    }


    
    /**
     * Setter for participant.
     * @param participant the participant to set
     */
    public void setParticipant(String participant) {
        this.participant = participant;
    }


    /**
     * Setter for publicationId.
     * @param publicationId the publicationId to set
     */
    public void setPublicationId(String publicationId) {
        this.publicationId = publicationId;
    }

    
    /**
     * Setter for id.
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

}
