/**
 * Classe traitant le retour de l'opération Document.FetchPublicationInfos et la transmission au portlet NuxeoPublishInfosPortlet.
 */
package fr.toutatice.portail.cms.nuxeo.portlets.service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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
import org.nuxeo.ecm.automation.client.jaxrs.OperationRequest;
import org.nuxeo.ecm.automation.client.jaxrs.Session;
import org.nuxeo.ecm.automation.client.jaxrs.model.Blob;
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

			String content = FileUtils.read(binariesInfos.getStream());

			JSONArray row = JSONArray.fromObject(content);
			Iterator it = row.iterator();
			while (it.hasNext()) {
				JSONObject obj = (JSONObject) it.next();
				publiInfos.setErrorCodes((List<Integer>) obj.get("errorCodes"));
				publiInfos.setDocumentPath( this.decode((String) obj.get("documentPath")));
				publiInfos.setLiveId((String) obj.get("liveId"));
				publiInfos.setEditableByUser(this.convertBoolean(obj.get("editableByUser")));
				publiInfos.setPublished(this.convertBoolean(obj.get("published")));
				publiInfos.setAnonymouslyReadable(this.convertBoolean(obj.get("anonymouslyReadable")));

				publiInfos.setSubTypes(this.decodeSubTypes((JSONObject) obj.get("subTypes")));

				publiInfos.setPublishSpaceType((String) obj.get("publishSpaceType"));

				String publishSpacePath = this.decode((String) obj.get("publishSpacePath"));
				if (StringUtils.isNotEmpty(publishSpacePath)) {
					publiInfos.setPublishSpacePath(publishSpacePath);
					publiInfos.setPublishSpaceDisplayName(this.decode((String) obj.get("publishSpaceDisplayName")));
					publiInfos.setLiveSpace(false);
				} else {
					String workspacePath = this.decode((String) obj.get("workspacePath"));
					if (StringUtils.isNotEmpty(workspacePath)) {
						publiInfos.setPublishSpacePath(workspacePath);
						publiInfos.setPublishSpaceDisplayName(this.decode((String) obj.get("workspaceDisplayName")));
						publiInfos.setLiveSpace(true);
					}
				}
			}

		}
		return publiInfos;
	}

    /**
     * Décode en UTF-8 les labels de la Map des sous-types permis.
     *
     * @param map
     * @return
     * @throws UnsupportedEncodingException
     */
    private Map<String, String> decodeSubTypes(Map<String, String> subTypes) throws UnsupportedEncodingException {
        if (subTypes != null) {
            Map<String, String> decodedSubTypes = new HashMap<String, String>();
            for (Entry<String, String> subType : subTypes.entrySet()) {
                String decodedLabel = this.decode(subType.getValue());
                decodedSubTypes.put(subType.getKey(), decodedLabel);
            }
            return decodedSubTypes;
        } else {
            return null;
        }
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

	private Boolean convertBoolean(Object value) {
		if (value != null) {
			return (Boolean) value;
		} else {
			return Boolean.FALSE;
		}
	}

}
