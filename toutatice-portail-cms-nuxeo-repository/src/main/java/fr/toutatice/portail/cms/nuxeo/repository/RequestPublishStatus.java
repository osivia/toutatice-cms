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
package fr.toutatice.portail.cms.nuxeo.repository;

import org.apache.commons.lang.StringUtils;


/**
 * Manages published status.
 * 
 * @author David Chevrier
 *
 */
public enum RequestPublishStatus {
    
    live ("1"), published (""), notLocalLives ("2"), liveNRemotePublished("3");
    
    /** Status. */
    private String status;
    
    /**
     * Constructor
     * @param status
     */
    private RequestPublishStatus(String status){
        this.status = status;
    }
    
    /**
     * @return publish status.
     */
    public String getStatus(){
        return this.status;
    }
    
    /**
     * @param status
     * @return RequestPublishStatus according to status.
     */
    public static RequestPublishStatus setRequestPublishStatus(String status) {
        RequestPublishStatus requestStatus = null;

        if ("1".equals(status)) {
            requestStatus = RequestPublishStatus.live;
        } else if (StringUtils.isEmpty(status)) {
            requestStatus = RequestPublishStatus.published;
        } else if ("2".equals(status)) {
            requestStatus = RequestPublishStatus.notLocalLives;
        } else if ("3".equals(status)) {
            requestStatus = RequestPublishStatus.liveNRemotePublished;
        }

        return requestStatus;
    }

}
