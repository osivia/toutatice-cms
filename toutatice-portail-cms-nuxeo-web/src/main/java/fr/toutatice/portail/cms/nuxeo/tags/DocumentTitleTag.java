package fr.toutatice.portail.cms.nuxeo.tags;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.lang.BooleanUtils;
import org.dom4j.Element;
import org.dom4j.io.HTMLWriter;
import org.osivia.portal.api.html.DOM4JUtils;
import org.osivia.portal.api.html.HTMLConstants;
import org.osivia.portal.api.urls.Link;
import org.osivia.portal.core.cms.CMSItemType;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;

/**
 * Document title tag.
 * 
 * @author Cédric Krommenhoek
 * @see SimpleTagSupport
 */
public class DocumentTitleTag extends SimpleTagSupport {

    /** Document DTO. */
    private DocumentDTO document;
    /** Document link indicator. */
    private Boolean link;
    /** Document icon indicator. */
    private Boolean icon;


    /**
     * Constructor.
     */
    public DocumentTitleTag() {
        super();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void doTag() throws JspException, IOException {
        // Context
        PageContext pageContext = (PageContext) this.getJspContext();
        // Request
        ServletRequest request = pageContext.getRequest();
        // Nuxeo controller
        NuxeoController nuxeoController = (NuxeoController) request.getAttribute("nuxeoController");


        // Document glyphicon
        String glyphicon = null;
        if (BooleanUtils.isTrue(icon)) {
            CMSItemType type = document.getType();
            if (type != null) {
                glyphicon = type.getGlyph();
            }
        }


        // Document title
        Element container;
        if (BooleanUtils.isFalse(link) || (nuxeoController == null)) {
            container = DOM4JUtils.generateElement(HTMLConstants.SPAN, null, document.getTitle(), glyphicon, null);
        } else {
            container = DOM4JUtils.generateElement(HTMLConstants.SPAN, null, null);

            // Document link
            Link documentLink = nuxeoController.getLink(this.document.getDocument());
            // Document link target
            String target = null;
            if (documentLink.isExternal()) {
                target = "_blank";
            }

            Element linkElement = DOM4JUtils.generateLinkElement(documentLink.getUrl(), target, null, null, document.getTitle(), glyphicon);
            container.add(linkElement);

            // External indicator
            if (documentLink.isExternal()) {
                Element externalElement = DOM4JUtils.generateElement(HTMLConstants.SMALL, null, null, "halflings halflings-new-window", null);
                container.add(externalElement);
            }

            // Downloadable indicator
            if (documentLink.isDownloadable()) {
                Element downloadableElement = DOM4JUtils.generateElement(HTMLConstants.SMALL, null, null, "halflings halflings-download-alt", null);
                container.add(downloadableElement);
            }
        }


        // Write
        HTMLWriter htmlWriter = new HTMLWriter(pageContext.getOut());
        htmlWriter.write(container);
    }


    /**
     * Setter for document.
     * 
     * @param document the document to set
     */
    public void setDocument(DocumentDTO document) {
        this.document = document;
    }

    /**
     * Setter for link.
     * 
     * @param link the link to set
     */
    public void setLink(Boolean link) {
        this.link = link;
    }

    /**
     * Setter for icon.
     * 
     * @param icon the icon to set
     */
    public void setIcon(Boolean icon) {
        this.icon = icon;
    }

}
