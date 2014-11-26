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
 *
 *
 *    
 */
package fr.toutatice.portail.cms.nuxeo.portlets.service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Blob;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSItemType;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.ICMSService;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;

/**
 * List CMS sub-items command.
 * 
 * @author Cédric Krommenhoek
 * @see INuxeoCommand
 */
public class ListCMSSubitemsCommand implements INuxeoCommand {

    /** Current parent identifier. */
    private final String parentId;
    /** Live content indicator. */
    private final boolean liveContent;
    /** CMS Context */
    private CMSServiceCtx cmsContext;


    /**
     * Constructor.
     * 
     * @param parentId current parent identifier
     * @param liveContent live content indicator
     */
    public ListCMSSubitemsCommand(CMSServiceCtx cmsContext, String parentId, boolean liveContent) {
        super();
        this.cmsContext = cmsContext;
        this.parentId = parentId;
        this.liveContent = liveContent;
    }


    /**
     * {@inheritDoc}
     */
    public List<CMSItem> execute(Session nuxeoSession) throws Exception {
        // Fetch live tree with publishing infos
        OperationRequest request = nuxeoSession.newRequest("Fetch.PublishingStatusChildren");
        request.set("documentId", this.parentId);
        request.set("liveStatus", this.liveContent);
        request.setHeader(Constants.HEADER_NX_SCHEMAS, this.getSchemas());
        Blob binariesPublishingInfos = (Blob) request.execute();

        String publishingInfos = IOUtils.toString(binariesPublishingInfos.getStream(), "UTF-8");
        return convertPublishingInfos(JSONArray.fromObject(publishingInfos));
    }
    
    /**
     * Convert the JsonArray returned by command to list of exposed CMSItems.
     * @param publishingInfos
     * @return
     * @throws CMSException
     * @throws UnsupportedEncodingException 
     */
    private List<CMSItem> convertPublishingInfos(JSONArray publishingInfos) throws CMSException, UnsupportedEncodingException{
        List<CMSItem> cmsItems = new ArrayList<CMSItem>(publishingInfos.size());
        Iterator<?> documentsIterator = publishingInfos.iterator();
        while (documentsIterator.hasNext()) {
            JSONObject documentWithPublishingStatus = (JSONObject) documentsIterator.next();

            String documentId = (String) documentWithPublishingStatus.get("docId");
            String documentPath = URLDecoder.decode((String) documentWithPublishingStatus.get("docPath"), "UTF-8");
            String documentType = (String) documentWithPublishingStatus.get("docType");

            String documentTitle = URLDecoder.decode((String) documentWithPublishingStatus.get("docTitle"), "UTF-8");
            PropertyMap nxProperties = new PropertyMap();
            nxProperties.set("dc:title", documentTitle);

            Document poorDocument = new Document(documentId, documentType, null, null, documentPath, null, null, null, null, null, nxProperties, null);
            
            CMSService cmsService = (CMSService) NuxeoController.getCMSService();
            CMSItem cmsItem = cmsService.createItem(this.cmsContext, poorDocument.getPath(), poorDocument.getTitle(), poorDocument);

            boolean isPublished = documentWithPublishingStatus.getBoolean("isPublished");
            boolean isLiveModifiedFromProxy = documentWithPublishingStatus.getBoolean("isLiveModifiedFromProxy");
            cmsItem.setPublished(Boolean.valueOf(isPublished));
            cmsItem.setBeingModified(Boolean.valueOf(isLiveModifiedFromProxy));

            boolean isFolderish = documentWithPublishingStatus.getBoolean("isFolderish");
            CMSItemType cmsItemType = new CMSItemType(documentType, isFolderish, false, false, false, false, null, null);
            cmsItem.setType(cmsItemType);
            
            cmsItems.add(cmsItem);
        }
        return cmsItems;
    }


    /**
     * {@inheritDoc}
     */
    public String getId() {
        StringBuilder id = new StringBuilder();
        id.append(this.getClass().getCanonicalName());
        id.append("[");
        id.append(this.parentId);
        id.append(";");
        id.append(this.liveContent);
        id.append("]");
        return id.toString();
    }


    /**
     * Utility method used to return schemas.
     * 
     * @return schemas
     */
    private String getSchemas() {
        return "dublincore, common, toutatice";
    }

}
