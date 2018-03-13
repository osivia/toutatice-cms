package fr.toutatice.portail.cms.nuxeo.taglib.toutatice;

import javax.servlet.jsp.PageContext;

import org.osivia.portal.api.urls.Link;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.taglib.common.ToutaticeLinkTag;

/**
 * Nuxeo document icon link tag.
 *
 * @author Cédric Krommenhoek
 * @see ToutaticeLinkTag
 */
public class NuxeoIconLinkTag extends ToutaticeLinkTag {

    /**
     * Constructor.
     */
    public NuxeoIconLinkTag() {
        super();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected Link getLink(NuxeoController nuxeoController, DocumentDTO document) {
        // Page context
        PageContext pageContext = (PageContext) this.getJspContext();
        // Context path
        String contextPath = pageContext.getServletContext().getContextPath();

        return this.getTagService().getNuxeoIconLink(nuxeoController, contextPath, document);
    }

}
