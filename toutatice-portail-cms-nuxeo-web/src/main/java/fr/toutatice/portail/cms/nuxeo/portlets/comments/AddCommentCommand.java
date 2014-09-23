/**
 *
 */
package fr.toutatice.portail.cms.nuxeo.portlets.comments;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.adapters.DocumentService;
import org.nuxeo.ecm.automation.client.model.Blob;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.FileBlob;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.domain.CommentDTO;
import fr.toutatice.portail.cms.nuxeo.api.domain.ThreadPostDTO;

/**
 * Add comment command.
 * 
 * @author David Chevrier
 * @author Cédric Krommenhoek
 * @see INuxeoCommand
 */
public class AddCommentCommand implements INuxeoCommand {

    /** Nuxeo document. */
    private final Document document;
    /** Comment. */
    private final CommentDTO comment;
    /** Parent comment identifier. */
    private final String parentId;


    /**
     * Constructor.
     * 
     * @param document Nuxeo document
     * @param comment comment
     * @param parentId parent comment identifier, may be null
     */
    public AddCommentCommand(Document document, CommentDTO comment, String parentId) {
        super();
        this.document = document;
        this.comment = comment;
        this.parentId = parentId;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Session nuxeoSession) throws Exception {
        // Document service
        DocumentService documentService = nuxeoSession.getAdapter(DocumentService.class);
        // Request
        OperationRequest request;

        // Thread post attributes
        String title = null;
        File attachment = null;
        String filename = null;
        if (this.comment instanceof ThreadPostDTO) {
            ThreadPostDTO threadPost = (ThreadPostDTO) this.comment;
            // Title
            title = threadPost.getTitle();
            // Attachment
            attachment = threadPost.getAttachment();
            // Attachment file name
            filename = threadPost.getFilename();
        }

        if (StringUtils.isBlank(this.parentId)) {
            // Add comment request
            request = nuxeoSession.newRequest("Document.AddComment");

            // Comment content
            request.set("comment", this.comment.getContent());
            // Thread post title
            if (title != null) {
                request.set("title", title);
            }
        } else {
            // Create child comment request
            request = nuxeoSession.newRequest("Document.CreateChildComment");

            // Comment parent identifier
            request.set("comment", this.parentId);
            // Comment content
            request.set("childComment", this.comment.getContent());
            // Thread post title
            if (title != null) {
                request.set("childCommentTitle", title);
            }
        }

        // Parent document identifier
        request.set("commentableDoc", this.document.getId());
        File tmpFile = null;
        // Thread post attachment
        if (attachment != null) {
            request.set("fileName", filename);
            request.setInput(new FileBlob(attachment));
        } else {
            /* Nuxeo operation need no null input */
            tmpFile = File.createTempFile("tmp_com", ".tmp");
            request.setInput(new FileBlob(tmpFile));
        }

        Blob blob = (Blob) request.execute();

        /* To avoid temporary file persistence */
        if (tmpFile != null) {
            tmpFile.delete();
        }

        // Update thread
        documentService.setProperty(this.document, "dc:title", this.document.getTitle());

        // return blob;
        return blob;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.isBlank(this.parentId)) {
            builder.append("Document.AddComment: ");
        } else {
            builder.append("Document.CreateChildComment: ");
        }
        builder.append(this.document.getTitle());
        return builder.toString();
    }

}
