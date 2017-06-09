/*
 * (C) Copyright 2014 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package fr.toutatice.portail.cms.nuxeo.portlets.document.helpers;

import javax.portlet.PortletRequest;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.contribution.IContributionService.EditionState;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.cms.CMSServiceCtx;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;


/**
 * Utility class to get informations on Document object
 * (Nuxeo automation client).
 *
 * @author David Chevrier.
 *
 */
public class DocumentHelper {

    /**
     * Private constructor.
     */
    private DocumentHelper(){

    }
    
    /**
     * @param document
     * @return true if document is a leaf.
     */
    public static boolean isLeaf(Document document){
        return !isFolder(document);
    }

    /**
     * @param document studied document
     * @return true if document is a folder.
     */
    public static boolean isFolder(Document document) {
        return ContextDocumentsHelper.isFolder(document);
    }
    
    /**
     * @param document
     * @return true if document is a live in a publish space.
     */
    public static boolean isLocalPublishLive(Document document){
        return ContextDocumentsHelper.isLocalPublishLive(document);
    }
    
    /**
     * @param document
     * @return true if document is a draft in live space.
     */
    public static boolean isDraft(Document document){
        return ContextDocumentsHelper.isDraft(document);
    }

    /**
     * @param document
     * @return true if document has a draft in live space.
     */
    public static boolean hasDraft(Document document){
        return ContextDocumentsHelper.hasDraft(document);
    }

    /**
     * Checks if current document is a remote proxy.
     *
     * @param cmsCtx CMS context
     * @param pubInfos CMS publication informations
     * @return true if current document is a remote proxy.
     */
    public static boolean isRemoteProxy(CMSServiceCtx cmsCtx, CMSPublicationInfos pubInfos) {
        if (cmsCtx.getDoc() == null) {
            return false;
        }

        if (pubInfos.isPublished() && !isInLiveMode(cmsCtx, pubInfos)) {
            // Document
            Document document = (Document) cmsCtx.getDoc();
            // Path
            String path = document.getPath();

            // Pour un proxy distant, le documentPath du pubInfos est égal au docPath
            if (pubInfos.getDocumentPath().equals(path)) {
                return true;
            }
        }

        return false;
    }

    public static String getParentWebId(CMSServiceCtx cmsCtx, CMSPublicationInfos pubInfos) throws CMSException {
        String parent = null;

        // Get Navigation item: it is parent
        CMSItem navItem = NuxeoController.getCMSService().getPortalNavigationItem(cmsCtx, pubInfos.getPublishSpacePath(), pubInfos.getDocumentPath());
        if (navItem != null) {
            // Get webId of parent
            parent = navItem.getWebId();
        }

        return parent;
    }

    /**
     * Controls if live mode is associated with the current document.
     *
     * @param cmsCtx CMS context
     * @param pubInfos CMS publication informations
     * @return true if in live mode
     */
    public static boolean isInLiveMode(CMSServiceCtx cmsCtx, CMSPublicationInfos pubInfos) {
        boolean liveMode = false;

        // Portlet request
        PortletRequest request = cmsCtx.getRequest();
        if (request != null) {
            // Current edition state
            EditionState curState = (EditionState) request.getAttribute("osivia.editionState");
            if ((curState != null) && curState.getContributionMode().equals(EditionState.CONTRIBUTION_MODE_EDITION)) {
                if (curState.getDocPath().equals(pubInfos.getDocumentPath())) {
                    liveMode = true;
                }
            }
        }
        return liveMode;
    }
    
    /**
     * 
     * @param document
     * @return name (last segment path) of a document.
     */
    public static String getName(Document document){
        String path = document.getPath();
        return StringUtils.substringAfterLast(path, "/");
    }
    
    
    /**
     * @param document
     * @return WebId of given document.
     */
    public static String getWebId(Document document){
        return (String) ContextDocumentsHelper.getPropertyValue(document, DocumentConstants.WEBID);
    }
    
    /**
     * Getter for document's webid
     * for corresponding given draft.
     * 
     * @param draft
     * @return WebId of given draft.
     */
    public static String getCheckinedWebIdFromDraft(Document draft){
        if(DocumentHelper.isDraft(draft)){
            return (String) ContextDocumentsHelper.getPropertyValue(draft, DocumentConstants.CHECKINED_DOC_ID);
        }
        return StringUtils.EMPTY;
    }
    
    /**
     * Getter for path of Draft of a document
     * (if it has one).
     * 
     * @param document
     * @return draft path of document if it has a draft
     */
    public static String getDraftPath(Document document){
        if(hasDraft(document)){
            String draftPath = (String) ContextDocumentsHelper.getPropertyValue(document, DocumentConstants.DRAFT_PATH);
            return StringUtils.isNotBlank(draftPath) ? draftPath : StringUtils.EMPTY;
        }
        return StringUtils.EMPTY;
    }

    /**
     * @param path
     * @return navigation path.
     */
    public static String  computeNavPath(String path){
        String result = path;
        if( path.endsWith(DocumentConstants.LOCAL_PROXIES_SUFFIX)) {
            result = result.substring(0, result.length() - 6);
        }
        return result;
    }
}
