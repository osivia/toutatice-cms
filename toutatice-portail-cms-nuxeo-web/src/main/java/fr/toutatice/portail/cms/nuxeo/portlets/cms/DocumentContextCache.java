package fr.toutatice.portail.cms.nuxeo.portlets.cms;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osivia.portal.core.cms.CMSServiceCtx;

/**
 * Document context cache.
 * 
 * @author Cédric Krommenhoek
 */
public class DocumentContextCache {

    /** Cache attribute name. */
    public static final String ATTRIBUTE_NAME = "osivia.document.context.cache";


    /** Cache. */
    private final Map<Key, NuxeoDocumentContextImpl> cache;


    /**
     * Constructor.
     */
    public DocumentContextCache() {
        super();
        this.cache = new ConcurrentHashMap<>();
    }


    /**
     * Get document context in cache.
     * 
     * @param cmsContext CMS context
     * @param path path
     * @return document context
     */
    public NuxeoDocumentContextImpl get(CMSServiceCtx cmsContext, String path) {
        Key key = new Key(cmsContext, path);
        return this.cache.get(key);
    }


    /**
     * Put document context in cache.
     * 
     * @param cmsContext CMS context
     * @param path path
     * @param documentContext document context
     */
    public void put(CMSServiceCtx cmsContext, String path, NuxeoDocumentContextImpl documentContext) {
        Key key = new Key(cmsContext, path);
        this.cache.put(key, documentContext);
    }


    /**
     * Cache key inner-class.
     * 
     * @author Cédric Krommenhoek
     */
    private class Key {

        /** CMS context. */
        private final CMSServiceCtx cmsContext;
        /** Path. */
        private final String path;


        /**
         * Constructor.
         * 
         * @param cmsContext CMS context
         * @param path path
         */
        public Key(CMSServiceCtx cmsContext, String path) {
            super();
            this.cmsContext = cmsContext;
            this.path = path;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((cmsContext == null) ? 0 : cmsContext.hashCode());
            result = prime * result + ((path == null) ? 0 : path.hashCode());
            return result;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Key other = (Key) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (cmsContext == null) {
                if (other.cmsContext != null)
                    return false;
            } else if (!cmsContext.equals(other.cmsContext))
                return false;
            if (path == null) {
                if (other.path != null)
                    return false;
            } else if (!path.equals(other.path))
                return false;
            return true;
        }

        /**
         * Get outer type.
         * 
         * @return outer type
         */
        private DocumentContextCache getOuterType() {
            return DocumentContextCache.this;
        }

    }

}
