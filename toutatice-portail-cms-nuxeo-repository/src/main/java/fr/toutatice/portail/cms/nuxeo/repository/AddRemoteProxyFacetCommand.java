package fr.toutatice.portail.cms.nuxeo.repository;

import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.adapters.DocumentService;
import org.nuxeo.ecm.automation.client.model.DocRef;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.osivia.portal.api.cms.exception.DocumentNotFoundException;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilter;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilterContext;

public class AddRemoteProxyFacetCommand implements INuxeoCommand {

    private final String path;
    
    
    public AddRemoteProxyFacetCommand(String path) {
        super();
        this.path = path;
    }

    @Override
    public Object execute(Session session) throws Exception {
           
        DocumentService documentService = session.getAdapter(DocumentService.class);
        
        documentService.addFacets(new DocRef(path), "isRemoteProxy");
        
        return null;
    }

    @Override
    public String getId() {
        return AddRemoteProxyFacetCommand.class.getCanonicalName()+"/"+path;
    }

}
