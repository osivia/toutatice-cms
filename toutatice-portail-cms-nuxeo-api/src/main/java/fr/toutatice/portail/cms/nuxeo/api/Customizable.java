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
package fr.toutatice.portail.cms.nuxeo.api;

/**
 * Listing of existing extension points and the property to access them.
 *
 * @author lbillon
 */
public enum Customizable {

    /** JSP. */
    JSP("osivia.customizer.cms.jsp"),
    /** List template. */
    LIST_TEMPLATE("osivia.customizer.cms.template."),
    /** Fragment. */
    FRAGMENT("osivia.customizer.cms.fragments."),
    /** Editable window. */
    EDITABLE_WINDOW("osivia.customizer.cms.ew."),
    /** Document type. */
    DOC_TYPE("osivia.customizer.cms.doctype"),
    /** Document player. */
    PLAYER("osivia.customizer.cms.modules"),
    /** Menubar. */
    MENUBAR("osivia.customizer.cms.menubar"),
    /** Menu template. */
    MENU_TEMPLATE("osivia.customizer.cms.menuTemplate"),
    /** Navigation adapters. */
    NAVIGATION_ADAPTERS("osivia.customizer.cms.navigationAdapters");


    /** Property name. */
    private String property;


    /**
     * Constructor.
     *
     * @param property property name.
     */
    private Customizable(String property) {
        this.property = property;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.property;
    }

}
