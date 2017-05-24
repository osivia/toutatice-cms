package fr.toutatice.portail.cms.nuxeo.portlets.forms;

import java.io.IOException;
import java.security.Principal;
import java.util.Iterator;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;

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
        PropertyMap dashboardM = getDashboard(procedureModel, dashboardName);
        
        // if (StringUtils.equals(procedureModel.getType(), "RecordFolder")) {
        // PropertyList steps = getSteps(nuxeoController, procedureModel);
        // }

        request.setAttribute("dashboardName", dashboardM.getString("name"));
        request.setAttribute("dashboardColumns", dashboardM.getList("columns").list());


        // boolean creator = false, created = false, lastContributor = false, modified = false;
        // for (Object columnO : dashboardM.getList("columns").list()) {
        // PropertyMap columnM = (PropertyMap) columnO;
        // String variableName = columnM.getString("variableName");
        // if (!creator && StringUtils.equals(variableName, "dc:creator")) {
        // creator = true;
        // } else if (!created && StringUtils.equals(variableName, "dc:created")) {
        // created = true;
        // } else if (!lastContributor && StringUtils.equals(variableName, "dc:lastContributor")) {
        // lastContributor = true;
        // } else if (!modified && StringUtils.equals(variableName, "dc:modified")) {
        // modified = true;
        // }
        // }
        //
        // if (creator || created || lastContributor || modified) {
        // List<DocumentDTO> documents = (List<DocumentDTO>) request.getAttribute("documents");
        //
        // for (DocumentDTO documentDTO : documents) {
        // PropertyMap globalVariablesValuesM = (PropertyMap) documentDTO.getProperties().get("pi:globalVariablesValues");
        //
        // if(creator){
        // globalVariablesValuesM.map().put("dc:creator", documentDTO.getDocument().g);
        // }
        //
        //
        // }
        // }


    }

    @Override
    protected void processAction(ActionRequest request, ActionResponse response, PortletContext portletContext) throws PortletException, IOException {
        super.processAction(request, response, portletContext);

        // Action
        String action = request.getParameter(ActionRequest.ACTION_NAME);
        // Current window
        PortalWindow window = WindowFactory.getWindow(request);

        if ("admin".equals(request.getPortletMode().toString())) {
            if ("save".equals(action)) {
                window.setProperty(ViewProcedurePortlet.PROCEDURE_MODEL_ID_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("procedureModelId")));
                window.setProperty(ViewProcedurePortlet.DASHBOARD_ID_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("dashboardId")));
            }
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
                PropertyMap dashboardM = getDashboard(procedureModel, dashboardName);
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
                                    return StringUtils.EMPTY;
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
    private PropertyMap getDashboard(Document procedureModel, String dashboardName) {
        PropertyMap properties = procedureModel.getProperties();
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


    // private PropertyList getSteps(NuxeoController nuxeoController, Document procedureModel) {
    // PropertyMap properties = procedureModel.getProperties();
    //
    // String webIdParent = properties.getString("pcd:webIdParent");
    //
    // Document procedureModelParent = getDocument(nuxeoController, webIdParent);
    //
    //
    // return null;
    // }

}
