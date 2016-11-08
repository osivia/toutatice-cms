/**
 * 
 */
package fr.toutatice.portail.cms.nuxeo.taglib.toutatice;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
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
    
    /** PDF conversion indicator. */
    public static final String PDF_CONTENT_CONVERT_INDICATOR = "pdf:content";

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

        String createFileLink = nuxeoController.createFileLink(document.getDocument(), PDF_CONTENT_CONVERT_INDICATOR);

        PropertyMap content = document.getDocument().getProperties().getMap("file:content");
        String fileName = (String) content.get("name");
        String mimeType = (String) content.get("mime-type");

        if (!StringUtils.contains(mimeType, "image")) {

            String contextName = nuxeoController.getPortletCtx().getPortletContextName();
            createFileLink = "/toutatice-portail-cms-nuxeo/components/ViewerJS/?title=" + fileName + "#../.."
                    + StringUtils.substringAfter(createFileLink, "/toutatice-portail-cms-nuxeo");
            return new Link(createFileLink, false);

        }
        return null;
    }
    
}
