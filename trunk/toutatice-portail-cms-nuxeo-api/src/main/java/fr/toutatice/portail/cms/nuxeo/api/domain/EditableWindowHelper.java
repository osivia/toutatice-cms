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
package fr.toutatice.portail.cms.nuxeo.api.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.core.cms.CMSException;

/**
 * Classe utilitaire de manipulation des schémas complexes.
 *
 * @author loic
 */
public final class EditableWindowHelper {

    /** Identifiant schéma générique des fragments Nuxeo. */
    public static final String SCHEMA_FRAGMENTS = "fgts:fragments";

    /** Regions schema. */
    public static final String SCHEMA_REGIONS = "rg:regions";

    /** URI. */
    public static final String FGT_URI = "uri";

    /** type de fragment (html, liste...). */
    public static final String FGT_TYPE = "fragmentCategory";

    /** ordre d'apparition dans la page. */
    public static final String FGT_ORDER = "order";

    /** Region identifier property key. */
    public static final String REGION_IDENTIFIER = "regionId";

    /** Inheritance property key. */
    public static final String INHERITANCE = "inheritance";

    /** Region layout property key. */
    public static final String REGION_LAYOUT = "regionLayout";


    /** Comparateur de fragments. */
    private static EditableWindowComparator comparator = new EditableWindowComparator();

    /** logger. */
    private static final Log LOGGER = LogFactory.getLog(EditableWindowHelper.class);


    /**
     * Private constructor.
     */
    private EditableWindowHelper() {
        super();
    }


    /**
     * Méthode permettant d'extraire l'index d'une liste de propriétés complexes
     * où la liste correspond à l'uri donné.
     *
     * @param doc e doc
     * @param refURI la clé de recherche
     * @return la map
     */
    public static Integer findIndexByURI(Document doc, String refURI) {

        PropertyList list = doc.getProperties().getList(SCHEMA_FRAGMENTS);
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

        LOGGER.warn("Fragment " + refURI + " non défini dans le schéma " + SCHEMA_FRAGMENTS);
        return null; // par défaut
    }


    /**
     * Méthode permettant d'extraire les propriétés complexes d'un objet par son RefURI.
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

        LOGGER.warn("Fragment " + refURI + " non défini dans le schéma " + schema);
        return null;
    }

    /**
     * Méthode permettant d'extraire les propriétés complexes d'un objet par son RefURI.
     *
     * @param doc e doc
     * @param schema le schéma a explorer
     * @param refURI la clé de recherche
     * @return la map
     */
    public static List<Integer> findIndexesByRefURI(Document doc, String schema, String refURI) {

        List<Integer> indexes = new ArrayList<Integer>();

        PropertyList list = doc.getProperties().getList(schema);
        int index = 0;

        for (Object o : list.list()) {
            if (o instanceof PropertyMap) {
                PropertyMap map = (PropertyMap) o;
                if (refURI.equals(map.get(FGT_URI)) || refURI.equals(map.get("refURI"))) {

                    indexes.add(new Integer(index));
                }
            }
            index++;
        }

        LOGGER.warn("Fragment " + refURI + " non défini dans le schéma " + schema);
        return indexes;
    }

    /**
     * Méthode permettant d'extraire les propriétés complexes d'un objet par son RefURI.
     *
     * @param doc e doc
     * @param schema le schéma a explorer
     * @param refURI la clé de recherche
     * @return la map
     */
    public static PropertyMap findSchemaByRefURI(Document doc, String schema, String refURI) {

        PropertyList list = doc.getProperties().getList(schema);

        if(list != null) {
	        for (Object o : list.list()) {
	            if (o instanceof PropertyMap) {
	                PropertyMap map = (PropertyMap) o;
	                if (refURI.equals(map.get(FGT_URI)) || refURI.equals(map.get("refURI"))) {
	
	                    return map;
	                }
	            }
	        }
        }
        else {
        	LOGGER.warn("Propriétés non trouvées pour " + refURI + " / " + schema);	
        }

        LOGGER.warn("Fragment " + refURI + " non défini dans le schéma " + schema);
        return null;
    }


    /**
     * Vérification de la cohérence des n° d'ordre dans les fragments.
     *
     * @param doc
     * @return
     */
    public static List<String> checkBeforeMove(Document doc, String fromRegion, Integer fromPos, String refUri) throws CMSException {
        List<String> propertiesToUpdate = new ArrayList<String>();

        PropertyList list = doc.getProperties().getList(SCHEMA_FRAGMENTS);


        Map<String, SortedSet<PropertyMap>> map = new HashMap<String, SortedSet<PropertyMap>>();

        // Pour chaque fragment...
        for (Object o : list.list()) {
            if (o instanceof PropertyMap) {
                PropertyMap currentFrag = (PropertyMap) o;

                String currentRegion = currentFrag.getString(REGION_IDENTIFIER);

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

                    String moveToOrder = SCHEMA_FRAGMENTS.concat("/").concat(fgtToUpdate.toString()).concat("/").concat(FGT_ORDER).concat("=")
                            .concat(expectedOrder.toString());

                    propertiesToUpdate.add(moveToOrder);
                }

                expectedOrder++;
            }
        }

        return propertiesToUpdate;
    }

    /**
     * Préparation du déplacement d'un fragment
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
    public static List<String> prepareMove(Document doc, String fromRegion, Integer fromPos, String toRegion, Integer toPos, String refUri) throws CMSException {

        // Détecter des cas de désynchro
        PropertyMap currentInNuxeo = findSchemaByRefURI(doc, SCHEMA_FRAGMENTS, refUri);
        if (!(currentInNuxeo.getString(FGT_ORDER).equals(Integer.toString(fromPos)) && currentInNuxeo.getString(REGION_IDENTIFIER).equals(fromRegion))) {
            throw new CMSException("Document Nuxéo désynchronisé");

        }

        List<String> propertiesToUpdate = new ArrayList<String>();


        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("+-------> Move " + refUri + " (" + fromRegion + "/" + fromPos + ") to (" + toRegion + "/" + toPos + ") ");
        }

        // Test si déplacement au même endroit, rien à faire
        if (fromRegion.equals(toRegion) && fromPos.equals(toPos)) {
            return propertiesToUpdate;
        }


        PropertyList list = doc.getProperties().getList(SCHEMA_FRAGMENTS);
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
                    if (fromRegion.equals(currentFrag.getString(REGION_IDENTIFIER))) {


                        if (fromPos.equals(currentOrder)) {

                            Integer newOrder = toPos;

                            String moveToOrder = SCHEMA_FRAGMENTS.concat("/").concat(index.toString()).concat("/").concat(FGT_ORDER).concat("=")
                                    .concat(newOrder.toString());

                            propertiesToUpdate.add(moveToOrder);
                        }

                        else if (fromRegion.equals(currentFrag.getString(REGION_IDENTIFIER)) && (currentOrder >= minOrder && currentOrder <= maxOrder)) {

                            Integer newOrder = moveDown ? currentOrder - 1 : currentOrder + 1;
                            String moveToOrder = SCHEMA_FRAGMENTS.concat("/").concat(index.toString()).concat("/").concat(FGT_ORDER).concat("=")
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
                    if ((fromRegion.equals(currentFrag.getString(REGION_IDENTIFIER))) && (fromPos.equals(currentOrder))) {

                        String moveToRegion = SCHEMA_FRAGMENTS.concat("/").concat(index.toString()).concat("/").concat(REGION_IDENTIFIER).concat("=").concat(toRegion);

                        propertiesToUpdate.add(moveToRegion);

                        Integer newOrder = toPos;

                        String moveToOrder = SCHEMA_FRAGMENTS.concat("/").concat(index.toString()).concat("/").concat(FGT_ORDER).concat("=").concat(newOrder.toString());

                        propertiesToUpdate.add(moveToOrder);
                    } else {

                        // Si fgt de la région d'origine et en dessous du fgt
                        // déplacé
                        // Décalage vers le haut
                        if (fromRegion.equals(currentFrag.getString(REGION_IDENTIFIER)) && currentOrder > fromPos) {

                            Integer newOrder = currentOrder - 1;
                            String moveToOrder = SCHEMA_FRAGMENTS.concat("/").concat(index.toString()).concat("/").concat(FGT_ORDER).concat("=")
                                    .concat(newOrder.toString());

                            propertiesToUpdate.add(moveToOrder);
                        }

                        // Si fgt de la région cible est au niveau de la région
                        // déplacée
                        // Décalage vers le bas
                        if (toRegion.equals(currentFrag.getString(REGION_IDENTIFIER))) {

                            if (currentOrder >= toPos) {

                                Integer newOrder = currentOrder + 1;
                                String moveToOrder = SCHEMA_FRAGMENTS.concat("/").concat(index.toString()).concat("/").concat(FGT_ORDER).concat("=")
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
