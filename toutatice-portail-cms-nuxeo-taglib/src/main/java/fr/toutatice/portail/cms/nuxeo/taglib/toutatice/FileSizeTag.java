package fr.toutatice.portail.cms.nuxeo.taglib.toutatice;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import org.osivia.portal.api.html.HtmlFormatter;
import org.osivia.portal.api.internationalization.Bundle;
import org.osivia.portal.api.internationalization.IBundleFactory;
import org.osivia.portal.api.internationalization.IInternationalizationService;
import org.osivia.portal.api.locator.Locator;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.taglib.common.ToutaticeSimpleTag;

/**
 * Format file size tag.
 *
 * @author Cédric Krommenhoek
 * @see ToutaticeSimpleTag
 */
public class FileSizeTag extends ToutaticeSimpleTag {

    /** File size units. */
    private static final String[] UNITS = {"BYTE", "KILOBYTE", "MEGABYTE", "GIGABYTE", "TERABYTE"};
    /** Unit factor. */
    private static final double UNIT_FACTOR = 1024;


    /** File size. */
    private Long size;

    /** Bundle factory. */
    private final IBundleFactory bundleFactory;


    /**
     * Constructor.
     */
    public FileSizeTag() {
        super();

        // Bundle factory
        IInternationalizationService internationalizationService = Locator.findMBean(IInternationalizationService.class,
                IInternationalizationService.MBEAN_NAME);
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

        JspWriter out = pageContext.getOut();


        out.write(HtmlFormatter.formatSize(locale, bundle, size));
     }


    /**
     * Setter for size.
     *
     * @param size the size to set
     */
    public void setSize(Long size) {
        this.size = size;
    }

}
