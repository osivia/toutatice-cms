package fr.toutatice.portail.cms.nuxeo.portlets.move;

import java.util.List;

import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.adapters.DocumentSecurityService;
import org.nuxeo.ecm.automation.client.model.DocRef;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

/**
 * Remove all permissions Nuxeo command.
 * 
 * @author Cédric Krommenhoek
 * @see INuxeoCommand
 */
public class RemoveAllPermissionsCommand implements INuxeoCommand {

    /** Document identifiers. */
    private final List<String> documentIds;


    /**
     * Constructor.
     * 
     * @param documentIds document identifiers
     */
    public RemoveAllPermissionsCommand(List<String> documentIds) {
        super();
        this.documentIds = documentIds;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Session nuxeoSession) throws Exception {
        // Document Service
        DocumentSecurityService securityService = nuxeoSession.getAdapter(DocumentSecurityService.class);

        for (String documentId : this.documentIds) {
            DocRef docRef = new DocRef(documentId);
            securityService.removePermissions(docRef, null, null, DocumentSecurityService.LOCAL_ACL, true, true);
        }

        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return null;
    }

}
