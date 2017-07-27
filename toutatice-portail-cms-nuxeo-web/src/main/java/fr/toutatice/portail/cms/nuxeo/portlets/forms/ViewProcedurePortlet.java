package fr.toutatice.portail.cms.nuxeo.portlets.forms;

import java.util.List;
import java.util.Locale;

import org.osivia.portal.api.windows.PortalWindow;

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
    public static final String DASHBOARD_ID_WINDOW_PROPERTY = "osivia.forms.list.dashboard.id";

    public static final String DEFAULT_FIELD_PREFIX = "rcd:globalVariablesValues.";

    public static final String DEFAULT_SORT_ORDER = "ASC";

    public static final String DEFAULT_FIELD_TITLE = DEFAULT_FIELD_PREFIX + "_title";

    /**
     * Constructor.
     */
    public ViewProcedurePortlet() {
        super();
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
        
        configuration.setUseES(true);
        configuration.setVersion("1");
        window.setProperty(VERSION_WINDOW_PROPERTY, "1");
        configuration.setContentFilter(String.valueOf(NuxeoQueryFilterContext.STATE_LIVE_N_PUBLISHED));
        configuration.setBeanShell(true);

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
