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
package fr.toutatice.portail.cms.nuxeo.service.editablewindow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.core.constants.InternalConstants;

import fr.toutatice.portail.cms.nuxeo.portlets.list.ViewListPortlet;

/**
 * List fragment.
 *
 * @see EditableWindow
 */
public class ListEditableWindow extends EditableWindow {

    /** List schema. */
    private static final String LIST_SCHEMA = "listfgt:listFragment";

    // private static final String NEWS_SCHEMA = "nwslk:links";


    /**
     * Constructor.
     *
     * @param instancePortlet portlet instance
     * @param prefixWindow window prefix
     */
    public ListEditableWindow(String instancePortlet, String prefixWindow) {
        super(instancePortlet, prefixWindow);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> fillProps(Document doc, PropertyMap fragment, Boolean modeEditionPage) {
        Map<String, String> properties = super.fillGenericProps(doc, fragment, modeEditionPage);

        PropertyMap mapListe = EditableWindowHelper.findSchemaByRefURI(doc, LIST_SCHEMA, fragment.getString("uri"));

        properties.put(ViewListPortlet.NUXEO_REQUEST_WINDOW_PROPERTY, mapListe.getString("request"));
        properties.put(ViewListPortlet.BEAN_SHELL_WINDOW_PROPERTY, String.valueOf(true));
        properties.put(ViewListPortlet.SCOPE_WINDOW_PROPERTY, null);
        properties.put(ViewListPortlet.METADATA_WINDOW_PROPERTY, "1");
        properties.put(ViewListPortlet.NUXEO_REQUEST_DISPLAY_WINDOW_PROPERTY, String.valueOf(false));

        if (mapListe.getBoolean("allContents")) {
            properties.put(ViewListPortlet.CONTENT_FILTER_WINDOW_PROPERTY, InternalConstants.PORTAL_CMS_REQUEST_FILTERING_POLICY_GLOBAL);
            properties.put(ViewListPortlet.VERSION_WINDOW_PROPERTY, "1");
        } else {
            properties.put(ViewListPortlet.CONTENT_FILTER_WINDOW_PROPERTY, null);
            properties.put(ViewListPortlet.VERSION_WINDOW_PROPERTY, null);
        }

        properties.put(ViewListPortlet.TEMPLATE_WINDOW_PROPERTY, mapListe.getString("view"));
        properties.put(ViewListPortlet.NORMAL_PAGINATION_WINDOW_PROPERTY, mapListe.getString("pageSize"));
        properties.put(ViewListPortlet.MAXIMIZED_PAGINATION_WINDOW_PROPERTY, mapListe.getString("pageSizeMax"));
        properties.put(ViewListPortlet.RESULTS_LIMIT_WINDOW_PROPERTY, mapListe.getString("maxItems"));

        properties.put(ViewListPortlet.PERMALINK_REFERENCE_WINDOW_PROPERTY, null);
        properties.put(ViewListPortlet.RSS_REFERENCE_WINDOW_PROPERTY, null);
        properties.put(ViewListPortlet.RSS_TITLE_WINDOW_PROPERTY, null);


        // // Cas particulier des feed
        // properties.put("osivia.cms.feed", mapListe.getString("feed"));
        // if ("true".equals(mapListe.getString("feed"))) {
        // this.fillFeedProps(doc, fragment, properties, mapListe);
        // }


        return properties;
    }


    // private void fillFeedProps(Document doc, PropertyMap fragment, Map<String, String> props, PropertyMap mapListe) {
    //
    // // On récupère la liste des documents
    // PropertyList newslinks = doc.getProperties().getList(NEWS_SCHEMA);
    //
    // int nbElements = 0;
    // Integer i = 0;
    // for (Object news : newslinks.list()) {
    //
    // if (news instanceof PropertyMap) {
    // PropertyMap mapNews = (PropertyMap) news;
    // if (mapNews.getString("refURI").equals(fragment.get("uri"))) {
    // props.put("osivia.cms.news." + i.toString() + ".docURI", mapNews.getString("documentURI"));
    // props.put("osivia.cms.news." + i.toString() + ".order", mapNews.getString("order"));
    // i++;
    // }
    // }
    //
    // }
    // props.put("osivia.cms.news.size", Integer.toString(i));
    // }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> prepareDelete(Document doc, String refURI) {
        List<String> propertiesToRemove = new ArrayList<String>();

        this.prepareDeleteGeneric(propertiesToRemove, doc, refURI);

        Integer indexToRemove = EditableWindowHelper.findIndexByRefURI(doc, LIST_SCHEMA, refURI);

        propertiesToRemove.add(LIST_SCHEMA.concat("/").concat(indexToRemove.toString()));

        // TODO supprimer les feed

        return propertiesToRemove;
    }

}
