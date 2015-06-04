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
 * @author David Chevrier.
 *
 */
public class RemotePublishedDocumentDTO {
    
    /** Nuxeo Published document url. */
    private String nxUrl;
    /** Name of publication section. */
    private String sectionTitle;
    
    /**
     * @return the nxUrl
     */
    public String getNxUrl() {
        return nxUrl;
    }

    /**
     * @param nxUrl the nxUrl to set
     */
    public void setNxUrl(String nxUrl) {
        this.nxUrl = nxUrl;
    }

    /**
     * @return the sectionTitle
     */
    public String getSectionTitle() {
        return sectionTitle;
    }

    /**
     * @param sectionTitle the sectionTitle to set
     */
    public void setSectionTitle(String sectionTitle) {
        this.sectionTitle = sectionTitle;
    }

}
