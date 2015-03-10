package fr.toutatice.portail.cms.nuxeo.tags;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.osivia.portal.api.internationalization.Bundle;
import org.osivia.portal.api.internationalization.IBundleFactory;
import org.osivia.portal.api.internationalization.IInternationalizationService;
import org.osivia.portal.api.locator.Locator;

/**
 * Format Nuxeo document file size tag.
 *
 * @author Cédric Krommenhoek
 * @see SimpleTagSupport
 */
public class FormatFileSizeTag extends SimpleTagSupport {

    /** File size units. */
    private static final String[] UNITS = {"BYTE", "KILOBYTE", "MEGABYTE", "GIGABYTE", "TERABYTE"};
    /** Unit factor. */
    private static final double UNIT_FACTOR = 1024;
    /** Bundle factory. */
    private static final IBundleFactory BUNDLE_FACTORY = getBundleFactory();


    /** File size. */
    private Long size;


    /**
     * Default constructor.
     */
    public FormatFileSizeTag() {
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
        // Locale
        Locale locale = request.getLocale();
        // Bundle
        Bundle bundle = BUNDLE_FACTORY.getBundle(locale);

        JspWriter out = pageContext.getOut();

        if (this.size > 0) {
            // Factor
            int factor = Double.valueOf(Math.log10(this.size) / Math.log10(UNIT_FACTOR)).intValue();
            // Factorized size
            double factorizedSize = this.size / Math.pow(UNIT_FACTOR, factor);
            // Unit
            String unit = bundle.getString(UNITS[factor]);
            // Number format
            NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
            numberFormat.setMaximumFractionDigits(1);

            out.write(numberFormat.format(factorizedSize));
            out.write("&nbsp;");
            out.write(unit);
        } else {
            out.write("0&nbsp;");
            out.write(bundle.getString(UNITS[0]));
        }
    }


    /**
     * Get bundle factory.
     *
     * @return bundle factory
     */
    private static IBundleFactory getBundleFactory() {
        IInternationalizationService internationalizationService = Locator.findMBean(IInternationalizationService.class,
                IInternationalizationService.MBEAN_NAME);
        return internationalizationService.getBundleFactory(FormatFileSizeTag.class.getClassLoader());
    }


    /**
     * Getter for size.
     *
     * @return the size
     */
    public Long getSize() {
        return this.size;
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
