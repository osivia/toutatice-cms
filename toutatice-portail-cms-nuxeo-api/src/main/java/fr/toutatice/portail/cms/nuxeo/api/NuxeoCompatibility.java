package fr.toutatice.portail.cms.nuxeo.api;

import org.apache.commons.lang.StringUtils;


public class NuxeoCompatibility {

    private static boolean versionChecked = false;
    private static int currentVersion = 99;

    public static int VERSION_60 = 60;

    public static boolean isVersionGreaterOrEqualsThan(int versionNumber) {
        if (versionChecked == false) {
//            versionChecked = true;
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

}
