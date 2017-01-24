/**
 * 
 */
package fr.toutatice.portail.cms.nuxeo.taglib.toutatice;

import org.osivia.portal.api.urls.Link;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.taglib.common.ToutaticeLinkTag;
import fr.toutatice.portail.cms.nuxeo.taglib.common.ToutaticeSimpleTag;


/**
 * Preview file tag.
 *
 * @author dchevrier
 * @see ToutaticeSimpleTag
 */
public class FilePreviewTag extends ToutaticeLinkTag {

    /**
     * Constructor.
     */
    public FilePreviewTag() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Link getLink(NuxeoController nuxeoController, DocumentDTO document) {
        // Preview file
        return this.getTagService().getPreviewFileLink(nuxeoController, document);
    }
}
