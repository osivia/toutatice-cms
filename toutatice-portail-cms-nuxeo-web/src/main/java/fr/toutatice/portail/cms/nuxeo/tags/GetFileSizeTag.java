package fr.toutatice.portail.cms.nuxeo.tags;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.internationalization.Bundle;
import org.osivia.portal.api.internationalization.IBundleFactory;
import org.osivia.portal.api.internationalization.IInternationalizationService;
import org.osivia.portal.api.locator.Locator;

/**
 * Get Nuxeo document file size tag.
 * @author Cédric Krommenhoek
 * @see SimpleTagSupport
 */
public class GetFileSizeTag extends SimpleTagSupport {

    /** File size units. */
    private static final String[] UNITS = {"BYTE", "KILOBYTE", "MEGABYTE", "GIGABYTE", "TERABYTE"};
    /** Unit factor. */
    private static final double UNIT_FACTOR = 1024;
    /** Bundle factory. */
    private static final IBundleFactory BUNDLE_FACTORY = getBundleFactory();


    /**
     * Default constructor.
     */
    public GetFileSizeTag() {
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
        // Nuxeo document
        Document document = (Document) request.getAttribute("nuxeoDocument");

        if (document != null) {
            // File size
            PropertyMap fileContent = document.getProperties().getMap("file:content");
            Long fileSize = fileContent.getLong("length");

            // Factor
            int factor = Double.valueOf(Math.log10(fileSize) / Math.log10(UNIT_FACTOR)).intValue();
            // Size
            double size = fileSize / Math.pow(UNIT_FACTOR, factor);
            // Unit
            String unit = bundle.getString(UNITS[factor]);
            // Number format
            NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
            numberFormat.setMaximumFractionDigits(1);

            JspWriter out = pageContext.getOut();
            out.write(numberFormat.format(size));
            out.write(" ");
            out.write(unit);
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
        return internationalizationService.getBundleFactory(GetFileSizeTag.class.getClassLoader());
    }

}
