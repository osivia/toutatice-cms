package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import java.util.ArrayList;
import java.util.List;

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

    /** User workspaces principal attribute name. */
    protected static final String USER_WORKSPACES_PRINCIPAL_ATTRIBUTE = "osivia.browser.userWorkspaces";


    /** Singleton instance. */
    private static BrowserAdapter instance;


    /** CMS service. */
    private final CMSService cmsService;


    /**
     * Constructor.
     *
     * @param cmsService CMS service
     */
    private BrowserAdapter(CMSService cmsService) {
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
     * Get user workspaces.
     *
     * @param cmsContext CMS context
     * @return user workspaces
     * @throws CMSException
     */
    public List<CMSItem> getUserWorkspaces(CMSServiceCtx cmsContext) throws CMSException {
        // Server invocation
        ServerInvocation invocation = cmsContext.getServerInvocation();

        List<CMSItem> workspaces;

        Object attribute = invocation.getAttribute(Scope.PRINCIPAL_SCOPE, USER_WORKSPACES_PRINCIPAL_ATTRIBUTE);
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
                // Query
                String query = this.getUserWorkspacesQuery(userName);
                // Schemas
                String schemas = this.getWorkspacesSchemas();
                // Portal policy filter
                String filter = InternalConstants.PORTAL_CMS_REQUEST_FILTERING_POLICY_NO_FILTER;

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
            }

            invocation.setAttribute(Scope.PRINCIPAL_SCOPE, USER_WORKSPACES_PRINCIPAL_ATTRIBUTE, workspaces);
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
     * Get workspaces query.
     *
     * @return query
     */
    protected String getWorkspacesQuery() {
        return "ecm:primaryType = 'Workspace' AND NOT ecm:path STARTSWITH '/default-domain/UserWorkspaces/'";
    }


    /**
     * Get user workspaces query.
     *
     * @param userName user name
     * @return query
     */
    protected String getUserWorkspacesQuery(String userName) {
        StringBuilder query = new StringBuilder();
        query.append("ecm:primaryType = 'Workspace' AND ecm:path STARTSWITH '/default-domain/UserWorkspaces/' AND dc:creator = '");
        query.append(userName);
        query.append("'");
        return query.toString();
    }


    /**
     * Get workspaces schemas.
     *
     * @return schemas
     */
    protected String getWorkspacesSchemas() {
        return "dublincore";
    }

}
