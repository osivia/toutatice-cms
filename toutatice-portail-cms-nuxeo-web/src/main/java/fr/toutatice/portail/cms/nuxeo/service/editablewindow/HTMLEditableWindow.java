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

import fr.toutatice.portail.cms.nuxeo.portlets.fragment.PropertyFragmentModule;
import fr.toutatice.portail.cms.nuxeo.portlets.fragment.ViewFragmentPortlet;

/**
 * HTML property editable window.
 *
 * @see EditableWindow
 */
public class HTMLEditableWindow extends EditableWindow {

    /** HTML property fragment schema. */
    public static final String HTML_SCHEMA = "htmlfgt:htmlFragment";


    /**
     * Constructor.
     *
     * @param instance portlet instance
     * @param prefix window prefix
     */
    public HTMLEditableWindow(String instance, String prefix) {
        super(instance, prefix);
    }


    /**
     * {@inheritDoc}
     */
	@Override
	public Map<String, String> fillProps(Document document, PropertyMap fragment, Boolean modeEditionPage) {
        // Properties
        Map<String, String> properties = super.fillGenericProps(document, fragment, modeEditionPage);
        properties.put(ViewFragmentPortlet.FRAGMENT_TYPE_ID_WINDOW_PROPERTY, PropertyFragmentModule.HTML_ID);
        properties.put(PropertyFragmentModule.NUXEO_PATH_WINDOW_PROPERTY, document.getPath());
        properties.put(PropertyFragmentModule.PROPERTY_NAME_WINDOW_PROPERTY, HTML_SCHEMA);

        return properties;
	}


    /**
     * {@inheritDoc}
     */
	@Override
    public List<String> prepareDelete(Document document, String refURI) {
        // Properties
        List<String> properties = new ArrayList<String>();

        this.prepareDeleteGeneric(properties, document, refURI);

        // Index
        Integer index = EditableWindowHelper.findIndexByRefURI(document, HTML_SCHEMA, refURI);
        properties.add(HTML_SCHEMA.concat("/").concat(index.toString()));

        return properties;
	}

}
