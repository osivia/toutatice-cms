package fr.toutatice.portail.cms.nuxeo.portlets.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.cms.EcmDocument;
import org.osivia.portal.api.cms.Symlink;
import org.osivia.portal.core.cms.DocumentsMetadata;
import org.osivia.portal.core.web.IWebUrlService;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Documents metadata implementation.
 *
 * @author Cédric Krommenhoek
 * @see DocumentsMetadata
 */
public class DocumentsMetadataImpl implements DocumentsMetadata {

    /**
     * WebId Nuxeo document property name.
     */
    public static final String WEB_ID_PROPERTY = "ttc:webid";
    /**
     * Web URL segment Nuxeo document property name.
     */
    public static final String WEB_URL_SEGMENT_PROPERTY = "ottcweb:segment";
    /**
     * Modified Nuxeo document property name.
     */
    public static final String MODIFIED_PROPERTY = "dc:modified";


    /**
     * WebId path prefix.
     */
    private static final String WEB_ID_PATH_PREFIX = "/" + IWebUrlService.WEB_ID_PREFIX;


    /**
     * CMS base path.
     */
    private final String basePath;
    /**
     * Nuxeo documents.
     */
    private final List<Document> documents;
    /**
     * Symlinks.
     */
    private final List<Symlink> symlinks;

    /**
     * WebId to path association map.
     */
    private final ConcurrentMap<String, String> webIds;
    /**
     * Path to webId and segment association map.
     */
    private final ConcurrentMap<String, PathValues> paths;
    /**
     * Parent path and segment to child path association map.
     */
    private final ConcurrentMap<SegmentKey, String> segments;

    /**
     * WebId to web path association map cache.
     */
    private final ConcurrentMap<String, String> toWebPaths;
    /**
     * Web path to webId association map cache.
     */
    private final ConcurrentMap<String, String> fromWebPaths;

    /**
     * Log.
     */
    private final Log log;


    /**
     * Timestamp.
     */
    private long timestamp;


    /**
     * Constructor.
     *
     * @param basePath  CMS base path
     * @param documents Nuxeo documents
     * @param symlinks  symlinks
     */
    public DocumentsMetadataImpl(String basePath, List<Document> documents, List<Symlink> symlinks) {
        super();
        this.basePath = basePath;
        this.documents = documents;
        this.symlinks = symlinks;
        this.webIds = new ConcurrentHashMap<>(this.documents.size());
        this.paths = new ConcurrentHashMap<>(this.documents.size() + this.symlinks.size());
        this.segments = new ConcurrentHashMap<>(this.documents.size() + this.symlinks.size());
        this.toWebPaths = new ConcurrentHashMap<>(this.documents.size());
        this.fromWebPaths = new ConcurrentHashMap<>(this.documents.size());


        // Symlinks
        for (Symlink symlink : this.symlinks) {
            // Paths
            this.paths.put(symlink.getVirtualPath(), new PathValues(symlink.getTargetWebId(), symlink.getSegment()));

            // Web URL segments
            this.segments.put(new SegmentKey(symlink.getParentPath(), symlink.getSegment()), symlink.getTargetPath());
        }


        // Maps & timestamp initialization
        this.timestamp = 0;
        for (Document document : documents) {
            String path = StringUtils.removeEnd(document.getPath(), ".proxy");
            String webId = document.getString(WEB_ID_PROPERTY);
            String segment = document.getString(WEB_URL_SEGMENT_PROPERTY);
            Date modified = document.getDate(MODIFIED_PROPERTY);

            // WebIds
            if (webId != null) {
                this.webIds.put(webId, path);
            }

            // Paths
            if ((webId != null) || (segment != null)) {
                this.paths.put(path, new PathValues(webId, segment));
            }

            // Web URL segments
            if (!path.equals(basePath) && (segment != null)) {
                this.segments.put(new SegmentKey(StringUtils.substringBeforeLast(path, "/"), segment), path);
            }

            if (modified != null) {
                this.timestamp = Math.max(this.timestamp, modified.getTime());
            }
        }


        // Increase time to exclude last modified document from the next request (Nuxeo documents timestamp are truncated to 10ms)
        this.timestamp += 10;


        // Log
        this.log = LogFactory.getLog(this.getClass());
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


        // Symlink
        Symlink symlink = null;
        if (!path.startsWith(this.basePath)) {
            for (Symlink link : this.symlinks) {
                if (path.startsWith(link.getTargetPath())) {
                    symlink = link;
                    break;
                }
            }
        }


        // Split path
        String[] splitPath;
        if (symlink == null) {
            splitPath = StringUtils.split(path, "/");
        } else {
            String lookupPath = symlink.getVirtualPath() + StringUtils.substringAfter(path, symlink.getTargetPath());
            splitPath = StringUtils.split(lookupPath, "/");
        }


        // Search the closest web path
        String closestWebPath = null;
        int factor;
        for (factor = splitPath.length; factor > 0; factor--) {
            // Working path
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < factor; i++) {
                builder.append("/");
                builder.append(splitPath[i]);
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
            workingPath.append(splitPath[i]);
        }


        // Build web path
        boolean incompleteWebPath = !StringUtils.startsWith(workingPath.toString(), this.basePath);
        for (int i = factor; i < splitPath.length; i++) {
            workingPath.append("/");
            workingPath.append(splitPath[i]);

            // Current path
            String currentPath = workingPath.toString();
            if ((symlink != null) && !currentPath.equals(symlink.getVirtualPath()) && currentPath.startsWith(symlink.getVirtualPath())) {
                currentPath = symlink.getTargetPath() + StringUtils.substringAfter(currentPath, symlink.getVirtualPath());
            }
            PathValues currentPathValues = this.paths.get(currentPath);

            // Segment
            if ((currentPathValues != null) && StringUtils.isNotBlank(currentPathValues.segment)) {
                workingWebPath.append("/");
                workingWebPath.append(currentPathValues.segment);

                // Current web path
                String currentWebPath = workingWebPath.toString();
                if (currentPathValues.webId != null) {
                    this.toWebPaths.put(currentPathValues.webId, currentWebPath);

                    // Reversed dependency
                    this.fromWebPaths.put(currentWebPath, currentPathValues.webId);
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
        }
        webPath = workingWebPath.toString();
        this.toWebPaths.put(webId, webPath);

        // Reversed dependency
        this.fromWebPaths.put(webPath, webId);

        return webPath;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getWebId(String webPath) {
        if (StringUtils.EMPTY.equals(webPath) || StringUtils.equals("/", webPath)) {
            PathValues pathValues = this.paths.get(this.basePath);
            String webId = null;
            if (pathValues != null) {
                webId = pathValues.webId;
            }
            return webId;
        } else if (StringUtils.isBlank(webPath)) {
            return null;
        } else {
            String webId = this.fromWebPaths.get(webPath);
            if (webId != null) {
                return webId;
            }
        }


        // Split web path
        String[] splitWebPath = StringUtils.split(webPath, "/");

        // Search closest webId
        String closestWebId = null;
        int factor;
        for (factor = splitWebPath.length; factor > 1; factor--) {
            // Working web path
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < factor; i++) {
                builder.append("/");
                builder.append(splitWebPath[i]);
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
            workingWebPath.append(splitWebPath[i]);
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
        for (int i = factor; i < splitWebPath.length; i++) {
            // Segment
            String segment = splitWebPath[i];

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
    public List<EcmDocument> getDocuments() {
        return new ArrayList<>(this.documents);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<Symlink> getSymlinks() {
        return this.symlinks;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void update(DocumentsMetadata updates) {
        if (CollectionUtils.isNotEmpty(updates.getDocuments())) {
            // Update symlinks
            this.updateSymlinks(updates);

            for (EcmDocument ecmDocument : updates.getDocuments()) {
                if (ecmDocument instanceof Document) {
                    // Nuxeo document
                    Document document = (Document) ecmDocument;

                    if (this.log.isDebugEnabled()) {
                        this.log.debug(String.format("[Update] Start update for document '%s'", document.getPath()));
                    }

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
                            Map<String, String> addedWebIds = new HashMap<>();
                            if (originalPath != null) {
                                for (Entry<String, String> entry : this.webIds.entrySet()) {
                                    String currentWebId = entry.getKey();
                                    String currentPath = entry.getValue();
                                    if (currentPath.startsWith(originalPath)) {
                                        addedWebIds.put(currentWebId, path + StringUtils.substringAfter(currentPath, originalPath));

                                        // Remove obsolete web path
                                        this.toWebPaths.remove(currentWebId);
                                    }
                                }
                            }
                            addedWebIds.put(webId, path);
                            this.webIds.putAll(addedWebIds);


                            // Update paths
                            Set<String> removedPathKeys = new HashSet<>();
                            Map<String, PathValues> addedPaths = new HashMap<>();
                            if (originalPath != null) {
                                removedPathKeys.add(originalPath);
                                for (Entry<String, PathValues> entry : this.paths.entrySet()) {
                                    String currentPath = entry.getKey();
                                    if (currentPath.startsWith(originalPath)) {
                                        removedPathKeys.add(currentPath);
                                        addedPaths.put(path + StringUtils.substringAfter(currentPath, originalPath), entry.getValue());
                                    }
                                }
                            }
                            addedPaths.put(path, new PathValues(webId, segment));
                            this.paths.keySet().removeAll(removedPathKeys);
                            this.paths.putAll(addedPaths);

                            // Update segments
                            Map<SegmentKey, String> addedSegments = new HashMap<>();
                            if ((originalPath != null) && (originalSegment != null)) {
                                this.segments.remove(new SegmentKey(StringUtils.substringBeforeLast(originalPath, "/"), originalSegment));
                                for (Entry<SegmentKey, String> entry : this.segments.entrySet()) {
                                    SegmentKey key = entry.getKey();
                                    if (key.parentPath.startsWith(originalPath)) {
                                        addedSegments.put(new SegmentKey(path, key.segment), path + StringUtils.substringAfter(entry.getValue(), originalPath));
                                    }
                                }
                            }
                            if (!path.equals(this.basePath) && (segment != null)) {
                                addedSegments.put(new SegmentKey(StringUtils.substringBeforeLast(path, "/"), segment), path);
                            }
                            this.segments.putAll(addedSegments);

                            // Remove obsolete web path
                            this.toWebPaths.remove(webId);
                        }
                    }

                    if (this.log.isDebugEnabled()) {
                        this.log.debug(String.format("[Update] End update for document '%s'", document.getPath()));
                    }
                }
            }
        }

        // Update timestamp
        this.timestamp = Math.max(this.timestamp, updates.getTimestamp());
    }


    /**
     * Update symlinks.
     *
     * @param updates update values
     */
    private void updateSymlinks(DocumentsMetadata updates) {
        // Original symlinks
        Set<Symlink> originalSymlinks = new HashSet<>(this.symlinks);
        // Removed symlinks
        Set<Symlink> removedSymlinks = new HashSet<>(this.symlinks);
        // Added symlinks
        Set<Symlink> addedSymlinks = new HashSet<>();


        // Loop on updated symlinks
        for (Symlink symlink : updates.getSymlinks()) {
            if (originalSymlinks.contains(symlink)) {
                removedSymlinks.remove(symlink);
            } else {
                addedSymlinks.add(symlink);
            }
        }


        // Updated paths
        Set<String> updatedPaths = new HashSet<>(removedSymlinks.size());


        // Remove obsolete symlinks
        for (Symlink symlink : removedSymlinks) {
            updatedPaths.add(symlink.getTargetPath());

            // Paths
            this.paths.remove(symlink.getVirtualPath());

            // Web URL segments
            this.segments.remove(new SegmentKey(symlink.getParentPath(), symlink.getSegment()));
        }
        this.symlinks.removeAll(removedSymlinks);


        // Add new symlinks
        for (Symlink symlink : addedSymlinks) {
            // Paths
            this.paths.put(symlink.getVirtualPath(), new PathValues(symlink.getTargetWebId(), symlink.getSegment()));

            // Web URL segments
            this.segments.put(new SegmentKey(symlink.getParentPath(), symlink.getSegment()), symlink.getTargetPath());
        }
        this.symlinks.addAll(addedSymlinks);


        // Remove obsolete web paths
        if (CollectionUtils.isNotEmpty(updatedPaths)) {
            String[] prefixes = updatedPaths.toArray(new String[0]);

            for (Entry<String, String> entry : this.webIds.entrySet()) {
                String currentWebId = entry.getKey();
                String currentPath = entry.getValue();
                if (StringUtils.startsWithAny(currentPath, prefixes)) {
                    this.toWebPaths.remove(currentWebId);
                }
            }
        }
    }


    /**
     * Path values inner-class.
     *
     * @author Cédric Krommenhoek
     */
    private static class PathValues {

        /**
         * WebId.
         */
        private final String webId;
        /**
         * Segment.
         */
        private final String segment;


        /**
         * Constructor.
         *
         * @param webId   webId
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
    private static class SegmentKey {

        /**
         * Parent path.
         */
        private final String parentPath;
        /**
         * Segment.
         */
        private final String segment;


        /**
         * Constructor.
         *
         * @param parentPath parent path
         * @param segment    segment
         */
        public SegmentKey(String parentPath, String segment) {
            super();
            this.parentPath = parentPath;
            this.segment = segment;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SegmentKey that = (SegmentKey) o;

            if (!Objects.equals(parentPath, that.parentPath)) return false;
            return Objects.equals(segment, that.segment);
        }


        @Override
        public int hashCode() {
            int result = parentPath != null ? parentPath.hashCode() : 0;
            result = 31 * result + (segment != null ? segment.hashCode() : 0);
            return result;
        }

    }

}
