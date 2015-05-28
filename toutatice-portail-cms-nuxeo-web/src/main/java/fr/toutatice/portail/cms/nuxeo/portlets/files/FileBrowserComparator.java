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
import java.util.Date;

import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.core.cms.CMSItemType;

/**
 * File browser comparator.
 *
 * @author Cédric Krommenhoek
 * @see Comparator
 * @see Document
 */
public class FileBrowserComparator implements Comparator<FileBrowserItem> {

    /** Sort criteria. */
    private final FileBrowserSortCriteria criteria;


    /**
     * Constructor.
     *
     * @param criteria sort criteria
     */
    public FileBrowserComparator(FileBrowserSortCriteria criteria) {
        super();
        this.criteria = criteria;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(FileBrowserItem item1, FileBrowserItem item2) {
        int result = 0;

        String sort = this.criteria.getSort();

        if ("index".equals(sort)) {
            result = this.compare(item1.getIndex(), item2.getIndex());
        } else {
            // Folderish comparison
            CMSItemType type1 = item1.getType();
            boolean folderish1 = (type1 != null) && type1.isFolderish();
            CMSItemType type2 = item2.getType();
            boolean folderish2 = (type2 != null) && type2.isFolderish();

            if (folderish1 && !folderish2) {
                return -1;
            } else if (!folderish1 && folderish2) {
                return 1;
            }


            // Attribute comparison
            if ("name".equals(sort)) {
                result = this.compare(item1.getTitle(), item2.getTitle());
            } else if ("date".equals(sort)) {
                Date date1 = (Date) item1.getProperties().get("dc:modified");
                if (date1 == null) {
                    date1 = (Date) item1.getProperties().get("dc:created");
                }

                Date date2 = (Date) item2.getProperties().get("dc:modified");
                if (date2 == null) {
                    date2 = (Date) item2.getProperties().get("dc:created");
                }

                result = this.compare(date1, date2);
            } else if ("contributor".equals(sort)) {
                String contributor1 = (String) item1.getProperties().get("dc:lastContributor");
                String contributor2 = (String) item2.getProperties().get("dc:lastContributor");
                result = this.compare(contributor1, contributor2);
            } else if ("size".equals(sort)) {
                long size1 = 0;
                String sizeProperty1 = (String) item1.getProperties().get("common:size");
                if (sizeProperty1 != null) {
                    size1 = Long.valueOf(sizeProperty1);
                }

                long size2 = 0;
                String sizeProperty2 = (String) item2.getProperties().get("common:size");
                if (sizeProperty2 != null) {
                    size2 = Long.valueOf(sizeProperty2);
                }

                result = this.compare(size1, size2);
            } else {
                result = 0;
            }
        }


        // Alternative sort
        if (this.criteria.isAlternative()) {
            result = -result;
        }

        return result;
    }


    /**
     * Compare two attributes.
     *
     * @param object1 object #1
     * @param object2 object #2
     * @return comparison value
     */
    private <T extends Comparable<T>> int compare(T object1, T object2) {
        int result;
        if (object1 == null) {
            result = -1;
        } else if (object2 == null) {
            result = 1;
        } else if ((object1 instanceof String) && (object2 instanceof String)) {
            String string1 = (String) object1;
            String string2 = (String) object2;
            result = string1.compareToIgnoreCase(string2);
        } else {
            result = object1.compareTo(object2);
        }
        return result;
    }

}
