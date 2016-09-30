package fr.toutatice.portail.cms.nuxeo.portlets.publish;

/**
 * Menu options java-bean.
 *
 * @author Cédric Krommenhoek
 */
public class MenuOptions {

    /** Base path. */
    private final String basePath;
    /** Current path. */
    private final String currentPath;
    /** Auxiliary path. */
    private final String auxiliaryPath;
    /** Open levels. */
    private final int openLevels;
    /** Start level. */
    private final int startLevel;
    /** Max levels. */
    private final int maxLevels;

    /** Lazy loading indicator. */
    private boolean lazy;


    /**
     * Constructor.
     *
     * @param basePath base path
     * @param currentPath current path
     * @param auxiliaryPath auxiliary path
     * @param openLevels open levels
     * @param startLevel start level
     * @param maxLevels max levels
     * @param lazy lazy loading indicator
     */
    public MenuOptions(String basePath, String currentPath, String auxiliaryPath, int openLevels, int startLevel, int maxLevels) {
        super();
        this.basePath = basePath;
        this.currentPath = currentPath;
        this.auxiliaryPath = auxiliaryPath;
        this.openLevels = openLevels;
        this.startLevel = startLevel;
        this.maxLevels = maxLevels;
    }


    /**
     * Getter for basePath.
     *
     * @return the basePath
     */
    public String getBasePath() {
        return this.basePath;
    }

    /**
     * Getter for currentPath.
     *
     * @return the currentPath
     */
    public String getCurrentPath() {
        return this.currentPath;
    }

    /**
     * Getter for auxiliaryPath.
     *
     * @return the auxiliaryPath
     */
    public String getAuxiliaryPath() {
        return this.auxiliaryPath;
    }

    /**
     * Getter for openLevels.
     *
     * @return the openLevels
     */
    public int getOpenLevels() {
        return this.openLevels;
    }

    /**
     * Getter for startLevel.
     *
     * @return the startLevel
     */
    public int getStartLevel() {
        return this.startLevel;
    }

    /**
     * Getter for maxLevels.
     *
     * @return the maxLevels
     */
    public int getMaxLevels() {
        return this.maxLevels;
    }

    /**
     * Getter for lazy.
     * 
     * @return the lazy
     */
    public boolean isLazy() {
        return this.lazy;
    }

    /**
     * Setter for lazy.
     * 
     * @param lazy the lazy to set
     */
    public void setLazy(boolean lazy) {
        this.lazy = lazy;
    }

}
