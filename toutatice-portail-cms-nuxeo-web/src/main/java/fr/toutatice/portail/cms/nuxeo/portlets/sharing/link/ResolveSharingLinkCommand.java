package fr.toutatice.portail.cms.nuxeo.portlets.sharing.link;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilter;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilterContext;

/**
 * Resolve sharing link Nuxeo command.
 * 
 * @author Cédric Krommenhoek
 * @see INuxeoCommand
 */
public class ResolveSharingLinkCommand implements INuxeoCommand {

    /** Operation identifier. */
    private static final String OPERATION_ID = "Document.UpdateSharingPermissions";

    /** Document creator property. */
    private static final String CREATOR_PROPERTY = "dc:creator";


    /** Link identifier. */
    private final String id;
    /** Current user. */
    private final String user;


    /**
     * Constructor.
     * 
     * @param id link identifier
     * @param user current user
     */
    public ResolveSharingLinkCommand(String id, String user) {
        super();
        this.id = id;
        this.user = user;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Document execute(Session nuxeoSession) throws Exception {
        // Target document
        Document document = this.getTargetDocument(nuxeoSession);

        if ((document != null) && !StringUtils.equals(this.user, document.getString(CREATOR_PROPERTY))) {
            // Update permissions
            this.updatePermissions(nuxeoSession, document);
        }

        return document;
    }


    /**
     * Get sharing link target document.
     * 
     * @param nuxeoSession Nuxeo session
     * @return Nuxeo document
     * @throws Exception
     */
    private Document getTargetDocument(Session nuxeoSession) throws Exception {
        // NXQL request
        StringBuilder nxqlRequest = new StringBuilder();
        nxqlRequest.append("sharing:linkId = '").append(this.id).append("' ");
        
        // Filtered request
        String filteredRequest = NuxeoQueryFilter.addPublicationFilter(NuxeoQueryFilterContext.CONTEXT_LIVE_N_PUBLISHED, nxqlRequest.toString());
        
        // Operation request
        OperationRequest request = nuxeoSession.newRequest("Document.QueryES");
        request.set(Constants.HEADER_NX_SCHEMAS, "*");
        request.set("query", filteredRequest);

        // Documents
        Documents documents = (Documents) request.execute();

        // Target document
        Document document;
        if ((documents == null) || documents.isEmpty() || (documents.size() > 1)) {
            document = null;
        } else {
            document = documents.iterator().next();
        }

        return document;
    }


    /**
     * Update sharing link target document permissions.
     * 
     * @param nuxeoSession Nuxeo session
     * @param document Nuxeo document
     * @throws Exception
     */
    private void updatePermissions(Session nuxeoSession, Document document) throws Exception {
        // Link permission
        String linkPermission = document.getString("sharing:linkPermission", "Read");

        // Operation request
        OperationRequest request = nuxeoSession.newRequest(OPERATION_ID);
        request.setInput(document);

        // Sharing permission
        request.set("permission", linkPermission);

        // User
        if (StringUtils.isNotEmpty(this.user)) {
            request.set("user", this.user);
        }

        // Add or remove permissions indicator
        request.set("add", true);

        request.execute();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getSimpleName());
        builder.append("/");
        builder.append(this.id);
        builder.append("/");
        builder.append(this.user);
        return builder.toString();
    }

}
