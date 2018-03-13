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
package fr.toutatice.portail.cms.nuxeo.portlets.service;

import java.util.Iterator;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Blob;
import org.nuxeo.ecm.automation.client.model.FileBlob;
import org.osivia.portal.core.web.IWebIdService;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.cms.LockStatus;
import fr.toutatice.portail.cms.nuxeo.api.cms.QuickAccessStatus;
import fr.toutatice.portail.cms.nuxeo.api.cms.SubscriptionStatus;
import fr.toutatice.portail.cms.nuxeo.portlets.cms.ExtendedDocumentInfos;

/**
 * Extended document informations Nuxeo command.
 *
 * @author David Chevrier
 * @see INuxeoCommand
 */
public class ExtendedDocumentInfosCommand implements INuxeoCommand {

    /** Document path. */
    private final String path;


    /**
     * Constructor.
     *
     * @param path document path
     */
    public ExtendedDocumentInfosCommand(String path) {
        this.path = path;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Session nuxeoSession) throws Exception {
        // Document extended informations
        ExtendedDocumentInfos docInfos = new ExtendedDocumentInfos();

        // Operation request
        OperationRequest request = nuxeoSession.newRequest("Document.FetchExtendedDocInfos");
        if (path.startsWith(IWebIdService.FETCH_PATH_PREFIX)) {
            request.set("webid", path.replaceAll(IWebIdService.FETCH_PATH_PREFIX, StringUtils.EMPTY));
        } else {
            request.set("path", path);
        }

        // Request execution
        Blob infosAsBlob = (Blob) request.execute();

        if (infosAsBlob != null) {
            String infosContentStr = IOUtils.toString(infosAsBlob.getStream(), "UTF-8");
            JSONArray infosContent = JSONArray.fromObject(infosContentStr);

            Iterator<?> iterator = infosContent.iterator();
            while (iterator.hasNext()) {
                JSONObject infos = (JSONObject) iterator.next();

                // DCH: FIXME: abstract task infos (like name)
                if (infos.containsKey("taskName")) {
                    String taskName = infos.getString("taskName");

                    if (StringUtils.isNotBlank(taskName)) {
                        docInfos.setTaskName(taskName);
                        docInfos.setOnlineTaskPending(infos.getBoolean("isTaskPending"));
                        docInfos.setCanUserValidateOnlineTask(infos.getBoolean("canManageTask"));
                        docInfos.setUserOnlineTaskInitiator(infos.getBoolean("isTaskInitiator"));
                    }
                }

                if (infos.containsKey("isValidationWfRunning")) {
                    docInfos.setValidationWorkflowRunning(infos.getBoolean("isValidationWfRunning"));
                }

                if (infos.containsKey("subscription_status")) {
                    String status = StringUtils.upperCase(infos.get("subscription_status").toString());
                    docInfos.setSubscriptionStatus(SubscriptionStatus.valueOf(status));
                }

                if (infos.containsKey("lockStatus")) {
                    String status = StringUtils.upperCase(infos.get("lockStatus").toString());
                    docInfos.setLockStatus(LockStatus.valueOf(status));

                    if (infos.containsKey("lockOwner")) {
                        docInfos.setLockOwner(infos.get("lockOwner").toString());
                    }
                }
                if (infos.containsKey("quickAccess_status")) {
                    String status = StringUtils.upperCase(infos.get("quickAccess_status").toString());
                    docInfos.setQuickAccessStatus(QuickAccessStatus.valueOf(status));
                }

                /* Infos from Drive */
                if (infos.containsKey("canSynchronize")) {
                    docInfos.setCanSynchronize(BooleanUtils.toBoolean(infos.get("canSynchronize").toString()));
                }
                if (infos.containsKey("canUnsynchronize")) {
                    docInfos.setCanUnsynchronize(BooleanUtils.toBoolean(infos.get("canUnsynchronize").toString()));
                }
                if (infos.containsKey("synchronizationRootPath")) {
                    docInfos.setSynchronizationRootPath(infos.get("synchronizationRootPath").toString());
                }

                // Draft count for Folderish
                if (infos.containsKey("draftCount")) {
                    docInfos.setDraftCount(infos.getInt("draftCount"));
                }

                // File PDF conversion
                if(infos.containsKey("isPdfConvertible")){
                    docInfos.setPdfConvertible(infos.getBoolean("isPdfConvertible"));
                }
                if(infos.containsKey("errorOnPdfConversion")){
                    docInfos.setErrorOnPdfConversion(infos.getBoolean("errorOnPdfConversion"));
                }

				if (infos.containsKey("isCurrentlyEdited")) {
                    boolean isCurrentlyEdited = infos.getBoolean("isCurrentlyEdited");
                    docInfos.setCurrentlyEdited(isCurrentlyEdited);
                    if (isCurrentlyEdited) {
                        docInfos.setCurrentlyEditedEntry(infos.getJSONObject("currentlyEditedEntry"));
                    }
                }

                if (infos.containsKey("isRecentlyEdited")) {
                    boolean isCurrentlyEdited = infos.getBoolean("isRecentlyEdited");
                    docInfos.setRecentlyEdited(isCurrentlyEdited);
                    if (isCurrentlyEdited) {
                        docInfos.setRecentlyEditedEntry(infos.getJSONObject("recentlyEditedEntry"));
                    }
                }

                // Parent webId
                if (infos.containsKey("parentWebId")) {
                    docInfos.setParentWebId(infos.getString("parentWebId"));
                }
            }
        }

        // Delete files?
        if (infosAsBlob instanceof FileBlob) {
            ((FileBlob) infosAsBlob).getFile().delete();
        }

        return docInfos;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getName());
        builder.append("/");
        builder.append(path);

        return builder.toString();
    }

}
