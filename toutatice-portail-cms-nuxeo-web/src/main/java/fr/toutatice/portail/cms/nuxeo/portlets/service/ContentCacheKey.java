package fr.toutatice.portail.cms.nuxeo.portlets.service;

import org.osivia.portal.core.cms.CMSServiceCtx;


public class ContentCacheKey {

    /** Path. */
    private final String path;
    /** CMS context. */
    private final CMSServiceCtx cmsContext;


    /**
     * Constructor.
     *
     * @param path path
     * @param cmsContext CMS context
     */
    public ContentCacheKey(String path, CMSServiceCtx cmsContext) {
        super();
        this.path = path;
        this.cmsContext = cmsContext;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((this.cmsContext == null) ? 0 : this.cmsContext.hashCode());
        result = (prime * result) + ((this.path == null) ? 0 : this.path.hashCode());
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
        ContentCacheKey other = (ContentCacheKey) obj;
        if (this.cmsContext == null) {
            if (other.cmsContext != null) {
                return false;
            }
        } else if (!this.cmsContext.equals(other.cmsContext)) {
            return false;
        }
        if (this.path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!this.path.equals(other.path)) {
            return false;
        }
        return true;
    }

}
