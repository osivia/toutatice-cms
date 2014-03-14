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
