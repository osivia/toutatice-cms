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

import java.util.List;

import javax.portlet.PortletRequest;

import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.osivia.portal.api.contribution.IContributionService.EditionState;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.cms.CMSServiceCtx;


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
     * @param document studied document
     * @return true if document is a folder
     */
    public static boolean isFolder(Document document) {
        boolean isFolder = false;

        PropertyList facetsProp = document.getFacets();
        if ((facetsProp != null) && !facetsProp.isEmpty()) {
            List<Object> facets = facetsProp.list();
            isFolder = facets.contains(DocumentConstants.FOLDERISH_FACET);
        }

        return isFolder;
    }


    /**
     * Checks if current document is a remote proxy.
     *
     * @param cmsCtx CMS context
     * @param pubInfos CMS publication informations
     * @return true if current document is a remote proxy
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
     * @param path
     * @return navigation path.
     */
    public static String  computeNavPath(String path){
        String result = path;
        if( path.endsWith(".proxy")) {
            result = result.substring(0, result.length() - 6);
        }
        return result;
    }
}
