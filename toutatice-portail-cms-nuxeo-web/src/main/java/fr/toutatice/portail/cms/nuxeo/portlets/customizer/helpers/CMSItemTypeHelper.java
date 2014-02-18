package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSItemType;

/**
 * CMS item type helper.
 * This helper establish link between Nuxeo and CMS item types.
 * 
 * @author Cédric Krommenhoek
 * @see CMSItemType
 */
public enum CMSItemTypeHelper {

    /** Portal site. */
    PORTAL_SITE("PortalSite", CMSItemType.SPACE),
    /** Portal page. */
    PORTAL_PAGE("PortalPage", CMSItemType.PAGE),
    /** Workspace. */
    WORKSPACE("Workspace", CMSItemType.WORKSPACE),
    /** Folder. */
    FOLDER("Folder", CMSItemType.FOLDER),
    /** Note. */
    NOTE("Note", CMSItemType.DOCUMENT);


    /** Nuxeo type. */
    private final String nuxeoType;
    /** CMS item type. */
    private final CMSItemType cmsItemType;


    /**
     * Constructor.
     * 
     * @param nuxeoType Nuxeo type
     * @param cmsItemType CMS item type
     */
    private CMSItemTypeHelper(String nuxeoType, CMSItemType cmsItemType) {
        this.nuxeoType = nuxeoType;
        this.cmsItemType = cmsItemType;
    }


    /**
     * Get CMS item type from Nuxeo document.
     * 
     * @param document Nuxeo document
     * @return CMS item type
     */
    public static final CMSItemType getCMSItemType(Document document) {
        CMSItemType result = null;

        // Search by Nuxeo document type
        String nuxeoType = document.getType();
        if (nuxeoType != null) {
            CMSItemTypeHelper[] values = CMSItemTypeHelper.values();
            for (CMSItemTypeHelper value : values) {
                if (nuxeoType.equals(value.nuxeoType)) {
                    result = value.cmsItemType;
                    break;
                }
            }
        }

        if (result == null) {
            // Search by Nuxeo document metadatas
            boolean folderish = document.getFacets().list().contains("Folderish");
            if (folderish) {
                result = CMSItemType.FOLDER;
            } else {
                result = CMSItemType.DOCUMENT;
            }
        }

        return result;
    }


    /**
     * Get CMS item type from CMS item.
     * 
     * @param cmsItem CMS item
     * @return CMS item type
     */
    public static final CMSItemType getCMSItemType(CMSItem cmsItem) {
        Object nativeItem = cmsItem.getNativeItem();
        if ((nativeItem != null) && (nativeItem instanceof Document)) {
            Document document = (Document) nativeItem;
            return getCMSItemType(document);
        }
        return null;
    }



    /**
     * Getter for nuxeoType.
     * 
     * @return the nuxeoType
     */
    public String getNuxeoType() {
        return this.nuxeoType;
    }

    /**
     * Getter for cmsItemType.
     * 
     * @return the cmsItemType
     */
    public CMSItemType getCmsItemType() {
        return this.cmsItemType;
    }

}
