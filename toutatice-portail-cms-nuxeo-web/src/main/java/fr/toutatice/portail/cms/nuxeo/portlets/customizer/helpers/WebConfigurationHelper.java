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

import org.jboss.portal.core.model.portal.Portal;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.osivia.portal.api.portalobject.bridge.PortalObjectUtils;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.constants.InternalConstants;

import fr.toutatice.portail.cms.nuxeo.portlets.service.CMSService;

/**
 * Assistant pour gestion des objets de conf.
 */
public final class WebConfigurationHelper {

    /** id 1 des objets de conf. */
    public static final String CODE = "wconf:code";
    /** id 2 des objets de conf. */
    public static final String ADDITIONAL_CODE = "wconf:code2";
    /** paramètres. */
    public static final String OPTIONS = "wconf:options";
    /** clé du paramnètre. */
    public static final String OPTION_KEY = "propertyName";
    /** valeur du paramètre. */
    public static final String OPTION_VALUE = "propertyDefaultValue";


    /**
     * Private constructor : prevent instantiation.
     */
    private WebConfigurationHelper() {
        throw new AssertionError();
    }


    /**
     * Retourne le domain Nuxeo à partir de la page courante.
     *
     * @param ctx contexte cms
     * @return domaine
     */
    public static String getDomainPath(CMSServiceCtx ctx) {
        
        String domainPath = null;
        

            String portalName =  PortalObjectUtils.getPortalName(ctx.getPortalControllerContext());

            // Dans certaines cas, le nom du portail n'est pas connu
            // cas des stacks server (par exemple, le pre-cahrgement des pages)
            if (portalName != null) {
                
                Portal portal = PortalObjectUtils.getPortal(ctx.getPortalControllerContext());

                if (InternalConstants.PORTAL_TYPE_SPACE.equals(portal.getDeclaredProperty("osivia.portal.portalType"))) {
                    domainPath = portal.getDefaultPage().getDeclaredProperty("osivia.cms.basePath");
                    if (domainPath != null) {
                        domainPath = domainPath.split("/")[1];
                        domainPath = "/".concat(domainPath).concat("/");
                    }
                }

      }

        return domainPath;
    }


    /**
     * Execute web config cmd (as superuser).
     *
     * @param ctx CMS context
     * @param cmsService CMS service
     * @param cmd Nuxeo command
     * @return configs
     * @throws Exception
     */
    public static Documents executeWebConfigCmd(CMSServiceCtx ctx, CMSService cmsService, WebConfigurationQueryCommand cmd) throws Exception {
        String savedScope = ctx.getScope();
        ctx.setScope("superuser_context");

        Documents configs = null;

        try {
            configs = (Documents) cmsService.executeNuxeoCommand(ctx, cmd);
        } finally {
            ctx.setScope(savedScope);
        }

        return configs;
    }

}
