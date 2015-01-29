package fr.toutatice.portail.cms.nuxeo.portlets.selectors.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

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
        String label = VocabSelectorPortlet.getLabel(this.othersLabel, this.id, this.entry, this.preselect);

        // JSP writer
        JspWriter out = pageContext.getOut();
        out.write(label);
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
