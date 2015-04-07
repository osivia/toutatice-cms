package fr.toutatice.portail.cms.nuxeo.portlets.selectors.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.lang.StringUtils;

import fr.toutatice.portail.cms.nuxeo.api.VocabularyEntry;
import fr.toutatice.portail.cms.nuxeo.portlets.selectors.VocabSelectorPortlet;

/**
 * Get vocabulary label tag.
 *
 * @author Cédric Krommenhoek
 * @see SimpleTagSupport
 */
public class GetVocabularyLabelTag extends SimpleTagSupport {

    /** Vocabulary identifier. */
    private String id;
    /** Vocabulary entry. */
    private VocabularyEntry entry;
    /** Others label. */
    private String othersLabel;
    /** Vocabulary preselection. */
    private String preselect;


    /**
     * Constructor.
     */
    public GetVocabularyLabelTag() {
        super();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void doTag() throws JspException, IOException {
        // Context
        PageContext pageContext = (PageContext) this.getJspContext();

        // Label
        String label = this.getLabel(this.othersLabel, this.id, this.entry, this.preselect);

        // JSP writer
        if (label != null) {
            JspWriter out = pageContext.getOut();
            out.write(label);
        }
    }


    /**
     * Get hierarchical vocabulary label.
     *
     * @param othersLabel others label
     * @param id vocabulary identifier
     * @param entry vocabulary entry
     * @param preselection vocabulary preselection
     * @return label
     */
    private String getLabel(String othersLabel, String id, VocabularyEntry entry, String preselection) {
        StringBuilder builder = new StringBuilder();

        if (id.contains(VocabSelectorPortlet.OTHER_ENTRIES_CHOICE) && StringUtils.isNotEmpty(othersLabel)) {
            builder.append(StringUtils.replace(id, VocabSelectorPortlet.OTHER_ENTRIES_CHOICE, othersLabel));
        } else {
            String[] tokens = id.split("/", 2);

            if ((tokens.length > 0) && (StringUtils.isEmpty(preselection))) {
                VocabularyEntry child = entry.getChild(tokens[0]);
                builder.append(child.getLabel());
            }

            if (tokens.length > 1) {
                VocabularyEntry childVocab = entry.getChild(tokens[0]);
                if (childVocab != null) {
                    if (builder.length() > 0) {
                        builder.append("/");
                    }
                    builder.append(this.getLabel(builder.toString(), tokens[1], childVocab, null));
                }
            }
        }

        return builder.toString();
    }


    /**
     * Getter for id.
     *
     * @return the id
     */
    public String getId() {
        return this.id;
    }

    /**
     * Setter for id.
     *
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter for entry.
     *
     * @return the entry
     */
    public VocabularyEntry getEntry() {
        return this.entry;
    }

    /**
     * Setter for entry.
     *
     * @param entry the entry to set
     */
    public void setEntry(VocabularyEntry entry) {
        this.entry = entry;
    }

    /**
     * Getter for othersLabel.
     *
     * @return the othersLabel
     */
    public String getOthersLabel() {
        return this.othersLabel;
    }

    /**
     * Setter for othersLabel.
     *
     * @param othersLabel the othersLabel to set
     */
    public void setOthersLabel(String othersLabel) {
        this.othersLabel = othersLabel;
    }

    /**
     * Getter for preselect.
     *
     * @return the preselect
     */
    public String getPreselect() {
        return this.preselect;
    }

    /**
     * Setter for preselect.
     *
     * @param preselect the preselect to set
     */
    public void setPreselect(String preselect) {
        this.preselect = preselect;
    }

}
