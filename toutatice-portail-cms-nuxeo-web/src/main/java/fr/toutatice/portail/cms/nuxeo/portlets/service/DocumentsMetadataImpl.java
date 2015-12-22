package fr.toutatice.portail.cms.nuxeo.portlets.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

    /** WebId Nuxeo document property name. */
    public static final String WEB_ID_PROPERTY = "ttc:webid";
    /** Web URL segment Nuxeo document property name. */
    public static final String WEB_URL_SEGMENT_PROPERTY = "ottcweb:segment";
    /** Modified Nuxeo document property name. */
    public static final String MODIFIED_PROPERTY = "dc:modified";


    /** WebId path prefix. */
    private static final String WEB_ID_PATH_PREFIX = "/" + IWebUrlService.WEB_ID_PREFIX;


    /** CMS base path. */
    private final String basePath;
    /** Nuxeo documents. */
    private final List<Document> documents;

    /** WebId to path association map. */
    private final Map<String, String> webIds;
    /** Path to webId and segment association map. */
    private final Map<String, PathValues> paths;
    /** Parent path and segment to child path association map. */
    private final Map<SegmentKey, String> segments;

    /** WebId to web path association map cache. */
    private final Map<String, String> toWebPaths;
    /** Web path to webId association map cache. */
    private final Map<String, String> fromWebPaths;


    /** Timestamp. */
    private long timestamp;


    /**
     * Constructor.
     *
     * @param documents Nuxeo documents
     */
    public DocumentsMetadataImpl(String basePath, List<Document> documents) {
        super();
        this.basePath = basePath;
        this.documents = documents;
        this.webIds = new ConcurrentHashMap<String, String>(this.documents.size());
        this.paths = new ConcurrentHashMap<String, PathValues>(this.documents.size());
        this.segments = new ConcurrentHashMap<SegmentKey, String>(this.documents.size());
        this.toWebPaths = new ConcurrentHashMap<String, String>(this.documents.size());
        this.fromWebPaths = new ConcurrentHashMap<String, String>(this.documents.size());

        // Maps & timestamp initialization
        this.timestamp = 0;
        for (Document document : documents) {
            String path = StringUtils.removeEnd(document.getPath(), ".proxy");
            String webId = document.getString(WEB_ID_PROPERTY);
            String segment = document.getString(WEB_URL_SEGMENT_PROPERTY);
            Date modified = document.getDate(MODIFIED_PROPERTY);

            if (webId != null) {
                this.webIds.put(webId, path);
            }

            if ((webId != null) || (segment != null)) {
                this.paths.put(path, new PathValues(webId, segment));
            }

            if (!path.equals(basePath) && (segment != null)) {
                this.segments.put(new SegmentKey(StringUtils.substringBeforeLast(path, "/"), segment), path);
            }

            this.timestamp = Math.max(this.timestamp, modified.getTime());
        }

        // Increase time to exclude last modified document from the next request (Nuxeo documents timestamp are truncated to 10ms)
        this.timestamp += 10;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getWebPath(String webId) {
        if (StringUtils.isBlank(webId)) {
            return null;
        }

        // Path
        String path = this.webIds.get(webId);
        if (path == null) {
            return null;
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

            PathValues workingPathValues = this.paths.get(workingPath);
            if ((workingPathValues != null) && (workingPathValues.webId != null)) {
                String webPath = this.toWebPaths.get(workingPathValues.webId);
                if ((webPath != null) && !webPath.startsWith(WEB_ID_PATH_PREFIX)) {
                    closestWebPath = webPath;
                    break;
                }
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
            PathValues currentPathValues = this.paths.get(currentPath);

            // Segment
            if ((currentPathValues != null) && StringUtils.isNotBlank(currentPathValues.segment)) {
                workingWebPath.append("/");
                workingWebPath.append(currentPathValues.segment);

                // Current web path
                String currentWebPath = workingWebPath.toString();
                if (currentPathValues.webId != null) {
                    this.toWebPaths.put(currentPathValues.webId, currentWebPath);
                }
            } else {
                incompleteWebPath = true;
                break;
            }
        }


        // Web path
        String webPath;
        if (incompleteWebPath) {
            workingWebPath.append(WEB_ID_PATH_PREFIX);
            workingWebPath.append(webId);
            webPath = workingWebPath.toString();
        } else {
            webPath = workingWebPath.toString();
        }
        this.toWebPaths.put(webId, webPath);

        return webPath;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getWebId(String webPath) {
        if (StringUtils.EMPTY.equals(webPath)) {
            PathValues pathValues = this.paths.get(this.basePath);
            String webId = null;
            if (pathValues != null) {
                webId = pathValues.webId;
            }
            return webId;
        } else if (StringUtils.isBlank(webPath)) {
            return null;
        }


        // Splitted web path
        String[] splittedWebPath = StringUtils.split(webPath, "/");

        // Search closest webId
        String closestWebId = null;
        int factor;
        for (factor = splittedWebPath.length; factor > 1; factor--) {
            // Working web path
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < factor; i++) {
                builder.append("/");
                builder.append(splittedWebPath[i]);
            }
            String workingWebPath = builder.toString();

            closestWebId = this.fromWebPaths.get(workingWebPath);
            if (closestWebId != null) {
                break;
            }
        }

        // Closest path
        String closestPath = null;
        if (closestWebId != null) {
            closestPath = this.webIds.get(closestWebId);
        }
        if (closestPath == null) {
            factor = 0;
        }


        // Working web path
        StringBuilder workingWebPath = new StringBuilder();
        for (int i = 0; i < factor; i++) {
            workingWebPath.append("/");
            workingWebPath.append(splittedWebPath[i]);
        }


        // Path
        String path;
        if (closestPath != null) {
            path = closestPath;
        } else {
            path = this.basePath;
        }


        // WebId
        String webId = closestWebId;
        for (int i = factor; i < splittedWebPath.length; i++) {
            // Segment
            String segment = splittedWebPath[i];

            workingWebPath.append("/");
            workingWebPath.append(segment);

            if (segment.startsWith(IWebUrlService.WEB_ID_PREFIX)) {
                webId = StringUtils.removeStart(segment, IWebUrlService.WEB_ID_PREFIX);
                if (this.webIds.containsKey(webId)) {
                    this.fromWebPaths.put(workingWebPath.toString(), webId);
                } else {
                    webId = null;
                }
                break;
            } else {
                // Key
                SegmentKey key = new SegmentKey(path, segment);
                path = this.segments.get(key);

                if (path == null) {
                    webId = null;
                    break;
                } else {
                    PathValues pathValues = this.paths.get(path);
                    if ((pathValues != null) && (pathValues.webId != null)) {
                        webId = pathValues.webId;
                        this.fromWebPaths.put(workingWebPath.toString(), webId);
                    }
                }
            }
        }

        return webId;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public long getTimestamp() {
        return this.timestamp;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void update(DocumentsMetadata updates) {
        if (updates instanceof DocumentsMetadataImpl) {
            DocumentsMetadataImpl metadata = (DocumentsMetadataImpl) updates;
            for (Document document : metadata.documents) {
                // WebId
                String webId = document.getString(WEB_ID_PROPERTY);
                // Path
                String path = StringUtils.removeEnd(document.getPath(), ".proxy");
                // Segment
                String segment = document.getString(WEB_URL_SEGMENT_PROPERTY);

                if (StringUtils.isNotBlank(webId)) {
                    // Original path
                    String originalPath = this.webIds.get(webId);
                    // Original path values
                    PathValues originalPathValues;
                    if (originalPath != null) {
                        originalPathValues = this.paths.get(originalPath);
                    } else {
                        originalPathValues = null;
                    }
                    // Original segment
                    String originalSegment;
                    if (originalPathValues != null) {
                        originalSegment = originalPathValues.segment;
                    } else {
                        originalSegment = null;
                    }


                    if (!StringUtils.equals(path, originalPath) || !StringUtils.equals(segment, originalSegment)) {
                        // Update webIds
                        if (originalPath != null) {
                            for (Entry<String, String> entry : this.webIds.entrySet()) {
                                String currentPath = entry.getValue();
                                if (currentPath.startsWith(originalPath)) {
                                    this.webIds.put(entry.getKey(), path + StringUtils.substringAfter(currentPath, originalPath));
                                }
                            }
                        }
                        this.webIds.put(webId, path);

                        // Update paths
                        if (originalPath != null) {
                            this.paths.remove(originalPath);
                            for (Entry<String, PathValues> entry : this.paths.entrySet()) {
                                String currentPath = entry.getKey();
                                if (currentPath.startsWith(originalPath)) {
                                    this.paths.put(path + StringUtils.substringAfter(currentPath, originalPath), entry.getValue());
                                    this.paths.remove(currentPath);
                                }
                            }
                        }
                        this.paths.put(path, new PathValues(webId, segment));

                        // Update segments
                        if ((originalPath != null) && (originalSegment != null)) {
                            this.segments.remove(new SegmentKey(StringUtils.substringBeforeLast(originalPath, "/"), originalSegment));
                            for (Entry<SegmentKey, String> entry : this.segments.entrySet()) {
                                SegmentKey key = entry.getKey();
                                if (key.parentPath.startsWith(originalPath)) {
                                    this.segments.put(new SegmentKey(path, key.segment), path + StringUtils.substringAfter(entry.getValue(), originalPath));
                                }
                            }
                        }
                        if (!path.equals(this.basePath) && (segment != null)) {
                            this.segments.put(new SegmentKey(StringUtils.substringBeforeLast(path, "/"), segment), path);
                        }

                        // Remove old web path
                        this.toWebPaths.remove(webId);
                    }
                }
            }

            // Update timestamp
            this.timestamp = Math.max(this.timestamp, metadata.timestamp);
        }
    }


    /**
     * Path values inner-class.
     *
     * @author Cédric Krommenhoek
     */
    private class PathValues {

        /** WebId. */
        private final String webId;
        /** Segment. */
        private final String segment;


        /**
         * Constructor.
         *
         * @param webId webId
         * @param segment segment
         */
        public PathValues(String webId, String segment) {
            super();
            this.webId = webId;
            this.segment = segment;
        }

    }


    /**
     * Segment key inner-class.
     *
     * @author Cédric Krommenhoek
     */
    private class SegmentKey {

        /** Parent path. */
        private final String parentPath;
        /** Segment. */
        private final String segment;


        /**
         * Constructor.
         *
         * @param parentPath parent path
         * @param segment segment
         */
        public SegmentKey(String parentPath, String segment) {
            super();
            this.parentPath = parentPath;
            this.segment = segment;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + this.getOuterType().hashCode();
            result = (prime * result) + ((this.parentPath == null) ? 0 : this.parentPath.hashCode());
            result = (prime * result) + ((this.segment == null) ? 0 : this.segment.hashCode());
            return result;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            SegmentKey other = (SegmentKey) obj;
            if (!this.getOuterType().equals(other.getOuterType())) {
                return false;
            }
            if (this.parentPath == null) {
                if (other.parentPath != null) {
                    return false;
                }
            } else if (!this.parentPath.equals(other.parentPath)) {
                return false;
            }
            if (this.segment == null) {
                if (other.segment != null) {
                    return false;
                }
            } else if (!this.segment.equals(other.segment)) {
                return false;
            }
            return true;
        }

        /**
         * Get outer type.
         *
         * @return outer type
         */
        private DocumentsMetadataImpl getOuterType() {
            return DocumentsMetadataImpl.this;
        }

    }

}
