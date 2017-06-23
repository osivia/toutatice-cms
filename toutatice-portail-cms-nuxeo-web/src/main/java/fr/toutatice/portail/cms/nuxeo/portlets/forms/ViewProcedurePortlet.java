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

    /** PROCEDURE_MODEL_ID_WINDOW_PROPERTY */
    public static final String PROCEDURE_MODEL_ID_WINDOW_PROPERTY = "osivia.forms.list.model.id";

    /** DASHBOARD_ID_WINDOW_PROPERTY */
    public static final String DASHBOARD_ID_WINDOW_PROPERTY = "osivia.forms.list.dashboard.id";

    /** PATH_ADMIN */
    protected static final String PATH_ADMIN = "/WEB-INF/jsp/forms/admin.jsp";

    /** PATH_VIEW */
    protected static final String PATH_VIEW = "/WEB-INF/jsp/forms/view-procedure.jsp";


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
        requestSb.append("(ecm:primaryType = 'ProcedureInstance' ");
        requestSb.append("AND pi:procedureModelWebId = '");
        requestSb.append(window.getProperty(PROCEDURE_MODEL_ID_WINDOW_PROPERTY));
        requestSb.append("') ");
        requestSb.append("OR (ecm:primaryType = 'Record' ");
        requestSb.append("AND rcd:procedureModelWebId = '");
        requestSb.append(window.getProperty(PROCEDURE_MODEL_ID_WINDOW_PROPERTY));
        requestSb.append("') ");
        configuration.setNuxeoRequest(requestSb.toString());
        
        configuration.setUseES(true);
        configuration.setVersion("1");
        window.setProperty(VERSION_WINDOW_PROPERTY, "1");
        configuration.setContentFilter(String.valueOf(NuxeoQueryFilterContext.STATE_LIVE_N_PUBLISHED));

        configuration.setProcedureModelId(window.getProperty(PROCEDURE_MODEL_ID_WINDOW_PROPERTY));
        configuration.setDashboardId(window.getProperty(DASHBOARD_ID_WINDOW_PROPERTY));

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