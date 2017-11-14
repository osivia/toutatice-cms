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
import java.util.Date;
import java.util.List;

import org.osivia.portal.api.directory.v2.model.Person;


/**
 * Nuxeo document comment data transfert object.
 *
 * @author Cédric Krommenhoek
 */
public class CommentDTO {

    /** Identifier. */
    private String id;
    /** Path. */
    private String path;
    /** Author. */
    private String author;

    /** Author's LDAP object. */
    private Person person;

    /** Creation date. */
    private Date creationDate;
    /** Content. */
    private String content;
    /** Deletable indicator. */
    private boolean deletable;
    /** Children. */
    private final List<CommentDTO> children;


    /**
     * Default constructor.
     */
    public CommentDTO() {
        super();

        this.children = new ArrayList<CommentDTO>();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Comment [id=" + this.id + ", author=" + this.author + ", content=" + this.content + "]";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((this.id == null) ? 0 : this.id.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        CommentDTO other = (CommentDTO) obj;
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }
        return true;
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
     * Getter for author.
     *
     * @return the author
     */
    public String getAuthor() {
        return this.author;
    }

    /**
     * Setter for author.
     *
     * @param author the author to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Getter for person.
     * 
     * @return the person
     */
    public Person getPerson() {
        return person;
    }

    /**
     * Setter for person.
     * 
     * @param person the person to set
     */
    public void setPerson(Person person) {
        this.person = person;
    }

    /**
     * Getter for creationDate.
     *
     * @return the creationDate
     */
    public Date getCreationDate() {
        return this.creationDate;
    }

    /**
     * Setter for creationDate.
     *
     * @param creationDate the creationDate to set
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Getter for content.
     *
     * @return the content
     */
    public String getContent() {
        return this.content;
    }

    /**
     * Setter for content.
     *
     * @param content the content to set
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Getter for deletable.
     *
     * @return the deletable
     */
    public boolean isDeletable() {
        return this.deletable;
    }

    /**
     * Setter for deletable.
     *
     * @param deletable the deletable to set
     */
    public void setDeletable(boolean deletable) {
        this.deletable = deletable;
    }

    /**
     * Getter for children.
     *
     * @return the children
     */
    public List<CommentDTO> getChildren() {
        return this.children;
    }

}
