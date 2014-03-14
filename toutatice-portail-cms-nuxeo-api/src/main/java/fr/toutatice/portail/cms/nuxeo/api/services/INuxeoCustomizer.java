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
package fr.toutatice.portail.cms.nuxeo.api.services;


import java.util.Map;

import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.urls.Link;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSHandlerProperties;
import org.osivia.portal.core.cms.CMSItemType;
import org.osivia.portal.core.cms.CMSServiceCtx;


/**
 * Nuxeo customizer interface.
 */
public interface INuxeoCustomizer {

    /**
     * Get CMS player
     * On détermine le player associé à chaque item.
     *
     * @param ctx CMS context
     * @return CMS player portlet with properties
     * @throws Exception
     */
    CMSHandlerProperties getCMSPlayer(CMSServiceCtx ctx) throws Exception;


    /**
     * Create custom link.
     *
     * Ici, on intercepte le traitement CMS lors de la génération du lien
     *
     * C'est le cas pour :
     * - des traitements directements pris en charge par le portlet (ex : download d'un document)
     * - des liens s'ouvrant dans des fenetres externes
     *
     * Si le lien n'est pas pris en charge ici, il sera intégré au traitement CMS
     * standard, cad
     * - lien de type /cms/
     * - passage par la couche player au moment du click sur le lien
     *
     * displayContext : menu, download, fileExplorer, permlink ...
     *
     * @param ctx CMS context
     * @return custom link
     * @throws Exception
     */
    Link createCustomLink(CMSServiceCtx ctx) throws Exception;


    /**
     * Format content menu bar.
     *
     * @param ctx CMS context
     * @throws Exception
     */
    void formatContentMenuBar(CMSServiceCtx ctx) throws Exception;


    /**
     * Get Nuxeo document comments HTML formatted content.
     * 
     * @param cmsContext CMS context
     * @param document Nuxeo document
     * @return comments HTML formatted content
     * @throws CMSException
     */
    String getCommentsHTMLContent(CMSServiceCtx cmsContext, Document document) throws CMSException;


    /**
     * Get document configuration.
     *
     * @param ctx CMS context
     * @param doc Nuxeo document
     * @return document configuration
     * @throws Exception
     */
    Map<String, String> getDocumentConfiguration(CMSServiceCtx ctx, Document doc) throws Exception;


    /**
     * Add publication filter.
     *
     * @param ctx CMS context
     * @param nuxeoRequest Nuxeo request
     * @param requestFilteringPolicy request filtering policy
     * @return edited Nuxeo request
     * @throws Exception
     */
    String addPublicationFilter(CMSServiceCtx ctx, String nuxeoRequest, String requestFilteringPolicy) throws Exception;


    /**
     * Transform HTML content.
     *
     * @param ctx CMS context
     * @param htmlContent HTML content
     * @return transformed HTML content
     * @throws Exception
     */
    String transformHTMLContent(CMSServiceCtx ctx, String htmlContent) throws Exception;


    /**
     * Get CMS item types.
     *
     * @return CMS item types
     */
    Map<String, CMSItemType> getCMSItemTypes();


    /**
     * Get Nuxeo comments service instance.
     *
     * @return Nuxeo comments service instance
     */
    INuxeoCommentsService getNuxeoCommentsService();

}
