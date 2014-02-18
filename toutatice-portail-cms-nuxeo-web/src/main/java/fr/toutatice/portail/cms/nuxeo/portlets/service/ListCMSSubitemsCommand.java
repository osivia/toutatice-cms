package fr.toutatice.portail.cms.nuxeo.portlets.service;

import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.osivia.portal.core.constants.InternalConstants;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilter;

/**
 * List CMS sub-items command.
 * 
 * @author Cédric Krommenhoek
 * @see INuxeoCommand
 */
public class ListCMSSubitemsCommand implements INuxeoCommand {

    /** Current parent identifier. */
    private final String parentId;
    /** Live content indicator. */
    private final boolean liveContent;


    /**
     * Constructor.
     * 
     * @param parentId current parent identifier
     * @param liveContent live content indicator
     */
    public ListCMSSubitemsCommand(String parentId, boolean liveContent) {
        super();
        this.parentId = parentId;
        this.liveContent = liveContent;
    }


    /**
     * {@inheritDoc}
     */
    public Object execute(Session nuxeoSession) throws Exception {
        // Query
        StringBuilder query = new StringBuilder();
        query.append("SELECT * ");
        query.append("FROM Document ");
        query.append("WHERE ecm:parentId = '").append(this.parentId).append("' ");
        query.append("ORDER BY ecm:pos ");
        String filteredQuery = NuxeoQueryFilter.addPublicationFilter(query.toString(), this.liveContent,
                InternalConstants.PORTAL_CMS_REQUEST_FILTERING_POLICY_NO_FILTER);

        // Request
        OperationRequest request = nuxeoSession.newRequest("Document.Query");
        request.set("query", filteredQuery);
        request.setHeader(Constants.HEADER_NX_SCHEMAS, this.getSchemas());
        return request.execute();
    }


    /**
     * {@inheritDoc}
     */
    public String getId() {
        StringBuilder id = new StringBuilder();
        id.append(this.getClass().getCanonicalName());
        id.append("[");
        id.append(this.parentId);
        id.append(";");
        id.append(this.liveContent);
        id.append("]");
        return id.toString();
    }


    /**
     * Utility method used to return schemas.
     * 
     * @return schemas
     */
    private String getSchemas() {
        return "dublincore, common, toutatice";
    }

}
