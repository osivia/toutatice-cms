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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.cms.DocumentType;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSServiceCtx;

import fr.toutatice.portail.cms.nuxeo.api.domain.ICmsItemAdapterModule;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.CustomizationPluginMgr;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.DefaultCMSCustomizer;

/**
 * CMS item adapter.
 */
public class CMSItemAdapter {

    private static final String NX_DC_TITLE = "dc:title";
    private static final String NX_DC_DESCRIPTION = "dc:description";
    private static final String NX_DC_CREATOR = "dc:creator";
    private static final String NX_TTC_KEYWORDS = "ttc:keywords";


    /** CMS customizer. */
    private final DefaultCMSCustomizer customizer;


    /**
     * Constructor.
     *
     * @param cmsService CMS service
     */
    public CMSItemAdapter(DefaultCMSCustomizer customizer) {
        super();
        this.customizer = customizer;
    };


    /**
     * Compute nav path.
     * TODO : remonter dans ICMSService
     *
     * @param path path
     * @return nav path
     */
    public static String computeNavPath(String path) {
        String result = path;
        if (path.endsWith(".proxy")) {
            result = result.substring(0, result.length() - 6);
        }
        return result;
    }


    /**
     * @param doc
     * @param facet
     * @return true if document has the given facet.
     */
    public static boolean docHasFacet(Document doc, String facet) {
        boolean has = false;
        PropertyList facets = doc.getFacets();
        if (facets != null) {
            List<Object> facetsList = facets.list();
            has = facetsList.contains(facet);
        }
        return has;
    }


    public void adaptItem(CMSServiceCtx ctx, CMSItem item) throws Exception {
        Document doc = (Document) item.getNativeItem();
        Map<String, String> properties = item.getProperties();

        this.adaptDoc(ctx, doc, properties);
        this.adaptSEOProperties(ctx, doc, item.getMetaProperties());
    }


    public Map<String, String> adaptDocument(CMSServiceCtx ctx, Document doc) throws Exception {
        Map<String, String> properties = new HashMap<String, String>();
        this.adaptDoc(ctx, doc, properties);
        return properties;
    }


    public boolean supportsOnlyPortalContextualization(CMSServiceCtx ctx, Document doc) {
        DocumentType cmsItemType = this.customizer.getCMSItemTypes().get(doc.getType());
        return ((cmsItemType != null) && (cmsItemType.isForcePortalContextualization()));
    }


    /**
     * Change navigation path in case of special types of documents accessed.
     *
     * @param cmsContext cms context
     * @param document current document
     * @param properties page properties
     * @throws Exception
     */
    public void adaptNavigationProperties(CMSServiceCtx cmsContext, Document document, Map<String, String> properties) throws Exception {
        // Plugin manager
        CustomizationPluginMgr pluginManager = this.customizer.getPluginMgr();
        // CMS item adapter modules
        List<ICmsItemAdapterModule> modules = pluginManager.customizeCmsItemAdapterModules();

        for (ICmsItemAdapterModule module : modules) {
            // Adapt navigation properties
            module.adaptNavigationProperties(cmsContext, document, properties);
        }
    }


    private void adaptSEOProperties(CMSServiceCtx ctx, Document doc, Map<String, String> properties) {
        PropertyMap nxProperties = doc.getProperties();
        if (nxProperties.getString(NX_DC_TITLE) != null) {
            properties.put(Constants.HEADER_TITLE, nxProperties.getString(NX_DC_TITLE));
        }

        if (StringUtils.isNotBlank(nxProperties.getString(NX_DC_DESCRIPTION))) {
            properties.put(Constants.HEADER_META.concat(".description"), nxProperties.getString(NX_DC_DESCRIPTION));
        }

        if (StringUtils.isNotBlank(nxProperties.getString(NX_DC_CREATOR))) {
            properties.put(Constants.HEADER_META.concat(".author"), nxProperties.getString(NX_DC_CREATOR));
        }

        // TODO dc:subjects ??
        if (nxProperties.getList(NX_TTC_KEYWORDS) != null) {
            String keywords = StringUtils.join(nxProperties.getList(NX_TTC_KEYWORDS).list(), ",");

            if (StringUtils.isNotBlank(keywords)) {
                properties.put(Constants.HEADER_META.concat(".keywords"), keywords);
            }

        }
    }


    public void adaptDoc(CMSServiceCtx ctx, Document doc, Map<String, String> properties) throws Exception {
        if (this.supportsOnlyPortalContextualization(ctx, doc)) {
            properties.put("supportsOnlyPortalContextualization", "1");
        }

        this.adaptNavigationProperties(ctx, doc, properties);
    }

}
