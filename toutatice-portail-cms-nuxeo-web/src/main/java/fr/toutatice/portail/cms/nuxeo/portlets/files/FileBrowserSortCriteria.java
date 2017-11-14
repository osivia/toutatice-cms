package fr.toutatice.portail.cms.nuxeo.portlets.files;

/**
 * File browser sort criteria java-bean.
 *
 * @author Cédric Krommenhoek
 */
public class FileBrowserSortCriteria {

    /** Sort by index criteria. */
    public static final String SORT_BY_INDEX = "index";
    /** Sort by name criteria. */
    public static final String SORT_BY_NAME = "name";

    /** Sort criteria. */
    private String sort;
    /** Alternative sort indicator. */
    private boolean alternative;


    /**
     * Constructor.
     */
    public FileBrowserSortCriteria() {
        super();
    }


    /**
     * Getter for sort.
     *
     * @return the sort
     */
    public String getSort() {
        return this.sort;
    }

    /**
     * Setter for sort.
     *
     * @param sort the sort to set
     */
    public void setSort(String sort) {
        this.sort = sort;
    }

    /**
     * Getter for alternative.
     *
     * @return the alternative
     */
    public boolean isAlternative() {
        return this.alternative;
    }

    /**
     * Setter for alternative.
     *
     * @param alternative the alternative to set
     */
    public void setAlternative(boolean alternative) {
        this.alternative = alternative;
    }

}
