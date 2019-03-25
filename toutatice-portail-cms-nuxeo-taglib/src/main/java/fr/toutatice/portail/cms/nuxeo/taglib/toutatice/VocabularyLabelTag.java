package fr.toutatice.portail.cms.nuxeo.taglib.toutatice;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.VocabularyEntry;
import fr.toutatice.portail.cms.nuxeo.api.VocabularyHelper;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.taglib.common.ToutaticeSimpleTag;

/**
 * Nuxeo vocabulary label tag.
 *
 * @author Cédric Krommenhoek
 * @see ToutaticeSimpleTag
 */
public class VocabularyLabelTag extends ToutaticeSimpleTag {

    /** Vocabulary name. */
    private String name;
    /** Vocabulary key. */
    private String key;

    /** Log. */
    private final Log log;


    /**
     * Constructor.
     */
    public VocabularyLabelTag() {
        super();

        // Log
        this.log = LogFactory.getLog(this.getClass());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void doTag(NuxeoController nuxeoController, DocumentDTO document) throws JspException, IOException {
        try {
            StringBuilder sb = new StringBuilder("");
            String[] keys;
            if (StringUtils.containsAny(this.key, "[]")) {
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
                    String[] subKeys = StringUtils.split(keys[i], "/");

                    VocabularyEntry vocabularyEntryRoot = VocabularyHelper.getVocabularyEntry(nuxeoController, this.name, true);

                    VocabularyEntry parent = vocabularyEntryRoot.getChild(subKeys[0]);
                    if (parent != null) {
                        sb.append(StringUtils.trimToEmpty(parent.getLabel()));
                        VocabularyEntry child = parent.getChild(subKeys[1]);
                        if (child != null) {
                            sb.append(" / ");
                            sb.append(StringUtils.trimToEmpty(child.getLabel()));
                        }
                    }
                } else {
                    sb.append(StringUtils.trimToEmpty(VocabularyHelper.getVocabularyLabel(nuxeoController, this.name, keys[i])));
                }
            }

            String label = sb.toString();

            JspWriter out = this.getJspContext().getOut();
            out.write(label);
        } catch (NuxeoException e) {
            this.log.error(e.getMessage() + " ; name=" + this.name + ", key=" + this.key);
        }
    }


	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}


	/**
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}


}
