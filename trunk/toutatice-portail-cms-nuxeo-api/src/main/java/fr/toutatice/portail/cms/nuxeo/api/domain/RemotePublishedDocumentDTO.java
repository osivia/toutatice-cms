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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;



/**
 * @author David Chevrier.
 *
 */
public class RemotePublishedDocumentDTO {
    
    //DCH: FIXME: no notion of Nuxeo at this level?
    
    /** NUxeo path context. */
    public static final String NX_PATH_CTX = "/nuxeo/nxpath/default/";
    
    /** Nuxeo Published document url. */
    private String nxUrl;
    /** Name of publication section. */
    private String sectionTitle;
    /** Version of remote published document. */
    private String versionLabel;
    
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
     * @return the path of published Document.
     */
    public String getPath(){
        String path = StringUtils.substringAfter(this.nxUrl, NX_PATH_CTX);
        return StringUtils.substringBefore(path, "@");
    }
    
    /**
     * @return portal contextualization param.
     */
    public Map<String, String> getLinkContextualization(){
        Map<String, String> param = new HashMap<String, String>(1);
        param.put("contextualization", "portal");
        return param;
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

    /**
     * @return the versionLabel
     */
    public String getVersionLabel() {
        return versionLabel;
    }
    
    /**
     * @param versionLabel the versionLabel to set
     */
    public void setVersionLabel(String versionLabel) {
        this.versionLabel = versionLabel;
    }

}
