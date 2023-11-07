package fr.toutatice.portail.cms.nuxeo.taglib.toutatice;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.taglib.common.ToutaticeSimpleTag;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.osivia.portal.api.html.HtmlFormatter;
import org.osivia.portal.api.internationalization.Bundle;
import org.osivia.portal.api.internationalization.IBundleFactory;
import org.osivia.portal.api.internationalization.IInternationalizationService;
import org.osivia.portal.api.locator.Locator;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import java.io.IOException;
import java.util.Locale;

/**
 * Format file size tag.
 *
 * @author Cédric Krommenhoek
 * @see ToutaticeSimpleTag
 */
public class FileSizeTag extends ToutaticeSimpleTag {

    /**
     * Bundle factory.
     */
    private final IBundleFactory bundleFactory;


    /**
     * File size.
     */
    private Long size;
    /**
     * Escape HTML indicator.
     */
    private Boolean escapeHtml;


    /**
     * Constructor.
     */
    public FileSizeTag() {
        super();

        // Bundle factory
        IInternationalizationService internationalizationService = Locator.getService(IInternationalizationService.MBEAN_NAME, IInternationalizationService.class);
        this.bundleFactory = internationalizationService.getBundleFactory(Thread.currentThread().getContextClassLoader());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void doTag(NuxeoController nuxeoController, DocumentDTO document) throws JspException, IOException {
        // Context
        PageContext pageContext = (PageContext) this.getJspContext();
        // Request
        ServletRequest request = pageContext.getRequest();
        // Locale
        Locale locale = request.getLocale();
        // Bundle
        Bundle bundle = this.bundleFactory.getBundle(locale);

        String text = HtmlFormatter.formatSize(locale, bundle, size);
        if (BooleanUtils.isTrue(this.escapeHtml)) {
            text = StringUtils.replace(text, "&nbsp;", "\u202f");
        }

        JspWriter out = pageContext.getOut();
        out.write(text);
    }


    public void setSize(Long size) {
        this.size = size;
    }

    public void setEscapeHtml(Boolean escapeHtml) {
        this.escapeHtml = escapeHtml;
    }
}
