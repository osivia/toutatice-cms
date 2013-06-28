package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import org.nuxeo.ecm.automation.client.jaxrs.Constants;
import org.nuxeo.ecm.automation.client.jaxrs.OperationRequest;
import org.nuxeo.ecm.automation.client.jaxrs.Session;
import org.nuxeo.ecm.automation.client.jaxrs.model.Documents;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.core.NuxeoQueryFilter;


/**
 * Return all the navigation items
 * 
 * @author jeanseb
 * 
 */
public class UserPagesPreloadCommand implements INuxeoCommand {



	public UserPagesPreloadCommand( ) {
		super();

	}

	public Object execute(Session session) throws Exception {

	
		OperationRequest request;

		request = session.newRequest("Document.Query");

  		
		//v2.0.9 : suite demande Marc
		String bufferedRequest = "ttc:isPreloadedOnLogin = 1 AND ecm:mixinType = 'Space' AND (ecm:isProxy = 1 OR (ecm:isProxy = 0 AND ecm:mixinType <> 'WebView')) AND (ecm:currentLifeCycleState <> 'deleted' AND ecm:isCheckedInVersion = 0 )";


		request.set("query", "SELECT * FROM Document WHERE " + bufferedRequest.toString() + " ORDER BY ttc:tabOrder");
		
	
		request.setHeader(Constants.HEADER_NX_SCHEMAS, "dublincore,common, toutatice");

		Documents children = (Documents) request.execute();


		return children;

	}

	public String getId() {
		return "UserPagesPreloadCommand";
	};

}
