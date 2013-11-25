package fr.toutatice.portail.cms.nuxeo.service.editablewindow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.portal.theme.ThemeConstants;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.core.cms.CMSEditableWindow;

/**
 * Classe générique de fragment nuxeo
 * 
 */
public abstract class EditableWindow {


    /** logger */
    protected static final Log logger = LogFactory.getLog(EditableWindow.class);


    /** Instance de la portlet */
    private String instancePortlet;

    /** prefixe au nom du porlet */
    private String prefixWindow;


    /**
     * 
     * @param instancePortlet
     * @param prefixWindow
     */
    public EditableWindow(String instancePortlet, String prefixWindow) {
        this.instancePortlet = instancePortlet;
        this.prefixWindow = prefixWindow;
    }


    /**
     * @return the instancePortlet
     */
    public String getInstancePortlet() {
        return instancePortlet;
    }


    /**
     * @param instancePortlet the instancePortlet to set
     */
    public void setInstancePortlet(String instancePortlet) {
        this.instancePortlet = instancePortlet;
    }


    /**
     * @return the prefixWindow
     */
    public String getPrefixWindow() {
        return prefixWindow;
    }


    /**
     * Extrait le mapping des propriétés par fragment récupéré depuis Nuxeo et
     * retourne ces propriétés pour créer chaque portlet.
     * 
     * @param doc
     *            conteneur des fragments
     * @param fragment
     *            les props du fragment
     * @param modeEditionPage
     *            page en cours d'édition
     * @return les props de la window
     */
    public abstract Map<String, String> fillProps(Document doc, PropertyMap fragment, Boolean modeEditionPage);

    /**
     * Extrait le mapping des propriétés par fragment récupéré depuis Nuxeo et
     * retourne ces propriétés pour créer chaque portlet.
     * 
     * @param doc
     *            conteneur des fragments
     * @param fragment
     *            les props du fragment
     * @param modeEditionPage
     *            page en cours d'édition
     * @return les props de la window
     */
    protected Map<String, String> fillGenericProps(Document doc, PropertyMap fragment, Boolean modeEditionPage) {

        // Propriétés génériques
        Map<String, String> propsFilled = new HashMap<String, String>();
        propsFilled.put("osivia.fragmentTypeId", "html_property");
        propsFilled.put("osivia.cms.uri", doc.getPath());
        propsFilled.put("osivia.propertyName", "htmlfgt:htmlFragment");

        propsFilled.put("osivia.refURI", fragment.getString(EditableWindowHelper.FGT_URI));

        propsFilled.put("osivia.title", fragment.getString("title"));
        propsFilled.put("osivia.style", fragment.getString("style"));

        if (fragment.getBoolean("hideTitle").equals(Boolean.TRUE)) {
            propsFilled.put("osivia.hideTitle", "1");
        } else {
            propsFilled.put("osivia.hideTitle", "0");
        }

        if (modeEditionPage)
            propsFilled.put("osivia.cms.displayLiveVersion", "1");

        propsFilled.put(ThemeConstants.PORTAL_PROP_REGION, fragment.getString("regionId"));
        propsFilled.put(ThemeConstants.PORTAL_PROP_ORDER, fragment.getString("order"));

        return propsFilled;
    }

    /**
     * Prépare la commande pour supprimer une entrée du schéma générique
     * 
     * @param propertiesToRemove
     * @param doc
     * @param refURI
     */
    protected void prepareDeleteGeneric(List<String> propertiesToRemove, Document doc, String refURI) {

        Integer indexToRemove = EditableWindowHelper.findIndexByURI(doc, refURI);

        propertiesToRemove.add(EditableWindowHelper.SCHEMA.concat("/").concat(indexToRemove.toString()));
    }

    /**
     * Prépare la commande pour supprimer un fragment
     * 
     * @param doc
     * @param refURI
     * @return
     */
    public abstract List<String> prepareDelete(Document doc, String refURI);

    /**
     * Retourner un nouveau porlet en fonction du type de fragment souhaité
     * 
     * @param id
     *            identifiant de la portlet dans la page
     * @param fp
     *            le type de portlet
     * @param portletProps
     *            ses propriétés
     * @return la fenetre éditable avec le portlet instancié
     */
    public CMSEditableWindow createNewEditabletWindow(int id, Map<String, String> portletProps) {
        String windowId = prefixWindow.concat(Integer.toString(id));

        return new CMSEditableWindow(windowId, instancePortlet, portletProps);
    }

}
