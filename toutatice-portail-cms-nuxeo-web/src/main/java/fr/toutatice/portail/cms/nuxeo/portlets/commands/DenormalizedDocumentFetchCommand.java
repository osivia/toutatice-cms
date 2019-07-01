package fr.toutatice.portail.cms.nuxeo.portlets.commands;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.osivia.portal.core.cms.CMSException;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilter;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilterContext;

/**
 * Fetch denormalized document Nuxeo command.
 * 
 * @author Cédric Krommenhoek
 * @see INuxeoCommand
 * @since 4.7
 */
public class DenormalizedDocumentFetchCommand implements INuxeoCommand {

    /** Operation request identifier. */
    private static final String OPERATION_ID = "Document.QueryES";


    /** Document path. */
    private final String path;
    /** Document state. */
    private final int state;


    /**
     * Constructor.
     * 
     * @param path document path
     * @param state document state
     */
    public DenormalizedDocumentFetchCommand(String path, int state) {
        super();
        this.path = path;
        this.state = state;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Document execute(Session nuxeoSession) throws Exception {
        // NXQL clause
        StringBuilder clause = new StringBuilder();
        clause.append("ecm:path = '").append(this.path).append("' ");

        // NXQL filtered request
        NuxeoQueryFilterContext queryFilterContext = new NuxeoQueryFilterContext(this.state);
        String filteredRequest = NuxeoQueryFilter.addPublicationFilter(queryFilterContext, clause.toString());

        // Operation request
        OperationRequest operationRequest = nuxeoSession.newRequest(OPERATION_ID);
        operationRequest.setHeader(Constants.HEADER_NX_SCHEMAS, "*");
        operationRequest.set("query", "SELECT * FROM Document WHERE " + filteredRequest);

        // Execution
        Documents documents = (Documents) operationRequest.execute();

        // Result
        Document document;
        if ((documents == null) || documents.isEmpty()) {
            throw new CMSException(CMSException.ERROR_NOTFOUND);
        } else if (documents.size() == 1) {
            document = documents.get(0);
        } else {
            throw new CMSException("Too much results.");
        }

        return document;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getName());
        builder.append("|");
        builder.append(StringUtils.trimToEmpty(this.path));
        builder.append("|");
        builder.append(this.state);
        return builder.toString();
    }

}
