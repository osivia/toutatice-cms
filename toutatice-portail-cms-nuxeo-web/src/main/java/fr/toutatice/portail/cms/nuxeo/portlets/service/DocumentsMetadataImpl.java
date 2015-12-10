package fr.toutatice.portail.cms.nuxeo.portlets.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.core.cms.DocumentsMetadata;
import org.osivia.portal.core.web.IWebUrlService;

/**
 * Documents metadata implementation.
 *
 * @author Cédric Krommenhoek
 * @see DocumentsMetadata
 */
public class DocumentsMetadataImpl implements DocumentsMetadata {

    /** CMS base path. */
    private final String basePath;
    /** Nuxeo documents. */
    private final List<Document> documents;

    /** Segments. */
    private final Map<String, String> segments;
    /** WebIds. */
    private final Map<String, String> webIds;
    /** Web paths. */
    private final Map<String, String> webPaths;


    /** Segments initialized indicator. */
    private boolean segmentsInitialized;
    /** WebIds initialized indicator. */
    private boolean webIdsInitialized;


    /**
     * Constructor.
     *
     * @param documents Nuxeo documents
     */
    public DocumentsMetadataImpl(String basePath, List<Document> documents) {
        super();
        this.basePath = basePath;
        this.documents = documents;
        this.segments = new ConcurrentHashMap<String, String>(this.documents.size());
        this.webIds = new ConcurrentHashMap<String, String>(this.documents.size());
        this.webPaths = new ConcurrentHashMap<String, String>(this.documents.size());
        this.segmentsInitialized = false;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getWebPath(String path) {
        if (!this.segmentsInitialized) {
            this.initializeSegments();
        }


        // Splitted path
        String[] splittedPath = StringUtils.split(path, "/");


        // Search closest web path
        String closestWebPath = null;
        int factor;
        for (factor = splittedPath.length; factor > 0; factor--) {
            // Working path
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < factor; i++) {
                builder.append("/");
                builder.append(splittedPath[i]);
            }
            String workingPath = builder.toString();

            // Check if base path includes working path
            if (!workingPath.startsWith(this.basePath) || workingPath.equals(this.basePath)) {
                break;
            }


            String webPath = this.webPaths.get(workingPath);
            if (webPath != null) {
                closestWebPath = webPath;
                break;
            }
        }


        // Working web path
        StringBuilder workingWebPath = new StringBuilder();

        if (closestWebPath != null) {
            workingWebPath.append(closestWebPath);
        }

        // Working path
        StringBuilder workingPath = new StringBuilder();
        for (int i = 0; i < factor; i++) {
            workingPath.append("/");
            workingPath.append(splittedPath[i]);
        }


        // Build web path
        boolean incompleteWebPath = false;
        for (int i = factor; i < splittedPath.length; i++) {
            workingPath.append("/");
            workingPath.append(splittedPath[i]);

            // Current path
            String currentPath = workingPath.toString();

            // Segment
            String segment = this.segments.get(currentPath);
            if (StringUtils.isNotBlank(segment)) {
                workingWebPath.append("/");
                workingWebPath.append(segment);

                // Current web path
                String currentWebPath = workingWebPath.toString();
                this.webPaths.put(currentPath, currentWebPath);
            } else {
                incompleteWebPath = true;
                break;
            }
        }


        // Web path
        String webPath;
        if (incompleteWebPath) {
            if (!this.webIdsInitialized) {
                this.initializeWebIds();
            }

            // WebId
            String webId = this.webIds.get(path);
            if (StringUtils.isNotBlank(webId)) {
                workingWebPath.append("/");
                workingWebPath.append(IWebUrlService.WEBID_PREFIX);
                workingWebPath.append(webId);
                webPath = workingWebPath.toString();
            } else {
                webPath = null;
            }
        } else {
            webPath = workingWebPath.toString();
        }


        return webPath;
    }


    /**
     * Initialize segments.
     */
    private synchronized void initializeSegments() {
        if (!this.segmentsInitialized) {
            for (Document document : this.documents) {
                String path = document.getPath();
                String segment = document.getString("ottcweb:segment");
                if (segment != null) {
                    this.segments.put(path, segment);
                }
            }

            this.segmentsInitialized = true;
        }
    }


    /**
     * Initialize webIds.
     */
    private synchronized void initializeWebIds() {
        if (!this.webIdsInitialized) {
            for (Document document : this.documents) {
                String path = document.getPath();
                String webId = document.getString("ttc:webid");
                if (webId != null) {
                    this.webIds.put(path, webId);
                }
            }

            this.webIdsInitialized = true;
        }
    }

}
