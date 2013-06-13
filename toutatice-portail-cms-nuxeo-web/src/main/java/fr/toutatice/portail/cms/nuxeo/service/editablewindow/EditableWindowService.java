package fr.toutatice.portail.cms.nuxeo.service.editablewindow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.portal.theme.ThemeConstants;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
import org.nuxeo.ecm.automation.client.jaxrs.model.PropertyList;
import org.nuxeo.ecm.automation.client.jaxrs.model.PropertyMap;
import org.osivia.portal.core.cms.CMSEditableWindow;

/**
 * Classe générique de manipulation des schémas complexes nuxeo côté portail
 * 
 */
public abstract class EditableWindowService {

    /** logger */
    protected static final Log logger = LogFactory.getLog(EditableWindowService.class);

    /** Identifiant schéma générique des fragments Nuxeo */
    public static String SCHEMA = "fgts:fragments";

    public static String FGT_URI = "uri";

    public static String FGT_TYPE = "fragmentCategory";

    public static String FGT_ORDER = "order";

    public static String FGT_REGION = "regionId";

    /** Référence du type de window */
    protected EditableWindowTypeEnum type;

    /**
     * Définit le type référent à la classe de service
     */
    public void setType(EditableWindowTypeEnum type) {
        this.type = type;
    }

    /**
     * Méthode permettant d'extraire l'index d'une liste de propriétés complexes
     * où la liste correspond à l'uri donné.
     * 
     * @param doc e doc
     * @param schema le schéma a explorer
     * @param refURI la clé de recherche
     * @return la map
     */
    public static Integer findIndexByURI(Document doc, String refURI) {

        PropertyList list = doc.getProperties().getList(SCHEMA);
        int index = 0;

        for (Object o : list.list()) {
            if (o instanceof PropertyMap) {
                PropertyMap map = (PropertyMap) o;
                if (refURI.equals(map.get(FGT_URI)) || refURI.equals(map.get("refURI"))) {

                    return index;
                }
            }
            index++;
        }

        logger.warn("Fragment " + refURI + " non défini dans le schéma " + SCHEMA);
        return null; // par défaut
    }

    /**
     * Méthode permettant d'extraire les propriétés complexes d'un objet par son
     * RefURI
     * 
     * @param doc e doc
     * @param schema le schéma a explorer
     * @param refURI la clé de recherche
     * @return la map
     */
    public static Integer findIndexByRefURI(Document doc, String schema, String refURI) {

        PropertyList list = doc.getProperties().getList(schema);
        int index = 0;

        for (Object o : list.list()) {
            if (o instanceof PropertyMap) {
                PropertyMap map = (PropertyMap) o;
                if (refURI.equals(map.get(FGT_URI)) || refURI.equals(map.get("refURI"))) {

                    return index;
                }
            }
            index++;
        }

        logger.warn("Fragment " + refURI + " non défini dans le schéma " + schema);
        return null;
    }

    /**
     * Méthode permettant d'extraire les propriétés complexes d'un objet par son
     * RefURI
     * 
     * @param doc e doc
     * @param schema le schéma a explorer
     * @param refURI la clé de recherche
     * @return la map
     */
    public static PropertyMap findSchemaByRefURI(Document doc, String schema, String refURI) {

        PropertyList list = doc.getProperties().getList(schema);

        for (Object o : list.list()) {
            if (o instanceof PropertyMap) {
                PropertyMap map = (PropertyMap) o;
                if (refURI.equals(map.get(FGT_URI)) || refURI.equals(map.get("refURI"))) {

                    return map;
                }
            }
        }

        logger.warn("Fragment " + refURI + " non défini dans le schéma " + schema);
        return null;
    }

    /**
     * Extrait le mapping des propriétés par fragment récupéré depuis Nuxeo et
     * retourne ces propriétés pour créer chaque ViewFragmentPortlet.
     * 
     * @param doc
     *            simplesite ou simplepage (conteneur des fragments)
     * @param fragment
     *            les props du fragment
     * @param modeEditionPage
     *            page en cours d'édition
     * @return les props de la window
     */
    public abstract Map<String, String> fillProps(Document doc, PropertyMap fragment, Boolean modeEditionPage);

    /**
     * Extrait le mapping des propriétés par fragment récupéré depuis Nuxeo et
     * retourne ces propriétés pour créer chaque ViewFragmentPortlet.
     * 
     * @param doc
     *            simplesite ou simplepage (conteneur des fragments)
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
        propsFilled.put("osivia.nuxeoPath", doc.getPath());
        propsFilled.put("osivia.propertyName", "htmlfgt:htmlFragment");

        propsFilled.put("osivia.refURI", fragment.getString(FGT_URI));

        propsFilled.put("osivia.title", fragment.getString("title"));

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

        Integer indexToRemove = findIndexByURI(doc, refURI);

        propertiesToRemove.add(SCHEMA.concat("/").concat(indexToRemove.toString()));
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
        String windowId = type.getPrefixeIdFrag().concat(Integer.toString(id));

        return new CMSEditableWindow(windowId, type.getPorletInstance(), portletProps);
    }

    /**
     * 
     * @param doc
     * @param refURI uri source
     * @param toURI uri destination si c'est un fragment, ou non de la région si elle ne contient aucun fragment
     * @param belowFragment positionne le fragment au dessus ou en dessous du fragment destination
     * @param dropOnEmptyRegion D&D vers une région vide
     * @return
     */
    public static List<String> prepareMove(Document doc, String refURI, String toURI, boolean belowFragment, boolean dropOnEmptyRegion) {

        List<String> propertiesToUpdate = new ArrayList<String>();

        PropertyMap propRefUri = findSchemaByRefURI(doc, SCHEMA, refURI);
        String regionRefUri = propRefUri.getString(FGT_REGION);
        Integer orderRefUri = Integer.parseInt(propRefUri.getString(FGT_ORDER));

        String regionToUri;
        Integer orderToUri;

        // En cas de déplacement d'un fragment dans une région vide, l'uri passée est le nom
        // de la région, la position est celle tout en haut (0 par défaut).
        if (dropOnEmptyRegion) {
            regionToUri = toURI;
            orderToUri = 0;
        }
        // Sinon, déterminer la position du fragment de destination pour le remplacer.
        else {
            PropertyMap propToUri = findSchemaByRefURI(doc, SCHEMA, toURI);
            regionToUri = propToUri.getString(FGT_REGION);
            orderToUri = Integer.parseInt(propToUri.getString(FGT_ORDER));
        }


        PropertyList list = doc.getProperties().getList(SCHEMA);
        Integer index = 0;

        for (Object o : list.list()) {
            if (o instanceof PropertyMap) {
                PropertyMap currentFrag = (PropertyMap) o;

                Integer orderCurrent = Integer.parseInt(currentFrag.getString(FGT_ORDER));

                // Si fgt en cours de déplacement :
                // Attribution de la nouvelle région et de la nouvelle position
                if (refURI.equals(currentFrag.get(FGT_URI))) {
                    String moveToRegion = SCHEMA.concat("/").concat(index.toString()).concat("/").concat(FGT_REGION).concat("=").concat(regionToUri);

                    propertiesToUpdate.add(moveToRegion);

                    Integer newOrder = orderToUri;
                    if (belowFragment)
                        newOrder = orderToUri + 1;

                    String moveToOrder = SCHEMA.concat("/").concat(index.toString()).concat("/").concat(FGT_ORDER).concat("=").concat(newOrder.toString());

                    propertiesToUpdate.add(moveToOrder);
                } else {
                    // Si fgt de la région d'origine et en dessous du fgt
                    // déplacé
                    // Décalage vers le haut
                    if (regionRefUri.equals(currentFrag.getString(FGT_REGION)) && orderCurrent > orderRefUri) {

                        Integer newOrder = orderCurrent - 1;
                        String moveToOrder = SCHEMA.concat("/").concat(index.toString()).concat("/").concat(FGT_ORDER).concat("=").concat(newOrder.toString());

                        propertiesToUpdate.add(moveToOrder);
                    }

                    // Si fgt de la région cible est au niveau de la région
                    // déplacée
                    // Décalage vers le bas
                    if (regionToUri.equals(currentFrag.getString(FGT_REGION))) {

                        if ((belowFragment && orderCurrent > orderToUri) || (!belowFragment && (orderCurrent >= orderToUri))) {

                            Integer newOrder = orderCurrent + 1;
                            String moveToOrder = SCHEMA.concat("/").concat(index.toString()).concat("/").concat(FGT_ORDER).concat("=")
                                    .concat(newOrder.toString());

                            propertiesToUpdate.add(moveToOrder);
                        }
                    }
                }
            }
            index++;
        }

        return propertiesToUpdate;
    }
}
