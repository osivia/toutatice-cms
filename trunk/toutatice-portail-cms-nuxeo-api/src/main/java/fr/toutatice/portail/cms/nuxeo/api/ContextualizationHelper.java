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

import org.apache.commons.lang.BooleanUtils;
import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.Window;
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
     * Méthode permettant de déterminer si une portlet s'affiche
     * en mode contextualisé.
     * 
     * @param cmsCtx
     * @return
     * @throws CMSException
     */
    public static boolean isCurrentDocContextualized(CMSServiceCtx cmsCtx) throws CMSException {
        // boolean isInContextualizedMode = false;

        Window window = (Window) cmsCtx.getRequest().getAttribute("osivia.window");

        if (window != null) {

            // Maximized windows
            if ("1".equals(window.getDeclaredProperty("osivia.cms.contextualization")))
                return true;

            // for spaceMenuBar fragment
            if( BooleanUtils.isTrue((Boolean) cmsCtx.getRequest().getAttribute("osivia.cms.menuBar.forceContextualization")))  {
               return true;
            }
           
            
        }


        return false;

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
