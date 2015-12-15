package fr.toutatice.portail.cms.nuxeo.portlets.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.osivia.portal.core.cms.DocumentsMetadata;

import fr.toutatice.portail.cms.nuxeo.portlets.list.ListCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.publish.RequestPublishStatus;

/**
 * Documents metadata command.
 *
 * @author Cédric Krommenhoek
 * @see ListCommand
 */
public class DocumentsMetadataCommand extends ListCommand {

    /** Schemas. */
    private static final String SCHEMAS = "dublincore, toutatice, ottc_web";


    /** CMS base path. */
    private final String basePath;
    /** Timestamp, may be null for full refresh. */
    private final Long timestamp;


    /**
     * Constructor.
     *
     * @param basePath CMS base path
     * @param timestamp timestamp, may be null for full refresh
     * @param version version
     */
    public DocumentsMetadataCommand(String basePath, RequestPublishStatus version, Long timestamp) {
        super(generateRequest(basePath, timestamp), version.getStatus(), 0, -1, SCHEMAS, null, true);
        this.basePath = basePath;
        this.timestamp = timestamp;
    }


    /**
     * Generate Nuxeo request.
     *
     * @param basePath CMS base path
     * @param timestamp timestamp, may be null
     * @return Nuxeo request
     */
    private static String generateRequest(String basePath, Long timestamp) {
        StringBuilder request = new StringBuilder();
        request.append("(ecm:path STARTSWITH '");
        request.append(basePath);
        request.append("'");
        if (timestamp != null) {
            // Timestamp date
            Date date = new Date(timestamp);
            // Date format
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

            request.append(" AND dc:modified > TIMESTAMP '");
            request.append(dateFormat.format(date));
            request.append("'");
        }
        request.append(")");
        return request.toString();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public DocumentsMetadata execute(Session nuxeoSession) throws Exception {
        // Documents
        Documents documents = (Documents) super.execute(nuxeoSession);

        // Root
        if (this.timestamp == null) {
            Document rootDocument = this.getRootDocument(nuxeoSession);
            documents.add(rootDocument);
        }

        return new DocumentsMetadataImpl(this.basePath, documents.list());
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
