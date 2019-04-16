package fr.toutatice.portail.cms.nuxeo.portlets.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilter;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilterContext;

public class GetAncestorsCommand implements INuxeoCommand {

    /** Document path. */
    private final String path;


    /**
     * Constructor.
     * 
     * @param path document path
     */
    public GetAncestorsCommand(String path) {
        super();
        this.path = path;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Session nuxeoSession) throws Exception {
        int count = StringUtils.countMatches(this.path, "/");

        // Ancestor paths
        List<String> ancestorPaths;
        if (count > 1) {
            ancestorPaths = new ArrayList<>(count - 1);

            String truncatedPath = this.path;
            while (StringUtils.countMatches(truncatedPath, "/") > 1) {
                ancestorPaths.add(truncatedPath);
                truncatedPath = StringUtils.substringBeforeLast(truncatedPath, "/");
            }
        } else {
            ancestorPaths = null;
        }

        // Operation result
        Object result;

        if (CollectionUtils.isEmpty(ancestorPaths)) {
            result = null;
        } else {
            // NXQL request
            StringBuilder nxqlRequest = new StringBuilder();
            nxqlRequest.append("ecm:path IN ('").append(StringUtils.join(ancestorPaths, "', '")).append("') ");

            // Filtered request
            String filteredRequest = NuxeoQueryFilter.addPublicationFilter(NuxeoQueryFilterContext.CONTEXT_LIVE_N_PUBLISHED, nxqlRequest.toString());

            // Operation request
            OperationRequest request = nuxeoSession.newRequest("Document.QueryES");
            request.set(Constants.HEADER_NX_SCHEMAS, "*");
            request.set("query", filteredRequest);

            result = request.execute();
        }

        return result;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getSimpleName());
        builder.append("|");
        builder.append(this.path);
        return builder.toString();
    }

}
