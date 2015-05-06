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
import org.osivia.portal.core.cms.CMSExtendedDocumentInfos;
import org.osivia.portal.core.web.IWebIdService;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;


/**
 * @author david
 *
 */
public class ExtendedDocInfosCommand implements INuxeoCommand {

    /** Document's path. */
    private String path;

    public ExtendedDocInfosCommand(String path) {
        this.path = path;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CMSExtendedDocumentInfos execute(Session nuxeoSession) throws Exception {

        CMSExtendedDocumentInfos docInfos = new CMSExtendedDocumentInfos();

        OperationRequest request = nuxeoSession.newRequest("Document.FetchExtendedDocInfos");
        if (path.startsWith(IWebIdService.PREFIX_WEBID_FETCH_PUB_INFO)) {
            request.set("webid", path.replaceAll(IWebIdService.PREFIX_WEBID_FETCH_PUB_INFO, StringUtils.EMPTY));
        } else {
            request.set("path", path);
        }
        Blob infosAsBlob = (Blob) request.execute();

        if (infosAsBlob != null) {

            docInfos = new CMSExtendedDocumentInfos();
            // Default behaviour
            docInfos.setIsOnlineTaskPending(Boolean.FALSE);
            docInfos.setCanUserValidateOnlineTask(Boolean.FALSE);
            docInfos.setIsUserOnlineTaskInitiator(Boolean.FALSE);

            String infosContentStr = IOUtils.toString(infosAsBlob.getStream(), "UTF-8");
            JSONArray infosContent = JSONArray.fromObject(infosContentStr);

            Iterator<?> iterator = infosContent.iterator();
            while (iterator.hasNext()) {
                JSONObject infos = (JSONObject) iterator.next();

                if (infos.containsKey("taskName")) {
                    String taskName = infos.getString("taskName");

                    if ("validate-online".equals(taskName)) {
                        docInfos.setIsOnlineTaskPending(infos.getBoolean("isTaskPending"));
                        docInfos.setCanUserValidateOnlineTask(infos.getBoolean("canManageTask"));
                        docInfos.setIsUserOnlineTaskInitiator(infos.getBoolean("isTaskInitiator"));
                    }
                }

                if (infos.containsKey("subscription_status")) {
                    docInfos.setSubscriptionStatus(CMSExtendedDocumentInfos.SubscriptionStatus.valueOf(infos.get("subscription_status").toString()));
                }
                
                if (infos.containsKey("lockStatus")) {
                    docInfos.setLockStatus(CMSExtendedDocumentInfos.LockStatus.valueOf(infos.get("lockStatus").toString()));
                    
                    if (infos.containsKey("lockOwner")) {
                    	docInfos.setLockOwner(infos.get("lockOwner").toString());
                    }
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
                if (infos.containsKey("driveEditURL")) {
                	docInfos.setDriveEditURL(infos.get("driveEditURL").toString());
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
        return "ExtendedDocInfosCommand /" + path;
    }

}
