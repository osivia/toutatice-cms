package fr.toutatice.portail.cms.nuxeo.portlets.forms;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.adapters.DocumentService;
import org.nuxeo.ecm.automation.client.model.Blob;
import org.nuxeo.ecm.automation.client.model.Blobs;
import org.nuxeo.ecm.automation.client.model.DocRef;
import org.nuxeo.ecm.automation.client.model.FileBlob;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.portlet.model.UploadedFile;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

/**
 * Procedure blobs Nuxeo command 
 * 
 * @author Cédric Krommenhoek
 * @see INuxeoCommand
 */
public class BlobsProcedureCommand implements INuxeoCommand {

    /** Files Nuxeo document property. */
    private static final String FILES_PROPERTY = "files:files";


    /** Task uploaded files. */
    private final Map<String, UploadedFile> uploadedFiles;
    
    /** Document reference */    
    private final DocRef docRef;
    


    /**
     * Constructor.
     * 
     * @param taskTitle task title
     * @param actors task actors
     * @param additionalAuthorizations task additional authorizations.
     * @param properties task properties
     * @param uploadedFiles task uploaded files
     */
    public BlobsProcedureCommand(  Map<String, UploadedFile> uploadedFiles, DocRef docRef) {
        super();
        this.uploadedFiles = uploadedFiles;
        this.docRef = docRef;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return null;
    }




    public Object execute(Session nuxeoSession) throws Exception {
    	
        if (MapUtils.isNotEmpty(this.uploadedFiles)) {
            // Document service
            DocumentService documentService = nuxeoSession.getAdapter(DocumentService.class);

            // Added blobs
            List<Blob> blobs = new ArrayList<>(this.uploadedFiles.size());
            // Removed blob indexes
            SortedSet<Integer> removedIndexes = new TreeSet<>(Collections.reverseOrder());

            for (UploadedFile uploadedFile : this.uploadedFiles.values()) {
                // Temporary file
                File temporaryFile = uploadedFile.getTemporaryFile();

                if ((uploadedFile.getIndex() != null) && (uploadedFile.isDeleted() || (temporaryFile != null))) {
                    // Remove existing blob
                    removedIndexes.add(uploadedFile.getIndex());
                }

                if (temporaryFile != null) {
                    // File name
                    String fileName = uploadedFile.getTemporaryMetadata().getFileName();
                    // Mime type
                    String mimeType;
                    if (uploadedFile.getTemporaryMetadata().getMimeType() == null) {
                        mimeType = null;
                    } else {
                        mimeType = uploadedFile.getTemporaryMetadata().getMimeType().getBaseType();
                    }

                    // File blob
                    Blob blob = new FileBlob(temporaryFile, fileName, mimeType);

                    blobs.add(blob);
                }
            }

            for (Integer index : removedIndexes) {
                StringBuilder xpath = new StringBuilder();
                xpath.append(FILES_PROPERTY);
                xpath.append("/item[");
                xpath.append(index);
                xpath.append("]");

                documentService.removeBlob(docRef, xpath.toString());
            }

            if (!blobs.isEmpty()) {
                documentService.setBlobs(docRef, new Blobs(blobs), FILES_PROPERTY);
            }

            // Delete temporary files
            for (UploadedFile uploadedFile : this.uploadedFiles.values()) {
                if (uploadedFile.getTemporaryFile() != null) {
                    uploadedFile.getTemporaryFile().delete();
                }
            }
        }
        
        return null;
    }

}
