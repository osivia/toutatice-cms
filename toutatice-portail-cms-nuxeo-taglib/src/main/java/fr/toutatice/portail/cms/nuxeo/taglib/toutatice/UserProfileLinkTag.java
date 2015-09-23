package fr.toutatice.portail.cms.nuxeo.taglib.toutatice;

import org.osivia.portal.api.urls.Link;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.taglib.common.ToutaticeLinkTag;

/**
 * User profile link tag.
 *
 * @author Cédric Krommenhoek
 * @see ToutaticeLinkTag
 */
public class UserProfileLinkTag extends ToutaticeLinkTag {

    /** User name. */
    private String name;


    /**
     * Constructor.
     */
    public UserProfileLinkTag() {
        super();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected Link getLink(NuxeoController nuxeoController, DocumentDTO document) {
        return this.getTagService().getUserProfileLink(nuxeoController, this.name);
    }


    /**
     * Setter for name.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

}
