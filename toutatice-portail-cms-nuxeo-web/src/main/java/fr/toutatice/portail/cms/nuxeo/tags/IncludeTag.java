package fr.toutatice.portail.cms.nuxeo.tags;

import java.io.IOException;

import javax.portlet.PortletContext;
import javax.portlet.RenderRequest;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.CustomizationPluginMgr;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.DefaultCMSCustomizer;

/**
 * Transform HTML content tag.
 * 
 * @author Cédric Krommenhoek
 * @see SimpleTagSupport
 */
public class IncludeTag extends SimpleTagSupport {


    /** Nuxeo document HTML content property name. */
    private String page;


    public String getPage() {
        return page;
    }


    public void setPage(String page) {
        this.page = page;
    }


    /**
     * Default constructor.
     */
    public IncludeTag() {
        super();
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
            NuxeoController nuxeoController = (NuxeoController) request.getAttribute("nuxeoController");
            
            String path = page;
           
            
            if( ! getPage().startsWith("/"))    {
                String servletPath = (String) request.getAttribute("javax.servlet.include.servlet_path");     
                
                String parentPath = servletPath.substring(0, StringUtils.lastIndexOf(servletPath, '/') + 1);
                path = parentPath + page;
            }
            
            String customName = ((DefaultCMSCustomizer)nuxeoController.getNuxeoCMSService().getCMSCustomizer()).getPluginMgr().customizeJSP( path, nuxeoController.getPortletCtx(), nuxeoController.getRequest());

            pageContext.include(customName);
            


        }

        catch (ServletException e) {
            throw new JspException(e);

        }


    }


}
