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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.Constants;

import fr.toutatice.portail.cms.nuxeo.portlets.list.ViewListPortlet;

/**
 * @author david chevrier
 *
 */
public class CriteriaListEditableWindow extends EditableWindow {

    public static final String CRITERIA_LIST_SCHEMA = "crtlistfgt:criteriaListFragment";

    protected static final String CRITERIA_SEPARATOR = " and ";
    protected static final String CRITERION_LIST_SEPARATOR = ",";
    protected static final String CRITERION_EQUAL = " = ";
    protected static final String QUOTE = "'";

    public CriteriaListEditableWindow(String instancePortlet, String prefixWindow) {
        super(instancePortlet, prefixWindow);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> fillProps(Document doc, PropertyMap fragment, Boolean modeEditionPage) {
        Map<String, String> properties = super.fillGenericProps(doc, fragment, modeEditionPage);

        PropertyMap schema = getListSchema(doc, fragment);

        /* Request */
        PropertyMap requestCriteria = (PropertyMap) schema.get("requestCriteria");
        String request = buildRequest(requestCriteria);
        properties.put(ViewListPortlet.NUXEO_REQUEST_WINDOW_PROPERTY, request);
        properties.put(Constants.WINDOW_PROP_VERSION, "__inherited");

        /* Display */
        properties.putAll(fillDisplayProperties(schema, new HashMap<String, String>()));

        /* Technical */
        properties.put(ViewListPortlet.BEAN_SHELL_WINDOW_PROPERTY, String.valueOf(false));
        properties.put(ViewListPortlet.SCOPE_WINDOW_PROPERTY, null);
        properties.put(ViewListPortlet.METADATA_WINDOW_PROPERTY, "1");
        properties.put(ViewListPortlet.NUXEO_REQUEST_DISPLAY_WINDOW_PROPERTY, String.valueOf(false));

        properties.put(ViewListPortlet.CONTENT_FILTER_WINDOW_PROPERTY, null);

        properties.put(ViewListPortlet.PERMALINK_REFERENCE_WINDOW_PROPERTY, null);
        properties.put(ViewListPortlet.RSS_REFERENCE_WINDOW_PROPERTY, null);
        properties.put(ViewListPortlet.RSS_TITLE_WINDOW_PROPERTY, null);


        return properties;
    }
    
    /**
     * 
     * @param doc
     * @param fragment
     * @return the list schema.
     */
    protected PropertyMap getListSchema(Document doc, PropertyMap fragment){
        return EditableWindowHelper.findSchemaByRefURI(doc, CRITERIA_LIST_SCHEMA, fragment.getString("uri"));
    }

    /**
     * @return display style properties.
     */
    protected Map<String, String> fillDisplayProperties(PropertyMap schema, Map<String, String> properties) {
        PropertyMap displayCriteria = (PropertyMap) schema.get("displayCriteria");
        properties.put(ViewListPortlet.TEMPLATE_WINDOW_PROPERTY, (String) displayCriteria.get("style"));
        properties.put(ViewListPortlet.RESULTS_LIMIT_WINDOW_PROPERTY, (String) displayCriteria.get("nbItems"));
        properties.put(ViewListPortlet.NORMAL_PAGINATION_WINDOW_PROPERTY, (String) displayCriteria.get("nbItemsPerPage"));
        return properties;
    }
    
    /**
     * 
     * @param requestCriteria
     * @return the list resquest.
     */
    private String buildRequest(PropertyMap requestCriteria) {

        String currentDocId = (String) requestCriteria.get("currentDocId");
        String currentSpaceId = (String) requestCriteria.get("currentSpaceId");

        Object docTypes = getDocTypes(requestCriteria); 
        PropertyList keyWords = (PropertyList) requestCriteria.get("keyWords");
        String searchArea = (String) requestCriteria.get("searchArea");
        String order = (String) requestCriteria.get("order");

        String docTypesCriterion = this.getDocTypesCriterion(new StringBuffer(), docTypes);
        String keyWordsCriterion = this.getKeyWordsCriterion(new StringBuffer(), keyWords, StringUtils.isBlank(docTypesCriterion));
        String searchAreaCriterion = this.getSearchAreaCriterion(new StringBuffer(), searchArea, currentDocId, currentSpaceId,
                StringUtils.isBlank(docTypesCriterion) && keyWords.isEmpty());
        String orderCriterion = this.getOrderCriterion(new StringBuffer(), order);

        StringBuffer clause = new StringBuffer().append(docTypesCriterion).append(keyWordsCriterion).append(searchAreaCriterion).append(orderCriterion);

        return clause.toString();
    }
    
    /**
     * 
     * @param requestCriteria
     * @return docTypes for request;
     */
    protected Object getDocTypes(PropertyMap requestCriteria){
        return requestCriteria.get("docTypes");
    }
    
    /**
     * 
     * @param docTypesCriterion
     * @param docTypes
     * @return the criterion's request on doctypes.
     */
    protected String getDocTypesCriterion(StringBuffer docTypesCriterion, Object docTypes) {
        if (docTypes != null && docTypes instanceof PropertyList) {
            PropertyList docTypesList = (PropertyList) docTypes;
            if (!docTypesList.isEmpty()) {
                docTypesCriterion.append(" ecm:primaryType in (").append(generateQuotedList(new StringBuffer(), docTypesList)).append(")");
            }
        }
        return docTypesCriterion.toString();
    }
    
    /**
     * 
     * @param keyWordsCriterion
     * @param keyWords
     * @param firstCriterion
     * @return the criterion's request on keywords.
     */
    protected String getKeyWordsCriterion(StringBuffer keyWordsCriterion, PropertyList keyWords, boolean firstCriterion) {
        if (!keyWords.isEmpty()) {
            if (!firstCriterion) {
                keyWordsCriterion.append(CRITERIA_SEPARATOR);
            }
            keyWordsCriterion.append("ttc:keywords in (").append(generateQuotedList(new StringBuffer(), keyWords)).append(")");
        }
        return keyWordsCriterion.toString();
    }
    
    /**
     * 
     * @param searchAreaCriterion
     * @param searchArea
     * @param currentDocId
     * @param currentSpaceId
     * @param firstCriterion
     * @return the criterion's request on path search.
     */
    protected String getSearchAreaCriterion(StringBuffer searchAreaCriterion, String searchArea, String currentDocId, String currentSpaceId,
            boolean firstCriterion) {
        if (StringUtils.isNotBlank(searchArea)) {
            if (!firstCriterion) {
                searchAreaCriterion.append(CRITERIA_SEPARATOR);
            }
            if ("ttc:spaceID".equals(searchArea)) {
                if (StringUtils.isBlank(currentSpaceId)) {
                    /* TODO: Exception */
                } else {
                    searchAreaCriterion.append("ttc:spaceID").append(CRITERION_EQUAL).append(QUOTE).append(currentSpaceId).append(QUOTE);
                }
            } else {
                searchAreaCriterion.append(searchArea).append(CRITERION_EQUAL).append(QUOTE).append(currentDocId).append(QUOTE);
            }
        }
        return searchAreaCriterion.toString();
    }
    
    /**
     * 
     * @param orderCriterion
     * @param order
     * @return the criterion's request on order.
     */
    protected String getOrderCriterion(StringBuffer orderCriterion, String order) {
        if (StringUtils.isNotBlank(order)) {
            orderCriterion.append(" order by ").append(order);
        }
        return orderCriterion.toString();
    }

    /**
     * 
     * @return a quoted list of properties with "," separator.
     */
    protected StringBuffer generateQuotedList(StringBuffer criterion, PropertyList properties) {
        int index = 1;
        for (Object docType : properties.list()) {
            criterion.append(QUOTE).append((String) docType).append(QUOTE);
            if (index < properties.size()) {
                criterion.append(CRITERION_LIST_SEPARATOR);
            }
            index++;
        }
        return criterion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> prepareDelete(Document doc, String refURI) {

        List<String> propertiesToRemove = new ArrayList<String>();
        this.prepareDeleteGeneric(propertiesToRemove, doc, refURI);

        Integer indexToRemove = EditableWindowHelper.findIndexByRefURI(doc, CRITERIA_LIST_SCHEMA, refURI);
        propertiesToRemove.add(CRITERIA_LIST_SCHEMA.concat("/").concat(indexToRemove.toString()));

        return propertiesToRemove;
    }

}
