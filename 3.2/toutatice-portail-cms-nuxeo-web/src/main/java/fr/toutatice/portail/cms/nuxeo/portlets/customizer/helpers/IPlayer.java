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

import java.util.Map;

import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.core.cms.CMSHandlerProperties;
import org.osivia.portal.core.cms.CMSServiceCtx;

/**
 * Gère l'affichage d'un document dans une portlet maximized.
 * 
 */
public interface IPlayer {

    /**
     * play in the portlet.
     * 
     * @param ctx cms context
     * @param doc current nuxeo document
     * @param windowProperties the properties initialized
     * @return portlet and properties
     * @throws Exception
     */
    public CMSHandlerProperties play(CMSServiceCtx ctx, Document doc) throws Exception;

    /**
     * play in the portlet.
     * 
     * @param ctx cms context
     * @param doc current nuxeo document
     * @param windowProperties the properties initialized
     * @return portlet and properties
     * @throws Exception
     */
    public CMSHandlerProperties play(CMSServiceCtx ctx, Document doc, Map<String, String> windowProperties) throws Exception;
}
