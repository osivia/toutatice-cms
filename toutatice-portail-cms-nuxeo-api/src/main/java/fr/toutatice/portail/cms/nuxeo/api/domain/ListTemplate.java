package fr.toutatice.portail.cms.nuxeo.api.domain;

/**
 * List template java-bean.
 *
 * @author Cédric Krommenhoek
 */
public class ListTemplate {

    /** Template key. */
    private final String key;
    /** Template label. */
    private final String label;
    /** Template schemas. */
    private final String schemas;
    /** Template module. */
    private ITemplateModule module;


    /**
     * Constructor.
     *
     * @param key template key
     * @param label template label
     * @param schemas template schemas
     */
    public ListTemplate(String key, String label, String schemas) {
        super();
        this.key = key;
        this.label = label;
        this.schemas = schemas;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "ListTemplate [label=" + this.label + "]";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.key == null) ? 0 : this.key.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (this.getClass() != obj.getClass())
            return false;
        ListTemplate other = (ListTemplate) obj;
        if (this.key == null) {
            if (other.key != null)
                return false;
        } else if (!this.key.equals(other.key))
            return false;
        return true;
    }


    /**
     * Getter for key.
     *
     * @return the key
     */
    public String getKey() {
        return this.key;
    }

    /**
     * Getter for label.
     *
     * @return the label
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Getter for schemas.
     *
     * @return the schemas
     */
    public String getSchemas() {
        return this.schemas;
    }

    /**
     * Getter for module.
     * 
     * @return the module
     */
    public ITemplateModule getModule() {
        return this.module;
    }

    /**
     * Setter for module.
     * 
     * @param module the module to set
     */
    public void setModule(ITemplateModule module) {
        this.module = module;
    }

}
