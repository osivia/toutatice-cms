package fr.toutatice.portail.cms.nuxeo.api.domain;


/**
 * Fragment type java-bean.
 *
 * @author Cédric Krommenhoek
 */
public class FragmentType {

    /** Fragment type key. */
    private final String key;
    /** Fragment type label. */
    private final String label;
    /** Fragment module. */
    private final IFragmentModule module;


    /**
     * Constructor.
     *
     * @param key fragment type key
     * @param label fragment type label
     * @param module fragment module
     */
    public FragmentType(String key, String label, IFragmentModule module) {
        super();
        this.key = key;
        this.label = label;
        this.module = module;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "FragmentType [label=" + this.label + "]";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((this.key == null) ? 0 : this.key.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        FragmentType other = (FragmentType) obj;
        if (this.key == null) {
            if (other.key != null) {
                return false;
            }
        } else if (!this.key.equals(other.key)) {
            return false;
        }
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
     * Getter for module.
     *
     * @return the module
     */
    public IFragmentModule getModule() {
        return this.module;
    }

}
