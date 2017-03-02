package fr.toutatice.portail.cms.nuxeo.portlets.forms;

import java.util.Comparator;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.PropertyMap;


public class FieldComparator implements Comparator<PropertyMap> {

    @Override
    public int compare(PropertyMap field, PropertyMap comparedField) {
        int returnValue;

        int index = 0;
        String fieldPath = field.getString("path");
        String comparedFieldPath = comparedField.getString("path");

        final String[] pathArray = StringUtils.split(fieldPath, ',');
        final String[] comparedPathArray = StringUtils.split(comparedFieldPath, ',');
        Integer pathPart = Integer.parseInt(pathArray[index]);
        Integer comparedPathPart = Integer.parseInt(comparedPathArray[index]);
        returnValue = pathPart.compareTo(comparedPathPart);
        boolean deeperPath = pathArray.length > (index + 1);
        boolean deeperComparedPath = comparedPathArray.length > (index + 1);
        if ((returnValue == 0) && (deeperPath || deeperComparedPath)) {
            if (deeperPath && !deeperComparedPath) {
                returnValue = 1;
            } else if (!deeperPath && deeperComparedPath) {
                returnValue = -1;
            } else {
                index++;
                returnValue = compare(pathArray, comparedPathArray, index);
            }
        }
        return returnValue;
    }

    private int compare(String[] pathArray, String[] comparedPathArray, int index) {
        Integer pathPart = Integer.parseInt(pathArray[index]);
        Integer comparedPathPart = Integer.parseInt(comparedPathArray[index]);
        int returnValue = pathPart.compareTo(comparedPathPart);
        boolean deeperPath = pathArray.length > (index + 1);
        boolean deeperComparedPath = comparedPathArray.length > (index + 1);
        if ((returnValue == 0) && (deeperPath || deeperComparedPath)) {
            if (deeperPath && !deeperComparedPath) {
                returnValue = 1;
            } else if (!deeperPath && deeperComparedPath) {
                returnValue = -1;
            } else {
                index++;
                returnValue = compare(pathArray, comparedPathArray, index);
            }
        }
        return returnValue;
    }

}
