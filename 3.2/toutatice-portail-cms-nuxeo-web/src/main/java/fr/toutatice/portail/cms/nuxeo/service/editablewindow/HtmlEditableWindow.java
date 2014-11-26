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
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.Constants;

/**
 * 
 * Service spécifique fragments HTML
 * 
 */
public class HtmlEditableWindow extends EditableWindow {


    public static final String HTMLSCHEMA = "htmlfgt:htmlFragment";
	
    public HtmlEditableWindow(String instancePortlet, String prefixWindow) {
        super(instancePortlet, prefixWindow);

    }
	
	@Override
	public Map<String, String> fillProps(Document doc, PropertyMap fragment, Boolean modeEditionPage) {

        Map<String, String> propsFilled = super.fillGenericProps(doc, fragment, modeEditionPage);
        propsFilled.put(Constants.WINDOW_PROP_URI, doc.getPath());

        return propsFilled;
	}


	@Override
	public List<String> prepareDelete(Document doc, String refURI) {
		
		List<String> propertiesToRemove = new ArrayList<String>();
		
		prepareDeleteGeneric(propertiesToRemove, doc, refURI);
		

        Integer indexToRemove = EditableWindowHelper.findIndexByRefURI(doc, HTMLSCHEMA, refURI);
		
		propertiesToRemove.add(HTMLSCHEMA.concat("/").concat(indexToRemove.toString()));
		
		return propertiesToRemove;
	}
}
