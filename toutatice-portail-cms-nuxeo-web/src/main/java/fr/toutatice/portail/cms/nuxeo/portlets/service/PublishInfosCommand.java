/**
 * Classe traitant le retour de l'opération Document.FetchPublicationInfos et la transmission au portlet NuxeoPublishInfosPortlet.
 */
package fr.toutatice.portail.cms.nuxeo.portlets.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.client.jaxrs.OperationRequest;
import org.nuxeo.ecm.automation.client.jaxrs.Session;
import org.nuxeo.ecm.automation.client.jaxrs.model.Blob;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

public class PublishInfosCommand implements INuxeoCommand {

	protected static Log logger = LogFactory.getLog(PublishInfosCommand.class);

	private final String inputNuxeoDocIdent;

	public PublishInfosCommand(String inputIdentDoc) {
		this.inputNuxeoDocIdent = inputIdentDoc;
	}

	public Map<String, Object> execute(Session automationSession) throws Exception {
		Map<String, Object> infos = new HashMap<String, Object>();

		OperationRequest request = automationSession.newRequest("Document.FetchPublicationInfos");
		request.set("docIdent", inputNuxeoDocIdent);
		Blob binariesInfos = (Blob) request.execute();

		if (binariesInfos != null) {
			String content = FileUtils.read(binariesInfos.getStream());
			JSONArray rows = JSONArray.fromObject(content);
			Iterator it = rows.iterator();
			while (it.hasNext()) {
				JSONObject obj = (JSONObject) it.next();
				infos.put("errorCodes", obj.get("errorCodes"));
				infos.put("documentPath", obj.get("documentPath"));
				infos.put("liveId", obj.get("liveId"));
				infos.put("publishSpacePath", obj.get("publishSpacePath"));
				infos.put("publishSpaceDisplayName", obj.get("publishSpaceDisplayName"));
				infos.put("publishSpaceInContextualization", obj.get("publishSpaceInContextualization"));
				infos.put("workspacePath", obj.get("workspacePath"));
				infos.put("workspaceDisplayName", obj.get("workspaceDisplayName"));
				infos.put("workspaceInContextualization", obj.get("workspaceInContextualization"));
				infos.put("editableByUser", obj.get("editableByUser"));
				infos.put("published", obj.get("published"));
				infos.put("anonymouslyReadable", obj.get("anonymouslyReadable"));
			}

		}
		return infos;
	}

	public String getId() {
		return "PublishInfosCommand" + inputNuxeoDocIdent;
	}
}
