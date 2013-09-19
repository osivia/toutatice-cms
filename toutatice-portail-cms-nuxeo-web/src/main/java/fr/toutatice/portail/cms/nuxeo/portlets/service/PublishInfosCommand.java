/**
 * Classe traitant le retour de l'opération Document.FetchPublicationInfos et la transmission au portlet NuxeoPublishInfosPortlet.
 */
package fr.toutatice.portail.cms.nuxeo.portlets.service;

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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Blob;
import org.osivia.portal.core.cms.CMSPublicationInfos;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

public class PublishInfosCommand implements INuxeoCommand {

	protected static Log logger = LogFactory.getLog(PublishInfosCommand.class);

	private final String path;

	public PublishInfosCommand(String path) {
		this.path = path;
	}

	public CMSPublicationInfos execute(Session automationSession) throws Exception {
		CMSPublicationInfos publiInfos = null;

		OperationRequest request = automationSession.newRequest("Document.FetchPublicationInfos");
		request.set("path", this.path);
		Blob binariesInfos = (Blob) request.execute();

		if (binariesInfos != null) {
			publiInfos = new CMSPublicationInfos();
		
			String pubInfosContent = FileUtils.read(binariesInfos.getStream());
			
			JSONArray infosContent = JSONArray.fromObject(pubInfosContent);
			Iterator it = infosContent.iterator();

			while (it.hasNext()) {
				JSONObject infos = (JSONObject) it.next();
				// Modif-RETOUR-begin
				publiInfos.setErrorCodes(adaptList((List<Integer>) infos.get("errorCodes")));
				publiInfos.setDocumentPath(decode(adaptType(String.class, infos.get("documentPath"))));
				publiInfos.setLiveId(adaptType(String.class, infos.get("liveId")));
				publiInfos.setEditableByUser(adaptBoolean(infos.get("editableByUser")));
				// Modif-FILEBROWSER-begin
				publiInfos.setDeletableByUser(adaptBoolean(infos.get("isDeletableByUser")));
				// Modif-FILEBROWSER-end
				publiInfos.setPublished(adaptBoolean(infos.get("published")));
				// Modif-COMMENTS-begin
				publiInfos.setCommentableByUser(adaptBoolean(infos.get("isCommentableByUser")));
				// Modif-COMMENTS-end
				publiInfos.setAnonymouslyReadable(adaptBoolean(infos.get("anonymouslyReadable")));
				// Modif SUBTYPES-begin
				publiInfos.setSubTypes(decodeSubTypes(adaptType(JSONObject.class, infos.get("subTypes"))));
				// Modif SUBTYPES-end
				publiInfos.setPublishSpaceType(adaptType(String.class, infos.get("publishSpaceType")));



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

						// Modif-SPACEID-begin
						/*
                         * les spaceId ne sont appliqués qu'aux ws pour le moment.
                         * CKR (27/08/13) : le spaceId n'est pas obligatoirement renseigné.
                         */
                        if (infos.containsKey("spaceID")) {
                            publiInfos.setSpaceID(this.adaptType(String.class, infos.getString("spaceID")));
                        }
                        if (infos.containsKey("parentSpaceID")) {
                            publiInfos.setParentSpaceID(this.adaptType(String.class, infos.getString("parentSpaceID")));
                        }
                        // Modif-SPACEID-end
					}
				}
			}
			// Modif-RETOUR-end
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

	public String getId() {
		return "PublishInfosCommand" + this.path;
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

	
	private List<Integer> adaptList(List<Integer> list) {
		List<Integer> returnedList = list;
		if(list == null){
			returnedList = new ArrayList<Integer>();
		}
		return returnedList;
	}
	
	private Boolean adaptBoolean(Object value) {

		if (value != null) {
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
