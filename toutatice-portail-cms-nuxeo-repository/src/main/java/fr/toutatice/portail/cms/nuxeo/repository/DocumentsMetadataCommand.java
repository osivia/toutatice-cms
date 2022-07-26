package fr.toutatice.portail.cms.nuxeo.repository;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.osivia.portal.api.cms.Symlink;
import org.osivia.portal.api.cms.Symlinks;
import org.osivia.portal.core.cms.DocumentsMetadata;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilter;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilterContext;


/**
 * Documents metadata command.
 *
 * @author Cédric Krommenhoek
 * @see INuxeoCommand
 */
public class DocumentsMetadataCommand implements INuxeoCommand {

    /** Schemas. */
    private static final String SCHEMAS = "dublincore, toutatice, ottc_web";


    /** CMS base path. */
    private final String basePath;
    /** Version status. */
    private final String version;
    /** Symlinks. */
    private final Symlinks symlinks;
    /** Timestamp, may be null for full refresh. */
    private final Long timestamp;


    /**
     * Constructor.
     *
     * @param basePath CMS base path
     * @param version version
     * @param symlinks symlinks
     * @param timestamp timestamp, may be null for full refresh
     */
    public DocumentsMetadataCommand(String basePath, RequestPublishStatus version, Symlinks symlinks, Long timestamp) {
        super();
        this.basePath = basePath;
        this.version = version.getStatus();
        this.symlinks = symlinks;
        this.timestamp = timestamp;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public DocumentsMetadata execute(Session nuxeoSession) throws Exception {
        // Documents
        List<Document> documents = this.getDocuments(nuxeoSession).list();

        // Root
        if (this.timestamp == null) {
            Document rootDocument = this.getRootDocument(nuxeoSession);
            documents.add(rootDocument);
        }

        // Symlinks
        List<Symlink> symlinks;
        if (this.symlinks == null) {
            symlinks = null;
        } else {
            symlinks = this.symlinks.getLinks();
        }

        return new DocumentsMetadataImpl(this.basePath, documents, symlinks);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        StringBuilder builder = new StringBuilder();
        builder.append("SpaceDocuments");
        builder.append("/");
        builder.append(this.basePath);
        builder.append("/");
        builder.append(this.version);
        builder.append("/");
        builder.append(this.timestamp);
        return builder.toString();
    }


    /**
     * Get documents.
     *
     * @param nuxeoSession Nuxeo session
     * @return documents
     * @throws Exception
     */
    private Documents getDocuments(Session nuxeoSession) throws Exception {
        // Documents request
        StringBuilder documentsRequest = new StringBuilder();
        documentsRequest.append("ecm:path STARTSWITH '");
        documentsRequest.append(this.basePath);
        documentsRequest.append("'");
        this.addTimestampClause(documentsRequest);

        // Filtered request
        int state = NuxeoQueryFilter.getState(this.version);
        NuxeoQueryFilterContext documentsFilterContext = new NuxeoQueryFilterContext(state);
        String documentsFilteredRequest = NuxeoQueryFilter.addPublicationFilter(documentsFilterContext, documentsRequest.toString());


        // Symlink targets request
        String targetsFilteredRequest;
        if (this.symlinks == null) {
            targetsFilteredRequest = null;
        } else if (CollectionUtils.isEmpty(this.symlinks.getPaths())) {
            targetsFilteredRequest = null;
        } else {
            StringBuilder targetsRequest = new StringBuilder();
            targetsRequest.append("(");
            boolean first = true;
            for (String path : this.symlinks.getPaths()) {
                if (first) {
                    first = false;
                } else {
                    targetsRequest.append(" OR ");
                }

                targetsRequest.append("ecm:path STARTSWITH '");
                targetsRequest.append(path);
                targetsRequest.append("'");
            }
            targetsRequest.append(")");
            this.addTimestampClause(targetsRequest);


            // Filtered request
            NuxeoQueryFilterContext targetsFilterContext = new NuxeoQueryFilterContext(NuxeoQueryFilterContext.STATE_LIVE);
            targetsFilteredRequest = NuxeoQueryFilter.addPublicationFilter(targetsFilterContext, targetsRequest.toString());
        }


        // NXQL Query
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM Document WHERE (");
        query.append(documentsFilteredRequest);
        query.append(")");
        if (targetsFilteredRequest != null) {
            query.append(" OR (");
            query.append(targetsFilteredRequest);
            query.append(")");
        }

        return this.executeRequest(nuxeoSession, query.toString());
    }


    /**
     * Add timestamp clause to NXQL request.
     *
     * @param request NXQL request
     */
    private void addTimestampClause(StringBuilder request) {
        if (this.timestamp != null) {
            // Timestamp date
            Date date = new Date(this.timestamp);
            // Date format
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

            request.append(" AND dc:modified > TIMESTAMP '");
            request.append(dateFormat.format(date));
            request.append("'");
        }
    }


    /**
     * Execute NXQL request.
     *
     * @param nuxeoSession Nuxeo session
     * @param query NXQL query
     * @return documents
     * @throws Exception
     */
    private Documents executeRequest(Session nuxeoSession, String query) throws Exception {
        OperationRequest operationRequest = nuxeoSession.newRequest("Document.Query");
        operationRequest.setHeader(Constants.HEADER_NX_SCHEMAS, SCHEMAS);
        operationRequest.set("query", query.toString());

        return (Documents) operationRequest.execute();
    }


    /**
     * Get root document.
     *
     * @param nuxeoSession Nuxeo session
     * @return document
     * @throws Exception
     */
    private Document getRootDocument(Session nuxeoSession) throws Exception {
        OperationRequest request = nuxeoSession.newRequest("Document.Fetch");
        request.setHeader(Constants.HEADER_NX_SCHEMAS, SCHEMAS);
        request.set("value", this.basePath);
        return (Document) request.execute();
    }

}
