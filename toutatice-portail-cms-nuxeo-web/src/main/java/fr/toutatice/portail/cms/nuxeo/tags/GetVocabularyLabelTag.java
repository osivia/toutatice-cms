package fr.toutatice.portail.cms.nuxeo.tags;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.VocabularyEntry;
import fr.toutatice.portail.cms.nuxeo.api.VocabularyHelper;

/**
 * Get vocabulary label tag.
 *
 * @author Cédric Krommenhoek
 * @see SimpleTagSupport
 */
public class GetVocabularyLabelTag extends SimpleTagSupport {

    /** Log. */
    private final Log log;

    /** Vocabulary name. */
    private String name;
    /** Vocabulary key. */
    private String key;


    /**
     * Constructor.
     */
    public GetVocabularyLabelTag() {
        super();
        this.log = LogFactory.getLog(this.getClass());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void doTag() throws JspException, IOException {
        try {
            // Context
            PageContext pageContext = (PageContext) this.getJspContext();
            // Request
            ServletRequest request = pageContext.getRequest();
            // Nuxeo controller
            NuxeoController nuxeoController = (NuxeoController) request.getAttribute(NuxeoController.REQUEST_ATTRIBUTE);

            if (nuxeoController != null) {
                StringBuilder sb = new StringBuilder("");
                String[] keys;
                if (StringUtils.contains(this.key, "[")) {
                    String[] substringsBetween = StringUtils.substringsBetween(this.key, "[", "]");
                    keys = StringUtils.split(substringsBetween[0], ",");
                } else {
                    keys = new String[1];
                    keys[0] = this.key;
                }

                for (int i = 0; i < keys.length; i++) {
                    if (i > 0) {
                        sb.append(", ");
                    }

                    keys[i] = StringUtils.trim(keys[i]);

                    if (StringUtils.contains(keys[i], "/")) {
            			String[]subKeys = StringUtils.split(keys[i], "/");

            			VocabularyEntry vocabularyEntryRoot = VocabularyHelper.getVocabularyEntry(nuxeoController, this.name, true);

            			VocabularyEntry parent = vocabularyEntryRoot.getChild(subKeys[0]);
                        sb.append(StringUtils.trimToEmpty(parent.getLabel()));

            			VocabularyEntry child = parent.getChild(subKeys[1]);
            			if(child != null){
            			    sb.append(" / ");
                            sb.append(StringUtils.trimToEmpty(child.getLabel()));
            			}
                    } else {
                        sb.append(StringUtils.trimToEmpty(VocabularyHelper.getVocabularyLabel(nuxeoController, this.name, keys[i])));
                    }
                }

                String label = sb.toString();

                JspWriter out = pageContext.getOut();
                out.write(label);
            }
        } catch (NuxeoException e) {
            this.log.error(e.getMessage() + " ; name=" + this.name + ", key=" + this.key);
        }
    }


    /**
     * Setter for name.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Setter for key.
     *
     * @param key the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

}
