package fr.toutatice.portail.cms.nuxeo.api;

import org.apache.commons.lang.BooleanUtils;


public class NuxeoCompatibility {

    private static boolean versionChecked = false;
    /** Indicator of ElasticSearch status checked. */
    private static boolean esChecked = false;

    private static int currentVersion = 99;
    /** Indicator of ElasticSearch activation. */
    private static boolean isESActivated = false;

    public static int VERSION_58 = 58;

    public static int VERSION_60 = 60;
    
    /** Linked to opentoutatice 3.1. */
    public static int VERSION_61 = 61;
    /** Linked to opentoutatice 3.2. */
    public static int VERSION_62 = 62;

    public static boolean isVersionGreaterOrEqualsThan(int versionNumber) {
        if (versionChecked == false) {
            versionChecked = true;
            String nuxeoVersion = System.getProperty("nuxeo.version");
            if (nuxeoVersion != null) {
                currentVersion = Integer.parseInt(nuxeoVersion);
            }

        }

        if (currentVersion >= versionNumber) {
            return true;

        }

        return false;

    }

    /**
     * @return true if ElasticSearch can be used by Portal
     *         (since Nuxeo 6.0).
     */
    public static boolean canUseES() {
        if (!esChecked) {
            boolean isNxVersionOk = isVersionGreaterOrEqualsThan(VERSION_60);
            if (isNxVersionOk) {
                isESActivated = BooleanUtils.toBoolean(System.getProperty("nuxeo.es.activated"));
            }
            esChecked = true;
        }
        return isESActivated;
    }
}
