package fr.toutatice.portail.cms.nuxeo.portlets.files;

import java.io.IOException;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Blob;
import org.nuxeo.ecm.automation.client.model.Blobs;
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

    /** overwite */
    private boolean overwite;


    /**
     * Constructor.
     */
    public UploadFilesCommand(String parentId, List<FileItem> fileItems) {
        super();
        this.parentId = parentId;
        this.fileItems = fileItems;
        this.overwite = false;
    }


    public UploadFilesCommand(String parentId, List<FileItem> fileItems, boolean overwite) {
        super();
        this.parentId = parentId;
        this.fileItems = fileItems;
        this.overwite = overwite;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Session nuxeoSession) throws Exception {
        Blobs blobs = this.getBlobsList(this.fileItems);

        // Operation request
        OperationRequest operationRequest = nuxeoSession.newRequest("FileManager.Import").setInput(blobs);
        operationRequest.setContextProperty("currentDocument", this.parentId);
        operationRequest.set("overwite", overwite);

        return operationRequest.execute();
    }


    /**
     * Build a blobs list from input files items.
     *
     * @param fileItems
     * @return blobs list
     * @throws IOException
     */
    public Blobs getBlobsList(List<FileItem> fileItems) throws IOException{
        Blobs blobs = new Blobs();

        for (FileItem fileItem : fileItems) {
            String name = fileItem.getName();
            Blob blob = new StreamBlob(fileItem.getInputStream(), name, fileItem.getContentType());
            blobs.add(blob);
        }

        return blobs;
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
