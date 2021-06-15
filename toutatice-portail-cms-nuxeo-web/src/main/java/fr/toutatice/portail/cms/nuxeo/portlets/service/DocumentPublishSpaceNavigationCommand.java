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
package fr.toutatice.portail.cms.nuxeo.portlets.service;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.osivia.portal.api.cms.Symlink;
import org.osivia.portal.api.cms.Symlinks;
import org.osivia.portal.api.cms.UniversalID;
import org.osivia.portal.api.cms.VirtualNavigationUtils;

import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.NavigationItem;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilter;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoQueryFilterContext;
import fr.toutatice.portail.cms.nuxeo.api.domain.INavigationAdapterModule;
import fr.toutatice.portail.cms.nuxeo.portlets.document.helpers.DocumentHelper;


/**
 * Return all the navigation items.
 *
 * @author jeanseb
 * @see INuxeoCommand
 */
public class DocumentPublishSpaceNavigationCommand implements INuxeoCommand {

    /** Default navigation schemas. */
    private static final String DEFAULT_NAVIGATION_SCHEMAS = "dublincore, common, toutatice, regions, record";


    /** CMS context. */
    private final CMSServiceCtx cmsContext;
    /** Space config. */
    private final CMSItem spaceConfig;
    /** Live version indicator. */
    private final boolean live;
    /** Navigation adapters. */
    private final List<INavigationAdapterModule> navigationAdapters;


    /**
     * Constructor.
     *
     * @param cmsContext CMS context
     * @param spaceConfig space config.
     * @param forceLiveVersion force live version indicator
     * @param navigationAdapters navigation adapters
     */
    public DocumentPublishSpaceNavigationCommand(CMSServiceCtx cmsContext, CMSItem spaceConfig, boolean forceLiveVersion,
            List<INavigationAdapterModule> navigationAdapters) {
        super();
        this.cmsContext = cmsContext;
        this.spaceConfig = spaceConfig;
        this.live = forceLiveVersion || "1".equals(this.spaceConfig.getProperties().get("displayLiveVersion"));
        this.navigationAdapters = navigationAdapters;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Session session) throws Exception {
        // Base path
        String basePath = this.spaceConfig.getNavigationPath();

        // Symlinks
        Symlinks symlinks;
        if (CollectionUtils.isEmpty(this.navigationAdapters)) {
            symlinks = null;
        } else {
            symlinks = new Symlinks();

            // Portal controller context
            PortalControllerContext portalControllerContext =this.cmsContext.getPortalControllerContext();

            // Navigation adapters
            for (INavigationAdapterModule navigationAdapter : this.navigationAdapters) {
                Symlinks adapterSymlinks = navigationAdapter.getSymlinks(portalControllerContext);
                if (adapterSymlinks != null) {
                    symlinks.addAll(adapterSymlinks);
                }
            }
        }

        // Extra navigation paths
        Set<String> extraPaths;
        if ((symlinks == null) || CollectionUtils.isEmpty(symlinks.getPaths())) {
            extraPaths = new HashSet<>(0);
        } else {
            extraPaths = symlinks.getPaths();
        }


        // Operation request
        OperationRequest operationRequest = session.newRequest("Document.Query");

        // Nuxeo query clause
        StringBuilder clause = new StringBuilder();
        clause.append("(ecm:path STARTSWITH '");
        clause.append(basePath);
        if (!this.live) {
            clause.append("' OR ecm:path = '");
            clause.append(basePath);
            clause.append(".proxy");
        }
        clause.append("') AND (ecm:mixinType = 'Folderish' OR ttc:showInMenu = 1)");

        // Nuxeo query filter
        NuxeoQueryFilterContext filter;
        if (this.live) {
            filter = NuxeoQueryFilterContext.CONTEXT_LIVE;
        } else {
            filter = NuxeoQueryFilterContext.CONTEXT_DEFAULT;
        }

        // Apply Nuxeo query clause filter
        String filteredClause = NuxeoQueryFilter.addPublicationFilter(filter, clause.toString());
        if (CollectionUtils.isNotEmpty(extraPaths)) {
            // Update clause with extra navigation paths;
            filteredClause = this.updateClause(basePath, filteredClause, extraPaths);
        }


        // Nuxeo query
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM Document WHERE ");
        query.append(filteredClause);
        query.append(" ORDER BY ecm:pos");
        operationRequest.set("query", query.toString());

        // Navigation schemas
        StringBuilder navigationSchemas = new StringBuilder();
        navigationSchemas.append(DEFAULT_NAVIGATION_SCHEMAS);
        String extraNavigationSchemas = System.getProperty("nuxeo.navigationSchemas");
        if (StringUtils.isNotBlank(extraNavigationSchemas)) {
            navigationSchemas.append(", ");
            navigationSchemas.append(extraNavigationSchemas);
        }
        operationRequest.setHeader(Constants.HEADER_NX_SCHEMAS, navigationSchemas.toString());

        // Operation execution
        Documents children = (Documents) operationRequest.execute();

        // Navigation items
        Map<String, NavigationItem> navigationItems = new LinkedHashMap<>(children.size());

        for (Document child : children) {
            // Path
            String path = child.getPath();

            // Navigation path
            String navigationPath;
            if (StringUtils.startsWith(path, basePath)) {
                navigationPath = path;
            } else {
                // Symlink
                Symlink symlink = null;

                if ((symlinks != null) && CollectionUtils.isNotEmpty(symlinks.getLinks())) {
                    Iterator<Symlink> iterator = symlinks.getLinks().iterator();
                    while ((symlink == null) && iterator.hasNext()) {
                        Symlink link = iterator.next();

                        if (StringUtils.startsWith(path, link.getTargetPath())) {
                            symlink = link;
                        }
                    }
                }

                if (symlink == null) {
                    // FIXME
                    navigationPath = basePath + "/_" + StringUtils.substringAfterLast(path, "/");
                } else {
                    if( "Staple".equals(child.getType()))   
                        // This is a virtual staple
                        // The link must be a cms link that integrates navigation + webId
                        navigationPath = VirtualNavigationUtils.adaptPath(symlink.getNavigationPath(), (String) child.getProperties().get(DocumentsMetadataImpl.WEB_ID_PROPERTY));
                     else    
                        navigationPath = symlink.getNavigationPath() + "/_" + StringUtils.substringAfterLast(path, "/");                        
                }
            }
            navigationPath = DocumentHelper.computeNavPath(navigationPath);

            // Navigation item
            NavigationItem navigationItem = navigationItems.get(navigationPath);
            if (navigationItem == null) {
                navigationItem = new NavigationItem();
                navigationItems.put(navigationPath, navigationItem);
            }
            navigationItem.setMainDoc(child);
            navigationItem.setPath(navigationPath);
        }


        // Update navigation item children
        for (Entry<String, NavigationItem> entry : navigationItems.entrySet()) {
            String navigationPath = entry.getKey();
            String parentPath = StringUtils.substringBeforeLast(navigationPath, "/");
            if (StringUtils.startsWith(navigationPath, basePath)) {
                // Navigation item
                NavigationItem navigationItem = entry.getValue();

                // Navigation item parent
                NavigationItem parent = navigationItems.get(parentPath);
                if (parent != null) {
                    parent.getChildren().add(navigationItem);
                }
            }
        }

        return navigationItems;
    }


    /**
     * Update Nuxeo query clause with extra navigation paths.
     * 
     * @param basePath base path
     * @param clause Nuxeo query clause
     * @param extraPaths extra navigation paths
     * @return updated Nuxeo query clause
     * @throws CMSException
     */
    private String updateClause(String basePath, String clause, Set<String> extraPaths) throws CMSException {
        // Extended Nuxeo query clause
        StringBuilder extendedClause = new StringBuilder();

        boolean first = true;
        for (String extraPath : extraPaths) {
            if (first) {
                first = false;
            } else {
                extendedClause.append(" OR ");
            }

            extendedClause.append("ecm:path STARTSWITH '");
            extendedClause.append(extraPath);
            extendedClause.append("'");
        }

        // Filtered extended Nuxeo query clause
        String filteredExtendedClause = NuxeoQueryFilter.addPublicationFilter(NuxeoQueryFilterContext.CONTEXT_LIVE, extendedClause.toString());

        // Update filtered Nuxeo query clause
        StringBuilder updatedFilteredClause = new StringBuilder();
        updatedFilteredClause.append("(");
        updatedFilteredClause.append(clause);
        updatedFilteredClause.append(") OR (");
        updatedFilteredClause.append(filteredExtendedClause);
        updatedFilteredClause.append(")");

        return updatedFilteredClause.toString();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getSimpleName());
        builder.append("|");
        builder.append(this.spaceConfig.getCmsPath());
        builder.append("|");
        builder.append(this.live);
        return builder.toString();
    }



}
