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
package fr.toutatice.portail.cms.nuxeo.service.editablewindow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.Constants;
import org.osivia.portal.core.constants.InternalConstants;

/**
 * 
 * Service spécifique fragments Liste
 * 
 */
public class ListEditableWindow extends EditableWindow {


    private static final String LISTSCHEMA = "listfgt:listFragment";
	
	private static final String NEWSSCHEMA = "nwslk:links";
	
    public ListEditableWindow(String instancePortlet, String prefixWindow) {
        super(instancePortlet, prefixWindow);

    }


	@Override
	public Map<String, String> fillProps(Document doc, PropertyMap fragment,
			Boolean modeEditionPage) {
		Map<String, String> props = super.fillGenericProps(doc, fragment, modeEditionPage);
		
        PropertyMap mapListe = EditableWindowHelper.findSchemaByRefURI(doc, LISTSCHEMA, fragment.getString("uri"));
		
		props.put("osivia.nuxeoRequest", mapListe.getString("request"));
		props.put("osivia.requestInterpretor", "beanShell");
        props.put(Constants.WINDOW_PROP_SCOPE, null);
		props.put("osivia.displayNuxeoRequest", null);
		props.put("osivia.cms.requestFilteringPolicy", null);
		props.put("osivia.cms.hideMetaDatas", null);
		
        if (mapListe.getBoolean("allContents")) {
            props.put(InternalConstants.PORTAL_PROP_NAME_CMS_REQUEST_FILTERING_POLICY, InternalConstants.PORTAL_CMS_REQUEST_FILTERING_POLICY_GLOBAL);
            props.put(Constants.WINDOW_PROP_VERSION, "1");
        } else {
            props.put(InternalConstants.PORTAL_PROP_NAME_CMS_REQUEST_FILTERING_POLICY, null);
            props.put(Constants.WINDOW_PROP_VERSION, null);
        }

		props.put("osivia.cms.style", mapListe.getString("view"));
		props.put("osivia.cms.pageSize", mapListe.getString("pageSize"));
		props.put("osivia.cms.pageSizeMax", mapListe.getString("pageSizeMax"));
		props.put("osivia.cms.maxItems", mapListe.getString("maxItems"));
		
		props.put("osivia.permaLinkRef", null);
		props.put("osivia.rssLinkRef", null);
		props.put("osivia.rssTitle", null);
		
		props.put("osivia.cms.feed", mapListe.getString("feed"));
		
		// Cas particulier des feed
		if("true".equals(mapListe.getString("feed"))) {
			fillFeedProps(doc, fragment, props, mapListe);
		}
		
		return props;
		
	}

	private void fillFeedProps(Document doc, PropertyMap fragment,
			Map<String, String> props, PropertyMap mapListe) {
		
		// On récupère la liste des documents
		PropertyList newslinks = doc.getProperties().getList(NEWSSCHEMA);

		int nbElements = 0;
		Integer i = 0;
		for (Object news : newslinks.list()) {

			if (news instanceof PropertyMap) {
				PropertyMap mapNews = (PropertyMap) news;
				if (mapNews.getString("refURI").equals(fragment.get("uri"))) {
					props.put("osivia.cms.news." + i.toString() + ".docURI",
							mapNews.getString("documentURI"));
					props.put("osivia.cms.news." + i.toString() + ".order",
							mapNews.getString("order"));
					i++;
				}
			}
			
		}
		props.put("osivia.cms.news.size", Integer.toString(i));

	}

	@Override
	public List<String> prepareDelete(Document doc, String refURI) {
		List<String> propertiesToRemove = new ArrayList<String>();
		
		prepareDeleteGeneric(propertiesToRemove, doc, refURI);
		
        Integer indexToRemove = EditableWindowHelper.findIndexByRefURI(doc, LISTSCHEMA, refURI);
		
		propertiesToRemove.add(LISTSCHEMA.concat("/").concat(indexToRemove.toString()));
		
		// TODO supprimer les feed
		
		return propertiesToRemove;
	}


}
