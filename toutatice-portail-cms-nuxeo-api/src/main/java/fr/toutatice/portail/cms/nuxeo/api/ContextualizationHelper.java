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
package fr.toutatice.portail.cms.nuxeo.api;

import javax.portlet.PortletRequest;

import org.apache.commons.lang.BooleanUtils;
import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.Window;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSServiceCtx;

/**
 * @author david
 * 
 */
public abstract class ContextualizationHelper {


    public static String getLivePath(String path) {
        String result = path;
        if (path.endsWith(".proxy")) {
            result = result.substring(0, result.length() - 6);
        }
        return result;
    }

  
    /**
     * Check if current document is contextualized.
     * 
     * @param cmsContext CMS context
     * @return true if current document is contextualized
     */
    public static boolean isCurrentDocContextualized(CMSServiceCtx cmsContext) {
        // Contextualized indicator
        boolean contextualized;

        if (cmsContext == null) {
            contextualized = false;
        } else {
            // Portlet request
            PortletRequest request = cmsContext.getRequest();

            contextualized = isCurrentDocContextualized(request);
        }

        return contextualized;
    }


    /**
     * Check if current document is contextualized.
     * 
     * @param portalControllerContext portal controller context
     * @return true if current document is contextualized
     */
    public static boolean isCurrentDocContextualized(PortalControllerContext portalControllerContext) {
        // Contextualized indicator
        boolean contextualized;
        
        if (portalControllerContext == null) {
            contextualized = false;
        } else {
            // Portlet request
            PortletRequest request = portalControllerContext.getRequest();
            
            contextualized = isCurrentDocContextualized(request);
        }
        
        return contextualized;
    }


    /**
     * Check if current document is contextualized.
     * 
     * @param request portlet request
     * @return true if current document is contextualized
     */
    public static boolean isCurrentDocContextualized(PortletRequest request) {
        // Current window
        Window window;
        if (request == null) {
            window = null;
        } else {
            window = (Window) request.getAttribute("osivia.window");
        }

        // Contextualized indicator
        boolean contextualized;
        if (window == null) {
            // Unknown window
            contextualized = false;
        } else if ("1".equals(window.getDeclaredProperty("osivia.cms.contextualization"))) {
            // Maximized window
            contextualized = true;
        } else {
            Boolean forceContextualization = (Boolean) request.getAttribute("osivia.cms.menuBar.forceContextualization");
            contextualized = BooleanUtils.isTrue(forceContextualization);
        }

        return contextualized;
    }


    /**
     * Détermine si le path courant impacte la navigation
     * 
     * @param cmsCtx
     * @param path
     * @return
     * @throws CMSException
     */
    public static Page getCurrentPage(CMSServiceCtx cmsCtx) throws CMSException {


        Window window = (Window) cmsCtx.getRequest().getAttribute("osivia.window");
        if (window != null) {
            return window.getPage();
        }
        return null;

    }

   

   

}
