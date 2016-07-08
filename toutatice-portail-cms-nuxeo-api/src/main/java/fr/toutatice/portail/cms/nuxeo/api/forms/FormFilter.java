package fr.toutatice.portail.cms.nuxeo.api.forms;


/**
 * The Class FormFilter.
 */
public class FormFilter {
    /** Form filter id. */
    private final String key;
    /** Fragment filter label. */
    private final String label;
    /** Form filter module. */
    private final IFormFilterModule module;
    
    /**
     * Gets the key.
     *
     * @return the key
     */
    public String getKey() {
        return key;
    }

    
    /**
     * Gets the label.
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }
    
    /**
     * Gets the module.
     *
     * @return the module
     */
    public IFormFilterModule getModule() {
        return module;
    }

    
    /**
     * Instantiates a new form filter.
     *
     * @param key the key
     * @param label the label
     * @param module the module
     */
    public FormFilter(String key, String label, IFormFilterModule module) {
        super();
        this.key = key;
        this.label = label;
        this.module = module;
    }
}
