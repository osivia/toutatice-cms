package fr.toutatice.portail.cms.nuxeo.portlets.files;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

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
    /** Accepted sub-types. */
    private Map<String, String> accepetedSubTypes;
    
   /**
    * Constructor.
    * 
    * @param documentDTO
    * @param acceptedSubtypes
    */
    public FileBrowserItem(DocumentDTO documentDTO, Map<String, String> acceptedSubtypes) {
        super(documentDTO);
        this.accepetedSubTypes = acceptedSubtypes;
    }


    /**
     * Get accepted types.
     * 
     * @return accepted types
     */
    public String[] getAcceptedTypes() {
        // Nx allowed sub-types
        List<String> nxSubTypes = new ArrayList<String>(0);
        if(this.accepetedSubTypes != null){
            Set<String> nxAcceptedSubTypes = this.accepetedSubTypes.keySet();
            if(nxAcceptedSubTypes != null){
                nxSubTypes = new ArrayList<String>(nxAcceptedSubTypes);
            } 
        }
        
        // Portal form sub-types
        List<String> portalFormSubTypes = null;
        if (this.getType() != null) {
            portalFormSubTypes = this.getType().getPortalFormSubTypes();
        }
        
        // Accepted types
        if (nxSubTypes.size() > 0 && portalFormSubTypes != null) {
            portalFormSubTypes = (List<String>) CollectionUtils.intersection(portalFormSubTypes, nxSubTypes);
        }

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

}
