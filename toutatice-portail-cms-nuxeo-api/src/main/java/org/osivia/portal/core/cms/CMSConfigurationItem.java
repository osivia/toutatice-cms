package org.osivia.portal.core.cms;



/**
 * CMS configuration item.
 *
 * @author Cédric Krommenhoek
 */
public class CMSConfigurationItem {

    /** Configuration name. */
    private final String name;
    /** Configuration code. */
    private final String code;

    /** Configuration additional code. */
    private String additionalCode;


    /**
     * Constructor.
     *
     * @param name configuration name
     * @param code configuration code
     */
    public CMSConfigurationItem(String name, String code) {
        super();
        this.name = name;
        this.code = code;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "CMSConfigurationItem [name=" + this.name + ", code=" + this.code + "]";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.code == null) ? 0 : this.code.hashCode());
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
        CMSConfigurationItem other = (CMSConfigurationItem) obj;
        if (this.code == null) {
            if (other.code != null)
                return false;
        } else if (!this.code.equals(other.code))
            return false;
        return true;
    }


    /**
     * Getter for code.
     *
     * @return the code
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Getter for additionalCode.
     *
     * @return the additionalCode
     */
    public String getAdditionalCode() {
        return this.additionalCode;
    }

    /**
     * Setter for additionalCode.
     *
     * @param additionalCode the additionalCode to set
     */
    public void setAdditionalCode(String additionalCode) {
        this.additionalCode = additionalCode;
    }

    /**
     * Getter for name.
     *
     * @return the name
     */
    public String getName() {
        return this.name;
    }

}
