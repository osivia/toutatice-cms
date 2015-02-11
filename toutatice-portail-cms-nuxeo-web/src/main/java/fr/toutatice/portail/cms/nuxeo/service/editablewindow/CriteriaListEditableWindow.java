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

        PropertyMap mapList = EditableWindowHelper.findSchemaByRefURI(doc, CRITERIA_LIST_SCHEMA, fragment.getString("uri"));

        PropertyMap requestCriteria = (PropertyMap) mapList.get("requestCriteria");
        PropertyMap displayCriteria = (PropertyMap) mapList.get("displayCriteria");

        /* Request */
        String request = this.buildRequest(requestCriteria);
        properties.put(ViewListPortlet.NUXEO_REQUEST_WINDOW_PROPERTY, request);

        /* Display */
        properties.put(ViewListPortlet.TEMPLATE_WINDOW_PROPERTY, (String) displayCriteria.get("style"));
        properties.put(ViewListPortlet.RESULTS_LIMIT_WINDOW_PROPERTY, (String) displayCriteria.get("nbItems"));
        properties.put(ViewListPortlet.NORMAL_PAGINATION_WINDOW_PROPERTY, (String) displayCriteria.get("nbItemsPerPage"));

        /* Technical */
        properties.put(ViewListPortlet.BEAN_SHELL_WINDOW_PROPERTY, String.valueOf(false));
        properties.put(ViewListPortlet.SCOPE_WINDOW_PROPERTY, null);
        properties.put(ViewListPortlet.METADATA_WINDOW_PROPERTY, "1");
        properties.put(ViewListPortlet.NUXEO_REQUEST_DISPLAY_WINDOW_PROPERTY, String.valueOf(false));

        properties.put(ViewListPortlet.CONTENT_FILTER_WINDOW_PROPERTY, null);
        properties.put(Constants.WINDOW_PROP_SCOPE, "__inherited");

        properties.put(ViewListPortlet.PERMALINK_REFERENCE_WINDOW_PROPERTY, null);
        properties.put(ViewListPortlet.RSS_REFERENCE_WINDOW_PROPERTY, null);
        properties.put(ViewListPortlet.RSS_TITLE_WINDOW_PROPERTY, null);


        return properties;
    }

    private String buildRequest(PropertyMap requestCriteria) {

        String currentDocId = (String) requestCriteria.get("currentDocId");
        String currentSpaceId = (String) requestCriteria.get("currentSpaceId");

        PropertyList docTypes = (PropertyList) requestCriteria.get("docTypes");
        PropertyList keyWords = (PropertyList) requestCriteria.get("keyWords");
        String searchArea = (String) requestCriteria.get("searchArea");
        String order = (String) requestCriteria.get("order");

        String docTypesCriterion = this.getDocTypesCriterion(new StringBuffer(), docTypes);
        String keyWordsCriterion = this.getKeyWordsCriterion(new StringBuffer(), keyWords, docTypes.isEmpty());
        String searchAreaCriterion = this.getSearchAreaCriterion(new StringBuffer(), searchArea, currentDocId, currentSpaceId, docTypes.isEmpty() && keyWords.isEmpty());
        String orderCriterion = this.getOrderCriterion(new StringBuffer(), order);

        StringBuffer clause = new StringBuffer().append(docTypesCriterion).append(keyWordsCriterion).append(searchAreaCriterion).append(orderCriterion);

        return clause.toString();
    }

    protected String getDocTypesCriterion(StringBuffer docTypesCriterion, PropertyList docTypes){
        if (!docTypes.isEmpty()) {
            docTypesCriterion.append(" ecm:primaryType in (").append(this.generateQuotedList(new StringBuffer(), docTypes)).append(")");
        }
        return docTypesCriterion.toString();
    }

    protected String getKeyWordsCriterion(StringBuffer keyWordsCriterion, PropertyList keyWords, boolean firstCriterion){
        if (!keyWords.isEmpty()) {
            if(!firstCriterion){
                keyWordsCriterion.append(CRITERIA_SEPARATOR);
            }
            keyWordsCriterion.append("ttc:keywords in (").append(this.generateQuotedList(new StringBuffer(), keyWords)).append(")");
        }
        return keyWordsCriterion.toString();
    }

    protected String getSearchAreaCriterion(StringBuffer searchAreaCriterion, String searchArea, String currentDocId, String currentSpaceId, boolean firstCriterion){
        if (StringUtils.isNotBlank(searchArea)) {
            if(!firstCriterion){
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

    protected String getOrderCriterion(StringBuffer orderCriterion, String order){
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
