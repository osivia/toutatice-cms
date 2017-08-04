package fr.toutatice.portail.cms.nuxeo.portlets.forms;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.portal.security.impl.jacc.JACCPortalPrincipal;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;
import org.osivia.portal.core.web.IWebIdService;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.PageSelectors;
import fr.toutatice.portail.cms.nuxeo.api.cms.NuxeoDocumentContext;
import fr.toutatice.portail.cms.nuxeo.api.portlet.PrivilegedPortletModule;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandContext;


/**
 * TemplateModule pour ViewProcedurePortlet
 * 
 * @author Dorian Licois
 */
public class ProcedureTemplateModule extends PrivilegedPortletModule {
    
    /**
     * @param portletContext
     */
    public ProcedureTemplateModule(PortletContext portletContext) {
        super(portletContext);
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.toutatice.portail.cms.nuxeo.api.portlet.PrivilegedPortletModule#getAuthType()
     */
    public int getAuthType() {
        return NuxeoCommandContext.AUTH_TYPE_SUPERUSER;
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.toutatice.portail.cms.nuxeo.api.portlet.PortletModule#doView(javax.portlet.RenderRequest, javax.portlet.RenderResponse,
     * javax.portlet.PortletContext)
     */
    @Override
    protected void doView(RenderRequest request, RenderResponse response, PortletContext portletContext) throws PortletException, IOException {
        super.doView(request, response, portletContext);

        PortalWindow window = WindowFactory.getWindow(request);

        String procedureModelWebid = window.getProperty(ViewProcedurePortlet.PROCEDURE_MODEL_ID_WINDOW_PROPERTY);
        String dashboardName = window.getProperty(ViewProcedurePortlet.DASHBOARD_ID_WINDOW_PROPERTY);

        NuxeoController nuxeoController = new NuxeoController(request, response, portletContext);
        
        Document procedureModel = getDocument(nuxeoController, procedureModelWebid);
        
        PropertyMap properties = procedureModel.getProperties();
        
        PropertyMap dashboardM = getDashboard(properties, dashboardName);
        
        request.setAttribute("dashboardName", dashboardM.getString("name"));
        request.setAttribute("dashboardColumns", dashboardM.getList("columns").list());
        request.setAttribute("variablesDefinitions", getVariablesDefinitions(properties));

        Map<String, List<String>> selectors = PageSelectors.decodeProperties(request.getParameter("selectors"));
        List<String> sortValueL = selectors.get("sortValue");
        List<String> sortOrderL = selectors.get("sortOrder");

        String sortValue = null;
        if (CollectionUtils.isNotEmpty(sortValueL)) {
            sortValue = sortValueL.get(0);
        }
        String sortOrder = null;
        if (CollectionUtils.isNotEmpty(sortOrderL)) {
            sortOrder = sortOrderL.get(0);
        }

        sortValue = StringUtils.removeStart(sortValue, ViewProcedurePortlet.DEFAULT_FIELD_PREFIX_RECORD);
        sortValue = StringUtils.removeStart(sortValue, ViewProcedurePortlet.DEFAULT_FIELD_PREFIX_PROCEDURE);
        sortValue = StringUtils.defaultIfBlank(sortValue, ViewProcedurePortlet.DEFAULT_FIELD_TITLE);
        request.setAttribute("sortValue", sortValue);
        request.setAttribute("sortOrder", StringUtils.defaultIfBlank(sortOrder, ViewProcedurePortlet.DEFAULT_SORT_ORDER));

    }


    @Override
    protected void processAction(ActionRequest request, ActionResponse response, PortletContext portletContext) throws PortletException, IOException {
        super.processAction(request, response, portletContext);

        // Action
        String action = request.getParameter(ActionRequest.ACTION_NAME);
        // Current window
        PortalWindow window = WindowFactory.getWindow(request);

        if (StringUtils.equals("admin", request.getPortletMode().toString())) {
            if ("save".equals(action)) {
                window.setProperty(ViewProcedurePortlet.PROCEDURE_MODEL_ID_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("procedureModelId")));
                window.setProperty(ViewProcedurePortlet.DASHBOARD_ID_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("dashboardId")));
            }
        } else if (StringUtils.equals(PortletMode.VIEW.toString(), request.getPortletMode().toString())) {

            Map<String, List<String>> selectors = PageSelectors.decodeProperties(request.getParameter("selectors"));
            List<String> sortValueL = selectors.get("sortValue");
            List<String> sortOrderL = selectors.get("sortOrder");
            if (sortValueL == null) {
                sortValueL = new ArrayList<String>();
            } else {
                sortValueL.clear();
            }
            if (sortOrderL == null) {
                sortOrderL = new ArrayList<String>();
            } else {
                sortOrderL.clear();
            }

            String sortValue = request.getParameter("sortValue");
            if (!StringUtils.startsWith(sortValue, "dc:")) {

                if (StringUtils.equals(window.getProperty("osivia.doctype"), "RecordFolder")) {
                    sortValue = ViewProcedurePortlet.DEFAULT_FIELD_PREFIX_RECORD.concat(sortValue);
                } else {
                    sortValue = ViewProcedurePortlet.DEFAULT_FIELD_PREFIX_PROCEDURE.concat(sortValue);
                }

            }
            
            sortValueL.add(StringUtils.defaultIfBlank(sortValue, ViewProcedurePortlet.DEFAULT_FIELD_TITLE));
            sortOrderL.add(StringUtils.defaultIfBlank(request.getParameter("sortOrder"), ViewProcedurePortlet.DEFAULT_SORT_ORDER));

            selectors.put("sortValue", sortValueL);
            selectors.put("sortOrder", sortOrderL);
            response.setRenderParameter("selectors", PageSelectors.encodeProperties(selectors));
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see fr.toutatice.portail.cms.nuxeo.api.portlet.PrivilegedPortletModule#getFilter(org.osivia.portal.api.context.PortalControllerContext)
     */
    public String getFilter(PortalControllerContext portalControllerContext) {

        // Portlet request
        PortletRequest request = portalControllerContext.getRequest();
        
        PortalWindow window = WindowFactory.getWindow(request);

        String procedureModelWebid = window.getProperty(ViewProcedurePortlet.PROCEDURE_MODEL_ID_WINDOW_PROPERTY);
        String dashboardName = window.getProperty(ViewProcedurePortlet.DASHBOARD_ID_WINDOW_PROPERTY);

        NuxeoController nuxeoController = new NuxeoController(portalControllerContext);

        if (StringUtils.isNotBlank(procedureModelWebid)) {
            Document procedureModel = getDocument(nuxeoController, procedureModelWebid);
            if (!StringUtils.equals(procedureModel.getType(), "RecordFolder")) {
                PropertyMap dashboardM = getDashboard(procedureModel.getProperties(), dashboardName);
                if (dashboardM != null) {

                    PropertyList groups = dashboardM.getList("groups");

                    // Get the current authenticated subject through the JACC
                    // contract
                    Subject subject = null;
                    try {
                        subject = (Subject) PolicyContext.getContext("javax.security.auth.Subject.container");
                    } catch (PolicyContextException e) {
                    }

                    if (subject != null) {
                        JACCPortalPrincipal pp = new JACCPortalPrincipal(subject);
                        Iterator iter = pp.getRoles().iterator();
                        while (iter.hasNext()) {
                            Principal principal = (Principal) iter.next();
                            String groupName = principal.getName();

                            for (Object groupO : groups.list()) {
                                String group = (String) groupO;
                                if (StringUtils.equals(groupName, group)) {
                                    // apply dashboard request
                                    String requestFilter = dashboardM.getString("requestFilter");
                                    return requestFilter;
                                }
                            }
                        }
                    }
                }
            } else {
                // no group check for recordFolder
                return StringUtils.EMPTY;
            }
        }

        return FILTER_NO_RESULTS;
    }

    private Document getDocument(NuxeoController nuxeoController, String procedureModelWebid) {
        NuxeoDocumentContext procedureModelContext = nuxeoController.getDocumentContext(IWebIdService.FETCH_PATH_PREFIX + procedureModelWebid);
        Document procedureModel = procedureModelContext.getDocument();
        return procedureModel;
    }

    /**
     * returns the dashboard of a procedure by name
     * 
     * @param procedureModel
     * @param dashboardName
     */
    private PropertyMap getDashboard(PropertyMap properties, String dashboardName) {
        PropertyList dashboardsList = properties.getList("pcd:dashboards");

        if (StringUtils.isNotBlank(dashboardName)) {
            for (Object dashboardO : dashboardsList.list()) {
                PropertyMap dashboardM = (PropertyMap) dashboardO;
                if (StringUtils.equals(dashboardM.getString("name"), dashboardName)) {
                    return dashboardM;
                }
            }
        } else {
            return dashboardsList.getMap(0);
        }

        return null;
    }

    /**
     * returns a map of all the variables defined in a model
     * 
     * @param properties
     * @return variablesDefinitions
     */
    private Map<String, Map<String, String>> getVariablesDefinitions(PropertyMap properties) {
        
        PropertyList varDef = properties.getList("pcd:globalVariablesDefinitions");

        Map<String, Map<String, String>> variablesDefinitions = new HashMap<String, Map<String, String>>(varDef.size()+2);
        for (Object varDefO : varDef.list()) {
            PropertyMap varDefM = (PropertyMap) varDefO;

            String name = varDefM.getString("name");
            String label = varDefM.getString("label");
            String type = varDefM.getString("type");
            String varOptions = varDefM.getString("varOptions");

            variablesDefinitions.put(name, buildVariableDefinition(name, label, type, varOptions));
        }
        
        variablesDefinitions.put("dc:created", buildVariableDefinition("dc:created", "Créé le", "DATETIME", null));
        variablesDefinitions.put("dc:modified", buildVariableDefinition("dc:modified", "Modifié le", "DATETIME", null));
        
        return variablesDefinitions;
    }

    private Map<String, String> buildVariableDefinition(String name, String label, String type, String varOptions) {
        Map<String, String> variableDefinition = new HashMap<String, String>(4);

        variableDefinition.put("name", name);
        variableDefinition.put("label", label);
        variableDefinition.put("type", type);
        variableDefinition.put("varOptions", varOptions);

        return variableDefinition;
    }

}
