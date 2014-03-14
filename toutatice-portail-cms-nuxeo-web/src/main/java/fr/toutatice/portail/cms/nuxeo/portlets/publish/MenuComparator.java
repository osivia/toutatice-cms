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
package fr.toutatice.portail.cms.nuxeo.portlets.publish;

import java.util.Comparator;
import java.util.Map;

import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.core.cms.CMSItemType;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;

/**
 * Menu comparator.
 * 
 * @author Cédric Krommenhoek
 * @see Comparator
 * @see NavigationDisplayItem
 */
public class MenuComparator implements Comparator<NavigationDisplayItem> {

    /** Nuxeo controller. */
    private final NuxeoController nuxeoController;


    /**
     * Constructor.
     * 
     * @param nuxeoController Nuxeo controller
     */
    public MenuComparator(NuxeoController nuxeoController) {
        super();
        this.nuxeoController = nuxeoController;
    }


    /**
     * {@inheritDoc}
     */
    public int compare(NavigationDisplayItem item1, NavigationDisplayItem item2) {
        Document doc1 = (Document) item1.getNavItem().getNativeItem();
        Document doc2 = (Document) item2.getNavItem().getNativeItem();

        Map<String, CMSItemType> managedTypes = this.nuxeoController.getCMSItemTypes();
        CMSItemType type1 = managedTypes.get(doc1.getPath());
        CMSItemType type2 = managedTypes.get(doc2.getPath());

        if ((type1 != null) && type1.isFolderish()) {
            if ((type2 != null) && type2.isFolderish()) {
                return doc1.getTitle().toUpperCase().compareTo(doc2.getTitle().toUpperCase());
            } else {
                return -1;
            }
        } else {
            if ((type2 != null) && type2.isFolderish()) {
                return 1;
            } else {
                return doc1.getTitle().toUpperCase().compareTo(doc2.getTitle().toUpperCase());
            }
        }
    }

}
