package fr.toutatice.portail.cms.nuxeo.portlets.statistics;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilter;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilterContext;

/**
 * Get space statistics Nuxeo command.
 * 
 * @author Cédric Krommenhoek
 * @see INuxeoCommand
 */
public class GetSpaceStatisticsCommand implements INuxeoCommand {

    /** Space paths. */
    private final Set<String> paths;


    /**
     * Constructor.
     */
    public GetSpaceStatisticsCommand(Set<String> paths) {
        super();
        this.paths = paths;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Session nuxeoSession) throws Exception {
        // Clause
        StringBuilder clause = new StringBuilder();
        clause.append("ecm:mixinType = 'Statistics' ");
        clause.append("AND ecm:path IN (");
        boolean first = true;
        for (String path : this.paths) {
            if (first) {
                first = false;
            } else {
                clause.append(", ");
            }

            clause.append("'");
            clause.append(path);
            clause.append("'");
        }
        clause.append(")");

        // Filtered clause
        String filteredClause = NuxeoQueryFilter.addPublicationFilter(NuxeoQueryFilterContext.CONTEXT_LIVE, clause.toString());

        // Operation request
        OperationRequest request = nuxeoSession.newRequest("Document.QueryES");
        request.set(Constants.HEADER_NX_SCHEMAS, "statistics");
        request.set("query", "SELECT * FROM Document WHERE " + filteredClause);

        return request.execute();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getSimpleName());
        builder.append("|");
        builder.append(StringUtils.join(this.paths, ","));
        return builder.toString();
    }

}
