package fr.toutatice.portail.cms.nuxeo.portlets.forms;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.security.auth.Subject;


import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentAttachmentDTO;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.api.portlet.PrivilegedPortletModule;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandContext;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;


/**
 * TemplateModule pour ViewProcedurePortlet
 * 
 * @author Dorian Licois
 */
public class ProcedureTemplateModule extends PrivilegedPortletModule {
    
    /** Logger. */
    private static final Log LOGGER = LogFactory.getLog(ProcedureTemplateModule.class);

    private static final String VAR__RQST_USR = "${user}";

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
        List<Object> dashBoardColumns = dashboardM.getList("columns").list();
        request.setAttribute("dashboardColumns", dashBoardColumns);
        request.setAttribute("exportVarList", dashboardM.getList("exportVarList").list());
        Map<String, Map<String, String>> variablesDefinitions = getVariablesDefinitions(properties);
        request.setAttribute("variablesDefinitions", variablesDefinitions);
        Map<String, Map<String, String>> varsOptionsMap = buildVarOptionsMap(variablesDefinitions);
        request.setAttribute("varOptionsMap", varsOptionsMap);

        // get real values for each doc
        List<DocumentDTO> documents = (List<DocumentDTO>) request.getAttribute("documents");
        for (DocumentDTO documentDTO : documents) {
            updateDocValues(dashBoardColumns, variablesDefinitions, varsOptionsMap, documentDTO);
        }


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

        sortValue = StringUtils.defaultIfBlank(sortValue, ViewProcedurePortlet.DEFAULT_FIELD_TITLE);
        sortValue = StringUtils.removeStart(sortValue, ViewProcedurePortlet.DEFAULT_FIELD_PREFIX_RECORD);
        sortValue = StringUtils.removeStart(sortValue, ViewProcedurePortlet.DEFAULT_FIELD_PREFIX_PROCEDURE);
        request.setAttribute("sortValue", sortValue);
        request.setAttribute("sortOrder", StringUtils.defaultIfBlank(sortOrder, ViewProcedurePortlet.DEFAULT_SORT_ORDER));

    }

    /**
     * Update the doc with values to handle RADIOLIST, CHECKBOXLIST, SELECTLIST custom labels
     * 
     * @param dashBoardColumns
     * @param varsOptionsMap
     * @param documentDTO
     */
    private void updateDocValues(List<Object> dashBoardColumns, Map<String, Map<String, String>> variablesDefinitions,
            Map<String, Map<String, String>> varsOptionsMap, DocumentDTO documentDTO) {
        Map<String, String> globalVariablesValues = getGlobalVariableValues(documentDTO);

        if (globalVariablesValues != null) {
            for (Object columnO : dashBoardColumns) {
                PropertyMap column = (PropertyMap) columnO;

                String variableName = column.getString("variableName");
                String varValue = globalVariablesValues.get(variableName);
                Map<String, String> varOptionsMap = varsOptionsMap.get(variableName);
                Map<String, String> variableDefinition = variablesDefinitions.get(variableName);

                updateValue(documentDTO, variableName, varValue, varOptionsMap, variableDefinition);
            }
        }
    }

    /**
     * retrieves the globalVariablesValues properties based on document type
     * 
     * @param documentDTO
     * @return
     */
    private Map<String, String> getGlobalVariableValues(DocumentDTO documentDTO) {
        Map<String, Object> docProperties = documentDTO.getProperties();

        Map<String, String> globalVariablesValues = null;
        if (StringUtils.equals(documentDTO.getDocument().getType(), "Record")) {
            globalVariablesValues = (Map<String, String>) docProperties.get("rcd:globalVariablesValues");
        } else if (StringUtils.equals(documentDTO.getDocument().getType(), "ProcedureInstance")) {
            globalVariablesValues = (Map<String, String>) docProperties.get("pi:globalVariablesValues");
        }
        return globalVariablesValues;
    }


    /**
     * Update document with customized values.
     * 
     * @param documentDTO document DTO
     * @param variableName variable name
     * @param varValue variable value
     * @param varOptionsMap variable options
     * @param variableDefinition variable definition
     */
    private void updateValue(DocumentDTO documentDTO, String variableName, String varValue, Map<String, String> varOptionsMap,
            Map<String, String> variableDefinition) {
        if (StringUtils.isNotBlank(varValue)) {
            Object value;

            String type;
            if (variableDefinition == null) {
                type = null;
            } else {
                type = variableDefinition.get("type");
            }

            if ("FILE".equals(type) || "PICTURE".equals(type)) {
                try {
                    JSONObject jsonObject = JSONObject.fromObject(varValue);
                    String digest = jsonObject.getString("digest");
                    String fileName = jsonObject.getString("fileName");

                    value = null;
                    Iterator<DocumentAttachmentDTO> iterator = documentDTO.getAttachments().iterator();

                    while ((value == null) && iterator.hasNext()) {
                        DocumentAttachmentDTO attachment = iterator.next();

                        if (StringUtils.equals(digest, attachment.getDigest()) && StringUtils.equals(fileName, attachment.getName())) {
                            value = attachment;
                        }
                    }
                } catch (JSONException e) {
                    value = null;
                }
            } else if (varOptionsMap != null) {
                String[] splittedValues = StringUtils.split(varValue, ',');
                for (int j = 0; j < splittedValues.length; j++) {
                    splittedValues[j] = varOptionsMap.get(splittedValues[j]) != null ? varOptionsMap.get(splittedValues[j]) : splittedValues[j];
                }

                value = StringUtils.join(splittedValues, ',');
            } else {
                value = varValue;
            }


            documentDTO.getProperties().put(variableName, value);
        }
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

    @Override
    protected void serveResource(ResourceRequest request, ResourceResponse response, PortletContext portletContext) throws PortletException, IOException {
        final String resourceID = request.getResourceID();
        if (StringUtils.equals(resourceID, "exportCSV")) {

            PortalWindow window = WindowFactory.getWindow(request);

            String procedureModelWebid = window.getProperty(ViewProcedurePortlet.PROCEDURE_MODEL_ID_WINDOW_PROPERTY);
            String dashboardNameProperty = window.getProperty(ViewProcedurePortlet.DASHBOARD_ID_WINDOW_PROPERTY);

            NuxeoController nuxeoController = new NuxeoController(request, response, portletContext);

            Document procedureModel = getDocument(nuxeoController, procedureModelWebid);

            PropertyMap properties = procedureModel.getProperties();

            PropertyMap dashboardM = getDashboard(properties, dashboardNameProperty);

            Map<String, Map<String, String>> variablesDefinitions = getVariablesDefinitions(properties);

            PropertyList exportVarList = (PropertyList) dashboardM.getList("exportVarList");
            String dashboardName = dashboardM.getString("name");

            List<DocumentDTO> documents = (List<DocumentDTO>) request.getAttribute("documents");

            // build the CSV and write to output
            response.setContentType("text/csv");
            response.setCharacterEncoding("UTF-8");
            response.setProperty("Content-disposition", "attachment; filename=\"" + dashboardName + ".csv" + "\"");
            try {
                printCSV(documents, exportVarList, variablesDefinitions, response.getPortletOutputStream(), nuxeoController);
            } catch (IOException e) {
                throw new PortletException(e);
            }
        }
    }


    /**
     * prints csv file to the outputstream
     * 
     * @param documents
     * @param portletOutputStream
     * @param nuxeoController
     */
    private void printCSV(List<DocumentDTO> documents, PropertyList exportVarList, Map<String, Map<String, String>> variablesDefinitions,
            OutputStream portletOutputStream, NuxeoController nuxeoController) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(portletOutputStream);

        // force UTF-8 encoding
        writer.write('\ufeff');

        CSVPrinter printer;
        try {
            String header[] = null;
            if(exportVarList!=null && !exportVarList.isEmpty()){
                header = new String[exportVarList.size()];
                for (int i = 0; i < exportVarList.size(); i++) {
                    String exportVar = exportVarList.getString(i);
                    Map<String, String> exportVarDef = variablesDefinitions.get(exportVar);
                    header[i] = exportVarDef != null ? StringUtils.defaultIfBlank(exportVarDef.get("label"), exportVar) : exportVar;
                }
                printer = CSVFormat.EXCEL.withHeader(header).print(writer);
                Object[] record = null;
                for (DocumentDTO documentDTO : documents) {
                    final Map<String, Object> docProperties = documentDTO.getProperties();
                    
                    Map<String, String> globalVariablesValues = getGlobalVariableValues(documentDTO);
                    
                    record = new Object[exportVarList.size()];
                    for (int i = 0; i < exportVarList.size(); i++) {
                        
                        Map<String, Map<String, String>> varsOptionsMap = buildVarOptionsMap(variablesDefinitions);

                        String variableName = exportVarList.getString(i);
                        String varValue = globalVariablesValues.get(variableName);
                        Map<String, String> varOptionsMap = varsOptionsMap.get(variableName);
                        Map<String, String> variableDefinition = variablesDefinitions.get(variableName);

                        updateValue(documentDTO, variableName, varValue, varOptionsMap, variableDefinition);
                        
                        record[i] = docProperties.get(variableName);
                    }
                    printer.printRecord(record);
                }
                printer.close();
            }
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    /**
     * handle RADIOLIST, CHECKBOXLIST, SELECTLIST custom labels
     * 
     * @param variablesDefinitions
     * @return
     */
    private Map<String, Map<String, String>> buildVarOptionsMap(Map<String, Map<String, String>> variablesDefinitions) {

        Map<String, Map<String, String>> varsOptionsMap = new HashMap<String, Map<String, String>>(variablesDefinitions.size());

        for (Map<String, String> variablesDefinition : variablesDefinitions.values()) {

            String varName = variablesDefinition.get("name");
            String varOptions = variablesDefinition.get("varOptions");

            Map<String, String> varOptionsMap = null;
            if (StringUtils.isNotBlank(varOptions)) {
                varOptions = StringUtils.substringBetween(varOptions, "[", "]");
                String[] varOptionT = StringUtils.splitByWholeSeparator(varOptions, "},{");

                if (varOptionT != null) {
                    varOptionsMap = new HashMap<String, String>(varOptionT.length);
                    for (int j = 0; j < varOptionT.length; j++) {
                        String varOption = varOptionT[j];
                        String[] varOptionLV = StringUtils.split(varOption, ',');
                        if (varOptionLV != null) {
                            String varOptionValue = null;
                            String varOptionLabel = null;
                            for (int k = 0; k < varOptionLV.length; k++) {
                                String varOptionLVS = varOptionLV[k];
                                varOptionLVS = StringUtils.replaceChars(varOptionLVS, "\"{}", StringUtils.EMPTY);

                                if (StringUtils.startsWith(varOptionLVS, "label")) {
                                    varOptionLabel = StringUtils.substringAfterLast(varOptionLVS, ":");
                                } else if (StringUtils.startsWith(varOptionLVS, "value")) {
                                    varOptionValue = StringUtils.substringAfterLast(varOptionLVS, ":");
                                }
                            }
                            if (StringUtils.isNotBlank(varOptionValue) && StringUtils.isNotBlank(varOptionLabel)) {
                                varOptionsMap.put(varOptionValue, varOptionLabel);
                            }
                        }
                    }
                }
            }
            varsOptionsMap.put(varName, varOptionsMap);
        }
        return varsOptionsMap;
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

                    if (isAuthorized(groups)) {
                        // apply dashboard request
                        String requestFilter = dashboardM.getString("requestFilter");
                        return parseRequest(requestFilter, request.getUserPrincipal().getName());
                    }

                }
            } else {
                // no group check for recordFolder
                return StringUtils.EMPTY;
            }
        }

        return FILTER_NO_RESULTS;
    }

    /**
     * Check current user group is part of the validated groups for the dashboard
     * 
     * @param groups
     * @param iter
     */
    private boolean isAuthorized(PropertyList groups) {
        if (groups == null || groups.isEmpty() || groups.list().contains("members")) {
            return true;
        }
        //TODO : tomcat migration : no policy context
/*
        // Get the current authenticated subject through the JACC contract
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
                        return true;
                    }
                }
            }
        }

*/
        return false;
    }

    private String parseRequest(String request, String user) {

        request = StringUtils.replace(request, VAR__RQST_USR, user);

        return request;
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
        Map<String, String> variableDefinition = new HashMap<String, String>();

        variableDefinition.put("name", name);
        variableDefinition.put("label", label);
        variableDefinition.put("type", type);
        variableDefinition.put("varOptions", varOptions);

        if ("VOCABULARY".equals(type) && StringUtils.isNotBlank(varOptions)) {
            String vocabularyId;
            try {
                JSONObject object = JSONObject.fromObject(varOptions);
                vocabularyId = object.getString("vocabularyId");
            } catch (JSONException e) {
                vocabularyId = null;
            }
            variableDefinition.put("vocabularyId", vocabularyId);
        }

        return variableDefinition;
    }

}
