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
package fr.toutatice.portail.cms.nuxeo.portlets.files;

import java.util.Comparator;
import java.util.Map;

import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.core.cms.CMSItemType;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;

/**
 * File browser comparator.
 *
 * @author Cédric Krommenhoek
 * @see Comparator
 * @see Document
 */
public class FileBrowserComparator implements Comparator<Document> {

    /** Nuxeo controller. */
    private final NuxeoController nuxeoController;


    /**
     * Constructor.
     *
     * @param nuxeoController Nuxeo controller
     */
    public FileBrowserComparator(NuxeoController nuxeoController) {
        super();
        this.nuxeoController = nuxeoController;
    }


    /**
     * {@inheritDoc}
     */
    public int compare(Document doc1, Document doc2) {
        // Folderish comparison
        Map<String, CMSItemType> managedTypes = this.nuxeoController.getCMSItemTypes();
        CMSItemType type1 = managedTypes.get(doc1.getType());
        CMSItemType type2 = managedTypes.get(doc2.getType());
        if ((type1 != null) && type1.isFolderish()) {
            if ((type2 == null) || !type2.isFolderish()) {
                return -1;
            }
        } else if ((type2 != null) && type2.isFolderish()) {
            return 1;
        }

        // Title comparison
        String title1 = doc1.getTitle();
        String title2 = doc2.getTitle();
        return title1.compareToIgnoreCase(title2);
    }

}
