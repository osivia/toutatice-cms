package fr.toutatice.portail.cms.nuxeo.portlets.service;

import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Documents;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilter;

/**
 * Requête Nuxeo de recherche des Liens définis dans un dossier
 *
 */
public class DocumentGetMediaLibraryCommand implements INuxeoCommand {

	String path;
	
    public DocumentGetMediaLibraryCommand(String path) {
		super();
		this.path = path;
	}
	
	/**
	 * Construction de la requête
	 */
	public Object execute(Session session) throws Exception {
		OperationRequest request;
		
		request =  session.newRequest("Document.Query");
		
        String nuxeoRequest = "ecm:path STARTSWITH '" + path + "' AND ecm:primaryType = 'MediaLibrary' ORDER BY ecm:pos";
		
        boolean displayLiveVersion = true; // Ne pas afficher les versions de travail
		// Insertion du filtre sur les élements publiés
		String filteredRequest = NuxeoQueryFilter.addPublicationFilter(nuxeoRequest, displayLiveVersion , "global");
		
		request.set("query", "SELECT * FROM Document WHERE "  + filteredRequest);
		
		// On récupère seulement le schéma global et celui du contextualLink (pour l'url).
        request.setHeader(Constants.HEADER_NX_SCHEMAS, "dublincore");

		Documents children = (Documents) request.execute();	
		
        if (children.size() == 1)
            return children.get(0);
        else
            return null;
	}

	public String getId() {
        return "DocumentGetMediaLibraryCommand/" + path;
	}

	public String getPath() {
		return path;
	}
	
	

}
