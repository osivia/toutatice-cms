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

import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletContext;

import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.Constants;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSItemType;
import org.osivia.portal.core.cms.CMSServiceCtx;

import fr.toutatice.portail.cms.nuxeo.portlets.customizer.DefaultCMSCustomizer;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.WebConfiguratinQueryCommand.WebConfigurationType;
import fr.toutatice.portail.cms.nuxeo.portlets.service.CMSService;

public class CMSItemAdapter {

    private static final String NX_DC_TITLE = "dc:title";
    private static final String NX_DC_DESCRIPTION = "dc:description";
    private static final String NX_DC_CREATOR = "dc:creator";

    protected CMSService CMSService;
    protected DefaultCMSCustomizer customizer;
    protected PortletContext portletCtx;

    public CMSItemAdapter(PortletContext portletCtx, DefaultCMSCustomizer customizer, CMSService cmsService) {
        super();
        CMSService = cmsService;
        this.portletCtx = portletCtx;
        this.customizer = customizer;

    };

    // TODO : remonter dans ICMSService
    public static String computeNavPath(String path) {
        String result = path;
        if (path.endsWith(".proxy"))
            result = result.substring(0, result.length() - 6);
        return result;
    }


    /*
     * Personnalisation des propriétés des éléments d'un CMSItem
     */

    public void adaptItem(CMSServiceCtx ctx, CMSItem item) throws Exception {

        Document doc = (Document) item.getNativeItem();

        Map<String, String> properties = item.getProperties();

        adaptDoc(ctx, doc, properties);
        adaptSEOProperties(ctx, doc, item.getMetaProperties());
    }

    public Map<String, String> adaptDocument(CMSServiceCtx ctx, Document doc) throws Exception {

        Map<String, String> properties = new HashMap<String, String>();

        adaptDoc(ctx, doc, properties);

        return properties;
    }


    public boolean supportsOnlyPortalContextualization(CMSServiceCtx ctx, Document doc) {
    	CMSItemType cmsItemType = this.customizer.getCMSItemTypes().get(doc.getType());
		return ((cmsItemType != null) && (cmsItemType.isForcePortalContextualization()));
    }


    /**
     * Change navigation path in case of special types of documents accessed.
     * 
     * @param ctx cms context
     * @param doc current document
     * @param properties page properties
     * @throws Exception
     */
    public void adaptNavigationProperties(CMSServiceCtx ctx, Document doc, Map<String, String> properties) throws Exception {
        // compute domain path
        String domainPath = WebConfigurationHelper.getDomainPath(ctx);

        if (domainPath != null) {
            // get configs installed in nuxeo
            WebConfiguratinQueryCommand command = new WebConfiguratinQueryCommand(domainPath, WebConfigurationType.CMSNavigationAdapter);
            Documents configs = (Documents) CMSService.executeNuxeoCommand(ctx, command);

            if (configs.size() > 0) {
                for (Document config : configs) {
                    String documentType = config.getProperties().getString(WebConfigurationHelper.CODE);
                    String urlAdapted = config.getProperties().getString(WebConfigurationHelper.CODECOMP);

                    if (doc.getType().equals(documentType)) {
                        String path = computeNavPath(urlAdapted);
                        properties.put("navigationPath", path);
                        break;
                    }
                }
            }
        }

        return;

    }

    private void adaptSEOProperties(CMSServiceCtx ctx, Document doc, Map<String, String> properties) {
        PropertyMap nxProperties = doc.getProperties();
        if (nxProperties.getString(NX_DC_TITLE) != null) {
            properties.put(Constants.HEADER_TITLE, nxProperties.getString(NX_DC_TITLE));
        }

        if (nxProperties.getString(NX_DC_DESCRIPTION) != null) {
            properties.put(Constants.HEADER_META.concat(".description"), nxProperties.getString(NX_DC_DESCRIPTION));
        }

        if (nxProperties.getString(NX_DC_CREATOR) != null) {
            properties.put(Constants.HEADER_META.concat(".author"), nxProperties.getString(NX_DC_CREATOR));
        }

    }

    public void adaptDoc(CMSServiceCtx ctx, Document doc, Map<String, String> properties) throws Exception {


        if (supportsOnlyPortalContextualization(ctx, doc))
            properties.put("supportsOnlyPortalContextualization", "1");


        adaptNavigationProperties(ctx, doc, properties);


    }


}
