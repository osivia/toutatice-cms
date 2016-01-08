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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyList;


/**
 * Utility class for documents, generally issued from CmsServiceContext.
 * 
 * @author David Chevrier.
 *
 */
public class ContextDocumentsHelper {
    
    /**
     * Utility class.
     */
    private ContextDocumentsHelper(){};
    
    /**
     * @param document document (possibly in context).
     * @return true if document is a folder
     */
    public static boolean isFolder(Document document) {
        return getFacets(document).contains(DocumentConstants.FOLDERISH_FACET);
    }
    
    /**
     * @param document
     * @return true if document is a live in a publish space
     */
    public static boolean isLocalPublishLive(Document document){
        return getFacets(document).contains(DocumentConstants.LOCAL_PUBLISH_LIVE_FACET);
    }
    
    /**
     * @param document (possibly in context).
     * @return true if document is a remote proxy.
     */
    public static boolean isRemoteProxy(Document document){
        return getFacets(document).contains(DocumentConstants.REMOTE_PROXY_FACET);
    }
    
    /**
     * @param document
     * @return list of document's facets.
     */
    private static List<Object> getFacets(Document document){
        List<Object> facets = new ArrayList<Object>(0);
        
        PropertyList facetsProp = document.getFacets();
        if(facetsProp != null && facetsProp.size() > 0){
            facets.addAll(facetsProp.list());
        }
        
        return facets;
    }
    
}
