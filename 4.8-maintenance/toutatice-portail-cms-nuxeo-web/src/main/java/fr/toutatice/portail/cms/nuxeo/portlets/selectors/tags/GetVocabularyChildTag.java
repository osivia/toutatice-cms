package fr.toutatice.portail.cms.nuxeo.portlets.selectors.tags;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import fr.toutatice.portail.cms.nuxeo.api.VocabularyEntry;

/**
 * Get vocabulary child tag.
 *
 * @author Cédric Krommenhoek
 * @see SimpleTagSupport
 */
public class GetVocabularyChildTag extends SimpleTagSupport {

    /** Vocabulary identifier. */
    private String id;
    /** Vocabulary entry. */
    private VocabularyEntry entry;
    /** Request variable name. */
    private String var;


    /**
     * Constructor.
     */
    public GetVocabularyChildTag() {
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

        // Child
        VocabularyEntry child = this.entry.getChild(this.id);

        request.setAttribute(this.var, child);
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
     * Getter for var.
     *
     * @return the var
     */
    public String getVar() {
        return this.var;
    }

    /**
     * Setter for var.
     *
     * @param var the var to set
     */
    public void setVar(String var) {
        this.var = var;
    }

}
