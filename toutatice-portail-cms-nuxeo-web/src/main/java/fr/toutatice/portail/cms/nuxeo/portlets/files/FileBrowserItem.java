package fr.toutatice.portail.cms.nuxeo.portlets.files;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

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
     */
     public FileBrowserItem(DocumentDTO documentDTO) {
         super(documentDTO);
         this.accepetedSubTypes = new HashMap<String, String>(0);
     }
    
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
        if(MapUtils.isNotEmpty(this.accepetedSubTypes)){
            Set<String> nxAcceptedSubTypes = this.accepetedSubTypes.keySet();
            if(CollectionUtils.isNotEmpty(nxAcceptedSubTypes)){
                nxSubTypes = new ArrayList<String>(nxAcceptedSubTypes);
            } 
        }
        
        // Portal form sub-types
        List<String> portalFormSubTypes = new ArrayList<String>(0);
        if (this.getType() != null) {
            portalFormSubTypes = this.getType().getPortalFormSubTypes();
        }
        
        // Accepted types
        String[] acceptedTypes = new String[0];
        
        if (nxSubTypes.size() > 0 && portalFormSubTypes.size() > 0) {
            Collection<String> accepetedTypesColl = CollectionUtils.intersection(portalFormSubTypes, nxSubTypes);
            acceptedTypes = portalFormSubTypes.toArray(new String[accepetedTypesColl.size()]);
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
