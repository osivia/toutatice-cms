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
 *
 *
 *    
 */
package fr.toutatice.portail.cms.nuxeo.api.domain;

import java.util.List;

import org.osivia.portal.api.directory.entity.DirectoryPerson;


/**
 * Nuxeo document comment java-bean.
 *
 * @author Cédric Krommenhoek
 */
public class Comment {

    /** Identifier. */
    private String id;
    /** Path. */
    private String path;
    /** Author. */
    private String author;

    /** Author's LDAP object. */
    private DirectoryPerson person;

    /** Creation date. */
    private String creationDate;
    /** Content. */
    private String content;
    /** Deletable indicator. */
    private boolean deletable;
    /** Children. */
    private List<? extends Comment> children;


    /**
     * Default constructor.
     */
    public Comment() {
        super();
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
        Comment other = (Comment) obj;
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
     * @return the person
     */
    public DirectoryPerson getPerson() {
        return person;
    }


    /**
     * @param person the person to set
     */
    public void setPerson(DirectoryPerson person) {
        this.person = person;
    }


    /**
     * Getter for creationDate.
     *
     * @return the creationDate
     */
    public String getCreationDate() {
        return this.creationDate;
    }

    /**
     * Setter for creationDate.
     *
     * @param creationDate the creationDate to set
     */
    public void setCreationDate(String creationDate) {
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
    public List<? extends Comment> getChildren() {
        return this.children;
    }

    /**
     * Setter for children.
     *
     * @param children the children to set
     */
    public void setChildren(List<? extends Comment> children) {
        this.children = children;
    }

}
