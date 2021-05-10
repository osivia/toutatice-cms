package fr.toutatice.portail.cms.nuxeo.repository;

import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.osivia.portal.api.cms.exception.DocumentNotFoundException;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilter;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilterContext;

public class GetChildrenCommand implements INuxeoCommand {

    private final String nuxeoId;
    
    
    public GetChildrenCommand(String nuxeoId) {
        super();
        this.nuxeoId = nuxeoId;
    }

    @Override
    public Object execute(Session nuxeoSession) throws Exception {

        // Clause
        StringBuilder clause = new StringBuilder();
        clause.append("ecm:parentId = '");
        clause.append(this.nuxeoId);
        clause.append("' ORDER BY ecm:pos ASC");

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

    @Override
    public String getId() {
        return this.getClass().getCanonicalName()+"/"+nuxeoId;
    }

}
