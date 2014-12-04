package fr.toutatice.portail.cms.nuxeo.portlets.files;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.adapters.DocumentService;
import org.nuxeo.ecm.automation.client.model.Blob;
import org.nuxeo.ecm.automation.client.model.DocRef;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.IdRef;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.nuxeo.ecm.automation.client.model.StreamBlob;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

/**
 * Upload files command.
 *
 * @author Cédric Krommenhoek
 * @see INuxeoCommand
 */
public class UploadFilesCommand implements INuxeoCommand {

    /** Parent identifier. */
    private final String parentId;
    /** File items. */
    private final List<FileItem> fileItems;


    /**
     * Constructor.
     */
    public UploadFilesCommand(String parentId, List<FileItem> fileItems) {
        super();
        this.parentId = parentId;
        this.fileItems = fileItems;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Session nuxeoSession) throws Exception {
        // Parent document reference
        DocRef parent = new IdRef(this.parentId);
        // Document type
        String type = "File";

        // Documents
        List<Document> documents = new ArrayList<Document>(this.fileItems.size());

        // Document service
        DocumentService documentService = nuxeoSession.getAdapter(DocumentService.class);

        for (FileItem fileItem : this.fileItems) {
            // Document name
            String name = fileItem.getName();

            // Document properties
            PropertyMap properties = new PropertyMap();
            properties.set("dc:title", name);

            // Create document
            Document document = documentService.createDocument(parent, type, name, properties);

            // Add blob
            Blob blob = new StreamBlob(fileItem.getInputStream(), name, fileItem.getContentType());
            documentService.setBlob(document, blob);

            documents.add(document);
        }

        return documents;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getSimpleName());
        builder.append(" : ");
        builder.append(this.parentId);
        builder.append(" ; ");
        builder.append(this.fileItems);
        return builder.toString();
    }

}
