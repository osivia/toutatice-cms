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

public class FetchByWebIdCommand implements INuxeoCommand {

    private final String id;
    
    
    public FetchByWebIdCommand(String id) {
        super();
        this.id = id;
    }

    @Override
    public Object execute(Session session) throws Exception {

           
        // Clause
        StringBuilder clause = new StringBuilder();
        clause.append("ttc:webid = '");
        clause.append(this.id);
        clause.append("'");

        // Nuxeo query filter context
        NuxeoQueryFilterContext filterContext = new NuxeoQueryFilterContext(NuxeoQueryFilterContext.STATE_DEFAULT);

        // Filtered clause
        String filteredClause = NuxeoQueryFilter.addPublicationFilter(filterContext, clause.toString());

        // Operation request
        OperationRequest request = session.newRequest("Document.QueryES");
        request.set(Constants.HEADER_NX_SCHEMAS, "*");
        request.set("query", "SELECT * FROM Document WHERE " + filteredClause);

        Documents results = (Documents) request.execute();


        if (results.size() != 1)
            throw new DocumentNotFoundException();

        Document nxDocument = results.get(0);
        
        return nxDocument;
    }

    @Override
    public String getId() {
        return FetchByWebIdCommand.class.getCanonicalName()+"/"+id;
    }

}
