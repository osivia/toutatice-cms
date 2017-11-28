package fr.toutatice.portail.cms.nuxeo.portlets.cms;

import org.osivia.portal.core.cms.CMSPublicationInfos;

import fr.toutatice.portail.cms.nuxeo.api.cms.NuxeoPermissions;

/**
 * Nuxeo permissions implementation.
 * 
 * @author Cédric Krommenhoek
 * @see NuxeoPermissions
 */
public class NuxeoPermissionsImpl implements NuxeoPermissions {

    /** Editable indicator. */
    private boolean editable;
    /** Manageable indicator. */
    private boolean manageable;
    /** Deletable indicator. */
    private boolean deletable;
    /** Commentable indicator. */
    private boolean commentable;
    /** Anonymously readable indicator. */
    private boolean anonymouslyReadable;
    /** Can be validated indicator. */
    private boolean canBeValidated;
    /** Can be copied indicator. */
    private boolean canBeCopied;

    /** Can validate online task indicator. */
    private boolean canValidateOnlineTask;
    /** Can synchronize indicator. */
    private boolean canSynchronize;
    /** Can unsynchronize indicator. */
    private boolean canUnsynchronize;

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
    public NuxeoPermissionsImpl(NuxeoDocumentContextImpl documentContext) {
        super();
        this.documentContext = documentContext;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEditable() {
        if (!this.initializedCmsPublicationInfos) {
            this.initCmsPublicationInfos();
        }

        return this.editable;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isManageable() {
        if (!this.initializedCmsPublicationInfos) {
            this.initCmsPublicationInfos();
        }

        return this.manageable;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDeletable() {
        if (!this.initializedCmsPublicationInfos) {
            this.initCmsPublicationInfos();
        }

        return this.deletable;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCommentable() {
        if (!this.initializedCmsPublicationInfos) {
            this.initCmsPublicationInfos();
        }

        return this.commentable;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAnonymouslyReadable() {
        if (!this.initializedCmsPublicationInfos) {
            this.initCmsPublicationInfos();
        }

        return this.anonymouslyReadable;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canBeValidated() {
        if (!this.initializedCmsPublicationInfos) {
            this.initCmsPublicationInfos();
        }

        return this.canBeValidated;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canBeCopied() {
        if (!this.initializedCmsPublicationInfos) {
            this.initCmsPublicationInfos();
        }

        return this.canBeCopied;
    }


    /**
     * Initialize CMS publication informations.
     */
    private synchronized void initCmsPublicationInfos() {
        if (!this.initializedCmsPublicationInfos) {
            // CMS publication infos
            CMSPublicationInfos cmsPublicationInfos = this.documentContext.getCmsPublicationInfos();

            // Editable indicator
            this.editable = cmsPublicationInfos.isEditableByUser();
            // Manageable indicator
            this.manageable = cmsPublicationInfos.isManageableByUser();
            // Deletable indicator
            this.deletable = cmsPublicationInfos.isDeletableByUser();
            // Commentable indicator
            this.commentable = cmsPublicationInfos.isCommentableByUser();
            // Anonymously readable indicator
            this.anonymouslyReadable = cmsPublicationInfos.isAnonymouslyReadable();
            // Can be validated indicator
            this.canBeValidated = cmsPublicationInfos.isUserCanValidate();
            // Can be copied indicator
            this.canBeCopied = cmsPublicationInfos.isCopiable();

            this.initializedCmsPublicationInfos = true;
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canValidateOnlineTask() {
        if (!this.initializedExtendedInfos) {
            this.initExtendedInfos();
        }

        return this.canValidateOnlineTask;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canSynchronize() {
        if (!this.initializedExtendedInfos) {
            this.initExtendedInfos();
        }

        return this.canSynchronize;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canUnsynchronize() {
        if (!this.initializedExtendedInfos) {
            this.initExtendedInfos();
        }

        return this.canUnsynchronize;
    }


    /**
     * Initialize extended document informations.
     */
    private synchronized void initExtendedInfos() {
        if (!this.initializedExtendedInfos) {
            // Extended document informations
            ExtendedDocumentInfos extendedInfos = this.documentContext.getExtendedInfos();

            // Can validate online task indicator
            this.canValidateOnlineTask = extendedInfos.isCanUserValidateOnlineTask();
            // Can synchronize indicator
            this.canSynchronize = extendedInfos.isCanSynchronize();
            // Can unsynchronize indicator
            this.canUnsynchronize = extendedInfos.isCanUnsynchronize();

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
