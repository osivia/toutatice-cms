/*
 * (C) Copyright 2014 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package fr.toutatice.portail.cms.nuxeo.api.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.cms.DocumentType;

/**
 * Document data transfert object.
 *
 * @author Cédric Krommenhoek
 * @see Cloneable
 */
public class DocumentDTO implements Cloneable {

    /** Document identifier. */
    private String id;
    /** Document title. */
    private String title;
    /** Document path. */
    private String path;
    /** Document type. */
    private DocumentType type;
    /** Document properties. */
    private final Map<String, Object> properties;
    /** Document attachments. */
    private final List<DocumentAttachmentDTO> attachments;
    /** Document commentable indicator. */
    private boolean commentable;
    /** Document comments. */
    private final List<CommentDTO> comments;
    /** Remote published documents of this document. */
    private final List<RemotePublishedDocumentDTO> publishedDocuments;

    /** Original Nuxeo document. */
    private Document document;


    /**
     * Constructor.
     */
    public DocumentDTO() {
        super();
        this.properties = new HashMap<String, Object>();
        this.attachments = new ArrayList<DocumentAttachmentDTO>();
        this.comments = new ArrayList<CommentDTO>();
        this.publishedDocuments = new ArrayList<RemotePublishedDocumentDTO>();
    }


    /**
     * Constructor.
     *
     * @param documentDTO document DTO
     */
    protected DocumentDTO(DocumentDTO documentDTO) {
        this();
        this.id = documentDTO.id;
        this.title = documentDTO.title;
        this.path = documentDTO.path;
        this.type = documentDTO.type;
        this.properties.putAll(documentDTO.properties);
        this.attachments.addAll(documentDTO.attachments);
        this.commentable = documentDTO.commentable;
        this.comments.addAll(documentDTO.comments);
        this.publishedDocuments.addAll(documentDTO.publishedDocuments);
        this.document = documentDTO.document;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DocumentDTO [title=");
        builder.append(this.title);
        builder.append(", path=");
        builder.append(this.path);
        builder.append(", type=");
        builder.append(this.type);
        builder.append("]");
        return builder.toString();
    }


    /**
     * Getter for id.
     *
     * @return the id
     */
    public String getId() {
        return this.id;
    }

    /**
     * Setter for id.
     *
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter for title.
     *
     * @return the title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Setter for title.
     *
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Getter for path.
     *
     * @return the path
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Setter for path.
     *
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Getter for type.
     *
     * @return the type
     */
    public DocumentType getType() {
        return this.type;
    }

    /**
     * Setter for type.
     *
     * @param type the type to set
     */
    public void setType(DocumentType type) {
        this.type = type;
    }

    /**
     * Getter for commentable.
     *
     * @return the commentable
     */
    public boolean isCommentable() {
        return this.commentable;
    }

    /**
     * Setter for commentable.
     *
     * @param commentable the commentable to set
     */
    public void setCommentable(boolean commentable) {
        this.commentable = commentable;
    }

    /**
     * Getter for document.
     *
     * @return the document
     */
    public Document getDocument() {
        return this.document;
    }

    /**
     * Setter for document.
     *
     * @param document the document to set
     */
    public void setDocument(Document document) {
        this.document = document;
    }

    /**
     * Getter for properties.
     *
     * @return the properties
     */
    public Map<String, Object> getProperties() {
        return this.properties;
    }

    /**
     * Getter for attachments.
     *
     * @return the attachments
     */
    public List<DocumentAttachmentDTO> getAttachments() {
        return this.attachments;
    }

    /**
     * Getter for comments.
     *
     * @return the comments
     */
    public List<CommentDTO> getComments() {
        return this.comments;
    }

    /**
     * Getter for publishedDocuments.
     *
     * @return the publishedDocuments
     */
    public List<RemotePublishedDocumentDTO> getPublishedDocuments() {
        return this.publishedDocuments;
    }

}
