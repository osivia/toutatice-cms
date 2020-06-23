package fr.toutatice.portail.cms.nuxeo.taglib.toutatice;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.dom4j.io.HTMLWriter;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.cms.DocumentType;
import org.osivia.portal.api.cms.FileMimeType;
import org.osivia.portal.api.html.DOM4JUtils;
import org.osivia.portal.api.html.HTMLConstants;
import org.osivia.portal.api.urls.Link;
import org.osivia.portal.core.cms.CMSException;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCustomizer;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoService;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoServiceFactory;
import fr.toutatice.portail.cms.nuxeo.taglib.common.ToutaticeSimpleTag;

/**
 * Document title tag.
 *
 * @author Cédric Krommenhoek
 * @see ToutaticeSimpleTag
 */
public class TitleTag extends ToutaticeSimpleTag {

    /** Document linkable indicator. */
    private boolean linkable;
    /** Document link display context. */
    private String displayContext;
    /** Document type icon indicator. */
    private boolean icon;
    /** Link shoud be targetted in Tab named with the spaceId */
    private boolean openInSpaceTabs;


	private INuxeoCustomizer cmsCustomizer;    


    /**
     * Constructor.
     */
    public TitleTag() {
        super();
        this.linkable = true;
        this.icon = false;
        this.openInSpaceTabs = false;
        
        INuxeoService nuxeoService = NuxeoServiceFactory.getNuxeoService();
        cmsCustomizer = nuxeoService.getCMSCustomizer();
        
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void doTag(NuxeoController nuxeoController, DocumentDTO document) throws JspException, IOException {
        
         
        String title;
        if( StringUtils.isNotEmpty(document.getTitle()))
            title = document.getDisplayTitle();
        else
            title = document.getId();
         
        // Icon
        String icon;
        if (this.icon && (document.getType() != null)) {
            icon = document.getType().getIcon();
        } else {
            icon = null;
        }


        // Container
        Element container = DOM4JUtils.generateElement(HTMLConstants.SPAN, null, null);

        if (this.linkable) {
            // Link
            Link link = this.getTagService().getDocumentLink(nuxeoController, document, null, this.displayContext, false, false);
            // Target
            String target;
            boolean isFile = document.getType() != null && "File".equals(document.getType().getName());
            if (link.isExternal() || ("download".equals(this.displayContext) && isFile)) {
                target = "_blank";
            } else if (this.openInSpaceTabs) {
                target = this.cmsCustomizer.getTarget(document);
            } else {
                target = null;
            }

            Element content = DOM4JUtils.generateLinkElement(link.getUrl(), target, null, "no-ajax-link", title, icon);
            container.add(content);

            if (link.isExternal()) {
                Element external = DOM4JUtils.generateElement(HTMLConstants.SMALL, null, null, "glyphicons glyphicons-new-window-alt", null);
                container.add(external);
            }
        } else {
            Element content = DOM4JUtils.generateElement(HTMLConstants.SPAN, null, title, icon, null);
            container.add(content);
        }


        // HTML writer
        HTMLWriter htmlWriter = new HTMLWriter(this.getJspContext().getOut());
        htmlWriter.setEscapeText(false);
        htmlWriter.write(container);
    }


    /**
     * Setter for linkable.
     *
     * @param linkable the linkable to set
     */
    public void setLinkable(boolean linkable) {
        this.linkable = linkable;
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
     * Setter for icon.
     *
     * @param icon the icon to set
     */
    public void setIcon(boolean icon) {
        this.icon = icon;
    }

    /**
     * Setter for open in space tabs
     * @param boolean true if enabled
     */
	public void setOpenInSpaceTabs(boolean openInSpaceTabs) {
		this.openInSpaceTabs = openInSpaceTabs;
	}

}
