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
package fr.toutatice.portail.cms.nuxeo.portlets.document.helpers;

/**
 * Constants relatives to Document object
 * (Nuxeo automation client).
 * 
 * @author David Chevrier.
 *
 */
public interface DocumentConstants {
    
    /** Indicates if document is a folder. */
    String FOLDERISH_FACET = "Folderish";
    /** Indicates if document is a remote proxy. */
    String REMOTE_PROXY_FACET = "isRemoteProxy";
    /** Indicates if document is a live in a publish space. */
    String LOCAL_PUBLISH_LIVE_FACET = "isLocalPublishLive";
    /** Indicates if document is a draft of collaborative space. */
    String DRAFT_FACET = "OttcDraft";
    /** Indicates if document has a draft of collaborative space. */
    String CHECKINED_FACET = "OttcCheckedIn";
    
    /** WebId property key. */
    String WEBID = "ttc:webid";
    /** Draft path property key. */
    String DRAFT_PATH = "ottcChk:draftPath";
    /** Draft schema. */
    String DRAFT_SCHEMA = "ottcDraft";
     
    
    /** Approved document state. */
    String APPROVED_DOC_STATE = "approved";
    
    /** Validate online task name of a document (if any). */
    String VALIDATE_ONLINE_TASK_NAME = "validate-online";
    /** Validate remote publication task name of a document (if any). */
    String VALIDATE_REMOTE_ONLINE_TASK_NAME = "org.nuxeo.ecm.platform.publisher.task.CoreProxyWithWorkflowFactory";
    
    /** Suffix of local proxies. */
    String LOCAL_PROXIES_SUFFIX = ".proxy";
    /** Id frefix of drafts. */
    String DRAFT_WEBID_PREFIX = "draft_";
    
    

}
