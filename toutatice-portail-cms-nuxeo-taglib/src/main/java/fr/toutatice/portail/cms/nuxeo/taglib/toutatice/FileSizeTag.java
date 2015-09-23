package fr.toutatice.portail.cms.nuxeo.taglib.toutatice;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

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
        this.bundleFactory = internationalizationService.getBundleFactory(this.getClass().getClassLoader());
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
     * Setter for size.
     *
     * @param size the size to set
     */
    public void setSize(Long size) {
        this.size = size;
    }

}
