package fr.toutatice.portail.cms.nuxeo.portlets.binaries;

import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilter;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilterContext;

/**
 * Fetch by share link Nuxeo command.
 *
 * @author Jean-Sébastien Steux
 * @see INuxeoCommand
 */
public class FetchByShareLinkCommand implements INuxeoCommand {

    /** link ID  */
    private final String linkId;
    
    /** The enabled link only. */
    private boolean enabledLinkOnly;



    /**
     * Constructor.
     *
     * @param parentId parent Nuxeo document identifier
     * @param state Nuxeo query filter context state
     */
    public FetchByShareLinkCommand(String linkId, boolean enabledLinkOnly) {
        super();
        this.linkId = linkId;
        this.enabledLinkOnly = enabledLinkOnly;
   }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Session nuxeoSession) throws Exception {
        // Clause
        StringBuilder clause = new StringBuilder();
        clause.append("rshr:linkId = '");
        clause.append(this.linkId);
        clause.append("' ");        

        if( enabledLinkOnly)
            clause.append(" AND rshr:enabledLink = 1");
            
        clause.append(" ORDER BY dc:modified ASC");

        // Nuxeo query filter context
        NuxeoQueryFilterContext filterContext = new NuxeoQueryFilterContext(NuxeoQueryFilterContext.STATE_LIVE);

        // Filtered clause
        String filteredClause = NuxeoQueryFilter.addPublicationFilter(filterContext, clause.toString());

        // Operation request
        OperationRequest request = nuxeoSession.newRequest("Document.QueryES");
        request.set(Constants.HEADER_NX_SCHEMAS, "*");
        request.set("query", "SELECT * FROM Document WHERE " + filteredClause);

        return request.execute();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getName());
        builder.append("/");
        builder.append(this.linkId);
        return builder.toString();
    }

}
