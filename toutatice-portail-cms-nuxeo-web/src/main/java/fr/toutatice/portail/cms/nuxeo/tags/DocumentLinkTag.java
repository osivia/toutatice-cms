package fr.toutatice.portail.cms.nuxeo.tags;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.Window;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.api.urls.Link;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.portalobjects.PortalObjectUtils;
import org.osivia.portal.core.web.IWebIdService;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.portlets.service.CMSService;

/**
 * Nuxeo document link tag.
 *
 * @author Cédric Krommenhoek
 * @see SimpleTagSupport
 */
public class DocumentLinkTag extends SimpleTagSupport {

    /** Document DTO. */
    private DocumentDTO document;
    /** Nuxeo document link property name. */
    private String property;
    /** Document display context. */
    private String displayContext;
    /** Picture document indicator. */
    private Boolean picture;
    /** Permalink indicator. */
    private Boolean permalink;
    /** Request variable name. */
    private String var;


    /** WebId service. */
    private final IWebIdService webIdService;


    /**
     * Default constructor.
     */
    public DocumentLinkTag() {
        super();

        // WebId service
        this.webIdService = Locator.findMBean(IWebIdService.class, IWebIdService.MBEAN_NAME);
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
        NuxeoController nuxeoController = (NuxeoController) request.getAttribute(NuxeoController.REQUEST_ATTRIBUTE);

        // Nuxeo document
        Document nuxeoDocument;
        if (this.document != null) {
            nuxeoDocument = this.document.getDocument();
        } else if (nuxeoController != null) {
            nuxeoDocument = nuxeoController.getCurrentDoc();
        } else {
            nuxeoDocument = null;
        }


        if ((nuxeoController != null) && (nuxeoDocument != null)) {
            // Link
            Link link;

            if (StringUtils.isEmpty(this.property)) {
                if (BooleanUtils.isTrue(this.picture)) {
                    String path = nuxeoDocument.getPath();
                    String url = nuxeoController.createPictureLink(path, StringUtils.defaultIfEmpty(this.displayContext, "Original"));
                    link = new Link(url, false);
                } else if (BooleanUtils.isTrue(this.permalink)) {
                    // Portal controller context
                    PortalControllerContext portalControllerContext = nuxeoController.getPortalCtx();

                    // Page
                    Page page = null;
                    Window window = (Window) nuxeoController.getRequest().getAttribute("osivia.window");
                    if (window != null) {
                         page = window.getPage();
                    }

                    try {
                        String path = this.document.getPath();
                        if (PortalObjectUtils.isSpaceSite(page)) {
                            CMSService cmsService = (CMSService) NuxeoController.getCMSService();
                            CMSServiceCtx cmsContext = nuxeoController.getCMSCtx();
                            CMSItem cmsItem = cmsService.createItem(cmsContext, this.document.getPath(), null, nuxeoDocument);

                            if (StringUtils.isNotEmpty(cmsItem.getWebId())) {
                                path = this.webIdService.itemToPageUrl(cmsContext, cmsItem);
                            }
                        }

                        String url = nuxeoController.getPortalUrlFactory().getPermaLink(portalControllerContext, null, null, path,
                                IPortalUrlFactory.PERM_LINK_TYPE_CMS);
                        link = new Link(url, false);
                    } catch (Exception e) {
                        link = new Link("#", false);
                    }
                } else {
                    link = nuxeoController.getLink(nuxeoDocument, StringUtils.trimToNull(this.displayContext));
                }
            } else {
                // Property value
                String value = String.valueOf(this.document.getProperties().get(this.property));
                link = nuxeoController.getLinkFromNuxeoURL(value);
            }


            if (StringUtils.isEmpty(this.var)) {
                JspWriter out = pageContext.getOut();
                out.write(link.getUrl());
            } else {
                request.setAttribute(this.var, link);
            }
        }
    }


    /**
     * Getter for document.
     *
     * @return the document
     */
    public DocumentDTO getDocument() {
        return this.document;
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
     * Getter for property.
     *
     * @return the property
     */
    public String getProperty() {
        return this.property;
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
     * Getter for displayContext.
     *
     * @return the displayContext
     */
    public String getDisplayContext() {
        return this.displayContext;
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
     * Getter for picture.
     *
     * @return the picture
     */
    public Boolean getPicture() {
        return this.picture;
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
     * Getter for permalink.
     *
     * @return the permalink
     */
    public Boolean getPermalink() {
        return this.permalink;
    }

    /**
     * Setter for permalink.
     *
     * @param permalink the permalink to set
     */
    public void setPermalink(Boolean permalink) {
        this.permalink = permalink;
    }

    /**
     * Getter for var.
     *
     * @return the var
     */
    public String getVar() {
        return this.var;
    }

    /**
     * Setter for var.
     *
     * @param var the var to set
     */
    public void setVar(String var) {
        this.var = var;
    }

}
