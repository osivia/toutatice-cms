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
package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

/**
 * Menu-bar creation sub-type java-bean.
 *
 * @author Cédric Krommenhoek
 */
public class MenuBarCreationSubType {

    /** URL. */
    private String url;
    /** Name. */
    private String name;
    /** Document type. */
    private String docType;


    /**
     * Constructor.
     */
    public MenuBarCreationSubType() {
        super();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "MenuBarCreationSubType [name=" + this.name + ", docType=" + this.docType + "]";
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
     * Getter for docType.
     *
     * @return the docType
     */
    public String getDocType() {
        return this.docType;
    }

    /**
     * Setter for docType.
     *
     * @param docType the docType to set
     */
    public void setDocType(String docType) {
        this.docType = docType;
    }

}
