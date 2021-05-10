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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.jboss.portal.common.invocation.Scope;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSPage;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.ICMSServiceLocator;
import org.osivia.portal.core.constants.InternalConstants;

import fr.toutatice.portail.cms.nuxeo.portlets.document.helpers.DocumentHelper;
import fr.toutatice.portail.cms.nuxeo.portlets.service.CMSService;
import fr.toutatice.portail.cms.nuxeo.portlets.service.GetUserProfileCommand;

/**
 * Préchargement des pages au login de l'utilisateur
 *
 * Pour l'instant, traitement minimimaliste ("ttc:isPreloadedOnLogin = 1";)
 *
 * A sous-classer pour spécificités
 *
 * @author jeanseb
 *
 */
public class UserPagesLoader {

    /** CMS service locator. */
    private final ICMSServiceLocator cmsServiceLocator;


    /**
     * Constructor.
     */
    public UserPagesLoader() {
        super();

        // CMS service locator
        this.cmsServiceLocator = Locator.findMBean(ICMSServiceLocator.class, ICMSServiceLocator.MBEAN_NAME);
    }


    public List<CMSPage> computeUserPreloadedPages(CMSServiceCtx cmsCtx) throws Exception {
        // Server invocation
        HttpServletRequest request = cmsCtx.getServletRequest();

        // CMS service
        CMSService cmsService = (CMSService) this.cmsServiceLocator.getCMSService();
        
        // Conversion en CMSItem
        List<CMSPage> pages = new ArrayList<CMSPage>();


        if (request.getUserPrincipal() != null) {
            String userName = request.getUserPrincipal().getName();

            // Vérifier l'init de l'espace perso avant de calculer des pages
            cmsService.executeNuxeoCommand(cmsCtx, new GetUserProfileCommand(userName));

            // User domains
            List<String> domains;
           // TODO refonte : invocation
            //List<?> domainsAttribute = (List<?>) invocation.getAttribute(Scope.SESSION_SCOPE, InternalConstants.USER_DOMAINS_ATTRIBUTE);
            List<?> domainsAttribute = new ArrayList<>();
            if (CollectionUtils.isEmpty(domainsAttribute)) {
                domains = null;
            } else {
                domains = new ArrayList<>(domainsAttribute.size());
                for (Object attribute : domainsAttribute) {
                    if (attribute instanceof String) {
                        String domain = (String) attribute;
                        domains.add(domain);
                    }
                }
            }

            Documents children = (Documents) cmsService.executeNuxeoCommand(cmsCtx, new UserPagesPreloadCommand(domains));

            for (Document child : children) {
                String spacePath = DocumentHelper.computeNavPath(child.getPath());

                CMSItem publishSpace = cmsService.createNavigationItem(cmsCtx, spacePath, child.getTitle(), child, spacePath);

                CMSPage userPage = new CMSPage();
                userPage.setPublishSpace(publishSpace);

                pages.add(userPage);
            }
        }

        return pages;
    }

}
