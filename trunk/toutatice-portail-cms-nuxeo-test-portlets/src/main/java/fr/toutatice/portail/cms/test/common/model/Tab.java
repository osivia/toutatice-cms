package fr.toutatice.portail.cms.test.common.model;

import org.apache.commons.lang.StringUtils;

/**
 * Test portlet tabs enumeration.
 *
 * @author Cédric Krommenhoek
 */
public enum Tab {

    /** Tags. */
    TAGS("tags"),
    /** Attributes storages. */
    ATTRIBUTES_STORAGE("attributes-storage");


    /** Default tab. */
    public static final Tab DEFAULT = TAGS;


    /** Tab identifier. */
    private final String id;
    /** Internationalization key. */
    private final String key;


    /**
     * Constructor.
     *
     * @param id tab identifier
     */
    private Tab(String id) {
        this.id = id;
        this.key = StringUtils.upperCase(StringUtils.replace(id, "-", "_"));
    }


    /**
     * Get tab from identifier.
     *
     * @param id tab identifier
     * @return tab
     */
    public static Tab fromId(String id) {
        Tab result = DEFAULT;
        for (Tab tab : Tab.values()) {
            if (tab.id.equals(id)) {
                result = tab;
                break;
            }
        }
        return result;
    }


    /**
     * Getter for id.
     *
     * @return the id
     */
    public String getId() {
        return this.id;
    }

    /**
     * Getter for key.
     *
     * @return the key
     */
    public String getKey() {
        return this.key;
    }

}
