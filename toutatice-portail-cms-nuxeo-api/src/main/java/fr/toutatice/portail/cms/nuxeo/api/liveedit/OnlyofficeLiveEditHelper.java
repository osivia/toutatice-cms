package fr.toutatice.portail.cms.nuxeo.api.liveedit;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osivia.portal.api.Constants;
import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.internationalization.Bundle;
import org.osivia.portal.core.constants.InternalConstants;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;

public class OnlyofficeLiveEditHelper {

    /** ONLYOFFICE_PLUGIN_NAME */
    public static final String ONLYOFFICE_PLUGIN_NAME = "onlyoffice.plugin";
    /** ONLYOFFICE_PORTLET_INSTANCE */
    public static final String ONLYOFFICE_PORTLET_INSTANCE = "osivia-services-onlyoffice-portletInstance";

    public static String getStartOnlyofficePortlerUrl(Bundle bundle, String documentPath, NuxeoController nuxeoController, Boolean withLock) throws PortalException {

        Map<String, String> windowProperties = new HashMap<>();
        windowProperties.put(Constants.WINDOW_PROP_URI, documentPath);
        windowProperties.put("osivia.hideTitle", "1");
        windowProperties.put("osivia.onlyoffice.withLock",withLock.toString());
        windowProperties.put(InternalConstants.PROP_WINDOW_TITLE, bundle.getString("ONLYOFFICE_EDIT"));

        return nuxeoController.getPortalUrlFactory().getStartPortletUrl(nuxeoController.getPortalCtx(), ONLYOFFICE_PORTLET_INSTANCE, windowProperties);
    }

    public static OnlyofficeFileType getFileType(String mimeType) {
        if (documentMimetype.contains(mimeType)) {
            return OnlyofficeFileType.word;
        }

        if (spreadsheetMimetype.contains(mimeType)) {
            return OnlyofficeFileType.cell;
        }

        if (presentationMimetype.contains(mimeType)) {
            return OnlyofficeFileType.slide;
        }
        
        return OnlyofficeFileType.word;
    }

    public static boolean isMimeTypeSupported(String mimeType) {
        if (documentMimetype.contains(mimeType)) {
            return true;
        }

        if (spreadsheetMimetype.contains(mimeType)) {
            return true;
        }

        if (presentationMimetype.contains(mimeType)) {
            return true;
        }
        return false;
    }

    private static List<String> documentMimetype = Arrays.asList("text/rtf", "application/rtf", "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/vnd.sun.xml.writer",
            "application/vnd.sun.xml.writer.template", "application/vnd.oasis.opendocument.text", "application/vnd.oasis.opendocument.text-template",
            "application/wordperfect", "application/pdf");

    private static List<String> spreadsheetMimetype = Arrays.asList("text/csv", "text/tsv", "application/vnd.ms-excel",
            "application/vnd.ms-excel.sheet.macroEnabled.12", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.sun.xml.calc", "application/vnd.sun.xml.calc.template", "application/vnd.oasis.opendocument.spreadsheet",
            "application/vnd.oasis.opendocument.spreadsheet-template");

    private static List<String> presentationMimetype = Arrays.asList("application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation", "application/vnd.sun.xml.impress",
            "application/vnd.sun.xml.impress.template", "application/vnd.oasis.opendocument.presentation",
            "application/vnd.oasis.opendocument.presentation-template");

    public enum OnlyofficeFileType {
        word, cell, slide
    }
}
