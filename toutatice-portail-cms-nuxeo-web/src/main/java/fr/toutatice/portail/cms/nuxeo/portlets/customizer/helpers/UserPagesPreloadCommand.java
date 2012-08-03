package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.nuxeo.ecm.automation.client.jaxrs.Constants;
import org.nuxeo.ecm.automation.client.jaxrs.OperationRequest;
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
public class UserPagesPreloadCommand implements INuxeoCommand {



	public UserPagesPreloadCommand( ) {
		super();

	}

	public Object execute(Session session) throws Exception {

	
		OperationRequest request;

		request = session.newRequest("Document.Query");

		
		String nuxeoRequest = "ttc:isPreloadedOnLogin = 1";
		
		// Insertion du filtre sur les élements publiés
		String filteredRequest = NuxeoQueryFilter.addPublicationFilter(nuxeoRequest, false);
	
		request.set("query", "SELECT * FROM Document WHERE " + filteredRequest + " ORDER BY ecm:pos");
		
	
		request.setHeader(Constants.HEADER_NX_SCHEMAS, "dublincore,common, toutatice");

		Documents children = (Documents) request.execute();


		return children;

	}

	public String getId() {
		return "UserPagesPreloadCommand";
	};

}
