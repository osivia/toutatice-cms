package fr.toutatice.portail.cms.nuxeo.service.editablewindow;

import java.util.Comparator;

import org.nuxeo.ecm.automation.client.model.PropertyMap;

/**
 * Comparateur de fragments selon l'ordre défini dans le schéma,
 * Le comparateur corrige les ordres pour éviter les doublons
 */
public class EditableWindowComparator implements Comparator<PropertyMap> {

    public int compare(PropertyMap arg0, PropertyMap arg1) {

        Integer orderA = Integer.parseInt(arg0.getString(EditableWindowHelper.FGT_ORDER));
        Integer orderB = Integer.parseInt(arg1.getString(EditableWindowHelper.FGT_ORDER));

        int compareTo = orderA.compareTo(orderB);

        // en cas de numéros d'ordres identique, B est considéré comme plus grand
        // et viendra donc en position suivante dans la collection
        if (compareTo == 0)
            return 1;
        else
            return compareTo;
    }

}
