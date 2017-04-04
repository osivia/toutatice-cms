package fr.toutatice.portail.cms.nuxeo.portlets.service;

import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.osivia.portal.core.constants.InternalConstants;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilter;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilterContext;

/**
 * Get full loaded navigation Nuxeo command.
 * Useful to prevent full loading of the navigation menu.
 * 
 * @author Cédric Krommenhoek
 * @see INuxeoCommand
 */
public class FullLoadedNavigationCommand implements INuxeoCommand {

    /** Schemas. */
    private static final String SCHEMAS = "dublincore, common, toutatice";


    /** Base path. */
    private final String basePath;
    /** Live version indicator. */
    private final boolean liveVersion;


    /**
     * Constructor.
     * 
     * @param basePath base path
     * @param liveVersion live version indicator
     */
    public FullLoadedNavigationCommand(String basePath, boolean liveVersion) {
        super();
        this.basePath = basePath;
        this.liveVersion = liveVersion;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Session nuxeoSession) throws Exception {
        // Operation request
        OperationRequest request = nuxeoSession.newRequest("Document.QueryES");
        request.set(Constants.HEADER_NX_SCHEMAS, SCHEMAS);

        // Clause
        StringBuilder clause = new StringBuilder();
        clause.append("(ecm:path = '").append(this.basePath).append("' OR ecm:path STARTSWITH '").append(this.basePath).append("') ");
        clause.append("AND (ecm:mixinType = 'Folderish' OR ttc:showInMenu = 1) ");
        clause.append("ORDER BY ecm:pos");

        // Filter context
        int filterState;
        if (this.liveVersion) {
            filterState = NuxeoQueryFilterContext.STATE_LIVE;
        } else {
            filterState = NuxeoQueryFilterContext.STATE_DEFAULT;
        }
        NuxeoQueryFilterContext filterContext = new NuxeoQueryFilterContext(filterState, InternalConstants.PORTAL_CMS_REQUEST_FILTERING_POLICY_NO_FILTER);
        
        // Query
        String query = "SELECT * FROM Document WHERE " + NuxeoQueryFilter.addPublicationFilter(filterContext, clause.toString());
        request.set("query", query);

        return request.execute();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getName());
        builder.append("|");
        builder.append(this.basePath);
        builder.append("|");
        builder.append(this.liveVersion);
        return builder.toString();
    }

}
