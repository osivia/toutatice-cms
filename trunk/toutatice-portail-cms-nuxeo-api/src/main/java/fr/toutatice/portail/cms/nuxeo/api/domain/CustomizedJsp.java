package fr.toutatice.portail.cms.nuxeo.api.domain;

/**
 * Customized JavaServer page.
 *
 * @author Cédric Krommenhoek
 */
public class CustomizedJsp {

    /** JSP name. */
    private final String name;
    /** Original JSP class loader. */
    private final ClassLoader classLoader;


    /**
     * Constructor.
     *
     * @param name JSP name
     * @param classLoader original JSP class loader
     */
    public CustomizedJsp(String name, ClassLoader classLoader) {
        super();
        this.name = name;
        this.classLoader = classLoader;
    }


    /**
     * Getter for name.
     * 
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Getter for classLoader.
     * 
     * @return the classLoader
     */
    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

}
