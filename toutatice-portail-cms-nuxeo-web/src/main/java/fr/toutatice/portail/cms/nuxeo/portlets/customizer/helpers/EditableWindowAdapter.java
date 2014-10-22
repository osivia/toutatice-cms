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
package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import java.util.HashMap;
import java.util.Map;

import fr.toutatice.portail.cms.nuxeo.service.editablewindow.EditableWindow;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.HTMLEditableWindow;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.LinksEditableWindow;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.ListEditableWindow;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.PictureEditableWindow;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.PortletEditableWindow;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.SummaryEditableWindow;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.ZoomEditableWindow;

/**
 * Permet de gérer et ajouter les types de Window affichables dans le portail
 *
 */
public class EditableWindowAdapter {

    /** Liste des window disponibles */
    private Map<String, EditableWindow> types = new HashMap<String, EditableWindow>();

    /**
     * Constructeur par défaut (créé le type de window HTML et liste)
     */
    public EditableWindowAdapter() {
        this.addType("fgt.html", new HTMLEditableWindow("toutatice-portail-cms-nuxeo-viewFragmentPortletInstance", "html_Frag_"));

        this.addType("fgt.links", new LinksEditableWindow("toutatice-portail-cms-nuxeo-viewFragmentPortletInstance", "links_Frag_"));

        this.addType("fgt.zoom", new ZoomEditableWindow("toutatice-portail-cms-nuxeo-viewFragmentPortletInstance", "zoom_Frag_"));

        this.addType("fgt.list", new ListEditableWindow("toutatice-portail-cms-nuxeo-viewListPortletInstance", "liste_Frag_"));
        
        this.addType("fgt.summary", new SummaryEditableWindow("toutatice-portail-cms-nuxeo-viewFragmentPortletInstance", "summary_Frag_"));

        this.addType("fgt.picture", new PictureEditableWindow("toutatice-portail-cms-nuxeo-viewFragmentPortletInstance", "picture_Frag_"));

        this.addType("fgt.portlet", new PortletEditableWindow("", "portlet_Frag_"));
    }

    /**
     * Ajout d'un type
     *
     * @param name le code identifié dans nuxeo
     * @param window l'objet EditableWindow correspondant
     */
    protected void addType(String name, EditableWindow window) {
        this.types.put(name, window);
    }

    /**
     * Recherche d'un type
     *
     * @param name le code identifié dans nuxeo
     * @return l'objet EditableWindow correspondant
     */
    public EditableWindow getType(String name) {
        return this.types.get(name);
    }


}
