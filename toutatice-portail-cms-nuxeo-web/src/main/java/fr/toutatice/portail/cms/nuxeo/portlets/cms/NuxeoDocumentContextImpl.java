package fr.toutatice.portail.cms.nuxeo.portlets.cms;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.osivia.portal.api.cms.DocumentState;
import org.osivia.portal.api.cms.DocumentType;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.ICMSService;
import org.osivia.portal.core.cms.ICMSServiceLocator;
import org.osivia.portal.core.web.IWebIdService;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.cms.NuxeoDocumentContext;
import fr.toutatice.portail.cms.nuxeo.api.cms.NuxeoPermissions;
import fr.toutatice.portail.cms.nuxeo.api.cms.NuxeoPublicationInfos;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCustomizer;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoService;
import fr.toutatice.portail.cms.nuxeo.portlets.document.helpers.DocumentConstants;
import fr.toutatice.portail.cms.nuxeo.portlets.service.CMSService;

/**
 * Nuxeo document context implementation.
 *
 * @author Cédric Krommenhoek
 * @see NuxeoDocumentContext
 */
public class NuxeoDocumentContextImpl implements NuxeoDocumentContext {

    /** Document. */
    private Document document;
    /** Document type. */
    private DocumentType documentType;
    /** CMS publication informations. */
    private CMSPublicationInfos cmsPublicationInfos;
    /** Extended document informations. */
    private ExtendedDocumentInfos extendedInfos;
    /** CMS path. */
    private String cmsPath;
    /** WebId. */
    private String webId;

    /** Initialized document indicator. */
    private boolean initializedDocument;
    /** Initialized document type indicator. */
    private boolean initializedDocumentType;
    /** Initialized CMS publications informations indicator. */
    private boolean initializedCmsPublicationInfos;
    /** Initialized CMS extended informations indicator. */
    private boolean initializedExtendedInfos;


    /** Publication infos. */
    private final NuxeoPublicationInfosImpl publicationInfos;
    /** Permissions. */
    private final NuxeoPermissionsImpl permissions;

    /** CMS context. */
    private final CMSServiceCtx cmsContext;
    /** Path. */
    private final String path;

    /** CMS service locator. */
    private final ICMSServiceLocator cmsServiceLocator;
    /** Nuxeo service. */
    private final INuxeoService nuxeoService;
    /** WebId service. */
    private final IWebIdService webIdService;


    /**
     * Constructor.
     */
    private NuxeoDocumentContextImpl(CMSServiceCtx cmsContext, String path) {
        super();
        this.cmsContext = cmsContext;
        this.path = path;
        this.publicationInfos = new NuxeoPublicationInfosImpl(this);
        this.permissions = new NuxeoPermissionsImpl(this);

        // CMS service locator
        this.cmsServiceLocator = Locator.findMBean(ICMSServiceLocator.class, ICMSServiceLocator.MBEAN_NAME);
        // Nuxeo service
        this.nuxeoService = Locator.findMBean(INuxeoService.class, INuxeoService.MBEAN_NAME);
        // WebId service
        this.webIdService = Locator.findMBean(IWebIdService.class, IWebIdService.MBEAN_NAME);
    }


    /**
     * Get document context.
     * 
     * @param cmsContext CMS context
     * @param path path
     * @return document context
     * @throws CMSException
     */
    public static NuxeoDocumentContextImpl getDocumentContext(CMSServiceCtx cmsContext, String path) throws CMSException {
        if (StringUtils.isEmpty(path)) {
            throw new CMSException("Document context path must not be empty.");
        }

        // HTTP servlet request
        HttpServletRequest request = cmsContext.getServletRequest();

        // Cloned CMS context
        CMSServiceCtx clonedCmsContext = new CMSServiceCtx();
        clonedCmsContext.setScope(cmsContext.getScope());
        clonedCmsContext.setDisplayLiveVersion(cmsContext.getDisplayLiveVersion());
        clonedCmsContext.setDisplayContext(cmsContext.getDisplayContext());
        clonedCmsContext.setForcePublicationInfosScope(cmsContext.getForcePublicationInfosScope());
        clonedCmsContext.setForcedLivePath(cmsContext.getForcedLivePath());
        clonedCmsContext.setForceReload(cmsContext.isForceReload());
        clonedCmsContext.setContextualizationBasePath(cmsContext.getContextualizationBasePath());
        if (cmsContext.getControllerContext() != null) {
            clonedCmsContext.setControllerContext(cmsContext.getControllerContext());
        } else {
            clonedCmsContext.setServerInvocation(cmsContext.getServerInvocation());
        }
        clonedCmsContext.setServletRequest(cmsContext.getServletRequest());

        // Document context
        NuxeoDocumentContextImpl documentContext;

        if (request == null) {
            documentContext = new NuxeoDocumentContextImpl(cmsContext, path);
        } else {
            // Cache
            DocumentContextCache cache = (DocumentContextCache) request.getAttribute(DocumentContextCache.ATTRIBUTE_NAME);
            if (cache == null) {
                cache = new DocumentContextCache();
                request.setAttribute(DocumentContextCache.ATTRIBUTE_NAME, cache);
            }

            documentContext = cache.get(clonedCmsContext, path);
            
            if (documentContext == null) {
                documentContext = new NuxeoDocumentContextImpl(clonedCmsContext, path);
                cache.put(clonedCmsContext, path, documentContext);
            }
        }

        return documentContext;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getPath() {
        return this.path;
    }


    /**
     * Get CMS path.
     * 
     * @return CMS path
     */
    public String getCmsPath() {
        if (this.cmsPath == null) {
            this.initCmsPublicationInfos();
        }

        return this.cmsPath;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getWebId() {
        if (this.webId == null) {
            this.initDocument();
        }

        return this.webId;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public DocumentType getDocumentType() {
        if (!this.initializedDocumentType) {
            this.initDocumentType();
        }

        return this.documentType;
    }


    /**
     * Initialize document type.
     */
    private synchronized void initDocumentType() {
        if (!this.initializedDocumentType) {
            // Document
            Document document = this.getDocument();

            // CMS customizer
            INuxeoCustomizer cmsCustomizer = this.nuxeoService.getCMSCustomizer();
            // CMS item types
            Map<String, DocumentType> types = cmsCustomizer.getDocumentTypes();

            if ((document != null) && (types != null)) {
                this.documentType = types.get(document.getType());
            }

            this.initializedDocumentType = true;
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public DocumentState getDocumentState() {
        // Display live version indicator
        String displayLiveVersion = this.cmsContext.getDisplayLiveVersion();

        return DocumentState.parse(displayLiveVersion);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getScope() {
        return this.cmsContext.getScope();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public NuxeoPermissions getPermissions() {
        return this.permissions;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public NuxeoPublicationInfos getPublicationInfos() {
        return this.publicationInfos;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Document getDocument() {
        if (!this.initializedDocument) {
            this.initDocument();
        }

        return this.document;
    }


    /**
     * Initialize document.
     */
    private synchronized void initDocument() {
        if (!this.initializedDocument) {
            // CMS service
            ICMSService cmsService = this.cmsServiceLocator.getCMSService();

            // CMS path
            String cmsPath = this.getCmsPath();

            // CMS content
            CMSItem content;
            try {
                content = cmsService.getContent(this.cmsContext, cmsPath);
            } catch (CMSException e) {
                if (e.getErrorCode() == CMSException.ERROR_NOTFOUND) {
                    throw new NuxeoException(NuxeoException.ERROR_NOTFOUND);
                } else if (e.getErrorCode() == CMSException.ERROR_FORBIDDEN) {
                    throw new NuxeoException(NuxeoException.ERROR_FORBIDDEN);
                } else {
                    throw new NuxeoException(NuxeoException.ERROR_UNAVAILAIBLE, e.getCause());
                }
            }

            // Native item
            Object nativeItem;
            if (content == null) {
                nativeItem = null;
            } else {
                nativeItem = content.getNativeItem();
            }

            if ((nativeItem != null) && (nativeItem instanceof Document)) {
                this.document = (Document) nativeItem;
            }

            // WebId
            if (content != null) {
                this.webId = content.getWebId();
            }

            this.initializedDocument = true;
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayContext() {
        return this.cmsContext.getDisplayContext();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isContextualized() {
        return StringUtils.isNotEmpty(this.cmsContext.getContextualizationBasePath());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRemoteProxy() {
        // Nuxeo document
        Document document = this.getDocument();

        // Facets
        PropertyList facets = document.getFacets();

        // Remote proxy indicator
        boolean remoteProxy;
        if (facets == null) {
            remoteProxy = false;
        } else {
            remoteProxy = facets.list().contains(DocumentConstants.REMOTE_PROXY_FACET);
        }

        return remoteProxy;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void reload() {
        boolean savedValue = this.cmsContext.isForceReload();
        try {
            this.cmsContext.setForceReload(true);
            this.refresh();
            this.initDocument();
        } finally {
            this.cmsContext.setForceReload(savedValue);
        }
    }


    /**
     * Get CMS publication informations.
     *
     * @return CMS publication informations
     */
    public CMSPublicationInfos getCmsPublicationInfos() {
        if (!this.initializedCmsPublicationInfos) {
            this.initCmsPublicationInfos();
        }

        return this.cmsPublicationInfos;
    }


    /**
     * Initialize CMS publication informations.
     */
    private synchronized void initCmsPublicationInfos() {
        if (!this.initializedCmsPublicationInfos) {
            // CMS service
            ICMSService cmsService = this.cmsServiceLocator.getCMSService();

            // Fetch path
            String fetchPath;
            if (this.cmsPath != null) {
                fetchPath = this.cmsPath;
            } else if (this.webId != null) {
                fetchPath = this.webIdService.webIdToFetchPath(this.webId);
            } else if (StringUtils.startsWith(this.path, "/") || StringUtils.startsWith(this.path, IWebIdService.FETCH_PATH_PREFIX)) {
                fetchPath = this.path;
            } else {
                fetchPath = this.webIdService.webIdToFetchPath(this.path);
            }

            try {
                this.cmsPublicationInfos = cmsService.getPublicationInfos(this.cmsContext, fetchPath);
            } catch (CMSException e) {
                if (e.getErrorCode() == CMSException.ERROR_NOTFOUND) {
                    throw new NuxeoException(NuxeoException.ERROR_NOTFOUND);
                } else if (e.getErrorCode() == CMSException.ERROR_FORBIDDEN) {
                    throw new NuxeoException(NuxeoException.ERROR_FORBIDDEN);
                } else {
                    throw new NuxeoException(NuxeoException.ERROR_UNAVAILAIBLE, e.getCause());
                }
            }

            // CMS path
            this.cmsPath = this.cmsPublicationInfos.getDocumentPath();

            this.initializedCmsPublicationInfos = true;
        }
    }


    /**
     * Get extended document informations.
     *
     * @return extended document informations
     */
    public ExtendedDocumentInfos getExtendedInfos() {
        if (!this.initializedExtendedInfos) {
            this.initExtendedInfos();
        }

        return this.extendedInfos;
    }


    /**
     * Initialize extended document informations.
     */
    private synchronized void initExtendedInfos() {
        if (!this.initializedExtendedInfos) {
            // CMS service
            ICMSService cmsService = this.cmsServiceLocator.getCMSService();

            // CMS path
            String cmsPath = this.getCmsPath();

            if (cmsService instanceof CMSService) {
                CMSService cmsServiceImpl = (CMSService) cmsService;

                try {
                    this.extendedInfos = cmsServiceImpl.getExtendedDocumentInfos(this.cmsContext, cmsPath);
                } catch (CMSException e) {
                    if (e.getErrorCode() == CMSException.ERROR_NOTFOUND) {
                        throw new NuxeoException(NuxeoException.ERROR_NOTFOUND);
                    } else if (e.getErrorCode() == CMSException.ERROR_FORBIDDEN) {
                        throw new NuxeoException(NuxeoException.ERROR_FORBIDDEN);
                    } else {
                        throw new NuxeoException(NuxeoException.ERROR_UNAVAILAIBLE, e.getCause());
                    }
                }
            }

            this.initializedExtendedInfos = true;
        }
    }


    /**
     * Refresh document context.
     */
    private void refresh() {
        this.publicationInfos.refresh();
        this.permissions.refresh();
        this.cmsPath = null;
        this.webId = null;
        this.initializedDocument = false;
        this.initializedDocumentType = false;
        this.initializedCmsPublicationInfos = false;
        this.initializedExtendedInfos = false;
    }

}
