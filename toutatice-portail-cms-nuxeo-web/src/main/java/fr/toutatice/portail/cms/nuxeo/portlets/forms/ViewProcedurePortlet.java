package fr.toutatice.portail.cms.nuxeo.portlets.forms;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSecurityException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.windows.PortalWindow;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilterContext;
import fr.toutatice.portail.cms.nuxeo.api.domain.ListTemplate;
import fr.toutatice.portail.cms.nuxeo.portlets.list.ListConfiguration;
import fr.toutatice.portail.cms.nuxeo.portlets.list.ViewListPortlet;


/**
 * Portlet List des procédures
 * 
 * @author Dorian Licois
 */
public class ViewProcedurePortlet extends ViewListPortlet {

    private static final Integer MAXIMIZED_PAGINATION = 10;

    private static final Integer NORMAL_PAGINATION = 10;

    /** PATH_ADMIN */
    protected static final String PATH_ADMIN = "/WEB-INF/jsp/forms/admin.jsp";

    /** PROCEDURE_MODEL_ID_WINDOW_PROPERTY */
    public static final String PROCEDURE_MODEL_ID_WINDOW_PROPERTY = "osivia.forms.list.model.id";
    
    /** DASHBOARD_ID_WINDOW_PROPERTY */
    public static final String DASHBOARD_ID_WINDOW_PROPERTY = "osivia.forms.dashboard.id";

    public static final String DEFAULT_FIELD_PREFIX_RECORD = "rcd:globalVariablesValues.";

    public static final String DEFAULT_FIELD_PREFIX_PROCEDURE = "pi:globalVariablesValues.";

    public static final String DEFAULT_SORT_ORDER = "ASC";

    public static final String DEFAULT_FIELD_TITLE = DEFAULT_FIELD_PREFIX_RECORD + "_title";

    /**
     * Constructor.
     */
    public ViewProcedurePortlet() {
        super();
    }

    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, PortletSecurityException, IOException {
        try {
        	// Version is computed dynamically
        	request.setAttribute(Constants.REQUEST_ATTR_VERSION, "1");
        	
            super.doView(request, response);
        } catch (PortletException e) {
            String rootCauseMessage = null;
            if (e.getCause() != null && e.getCause() instanceof PortletException) {
                Throwable t = e.getCause();
                if (t.getCause() != null && t.getCause() instanceof NuxeoException) {
                    rootCauseMessage = ExceptionUtils.getRootCause(t).getMessage();
                }
            }
            if (rootCauseMessage != null) {
                request.setAttribute("error", "LIST_MESSAGE_INVALID_REQUEST");
                request.setAttribute("errorMessage", rootCauseMessage);
                response.setContentType("text/html");
                PortletRequestDispatcher dispatcher = this.getPortletContext().getRequestDispatcher(PATH_VIEW);
                dispatcher.include(request, response);
            } else {
                throw e;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.toutatice.portail.cms.nuxeo.portlets.list.ViewListPortlet#getConfiguration(org.osivia.portal.api.windows.PortalWindow)
     */
    protected ListConfiguration getConfiguration(PortalWindow window) {
        ViewProcedureConfiguration configuration = new ViewProcedureConfiguration(super.getConfiguration(window));

        StringBuilder requestSb = new StringBuilder();

        requestSb.append("StringBuilder nuxeoRequest = new StringBuilder();\n");
        requestSb.append("nuxeoRequest.append(\"(ecm:primaryType = 'ProcedureInstance' \");\n");
        requestSb.append("nuxeoRequest.append(\"AND pi:procedureModelWebId = '\");\n");
        requestSb.append("nuxeoRequest.append(\"" + window.getProperty(PROCEDURE_MODEL_ID_WINDOW_PROPERTY) + "\");\n");
        requestSb.append("nuxeoRequest.append(\"') \");\n");
        requestSb.append("nuxeoRequest.append(\"OR (ecm:primaryType = 'Record' \");\n");
        requestSb.append("nuxeoRequest.append(\"AND rcd:procedureModelWebId = '\");\n");
        requestSb.append("nuxeoRequest.append(\"" + window.getProperty(PROCEDURE_MODEL_ID_WINDOW_PROPERTY) + "\");\n");
        requestSb.append("nuxeoRequest.append(\"') \");\n");
        
        requestSb.append("nuxeoRequest.append(\"ORDER BY \");\n");
        requestSb.append("if(params!=null && params.get(\"sortValue\") != null && params.get(\"sortValue\").size() >0 &&"
                + " params.get(\"sortOrder\") != null && params.get(\"sortOrder\").size() >0){\n");

        requestSb.append("nuxeoRequest.append(params.get(\"sortValue\").get(0));\n");
        requestSb.append("nuxeoRequest.append(\" \");\n");
        requestSb.append("nuxeoRequest.append(params.get(\"sortOrder\").get(0));\n");

        requestSb.append("} else {\n");
        requestSb.append("nuxeoRequest.append(\"" + DEFAULT_FIELD_TITLE + "\");\n");
        requestSb.append("nuxeoRequest.append(\" " + DEFAULT_SORT_ORDER + "\");\n");
        
        requestSb.append("}\n");


        requestSb.append("return nuxeoRequest.toString();");


        configuration.setNuxeoRequest(requestSb.toString());
        configuration.setBeanShell(true);
        

        configuration.setContentFilter(String.valueOf(NuxeoQueryFilterContext.STATE_LIVE_N_PUBLISHED));

        configuration.setProcedureModelId(window.getProperty(PROCEDURE_MODEL_ID_WINDOW_PROPERTY));
        configuration.setDashboardId(window.getProperty(DASHBOARD_ID_WINDOW_PROPERTY));

        configuration.setMaximizedPagination(MAXIMIZED_PAGINATION);
        configuration.setNormalPagination(NORMAL_PAGINATION);

        return configuration;
    }

    public ListTemplate getCurrentTemplate(Locale locale, ListConfiguration configuration) {

        // Search template
        ListTemplate currentTemplate = null;
        List<ListTemplate> templates = this.customizer.getListTemplates(locale);
        for (ListTemplate template : templates) {
            if (LIST_TEMPLATE_PROCEDURE.equals(template.getKey())) {
                return template;
            }
        }

        return currentTemplate;
    }

    /**
     * Getter for PATH_ADMIN.
     * 
     * @return the pathAdmin
     */
    public String getPathAdmin() {
        return PATH_ADMIN;
    }
}
