package fr.toutatice.portail.cms.nuxeo.taglib.toutatice;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.taglib.common.ToutaticeSimpleTag;

/**
 * Transform Nuxeo URL tag.
 *
 * @author Cédric Krommenhoek
 * @see ToutaticeSimpleTag
 */
public class TransformNuxeoUrlTag extends ToutaticeSimpleTag {

    /** Nuxeo URL. */
    private String url;
    /** Additional URL parameters. */
    private Map<String, String> params;


    /**
     * Constructor.
     */
    public TransformNuxeoUrlTag() {
        super();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void doTag(NuxeoController nuxeoController, DocumentDTO document) throws JspException, IOException {
        JspWriter out = this.getJspContext().getOut();

        // Transform
        out.write(nuxeoController.transformNuxeoLink(this.url));

        // Parameters
        if (this.params != null) {
            for (Entry<String, String> param : this.params.entrySet()) {
                out.write("&");
                out.write(param.getKey());
                out.write("=");
                out.write(param.getValue());
            }
        }
    }


    /**
     * Setter for url.
     *
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Setter for params.
     *
     * @param params the params to set
     */
    public void setParams(Map<String, String> params) {
        this.params = params;
    }

}
