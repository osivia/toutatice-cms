/**
 * Classe traitant le retour de l'opération Document.FetchPublicationInfos et la transmission au portlet NuxeoPublishInfosPortlet.
 */
package fr.toutatice.portail.cms.nuxeo.portlets.service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.client.jaxrs.OperationRequest;
import org.nuxeo.ecm.automation.client.jaxrs.Session;
import org.nuxeo.ecm.automation.client.jaxrs.model.Blob;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.core.cms.CMSPublicationInfos;

public class PublishInfosCommand implements INuxeoCommand {

	protected static Log logger = LogFactory.getLog(PublishInfosCommand.class);

	private final String path;

	public PublishInfosCommand(String path) {
		this.path = path;
	}

	public CMSPublicationInfos execute(Session automationSession) throws Exception {
		CMSPublicationInfos publiInfos = null;

		OperationRequest request = automationSession.newRequest("Document.FetchPublicationInfos");
		request.set("path", path);
		Blob binariesInfos = (Blob) request.execute();

		if (binariesInfos != null) {
			publiInfos = new CMSPublicationInfos();
			String content = FileUtils.read(binariesInfos.getStream());
			JSONArray row = JSONArray.fromObject(content);
			Iterator it = row.iterator();
			while (it.hasNext()) {
				JSONObject obj = (JSONObject) it.next();
				publiInfos.setErrorCodes((List<Integer>) obj.get("errorCodes"));
				publiInfos.setDocumentPath(decode((String) obj.get("documentPath")));
				publiInfos.setLiveId((String) obj.get("liveId"));
				
				// TODO : .proxy a supprimer dans l'opération
				String publishSpacePath = decode((String) obj.get("publishSpacePath"));
				if( publishSpacePath != null)
					publishSpacePath = DocumentPublishSpaceNavigationCommand.computeNavPath(publishSpacePath);
				
				publiInfos.setPublishSpacePath(publishSpacePath);
				
				publiInfos.setPublishSpaceDisplayName(decode((String) obj.get("publishSpaceDisplayName")));
				publiInfos.setPublishSpaceInContextualization((Boolean) obj.get("publishSpaceInContextualization"));
				publiInfos.setPublishSpaceType((String) obj.get("publishSpaceType"));
				publiInfos.setWorkspacePath(decode((String) obj.get("workspacePath")));
				publiInfos.setWorkspaceDisplayName(decode((String) obj.get("workspaceDisplayName")));
				publiInfos.setWorkspaceInContextualization((Boolean) obj.get("workspaceInContextualization"));

				publiInfos.setEditableByUser((Boolean) obj.get("editableByUser"));
				publiInfos.setPublished((Boolean) obj.get("published"));
				publiInfos.setAnonymouslyReadable((Boolean) obj.get("anonymouslyReadable"));
			}

		}
		return publiInfos;
	}

	public String getId() {
		return "PublishInfosCommand" + path;
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

}
