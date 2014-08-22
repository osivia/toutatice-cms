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
package fr.toutatice.portail.cms.nuxeo.portlets.customizer;


/**
 * @deprecated Use org.osivia.portal.core.cms.ListTemplate instead.
 * @see org.osivia.portal.core.cms.ListTemplate
 */
@Deprecated
public class ListTemplate extends org.osivia.portal.core.cms.ListTemplate {

    /**
     * Constructor.
     *
     * @param key template key
     * @param label template label
     * @param schemas template schemas
     */
    public ListTemplate(String key, String label, String schemas) {
        super(key, label, schemas);
    }


    /**
     * Constructor.
     *
     * @param template list template
     */
    public ListTemplate(org.osivia.portal.core.cms.ListTemplate template) {
        super(template.getKey(), template.getLabel(), template.getSchemas());
    }

}
