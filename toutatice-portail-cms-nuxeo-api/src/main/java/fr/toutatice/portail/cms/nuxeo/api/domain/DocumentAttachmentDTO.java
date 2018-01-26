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

/**
 * Document attachment data transfert object.
 *
 * @author Cédric Krommenhoek
 */
public class DocumentAttachmentDTO {

    /** Attachment name. */
    private String name;
    /** Attachment icon. */
    private String icon;
    /** Attachment size. */
    private Long size;
    /** Attachment digest. */
    private String digest;
    /** Attachment URL. */
    private String url;


    /**
     * Default constructor.
     */
    public DocumentAttachmentDTO() {
        super();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "DocumentAttachmentDTO [name=" + this.name + ", url=" + this.url + "]";
    }


    /**
     * Getter for name.
     *
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Setter for name.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for icon.
     * 
     * @return the icon
     */
    public String getIcon() {
        return icon;
    }

    /**
     * Setter for icon.
     * 
     * @param icon the icon to set
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     * Getter for size.
     * 
     * @return the size
     */
    public Long getSize() {
        return size;
    }

    /**
     * Setter for size.
     * 
     * @param size the size to set
     */
    public void setSize(Long size) {
        this.size = size;
    }

    /**
     * Getter for digest.
     * 
     * @return the digest
     */
    public String getDigest() {
        return digest;
    }

    /**
     * Setter for digest.
     * 
     * @param digest the digest to set
     */
    public void setDigest(String digest) {
        this.digest = digest;
    }

    /**
     * Getter for url.
     *
     * @return the url
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Setter for url.
     *
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

}
