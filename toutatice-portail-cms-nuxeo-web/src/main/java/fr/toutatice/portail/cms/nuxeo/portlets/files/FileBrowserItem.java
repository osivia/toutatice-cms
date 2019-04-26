package fr.toutatice.portail.cms.nuxeo.portlets.files;

import java.util.List;

import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;

/**
 * File browser item.
 *
 * @author Cédric Krommenhoek
 * @see DocumentDTO
 */
public class FileBrowserItem extends DocumentDTO {

    /** Virtual index. */
    private int index;
    /** Subscription indicator. */
    private boolean subscription;
    /** Sharing indicator. */
    private boolean sharing;


    /**
     * Constructor.
     *
     * @param documentDTO document DTO
     */
    public FileBrowserItem(DocumentDTO documentDTO) {
        super(documentDTO);
    }


    /**
     * Get accepted types.
     * 
     * @return accepted types
     */
    public String[] getAcceptedTypes() {
        // Portal form sub-types
        List<String> portalFormSubTypes = null;
        if (this.getType() != null) {
            portalFormSubTypes = this.getType().getSubtypes();
        }

        // Accepted types
        String[] acceptedTypes;
        if (portalFormSubTypes != null) {
            acceptedTypes = portalFormSubTypes.toArray(new String[portalFormSubTypes.size()]);
        } else {
            acceptedTypes = new String[0];
        }
        return acceptedTypes;
    }


    /**
     * Getter for index.
     *
     * @return the index
     */
    public int getIndex() {
        return this.index;
    }

    /**
     * Setter for index.
     *
     * @param index the index to set
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Getter for subscription.
     * 
     * @return the subscription
     */
    public boolean isSubscription() {
        return subscription;
    }

    /**
     * Setter for subscription.
     * 
     * @param subscription the subscription to set
     */
    public void setSubscription(boolean subscription) {
        this.subscription = subscription;
    }

    /**
     * Getter for sharing.
     * 
     * @return the sharing
     */
    public boolean isSharing() {
        return sharing;
    }

    /**
     * Setter for sharing.
     * 
     * @param sharing the sharing to set
     */
    public void setSharing(boolean sharing) {
        this.sharing = sharing;
    }

}
