package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jboss.portal.common.invocation.Scope;
import org.jboss.portal.server.ServerInvocation;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.constants.InternalConstants;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.portlets.list.ListCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.publish.RequestPublishStatus;
import fr.toutatice.portail.cms.nuxeo.portlets.service.CMSService;

/**
 * Browser adapter.
 *
 * @author Cédric Krommenhoek
 */
public class BrowserAdapter {

    /** Current user workspaces principal attribute name. */
    protected static final String CURRENT_USER_WORKSPACES_PRINCIPAL_ATTRIBUTE = "osivia.browser.currentUserWorkspaces";


    /** User workspaces default path. */
    private static final String USER_WORKSPACES_DEFAULT_PATH = "/default-domain/UserWorkspaces";
    /** User workspaces default type. */
    private static final String USER_WORKSPACES_DEFAULT_TYPE = "Workspace";


    /** Singleton instance. */
    private static BrowserAdapter instance;


    /** CMS service. */
    private final CMSService cmsService;


    /**
     * Constructor.
     *
     * @param cmsService CMS service
     */
    protected BrowserAdapter(CMSService cmsService) {
        super();
        this.cmsService = cmsService;
    }


    /**
     * Get singleton instance.
     *
     * @param cmsService CMS service
     * @return singleton instance
     */
    public static BrowserAdapter getInstance(CMSService cmsService) {
        if (instance == null) {
            instance = new BrowserAdapter(cmsService);
        }
        return instance;
    }


    /**
     * Get workspaces.
     *
     * @param cmsContext CMS context
     * @param administrator administrator indicator
     * @return workspaces
     * @throws CMSException
     */
    public List<CMSItem> getWorkspaces(CMSServiceCtx cmsContext, boolean administrator) throws CMSException {
        List<CMSItem> workspaces;

        if (administrator && !this.areWorkspacesDisplayedForAdministrator()) {
            workspaces = new ArrayList<CMSItem>(0);
        } else {
            // Nuxeo controller
            NuxeoController nuxeoController = new NuxeoController(cmsContext.getRequest(), cmsContext.getResponse(), cmsContext.getPortletCtx());

            // Query
            String query = this.getWorkspacesQuery();
            // Schemas
            String schemas = this.getWorkspacesSchemas();
            // Portal policy filter
            String filter = InternalConstants.PORTAL_CMS_REQUEST_FILTERING_POLICY_NO_FILTER;
            
            String liveStatus = RequestPublishStatus.live.getStatus();
            INuxeoCommand nuxeoCommand = new ListCommand(query, liveStatus, 0, -1, schemas, filter);
            Documents documents = (Documents) nuxeoController.executeNuxeoCommand(nuxeoCommand);

            workspaces = new ArrayList<CMSItem>(documents.size());
            for (Document document : documents.list()) {
                CMSItem workspace = this.cmsService.createItem(cmsContext, document.getPath(), document.getTitle(), document);
                workspaces.add(workspace);
            }
        }

        return workspaces;
    }


    /**
     * Get current user workspaces.
     *
     * @param cmsContext CMS context
     * @return user workspaces
     * @throws CMSException
     */
    public List<CMSItem> getCurrentUserWorkspaces(CMSServiceCtx cmsContext) throws CMSException {
        // Server invocation
        ServerInvocation invocation = cmsContext.getServerInvocation();

        List<CMSItem> workspaces;

        Object attribute = invocation.getAttribute(Scope.PRINCIPAL_SCOPE, CURRENT_USER_WORKSPACES_PRINCIPAL_ATTRIBUTE);
        if ((attribute != null) && (attribute instanceof List<?>)) {
            List<?> list = (List<?>) attribute;
            workspaces = new ArrayList<CMSItem>(list.size());

            for (Object object : list) {
                if (object instanceof CMSItem) {
                    CMSItem workspace = (CMSItem) object;
                    workspaces.add(workspace);
                }
            }
        } else {
            // User name
            String userName = null;
            if (invocation.getServerContext().getClientRequest().getUserPrincipal() != null) {
                userName = invocation.getServerContext().getClientRequest().getUserPrincipal().getName();
            }

            if (userName == null) {
                workspaces = new ArrayList<CMSItem>(0);
            } else {
                workspaces = this.getUserWorkspaces(cmsContext, userName);
            }

            invocation.setAttribute(Scope.PRINCIPAL_SCOPE, CURRENT_USER_WORKSPACES_PRINCIPAL_ATTRIBUTE, workspaces);
        }

        return workspaces;
    }


    /**
     * Get all user workspaces.
     *
     * @param cmsContext CMS context
     * @return user workspaces
     * @throws CMSException
     */
    public List<CMSItem> getAllUserWorkspaces(CMSServiceCtx cmsContext) throws CMSException {
        return this.getUserWorkspaces(cmsContext, null);
    }


    /**
     * Get user workspaces.
     * 
     * @param cmsContext CMS context
     * @param userName user name, may be null for getting all user workspaces
     * @return user workspaces
     * @throws CMSException
     */
    public List<CMSItem> getUserWorkspaces(CMSServiceCtx cmsContext, String userName) throws CMSException {
        // Query
        String query = this.getUserWorkspacesQuery(userName);
        // Schemas
        String schemas = this.getWorkspacesSchemas();
        // Portal policy filter
        String filter = InternalConstants.PORTAL_CMS_REQUEST_FILTERING_POLICY_NO_FILTER;

        // User workspaces
        List<CMSItem> workspaces;
        try {
            String liveStatus = RequestPublishStatus.live.getStatus();
            INuxeoCommand nuxeoCommand = new ListCommand(query, liveStatus, 0, -1, schemas, filter);
            Documents documents = (Documents) this.cmsService.executeNuxeoCommand(cmsContext, nuxeoCommand);

            workspaces = new ArrayList<CMSItem>(documents.size());
            for (Document document : documents.list()) {
                CMSItem workspace = this.cmsService.createItem(cmsContext, document.getPath(), document.getTitle(), document);
                workspaces.add(workspace);
            }
        } catch (CMSException e) {
            throw e;
        } catch (Exception e) {
            throw new CMSException(e);
        }

        return workspaces;
    }


    /**
     * Check if workspaces are displayed for administrator.
     *
     * @return true if workspaces are displayed for administrator
     */
    protected boolean areWorkspacesDisplayedForAdministrator() {
        return true;
    }


    /**
     * Get user workspaces path.
     * 
     * @return path
     */
    public String getUserWorkspacesPath() {
        return USER_WORKSPACES_DEFAULT_PATH;
    }


    /**
     * Get user workspaces document type.
     * 
     * @return document type
     */
    public String getUserWorkspacesType() {
        return USER_WORKSPACES_DEFAULT_TYPE;
    }


    /**
     * Get workspaces query.
     *
     * @return query
     */
    public String getWorkspacesQuery() {
        StringBuilder query = new StringBuilder();
        query.append("ecm:primaryType = 'Workspace' AND NOT ecm:path STARTSWITH '");
        query.append(this.getUserWorkspacesPath());
        query.append("/'");
        return query.toString();
    }


    /**
     * Get user workspaces query.
     *
     * @param userName user name, may be empty for getting all user workspaces
     * @return query
     */
    public String getUserWorkspacesQuery(String userName) {
        StringBuilder query = new StringBuilder();
        query.append("ecm:primaryType = '");
        query.append(this.getUserWorkspacesType());
        query.append("' AND ecm:path STARTSWITH '");
        query.append(this.getUserWorkspacesPath());
        query.append("/'");
        if (StringUtils.isNotEmpty(userName)) {
            query.append(" AND dc:creator = '");
            query.append(userName);
            query.append("'");
        }
        return query.toString();
    }


    /**
     * Get workspaces schemas.
     *
     * @return schemas
     */
    public String getWorkspacesSchemas() {
        return "dublincore";
    }


    /**
     * Getter for cmsService.
     * 
     * @return the cmsService
     */
    public CMSService getCmsService() {
        return cmsService;
    }

}
