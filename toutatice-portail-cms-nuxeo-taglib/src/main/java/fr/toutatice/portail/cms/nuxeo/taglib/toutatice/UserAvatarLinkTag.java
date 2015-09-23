package fr.toutatice.portail.cms.nuxeo.taglib.toutatice;

import org.osivia.portal.api.urls.Link;
import org.osivia.portal.core.cms.CMSException;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.taglib.common.ToutaticeLinkTag;

/**
 * User avatar link tag.
 *
 * @author Cédric Krommenhoek
 * @see ToutaticeLinkTag
 */
public class UserAvatarLinkTag extends ToutaticeLinkTag {

    /** User name. */
    private String name;


    /**
     * Constructor.
     */
    public UserAvatarLinkTag() {
        super();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected Link getLink(NuxeoController nuxeoController, DocumentDTO document) {
        Link link;
        try {
            link = nuxeoController.getUserAvatar(this.name);
        } catch (CMSException e) {
            link = null;
        }
        return link;
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
