package fr.toutatice.portail.cms.nuxeo.portlets.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.nuxeo.ecm.automation.client.jaxrs.Constants;
import org.nuxeo.ecm.automation.client.jaxrs.OperationRequest;
import org.nuxeo.ecm.automation.client.jaxrs.RemoteException;
import org.nuxeo.ecm.automation.client.jaxrs.Session;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
import org.nuxeo.ecm.automation.client.jaxrs.model.Documents;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.core.NuxeoQueryFilter;
import fr.toutatice.portail.core.cms.NavigationItem;

/**
 * Return all the navigation items
 * 
 * @author jeanseb
 * 
 */
public class DocumentResolvePublishSpaceCommand implements INuxeoCommand {

	String path;

	public DocumentResolvePublishSpaceCommand(  String path) {
		super();
		this.path = path;
	}

	public Object execute(Session session) throws Exception {

	
		
		OperationRequest request;
		org.nuxeo.ecm.automation.client.jaxrs.model.Document publishSpace = null;


		try	{
		 publishSpace = (org.nuxeo.ecm.automation.client.jaxrs.model.Document) session
		.newRequest("Document.FetchPublishSpace").setHeader(Constants.HEADER_NX_SCHEMAS, "dublincore,common, toutatice")
		.set("value", path).execute();
		} catch (RemoteException e){
			if( e.getStatus() == 404){
				String prefix = "/default-domain/workspaces/ac-rennes/";
				
				
				if( path.startsWith(prefix))	{
					String sitePath = path;
					int end = path.indexOf("/", prefix.length());
					if( end != -1)
						sitePath = path.substring(0, end);
					publishSpace = (org.nuxeo.ecm.automation.client.jaxrs.model.Document) session
				.newRequest("Document.Fetch").setHeader(Constants.HEADER_NX_SCHEMAS, "dublincore,common, toutatice")
				.set("value",sitePath).execute();
				}
				
			}
		}





		return publishSpace;

	}

	public String getId() {
		return "ResolvePublishSpaceCommand/" + path;
	};

}
