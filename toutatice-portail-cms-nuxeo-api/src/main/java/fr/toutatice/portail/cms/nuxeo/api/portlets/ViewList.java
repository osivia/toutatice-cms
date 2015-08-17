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
package fr.toutatice.portail.cms.nuxeo.api.portlets;

import fr.toutatice.portail.cms.nuxeo.api.CMSPortlet;

/**
 * Public descriptor of the portlet list
 * @author lbillon
 *
 */
public abstract class ViewList extends CMSPortlet {

    /** Nuxeo request window property name. */
    public static final String NUXEO_REQUEST_WINDOW_PROPERTY = "osivia.nuxeoRequest";
    /** Bean Shell interpretation indicator window property name. */
    public static final String BEAN_SHELL_WINDOW_PROPERTY = "osivia.beanShell";
    /** Use of ElasticSearch indicator window property */
    public static final String USE_ES_WINDOW_PROPERTY = "osivia.useES";
    /** Version window property name. */
    public static final String VERSION_WINDOW_PROPERTY = "osivia.cms.displayLiveVersion";
    /** Content filter window property name. */
    public static final String CONTENT_FILTER_WINDOW_PROPERTY = "osivia.cms.requestFilteringPolicy";
    /** Scope window property name. */
    public static final String SCOPE_WINDOW_PROPERTY = "osivia.cms.scope";
    /** Hide metadata indicator window property name. */
    public static final String METADATA_WINDOW_PROPERTY = "osivia.cms.hideMetaDatas";
    /** Nuxeo request display indicator window property name. */
    public static final String NUXEO_REQUEST_DISPLAY_WINDOW_PROPERTY = "osivia.displayNuxeoRequest";
    /** Results limit window property name. */
    public static final String RESULTS_LIMIT_WINDOW_PROPERTY = "osivia.cms.maxItems";
    /** Normal view pagination window property name. */
    public static final String NORMAL_PAGINATION_WINDOW_PROPERTY = "osivia.cms.pageSize";
    /** Maximized view pagination window property name. */
    public static final String MAXIMIZED_PAGINATION_WINDOW_PROPERTY = "osivia.cms.pageSizeMax";
    /** Template window property name. */
    public static final String TEMPLATE_WINDOW_PROPERTY = "osivia.cms.style";
    /** Permalink reference window property name. */
    public static final String PERMALINK_REFERENCE_WINDOW_PROPERTY = "osivia.permaLinkRef";
    /** RSS reference window property name. */
    public static final String RSS_REFERENCE_WINDOW_PROPERTY = "osivia.rssLinkRef";
    /** RSS title window property name. */
    public static final String RSS_TITLE_WINDOW_PROPERTY = "osivia.rssTitle";
    /** Creation parent container path window property name. */
    public static final String CREATION_PARENT_PATH_WINDOW_PROPERTY = "osivia.createParentPath";
    /** Creation content type window property name. */
    public static final String CREATION_CONTENT_TYPE_WINDOW_PROPERTY = "osivia.createDocType";
}
