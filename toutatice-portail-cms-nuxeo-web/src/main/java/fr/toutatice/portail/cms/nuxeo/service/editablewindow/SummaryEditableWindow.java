/**
 * 
 */
package fr.toutatice.portail.cms.nuxeo.service.editablewindow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.Constants;

import fr.toutatice.portail.cms.nuxeo.portlets.fragment.SummaryFragmentModule;


/**
 * @author David Chevrier
 * 
 */
public class SummaryEditableWindow extends EditableWindow {

    /** List schema. */
    public static final String SUMMARY_SCHEMA = "smyfgt:summaryFragment";

    public SummaryEditableWindow(String instancePortlet, String prefixWindow) {
        super(instancePortlet, prefixWindow);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> fillProps(Document doc, PropertyMap fragment, Boolean modeEditionPage) {
        Map<String, String> propsFilled = super.fillGenericProps(doc, fragment, modeEditionPage);
        propsFilled.put(Constants.WINDOW_PROP_URI, doc.getPath());

        PropertyMap mapListe = EditableWindowHelper.findSchemaByRefURI(doc, SUMMARY_SCHEMA, fragment.getString("uri"));

        propsFilled.put("osivia.cms.style", mapListe.getString("view"));
        propsFilled.put("osivia.fragmentTypeId", SummaryFragmentModule.ID);

        return propsFilled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> prepareDelete(Document doc, String refURI) {
        List<String> propertiesToRemove = new ArrayList<String>();

        prepareDeleteGeneric(propertiesToRemove, doc, refURI);

        Integer findIndexByRefURI = EditableWindowHelper.findIndexByRefURI(doc, SUMMARY_SCHEMA, refURI);
        propertiesToRemove.add(SUMMARY_SCHEMA.concat("/").concat(findIndexByRefURI.toString()));

        // Bug automation, supprimer la liste de propriétés par son dernier élément, puis l'avant dernier, etc.
        // sinon décalage des n° d'index dans les propriétés
        Collections.reverse(propertiesToRemove);

        return propertiesToRemove;
    }

}
