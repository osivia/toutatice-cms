package fr.toutatice.portail.cms.nuxeo.portlets.document.comments;

import net.sf.json.JSONArray;

import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Blob;
import org.nuxeo.ecm.automation.client.model.Document;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

public class GetCommentsCommand implements INuxeoCommand {

	private Document document;

	public GetCommentsCommand(Document document) {
		this.document = document;
	}

	public JSONArray  execute(Session nuxeoSession) throws Exception {
		OperationRequest request = nuxeoSession.newRequest("Fetch.DocumentComments");
		request.setHeader(Constants.HEADER_NX_SCHEMAS, "*");
		request.set("commentableDoc", document.getId());
		Blob commentsBlob = (Blob) request.execute();
		if(commentsBlob != null){
			String fileContent = FileUtils.read(commentsBlob.getStream());
			JSONArray jsonComments = JSONArray.fromObject(fileContent);
			return jsonComments;
			
		}else{
			return new JSONArray();
		}
	}
	
	

	public String getId() {
		return "Fetch.DocumentComments: " + document.getId();
	}

}
