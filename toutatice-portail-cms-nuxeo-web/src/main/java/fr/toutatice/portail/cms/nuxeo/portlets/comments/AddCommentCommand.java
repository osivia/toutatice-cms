/**
 *
 */
package fr.toutatice.portail.cms.nuxeo.portlets.comments;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
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
        } else {
            // Create child comment request
            request = nuxeoSession.newRequest("Document.CreateChildComment");

            // Comment parent identifier
            request.set("parent", this.parentId);
        }

        // Commentable document
        request.set("document", this.document.getId());
        // Comment content
        request.set("content", this.comment.getContent());
        // Comment author
        request.set("author", this.comment.getAuthor());
        // Comment creation date
        request.set("creationDate", this.comment.getCreationDate());
        // Thread post title
        request.set("title", title);
        // Thread post attachment
        if (attachment != null) {
            request.set("fileName", filename);
            request.setInput(new FileBlob(attachment));
        }

        return request.execute();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return null;
    }

}
