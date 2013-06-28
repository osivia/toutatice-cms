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

		
		String nuxeoRequest = "ttc:isPreloadedOnLogin = 1";
		
		// Insertion du filtre sur les élements publiés
		StringBuffer bufferedRequest = new StringBuffer();
		bufferedRequest.append("((");
		bufferedRequest.append(NuxeoQueryFilter.addPublicationFilter(nuxeoRequest, false));
		bufferedRequest.append(") OR (");
		bufferedRequest.append(NuxeoQueryFilter.addPublicationFilter(nuxeoRequest, true));
		bufferedRequest.append("))");
	
		request.set("query", "SELECT * FROM Document WHERE " + bufferedRequest.toString() + " ORDER BY ttc:tabOrder");
		
	
		request.setHeader(Constants.HEADER_NX_SCHEMAS, "dublincore,common, toutatice");

		Documents children = (Documents) request.execute();


		return children;

	}

	public String getId() {
		return "UserPagesPreloadCommand";
	};

}
