package fr.toutatice.portail.cms.nuxeo.portlets.files;

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


    /**
     * Constructor.
     * 
     * @param documentDTO document DTO
     */
    public FileBrowserItem(DocumentDTO documentDTO) {
        super(documentDTO);
    }


    /**
     * Getter for index.
     * 
     * @return the index
     */
    public int getIndex() {
        return index;
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
