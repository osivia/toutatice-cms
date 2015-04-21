package fr.toutatice.portail.cms.nuxeo.tags;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.lang.StringUtils;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.VocabularyHelper;

/**
 * Get vocabulary label tag.
 *
 * @author Cédric Krommenhoek
 * @see SimpleTagSupport
 */
public class GetVocabularyLabelTag extends SimpleTagSupport {

    /** Vocabulary name. */
    private String name;
    /** Vocabulary key. */
    private String key;


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
        // Request
        ServletRequest request = pageContext.getRequest();
        // Nuxeo controller
        NuxeoController nuxeoController = (NuxeoController) request.getAttribute("nuxeoController");

        if (nuxeoController != null) {
        	
        	StringBuilder sb = new StringBuilder("");
        	String[] keys ;
        	if(StringUtils.contains(this.key, "[")) {
        		String[] substringsBetween = StringUtils.substringsBetween(this.key, "[", "]");
        		keys = StringUtils.split(substringsBetween[0], ",");
        	}
        	else {
        		keys = new String[1];
        		keys[0] = this.key;
        	}
        	
        	for(int i =0; i<keys.length; i++) {
        		
        		if(i>0) {
        			sb.append(", ");
        		}
        		
        		keys[i] = StringUtils.trim(keys[i]);
        		
        		if(StringUtils.contains(keys[i], "/")) {
        			String[] subKeys = StringUtils.split(keys[i], "/");
        			sb.append(StringUtils.clean(VocabularyHelper.getVocabularyLabel(nuxeoController, this.name, subKeys[0])));
        			sb.append(" / ");
        			sb.append(StringUtils.clean(VocabularyHelper.getVocabularyLabel(nuxeoController, this.name,subKeys[1])));
        			
        		}
        		else {
        			sb.append(StringUtils.clean(VocabularyHelper.getVocabularyLabel(nuxeoController, this.name, keys[i])));
        		}
        		
        	}
        	
        	
            String label = sb.toString();

            if (label != null) {
                JspWriter out = pageContext.getOut();
                out.write(label);
            }
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
