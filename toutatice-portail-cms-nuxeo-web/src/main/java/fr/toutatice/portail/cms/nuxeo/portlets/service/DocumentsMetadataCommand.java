package fr.toutatice.portail.cms.nuxeo.portlets.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.osivia.portal.core.cms.DocumentsMetadata;
import org.osivia.portal.core.constants.InternalConstants;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoCompatibility;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilter;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilterContext;
import fr.toutatice.portail.cms.nuxeo.api.domain.Symlink;
import fr.toutatice.portail.cms.nuxeo.portlets.publish.RequestPublishStatus;

/**
 * Documents metadata command.
 *
 * @author Cédric Krommenhoek
 * @see INuxeoCommand
 */
public class DocumentsMetadataCommand implements INuxeoCommand {

    /** Schemas. */
    private static final String SCHEMAS = "dublincore, toutatice, ottc_web, symlink";


    /** CMS base path. */
    private final String basePath;
    /** Version status. */
    private final String version;
    /** Symlinks. */
    private final List<Symlink> symlinks;
    /** Timestamp, may be null for full refresh. */
    private final Long timestamp;

    /** Elastic-Search indicator. */
    private final boolean elasticSearch;


    /**
     * Constructor.
     *
     * @param basePath CMS base path
     * @param version version
     * @param timestamp timestamp, may be null for full refresh
     */
    public DocumentsMetadataCommand(String basePath, RequestPublishStatus version, List<Symlink> symlinks, Long timestamp) {
        super();
        this.basePath = basePath;
        this.version = version.getStatus();
        if (symlinks == null) {
            this.symlinks = new ArrayList<Symlink>(0);
        } else {
            this.symlinks = symlinks;
        }
        this.timestamp = timestamp;

        // Elastic-Search indicator
        this.elasticSearch = NuxeoCompatibility.canUseES();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public DocumentsMetadata execute(Session nuxeoSession) throws Exception {
        // Documents
        List<Document> documents = this.getDocuments(nuxeoSession, this.symlinks).list();

        // Root
        if (this.timestamp == null) {
            Document rootDocument = this.getRootDocument(nuxeoSession);
            documents.add(rootDocument);
        }

        return new DocumentsMetadataImpl(this.basePath, documents, this.symlinks);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getSimpleName());
        builder.append("/");
        builder.append(this.basePath);
        builder.append("/");
        builder.append(this.version);
        builder.append("/");
        builder.append(this.timestamp);
        return builder.toString();
    }


    // private List<Symlink> getSymlinks(Session nuxeoSession) throws Exception {
    // List<Symlink> symlinks;
    //
    // Documents sources = this.getSymlinkSources(nuxeoSession);
    //
    // if (sources.isEmpty()) {
    // symlinks = new ArrayList<Symlink>(0);
    // } else {
    // Map<String, Document> lookup = new ConcurrentHashMap<String, Document>(sources.size());
    // for (Document source : sources) {
    // String segment = source.getString(DocumentsMetadataImpl.WEB_URL_SEGMENT_PROPERTY);
    // String target = source.getString(DocumentsMetadataImpl.TARGET_PROPERTY);
    // if (StringUtils.isNotBlank(segment) && StringUtils.isNotBlank(target)) {
    // lookup.put(target, source);
    // }
    // }
    //
    // Documents targets = getSymlinkTargets(nuxeoSession, lookup.keySet());
    //
    // symlinks = new ArrayList<Symlink>(targets.size());
    //
    // for (Document target : targets) {
    // String targetPath = StringUtils.removeEnd(target.getPath(), ".proxy");
    // String targetWebId = target.getString(DocumentsMetadataImpl.WEB_ID_PROPERTY);
    // Document source = lookup.get(targetWebId);
    // String path = StringUtils.removeEnd(source.getPath(), ".proxy");
    // String segment = source.getString(DocumentsMetadataImpl.WEB_URL_SEGMENT_PROPERTY);
    //
    // Symlink symlink = new Symlink(path, segment, targetPath, targetWebId);
    // symlinks.add(symlink);
    // }
    // }
    //
    // return symlinks;
    // }
    //
    //
    // private Documents getSymlinkSources(Session nuxeoSession) throws Exception {
    // // Request
    // StringBuilder sourceRequest = new StringBuilder();
    // sourceRequest.append("ecm:path STARTSWITH '");
    // sourceRequest.append(this.basePath);
    // sourceRequest.append("/' AND ecm:primaryType = '");
    // sourceRequest.append(DocumentsMetadataImpl.SYMLINK_TYPE);
    // sourceRequest.append("'");
    //
    // if (this.timestamp != null) {
    // // Timestamp date
    // Date date = new Date(this.timestamp);
    // // Date format
    // DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    //
    // sourceRequest.append(" AND dc:modified > TIMESTAMP '");
    // sourceRequest.append(dateFormat.format(date));
    // sourceRequest.append("'");
    // }
    //
    // // Filtered request
    // int state = NuxeoQueryFilter.getState(this.version);
    // NuxeoQueryFilterContext sourceFilterContext = new NuxeoQueryFilterContext(state, InternalConstants.PORTAL_CMS_REQUEST_FILTERING_POLICY_NO_FILTER);
    // String filteredRequest = NuxeoQueryFilter.addPublicationFilter(sourceFilterContext, sourceRequest.toString());
    //
    // // NXQL Query
    // String query = "SELECT * FROM Document WHERE " + filteredRequest;
    //
    // return this.executeRequest(nuxeoSession, query);
    // }
    //
    //
    // private Documents getSymlinkTargets(Session nuxeoSession, Set<String> webIds) throws Exception {
    // // Request
    // StringBuilder request = new StringBuilder();
    // request.append("NOT ecm:path STARTSWITH '");
    // request.append(this.basePath);
    // request.append("/' AND ");
    // request.append(DocumentsMetadataImpl.WEB_ID_PROPERTY);
    // request.append(" IN (");
    // boolean first = true;
    // for (String webId : webIds) {
    // if (first) {
    // first = false;
    // } else {
    // request.append(", ");
    // }
    //
    // request.append("'");
    // request.append(webId);
    // request.append("'");
    // }
    // request.append(")");
    //
    // // Filtered request
    // NuxeoQueryFilterContext filterContext = new NuxeoQueryFilterContext(NuxeoQueryFilterContext.STATE_LIVE_N_PUBLISHED,
    // InternalConstants.PORTAL_CMS_REQUEST_FILTERING_POLICY_NO_FILTER);
    // String filteredRequest = NuxeoQueryFilter.addPublicationFilter(filterContext, request.toString());
    //
    // // NXQL Query
    // String query = "SELECT * FROM Document WHERE " + filteredRequest;
    //
    // return this.executeRequest(nuxeoSession, query);
    // }


    /**
     * Get documents.
     *
     * @param nuxeoSession Nuxeo session
     * @param symlinks symlinks
     * @return documents
     * @throws Exception
     */
    private Documents getDocuments(Session nuxeoSession, List<Symlink> symlinks) throws Exception {
        // Documents request
        StringBuilder documentsRequest = new StringBuilder();
        documentsRequest.append("ecm:path STARTSWITH '");
        documentsRequest.append(this.basePath);
        documentsRequest.append("/'");

        if (this.timestamp != null) {
            // Timestamp date
            Date date = new Date(this.timestamp);
            // Date format
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

            documentsRequest.append(" AND dc:modified > TIMESTAMP '");
            documentsRequest.append(dateFormat.format(date));
            documentsRequest.append("'");
        }

        // Filtered request
        int state = NuxeoQueryFilter.getState(this.version);
        NuxeoQueryFilterContext documentsFilterContext = new NuxeoQueryFilterContext(state, InternalConstants.PORTAL_CMS_REQUEST_FILTERING_POLICY_NO_FILTER);
        String documentsFilteredRequest = NuxeoQueryFilter.addPublicationFilter(documentsFilterContext, documentsRequest.toString());


        // Symlink targets request
        String targetsFilteredRequest;
        if (symlinks.isEmpty()) {
            targetsFilteredRequest = null;
        } else {
            StringBuilder targetsRequest = new StringBuilder();
            boolean first = true;
            for (Symlink symlink : symlinks) {
                if (first) {
                    first = false;
                } else {
                    targetsRequest.append(" OR ");
                }

                targetsRequest.append("ecm:path = '");
                targetsRequest.append(symlink.getTargetPath());
                targetsRequest.append("' OR ecm:path STARTSWITH '");
                targetsRequest.append(symlink.getTargetPath());
                targetsRequest.append("/'");
            }


            // Filtered request
            NuxeoQueryFilterContext targetsFilterContext = new NuxeoQueryFilterContext(NuxeoQueryFilterContext.STATE_LIVE,
                    InternalConstants.PORTAL_CMS_REQUEST_FILTERING_POLICY_NO_FILTER);
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
     * Execute NXQL request.
     *
     * @param nuxeoSession Nuxeo session
     * @param query NXQL query
     * @return documents
     * @throws Exception
     */
    private Documents executeRequest(Session nuxeoSession, String query) throws Exception {
        OperationRequest operationRequest;
        if (this.elasticSearch) {
            operationRequest = nuxeoSession.newRequest("Document.QueryES");
            operationRequest.set(Constants.HEADER_NX_SCHEMAS, SCHEMAS);
            operationRequest.set("pageSize", -1);
            operationRequest.set("currentPageIndex", 0);
        } else {
            operationRequest = nuxeoSession.newRequest("Document.Query");
            operationRequest.setHeader(Constants.HEADER_NX_SCHEMAS, SCHEMAS);
        }
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
