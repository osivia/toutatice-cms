package fr.toutatice.portail.cms.nuxeo.portlets.cms;

import java.util.Date;
import java.util.Set;

import org.osivia.portal.core.cms.CMSPublicationInfos;

import fr.toutatice.portail.cms.nuxeo.api.cms.LockStatus;
import fr.toutatice.portail.cms.nuxeo.api.cms.NuxeoPublicationInfos;
import fr.toutatice.portail.cms.nuxeo.api.cms.SubscriptionStatus;

/**
 * Nuxeo publication informations implementation.
 * 
 * @author Cédric Krommenhoek
 * @see NuxeoPublicationInfos
 */
public class NuxeoPublicationInfosImpl implements NuxeoPublicationInfos {

    /** Path. */
    private String path;
    /** Live identifier. */
    private String liveId;
    /** Published indicator. */
    private boolean published;
    /** Being modified indicator. */
    private boolean beingModified;
    /** Live space indicator. */
    private boolean liveSpace;
    /** Space path. */
    private String spacePath;
    /** Space display name. */
    private String spaceDisplayName;
    /** Space type. */
    private String spaceType;
    /** Has draft indicator. */
    private boolean hasDraft;
    /** Draft indicator. */
    private boolean draft;
    /** Orphan draft indicator. */
    private boolean orphanDraft;
    /** Draft path. */
    private String draftPath;
    /** Draft contextualization path. */
    private String draftContextualizationPath;
    /** Remote publishable indicator. */
    private boolean remotePublishable;
    /** Remote published indicator. */
    private boolean remotePublished;
    /** Subtypes. */
    private Set<String> subtypes;
    /** Space identifier. */
    private String spaceId;
    /** Parent space identifier. */
    private String parentSpaceId;
    /** Drive edition URL. */
    private String driveEditionUrl;

    /** Task name. */
    private String taskName;
    /** Online task pending indicator. */
    private boolean onlineTaskPending;
    /** Online task initiator indicator. */
    private boolean onlineTaskInitiator;
    /** Validation workflow running indicator. */
    private boolean validationWorkflowRunning;
    /** Draft count. */
    private int draftCount;
    /** Subscription status. */
    private SubscriptionStatus subscriptionStatus;
    /** Lock status. */
    private LockStatus lockStatus;
    /** Lock owner. */
    private String lockOwner;
    /** Lock date. */
    private Date lockDate;
    /** Synchronization root path. */
    private String synchronizationRootPath;
    /** PDF convertible indicator. */
    private boolean pdfConvertible;
    /** Error on PDF conversion indicator. */
    private boolean errorOnPdfConversion;

    /** Initialized CMS publication informations indicator. */
    private boolean initializedCmsPublicationInfos;
    /** Initialized extended document informations indicator. */
    private boolean initializedExtendedInfos;


    /** Document context. */
    private final NuxeoDocumentContextImpl documentContext;


    /**
     * Constructor.
     * 
     * @param documentContext document context
     */
    public NuxeoPublicationInfosImpl(NuxeoDocumentContextImpl documentContext) {
        super();
        this.documentContext = documentContext;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getPath() {
        if (!this.initializedCmsPublicationInfos) {
            this.initCmsPublicationInfos();
        }

        return this.path;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getLiveId() {
        if (!this.initializedCmsPublicationInfos) {
            this.initCmsPublicationInfos();
        }

        return this.liveId;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPublished() {
        if (!this.initializedCmsPublicationInfos) {
            this.initCmsPublicationInfos();
        }

        return this.published;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBeingModified() {
        if (!this.initializedCmsPublicationInfos) {
            this.initCmsPublicationInfos();
        }

        return this.beingModified;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLiveSpace() {
        if (!this.initializedCmsPublicationInfos) {
            this.initCmsPublicationInfos();
        }

        return this.liveSpace;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getSpacePath() {
        if (!this.initializedCmsPublicationInfos) {
            this.initCmsPublicationInfos();
        }

        return this.spacePath;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getSpaceDisplayName() {
        if (!this.initializedCmsPublicationInfos) {
            this.initCmsPublicationInfos();
        }

        return this.spaceDisplayName;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getSpaceType() {
        if (!this.initializedCmsPublicationInfos) {
            this.initCmsPublicationInfos();
        }

        return this.spaceType;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDraft() {
        if (!this.initializedCmsPublicationInfos) {
            this.initCmsPublicationInfos();
        }

        return this.hasDraft;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDraft() {
        if (!this.initializedCmsPublicationInfos) {
            this.initCmsPublicationInfos();
        }

        return this.draft;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOrphanDraft() {
        if (!this.initializedCmsPublicationInfos) {
            this.initCmsPublicationInfos();
        }

        return this.orphanDraft;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getDraftPath() {
        if (!this.initializedCmsPublicationInfos) {
            this.initCmsPublicationInfos();
        }

        return this.draftPath;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getDraftContextualizationPath() {
        if (!this.initializedCmsPublicationInfos) {
            this.initCmsPublicationInfos();
        }

        return this.draftContextualizationPath;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRemotePublishable() {
        if (!this.initializedCmsPublicationInfos) {
            this.initCmsPublicationInfos();
        }

        return this.remotePublishable;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRemotePublished() {
        if (!this.initializedCmsPublicationInfos) {
            this.initCmsPublicationInfos();
        }

        return this.remotePublished;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getSubtypes() {
        if (!this.initializedCmsPublicationInfos) {
            this.initCmsPublicationInfos();
        }

        return this.subtypes;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getSpaceId() {
        if (!this.initializedCmsPublicationInfos) {
            this.initCmsPublicationInfos();
        }

        return this.spaceId;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getParentSpaceId() {
        if (!this.initializedCmsPublicationInfos) {
            this.initCmsPublicationInfos();
        }

        return this.parentSpaceId;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getDriveEditionUrl() {
        if (!this.initializedCmsPublicationInfos) {
            this.initCmsPublicationInfos();
        }

        return this.driveEditionUrl;
    }


    /**
     * Initialize CMS publication informations.
     */
    private synchronized void initCmsPublicationInfos() {
        if (!this.initializedCmsPublicationInfos) {
            // CMS publication infos
            CMSPublicationInfos cmsPublicationInfos = this.documentContext.getCmsPublicationInfos();

            // Path
            this.path = cmsPublicationInfos.getDocumentPath();
            // Live identifier
            this.liveId = cmsPublicationInfos.getLiveId();
            // Published indicator
            this.published = cmsPublicationInfos.isPublished();
            // Being modified indicator
            this.beingModified = cmsPublicationInfos.isBeingModified();
            // Live space indicator
            this.liveSpace = cmsPublicationInfos.isLiveSpace();
            // Space path
            this.spacePath = cmsPublicationInfos.getPublishSpacePath();
            // Space display name
            this.spaceDisplayName = cmsPublicationInfos.getPublishSpaceDisplayName();
            // Space type
            this.spaceType = cmsPublicationInfos.getPublishSpaceType();
            // Has draft indicator
            this.hasDraft = cmsPublicationInfos.hasDraft();
            // Draft indicator
            this.draft = cmsPublicationInfos.isDraft();
            // Orphan draft indicator
            this.orphanDraft = !cmsPublicationInfos.isNotOrphanDraft();
            // Draft path
            this.draftPath = cmsPublicationInfos.getDraftPath();
            // Draft contextualization path
            this.draftContextualizationPath = cmsPublicationInfos.getDraftContextualizationPath();
            // Remote publishable indicator
            this.remotePublishable = cmsPublicationInfos.isRemotePublishable();
            // Remote published indicator
            this.remotePublished = cmsPublicationInfos.isRemotePublished();
            // Subtypes
            this.subtypes = cmsPublicationInfos.getSubTypes().keySet();
            // Space identifier
            this.spaceId = cmsPublicationInfos.getSpaceID();
            // Parent space identifier
            this.parentSpaceId = cmsPublicationInfos.getParentSpaceID();
            // Drive edition URL
            this.driveEditionUrl = cmsPublicationInfos.getDriveEditURL();

            this.initializedCmsPublicationInfos = true;
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getTaskName() {
        if (!this.initializedExtendedInfos) {
            this.initExtendedInfos();
        }

        return this.taskName;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOnlineTaskPending() {
        if (!this.initializedExtendedInfos) {
            this.initExtendedInfos();
        }

        return this.onlineTaskPending;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOnlineTaskInitiator() {
        if (!this.initializedExtendedInfos) {
            this.initExtendedInfos();
        }

        return this.onlineTaskInitiator;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValidationWorkflowRunning() {
        if (!this.initializedExtendedInfos) {
            this.initExtendedInfos();
        }

        return this.validationWorkflowRunning;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int getDraftCount() {
        if (!this.initializedExtendedInfos) {
            this.initExtendedInfos();
        }

        return this.draftCount;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public SubscriptionStatus getSubscriptionStatus() {
        if (!this.initializedExtendedInfos) {
            this.initExtendedInfos();
        }

        return this.subscriptionStatus;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public LockStatus getLockStatus() {
        if (!this.initializedExtendedInfos) {
            this.initExtendedInfos();
        }

        return this.lockStatus;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getLockOwner() {
        if (!this.initializedExtendedInfos) {
            this.initExtendedInfos();
        }

        return this.lockOwner;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Date getLockDate() {
        if (!this.initializedExtendedInfos) {
            this.initExtendedInfos();
        }

        // Date
        return this.lockDate;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getSynchronizationRootPath() {
        if (!this.initializedExtendedInfos) {
            this.initExtendedInfos();
        }

        return this.synchronizationRootPath;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPdfConvertible() {
        if (!this.initializedExtendedInfos) {
            this.initExtendedInfos();
        }

        return this.pdfConvertible;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isErrorOnPdfConversion() {
        if (!this.initializedExtendedInfos) {
            this.initExtendedInfos();
        }

        return this.errorOnPdfConversion;
    }


    /**
     * Initialize extended document informations.
     */
    private synchronized void initExtendedInfos() {
        if (!this.initializedExtendedInfos) {
            // Extended document informations
            ExtendedDocumentInfos extendedInfos = this.documentContext.getExtendedInfos();

            // Task name
            this.taskName = extendedInfos.getTaskName();
            // Online task pending indicator
            this.onlineTaskPending = extendedInfos.isOnlineTaskPending();
            // Online task initiator indicator
            this.onlineTaskInitiator = extendedInfos.isUserOnlineTaskInitiator();
            // Validation workflow running indicator
            this.validationWorkflowRunning = extendedInfos.isValidationWorkflowRunning();
            // Draft count
            this.draftCount = extendedInfos.getDraftCount();
            // Subscription status
            this.subscriptionStatus = extendedInfos.getSubscriptionStatus();
            // Lock status
            this.lockStatus = extendedInfos.getLockStatus();
            // Lock owner
            this.lockOwner = extendedInfos.getLockOwner();
            // Lock date
            if (extendedInfos.getLockDate() == null) {
                this.lockDate = null;
            } else {
                this.lockDate = extendedInfos.getLockDate().getTime();
            }
            // Synchronization root path
            this.synchronizationRootPath = extendedInfos.getSynchronizationRootPath();
            // PDF convertible indicator
            this.pdfConvertible = extendedInfos.isPdfConvertible();
            // Error on PDF conversion indicator
            this.errorOnPdfConversion = extendedInfos.isErrorOnPdfConversion();

            this.initializedExtendedInfos = true;
        }
    }


    /**
     * Refresh publication informations.
     */
    public void refresh() {
        this.initializedCmsPublicationInfos = false;
        this.initializedExtendedInfos = false;
    }

}
