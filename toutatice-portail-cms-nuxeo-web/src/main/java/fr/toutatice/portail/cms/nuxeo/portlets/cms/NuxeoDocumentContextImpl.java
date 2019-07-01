package fr.toutatice.portail.cms.nuxeo.portlets.cms;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.cms.NuxeoDocumentContext;
import fr.toutatice.portail.cms.nuxeo.api.cms.NuxeoPermissions;
import fr.toutatice.portail.cms.nuxeo.api.cms.NuxeoPublicationInfos;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCustomizer;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoService;
import fr.toutatice.portail.cms.nuxeo.portlets.commands.DenormalizedDocumentFetchCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.document.helpers.DocumentConstants;
import fr.toutatice.portail.cms.nuxeo.portlets.service.CMSService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jboss.portal.core.controller.ControllerContext;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.osivia.portal.api.cms.DocumentState;
import org.osivia.portal.api.cms.DocumentType;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.core.cms.*;
import org.osivia.portal.core.web.IWebIdService;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Nuxeo document context implementation.
 *
 * @author Cédric Krommenhoek
 * @see NuxeoDocumentContext
 */
public class NuxeoDocumentContextImpl implements NuxeoDocumentContext {

    /**
     * Publication infos.
     */
    private final NuxeoPublicationInfosImpl publicationInfos;
    /**
     * Permissions.
     */
    private final NuxeoPermissionsImpl permissions;
    /**
     * CMS context.
     */
    private final CMSServiceCtx cmsContext;
    /**
     * CMS path or webId.
     */
    private final String path;
    /**
     * CMS service locator.
     */
    private final ICMSServiceLocator cmsServiceLocator;
    /**
     * Nuxeo service.
     */
    private final INuxeoService nuxeoService;
    /**
     * WebId service.
     */
    private final IWebIdService webIdService;
    /**
     * Document.
     */
    private Document document;
    /**
     * Denormalized document.
     */
    private Document denormalizedDocument;
    /**
     * Document type.
     */
    private DocumentType documentType;
    /**
     * CMS publication informations.
     */
    private CMSPublicationInfos cmsPublicationInfos;
    /**
     * Extended document informations.
     */
    private ExtendedDocumentInfos extendedInfos;
    /**
     * CMS path.
     */
    private String cmsPath;
    /**
     * WebId.
     */
    private String webId;
    /**
     * Initialized document indicator.
     */
    private boolean initializedDocument;
    /**
     * Initialized denormalized document indicator.
     */
    private boolean initializedDenormalizedDocument;
    /**
     * Initialized document type indicator.
     */
    private boolean initializedDocumentType;
    /**
     * Initialized CMS publications informations indicator.
     */
    private boolean initializedCmsPublicationInfos;
    /**
     * Initialized CMS extended informations indicator.
     */
    private boolean initializedExtendedInfos;


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
     * @param path       CMS path or webId
     * @return document context
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
        clonedCmsContext.setSatellite(cmsContext.getSatellite());


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
    public PortalControllerContext getPortalControllerContext() {
        // Controller context
        ControllerContext controllerContext = this.cmsContext.getControllerContext();

        return new PortalControllerContext(controllerContext);
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
                throwNuxeoException(e);
                return;
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
    public Document getDenormalizedDocument() {
        if (!this.initializedDenormalizedDocument) {
            this.initDenormalizedDocument();
        }

        return this.denormalizedDocument;
    }


    /**
     * Initialized denormalized document.
     */
    private synchronized void initDenormalizedDocument() {
        if (!this.initializedDenormalizedDocument) {
            // CMS customizer
            INuxeoCustomizer cmsCustomizer = this.nuxeoService.getCMSCustomizer();

            // CMS path
            String cmsPath = this.getCmsPath();
            // State
            int state = NumberUtils.toInt(this.cmsContext.getDisplayLiveVersion(), 2);

            // Nuxeo command
            INuxeoCommand command = new DenormalizedDocumentFetchCommand(cmsPath, state);

            try {
                this.denormalizedDocument = (Document) cmsCustomizer.executeNuxeoCommand(this.cmsContext, command);
            } catch (CMSException e) {
                throwNuxeoException(e);
            }

            this.initializedDenormalizedDocument = true;
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
        boolean savedForceReload = this.cmsContext.isForceReload();
        try {
            this.cmsContext.setForceReload(true);
            this.reset();
            this.initDocument();
        } finally {
            this.cmsContext.setForceReload(savedForceReload);
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
                throwNuxeoException(e);
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
                    throwNuxeoException(e);
                }
            }

            this.initializedExtendedInfos = true;
        }
    }


    /**
     * Reset document context.
     */
    private void reset() {
        this.publicationInfos.reset();
        this.permissions.reset();
        this.cmsPath = null;
        this.webId = null;
        this.initializedDocument = false;
        this.initializedDenormalizedDocument = false;
        this.initializedDocumentType = false;
        this.initializedCmsPublicationInfos = false;
        this.initializedExtendedInfos = false;
    }


    /**
     * Throw Nuxeo exception.
     *
     * @param e CMS exception
     */
    private void throwNuxeoException(CMSException e) {
        if (e.getErrorCode() == CMSException.ERROR_NOTFOUND) {
            throw new NuxeoException(NuxeoException.ERROR_NOTFOUND);
        } else if (e.getErrorCode() == CMSException.ERROR_FORBIDDEN) {
            throw new NuxeoException(NuxeoException.ERROR_FORBIDDEN);
        } else {
            throw new NuxeoException(NuxeoException.ERROR_UNAVAILAIBLE, e.getCause());
        }
    }

}
