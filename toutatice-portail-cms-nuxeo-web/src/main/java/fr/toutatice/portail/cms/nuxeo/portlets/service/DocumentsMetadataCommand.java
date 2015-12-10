package fr.toutatice.portail.cms.nuxeo.portlets.service;

import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.osivia.portal.core.cms.DocumentsMetadata;
import org.osivia.portal.core.constants.InternalConstants;

import fr.toutatice.portail.cms.nuxeo.portlets.list.ListCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.publish.RequestPublishStatus;


public class DocumentsMetadataCommand extends ListCommand {

    /** Version. */
    private static final String VERSION = RequestPublishStatus.published.getStatus();
    /** Schemas. */
    private static final String SCHEMAS = "dublincore, toutatice, ottc_web";
    /** Filter. */
    private static final String FILTER = InternalConstants.PORTAL_CMS_REQUEST_FILTERING_POLICY_LOCAL;


    /** CMS base path. */
    private final String basePath;


    /**
     * Constructor.
     *
     * @param basePath CMS base path
     */
    public DocumentsMetadataCommand(String basePath) {
        super(generateRequest(basePath), VERSION, 0, -1, SCHEMAS, FILTER, true);
        this.basePath = basePath;
    }


    /**
     * Generate Nuxeo request.
     *
     * @param basePath CMS base path
     * @return Nuxeo request
     */
    private static String generateRequest(String basePath) {
        StringBuilder request = new StringBuilder();
        request.append("STARTSWITH '");
        request.append(basePath);
        request.append("'");
        return request.toString();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public DocumentsMetadata execute(Session nuxeoSession) throws Exception {
        // Documents
        Documents documents = (Documents) super.execute(nuxeoSession);

        return new DocumentsMetadataImpl(this.basePath, documents.list());
    }

}
