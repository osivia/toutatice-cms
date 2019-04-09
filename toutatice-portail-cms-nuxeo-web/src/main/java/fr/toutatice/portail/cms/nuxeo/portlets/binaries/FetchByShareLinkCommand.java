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



    /**
     * Constructor.
     *
     * @param parentId parent Nuxeo document identifier
     * @param state Nuxeo query filter context state
     */
    public FetchByShareLinkCommand(String linkId) {
        super();
        this.linkId = linkId;
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
        clause.append("' AND rshr:enabledLink = 1 ORDER BY ecm:pos ASC");

        // Nuxeo query filter context
        NuxeoQueryFilterContext filterContext = new NuxeoQueryFilterContext(NuxeoQueryFilterContext.STATE_LIVE_N_PUBLISHED);

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
