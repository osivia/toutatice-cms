package fr.toutatice.portail.cms.nuxeo.service.editablewindow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.portal.theme.ThemeConstants;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
import org.nuxeo.ecm.automation.client.jaxrs.model.PropertyList;
import org.nuxeo.ecm.automation.client.jaxrs.model.PropertyMap;
import org.osivia.portal.core.cms.CMSEditableWindow;
import org.osivia.portal.core.cms.CMSException;

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

    /** Comparateur de fragments */
    private static FragmentComparator comparator = new FragmentComparator();

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
     * Vérification de la cohérence des n° d'ordre dans les fragments
     * 
     * @param doc
     * @return
     */
    public static List<String> checkBeforeMove(Document doc, String fromRegion, Integer fromPos, String refUri) throws CMSException {
        List<String> propertiesToUpdate = new ArrayList<String>();

        PropertyList list = doc.getProperties().getList(SCHEMA);
        
        // Détecter des cas de désynchro
        PropertyMap currentInNuxeo = findSchemaByRefURI(doc, SCHEMA, refUri);
        if (!(currentInNuxeo.getString(FGT_ORDER).equals(Integer.toString(fromPos)) && currentInNuxeo.getString(FGT_REGION).equals(fromRegion))) {
            throw new CMSException("Document Nuxéo désynchronisé");

        }

        

        Map<String, SortedSet<PropertyMap>> map = new HashMap<String, SortedSet<PropertyMap>>();

        // Pour chaque fragment...
        for (Object o : list.list()) {
            if (o instanceof PropertyMap) {
                PropertyMap currentFrag = (PropertyMap) o;

                String currentRegion = currentFrag.getString(FGT_REGION);

                SortedSet<PropertyMap> currentSet;

                if (map.get(currentRegion) != null) {
                    currentSet = map.get(currentRegion);
                } else {
                    currentSet = new TreeSet<PropertyMap>(comparator);
                    map.put(currentRegion, currentSet);
                }

                currentSet.add(currentFrag);
            }
        }

        for (SortedSet<PropertyMap> region : map.values()) {
            Integer expectedOrder = 0;
            for (PropertyMap fragment : region) {
                Integer currentOrder = Integer.parseInt(fragment.getString(FGT_ORDER));

                if (!(currentOrder == expectedOrder)) {
                    Integer fgtToUpdate = findIndexByURI(doc, fragment.getString(FGT_URI));

                    String moveToOrder = SCHEMA.concat("/").concat(fgtToUpdate.toString()).concat("/").concat(FGT_ORDER).concat("=")
                            .concat(expectedOrder.toString());

                    propertiesToUpdate.add(moveToOrder);
                }

                expectedOrder++;
            }
        }

        return propertiesToUpdate;
    }

    /**
     * Prepare the list of nuxeo update commands to move the fragment
     * 
     * @param doc
     * @param fromRegion the identifier of the region from the fragment is moved
     * @param fromPos position in the fromRegion (from 0 (top) to N-1 ( number of current fgts in the region)
     * @param toRegion the identifier of the region where the fragment is dropped
     * @param toPos the new position of the fgt in the toRegion
     * @param refUri the id of the window moved
     * @return list of nuxeo properties and values
     * @throws CMSException
     */
    public static List<String> prepareMove(Document doc, String fromRegion, Integer fromPos, String toRegion, Integer toPos, String refUri) {

        List<String> propertiesToUpdate = new ArrayList<String>();


        if (logger.isDebugEnabled()) {
            logger.debug("+-------> Move " + refUri + " (" + fromRegion + "/" + fromPos + ") to (" + toRegion + "/" + toPos + ") ");
        }

        // Test si déplacement au même endroit, rien à faire
        if (fromRegion.equals(toRegion) && fromPos.equals(toPos)) {
            return propertiesToUpdate;
        }


        PropertyList list = doc.getProperties().getList(SCHEMA);
        Integer index = 0;


        // Cas d'un déplacement dans une même région
        if (fromRegion.equals(toRegion)) {
            boolean moveDown = true; // Déterminer le sens du déplacement
            Integer minOrder = fromPos;
            Integer maxOrder = toPos;
            if (fromPos > toPos) {
                moveDown = false;
                minOrder = toPos;
                maxOrder = fromPos;
            }

            for (Object o : list.list()) {
                if (o instanceof PropertyMap) {
                    PropertyMap currentFrag = (PropertyMap) o;

                    Integer currentOrder = Integer.parseInt(currentFrag.getString(FGT_ORDER));

                    // Si fgt en cours de déplacement :
                    // Attribution de la nouvelle région et de la nouvelle position
                    if (fromRegion.equals(currentFrag.getString(FGT_REGION))) {


                        if (fromPos.equals(currentOrder)) {

                            Integer newOrder = toPos;

                            String moveToOrder = SCHEMA.concat("/").concat(index.toString()).concat("/").concat(FGT_ORDER).concat("=")
                                    .concat(newOrder.toString());

                            propertiesToUpdate.add(moveToOrder);
                        }

                        else if (fromRegion.equals(currentFrag.getString(FGT_REGION)) && (currentOrder >= minOrder && currentOrder <= maxOrder)) {

                            Integer newOrder = moveDown ? currentOrder - 1 : currentOrder + 1;
                            String moveToOrder = SCHEMA.concat("/").concat(index.toString()).concat("/").concat(FGT_ORDER).concat("=")
                                    .concat(newOrder.toString());

                            propertiesToUpdate.add(moveToOrder);
                        }
                    }
                }
                index++;
            }

        } else {
            for (Object o : list.list()) {
                if (o instanceof PropertyMap) {
                    PropertyMap currentFrag = (PropertyMap) o;

                    Integer currentOrder = Integer.parseInt(currentFrag.getString(FGT_ORDER));

                    // Si fgt en cours de déplacement :
                    // Attribution de la nouvelle région et de la nouvelle position
                    if ((fromRegion.equals(currentFrag.getString(FGT_REGION))) && (fromPos.equals(currentOrder))) {

                        String moveToRegion = SCHEMA.concat("/").concat(index.toString()).concat("/").concat(FGT_REGION).concat("=").concat(toRegion);

                        propertiesToUpdate.add(moveToRegion);

                        Integer newOrder = toPos;

                        String moveToOrder = SCHEMA.concat("/").concat(index.toString()).concat("/").concat(FGT_ORDER).concat("=").concat(newOrder.toString());

                        propertiesToUpdate.add(moveToOrder);
                    } else {

                        // Si fgt de la région d'origine et en dessous du fgt
                        // déplacé
                        // Décalage vers le haut
                        if (fromRegion.equals(currentFrag.getString(FGT_REGION)) && currentOrder > fromPos) {

                            Integer newOrder = currentOrder - 1;
                            String moveToOrder = SCHEMA.concat("/").concat(index.toString()).concat("/").concat(FGT_ORDER).concat("=")
                                    .concat(newOrder.toString());

                            propertiesToUpdate.add(moveToOrder);
                        }

                        // Si fgt de la région cible est au niveau de la région
                        // déplacée
                        // Décalage vers le bas
                        if (toRegion.equals(currentFrag.getString(FGT_REGION))) {

                            if (currentOrder >= toPos) {

                                Integer newOrder = currentOrder + 1;
                                String moveToOrder = SCHEMA.concat("/").concat(index.toString()).concat("/").concat(FGT_ORDER).concat("=")
                                        .concat(newOrder.toString());

                                propertiesToUpdate.add(moveToOrder);
                            }
                        }
                    }
                }
                index++;
            }

        }
        return propertiesToUpdate;
    }


}
