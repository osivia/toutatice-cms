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
package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSServiceCtx;

import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.WebConfiguratinQueryCommand.WebConfigurationType;
import fr.toutatice.portail.cms.nuxeo.portlets.service.CMSService;


public class CMSToWebPathAdapter {

    private CMSService cmsService;

    public CMSToWebPathAdapter(CMSService cmsService) {
        this.cmsService = cmsService;
    }

    public String adaptCMSPathToWeb(CMSServiceCtx cmsCtx, String basePath, String requestPath, boolean webPath) throws CMSException {

        // compute domain path
        String domainPath = WebConfigurationHelper.getDomainPath(cmsCtx);

        if (domainPath != null) {
            // get configs installed in nuxeo
            WebConfiguratinQueryCommand command = new WebConfiguratinQueryCommand(domainPath, WebConfigurationType.CMSToWebPathAdapter);
             
            Documents configs = null;
            try {
                 configs = WebConfigurationHelper.executeWebConfigCmd(cmsCtx, cmsService, command);
           } catch (Exception e) {
                // Can't get confs
            }
            
            // Try to translate virtual urls defined in Nuxeo
            if (configs != null && configs.size() > 0) {
                int i = 0;
                for (Document config : configs) {
                    String cmsPathPattern = config.getProperties().getString(WebConfigurationHelper.CODE);
                    String webPathPattern = config.getProperties().getString(WebConfigurationHelper.CODECOMP);

                    if (webPath) {
                        if (requestPath.startsWith(webPathPattern)) {
                            return cmsPathPattern.concat(requestPath.substring(webPathPattern.length()));
                        }
                    } else {
                        if (requestPath.startsWith(cmsPathPattern)) {
                            return webPathPattern.concat(requestPath.substring(cmsPathPattern.length()));
                        }
                    }
                }
            }
        }

        // Default, use weburl for documents in the docs in the current space
        if (webPath) {
            return basePath + requestPath;
        } else {
            if (requestPath.startsWith(basePath))
                return requestPath.substring(basePath.length());
            else
                return null;
        }
    }
}
