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


import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.servlet.http.HttpSessionListener;

import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.cms.DocumentContext;
import org.osivia.portal.api.cms.DocumentType;
import org.osivia.portal.api.ecm.EcmCommand;
import org.osivia.portal.api.player.Player;
import org.osivia.portal.api.urls.Link;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSServiceCtx;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.domain.EditableWindow;
import fr.toutatice.portail.cms.nuxeo.api.domain.FragmentType;
import fr.toutatice.portail.cms.nuxeo.api.domain.ListTemplate;


/**
 * Nuxeo customizer interface.
 */
public interface INuxeoCustomizer extends HttpSessionListener {

    /**
     * Get CMS player
     * the player is deduced from the current document but also from the context.
     *
     * @param ctx CMS context
     * @return CMS player portlet with properties
     * @throws Exception the exception
     */
    Player getCMSPlayer(CMSServiceCtx ctx) throws Exception;

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
     * @throws Exception the exception
     */
    Link createCustomLink(CMSServiceCtx ctx) throws Exception;


    /**
     * Format content menu bar.
     *
     * @param ctx CMS context
     * @throws Exception the exception
     */
    void formatContentMenuBar(CMSServiceCtx ctx) throws Exception;


    /**
     * Get Nuxeo document comments HTML formatted content.
     *
     * @param cmsContext CMS context
     * @param document Nuxeo document
     * @return comments HTML formatted content
     * @throws CMSException the CMS exception
     */
    String getCommentsHTMLContent(CMSServiceCtx cmsContext, Document document) throws CMSException;


    /**
     * Get document configuration.
     *
     * @param ctx CMS context
     * @param doc Nuxeo document
     * @return document configuration
     * @throws Exception the exception
     */
    Map<String, String> getDocumentConfiguration(CMSServiceCtx ctx, Document doc) throws Exception;


    /**
     * Add publication filter.
     *
     * @param ctx CMS context
     * @param nuxeoRequest Nuxeo request
     * @param requestFilteringPolicy request filtering policy
     * @return edited Nuxeo request
     * @throws Exception the exception
     */
    String addPublicationFilter(CMSServiceCtx ctx, String nuxeoRequest, String requestFilteringPolicy) throws Exception;
    
    /**
     * Add search filter.
     *
     * @param ctx CMS context
     * @param nuxeoRequest Nuxeo request
     * @param requestFilteringPolicy request filtering policy
     * @return edited Nuxeo request
     * @throws Exception the exception
     */
    String addSearchFilter(CMSServiceCtx ctx, String nuxeoRequest, String requestFilteringPolicy) throws Exception;

    /**
     * Transform HTML content.
     *
     * @param ctx CMS context
     * @param htmlContent HTML content
     * @return transformed HTML content
     * @throws Exception the exception
     */
    String transformHTMLContent(CMSServiceCtx ctx, String htmlContent) throws Exception;


    /**
     * Transform link URL.
     *
     * @param ctx CMS context
     * @param link link URL
     * @return transformed link URL
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
     * @return the content web id path
     */
    String getContentWebIdPath(CMSServiceCtx ctx) ;


    /**
     * Get CMS item types.
     *
     * @return CMS item types
     */
    Map<String, DocumentType> getCMSItemTypes();


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
     * @return the user avatar
     * @throws CMSException the CMS exception
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
     * Get fragments list.
     *
     * @param locale the locale
     * @return fragments list
     */
    Map<String, FragmentType> getFragmentTypes(Locale locale);


    /**
     * Get editable window list.
     *
     * @param locale the locale
     * @return editable winsow list
     */
    Map<String,EditableWindow> getEditableWindows(Locale locale);
    
    /**
     * Get menu templates.
     *
     * @param locale user locale
     * @return menu templates
     */
    SortedMap<String, String> getMenuTemplates(Locale locale);
    
    
    /**
     * Customize jsp new.
     * 
     * @param name the name
     * @param portletContext the portlet context
     * @param request the request
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    String getJSPName(String name, PortletContext portletContext, PortletRequest request) throws CMSException ;


    /**
     * Execute Nuxeo command.
     *
     * @param cmsContext CMS context
     * @param command Nuxeo command
     * @return Nuxeo command result
     * @throws CMSException the CMS exception
     */
    Object executeNuxeoCommand(CMSServiceCtx cmsContext, INuxeoCommand command) throws CMSException;



    
//    /**
//     * Creates the folder request.
//     *
//     * @param ctx the ctx
//     * @param ordered the ordered
//     * @return the string
//     * @throws CMSException the CMS exception
//     */
//    String createFolderRequest(DocumentContext<Document> docCtx, boolean ordered);
    
    
    
    /**
     * Define ECM commands.
     *
     * @return the ecm commands
     */
    Map<String, EcmCommand> getEcmCommands();

    /**
     * Gets the CMS file browser.
     *
     * @param docCtx the cms context
     * @return the CMS file browser
     */
	Player getCMSFileBrowser(DocumentContext<Document> docCtx);


}
