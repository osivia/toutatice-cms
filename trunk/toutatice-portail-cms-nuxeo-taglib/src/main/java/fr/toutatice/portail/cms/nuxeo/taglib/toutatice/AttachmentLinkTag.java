package fr.toutatice.portail.cms.nuxeo.taglib.toutatice;

import org.osivia.portal.api.urls.Link;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.taglib.common.ToutaticeLinkTag;

/**
 * Attachment link tag.
 *
 * @author Cédric Krommenhoek
 * @see ToutaticeLinkTag
 */
public class AttachmentLinkTag extends ToutaticeLinkTag {

    /** Attachment index. */
    private Integer index;


    /**
     * Constructor.
     */
    public AttachmentLinkTag() {
        super();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected Link getLink(NuxeoController nuxeoController, DocumentDTO document) {
        // URL
        String url = nuxeoController.createAttachedFileLink(document.getPath(), String.valueOf(this.index));

        return new Link(url, false);
    }


    /**
     * Setter for index.
     *
     * @param index the index to set
     */
    public void setIndex(Integer index) {
        this.index = index;
    }

}
