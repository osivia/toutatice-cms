package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import java.util.HashMap;
import java.util.Map;

import fr.toutatice.portail.cms.nuxeo.service.editablewindow.EditableWindow;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.HtmlEditableWindow;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.ListEditableWindow;
import fr.toutatice.portail.cms.nuxeo.service.editablewindow.PortletEditableWindow;

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
        addType("fgt.html", new HtmlEditableWindow("toutatice-portail-cms-nuxeo-viewFragmentPortletInstance", "html_Frag_"));

        addType("fgt.list", new ListEditableWindow("toutatice-portail-cms-nuxeo-viewListPortletInstance", "liste_Frag_"));

        addType("fgt.portlet", new PortletEditableWindow("", "portlet_Frag_"));
    }

    /**
     * Ajout d'un type
     * 
     * @param name le code identifié dans nuxeo
     * @param window l'objet EditableWindow correspondant
     */
    protected void addType(String name, EditableWindow window) {
        types.put(name, window);
    }

    /**
     * Recherche d'un type
     * 
     * @param name le code identifié dans nuxeo
     * @return l'objet EditableWindow correspondant
     */
    public EditableWindow getType(String name) {
        return types.get(name);
    }


}
