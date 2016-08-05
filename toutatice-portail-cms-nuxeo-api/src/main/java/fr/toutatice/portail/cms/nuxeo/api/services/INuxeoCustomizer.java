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


import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;

import javax.servlet.http.HttpSessionListener;

import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.urls.Link;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSHandlerProperties;
import org.osivia.portal.core.cms.CMSItemType;
import org.osivia.portal.core.cms.CMSServiceCtx;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.domain.FragmentType;
import fr.toutatice.portail.cms.nuxeo.api.domain.ListTemplate;
import fr.toutatice.portail.cms.nuxeo.api.forms.FormFilter;


/**
 * Nuxeo customizer interface.
 */
public interface INuxeoCustomizer extends HttpSessionListener {

    /**
     * Get CMS player
     * the player is deduced from the current document but also from the context
     *
     * @param ctx CMS context
     * @return CMS player portlet with properties
     * @throws Exception
     */
    CMSHandlerProperties getCMSPlayer(CMSServiceCtx ctx) throws Exception;


    /**
     * Create custom link.
     *
     * custom links are useful during the cms link generation when the cms controller phase
     * is not adapted
     *
     *  Use cases :
     * - the action for the link is powered directly by the portlet (for example, the download of an attached file)
     * - the link opens an external application
     *
     * if this method returns null, then the standard cms pattern will be applied
     *
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
     * Transform link URL.
     *
     * @param ctx CMS context
     * @param link link URL
     * @return transformed link URL
     * @throws Exception
     */
    String transformLink(CMSServiceCtx ctx, String link);


    /**
     * Get portal link from Nuxeo or absolute URL.
     *
     * @param cmsContext CMS context
     * @param url Nuxeo or absolute URL
     * @return link
     */
    Link getLinkFromNuxeoURL(CMSServiceCtx cmsContext, String url);


    /**
     * Get webID path like /_webid/domain-def-jss/publistatfaq.html
     *
     * @param ctx CMS context
     * @throws Exception
     */
    String getContentWebIdPath(CMSServiceCtx ctx) ;


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

    /**
     * Get the user avatar.
     *
     * @param cmsCtx cms context
     * @param username username
     * @return
     */
    Link getUserAvatar(CMSServiceCtx cmsCtx, String username) throws CMSException;

    /**
     * Refresh the user avatar.
     *
     * @param cmsCtx cms context
     * @param username username
     * @return the timestamp associated with the refresh event
     */
    String refreshUserAvatar(CMSServiceCtx cmsCtx, String username);



    /**
     * Get templates list.
     *
     * @param locale user locale
     * @return template list
     */
    List<ListTemplate> getListTemplates(Locale locale);

    /**
     * get forms filters
     * 
     * @return the filters
     */
    Map<String, FormFilter> getFormsFilters();

    /**
     * Get fragments list.
     *
     * @return fragments list
     */
    List<FragmentType> getFragmentTypes(Locale locale);


    /**
     * Get menu templates.
     *
     * @param locale user locale
     * @return menu templates
     */
    SortedMap<String, String> getMenuTemplates(Locale locale);


    /**
     * Execute Nuxeo command.
     *
     * @param cmsContext CMS context
     * @param command Nuxeo command
     * @return Nuxeo command result
     * @throws CMSException
     */
    Object executeNuxeoCommand(CMSServiceCtx cmsContext, INuxeoCommand command) throws CMSException;

}
