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

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyMap;

import fr.toutatice.portail.cms.nuxeo.portlets.fragment.SliderTemplateModule;
import fr.toutatice.portail.cms.nuxeo.portlets.list.ViewListPortlet;


/**
 * @author david
 *
 */
public class SliderListEditableWindow extends CriteriaListEditableWindow {
    
    public static final String SLIDER_LIST_SCHEMA = "sldlistfgt:sliderListFragment";
    public static final String ALL_DOC_TYPES = "others";
    
    public SliderListEditableWindow(String instancePortlet, String prefixWindow) {
        super(instancePortlet, prefixWindow);
    }
    
    @Override
    protected Object getDocTypes(PropertyMap requestCriteria){
        return requestCriteria.get("docType");
    }
    
    @Override
    protected String getDocTypesCriterion(StringBuffer docTypesCriterion, Object docTypes) {
        if (docTypes != null && docTypes instanceof String) {
            String docType = (String) docTypes;
            if (!ALL_DOC_TYPES.equalsIgnoreCase(docType)) {
                docTypesCriterion.append(" ecm:primaryType = '").append(docType).append("'");
            }
        }
        return docTypesCriterion.toString();
    }
    
    /**
     * {@inheritDoc}}
     */
    @Override
    protected PropertyMap getListSchema(Document doc, PropertyMap fragment){
        return EditableWindowHelper.findSchemaByRefURI(doc, SLIDER_LIST_SCHEMA, fragment.getString("uri"));
    }
    
    /**
     * {@inheritDoc}}
     */
    @Override
    protected Map<String, String> fillDisplayProperties(PropertyMap schema, Map<String, String> properties){
        PropertyMap displayCriteria = (PropertyMap) schema.get("displaySlider");
        properties.put(ViewListPortlet.TEMPLATE_WINDOW_PROPERTY, (String) displayCriteria.get("style"));
        properties.put(ViewListPortlet.RESULTS_LIMIT_WINDOW_PROPERTY, (String) displayCriteria.get("nbItems"));
        properties.put(SliderTemplateModule.SLIDER_TIMER, (String) displayCriteria.get("timer"));
        
        PropertyMap requestCriteria = (PropertyMap) schema.get("requestCriteria");
        properties.put(SliderTemplateModule.SLIDER_DOC_TYPE, StringUtils.lowerCase((String) requestCriteria.getString("docType")));
        
        return properties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> prepareDelete(Document doc, String refURI) {

        List<String> propertiesToRemove = new ArrayList<String>();
        this.prepareDeleteGeneric(propertiesToRemove, doc, refURI);

        Integer indexToRemove = EditableWindowHelper.findIndexByRefURI(doc, SLIDER_LIST_SCHEMA, refURI);
        propertiesToRemove.add(SLIDER_LIST_SCHEMA.concat("/").concat(indexToRemove.toString()));

        return propertiesToRemove;
    }
}
