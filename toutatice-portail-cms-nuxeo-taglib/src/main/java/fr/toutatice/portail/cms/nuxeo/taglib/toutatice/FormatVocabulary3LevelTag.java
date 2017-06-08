package fr.toutatice.portail.cms.nuxeo.taglib.toutatice;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyList;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.VocabularyHelper;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.taglib.common.ToutaticeSimpleTag;

/**
 * Format vocabulary 3 level tag.
 * 
 * @author Cédric Krommenhoek
 * @see SimpleTagSupport
 */
public class FormatVocabulary3LevelTag extends ToutaticeSimpleTag {

    /** Vocabulary parent. */
    private String vocabulary;
    /** Vocabulary child #1. */
    private String child1;
    /** Vocabulary child #2. */
    private String child2;
    /** Vocabulary XPath. */
    private String xpath;

    /** Log. */
    private final Log log;


    /**
     * Constructor.
     */
    public FormatVocabulary3LevelTag() {
        super();
        this.log = LogFactory.getLog(this.getClass());
    }

    /**
     * Setter for vocabulary.
     * 
     * @param vocabulary the vocabulary to set
     */
    public void setVocabulary(String vocabulary) {
        this.vocabulary = vocabulary;
    }

    /**
     * Setter for child1.
     * 
     * @param child1 the child1 to set
     */
    public void setChild1(String child1) {
        this.child1 = child1;
    }

    /**
     * Setter for child2.
     * 
     * @param child2 the child2 to set
     */
    public void setChild2(String child2) {
        this.child2 = child2;
    }

    /**
     * Setter for xpath.
     * 
     * @param xpath the xpath to set
     */
    public void setXpath(String xpath) {
        this.xpath = xpath;
    }


    private String formatVocabulary3Level(NuxeoController ctx, Document doc, String vocParent, String vocChild1, String vocChild2, String xpath)
            throws Exception {
        StringBuffer stb = new StringBuffer();
        String value = doc.getProperties().getString(xpath);
        if (value != null) {
            String[] tab = value.split("/");

            stb.append(VocabularyHelper.getVocabularyLabel(ctx, vocParent, tab[0]));

            if (tab.length > 1) {
                stb.append(" / ");
                stb.append(VocabularyHelper.getVocabularyLabel(ctx, vocChild1, tab[1]));
            }
            if (tab.length > 2 && vocChild2 != null) {
                stb.append(" / ");
                stb.append(VocabularyHelper.getVocabularyLabel(ctx, vocChild2, tab[2]));
            }
        }
        return stb.toString();
    }

    private String formatVocabulary3Level(NuxeoController ctx, String value, String vocParent, String vocChild1, String vocChild2) throws Exception {
        StringBuffer stb = new StringBuffer();
        if (value != null) {
            String[] tab = value.split("/");

            stb.append(VocabularyHelper.getVocabularyLabel(ctx, vocParent, tab[0]));

            if (tab.length > 1) {
                stb.append(" / ");
                stb.append(VocabularyHelper.getVocabularyLabel(ctx, vocChild1, tab[1]));
            }
            if (tab.length > 2 && vocChild2 != null) {
                stb.append(" / ");
                stb.append(VocabularyHelper.getVocabularyLabel(ctx, vocChild2, tab[2]));
            }
        }
        return stb.toString();
    }


    @Override
    protected void doTag(NuxeoController nuxeoController, DocumentDTO document) throws JspException, IOException {
        // Context
        PageContext pageContext = (PageContext) this.getJspContext();

        if ((nuxeoController != null) && (document != null)) {
            // Nuxeo document
            Document nuxeoDocument = document.getDocument();

            Object obj = nuxeoDocument.getProperties().get(xpath);

            String result = "";
            try {
                if (obj instanceof PropertyList) {
                    StringBuilder stb = new StringBuilder();
                    for (int i = 0; i < ((PropertyList) obj).size(); i++) {

                        stb.append(formatVocabulary3Level(nuxeoController, ((PropertyList) obj).getString(i), this.vocabulary, this.child1, this.child2));
                        stb.append("<br/>");
                    }
                    result = stb.toString();
                } else {
                    result = formatVocabulary3Level(nuxeoController, nuxeoDocument, this.vocabulary, this.child1, this.child2, this.xpath);
                }
                JspWriter out = pageContext.getOut();
                out.write(result);
            } catch (Exception e) {
                log.error(e.getLocalizedMessage(), e);
            }
        }
    }

}
