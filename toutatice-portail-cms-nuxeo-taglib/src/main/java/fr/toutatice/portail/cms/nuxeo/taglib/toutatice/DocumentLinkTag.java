package fr.toutatice.portail.cms.nuxeo.taglib.toutatice;

import org.apache.commons.lang.BooleanUtils;
import org.osivia.portal.api.urls.Link;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.taglib.common.ToutaticeLinkTag;

/**
 * Document link tag.
 *
 * @author Cédric Krommenhoek
 * @see ToutaticeLinkTag
 */
public class DocumentLinkTag extends ToutaticeLinkTag {

    /** Nuxeo document link property name. */
    private String property;
    /** Document display context. */
    private String displayContext;
    /** Picture document indicator. */
    private Boolean picture;
    /** Permalink indicator. */
    private Boolean permalink;


    /**
     * Constructor.
     */
    public DocumentLinkTag() {
        super();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected Link getLink(NuxeoController nuxeoController, DocumentDTO document) {
        return this.getTagService().getDocumentLink(nuxeoController, document, this.property, this.displayContext, BooleanUtils.isTrue(this.picture),
                BooleanUtils.isTrue(this.permalink));
    }


    /**
     * Setter for property.
     *
     * @param property the property to set
     */
    public void setProperty(String property) {
        this.property = property;
    }

    /**
     * Setter for displayContext.
     *
     * @param displayContext the displayContext to set
     */
    public void setDisplayContext(String displayContext) {
        this.displayContext = displayContext;
    }

    /**
     * Setter for picture.
     *
     * @param picture the picture to set
     */
    public void setPicture(Boolean picture) {
        this.picture = picture;
    }

    /**
     * Setter for permalink.
     *
     * @param permalink the permalink to set
     */
    public void setPermalink(Boolean permalink) {
        this.permalink = permalink;
    }

}
