package fr.toutatice.portail.cms.nuxeo.taglib.toutatice;

import org.osivia.portal.api.urls.Link;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.taglib.common.ToutaticeLinkTag;

/**
 * Picture link tag.
 *
 * @author Cédric Krommenhoek
 * @see ToutaticeLinkTag
 */
public class PictureLinkTag extends ToutaticeLinkTag {

    /** Nuxeo document link property name. */
    private String property;


    /**
     * Constructor.
     */
    public PictureLinkTag() {
        super();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected Link getLink(NuxeoController nuxeoController, DocumentDTO document) {
        return this.getTagService().getDocumentLink(nuxeoController, document, this.property, null, true, false);
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
