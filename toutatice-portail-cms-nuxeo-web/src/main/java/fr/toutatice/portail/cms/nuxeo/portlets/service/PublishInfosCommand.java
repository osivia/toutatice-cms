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
 *
*/

package fr.toutatice.portail.cms.nuxeo.portlets.service;

/**
 * Classe traitant le retour de l'opération Document.FetchPublicationInfos et
 * la transmission au portlet NuxeoPublishInfosPortlet.
 */

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Blob;
import org.nuxeo.ecm.automation.client.model.FileBlob;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.web.IWebIdService;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoCompatibility;

public class PublishInfosCommand implements INuxeoCommand {

	protected static Log logger = LogFactory.getLog(PublishInfosCommand.class);
	
	private final String path;
	private String navigationPath;
	private String displayLiveVersion;
	private final String parentId;
	private final String parentPath;

	public PublishInfosCommand(String navigationPath, String parentId, String parentPath, String path, String displayLiveVersion) {
		this.path = path;
		this.navigationPath = navigationPath;
		this.displayLiveVersion = displayLiveVersion;
		this.parentId = parentId;
		this.parentPath = parentPath;
	}

	@Override
	public CMSPublicationInfos execute(Session automationSession) throws Exception {
		CMSPublicationInfos publiInfos = null;

		OperationRequest request = automationSession.newRequest("Document.FetchPublicationInfos");
		
        if (path.startsWith(IWebIdService.PREFIX_WEBID_FETCH_PUB_INFO)) {
            request.set("webid", path.replaceAll(IWebIdService.PREFIX_WEBID_FETCH_PUB_INFO, StringUtils.EMPTY));
            
            if(StringUtils.isNotBlank(navigationPath)){
                request.set("navigationPath", navigationPath);
            }
            
            if (NuxeoCompatibility.isVersionGreaterOrEqualsThan(NuxeoCompatibility.VERSION_61)) {

                if (StringUtils.isNotBlank(parentId)) {
                    request.set("parentId", parentId);
                } else if (StringUtils.isNotBlank(parentPath)) {
                    request.set("parentPath", parentPath);
                }

            }
            
        }
        else {
            request.set("path", path);
        }
        
        if(StringUtils.isNotBlank(displayLiveVersion)){
            boolean draft = "1".equals(displayLiveVersion);
            request.set("displayLiveVersion", draft);         
        }
        
		
        Blob binariesInfos = (Blob) request.execute();

        if (binariesInfos != null) {
            publiInfos = new CMSPublicationInfos();

            String pubInfosContent = IOUtils.toString(binariesInfos.getStream(), "UTF-8");

            JSONArray infosContent = JSONArray.fromObject(pubInfosContent);
            Iterator it = infosContent.iterator();
            while (it.hasNext()) {
                JSONObject infos = (JSONObject) it.next();

                if (infos.containsKey("hasManyProxies")) {
                    Boolean hasManyProxies = adaptBoolean(infos.get("hasManyProxies"));
                    publiInfos.setHasManyPublications(Boolean.valueOf(hasManyProxies));
                    publiInfos.setDocumentPath(decode(adaptType(String.class, infos.get("documentPath"))));
                    // Informations used in some navigation cases
                    publiInfos.setLiveId(adaptType(String.class, infos.get("liveId")));
                    publiInfos.setSubTypes(new HashMap<String, String>(0));
                }

                if (!publiInfos.hasManyPublications()) {

                    publiInfos.setErrorCodes(adaptList((JSONArray) infos.get("errorCodes")));

                    publiInfos.setDocumentPath(decode(adaptType(String.class, infos.get("documentPath"))));
                    publiInfos.setLiveId(adaptType(String.class, infos.get("liveId")));

                    publiInfos.setEditableByUser(adaptBoolean(infos.get("editableByUser")));
                    publiInfos.setManageableByUser(adaptBoolean(infos.get("manageableByUser")));
                    publiInfos.setRemotePublishable(adaptBoolean(infos.get("isRemotePublishable")));
                    publiInfos.setDeletableByUser(adaptBoolean(infos.get("isDeletableByUser")));
                    publiInfos.setUserCanValidate(adaptBoolean(infos.get("canUserValidate")));
                    publiInfos.setCommentableByUser(adaptBoolean(infos.get("isCommentableByUser")));
                    publiInfos.setAnonymouslyReadable(adaptBoolean(infos.get("anonymouslyReadable")));

                    publiInfos.setPublished(adaptBoolean(infos.get("published")));
                    publiInfos.setBeingModified(adaptBoolean(infos.get("isLiveModifiedFromProxy")));

                    publiInfos.setSubTypes(decodeSubTypes(adaptType(JSONObject.class, infos.get("subTypes"))));
                    publiInfos.setPublishSpaceType(adaptType(String.class, infos.get("publishSpaceType")));

                    if (infos.containsKey("spaceID")) {
                        publiInfos.setSpaceID(this.adaptType(String.class, infos.getString("spaceID")));
                    }
                    if (infos.containsKey("parentSpaceID")) {
                        publiInfos.setParentSpaceID(this.adaptType(String.class, infos.getString("parentSpaceID")));
                    }

                    String publishSpacePath = decode(adaptType(String.class, infos.get("publishSpacePath")));
                    if (StringUtils.isNotEmpty(publishSpacePath)) {
                        publiInfos.setPublishSpacePath(publishSpacePath);
                        publiInfos.setPublishSpaceDisplayName(decode(adaptType(String.class, infos.get("publishSpaceDisplayName"))));
                        publiInfos.setLiveSpace(false);
                    } else {
                        String workspacePath = decode(adaptType(String.class, infos.get("workspacePath")));
                        if (StringUtils.isNotEmpty(workspacePath)) {
                            publiInfos.setPublishSpacePath(workspacePath);
                            publiInfos.setPublishSpaceDisplayName(decode(adaptType(String.class, infos.get("workspaceDisplayName"))));
                            publiInfos.setLiveSpace(true);
                        }
                    }

                }

            }
            // suppression des files
            if( binariesInfos instanceof FileBlob){
                ((FileBlob) binariesInfos).getFile().delete();
            }			

		}
		return publiInfos;
	}

	/**
	 * Décode en UTF-8 les labels de la Map des sous-types permis.
	 * @param map
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	private Map<String, String> decodeSubTypes(Map<String, String> subTypes) throws UnsupportedEncodingException {
		Map<String, String> decodedSubTypes = new HashMap<String, String>();
		for(Entry<String, String> subType : subTypes.entrySet()){
			String decodedLabel = decode(subType.getValue());
			decodedSubTypes.put(subType.getKey(), decodedLabel);
		}
		return decodedSubTypes;
	}

    @Override
    public String getId() {
        StringBuffer id = new StringBuffer();
        id.append("PublishInfosCommand/").append(StringUtils.removeEnd(this.path, ".proxy"));

        if (this.path.startsWith(IWebIdService.PREFIX_WEBID_FETCH_PUB_INFO)) {
            id.append(": ").append(this.displayLiveVersion).append("/").append(this.navigationPath ).append("/").append(this.parentId)
                .append("/").append(this.parentPath);
        } 
        
        return id.toString();
    }

	/**
	 * Décode une chaîne de caractères en UTF-8
	 * 
	 * @param value
	 *            chaîne de caractères à décoder
	 * @return la chaîne de caractères décodée
	 * @throws UnsupportedEncodingException
	 */
	private String decode(String value) throws UnsupportedEncodingException {
		if (value != null) {
			return URLDecoder.decode(value, "UTF-8");
		}
		return value;
	}
	
	private List<Integer> adaptList(JSONArray list) {
		List<Integer> returnedList = new ArrayList<Integer>();
		List<Integer> jsonList = (List<Integer>) JSONArray.toCollection(list);
		if(jsonList != null){
			returnedList.addAll(jsonList);
		}
		return returnedList;
	}
	
	private Boolean adaptBoolean(Object value) {
		if (value != null) {
            if (value instanceof String) {
                return Boolean.valueOf((String) value);
            }
			return (Boolean) value;
		} else {
			return Boolean.FALSE;
		}
	}
	
	private <T> T adaptType(Class<T> clazz, Object object) throws InstantiationException, IllegalAccessException{
		T returnedObject = (T) object;
		if(object == null){
			returnedObject = clazz.newInstance();
		}
		return returnedObject;
	}

}
