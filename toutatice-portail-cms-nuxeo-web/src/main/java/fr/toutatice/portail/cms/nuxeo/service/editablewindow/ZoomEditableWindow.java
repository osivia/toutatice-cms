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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.Constants;

import fr.toutatice.portail.cms.nuxeo.portlets.fragment.ZoomFragmentModule;

/**
 * 
 * Service spécifique fragments HTML
 * 
 */
public class ZoomEditableWindow extends EditableWindow {

    /** sch for fragment. */
    public static final String ZOOM_SCHEMA = "zfgt:zoomFragment";

    /** sch for each link of the list . */
    //public static final String ZOOM_LINKS = "zl:zoomLink";
	
    public ZoomEditableWindow(String instancePortlet, String prefixWindow) {
        super(instancePortlet, prefixWindow);

    }
	
	@Override
	public Map<String, String> fillProps(Document doc, PropertyMap fragment, Boolean modeEditionPage) {

        Map<String, String> propsFilled = super.fillGenericProps(doc, fragment, modeEditionPage);
        propsFilled.put(Constants.WINDOW_PROP_URI, doc.getPath());

        PropertyMap mapListe = EditableWindowHelper.findSchemaByRefURI(doc, ZOOM_SCHEMA, fragment.getString("uri"));

        propsFilled.put("osivia.cms.style", mapListe.getString("view"));
        propsFilled.put("osivia.fragmentTypeId", ZoomFragmentModule.ID);
        //propsFilled.put("osivia.propertyName", ZOOM_LINKS);

        return propsFilled;
	}


	@Override
	public List<String> prepareDelete(Document doc, String refURI) {
		
		List<String> propertiesToRemove = new ArrayList<String>();
		
		prepareDeleteGeneric(propertiesToRemove, doc, refURI);
		

        Integer findIndexByRefURI = EditableWindowHelper.findIndexByRefURI(doc, ZOOM_SCHEMA, refURI);
        propertiesToRemove.add(ZOOM_SCHEMA.concat("/").concat(findIndexByRefURI.toString()));
		
//        List<Integer> findIndexesByRefURI = EditableWindowHelper.findIndexesByRefURI(doc, ZOOM_LINKS, refURI);
//
//        for (Integer indexToRemove : findIndexesByRefURI) {
//            propertiesToRemove.add(ZOOM_LINKS.concat("/").concat(indexToRemove.toString()));
//        }

        // Bug automation, supprimer la liste de propriétés par son dernier élément, puis l'avant dernier, etc.
        // sinon décalage des n° d'index dans les propriétés
        Collections.reverse(propertiesToRemove);

		return propertiesToRemove;
	}
}
