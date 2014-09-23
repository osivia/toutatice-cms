package fr.toutatice.portail.cms.nuxeo.tags;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;

/**
 * Set Nuxeo document date into request attribute tag.
 *
 * @author Cédric Krommenhoek
 * @see SimpleTagSupport
 */
public class SetDateTag extends SimpleTagSupport {

    /** Request attribute name. */
    private String var;
    /** Nuxeo document property name. */
    private String property;


    /**
     * Default constructor.
     */
    public SetDateTag() {
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
        // Nuxeo document
        Document document = (Document) request.getAttribute("nuxeoDocument");

        if (document != null) {
            // Date
            Date date;
            if (StringUtils.isEmpty(this.property)) {
                date = document.getDate("dc:modified");

                if (date == null) {
                    date = document.getDate("dc:created");
                }
            } else {
                date = document.getDate(this.property);
            }

            request.setAttribute(this.var, date);
        }
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


    /**
     * Getter for property.
     * 
     * @return the property
     */
    public String getProperty() {
        return this.property;
    }

    /**
     * Setter for property.
     * 
     * @param property the property to set
     */
    public void setProperty(String property) {
        this.property = property;
    }

}
